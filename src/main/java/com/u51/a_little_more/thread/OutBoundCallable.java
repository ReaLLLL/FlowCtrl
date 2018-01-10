package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpUtil;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.Callable;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: OutBoundCallable.java, v 0.1 2018年01月09日 下午11:54:54 alexsong Exp $
 */
public class OutBoundCallable implements Callable<OutBoundResult> {
    private int channel;

    private String reqNo;

    private RateLimiter limiter;

    private HttpClient client;

    public OutBoundCallable(int channel, String reqNo, RateLimiter limiter, HttpClient client) {
        this.channel = channel;
        this.reqNo = reqNo;
        this.limiter = limiter;
        this.client = client;
    }

    @Override
    public OutBoundResult call() throws Exception {
        long start = System.currentTimeMillis();
        OutBoundResult result = new OutBoundResult();
        HttpResponse response = this.client.execute(new HttpGet(HttpUtil.buildUrl(this.channel, this.reqNo)));
        if(response == null)
            result.setState(OutBoundStateEnum.TIMEOUT);
        else if(EntityUtils.toString(response.getEntity()).equals("200"))
            result.setState(OutBoundStateEnum.SUCCESS);
        else
            result.setState(OutBoundStateEnum.FAILURE);
        result.setChannel(this.channel);
        result.setTime(System.currentTimeMillis() - start);
        result.setLimiter(this.limiter);
        return result;
    }

//    public static void main(String[] args) throws IOException {
//        HttpClient client = HttpClients.createDefault();
//        HttpResponse response = client.execute(new HttpGet("http://10.6.20.84:8081/index?requestNo=1&token=ABCd588"));
//
//        System.out.println(EntityUtils.toString(response.getEntity()));
//    }
}
