package com.sqldd.crawler;

//默认资源筛选类
public class MyResourseChooser{
    public Boolean isNeed(String url) {
        if(Character.isLowerCase(url.charAt(0)) || Character.isUpperCase(url.charAt(0)) || url.charAt(0) == '\\')
            return true;
        else
            return false;
    }
    public Boolean isResourse(String url) {
        return true;
    }
    public String process(String seed,String url) {
        if(!url.startsWith("http")){
            if(seed.lastIndexOf('.') > 0)
                url = seed.substring(0, seed.lastIndexOf('/')) + "/" + url;
            else{
                if(url.lastIndexOf('.') > 0)
                    url = seed + "/" + url;
                else
                    url = seed + "/" + url + "/";
            }
        }else{
            if(seed.lastIndexOf('.')>0)
                url = url;
            else
                url =  url + "/";
        }
        return url;
    }

    public static void main(String[] args) {
        MyResourseChooser chooser = new MyResourseChooser();
        String process = new MyResourseChooser().process("http://localhost/", "hello.php?name=1&pwd=2");
        System.out.println(process);
    }
}
