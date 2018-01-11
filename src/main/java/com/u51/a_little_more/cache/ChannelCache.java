package com.u51.a_little_more.cache;

import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: ChannelCache.java, v 0.1 2018年01月11日 下午5:20:20 alexsong Exp $
 */

public class ChannelCache extends AbstractCache<String, List<Integer>> implements InitializingBean{
    private static final String KEY = "CHANNEL_INFO";
    private volatile Map<String, Integer> cacheMap;

    protected List<Integer> loadData(String K){
        System.out.println("创建缓存信息");
        if(!K.equals(KEY))
            return null;

        List<Integer> list = new ArrayList<>(this.cacheMap.values());

        Collections.reverse(list);

        return list;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.cacheMap = new HashMap<>(5);
        this.cacheMap.put("0", 0);
        this.cacheMap.put("1", 1);
        this.cacheMap.put("2", 2);
        this.cacheMap.put("3", 3);
        this.cacheMap.put("4", 4);

        refresh(KEY);
    }

}
