package com.u51.a_little_more.util;

import com.google.common.util.concurrent.RateLimiter;
import com.u51.a_little_more.cache.PriorCache;
import com.u51.a_little_more.dataObject.FundChannel;

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
        List<Integer> priorList = new ArrayList<>();
        PriorCache cache = (PriorCache)SpringContextUtil.getBean("cacheConfig");
        List<FundChannel> fundChannels = cache.getCache("prior_INFO");
        for(int i=0;i<fundChannels.size();i++){
            priorList.add(Integer.parseInt(fundChannels.get(i).getId()));
        }
        return priorList;
    }

    //更新渠道可用信息
    public static void recoverChannelState(RateLimiter limiter, int channel){
        limiter.setRate(20);
    }
}
