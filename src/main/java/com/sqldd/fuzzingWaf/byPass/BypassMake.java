package com.sqldd.fuzzingWaf.byPass;

import com.sqldd.fuzzingWaf.ByPassRule;
import com.sqldd.ifInjection.utils.TestOneSiteSaveUtil;
import com.sqldd.p0jo.InjectableDomain;
import com.sqldd.p0jo.SqlddDomain;

import java.util.List;

public class BypassMake {
    private List<InjectableDomain> injectableDomains;
    public BypassMake(List<InjectableDomain> injectableDomains){
        this.injectableDomains = injectableDomains;
    }
    public boolean bypassMake(){
        boolean findGetBypass = false;
        boolean findPostBypass = false;
        for (InjectableDomain injDD : injectableDomains) {
            if(findGetBypass == true && findPostBypass == true)
                break;
            SqlddDomain sqldd = injDD.getSqlddDomain();
            if(sqldd.getMethod().equals("get") && findGetBypass == false){
                ByPassRule byPassRule = new ByPassRule(sqldd.getUrl(), sqldd.getMethod(), injDD.getNormalParams1().get(injDD.getInjectingKey()) + injDD.getClosure(), injDD.getSuf(), injDD.getSufbk(), injDD.getNormalParams1(), injDD.getInjectingKey(), injDD.getNormalParams1(), injDD.getNormalParams2(), true);
                boolean ifFind = byPassRule.filterBypassDoubleTypeInclude_Orderby("get");//命名order_by_get保存
                if(ifFind == true)
                    findGetBypass = true;
            }
            if(sqldd.getMethod().equals("post") && findPostBypass == false){
                ByPassRule byPassRule = new ByPassRule(sqldd.getUrl(), sqldd.getMethod(), injDD.getNormalParams1().get(injDD.getInjectingKey()) + injDD.getClosure(), injDD.getSuf(), injDD.getSufbk(), injDD.getNormalParams1(), injDD.getInjectingKey(), injDD.getNormalParams1(), injDD.getNormalParams2(), true);
                boolean ifFind = byPassRule.filterBypassDoubleTypeInclude_Orderby("post");//命名order_by_get保存
                if(ifFind == true)
                    findPostBypass = true;
            }
        }
        if( findGetBypass == true || findPostBypass == true)
            return true;
        else
            return false;
    }

    public static void main(String[] args) {
        TestOneSiteSaveUtil testOneSiteSaveUtil = new TestOneSiteSaveUtil();
        List<InjectableDomain> injectableDomains = testOneSiteSaveUtil.getInjectableDomainsFromJson("http://localhost/sqli-labs");//从保存的可注入点文件提取出注入点实体
        BypassMake bypassMake = new BypassMake(injectableDomains);
        boolean b = bypassMake.bypassMake();
    }
}
