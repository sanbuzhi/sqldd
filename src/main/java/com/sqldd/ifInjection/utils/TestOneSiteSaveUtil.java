package com.sqldd.ifInjection.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqldd.crawler.MySaveUtil;
import com.sqldd.p0jo.InjectableDomain;
import com.sqldd.p0jo.SqlddDomain;
import com.sqldd.utils.Utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * TestOneSite结果保存和读取
 */
public class TestOneSiteSaveUtil {
    public void saveInjectableDomains(String url,ArrayList<InjectableDomain> injectableDomains){
        Utils utils = new Utils();
        BufferedWriter writer = null;
        //没文件夹先创建文件夹
        String domain = utils.getDomainSaved(url);
        File folder=new File("f:\\injectabledomains");
        if(!folder.exists()){//如果文件夹不存在
            folder.mkdir();//创建文件夹
        }
        File file = new File("f:\\injectabledomains\\"+domain+".json");
        if(!file.exists()){//如果文件不存在
            try {
                file.createNewFile();//创建文件
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("文件【f:\\injectabledomains\\"+domain+".json】创建失败！");
            }
        }
        //写入
        try {
            //JSONArray jsonArray = ConvertToJsonArray(url);
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            String jsonString = JSON.toJSONString(injectableDomains);
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
        System.out.println("文件【f:\\injectabledomains\\"+domain+".json】存档成功！");
    }

    public List<InjectableDomain> getInjectableDomainsFromJson(String url){
        Utils utils = new Utils();
        String domain = utils.getDomainSaved(url);
        BufferedReader reader = null;
        StringBuilder jsonStrs = new StringBuilder();
        InputStream inputStream=null;
        try {
            inputStream = new FileInputStream("f:\\injectabledomains\\"+domain+".json");
            if(inputStream==null){
                return null;
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
        JSONArray jsonArray = JSONArray.parseArray(jsonStrs.toString().trim());

        List<InjectableDomain> domains = jsonArray.toJavaList(InjectableDomain.class);
        return domains;
    }

    //从json文件里获得jsonArray数组
    public JSONArray ConvertToJsonArray(String url){
        String domainSaved = new Utils().getDomainSaved(url);
        JSONArray jsonArray =null;
        BufferedReader reader = null;
        StringBuilder jsonStrs = new StringBuilder();
        InputStream inputStream=null;
        try {
            inputStream = new FileInputStream("f:\\injectabledomains\\"+domainSaved+".json");
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
        TestOneSiteSaveUtil testOneSiteSaveUtil = new TestOneSiteSaveUtil();
        List<InjectableDomain> injectableDomains = testOneSiteSaveUtil.getInjectableDomainsFromJson("http://localhost/sqli-labs");
        System.out.println(injectableDomains.size());
        for (InjectableDomain injectableDomain : injectableDomains) {
            System.out.println(injectableDomain);
        }
    }
}
