package com.u51.a_little_more.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: AbstractCache.java, v 0.1 2018年01月11日 下午6:09:09 alexsong Exp $
 */
public abstract class AbstractCache<K, V> {
    private LoadingCache<K, V> cache;

    public AbstractCache(){
        cache = CacheBuilder.newBuilder()
                .maximumSize(10)
                .build(new CacheLoader<K, V>() {
                    @Override
                    public V load(K k) throws Exception
                    {
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
}
