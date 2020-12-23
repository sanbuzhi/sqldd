package com.sqldd.crawler;

import com.sqldd.crawler.ifHasKey.IfHasKey;
import com.sqldd.p0jo.SqlddDomain;

import java.util.*;

/**
 * 用于爬取资源的类
 **/
public class Spider{
    //种子url
    String seed = null;
    //用于保存数据的类,需要自己实现
    private MySaveUtil saveutil = null;
    //html下载类
    private HtmlDownloader downloader = null;
    //url下载类
    private UrlGet urldownloader = null;
    //资源选择工具
    private MyResourseChooser resoursechooser = null;
    //用于保存未下载的网页。广度优先搜索的队列
    LinkedList<String> unvisited = new LinkedList<String>();
    //用于保存已下载的网页
    HashSet<String> visited = new HashSet<String>();

    /*//自定义储存方式,请求方式,资源筛选方式的构造方法
    public Spider(SaveUtil saveutil,RequestSet request,ResourseChooser resoursechooser,String seed){
        this.saveutil = saveutil;
        this.downloader = new HtmlDownloader(request);
        this.urldownloader = new UrlGet();
        this.resoursechooser = resoursechooser;
        this.seed = seed;
        unvisited.add(seed);
    }
    //自定义储存方式,资源筛选方式的构造方法
    public Spider(SaveUtil saveutil,ResourseChooser resoursechooser,String seed){
        this.resoursechooser = resoursechooser;
        this.downloader = new HtmlDownloader(new MyRequestSet());
        this.saveutil = saveutil;
        this.urldownloader = new UrlGet();
        this.seed = seed;
        unvisited.add(seed);
    }
    //自定义储存方式,请求的构造方法
    public Spider(SaveUtil saveutil,RequestSet requestset,String seed){
        this.saveutil = saveutil;
        this.downloader = new HtmlDownloader(requestset);
        this.resoursechooser = new MyResourseChooser();
        this.urldownloader = new UrlGet();
        this.seed = seed;
        unvisited.add(seed);
    }
    //自定义储存方式的构造方法
    public Spider(SaveUtil saveutil,String seed){
        this.saveutil = saveutil;
        this.downloader = new HtmlDownloader(new MyRequestSet());
        this.resoursechooser = (new MyResourseChooser());
        this.urldownloader = new UrlGet();
        this.seed = seed;
        unvisited.add(seed);
    }*/
    //默认的爬虫构造方法
    public Spider(String seed){
        this.saveutil = new MySaveUtil();
        this.downloader = new HtmlDownloader(new MyRequestSet());
        this.resoursechooser = (new MyResourseChooser());
        this.urldownloader = new UrlGet();
        this.seed = seed;
        unvisited.add(seed);
    }
    //开始爬取的方法
    public void spiderstart(){
        ArrayList<SqlddDomain> sqlddDomains = new ArrayList<>();
        ArrayList<String []> allurls = new ArrayList<>();//0-url,1-htmls
        String html = null;
        while(!unvisited.isEmpty()){
            String url = unvisited.poll();
            System.out.println("开始获取 "+url);
            if(resoursechooser.isNeed(url)){
                try{
                    html = downloader.simpleDownloadhtml(url);
                }
                catch(RuntimeException e){
                    System.out.println(url+" 连接获取失败");
                    continue;
                }
                visited.add(url);
                LinkedList<String> urls = new LinkedList<String>();
                try{
                    urls = urldownloader.geturls(html);
                }
                catch(RuntimeException e){
                    System.out.println(url+" 的html页面为空");
                    continue;
                }
                Iterator<String> it = urls.iterator();
                while(it.hasNext()){
                    String newurl = it.next();
                    if(resoursechooser.isNeed(newurl)){
                        newurl = resoursechooser.process(seed,newurl);
                        if(!visited.contains(newurl)&&!unvisited.contains(newurl)){
                            unvisited.add(newurl);
                            System.out.println(newurl+" 加入未访问队列");
                        }
                    }
                }
                System.out.println("获取了 "+url+"上的所有url");
                if(resoursechooser.isResourse(url)){
                    boolean ifurlexists = false;
                    for (String[] allurl : allurls) {
                        if(allurl[0].equals(url))
                            ifurlexists = true;
                    }
                    if(ifurlexists == false){
                        allurls.add(new String[]{url, html});
                    }
                }
            }
        }
        for (String[] allurl : allurls) {
            SqlddDomain sqlddDomain = getSqlddDomain(allurl[0], allurl[1]);
            sqlddDomains.add(sqlddDomain);
        }
        new MySaveUtil().save(seed, sqlddDomains);
    }

    public SqlddDomain getSqlddDomain(String url,String html){
        SqlddDomain sqlddDomain = new SqlddDomain();
        sqlddDomain.setUrl(url);//setUrl
        IfHasKey ifHasKey = new IfHasKey(url,html);//setMethod和setKeys
        ArrayList<String> keys = ifHasKey.ifHasPostKey();
        if(keys.size() != 0){
            sqlddDomain.setMethod("post");
            sqlddDomain.setKeys(keys);
        }else{
            ArrayList<String> keysGET = ifHasKey.ifHasGetKey();
            if(keysGET.size() != 0){
                if(url.indexOf('?') > 0)
                    sqlddDomain.setUrl(url.substring(0, url.indexOf('?')));
                sqlddDomain.setMethod("get");
                sqlddDomain.setKeys(keysGET);
            }
            else {
                sqlddDomain.setMethod(null);
                sqlddDomain.setKeys(null);
            }
        }
        return sqlddDomain;
    }

    public static void main(String[] args) {
        //Spider spider = new Spider("https://baijiahao.baidu.com/s?id=1681056049238905387&wfr=spider&for=pc");
        //Spider spider = new Spider("http://localhost/sqli-labs/index.html");
        Spider spider = new Spider("http://localhost/sqli-labs");
        spider.spiderstart();
    }
}
