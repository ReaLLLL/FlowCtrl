package com.u51.a_little_more.thread;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: RequestGenRunnable.java, v 0.1 2018年01月09日 下午5:54:54 alexsong Exp $
 */
public class RequestGenRunnable implements Runnable{

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
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.print("请求生成完毕");

        while(true){
            int size = this.queue.size();
            System.out.print("当前剩余待发送请求数量：" + size);

            if(size == 0)
                break;
            else {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
