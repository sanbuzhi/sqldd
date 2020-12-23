package com.sqldd.crawler;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqldd.crawler.ifHasKey.IfHasKey;
import com.sqldd.p0jo.SqlddDomain;
import com.sqldd.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

//默认的存储类
public class MySaveUtil{
    public void save(String url, ArrayList<SqlddDomain> sqlddDomains) {
        String domainSaved = new Utils().getDomainSaved(url);
        BufferedWriter writer = null;
        File folder=new File("f:\\urls");
        if(!folder.exists()){//如果文件夹不存在
            folder.mkdir();//创建文件夹
        }
        File file = new File("f:\\urls\\"+domainSaved+".json");
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("f:\\urls\\"+domainSaved+".json创建失败！");
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            String jsonString = JSON.toJSONString(sqlddDomains);
            writer.write(jsonString);

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
        System.out.println("f:\\urls\\"+domainSaved+".json文件成功！\n============================");
    }


    //从json文件里获得jsonArray数组
    public JSONArray ConvertToJsonArray(String url){
        String domainSaved = new Utils().getDomainSaved(url);
        JSONArray jsonArray =null;
        BufferedReader reader = null;
        StringBuilder jsonStrs = new StringBuilder();
        InputStream inputStream=null;
        try {
            inputStream = new FileInputStream("f:\\urls\\"+domainSaved+".json");
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

    public static void main(String[] args) {
        MySaveUtil mySaveUtil = new MySaveUtil();
        JSONArray jsonArray = mySaveUtil.ConvertToJsonArray("http://localhost/sqli-labs");
        List<SqlddDomain> sqlddDomains = jsonArray.toJavaList(SqlddDomain.class);
        System.out.println(sqlddDomains.size());
        for (SqlddDomain sqlddDomain : sqlddDomains) {
            System.out.println(sqlddDomain);
        }
    }
}