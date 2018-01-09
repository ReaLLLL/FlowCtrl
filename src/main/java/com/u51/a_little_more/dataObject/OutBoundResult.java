package com.u51.a_little_more.dataObject;

import java.io.Serializable;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: OutBoundResult.java, v 0.1 2018年01月10日 上午12:01:01 alexsong Exp $
 */
public class OutBoundResult implements Serializable {
    private OutBoundStateEnum state;
    private int channel;
    private long time;

    public OutBoundStateEnum getState() {
        return state;
    }

    public void setState(OutBoundStateEnum state) {
        this.state = state;
    }

    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
}
