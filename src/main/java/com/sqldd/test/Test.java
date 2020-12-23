package com.sqldd.test;

import java.beans.Encoder;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Test {
    private ArrayList<String> arrs = new ArrayList<>();
    Test(){
        ArrayList<String> list = new ArrayList<String>() {
            {
                add("one");
                add("two");
                add("xhpHP+vip");
            }
        };
        arrs.addAll(list);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        /*Test test = new Test();
        for (String arr : test.arrs) {
            System.out.println(arr);
        }*/
        String url = "http:\\localhost\\sqli-labs\\Less-1\\";
        System.out.println(url.indexOf(':'));
        System.out.println(url.indexOf('\\', url.indexOf(':') + 2));
        String filename = url.substring(url.indexOf(':')+2, url.indexOf('\\',url.indexOf(':')+2));
        System.out.println(filename);
    }
}
