package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.util.HttpUtil;
import org.apache.http.client.HttpClient;

import java.util.Random;

/**
 * <p>心跳检测渠道是否可用</p>
 *
 * @author alexsong
 * @version $Id: DetectiveThread.java, v 0.1 2018年01月10日 上午12:37:37 alexsong Exp $
 */
public class DetectiveThread implements Runnable{

    private HttpClient client;
    private String channel;
    private String reqNo;
    private RateLimiter limiter;

    public DetectiveThread(HttpClient client, String channel, String reqNo, RateLimiter limiter) {
        this.client = client;
        this.channel = channel;
        this.reqNo = reqNo;
        this.limiter = limiter;
    }

    @Override
    public void run() {
        int interval = 48;
        //String url = HttpUtil.buildUrl(this.channel, this.reqNo);
        while(true){
            try {
                System.out.println("当前渠道通讯异常，渠道号："+this.channel +"\n检测线程开始工作，线程号："+Thread.currentThread().getName()+"\n间隔："+interval);
                Thread.sleep(interval*1000);
                Random rand = new Random();
                int cost = rand.nextInt(8)+1;
                if(cost > 6){
                    HttpUtil.setChannelState(this.channel, true);
                    break;
                }
                else {
                    interval = interval > 5 ? interval/2 : 2;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
