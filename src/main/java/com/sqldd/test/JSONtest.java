/*
package com.sqldd.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqldd.p0jo.SqlddDomain;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class JSONtest {
    public static void main(String[] args) {
        ArrayList<SqlddDomain> sqlddDomains = new ArrayList<>();

        ArrayList<String> keys = new ArrayList<>();
        keys.add("id");
        keys.add("name");
        keys.add("pwd");
        SqlddDomain sqlddDomain = new SqlddDomain("http://localhost/index.html", keys);

        ArrayList<String> keys1 = new ArrayList<>();
        keys1.add("any1");
        keys1.add("any2");
        keys1.add("any3");
        SqlddDomain sqlddDomain1 = new SqlddDomain("http://localhost/any.html", keys1);

        sqlddDomains.add(sqlddDomain);
        sqlddDomains.add(sqlddDomain1);

        //list<bean>
        System.out.println(sqlddDomains);
        //转json字符串
        String s = JSON.toJSONString(sqlddDomains);
        System.out.println(s);
        //转JsonArray
        JSONArray array = JSONArray.parseArray(s);
        System.out.println(array);
        //转list<bean>
        List<? extends SqlddDomain> domains = JSONObject.parseArray(array.toJSONString(), sqlddDomain.getClass());
        System.out.println(domains);

        */
/*//*
/测试函数ConvertToJsonArray(从json文件读取出JsonArray)
        //前提json已有数据，不然空指针异常
        JSONArray jsonArray = new JSONtest().ConvertToJsonArray("f:\\urls.json");
        System.out.println(jsonArray);
        List<SqlddDomain> domains1 = JSONObject.parseArray(jsonArray.toJSONString(), SqlddDomain.class);
        //List<? extends SqlddDomain> domains1 = JSONObject.parseArray(jsonArray.toJSONString(), sqlddDomain.getClass());
        System.out.println("domains1domains1"+domains1);
        System.out.println("\n\n");*//*


        new JSONtest().savetest("111", "111");
        new JSONtest().savetest("222", "111");
    }

    public void savetest(String url,String html){
        BufferedWriter writer = null;
        File file = new File("f:\\urls.json");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("urls.json创建失败！");
            }
        }
        //写入
        try {
            //先取出json数组，转换成arraylist<domain>添加新数据后，再准换成json数组，再写入
            JSONArray jsonArray = new JSONtest().ConvertToJsonArray("f:\\urls.json");
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            if(jsonArray != null){
                List<SqlddDomain> domains = JSONObject.parseArray(jsonArray.toJSONString(), SqlddDomain.class);
                System.out.println("domains+"+domains);
                */
/**
                 * key从html里提取，暂时就用一个默认的name，pwd
                 *//*

                ArrayList<String> keys = new ArrayList<>();
                keys.add("name");
                keys.add("pwd");
                SqlddDomain sqlddDomain = new SqlddDomain(url, keys);
                domains.add(sqlddDomain);
                String jsonString = JSON.toJSONString(domains);
                writer.write(jsonString);
            }else {
                ArrayList<SqlddDomain> domains = new ArrayList<>();
                */
/**
                 * key从html里提取，暂时就用一个默认的name，pwd
                 *//*

                ArrayList<String> keys = new ArrayList<>();
                keys.add("name");
                keys.add("pwd");
                SqlddDomain sqlddDomain = new SqlddDomain(url, keys);
                domains.add(sqlddDomain);
                String jsonString = JSON.toJSONString(domains);
                writer.write(jsonString);
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if(writer != null){
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("数据写入urls.json文件成功！");
    }


    //从json文件里获得jsonArray数组
    public JSONArray ConvertToJsonArray(String path){
        JSONArray jsonArray =null;
        BufferedReader reader = null;
        StringBuilder jsonStrs = new StringBuilder();
        InputStream inputStream=null;
        try {
            inputStream = new FileInputStream(path);
            if(inputStream==null){
                return jsonArray;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempStr = null;
            while ((tempStr = reader.readLine()) != null) {
                jsonStrs.append(tempStr);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        jsonArray=JSONArray.parseArray(jsonStrs.toString().trim());
        return jsonArray;
    }
}*/
