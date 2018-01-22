package com.u51.a_little_more.util;

import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.dataObject.FundChannel;
import com.u51.a_little_more.cache.ChannelCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: HttpUtil.java, v 0.1 2018年01月09日 下午9:49:49 alexsong Exp $
 */
@Configuration
public class HttpUtil {
    private static final Logger log = LoggerFactory.getLogger(HttpUtil.class);

    private static Map<String, String> url = new HashMap<>();
    private static ChannelCache cache = new ChannelCache();

    static {
        init();
        cache.init();
    }

    public Map<String, String> getUrl() {
        return url;
    }

    public void setUrl(Map<String, String> url) {
        HttpUtil.url = url;
    }

    //生成请求url
    public static String buildUrl(String channelNo, String reqNo, String token){
        //return "http://cc.intra.yacolpay.com/"+channelNo+"/apply.htm?reqNo="+reqNo+"&token="+token;
        return url.get(channelNo)+"?reqNo="+reqNo+"&token="+token;
        //return url.get(c)+"/apply.htm?reqNo="+reqNo+"&token="+token;
    }

    //获取当前可用渠道优先级列表
    public static List<String> getChannel(){
        return (List<String>)cache.getCache("CHANNEL_INFO");
    }


    //更新渠道可用信息
    public static boolean setChannelState(String channel, boolean state, Map<String, RateLimiter> limiterList) {
        if(isChannelAvailable(channel) == !state){
            synchronized (HttpUtil.class){
                if(isChannelAvailable(channel) == !state){
                    if(!state){
                        //渠道不可用时的调整
                        if("C4".equals(channel) || "C5".equals(channel)){
                            limiterList.get("C1").setRate(limiterList.get("C1").getRate()+7.5);
                            limiterList.get("C2").setRate(limiterList.get("C2").getRate()+5.0);
                            limiterList.get("C3").setRate(limiterList.get("C3").getRate()+2.5);
                        }

                    }else {
                        //渠道恢复时的调整
                        if("C4".equals(channel) || "C5".equals(channel)){
                            limiterList.get("C1").setRate(limiterList.get("C1").getRate()-7.5);
                            limiterList.get("C2").setRate(limiterList.get("C2").getRate()-5.0);
                            limiterList.get("C3").setRate(limiterList.get("C3").getRate()-2.5);
                        }
                    }
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

    private static void init(){
        try {
            Properties prop = PropertiesLoaderUtils.loadAllProperties("file:application.properties");
            url.put("C1",new String(prop.getProperty("c1.host").getBytes("ISO-8859-1"), "utf-8"));
            url.put("C2",new String(prop.getProperty("c2.host").getBytes("ISO-8859-1"), "utf-8"));
            url.put("C3",new String(prop.getProperty("c3.host").getBytes("ISO-8859-1"), "utf-8"));
            url.put("C4",new String(prop.getProperty("c4.host").getBytes("ISO-8859-1"), "utf-8"));
            url.put("C5",new String(prop.getProperty("c5.host").getBytes("ISO-8859-1"), "utf-8"));
        }catch (IOException e){
            log.error("加载服务端信息失败");
        }
    }
}
