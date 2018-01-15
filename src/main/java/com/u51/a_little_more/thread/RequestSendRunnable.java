package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.*;
import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: RequestSendRunnable.java, v 0.1 2018年01月09日 下午6:05:05 alexsong Exp $
 */
public class RequestSendRunnable implements Runnable {
    private BlockingQueue<String> queue;

    private volatile Map<String, RateLimiter> limiterList;

    private ListeningExecutorService executorService;

    private HttpClient client;

    private String token;

    private Map<String, AtomicInteger> statCount;

    private Map<String, AtomicLong> statTime;

    private CountDownLatch countDownLatch;

    public RequestSendRunnable(BlockingQueue<String> queue, Map<String, RateLimiter> limiterList,
                               ListeningExecutorService executorService, HttpClient client,
                               String token, Map<String, AtomicInteger> statCount,
                               Map<String, AtomicLong> statTime, CountDownLatch countDownLatch) {
        this.queue = queue;
        this.limiterList = limiterList;
        this.executorService = executorService;
        this.client = client;
        this.token = token;
        this.statCount = statCount;
        this.statTime = statTime;
        this.countDownLatch = countDownLatch;
    }

    @Override
    public void run() {
        while (true){
            String ele = "";
            try {
                //从待发送请求队列中取出请求信息
                ele = this.queue.take();
                boolean available = false;
                String channel = "";

                while(true) {
                    //获取此时最优渠道排序结果
                    List<String> channelList = HttpUtil.getChannel();
                    for(String c : channelList){
                        available = limiterList.get(c).tryAcquire(5, TimeUnit.MILLISECONDS);
                        if(available){
                            channel = c;
                            break;
                        }
                    }
                    if(available)
                        break;
                }

                //已获取令牌；
                final ListenableFuture<OutBoundResult> listenableFuture = this.executorService.submit(new OutBoundCallable(channel, ele, limiterList.get(channel), this.client, this.token));
                Futures.addCallback(listenableFuture, new FutureCallback<OutBoundResult>() {
                    private HttpClient client = HttpClients.createDefault();
                    @Override
                    public void onSuccess(OutBoundResult result) {
                        OutBoundStateEnum state = result.getState();
                        String channel = result.getChannel();
                        double i = 0.0;
                        switch (state){
                            case TIMEOUT:
                                //调低优先级，降低流速
                                i= result.getLimiter().getRate();
                                result.getLimiter().setRate(i/20.0);
                                break;
                            case FAILURE:
                                //置渠道不可用，开始心跳检测；
                                if(HttpUtil.isNeedDetect(channel)){
                                    HttpUtil.setChannelState(channel, false);
                                    new Thread(new DetectiveThread(this.client, channel, result.getReqNo(), result.getLimiter())).start();
                                }
                                break;
                            case SUCCESS:
                                if(HttpUtil.isNeedSample()){
                                    countDownLatch.countDown();
                                    statCount.get(channel).getAndIncrement();
                                    statTime.get(channel).addAndGet(result.getTime());
                                }
                                if(result.getLimiter().getRate() <1) {
                                    i = result.getLimiter().getRate();
                                    result.getLimiter().setRate(i*20.0);
                                }
                                break;
                            case OTHER:
                                //将此请求再次放入请求队列，重新路由
                                try {
                                    queue.put(result.getReqNo());
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        t.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
