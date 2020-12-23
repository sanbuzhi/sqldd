package com.sqldd.fuzzingWaf.utils;

import java.util.ArrayList;

public class GetStr {
    //单独一个空格替换
    public ArrayList<String> getBlankReplace(){
        ArrayList<String> rst = new ArrayList<>();
        rst.add("/**/");
        rst.add("/*123abc*/");
        rst.add("/*!*/");
        rst.add("/*!14400*/");
        String [] strings = {"%0a","%0b","%0c","%0d","%09","%20","%a0"};//mysql的空白符
        for (String s : strings) {
            rst.add(s);
            rst.add("/*"+s+"*/");
        }
        //以上常规编码一般被过滤，可以使用其他特殊编码，比如%![a-z]
        String str="abcdefghijklmnopqrstuvwxyz";
        for (int i = 0; i < str.length(); i++) {
            rst.add("%!"+str.charAt(i));
            rst.add("/*%!"+str.charAt(i)+"*/");
        }
        return rst;
    }
}
