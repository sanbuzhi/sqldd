package com.sqldd.p0jo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 保存了每个url下的漏洞检测点
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlddDomain {
    private String url = null;
    private String method = null;
    private ArrayList<String> keys = null;
}
