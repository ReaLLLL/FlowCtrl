package com.u51.a_little_more.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: RequestGenRunnable.java, v 0.1 2018年01月09日 下午5:54:54 alexsong Exp $
 */
public class RequestGenRunnable implements Runnable{
    private static final Logger log = LoggerFactory.getLogger(RequestGenRunnable.class);

    private BlockingQueue<String> queue;

    private int num;

    public RequestGenRunnable(BlockingQueue<String> queue, int num) {
        this.queue = queue;
        this.num = num;
    }

    @Override
    public void run() {
        for(int i = 1; i < this.num+1; i++){
            try {
                this.queue.put(String.valueOf(i));
                Thread.sleep(5);
            } catch (InterruptedException e) {
                log.error("请求生成线程异常中断", e);
            }
        }

        log.info("=======请求生成完毕=======");

        while(true){
            int size = this.queue.size();
            log.info("当前剩余待发送请求数量:{}",size);

            if(size == 0)
                break;
            else {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    log.error("请求生成线程异常中断", e);
                }
            }
        }
    }
}
