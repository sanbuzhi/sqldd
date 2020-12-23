package com.sqldd.payloadOrigin.utils;

import java.util.ArrayList;

public class Tools {
    //输入str，返回concat(0x7e,str,0x7e)
    public String tools_concat(String str){
        return "concat(0x7e," + str + ",0x7e)";
    }

    //输入str,返回group_concat(0x7e,str,0x7e)
    public String tools_group_concat(String str){
        return "group_concat(0x7e," + str + ",0x7e)";
    }

    //从html里正则提取出所有~value~包含的value出来，返回一个arraylist
    public ArrayList<String> tools_returnClosureRst(String html){
        ArrayList<Integer> locatorIndexs = new ArrayList<>();
        ArrayList<String> rst = new ArrayList<>();
        if(html.indexOf("~") < 0)
            return null;
        for (int i = 0; i < html.length(); i++) {
            if(html.charAt(i) == '~'){
                locatorIndexs.add(i);
            }
        }
        for (int i = 0; i < locatorIndexs.size(); i+=2) {
            if(i+1 != locatorIndexs.size())//防止~的数量位奇数，i+1越界的情况
                rst.add(html.substring(locatorIndexs.get(i)+1, locatorIndexs.get(i+1)));
        }
        int i = html.indexOf("123321");
        if(i > 0){
            int i1 = Integer.parseInt(html.substring(i + 6, i + 8)) - 11;
            rst.add(""+i1);
        }

        return rst;
    }

    //报错提取：从html里正则提取出所有^value^包含的value出来，返回一个arraylist
    public ArrayList<String> tools_Error_returnClosureRst(String html){
        ArrayList<Integer> locatorIndexs = new ArrayList<>();
        ArrayList<String> rst = new ArrayList<>();
        for (int i = 0; i < html.length(); i++) {
            if(html.charAt(i) == '^'){
                locatorIndexs.add(i);
            }
        }
        for (int i = 0; i < locatorIndexs.size(); i+=2) {
            if(i+1 == locatorIndexs.size())
                break;
            rst.add(html.substring(locatorIndexs.get(i)+1, locatorIndexs.get(i+1)));
        }
        return rst;
    }

    public String tools_Error_returnClosureRst1(String html){
        int start = -1;
        int end = -1;
        int flag = 0;
        for (int i = 0; i < html.length(); i++) {
            if(html.charAt(i) == '^' && flag == 0){
                start = i+1;
                flag++;
            }else if(html.charAt(i) == '^' && flag == 1){
                end = i;
                flag++;
            }
            if(flag == 2)
                break;
        }
        if(flag == 0)
            return null;//没找到
        return html.substring(start, end);
    }

    //报错替换：将[]替换为我们的查询query
    public String tihuan(String boundary,String query){
        int i = boundary.indexOf("[");
        StringBuilder builder = new StringBuilder(boundary);
        builder.replace(i, i + 2, query);
        return builder.toString();
    }

    public static void main(String[] args) {
        Tools tools = new Tools();
        ArrayList<String> strings = tools.tools_returnClosureRst("~hello~and12332321");
        for (String string : strings) {
            System.out.println(string);
        }
        String s = tools.tools_Error_returnClosureRst1("^123^");
        System.out.println(s);
    }
}
