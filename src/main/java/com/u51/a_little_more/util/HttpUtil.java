package com.u51.a_little_more.util;

import com.u51.a_little_more.dataObject.FundChannel;
import com.u51.a_little_more.cache.ChannelCache;

import java.util.List;


/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: HttpUtil.java, v 0.1 2018年01月09日 下午9:49:49 alexsong Exp $
 */
public class HttpUtil {
    //生成请求url
    public static String buildUrl(String channelNo, String reqNo, String token){
        return "http://cc.intra.yacolpay.com/"+channelNo+"/apply.htm?reqNo="+reqNo+"&token="+token;
    }

    //获取当前可用渠道优先级列表
    public static List<String> getChannel(){
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        return (List<String>)cache.getCache("CHANNEL_INFO");
    }

    public static boolean isNeedDetect(String channel){
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        return (Boolean)cache.getCache("DETECTIVE_FLAG_"+channel);
    }

    //更新渠道可用信息
    public static void setChannelState(String channel, boolean state) {
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        cache.updateChannelState(channel, state);
        cache.refresh("DETECTIVE_FLAG_"+channel);
        cache.refresh("CHANNEL_INFO");
    }

    //获取采样标志
    public static boolean isNeedSample(){
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        return (Boolean)cache.getCache("CHANNEL_SAMPLE");
    }
    //打开和关闭采样标志
    public static void setChannelSample(boolean state){
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        cache.put("CHANNEL_SAMPLE", state);
    }

    //重新设置渠道优先级
    public static void resetChannel(List<FundChannel> list){
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        cache.resetChannelList(list);
        cache.refresh("CHANNEL_INFO");
    }
}
