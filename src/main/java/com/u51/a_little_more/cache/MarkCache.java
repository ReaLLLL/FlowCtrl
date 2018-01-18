package com.u51.a_little_more.cache;

import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: ChannelCache.java, v 0.1 2018年01月11日 下午5:20:20 alexsong Exp $
 */

public class MarkCache extends AbstractCache<String, Map<String,Integer>> implements InitializingBean{
    private static final String KEY = "markCache";
    private volatile Map<String, Integer> cacheMap;

    protected Map<String,Integer> loadData(String K){
        if(!K.equals(KEY))
            return null;

        Map<String,Integer> list =  (Map)this.cacheMap.values();

       // Collections.reverse(list);

        return list;
    }

    @Override
    public void afterPropertiesSet() throws Exception {

        refresh(KEY);
    }

}
