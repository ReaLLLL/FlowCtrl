package com.u51.a_little_more.cache;

import com.u51.a_little_more.dataObject.FundChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.*;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: ChannelCache.java, v 0.1 2018年01月11日 下午5:20:20 alexsong Exp $
 */
public class ChannelCache extends AbstractCache<String, Object>{
    private static final Logger log = LoggerFactory.getLogger(ChannelCache.class);
    private static final String CHANNEL_INFO_KEY = "CHANNEL_INFO";
    private static final String CHANNEL_AVAILABLE_KEY = "AVAILABLE_FLAG_";
    private static final String CHANNEL_DETECTIVE_KEY = "DETECTIVE_FLAG_";
    private static final String CHANNEL_SAMPLE_KEY = "CHANNEL_SAMPLE";
    private List<FundChannel> channelList;
    private Map<String, Boolean> channelAvailableMap;
    private Map<String, Boolean> detectiveMap;

    protected Object loadData(String key){
        if(key.equals(CHANNEL_INFO_KEY)){
            Collections.sort(this.channelList, new Comparator<FundChannel>() {
                @Override
                public int compare(FundChannel o1, FundChannel o2) {
                    if(o1.getPrior() > o2.getPrior())
                        return 1;
                    else
                        return -1;
                }
            });

            List<String> list = new ArrayList<>();
            Iterator iter = channelList.iterator();
            while (iter.hasNext()){
                FundChannel f = (FundChannel)iter.next();
                if(this.channelAvailableMap.get(CHANNEL_AVAILABLE_KEY+f.getId()))
                    list.add(f.getId());
            }

            return list;
        }else if(key.startsWith(CHANNEL_AVAILABLE_KEY)){
            return this.channelAvailableMap.get(key);
        } else if(key.startsWith(CHANNEL_DETECTIVE_KEY)){
            return this.detectiveMap.get(key);
        }else if(key.equals(CHANNEL_SAMPLE_KEY))
            return false;
        else
            return null;
    }

    public void init() {

        this.channelList = new ArrayList<>(5);
        this.channelList.add(new FundChannel("C1",8000,1000,"200",1));
        this.channelList.add(new FundChannel("C2",9000,1000,"200",2));
        this.channelList.add(new FundChannel("C3",10000,1000,"200",3));
        this.channelList.add(new FundChannel("C4",11000,1000,"200",4));
        this.channelList.add(new FundChannel("C5",12000,1000,"200",5));


        this.channelAvailableMap = new HashMap<>(5);
        this.channelAvailableMap.put("AVAILABLE_FLAG_C1", true);
        this.channelAvailableMap.put("AVAILABLE_FLAG_C2", true);
        this.channelAvailableMap.put("AVAILABLE_FLAG_C3", true);
        this.channelAvailableMap.put("AVAILABLE_FLAG_C4", true);
        this.channelAvailableMap.put("AVAILABLE_FLAG_C5", true);

        this.detectiveMap = new HashMap<>(5);
        this.detectiveMap.put("DETECTIVE_FLAG_C1", true);
        this.detectiveMap.put("DETECTIVE_FLAG_C2", true);
        this.detectiveMap.put("DETECTIVE_FLAG_C3", true);
        this.detectiveMap.put("DETECTIVE_FLAG_C4", true);
        this.detectiveMap.put("DETECTIVE_FLAG_C5", true);

        refresh(CHANNEL_INFO_KEY);
        refresh("AVAILABLE_FLAG_C1");
        refresh("AVAILABLE_FLAG_C2");
        refresh("AVAILABLE_FLAG_C3");
        refresh("AVAILABLE_FLAG_C4");
        refresh("AVAILABLE_FLAG_C5");

        refresh("DETECTIVE_FLAG_C1");
        refresh("DETECTIVE_FLAG_C2");
        refresh("DETECTIVE_FLAG_C3");
        refresh("DETECTIVE_FLAG_C4");
        refresh("DETECTIVE_FLAG_C5");

        refresh(CHANNEL_SAMPLE_KEY);
    }

    public void updateChannelState(String channel, boolean state){
        this.channelAvailableMap.put(CHANNEL_AVAILABLE_KEY+channel, state);
        this.detectiveMap.put(CHANNEL_DETECTIVE_KEY+channel, state);
    }
    //返回采样数据信息
    public void resetChannelList(List<FundChannel> SampleList){
        this.channelList.clear();
        this.channelList.addAll(SampleList);
        for(FundChannel f : this.channelList){
            int i = this.channelList.indexOf(f);
            double prior = Math.exp(-i/5)*f.getMark()/f.getTime();
            f.setPrior(prior);
            log.info(f.toString());
        }
    }
}
