package com.sqldd.crawler;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.entity.mime.MultipartEntity;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

public class MyRequestSet{
    public HttpGet getMethod(String url) throws UnsupportedEncodingException {
        //创建一个get请求方法
        URLEncoder.encode(url, "utf-8");
        HttpGet getmethod = new HttpGet(url);
        //设置请求超时时间等
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(30000).setConnectTimeout(30000).setSocketTimeout(30000).build();
        //设置请求头,主要是user-agent
        getmethod.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        //设置请求参数
        getmethod.setConfig(requestConfig);
        return getmethod;
    }

    public HttpGet getMethodWithParams(String url,HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();
        builder.append(url + "?");
        for (String key : params.keySet()) {
            String value = params.get(key);
            String encode = URLEncoder.encode(value, "utf-8");
            //解码%25=>%
            String valueEncodeMine = encodeMine(encode);
            builder.append(key + "=" + encode + "&");
        }
        builder.deleteCharAt(builder.length()-1);
        System.out.println("#url#  "+builder.toString());
        HttpGet getmethod = new MyRequestSet().getMethod(builder.toString());
        return getmethod;
    }

    //自定义编码函数
    public String encodeMine(String value){
        //只转义' 空格 # /
        StringBuilder valuetmp = new StringBuilder(value);
        while(valuetmp.indexOf("%25") > 0){
            valuetmp.replace(valuetmp.indexOf("%25"), valuetmp.indexOf("%25")+3, "%");
        }
        return valuetmp.toString();
    }

    public HttpPost postMethodWithParams(String url,HashMap<String,String> params) throws UnsupportedEncodingException {
        URLEncoder.encode(url, "utf-8");
        HttpPost postmethod = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectionRequestTimeout(30000).setConnectTimeout(30000).setSocketTimeout(30000).build();
        postmethod.addHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36");
        postmethod.setConfig(requestConfig);

        // 构建消息实体
        String KV = "";
        MultipartEntity entity = new MultipartEntity();
        for (String key : params.keySet()) {
            String value = params.get(key);
            //String encode = URLEncoder.encode(value, "utf-8");
            //解码%25=>%
            //String valueEncodeMine = encodeMine(encode);
            entity.addPart(key,new StringBody(value));
            KV+=key+"="+value+"\n";
        }
        System.out.println("#url#  "+url +"\n" + KV.substring(0, KV.length() - 1));
        /*
        entity.setContentEncoding("UTF-8");
        // 发送Json格式的数据请求
        //entity.setContentType("application/json");
        //发送接收数据类型为text/html的数据请求
        entity.setContentType("application/x-www-form-urlencoded");*/

        postmethod.setEntity(entity);
        return postmethod;
    }
}
