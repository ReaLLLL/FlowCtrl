package com.u51.a_little_more.util;

import com.u51.a_little_more.dataObject.FundChannel;
import com.u51.a_little_more.cache.ChannelCache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: HttpUtil.java, v 0.1 2018年01月09日 下午9:49:49 alexsong Exp $
 */
public class HttpUtil {
    private static Map<Integer, String> url = new HashMap<>();
    private static ChannelCache cache = new ChannelCache();

    static {
        url.put(1, "http://10.6.20.67:8081/hello/c1");
        url.put(2, "http://10.6.20.67:8081/hello/c2");
        url.put(3, "http://10.6.20.67:8081/hello/c3");
        url.put(4, "http://10.6.20.67:8081/hello/c4");
        url.put(5, "http://10.6.20.67:8081/hello/c5");

//        url.put(1, "http://10.6.200.56:8081/");
//        url.put(2, "http://10.6.200.56:8081/");
//        url.put(3, "http://10.6.200.56:8081/");
//        url.put(4, "http://10.6.200.56:8081/");
//        url.put(5, "http://10.6.200.56:8081/");

//        url.put(1, "http://test.hello51world.yacolpay.com/ccmchannel2/");
//        url.put(2, "http://test.hello51world.yacolpay.com/ccmchannel2/");
//        url.put(3, "http://test.hello51world.yacolpay.com/ccmchannel3/");
//        url.put(4, "http://test.hello51world.yacolpay.com/ccmchannel4/");
//        url.put(5, "http://test.hello51world.yacolpay.com/ccmchannel5/");

        cache.init();
    }
    //生成请求url
    public static String buildUrl(String channelNo, String reqNo, String token){
        //return "http://cc.intra.yacolpay.com/"+channelNo+"/apply.htm?reqNo="+reqNo+"&token="+token;
        int c = channelNo.charAt(1)-48;
        return url.get(c)+"?reqNo="+reqNo+"&token="+token;
        //return url.get(c)+"/apply.htm?reqNo="+reqNo+"&token="+token;
    }

    //获取当前可用渠道优先级列表
    public static List<String> getChannel(){
        return (List<String>)cache.getCache("CHANNEL_INFO");
    }


    //更新渠道可用信息
    public static boolean setChannelState(String channel, boolean state) {
        if(isChannelAvailable(channel) == !state){
            synchronized (channel){
                if(isChannelAvailable(channel) == !state){
                    cache.updateChannelState(channel, state);
                    cache.remove("CHANNEL_INFO");
                    cache.remove("AVAILABLE_FLAG_"+channel);
                    cache.remove("DETECTIVE_FLAG_"+channel);

                    return true;
                }
            }
        }

        return false;
    }

    //获取当前渠道是否可用
    public static boolean isChannelAvailable(String channel){
        return (boolean)cache.getCache("AVAILABLE_FLAG_"+channel);
    }

    //获取采样标志
    public static boolean isNeedSample(){
        return (Boolean)cache.getCache("CHANNEL_SAMPLE");
    }
    //打开和关闭采样标志
    public static void setChannelSample(boolean state){
        cache.put("CHANNEL_SAMPLE", state);
    }

    //重新设置渠道优先级
    public static void resetChannel(List<FundChannel> list){
        cache.resetChannelList(list);
        cache.remove("CHANNEL_INFO");
    }
}
