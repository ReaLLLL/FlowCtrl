package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.*;
import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpClientService;
import com.u51.a_little_more.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger log = LoggerFactory.getLogger(RequestSendRunnable.class);

    private BlockingQueue<String> queue;

    private Map<String, RateLimiter> limiterList;

    private ListeningExecutorService executorService;

    private String token;

    private Map<String, AtomicInteger> statCount;

    private Map<String, AtomicLong> statTime;

    private CountDownLatch countDownLatch;

    private HttpClientService clientService;

    public RequestSendRunnable(BlockingQueue<String> queue, Map<String, RateLimiter> limiterList,
                               ListeningExecutorService executorService, String token,
                               Map<String, AtomicInteger> statCount, Map<String, AtomicLong> statTime,
                               CountDownLatch countDownLatch, HttpClientService clientService) {
        this.queue = queue;
        this.limiterList = limiterList;
        this.executorService = executorService;
        this.token = token;
        this.statCount = statCount;
        this.statTime = statTime;
        this.countDownLatch = countDownLatch;
        this.clientService = clientService;
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

                //先获取令牌再取渠道
                while(true) {
                    for(String s : this.limiterList.keySet()){
                        if(HttpUtil.isChannelAvailable(s) && this.limiterList.get(s).tryAcquire()){
                            available = true;
                            channel = s;
                            break;
                        }
                    }
                    if(available)
                        break;
                }

//                while(true) {
//                    //获取此时最优渠道排序结果
//                    List<String> channelList = HttpUtil.getChannel();
//                    for(String c : channelList){
//                        //available = limiterList.get(c).tryAcquire(5, TimeUnit.MILLISECONDS);
//                        available = limiterList.get(c).tryAcquire();
//                        if(available){
//                            channel = c;
//                            break;
//                        }
//                    }
//                    if(available)
//                        break;
//                }

                //已获取令牌；
                final ListenableFuture<OutBoundResult> listenableFuture = this.executorService.submit(new OutBoundCallable(channel, ele, limiterList.get(channel), this.token, this.clientService));
                Futures.addCallback(listenableFuture, new FutureCallback<OutBoundResult>() {
                    @Override
                    public void onSuccess(OutBoundResult result) {
                        OutBoundStateEnum state = result.getState();
                        String channel = result.getChannel();
                        double i = 0.0;
                        switch (state){
                            case UNKNOWN:
                                //调低优先级，降低流速
                                log.info("当前请求超时，开始降低流速，渠道编号：{}", channel);
                                i= result.getLimiter().getRate();
                                result.getLimiter().setRate(i/20.0);
                                break;
                            case DUPLICATE_REQUEST:
                            case INVALID_REQUEST:
                            case SERVICE_REJECT:
                            case SERVICE_BUSY:
                                //置渠道不可用，开始心跳检测；
                                if(HttpUtil.setChannelState(channel, false))
                                    new Thread(new DetectiveThread(clientService, channel, result.getReqNo(), token)).start();
                                else {
                                    try {
                                        log.info("将此请求再次放入请求队列，重新路由。");
                                        queue.put(result.getReqNo());
                                    } catch (InterruptedException e) {
                                        log.error("重置请求被中断",e);
                                    }
                                }
                                break;
                            case SUCCESS:
                                if(HttpUtil.isNeedSample()){
                                    countDownLatch.countDown();
                                    statCount.get(channel).getAndIncrement();
                                    statTime.get(channel).addAndGet(result.getTime());
                                }
                                if(result.getLimiter().getRate() <2) {
                                    log.info("当前渠道恢复流速，渠道编号：{}", channel);
                                    i = result.getLimiter().getRate();
                                    result.getLimiter().setRate(i*20.0);
                                }
                                break;
                            case OTHER:
                                //将此请求再次放入请求队列，重新路由
                                try {
                                    queue.put(result.getReqNo());
                                } catch (InterruptedException e) {
                                    log.error("重置请求被中断",e);
                                }
                                break;
                            default:
                                break;
                        }

                    }

                    @Override
                    public void onFailure(Throwable t) {
                        log.error("异步请求远程服务失败", t);
                    }
                });
            } catch (InterruptedException e) {
                log.error("请求路由线程异常中断", e);
            }
        }
    }
}
