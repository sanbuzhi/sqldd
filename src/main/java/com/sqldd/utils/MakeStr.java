package com.sqldd.utils;

import java.util.ArrayList;
import java.util.Random;

public class MakeStr {

    //给定字符集，返回长度1到maxLength的字符串集
    public ArrayList<String> tools_getStrsByMaxLength(char [] chars, int maxLength){
        ArrayList<String> rst = new ArrayList<>();
        ArrayList<String> tmps1 = new ArrayList<>();
        //暂定长度最长为5，组合有3^1+3^2+3^3+3^4+3^5 = 3+9+27+81+243=363种
        for (char c : chars) {
            rst.add(Character.toString(c));
            tmps1.add(Character.toString(c));
        }
        for (int i = 2; i <= maxLength; i++) {//长度
            ArrayList<String> tmps2 = new ArrayList<>();
            for (String s1 : tmps1) {
                for (char c : chars) {//各种字符取一次
                    String tmp = s1 + c;
                    tmps2.add(tmp);
                }
            }
            tmps1.clear();
            for (String tmp2 : tmps2) {
                rst.add(tmp2);
                tmps1.add(tmp2);
            }
            tmps2.clear();
        }
        return rst;
    }

    //根据最长长度返回字符串，即返回随机长度(<length)的字符串
    public String tools_getStrsRandom(int length){
        int len = (int) (1 + Math.random() * (length - 2 + 1));
        String str="abcdefghijklmnopqrstuvwxyz";
        Random random=new Random();
        StringBuffer sb=new StringBuffer();
        for(int i=0;i<length;i++){
            int number=random.nextInt(26);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }


    //字符串转换unicode
    public String string2Unicode(String string) {
        StringBuffer unicode = new StringBuffer();
        for (int i = 0; i < string.length(); i++) {
            // 取出每一个字符
            char c = string.charAt(i);
            // 转换为unicode
            unicode.append("%" + Integer.toHexString(c));
        }
        return unicode.toString();
    }




    public static void main(String[] args) {
        MakeStr makeStr = new MakeStr();
        String abc = makeStr.string2Unicode("abc");
        System.out.println(abc);
    }
}
