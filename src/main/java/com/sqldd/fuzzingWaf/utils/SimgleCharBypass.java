package com.sqldd.fuzzingWaf.utils;

import com.sqldd.utils.MakeStr;

import java.util.ArrayList;

public class SimgleCharBypass {

    //1.大小写
    public String uplowKeyword(String keyword){
        if(keyword.length()>1){
            String sLOW = keyword.toLowerCase();
            String sUP = keyword.toUpperCase();
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < keyword.length()-1; i+=2) {
                builder.append(sLOW.charAt(i));
                builder.append(sUP.charAt(i+1));
            }
            return builder.toString();
        }
        return keyword;
    }

    //2.重复替换
    public ArrayList<String> repeatKeyword(String keyword){
        ArrayList<String> rsts = new ArrayList<>();
        String newStrTmp = keyword;
        for (int i = 0; i < 5; i++) {
            newStrTmp= new String(keyword.substring(0, 1)+newStrTmp+keyword.substring(1, keyword.length()));
            rsts.add(newStrTmp);
        }
        return rsts;
    }

    //3.注释符
    public String Comments(String keyword){
        if(keyword.length()>1)
            return keyword.substring(0, 1) + "/**/" + keyword.substring(1, keyword.length());
        return keyword;
    }

    //4.内联注释
    public ArrayList<String> inlineComments(String keyword){
        ArrayList<String> rsts = new ArrayList<>();
        rsts.add("/*!23144 "+keyword+"*/");
        rsts.add("/*!24432 "+keyword+"*/");
        MakeStr makeStr = new MakeStr();
        char [] chars1 = {'a','0','9','!','@','#','$','%','*','|','&','?','/'};//特殊字符集
        ArrayList<String> rsts1 = makeStr.tools_getStrsByMaxLength(chars1, 3);
        char [] chars2 = {'/','*','!','%'};//特殊字符集
        ArrayList<String> rsts2 = makeStr.tools_getStrsByMaxLength(chars2, 5);
        for (String rst : rsts1) {
            rsts.add("/*!"+keyword+rst+"*/");
        }
        for (String rst : rsts2) {
            rsts.add("/*!"+keyword+rst+"*/");
        }
        return rsts;
    }

    //5.特殊编码
    public ArrayList<String> encodeKeyword(String keyword){
        ArrayList<String> rsts = new ArrayList<>();

        //一次url编码
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < keyword.length(); i++) {
            unicode.append("%" + Integer.toHexString(keyword.charAt(i)));
        }
        rsts.add(unicode.toString());
        //两次url编码
        StringBuffer unicode2 = new StringBuffer();
        for (int i = 0; i < keyword.length(); i++) {
            unicode2.append("%25" + Integer.toHexString(keyword.charAt(i)));
        }
        rsts.add(unicode2.toString());

        return rsts;
    }

    //使用上面5种bypass方法
    public ArrayList<String> getAllBypass(String keyword){
        ArrayList<String> rsts = new ArrayList<>();
        String rst1 = uplowKeyword(keyword);
        ArrayList<String> rst2 = repeatKeyword(keyword);
        String rst3 = Comments(keyword);
        ArrayList<String> rst4 = inlineComments(keyword);
        ArrayList<String> rst5 = encodeKeyword(keyword);
        rsts.add(rst1);
        rsts.addAll(rst2);
        rsts.add(rst3);
        rsts.addAll(rst4);
        rsts.addAll(rst5);
        return rsts;
    }

    public static void main(String[] args) {
        SimgleCharBypass tset = new SimgleCharBypass();
        ArrayList<String> select = tset.repeatKeyword("select");
        for (String s : select) {
            System.out.println(s);
        }
    }
}
