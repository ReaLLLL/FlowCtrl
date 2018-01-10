package com.u51.a_little_more.processor;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.thread.RequestGenRunnable;
import com.u51.a_little_more.thread.RequestSendRunnable;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.InitializingBean;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: MainProcess.java, v 0.1 2018年01月10日 上午10:46:46 alexsong Exp $
 */
public class MainProcess implements InitializingBean {

    private ThreadPoolExecutor threadPoolExecutorForProcess;

    private ListeningExecutorService executorService;

    private int requestTotalNum;

    public void doProcess(){

        BlockingDeque<String> queue = new LinkedBlockingDeque<>(100);
        RateLimiter[] rateLimiters = new RateLimiter[5];
        for(RateLimiter r : rateLimiters){
            r = RateLimiter.create(20);
        }
        HttpClient client = HttpClients.createDefault();
        this.threadPoolExecutorForProcess.execute(new RequestGenRunnable(queue, this.requestTotalNum));
        for(int i = 0; i < 5; i++){
            this.threadPoolExecutorForProcess.execute(new RequestSendRunnable(queue, rateLimiters, this.executorService, client));
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.doProcess();
    }
}
