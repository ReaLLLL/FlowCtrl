package com.u51.a_little_more.processor;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.dataObject.FundChannel;
import com.u51.a_little_more.thread.RequestGenRunnable;
import com.u51.a_little_more.thread.RequestSendRunnable;
import com.u51.a_little_more.util.HttpUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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

    private String token;

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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void doProcess() throws Exception {
        System.out.println("===========主流程开始准备!!!===========");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long current = System.currentTimeMillis();
        long start = sdf.parse(this.getStartTime()).getTime();
        HttpClient client = HttpClients.createDefault();

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(100));
        BlockingQueue<String> queue = new ArrayBlockingQueue<>(100);
        Map<String, RateLimiter> rateMap = new HashMap<>();
        Map<String, AtomicInteger> statCount = new HashMap<>();
        Map<String, AtomicLong> statTime = new HashMap<>();
        CountDownLatch count = new CountDownLatch(500);

        for(int i = 1; i < 6; i++){

            rateMap.put("C"+i, RateLimiter.create((1<<(i-1))+4));
            statCount.put("C"+i, new AtomicInteger(0));
            statTime.put("C"+i, new AtomicLong(0L));
        }

        while (current < start){
            System.out.println("倒计时："+(start-current)/1000);
            Thread.sleep(1000);
            current = System.currentTimeMillis();
        }

        System.out.println("===========Let's go!!!=============");

        this.threadPoolForProcess.execute(new RequestGenRunnable(queue, this.requestTotalNum));
        for(int i = 0; i < 5; i++){
            this.threadPoolForProcess.execute(new RequestSendRunnable(queue, rateMap, executorService, client, token, statCount, statTime, count));
        }

        System.out.println("===========开始渠道处理耗时采样!!!===========");
        HttpUtil.setChannelSample(true);

        count.await();
        List<FundChannel> list = new ArrayList<>();
        for(int i = 1; i<6; i++){
            System.out.println("当前渠道编号：C"+i);
            System.out.println(" 总交易笔数："+ statCount.get("C"+i).get());
            System.out.println(" 总交易耗时："+ statTime.get("C"+i).get());
            list.add(new FundChannel("C"+i, 7000+1000*i, statTime.get("C"+i).get()/statCount.get("C"+i).get(),"",1));
        }

        HttpUtil.setChannelSample(false);

        System.out.println("===========渠道处理耗时采样结束,开始更新缓存信息!!!===========");
        HttpUtil.resetChannel(list);
        List<String> l = HttpUtil.getChannel();
        for(String s : l){
            rateMap.get(s).setRate(1<<(4-l.indexOf(s))+1);
        }
        System.out.println("===========更新缓存信息结束!!!===========");

        System.out.println("===========主流程处理结束!!!===========");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.doProcess();
    }
}
