package com.sqldd.crawler;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Time;
import java.util.HashMap;

/**
 * 根据一个url下载一个html页面
 **/
public class HtmlDownloader {
    MyRequestSet requestset = null;
    public HtmlDownloader(MyRequestSet requestset){
        this.requestset = requestset;
    }
    public String simpleDownloadhtml1time(String url){
        String html = null;
        //创建一个客户端
        //创建一个读取流从entity读取html
        BufferedReader reader = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = null;
        try {
            response = httpclient.execute(requestset.getMethod(url));
            HttpEntity entity = response.getEntity();
            reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder sb = new StringBuilder();
            while((html = reader.readLine()) != null){
                sb.append(html);
            }
            html = sb.toString();
            //System.out.println("HtmlDownloader-simpleDownloadhtml-获取成功");
        }
        catch (IOException e) {
            //System.out.println(url+"-HtmlDownloader-simpleDownloadhtml-连接失败");
        }
        finally{
            if(reader != null){
                try {
                    reader.close();
                    httpclient.close();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return html;
    }
    //连接失败重传
    public String simpleDownloadhtml(String url){
        String html = null;
        int time = 0;
        while(html == null && time < 10){
            time++;
            html = simpleDownloadhtml1time(url);
        }
        return html;
    }

    public String downloadHtmlWithParamsGet1time(String url, HashMap<String,String> params){
        String html = "";
        //创建一个客户端
        //创建一个读取流从entity读取html
        BufferedReader reader = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = null;
        try {
            response = httpclient.execute(requestset.getMethodWithParams(url,params));
            HttpEntity entity = response.getEntity();
            reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder sb = new StringBuilder();
            while((html = reader.readLine()) != null){
                sb.append(html);
            }
            html = sb.toString();
            System.out.println("HtmlDownloader-downloadHtmlWithParamsGet-获取成功");
        }
        catch (IOException e) {
            System.out.println(url+"-HtmlDownloader-downloadHtmlWithParamsGet-连接失败");
        }
        finally{
            if(reader != null){
                try {
                    reader.close();
                    httpclient.close();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return html;
    }
    //连接失败重传
    public String downloadHtmlWithParamsGet(String url, HashMap<String,String> params){
        String html = null;
        int time = 0;
        while(html == null && time < 10){
            time++;
            html = downloadHtmlWithParamsGet1time(url, params);
        }
        return html;
    }

    public String downloadHtmlWithParamsPost1time(String url, HashMap<String,String> params){
        String html = null;
        //创建一个客户端
        //创建一个读取流从entity读取html
        BufferedReader reader = null;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpResponse response = null;
        try {
            response = httpclient.execute(requestset.postMethodWithParams(url,params));
            HttpEntity entity = response.getEntity();
            reader = new BufferedReader(new InputStreamReader(entity.getContent()));
            StringBuilder sb = new StringBuilder();
            while((html = reader.readLine()) != null){
                sb.append(html);
            }
            html = sb.toString();
            System.out.println("HtmlDownloader-downloadHtmlWithParamsPost-获取成功");
        }
        catch (IOException e) {
            System.out.println(url+"-HtmlDownloader-downloadHtmlWithParamsPost-连接失败");
        }
        finally{
            if(reader != null){
                try {
                    reader.close();
                    httpclient.close();
                }
                catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        return html;
    }
    //连接失败重传
    public String downloadHtmlWithParamsPost(String url, HashMap<String,String> params){
        String html = null;
        int time = 0;
        while(html == null && time < 10){
            time++;
            html = downloadHtmlWithParamsPost1time(url,params);
        }
        return html;
    }


    public static void main(String[] args) {
        HashMap<String, String> params = new HashMap<>();
        params.put("uname", "admin");
        params.put("passwd", "admin");
        params.put("submit", "Submit");
        MyRequestSet requestSet = new MyRequestSet();
        HtmlDownloader downloader = new HtmlDownloader(requestSet);
        String html = downloader.downloadHtmlWithParamsPost("http://localhost/sqli-labs/Less-12/", params);
        System.out.println(html);
    }
}
