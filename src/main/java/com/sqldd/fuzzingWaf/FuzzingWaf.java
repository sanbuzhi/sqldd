package com.sqldd.fuzzingWaf;

import com.sqldd.payloadOrigin.base.KeywordLib;
import java.util.ArrayList;
import java.util.HashMap;

/**
     * 探测waf规则
     * 有回显的情况
     */
public class FuzzingWaf {
    private String url;
    private String method;
    private HashMap<String,String> params = new HashMap<String,String>();//参数
    private String injectingKey;//正在测试的注入点
    private HashMap<String,String>  normalvalue1= new HashMap<String,String>();
    private HashMap<String,String>  normalvalue2= new HashMap<String,String>();

    public FuzzingWaf(String url,String method,HashMap<String,String> params,String injectingKey,HashMap<String,String>  normalvalue1,HashMap<String,String>  normalvalue2){
        this.url = url;
        this.method = method;
        this.params = params;
        this.injectingKey = injectingKey;
        this.normalvalue1 = normalvalue1;
        this.normalvalue2 = normalvalue2;
    }

    /**
     * 探测被WAF拦截的关键字和组合
     * @return 未被拦截的关键字
     */
    public ArrayList<String> filterKeywords(){
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        KeywordLib keywordLib = new KeywordLib();
        ArrayList<String> keywordsNoBan = new ArrayList<>();


        //正常页面
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(normalvalue1, normalvalue2);

        ArrayList<String> keyLists = keywordLib.getKeyList();
        for (String key : keyLists) {
            this.params.put(this.injectingKey, key);
            int rate = pageComparison.getRate(normalRst, this.params);
            if(rate != 3)
                keywordsNoBan.add(key);
        }
        return keywordsNoBan;
    }

    //探测被WAF拦截的组合
    //比如-1' union select 1,group_concat(0x7e,schema_name,0x7e),3 from information_schema.schemata --+
    //先探测-1' union select 1,
    //再探测

    public static void main(String[] args){
        HashMap<String, String> params = new HashMap<>();
        /*params.put("id", "any");
        String [] normalvalue = {"1","2"};
        FuzzingWaf test = new FuzzingWaf("http://localhost/sqli-labs/Less-1/","get",params, "id", normalvalue);
        ArrayList<String> strings = test.filterKeywords();
        for (String string : strings) {
            System.out.println(string);
        }*/
    }
}