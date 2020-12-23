package com.sqldd.payloadOrigin.utils;

import java.io.*;
import java.util.ArrayList;

public class OpBoundary {
    public void storeBoundaryInTxt(String type,String typeAdd,ArrayList<String> boundaryLists) {
        BufferedWriter writer = null;
        //没文件夹先创建文件夹
        File folder=new File("f:\\boundary");
        if(!folder.exists()){//如果文件夹不存在
            folder.mkdir();//创建文件夹
        }
        File file = new File("f:\\boundary\\"+type+typeAdd+"_boundarys.txt");

        if(!file.exists()){//如果文件不存在
            try {
                file.createNewFile();//创建文件
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("【f:\\boundary\\"+type+typeAdd+"_boundarys.txt】创建失败！");
            }
        }
        //写入
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file,false), "UTF-8"));
            for (String bypassList : boundaryLists) {
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
        System.out.println("【f:\\boundary\\"+type+typeAdd+"_boundarys.txt】存档成功！");
    }

    public ArrayList<String> getBoundarysFromTxt(String type,String typeAdd){
        ArrayList<String> rsts = new ArrayList<>();

        BufferedReader reader = null;
        StringBuilder jsonStrs = new StringBuilder();
        InputStream inputStream=null;
        File file = new File("f:\\boundary\\"+type+typeAdd+"_boundarys.txt");
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
}
