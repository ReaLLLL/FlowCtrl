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
            result.setState(OutBoundStateEnum.OTHER);
        }else {
            String url = HttpUtil.buildUrl(this.channel, this.reqNo, this.token);

            long start = System.currentTimeMillis();
            result = clientService.doGet(url);
            //result = clientService.doGetForTest(url);
            long end = System.currentTimeMillis();

            if(result == null)
                result.setState(OutBoundStateEnum.UNKNOWN);
            else {
                if(result.getState().equals(OutBoundStateEnum.SUCCESS))
                    result.setTime(end-start);
            }
        }
        result.setReqNo(this.reqNo);
        result.setChannel(this.channel);
        result.setLimiter(this.limiter);

        return result;
    }
}
