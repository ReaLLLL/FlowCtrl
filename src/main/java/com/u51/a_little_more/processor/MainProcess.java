package com.u51.a_little_more.processor;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.dataObject.FundChannel;
import com.u51.a_little_more.thread.RequestGenRunnable;
import com.u51.a_little_more.thread.RequestSendRunnable;
import com.u51.a_little_more.util.HttpClientService;
import com.u51.a_little_more.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(MainProcess.class);

    private ThreadPoolTaskExecutor threadPoolForProcess;

    private int requestTotalNum;

    private String startTime;

    private String token;

    private HttpClientService clientService;

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

    public HttpClientService getClientService() {
        return clientService;
    }

    public void setClientService(HttpClientService clientService) {
        this.clientService = clientService;
    }

    public void doProcess() throws Exception {

        log.info("===========主流程开始准备!!!===========");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        long current = System.currentTimeMillis();
        long start = sdf.parse(this.getStartTime()).getTime();

        ListeningExecutorService executorService = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(400));
        BlockingQueue<String> queue = new LinkedBlockingQueue<>();
        Map<String, RateLimiter> rateMap = new HashMap<>();
        Map<String, AtomicInteger> statCount = new HashMap<>();
        Map<String, AtomicLong> statTime = new HashMap<>();
        CountDownLatch count = new CountDownLatch(3000);

        for(int i = 1; i < 6; i++){

            rateMap.put("C"+i, RateLimiter.create(20));
            statCount.put("C"+i, new AtomicInteger(0));
            statTime.put("C"+i, new AtomicLong(0L));
        }

        while (current < start){
            log.info("倒计时：{}", (start-current)/1000);
            Thread.sleep(1000);
            current = System.currentTimeMillis();
        }

        log.info("===========Let's go!!!=============");

        this.threadPoolForProcess.execute(new RequestGenRunnable(queue, this.requestTotalNum));
        for(int i = 0; i < 5; i++){
            this.threadPoolForProcess.execute(new RequestSendRunnable(queue, rateMap, executorService, token, statCount, statTime, count, this.clientService));
        }

        log.info("===========开始渠道处理耗时采样!!!===========");

        HttpUtil.setChannelSample(true);
        log.info("预先设定的优先级：{}", HttpUtil.getChannel());

        count.await();
        List<FundChannel> list = new ArrayList<>();
        for(int i = 1; i<6; i++){
            log.info("当前渠道编号:{}, 请求总笔数:{}, 请求总耗时:{}",i,statCount.get("C"+i).get(),statTime.get("C"+i).get());
            list.add(new FundChannel("C"+i, 1000*(7+i)*statCount.get("C"+i).get(), statTime.get("C"+i).get(),"",1));
        }

        HttpUtil.setChannelSample(false);

        log.info("===========渠道处理耗时采样结束,开始更新缓存信息!!!===========");

        HttpUtil.resetChannel(list);
        List<String> l = HttpUtil.getChannel();

        rateMap.get("C1").setRate(10.0);
        rateMap.get("C2").setRate(15.0);
        rateMap.get("C3").setRate(15.0);
        rateMap.get("C4").setRate(20.0);
        rateMap.get("C5").setRate(20.0);

        log.info("重新设定的优先级：{}", l);

        log.info("===========更新缓存信息结束!!!===========");
        log.info("============主流程处理结束!!!============");
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.doProcess();
    }
}
