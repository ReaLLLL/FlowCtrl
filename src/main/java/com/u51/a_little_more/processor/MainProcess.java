package com.u51.a_little_more.processor;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.thread.RequestGenRunnable;
import com.u51.a_little_more.thread.RequestSendRunnable;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: MainProcess.java, v 0.1 2018年01月10日 上午10:46:46 alexsong Exp $
 */
@Configuration
@ImportResource(locations={"classpath:META-INF/spring-context.xml"})
public class MainProcess implements InitializingBean {

    private ThreadPoolTaskExecutor threadPoolForProcess;

    private int requestTotalNum;

    public ThreadPoolTaskExecutor getThreadPoolForProcess() {
        return threadPoolForProcess;
    }

    public void setThreadPoolForProcess(ThreadPoolTaskExecutor threadPoolForProcess) {
        this.threadPoolForProcess = threadPoolForProcess;
    }

    public int getRequestTotalNum() {
        return requestTotalNum;
    }

    public void setRequestTotalNum(int requestTotalNum) {
        this.requestTotalNum = requestTotalNum;
    }

    public void doProcess(){
        System.out.println("主流程处理开始");

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(100));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
        final List<RateLimiter> rateList = new ArrayList<>(5);
        for(int i = 0; i < 5; i++){
            rateList.add(RateLimiter.create(20));
        }

        HttpClient client = HttpClients.createDefault();
        this.threadPoolForProcess.execute(new RequestGenRunnable(queue, this.requestTotalNum));
        for(int i = 0; i < 5; i++){
            this.threadPoolForProcess.execute(new RequestSendRunnable(queue, rateList, executorService, client));
        }

        System.out.println("主流程处理结束");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.doProcess();
    }
}
