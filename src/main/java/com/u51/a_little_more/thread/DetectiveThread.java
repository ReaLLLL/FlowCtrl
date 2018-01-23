package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpClientService;
import com.u51.a_little_more.util.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * <p>心跳检测渠道是否可用</p>
 *
 * @author alexsong
 * @version $Id: DetectiveThread.java, v 0.1 2018年01月10日 上午12:37:37 alexsong Exp $
 */
public class DetectiveThread implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(DetectiveThread.class);

    private static Map<String, Integer> intervalMap;

    static {
        intervalMap = new HashMap<>();
        intervalMap.put("C1", 12);
        intervalMap.put("C2", 12);
        intervalMap.put("C3", 8);
        intervalMap.put("C4", 4);
        intervalMap.put("C5", 4);
    }

    private HttpClientService client;
    private String channel;
    private String reqNo;
    private String token;
    private Map<String, RateLimiter> limiterList;

    public DetectiveThread(HttpClientService client, String channel, String reqNo, String token, Map<String, RateLimiter> limiterList) {
        this.client = client;
        this.channel = channel;
        this.reqNo = reqNo;
        this.token = token;
        this.limiterList = limiterList;
    }

    @Override
    public void run() {
        int interval = intervalMap.get(this.channel);
        while(true){
            try {
                log.info("当前渠道通讯异常，渠道号：{}, 检测线程开始工作，线程号：{}, 检测间隔：{}",this.channel, Thread.currentThread().getName(), interval);

                Thread.sleep(interval*1000);
                String url = HttpUtil.buildUrl(this.channel, this.reqNo, this.token);
                OutBoundResult response = this.client.doGet(url);

                if(response == null || !response.getState().equals(OutBoundStateEnum.SUCCESS))
                    interval = interval > 2 ? interval/2 : 2;
                else{
                    log.info("当前渠道已恢复，渠道编号：{}， 请求编号：{}， 响应信息：{}", channel,this.reqNo, response.getState().getCode());
                    HttpUtil.setChannelState(this.channel, true, this.limiterList);
                    for(String s: limiterList.keySet()){
                        log.info("当前渠道编号：{}，流速：{}",s, limiterList.get(s).getRate());
                    }
                    break;
                }
            }catch (InterruptedException e) {
                log.error("检测线程被中断", e);
            }

        }
    }
}
