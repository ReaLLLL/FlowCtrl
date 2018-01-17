package com.u51.a_little_more.thread;

import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import com.u51.a_little_more.util.HttpClientService;
import com.u51.a_little_more.util.HttpUtil;

import java.util.concurrent.Callable;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: OutBoundCallable.java, v 0.1 2018年01月09日 下午11:54:54 alexsong Exp $
 */
public class OutBoundCallable implements Callable<OutBoundResult> {
    private String channel;

    private String reqNo;

    private RateLimiter limiter;

    private String token;

    private HttpClientService clientService;

    public OutBoundCallable(String channel, String reqNo, RateLimiter limiter, String token, HttpClientService clientService) {
        this.channel = channel;
        this.reqNo = reqNo;
        this.limiter = limiter;
        this.token = token;
        this.clientService = clientService;
    }

    @Override
    public OutBoundResult call() throws Exception {
        OutBoundResult result = new OutBoundResult();
        if(!HttpUtil.isChannelAvailable(this.channel)){
            //路由时渠道可用，实际发送时不可用
//            System.out.print("当前渠道不可用，渠道编号："+this.channel+" 请求编号："+this.reqNo);
            result.setState(OutBoundStateEnum.OTHER);
        }else {
            String url = HttpUtil.buildUrl(this.channel, this.reqNo, this.token);

            long start = System.currentTimeMillis();
            String response = clientService.doGet(url);
            long end = System.currentTimeMillis();

            if(response == null)
                result.setState(OutBoundStateEnum.TIMEOUT);
            else if(response.length()>6)
                result.setState(OutBoundStateEnum.FAILURE);
            else
                result.setState(OutBoundStateEnum.SUCCESS);

            result.setTime(end - start);
            //System.out.println("请求url："+url +" 耗时："+result.getTime());
        }

//        int c = this.channel.charAt(1)-48;
//        Thread.sleep(800+100*c);
//        result.setState(OutBoundStateEnum.SUCCESS);
        result.setReqNo(this.reqNo);
        result.setChannel(this.channel);
        result.setLimiter(this.limiter);

        return result;
    }
}
