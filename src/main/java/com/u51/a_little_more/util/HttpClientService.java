package com.u51.a_little_more.util;

import com.u51.a_little_more.dataObject.OutBoundResult;
import com.u51.a_little_more.dataObject.OutBoundStateEnum;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Random;

/**
 * <p>注释</p>
 *
 * @author alexsong
 * @version $Id: HttpClientService.java, v 0.1 2018年01月17日 下午3:11:11 alexsong Exp $
 */

@Service("httpClientService")
public class HttpClientService {

    private static final Logger log = LoggerFactory.getLogger(HttpClientService.class);

    @Autowired
    private CloseableHttpClient httpClient;

    @Autowired
    private RequestConfig requestConfig;

    public CloseableHttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public RequestConfig getRequestConfig() {
        return requestConfig;
    }

    public void setRequestConfig(RequestConfig requestConfig) {
        this.requestConfig = requestConfig;
    }

    public OutBoundResult doGet(String url) {
        log.info("请求URL：{}",url);
        HttpGet httpGet = new HttpGet(url);
        httpGet.setConfig(this.requestConfig);

        CloseableHttpResponse response = null;
        OutBoundResult result = new OutBoundResult();
        try {
            // 执行请求
            response = httpClient.execute(httpGet);

            if (response != null) {
                int code = response.getStatusLine().getStatusCode();
                //int code = Integer.valueOf(EntityUtils.toString(response.getEntity()));
                switch (code){
                    case 200:
                        result.setState(OutBoundStateEnum.SUCCESS);
                        break;
                    case 400:
                        result.setState(OutBoundStateEnum.DUPLICATE_REQUEST);
                        break;
                    case 402:
                        result.setState(OutBoundStateEnum.INVALID_REQUEST);
                        break;
                    case 403:
                        result.setState(OutBoundStateEnum.SERVICE_REJECT);
                        break;
                    case 500:
                        result.setState(OutBoundStateEnum.SERVICE_BUSY);
                        break;
                    case 502:
                        result.setState(OutBoundStateEnum.UNKNOWN);
                        break;
                }

                return result;
            }
        }
        catch(IOException ioe){
            log.error("服务端通信异常", ioe);
        } finally {
            if (response != null) {
                try {
                    response.close();
                } catch (IOException e) {
                    log.error("关闭连接异常", e);
                }
            }
        }
        return null;
    }

    public OutBoundResult doGetForTest(String url){
        OutBoundResult result = new OutBoundResult();

        Random rand = new Random();
        int s = rand.nextInt(3)+3;
        try{
            Thread.sleep(3 * 1000);
        }catch (InterruptedException e){
            log.error("线程中断", e);
        }

        result.setState(OutBoundStateEnum.SUCCESS);

        return result;
    }
}
