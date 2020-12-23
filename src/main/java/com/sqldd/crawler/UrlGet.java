package com.sqldd.crawler;

/**
 * 用于根据一个html页面获得所有的url连接
 */

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.LinkedList;

public class UrlGet {
    public LinkedList<String> geturls(String html){
        LinkedList<String> urls = new LinkedList<String>();
        Document doc = Jsoup.parse(html);
        //a标签的url
        Elements links = doc.getElementsByTag("a");
        for (Element link:links){
            String url = link.attr("href");
            urls.add(url);
        }
        //area标签的url
        Elements links1 = doc.getElementsByTag("area");
        for (Element link:links1){
            String url = link.attr("href");
            urls.add(url);
        }

        return urls;
    }
}