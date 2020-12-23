package com.sqldd.test;

import org.springframework.util.DigestUtils;

import javax.lang.model.element.VariableElement;
import java.util.ArrayList;

public class Testwilldel {
    private static final org.springframework.util.DigestUtils DigestUtils = null;

    public static void main(String[] args) {
/*        int time = 2;
        //构造select内联查询次数
        StringBuilder builder = new StringBuilder();
        String ori = "(select * from)";
        for (int i = 0; i < time; i++) {
            builder.insert(builder.length()-i, ori);
            System.out.println(builder.toString());
        }
        String query = "(select user())";
        builder.insert(builder.length()-time, query);
        //alias
        for (int i = 0; i < time; i++) {
            builder.insert(builder.length()-time-2*i, (char)('a'+i));
            System.out.println(builder.toString());
        }
        System.out.println(builder.toString());*/

        ArrayList<String> newB = new ArrayList<>();
        newB.add("1");
        newB.add("2");
        newB.add("3");
        newB.add("4");
        newB.add("5");
        System.out.println(newB.size());
        ArrayList<String> tmp = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            tmp.add(newB.get(i));
        }
        newB.removeAll(newB);
        newB.addAll(tmp);
        System.out.println(newB.size());
        for (String s : newB) {
            System.out.println(s);
        }
    }
}
