package com.sqldd.ifInjection;

import com.sqldd.p0jo.InjectableDomain;
import com.sqldd.p0jo.InjectableDomainType;
import com.sqldd.p0jo.SqlddDomain;
import com.sqldd.payloadOrigin.ErrorPayloads;
import com.sqldd.payloadOrigin.TimeBlindPayloads;
import com.sqldd.payloadOrigin.UnionQueryPayloads;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 探测注入类型
 */
public class DetectType {
    private List<InjectableDomain> injectableDomains;
    public DetectType(List<InjectableDomain> injectableDomains){
        this.injectableDomains = injectableDomains;
    }

    public ArrayList<InjectableDomainType> detectType(){
        ArrayList<InjectableDomainType> injectableDomainTypes = new ArrayList<>();
        for (InjectableDomain injectableDomain : injectableDomains) {
            SqlddDomain sqldd = injectableDomain.getSqlddDomain();
            String url = sqldd.getUrl();
            String method = sqldd.getMethod();
            ArrayList<String> keys = sqldd.getKeys();
            String closure = injectableDomain.getClosure();
            String suf = injectableDomain.getSuf();
            String sufbk = injectableDomain.getSufbk();
            String injectingKey = injectableDomain.getInjectingKey();
            HashMap<String, String> normalParams1 = injectableDomain.getNormalParams1();
            HashMap<String, String> normalParams2 = injectableDomain.getNormalParams2();
            HashMap<String, String> abnormalParams = injectableDomain.getAbnormalParams();

            InjectableDomainType injectableDomainType = new InjectableDomainType();
            injectableDomainType.setInjectableDomain(injectableDomain);
            //探测是否可union注入
            HashMap<String, String> params = new HashMap<String, String>() {{
                for (String key : normalParams1.keySet()) {
                    put(key, normalParams1.get(key));
                }
            }};
            UnionQueryPayloads unionQueryPayloads = new UnionQueryPayloads(url, method, normalParams1.get(injectingKey) + closure, abnormalParams.get(injectingKey) + closure, suf, sufbk, params, injectingKey, normalParams1, normalParams2);
            boolean ifCouldBeUnionInjected = unionQueryPayloads.ifCouldBeInjected();
            if(ifCouldBeUnionInjected == true)
                injectableDomainType.setUnion(true);
            else
                injectableDomainType.setUnion(false);
            System.out.println("探测union结束");
            //探测是否可error注入
            ErrorPayloads errorPayloads = new ErrorPayloads(url, method, normalParams1.get(injectingKey) + closure, abnormalParams.get(injectingKey) + closure, suf, sufbk, params, injectingKey, normalParams1, normalParams2);
            boolean ifCouldBeErrorInjected = errorPayloads.makeErrorBoundarys();
            if(ifCouldBeErrorInjected == true)
                injectableDomainType.setError(true);
            else
                injectableDomainType.setError(false);
            System.out.println("探测error结束");
            //探测是否可time注入
            TimeBlindPayloads timeBlindPayloads = new TimeBlindPayloads(url, method, normalParams1.get(injectingKey) + closure, abnormalParams.get(injectingKey) + closure, suf, sufbk, params, injectingKey, normalParams1, normalParams2);
            boolean ifCouldBeTimeInjected = timeBlindPayloads.filterBoundarys();
            if(ifCouldBeTimeInjected == true)
                injectableDomainType.setTime(true);
            else
                injectableDomainType.setTime(false);
            System.out.println("探测time结束");
            injectableDomainTypes.add(injectableDomainType);
        }
        return injectableDomainTypes;
    }
}
