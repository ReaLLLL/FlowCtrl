package com.u51.a_little_more.util;

import com.google.common.util.concurrent.RateLimiter;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: HttpUtil.java, v 0.1 2018年01月09日 下午9:49:49 alexsong Exp $
 */
public class HttpUtil {

    //生成请求url
    public static String buildUrl(int channelNo, String reqNo){
        return "http://10.6.20.84:8081/index?requestNo="+reqNo+"&token=ABCd588";
    }

    //获取当前可用渠道优先级列表
    public static List<Integer> getChannel(){
        List<Integer> list = new ArrayList<>();
        list.add(4);
        list.add(3);
        list.add(2);
        list.add(1);
        list.add(0);
        return list;
    }

    //更新渠道可用信息
    public static void recoverChannelState(RateLimiter limiter, int channel){
        limiter.setRate(20);
    }
}