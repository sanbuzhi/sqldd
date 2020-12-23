package com.sqldd;

import com.sqldd.crawler.Spider;
import com.sqldd.fuzzingWaf.ByPassRule;
import com.sqldd.fuzzingWaf.byPass.BypassMake;
import com.sqldd.fuzzingWaf.utils.BypassTxtOprator;
import com.sqldd.ifInjection.DetectType;
import com.sqldd.ifInjection.TestOneSite;
import com.sqldd.ifInjection.utils.TestOneSiteSaveUtil;
import com.sqldd.p0jo.InjectableDomain;
import com.sqldd.p0jo.InjectableDomainType;
import com.sqldd.p0jo.SqlddDomain;
import com.sqldd.payloadOrigin.UnionQueryPayloads;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class DDStart {
    private String url;
    public DDStart(){
        System.out.println(" ____  ____  _     ____  ____ \n" +
                "/ ___\\/  _ \\/ \\   /  _ \\/  _ \\\n" +
                "|    \\| / \\|| |   | | \\|| | \\|\n" +
                "\\___ || \\_\\|| |_/\\| |_/|| |_/|\n" +
                "\\____/\\____\\\\____/\\____/\\____/\n" +
                "                             ");
        System.out.println("一款SQL注入漏洞检测软件，请确保正常键值对库补充完毕!" +
                "在下面输入网页爬取的起点.\n");
        System.out.println("URL：");
        Scanner scanner = new Scanner(System.in);
        this.url = scanner.nextLine();
    }


    public void DDstartok(){

        //爬取网页
        System.out.println("爬取网页开始！");
        Spider spider = new Spider(url);
        spider.spiderstart();

        //筛选可注入点
        System.out.println("筛选可注入点开始！");
        TestOneSite testOneSite = new TestOneSite(url);
        testOneSite.testAndSaveInjectableDomainsOneSite();


        //fuzzing注入点的过滤规则,bypass保存{基于一个网站通常公用一套过滤规则，所以可按get和post各存储一套bypass规则}
        System.out.println("fuzzing可用规则！");
        TestOneSiteSaveUtil testOneSiteSaveUtil = new TestOneSiteSaveUtil();
        List<InjectableDomain> injectableDomains = testOneSiteSaveUtil.getInjectableDomainsFromJson(url);//从保存的可注入点文件提取出注入点实体
/*
        BypassMake bypassMake = new BypassMake(injectableDomains);
        boolean ifBypassMake = bypassMake.bypassMake();
        if(ifBypassMake ==true){
            System.out.println("get和post bypass文件已保存");
        }
*/

        //所有可注入点的注入类型
        System.out.println("注入类型开始1");
        DetectType detectType = new DetectType(injectableDomains);
        ArrayList<InjectableDomainType> injectableDomainTypes = detectType.detectType();
        System.out.println("注入类型开始");
        for (InjectableDomainType injectableDomainType : injectableDomainTypes) {
            InjectableDomain injectableDomain = injectableDomainType.getInjectableDomain();
            System.out.println("injectableDomainType type:"+injectableDomain.getSqlddDomain().getUrl());
        }

        /*for (InjectableDomain injDD : injectableDomains) {
            SqlddDomain sqldd = injDD.getSqlddDomain();
            UnionQueryPayloads xx = new UnionQueryPayloads(sqldd.getUrl(), sqldd.getMethod(), injDD.getNormalParams1().get(injDD.getInjectingKey()) + injDD.getClosure(), "xx", injDD.getSuf(), injDD.getSufbk(), injDD.getNormalParams1(), injDD.getInjectingKey(), injDD.getNormalParams1(), injDD.getNormalParams2());
            System.out.println("取出bypass");
            System.out.println(injDD.getSqlddDomain().getUrl());
            ArrayList<String> bypassFromTxt = new BypassTxtOprator().getBypassFromTxt(url, "order_by_"+String.valueOf(injDD.hashCode()));
            for (String bypass : bypassFromTxt) {
                System.out.println(bypass);
            }
            System.out.println("\n\n");
        }*/
    }

    public static void main(String[] args) {
        DDStart ddStart = new DDStart();
        ddStart.DDstartok();
    }
}