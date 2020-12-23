package com.sqldd.p0jo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class InjectableDomain {
    private SqlddDomain sqlddDomain;
    private HashMap<String,String> normalParams1 = null;
    private HashMap<String,String> normalParams2 = null;
    private HashMap<String,String> abnormalParams = null;
    private String injectingKey = null;
    private String closure = null;
    private String sufbk = null;
    private String suf = null;
}
