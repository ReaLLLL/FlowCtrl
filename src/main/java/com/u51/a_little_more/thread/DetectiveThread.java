package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpClientService;
import com.u51.a_little_more.util.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;

import java.util.Random;

/**
 * <p>心跳检测渠道是否可用</p>
 *
 * @author alexsong
 * @version $Id: DetectiveThread.java, v 0.1 2018年01月10日 上午12:37:37 alexsong Exp $
 */
public class DetectiveThread implements Runnable{

    private HttpClientService client;
    private String channel;
    private String reqNo;
    private String token;

    public DetectiveThread(HttpClientService client, String channel, String reqNo, String token) {
        this.client = client;
        this.channel = channel;
        this.reqNo = reqNo;
        this.token = token;
    }

    @Override
    public void run() {
        int interval = 16;
        while(true){
            try {
                System.out.println("当前渠道通讯异常，渠道号："+this.channel +"\n检测线程开始工作，线程号："+Thread.currentThread().getName()+"\n间隔："+interval);

                Thread.sleep(interval*1000);
                String url = HttpUtil.buildUrl(this.channel, this.reqNo, this.token);
                String response = this.client.doGet(url);

                if(response == null || response.length()>6)
                    interval = interval > 5 ? interval/2 : 2;
                else{
                    System.out.println("当前渠道已恢复，渠道编号："+channel);
                    HttpUtil.setChannelState(this.channel, true);
                    //System.out.println(HttpUtil.getChannel());
                    break;
                }
            }catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
}
