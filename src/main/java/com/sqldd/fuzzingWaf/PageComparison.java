package com.sqldd.fuzzingWaf;

import ch.qos.logback.core.status.OnErrorConsoleStatusListener;
import com.sqldd.crawler.HtmlDownloader;
import com.sqldd.crawler.MyRequestSet;
import com.sqldd.payloadOrigin.base.KeywordLib;
import com.sqldd.utils.StringSubSame;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class PageComparison {
    private HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
    private String url;
    private String method;
    StringSubSame stringSubSame = new StringSubSame();
    public PageComparison(String url,String method){
        this.url = url;
        this.method = method;
    }

    //返回两个正常网页的公共字符串，相似比
    public ArrayList<String> getNormalPageComparison(HashMap<String,String> paramsOne, HashMap<String,String> paramsTwo){
        ArrayList<String> rst = new ArrayList<>();
        String htmlOne = "";
        String htmlTwo = "";
        if(method.equals("get")){
            htmlOne = htmlDownloader.downloadHtmlWithParamsGet(url, paramsOne);
            htmlTwo = htmlDownloader.downloadHtmlWithParamsGet(url, paramsTwo);
        }else if(method.equals("post")){
            htmlOne = htmlDownloader.downloadHtmlWithParamsPost(url, paramsOne);
            htmlTwo = htmlDownloader.downloadHtmlWithParamsPost(url, paramsTwo);
        }

        String s = stringSubSame.intersection(htmlOne, htmlTwo);
        /*System.out.println("页面1的长度："+htmlOne.length());
        System.out.println("页面2的长度："+htmlTwo.length());
        System.out.println("页面1和页面2的交集长度："+s.length());*/
        String rate = "";
        if(s != null && htmlOne != null && htmlTwo != null)
            rate = String.valueOf((double) s.length() * 2 / (htmlOne.length() + htmlTwo.length()));
        else
            rate = "0";
        /*System.out.println("相似比："+rate);*/
        rst.add(s);//交集
        rst.add(rate);//相似比
        return rst;
    }

    //三种可能，正常/报错/waf。其实应该还有一种可能，就是正常，但没有查找到数据
    public int getRate(ArrayList<String> getNormalPageComparisonRST,HashMap<String,String> params){
        //阈值
        double yuzhiNormal = 0.98;//这个页面和一个正常页面的交集，正常的话应该还是交集（因为交集就来自于两个正常页面）。所以这个阈值不会太小
        double yuzhiError = 0.85;//大致范围
        double yuzhiWaf = 0.0;//有waf的页面和正常页面，悬殊应该较大。
        String html = "";
        if(method.equals("get")){
            html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
        }else if(method.equals("post")){
            html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
        }
        String jiaoji = stringSubSame.intersection(getNormalPageComparisonRST.get(0), html);
        double rate = (double)jiaoji.length()/getNormalPageComparisonRST.get(0).length();
        System.out.println("getRate："+rate);
        if( rate>yuzhiNormal){
            System.out.println("[rate]:"+1);
            return 1;
        }
        else if(rate > yuzhiError){
            System.out.println("[rate]:"+2);
            return 2;
        }
        else{
            System.out.println("[rate]:"+3);
            return 3;
        }
    }

    //获取某个网页得MD5值
    public String getPageMd5(HashMap<String,String> params){
        String html = "";
        if(method.equals("get")){
            html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
        }else if(method.equals("post")){
            html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
        }
        String pageMd5 = DigestUtils.md5DigestAsHex(html.getBytes());
        return pageMd5;
    }

    //传入两个参数，是否改变
    public int ifSamePage(String url,String method,HashMap<String,String> paramsOrigin,HashMap<String,String> paramsNow){
        HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
        if(method.equals("get")){
            String html = htmlDownloader.downloadHtmlWithParamsGet(url, paramsOrigin);
            String html1 = htmlDownloader.downloadHtmlWithParamsGet(url, paramsNow);
            if(html.length() == html1.length())
                return 1;
            else return 0;
        }else if(method.equals("post")){
            String html = htmlDownloader.downloadHtmlWithParamsPost(url, paramsOrigin);
            String html1 = htmlDownloader.downloadHtmlWithParamsPost(url, paramsNow);
            if(html.length() == html1.length())
                return 1;
            else return 0;
        }
        return 0;
    }

    public static void main(String[] args) throws InterruptedException {
        PageComparison test = new PageComparison("http://localhost/sqli-labs/Less-1/","get");

        KeywordLib lib = new KeywordLib();
        HashMap<String, String> params1 = new HashMap<>();
        params1.put("id", "1");
        HashMap<String, String> params2 = new HashMap<>();
        params2.put("id", "2");
        ArrayList<String> list = test.getNormalPageComparison(params1, params2);

        HashMap<String, String> params = new HashMap<>();
        //params.put("id", "2' order by--+");
        params.put("id", "3");
        int rate1 = test.getRate(list, params);
        System.out.println("正常值{1}得分:"+rate1);
        params.put("id", "-1");
        int rate2 = test.getRate(list, params);
        System.out.println("错误值{-1}得分:"+rate2);
        params.put("id", "1' order by 1--+");
        int rate = test.getRate(list, params);
        System.out.println("被WAF拦截值{1' order by 1--+}得分:"+rate);
    }
}
