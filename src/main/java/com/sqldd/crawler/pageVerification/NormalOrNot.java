package com.sqldd.crawler.pageVerification;

import com.sqldd.crawler.HtmlDownloader;
import com.sqldd.crawler.MyRequestSet;

import java.util.HashMap;

/**
 * 有回显的情况
 */
public class NormalOrNot {
    //适用于回显固定的情况
    public String pageVerifyViaKeyword(String url, String method,HashMap<String,String> params){
        String falseKeyword = "slap.jpg";
        String normalKeyword = "flag.jpg";
        String abnormalKeyword = "You have an error in your SQL syntax";
        MyRequestSet requestSet = new MyRequestSet();
        HtmlDownloader htmlDownloader = new HtmlDownloader(requestSet);
        String html = null;
        if("post".equals(method))
            html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
        else if("get".equals(method))
            html = htmlDownloader.downloadHtmlWithParamsGet(url,params);
        if(html.contains(normalKeyword))
            return "normal";
        else if(html.contains(abnormalKeyword))
            return "abnormal";
        else
            return "false";
    }

    //适用于输入会回显的情况
    public String pageVerityViaEnter(String url,String method,HashMap<String,String> params,String enter){
        String falseKeyword = "";
        String normalKeyword = "Your Login name";//非faleKeyword
        MyRequestSet requestSet = new MyRequestSet();
        HtmlDownloader htmlDownloader = new HtmlDownloader(requestSet);
        String html = null;
        if("post".equals(method))
            html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
        else if("get".equals(method))
            html = htmlDownloader.downloadHtmlWithParamsGet(url,params);
        if(!html.contains(enter))
            return "abnormal";
        else if(html.contains(enter))
            return "normal";
        else
            return "false";
    }
}
