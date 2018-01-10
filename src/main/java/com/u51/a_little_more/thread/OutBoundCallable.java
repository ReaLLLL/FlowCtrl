package com.u51.a_little_more.thread;

import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.util.concurrent.Callable;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: OutBoundCallable.java, v 0.1 2018年01月09日 下午11:54:54 alexsong Exp $
 */
public class OutBoundCallable implements Callable<OutBoundResult> {
    private String url;

    private HttpClient client;

    public OutBoundCallable(String url, HttpClient client) {
        this.url = url;
        this.client = client;
    }

    @Override
    public OutBoundResult call() throws Exception {
        OutBoundResult result = new OutBoundResult();
        //HttpResponse response = this.client.execute(new HttpGet(this.url));
        //TODO 解析response信息

        System.out.println(this.url);
        result.setState(OutBoundStateEnum.SUCCESS);
        result.setChannel(1);
        result.setTime(1000);
        return result;
    }
}
