package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.*;
import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpClientService;
import com.u51.a_little_more.util.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
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

    private volatile Map<String, RateLimiter> limiterList;

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
                String channel = "";
                boolean available = false;

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

                //===================此处为备选方案====================
                int idx = 0;
                if(HttpUtil.isNeedSample()){
                    idx = Integer.valueOf(ele)%5 + 1;
                    channel = "C"+idx;
                    this.limiterList.get(channel).acquire();
                }else {
                    idx = Integer.valueOf(ele)%5 + 1;
                    channel = "C"+idx;

                    while(!HttpUtil.isChannelAvailable(channel) || !this.limiterList.get(channel).tryAcquire()){

                        List<String> list = HttpUtil.getChannel();

                        if(list.size() == 0){
                            log.error("当前无可用渠道");
                            Thread.sleep(2000);
                            continue;
                        }

                        if(idx > list.size())
                            idx = list.size()-1;
                        else
                            idx = Math.max(0, idx-1);

                        channel = list.get(idx);
                    }
                }

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
                                result.getLimiter().setRate(i/21.0);
                                break;
                            case DUPLICATE_REQUEST:
                                log.error("当前请求重复，请求编号：{}", result.getReqNo());
                                break;
                            case INVALID_REQUEST:
                                log.error("当前请求无效，请求编号：{}", result.getReqNo());
                                break;
                            case SERVICE_REJECT:
                            case SERVICE_BUSY:
                                log.error("当前服务端不可用或者限流，请求编号：{}", result.getReqNo());
                                //置渠道不可用，开始心跳检测；
                                if(HttpUtil.setChannelState(channel, false, limiterList)) {
                                    //动态调整优先级
                                    for(String s: limiterList.keySet()){
                                        log.info("当前渠道编号：{}，流速：{}",s, limiterList.get(s).getRate());
                                    }
                                    new Thread(new DetectiveThread(clientService, channel, result.getReqNo(), token, limiterList)).start();
                                }
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
                                if(result.getLimiter().getRate() <1) {
                                    log.info("当前渠道恢复流速，渠道编号：{}", channel);
                                    i = result.getLimiter().getRate();
                                    result.getLimiter().setRate(i*21.0);
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
