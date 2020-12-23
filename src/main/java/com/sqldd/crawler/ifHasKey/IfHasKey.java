package com.sqldd.crawler.ifHasKey;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class IfHasKey {
    private String url = null;
    private String html = null;
    public IfHasKey(){}
    public IfHasKey(String url,String html){
        this.url = url;
        this.html = html;
    }

    public ArrayList<String> ifHasGetKey(){
        //看原始url里是否存在key
        //如果默认没给出，则需要我们建立一个库进行猜解
        //sqli-lib给定的是id，这里假定预先知道是id，根据反馈猜解是否正确
        ArrayList<String> keys = new ArrayList<>();
        int of = url.indexOf('?');
        if(of>0){
            String[] split = url.substring(of+1).split("&");
            for (String s : split) {
                keys.add(s.substring(0,s.indexOf('=')));
            }
            return keys;
        }else {
            keys.add("id");
        }
        return keys;
    }

    /**
     * POST参数，只需从hmtl文档找到input字段值即可
     */
    public ArrayList<String> ifHasPostKey(){
        ArrayList<String> keys = new ArrayList<>();
        Document doc = Jsoup.parse(html);
        //a标签的url
        Elements inputKeys = doc.getElementsByTag("input");
        for (Element inputKey:inputKeys){
            String key = inputKey.attr("name");
            keys.add(key);
        }
        return keys;
    }

    public static void main(String[] args) {
        IfHasKey hasKey = new IfHasKey();
        hasKey.url = "http://localhost/hello.php?name=1&pwd=2";
        //hasKey.url = "http://localhost/hello.php";
        ArrayList<String> keys = hasKey.ifHasGetKey();
        for (String key : keys) {
            System.out.println(key);
        }
        System.out.println(hasKey.url.substring(0, hasKey.url.indexOf('?')));
    }
}
