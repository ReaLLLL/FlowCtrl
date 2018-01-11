package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.*;
import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpUtil;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: RequestSendRunnable.java, v 0.1 2018年01月09日 下午6:05:05 alexsong Exp $
 */
public class RequestSendRunnable implements Runnable {
    private BlockingQueue<String> queue;

    private List<RateLimiter> limiterList;

    private ListeningExecutorService executorService;

    private HttpClient client;

    public RequestSendRunnable(BlockingQueue<String> queue, List<RateLimiter> limiterList, ListeningExecutorService executorService, HttpClient client) {
        this.queue = queue;
        this.limiterList = limiterList;
        this.executorService = executorService;
        this.client = client;
    }

    @Override
    public void run() {
        while (true){
            String ele = "";
            try {
                //从待发送请求队列中取出请求信息
                ele = this.queue.take();
                boolean available = false;
                int channel = 0;

                while(true) {
                    //获取此时最优渠道排序结果
                    List<Integer> channelList = HttpUtil.getChannel();
                    for(int i : channelList){
                        available = limiterList.get(i).tryAcquire(5, TimeUnit.MILLISECONDS);
                        if(available){
                            channel = i;
                            break;
                        }
                    }
                    if(available)
                        break;
                }

                //已获取令牌；
                final ListenableFuture<OutBoundResult> listenableFuture = this.executorService.submit(new OutBoundCallable(channel, ele, limiterList.get(channel), this.client));
                Futures.addCallback(listenableFuture, new FutureCallback<OutBoundResult>() {
                    private HttpClient client = HttpClients.createDefault();
                    @Override
                    public void onSuccess(OutBoundResult result) {
                        OutBoundStateEnum state = result.getState();
                        switch (state){
                            case TIMEOUT:
                                //调低优先级，降低流速
                                result.getLimiter().setRate(0.2);
                                break;
                            case FAILURE:
                                //置渠道不可用，开始心跳检测；
                                result.getLimiter().setRate(0.02);
                                new Thread(new DetectiveThread(this.client, result.getChannel(), result.getReqNo(), result.getLimiter())).start();
                                break;
                            case SUCCESS:
                                if(result.getLimiter().getRate() != 20.0)
                                    result.getLimiter().setRate(5*(result.getChannel()+1));
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
