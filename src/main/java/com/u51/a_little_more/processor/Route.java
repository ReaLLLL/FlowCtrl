package com.u51.a_little_more.processor;

import com.u51.a_little_more.cache.ChannelCache;
import com.u51.a_little_more.cache.MarkCache;
import com.u51.a_little_more.cache.PriorCache;
import com.u51.a_little_more.dataObject.FundChannel;
import com.u51.a_little_more.util.SpringContextUtil;

import java.util.*;

/**
 * Created by liuty on 2018/1/11.
 */
public class Route {
    private ChannelCache cache_channel = (ChannelCache) SpringContextUtil.getBean("cacheConfig");
    private MarkCache cache_mark = (MarkCache) SpringContextUtil.getBean("cacheConfig");
    private PriorCache cache_prior = (PriorCache) SpringContextUtil.getBean("prior_INFO");
    public static boolean flag = true;

    public void route(){

        Map<String,FundChannel> channelMap = (Map<String,FundChannel>)cache_channel.getCache("CHANNEL_INFO");
        cache_prior.put("prior_INFO",descSort(channelMap));

    }

    public List<FundChannel> descSort(Map<String,FundChannel> channelMap) {
        List<FundChannel> list = new ArrayList<FundChannel>();
        Iterator<String> keys = channelMap.keySet().iterator();
        Map<String,Integer> priorCache = new HashMap<>();
        while (keys.hasNext()) {
            String key = keys.next();
            FundChannel fund = channelMap.get(key);
            //假设1 表示成功
            if(!"1".equals(fund.getState())){
                keys.remove();
                continue;
            }

            int prior;
            if(flag){
                double mark = Double.parseDouble(fund.getMark());
                double time = Double.parseDouble(fund.getTime());
                double tmp = (mark/time)*10000;
                prior = (int)tmp;
            }else {
                prior = cache_mark.getCache("markCache").get(fund.getId());
            }

            priorCache.put(fund.getId(),prior);
            fund.setPrior(prior);
            list.add(fund);
        }

        cache_mark.put("markCache",priorCache);
        Collections.sort(list);

        flag = false;

        return list;
    }

    public static void main(String[] arg){
//        Map<String,FundChannel> channelMap = new HashMap();
//        FundChannel c1 = new FundChannel("c1","8","1","20",1);
//
//        channelMap.put("c1",c1);
//        FundChannel c2 = new FundChannel("c2","9","1","40",1);
//
//        FundChannel c3 = new FundChannel();
//        c3.setId("c3");
//        c3.setMark("10");
//        c3.setState("1");
//        c3.setTime("80");
//        channelMap.put("c3",c3);
//        FundChannel c4 = new FundChannel();
//        c4.setId("c4");
//        c4.setMark("11");
//        c4.setState("1");
//        c4.setTime("100");
//        channelMap.put("c4",c4);
//        FundChannel c5 = new FundChannel();
//        c5.setId("c5");
//        c5.setMark("12");
//        c5.setState("1");
//        c5.setTime("120");
//        channelMap.put("c5",c5);
//        List<FundChannel> list = descSort(channelMap);
//        for(int i = 0 ;i < list.size();i++){
//            FundChannel f = list.get(i);
//            System.out.println("渠道"+f.getId()+"的优先级为："+f.getPrior());
//        }

    }
}
