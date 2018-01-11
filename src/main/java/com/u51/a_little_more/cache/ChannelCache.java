package com.u51.a_little_more.cache;

import com.u51.a_little_more.dataObject.FundChannel;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: ChannelCache.java, v 0.1 2018年01月11日 下午5:20:20 alexsong Exp $
 */

public class ChannelCache extends AbstractCache<String, Object> implements InitializingBean{
    private static final String KEY = "CHANNEL_INFO";
    private volatile Map<String, FundChannel> cacheMap;

    protected List<FundChannel> loadData(String K){
        System.out.println("创建缓存信息");
        if(!K.equals(KEY))
            return null;

        List<FundChannel> list = new ArrayList<>(this.cacheMap.values());

        Collections.reverse(list);

        return list;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        this.cacheMap = new HashMap<>(5);
        this.cacheMap.put("0", new FundChannel("C1","50000","1","1",100));
        this.cacheMap.put("1", new FundChannel("C1","50000","1","1",100));
        this.cacheMap.put("2", new FundChannel("C1","50000","1","1",100));
        this.cacheMap.put("3", new FundChannel("C1","50000","1","1",100));
        this.cacheMap.put("4", new FundChannel("C1","50000","1","1",100));

        refresh(KEY);
    }

}
