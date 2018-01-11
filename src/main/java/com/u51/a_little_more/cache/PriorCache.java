package com.u51.a_little_more.cache;

import com.u51.a_little_more.dataObject.FundChannel;
import org.springframework.beans.factory.InitializingBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by liuty on 2018/1/12.
 */

public class PriorCache extends AbstractCache<String, List<FundChannel>> implements InitializingBean {
        private static final String KEY = "prior_INFO";
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
//            this.cacheMap = new HashMap<>(5);
//            this.cacheMap.put("0", 0);
//            this.cacheMap.put("1", 1);
//            this.cacheMap.put("2", 2);
//            this.cacheMap.put("3", 3);
//            this.cacheMap.put("4", 4);

            refresh(KEY);
        }

}

