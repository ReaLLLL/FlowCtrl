package com.u51.a_little_more.dataObject;

import java.io.Serializable;

/**
 * Created by liuty on 2018/1/10.
 */
public class FundChannel implements Serializable, Comparable<FundChannel>{
    /**渠道编号*/
    private String Id;
    /**分数*/
    private String mark;
    /**单比交易平均时间*/
    private String time;
    /**渠道状态 200 Number(10)服务在指令处理成功后，返回该指令的处理时间。单位为毫秒，例：1500，表示本次处理时间为1.5秒
     400 DUPLICATE_REQUEST 重复请求
     401 INVALID_REQUEST 请求校验失败
     403 SERVICE_REJECT 服务限流
     500 SERVICE_BUSY 系统繁忙
     502 UNKNOW 其他失败*/
    private String state;

    /**优先级*/
    private int prior;

//    /**维护时间 总渠道维护不超过10分钟*/
//    private String restTime;

    public FundChannel(String Id,String mark,String time,String state,int prior){
        this.Id= Id;
        this.mark = mark;
        this.time = time;
        this.state = state;
        this.prior = prior;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getMark() {
        return mark;
    }

    public void setMark(String mark) {
        this.mark = mark;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getPrior() {
        return prior;
    }

    public void setPrior(int prior) {
        this.prior = prior;
    }

    @Override
    public int compareTo(FundChannel o) {
        // 按照优先级排序
        /*if (this.prior < o.getPrior()) {
            return (int)(Math.ceil(this.prior - o.getPrior()));
        }
        if (this.prior > o.getPrior()) {
            return (int)(Math.ceil(this.prior - o.getPrior()));
        }
        return 0;*/
        return -(int)(Math.ceil(this.prior - o.getPrior()));
    }
}
