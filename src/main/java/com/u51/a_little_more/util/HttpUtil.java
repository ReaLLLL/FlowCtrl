package com.u51.a_little_more.util;

<<<<<<< HEAD
import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.cache.PriorCache;
import com.u51.a_little_more.dataObject.FundChannel;
=======
import com.u51.a_little_more.cache.ChannelCache;
>>>>>>> 7597ed6d4298b1cfdb99f3f9617689e75e9dc91e

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
        return "http://10.6.20.84:808"+channelNo+"/index?reqNo="+reqNo+"&token=ABCd589";
    }

    //获取当前可用渠道优先级列表
    public static List<Integer> getChannel(){
<<<<<<< HEAD
        List<Integer> priorList = new ArrayList<>();
        PriorCache cache = (PriorCache)SpringContextUtil.getBean("cacheConfig");
        List<FundChannel> fundChannels = cache.getCache("prior_INFO");
        for(int i=0;i<fundChannels.size();i++){
            priorList.add(Integer.parseInt(fundChannels.get(i).getId()));
        }
        return priorList;
=======
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        return (List<Integer>)cache.getCache("CHANNEL_INFO");
    }

    public static boolean isNeedDetect(int channel){
        ChannelCache cache = (ChannelCache)SpringContextUtil.getBean("cacheConfig");
        return (Boolean)cache.getCache("DETECTIVE_FLAG_"+channel);
>>>>>>> 7597ed6d4298b1cfdb99f3f9617689e75e9dc91e
    }

    //更新渠道可用信息
    public static void setChannelState(int channel, boolean state) {
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
}
