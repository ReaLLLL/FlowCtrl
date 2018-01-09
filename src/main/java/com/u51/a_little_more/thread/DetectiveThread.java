package com.u51.a_little_more.thread;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;

/**
 * <p>心跳检测渠道是否可用</p>
 *
 * @author alexsong
 * @version $Id: DetectiveThread.java, v 0.1 2018年01月10日 上午12:37:37 alexsong Exp $
 */
public class DetectiveThread implements Runnable{

    private HttpClient client;
    private String url;

    public DetectiveThread(HttpClient client, String url) {
        this.client = client;
        this.url = url;
    }

    @Override
    public void run() {
        try {
            HttpResponse response = this.client.execute(new HttpGet(this.url));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
