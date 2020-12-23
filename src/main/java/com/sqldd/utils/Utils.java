package com.sqldd.utils;

public class Utils {
    //提取保存所用方法
    public String getDomainSaved(String url){
        String replace = url.replace('.', '_');
        String replace1 = replace.replace('/', '_');
        String replace2 = replace1.replace(':', '_');
        return replace2;
    }

    public static void main(String[] args) {
        String domainSaved = new Utils().getDomainSaved("http://localhost/sqli-labs/");
        System.out.println(domainSaved);
    }
}
