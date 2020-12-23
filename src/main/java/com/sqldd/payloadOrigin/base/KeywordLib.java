package com.sqldd.payloadOrigin.base;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class KeywordLib {
    public Map<String,Integer> key2num = new LinkedHashMap<>();//关键字到数字的映射
    public ArrayList<String> closureSymble=new ArrayList<String>();//闭合符
    {
        String s = "select,*,from,";
        s += "where,=,<>,!=,>,<,>=,<=,<=>,',\",regexp,";//<=>两个值相等或都为null返回true
        s += "--+,-- ,#,%23";//截断注释符
        s += "like,_,[,],$,{,},+,";
        s += "union,all,null,as,union select";//按,分隔后记得最后补充加上,逗号
        s += "order by,asc,desc,";
        s += "group by,with rollup,";//having呢
        s += "join,on,left join,right join,";
        s += "-,/,DIV,%,MOD,between,in,not in,(,),not between,is null,is not null,";//比较运算符
        s += "not,!,and,or,&&,||,xor,null or,1^,";//逻辑运算符
        s += "&,|,^,<<,>>,";//位运算符
        //报错注入相关函数
        s += "floor,extractvalue,updatexml,exp,GeometryCollection,polygon,multipoint,multilinestring,linestring,multipolygon,";//报错注入函数
        s += "concat,concat_ws,group_concat,";//字符串拼接函数
        s += "rand,round,limit,count,";//limt需要,逗号
        s += "version(),database(),user(),@@datadir,@@basedir,@@version_compile_os,@@hostname";//内置变量
        //boolean时间注入相关函数
        s += "substring,substr,mid,left,right,replace,lpad";//字符串截取函数,前三可返回单字符，left和right可组合返回单字符,replace和lpad可组合
        s += "ascii,ord,conv,hex";//返回字符ascii码,conv和hex组合
        s += "if,";//判断选择函数
        s += "sleep,benchmark,";//时间盲注,笛卡尔积
        s += "if,case,when,then,else,end,";//case,when,then,else,end一起使用
        //boolean盲注相关函数
        s += "";//like,regexp

        String[] split = s.split(",");
        for (int i = 0; i < split.length; i++) {
            key2num.put(split[i], i+1);
        }
        int size = key2num.size();
        key2num.put(",", size+1);
    }

    {
        //闭合符
        ArrayList<String> tmps1 = new ArrayList<>();
        char [] symble = {'\'','\"',')'};
        //暂定长度最长为5，组合有3^1+3^2+3^3+3^4+3^5 = 3+9+27+81+243=363种
        for (char c : symble) {
            this.closureSymble.add(Character.toString(c));
            tmps1.add(Character.toString(c));
        }
        for (int i = 2; i <= 5; i++) {//长度
            ArrayList<String> tmps2 = new ArrayList<>();
            for (String s1 : tmps1) {
                for (char c : symble) {//各种字符取一次
                    String tmp = s1 + c;
                    tmps2.add(tmp);
                }
            }
            tmps1.clear();
            for (String tmp2 : tmps2) {
                this.closureSymble.add(tmp2);
                tmps1.add(tmp2);
            }
            tmps2.clear();
        }
    }


    public Integer getindexByKey(String key){
        for (String keyk : key2num.keySet()) {
            if (keyk.equals(key))
                return key2num.get(key);
        }
        return null;
    }

    public String getKeyByIndex(Integer num){
        for (String key : key2num.keySet()) {
            if(num.equals(key2num.get(key)))
                return key;
        }
        return null;
    }

    public ArrayList<String> getKeyList(){
        ArrayList<String> rst = new ArrayList<>();
        for (String key : key2num.keySet()) {
            rst.add(key);
        }
        return rst;
    }

    public static void main(String[] args) {
        KeywordLib lib = new KeywordLib();
        for (String key : lib.key2num.keySet()) {
            System.out.println(key +" "+ lib.key2num.get(key));
        }

        Integer integer = lib.getindexByKey("select1");
        System.out.println(integer);
        String key = lib.getKeyByIndex(0);
        System.out.println(key);
    }

}
