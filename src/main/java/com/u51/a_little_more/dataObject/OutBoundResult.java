package com.u51.a_little_more.dataObject;

import com.google.common.util.concurrent.RateLimiter;

import java.io.Serializable;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: OutBoundResult.java, v 0.1 2018年01月10日 上午12:01:01 alexsong Exp $
 */
public class OutBoundResult implements Serializable {
    private OutBoundStateEnum state;
    private String reqNo;
    private String channel;
    private long time = 1000;
    private RateLimiter limiter;

    public OutBoundStateEnum getState() {
        return state;
    }

    public void setState(OutBoundStateEnum state) {
        this.state = state;
    }

    public String getReqNo() {
        return reqNo;
    }

    public void setReqNo(String reqNo) {
        this.reqNo = reqNo;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public RateLimiter getLimiter() {
        return limiter;
    }

    public void setLimiter(RateLimiter limiter) {
        this.limiter = limiter;
    }
}
