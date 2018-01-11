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

import java.text.SimpleDateFormat;
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
public class MainProcess implements InitializingBean {

    private ThreadPoolTaskExecutor threadPoolForProcess;

    private int requestTotalNum;

    private String startTime;

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

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void doProcess() throws Exception {
        System.out.println("===========主流程开始准备!!!===========");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long current = System.currentTimeMillis();
        long start = sdf.parse(this.getStartTime()).getTime();
        HttpClient client = HttpClients.createDefault();

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(100));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
        final List<RateLimiter> rateList = new ArrayList<>(5);
        for(int i = 0; i < 5; i++){
            rateList.add(RateLimiter.create((1<<i)+4));
        }

        while (current < start){
            System.out.println("倒计时："+(start-current)/1000);
            Thread.sleep(1000);
            current = System.currentTimeMillis();
        }

        System.out.println("===========Let's go!!!=============");

        this.threadPoolForProcess.execute(new RequestGenRunnable(queue, this.requestTotalNum));
        for(int i = 0; i < 5; i++){
            this.threadPoolForProcess.execute(new RequestSendRunnable(queue, rateList, executorService, client));
        }

        System.out.println("===========主流程处理结束!!!===========");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.doProcess();
    }
}
