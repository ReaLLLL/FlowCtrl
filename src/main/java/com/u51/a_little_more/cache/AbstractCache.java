package com.u51.a_little_more.cache;

import com.google.common.cache.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: AbstractCache.java, v 0.1 2018年01月11日 下午6:09:09 alexsong Exp $
 */
public abstract class AbstractCache<K, V> {
    private static final Logger log = LoggerFactory.getLogger(AbstractCache.class);
    private LoadingCache<K, V> cache;

    public AbstractCache(){
        cache = CacheBuilder.newBuilder()
                .refreshAfterWrite(1, TimeUnit.MINUTES)
                .expireAfterAccess(2, TimeUnit.MINUTES)
                .maximumSize(50)
                .removalListener(new RemovalListener<K, V>() {
                    @Override
                    public void onRemoval(RemovalNotification<K, V> notification) {
                        log.info("缓存数据被移除");
                    }
                })
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K k) throws Exception
                    {
                        log.info("缓存数据被加载");
                        return loadData(k);
                    }
                });
    }

    protected abstract V loadData(K k);

    public V getCache(K param)
    {
        return cache.getUnchecked(param);
    }

    public void refresh(K k)
    {
        cache.refresh(k);
    }

    public void put(K k, V v)
    {
        cache.put(k, v);
    }

    public void remove(K k){
        cache.invalidate(k);
    }
}
