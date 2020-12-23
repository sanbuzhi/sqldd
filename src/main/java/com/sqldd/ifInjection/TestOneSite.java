package com.sqldd.ifInjection;

import com.alibaba.fastjson.JSONArray;
import com.sqldd.crawler.MySaveUtil;
import com.sqldd.ifInjection.utils.TestOneSiteSaveUtil;
import com.sqldd.p0jo.InjectableDomain;
import com.sqldd.p0jo.SqlddDomain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class TestOneSite {
    private String url;//http://localhost/sqli-labs/

    public TestOneSite(String url){
        this.url = url;
    }

    //测试并保存一个网站的所有可注入点
    public void testAndSaveInjectableDomainsOneSite(){
        ArrayList<InjectableDomain> injectableDomains = new ArrayList<>();
        IfInjection ifInjection = new IfInjection();
        MySaveUtil mySaveUtil = new MySaveUtil();

        Scanner scanner = new Scanner(System.in);
        JSONArray jsonArray = mySaveUtil.ConvertToJsonArray(url);
        System.out.println("\n\n\n\n\n=====================================测试开始=============================================");
        List<SqlddDomain> sqlddDomains = jsonArray.toJavaList(SqlddDomain.class);

        for (int testpageIndex = 0; testpageIndex < sqlddDomains.size(); testpageIndex++) {
            /*System.out.println("是否测试网页?");
            String s = scanner.nextLine();
            if(s.equals("no"))
                break;*/
            System.out.println("注入网页开始:["+testpageIndex+"]");
            System.out.println("====== url : " + sqlddDomains.get(testpageIndex).getUrl());
            System.out.print("====== 全部参数 : ");
            //待续...
            for (String key : sqlddDomains.get(testpageIndex).getKeys()) {
                System.out.print(key+"+  ");
            }
            System.out.println("");
            //每个网页，每个参数+其他正常的参数就是注入点，正常的参数需要我们手动赋予
            System.out.println("====== 参数赋正确值(两组+逗号隔开)");
            /*String value1s = scanner.nextLine();
            String value2s = scanner.nextLine();
            String[] split1 = value1s.split(",");
            String[] split2 = value2s.split(",");
            HashMap<String, String> params1 = new HashMap<>();//params正常值有了
            HashMap<String, String> params2 = new HashMap<>();//params正常值有了
            for (int i = 0; i < sqlddDomains.get(testpageIndex).getKeys().size(); i++) {
                params1.put(sqlddDomains.get(testpageIndex).getKeys().get(i), split1[i]);
                params2.put(sqlddDomains.get(testpageIndex).getKeys().get(i), split2[i]);
            }*/
            //自动赋值
            String firstKey = sqlddDomains.get(testpageIndex).getKeys().get(0);
            HashMap<String, String[]> autoValues = paramsAutoCreate(firstKey);
            if(autoValues == null)
                continue;
            HashMap<String, String> params1 = new HashMap<String,String>(){
                {
                    for (String key : autoValues.keySet()) {
                        put(key, autoValues.get(key)[0]);
                    }
                }
            };
            HashMap<String, String> params2 = new HashMap<String,String>(){
                {
                    for (String key : autoValues.keySet()) {
                        put(key, autoValues.get(key)[1]);
                    }
                }
            };
            HashMap<String, String> abnormalparams = new HashMap<String,String>(){
                {
                    for (String key : autoValues.keySet()) {
                        put(key, autoValues.get(key)[2]);
                    }
                }
            };

            String[] rsts = ifInjection.filterInjectionDot(sqlddDomains.get(testpageIndex), params1, params2);
            if(rsts != null){
                System.out.println("可注入");
                for (String rst : rsts) {
                    System.out.print(rst+"  ");
                }
                System.out.println("");
                InjectableDomain injectableDomain = new InjectableDomain(sqlddDomains.get(testpageIndex), params1, params2, abnormalparams, rsts[0], rsts[1], rsts[2], rsts[3]);
                injectableDomains.add(injectableDomain);
            }
            System.out.println("注入网页结束");
        }
        //保存
        new TestOneSiteSaveUtil().saveInjectableDomains(url,injectableDomains);
    }

    //自动赋正常和异常值，实际使用时需手动赋予
    HashMap<String,String []> paramsAutoCreate(String key){
        HashMap<String, String[]> map = new HashMap<>();
        if(key.equals("id")){
            map.put("id", new String[]{"1","2","-1"});
        }else if(key.equals("uname")){
            map.put("uname", new String[]{"admin1","admin2","xx"});
            map.put("passwd", new String[]{"admin1","admin2","xx"});
            map.put("submit", new String[]{"Submit","Submit","xx"});
        }else if(key.equals("login_user")){
            map.put("login_user", new String[]{"admin1","admin2","xx"});
            map.put("login_password", new String[]{"admin1","admin2","xx"});
            map.put("mysubmit", new String[]{"Submit","Submit","xx"});
        }else
            return null;
        return map;
    }

    public static void main(String[] args) {
        TestOneSite test = new TestOneSite("http://localhost/sqli-labs");
        test.testAndSaveInjectableDomainsOneSite();
    }
}
