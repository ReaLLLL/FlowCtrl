package com.u51.a_little_more.cache;

import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: ChannelCache.java, v 0.1 2018年01月11日 下午5:20:20 alexsong Exp $
 */
public class ChannelCache extends AbstractCache<String, Object> implements InitializingBean{
    private static final String CHANNEL_INFO_KEY = "CHANNEL_INFO";
    private static final String CHANNEL_DETECTIVE_KEY = "DETECTIVE_FLAG_";
    private static final String CHANNEL_SAMPLE_KEY = "CHANNEL_SAMPLE";
    private volatile Map<String, Integer> cacheMap;
    private volatile Map<String, Boolean> channelAvailableMap;

    protected Object loadData(String key){
        if(key.equals(CHANNEL_INFO_KEY)){
            List<Integer> list = new ArrayList<>(this.cacheMap.values());
            Iterator iter = list.iterator();
            while (iter.hasNext()){
                if(!this.channelAvailableMap.get(CHANNEL_DETECTIVE_KEY+iter.next()))
                    iter.remove();
            }

            Collections.reverse(list);
            return list;

        }else if(key.startsWith(CHANNEL_DETECTIVE_KEY)){
            return this.channelAvailableMap.get(key);
        }else if(key.equals(CHANNEL_SAMPLE_KEY))
            return false;
        else
            return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.cacheMap = new HashMap<>(5);
        this.cacheMap.put("0", 0);
        this.cacheMap.put("1", 1);
        this.cacheMap.put("2", 2);
        this.cacheMap.put("3", 3);
        this.cacheMap.put("4", 4);

        this.channelAvailableMap = new HashMap<>(5);
        this.channelAvailableMap.put("DETECTIVE_FLAG_0", true);
        this.channelAvailableMap.put("DETECTIVE_FLAG_1", true);
        this.channelAvailableMap.put("DETECTIVE_FLAG_2", true);
        this.channelAvailableMap.put("DETECTIVE_FLAG_3", true);
        this.channelAvailableMap.put("DETECTIVE_FLAG_4", true);

        refresh(CHANNEL_INFO_KEY);
        refresh("DETECTIVE_FLAG_0");
        refresh("DETECTIVE_FLAG_1");
        refresh("DETECTIVE_FLAG_2");
        refresh("DETECTIVE_FLAG_3");
        refresh("DETECTIVE_FLAG_4");
        refresh(CHANNEL_SAMPLE_KEY);
    }

    public void updateChannelState(int channel, boolean state){
        this.channelAvailableMap.put(CHANNEL_DETECTIVE_KEY+channel, state);
    }
    //返回采样数据信息
    public void sampleData(String channel, long cost){

    }

}
