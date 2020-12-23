package com.sqldd.fuzzingWaf.utils;

import java.io.*;
import java.util.ArrayList;

/**
 * 存取bypass
 */

public class BypassTxtOprator {
    /**
     *
     * @param url 完整url地址，且都是以/分隔，不是\\
     * @param bypassLists  列表
     */
    public void storeBypassInTxt(String url, String type,ArrayList<String> bypassLists) {
        BufferedWriter writer = null;
        //没文件夹先创建文件夹
        File folder=new File("f:\\bypass");
        if(!folder.exists()){//如果文件夹不存在
            folder.mkdir();//创建文件夹
        }
        String domain = url.substring(url.indexOf(':')+2, url.indexOf('/',url.indexOf(':')+3));//域名
        File file = null;
        if("simgle".equals(type))
            file = new File("f:\\bypass\\"+domain.replace('.', '_')+"_simgle.txt");
        else if("double".equals(type))
            file = new File("f:\\bypass\\"+domain.replace('.', '_')+"_double.txt");
        else
            file = new File("f:\\bypass\\"+domain.replace('.', '_')+"_"+type+".txt");
        if(!file.exists()){//如果文件不存在
            try {
                file.createNewFile();//创建文件
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("文件【f:\\bypass\\"+domain.replace('.', '_')+"_"+type+".txt"+"】创建失败！");
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            for (String bypassList : bypassLists) {
                writer.write(bypassList+"\n");
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
        System.out.println("文件【f:\\bypass\\"+domain.replace('.', '_')+"_"+type+"_.txt"+"】存档成功！");
    }

    /**
     *
     * @param url  完整url地址，以/分隔
     * @return
     */
    public ArrayList<String> getBypassFromTxt(String url,String type){
        ArrayList<String> rsts = new ArrayList<>();

        BufferedReader reader = null;
        StringBuilder jsonStrs = new StringBuilder();
        InputStream inputStream=null;
        String domain = url.substring(url.indexOf(':')+2, url.indexOf('/',url.indexOf(':')+3));
        File file = null;
        if("simgle".equals(type))
            file = new File("f:\\bypass\\"+domain.replace('.', '_')+"_simgle.txt");
        else if("double".equals(type))
            file = new File("f:\\bypass\\"+domain.replace('.', '_')+"_double.txt");
        else
            file = new File("f:\\bypass\\"+domain.replace('.', '_')+"_"+type+".txt");
        try {
            inputStream = new FileInputStream(file);
            if(inputStream==null){
                return null;
            }
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
            reader = new BufferedReader(inputStreamReader);
            String tempStr = null;
            while ((tempStr = reader.readLine()) != null) {
                rsts.add(tempStr);
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
        return rsts;
    }

    public static void main(String[] args) {
        BypassTxtOprator bypassTxtOprator = new BypassTxtOprator();
        ArrayList<String> strings = new ArrayList<>();
        strings.add("123");
        strings.add("234");
        strings.add("456");
        bypassTxtOprator.storeBypassInTxt("http://localhost/sqli-labs/Less-1/","simgle",strings);
        ArrayList<String> bypassFromTxt = bypassTxtOprator.getBypassFromTxt("http://localhost/sqli-labs/Less-1/","simgle");
        for (String s : bypassFromTxt) {
            System.out.println(s);
        }

    }
}

