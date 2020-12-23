package com.sqldd.p0jo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 可注入类型
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InjectableDomainType {
    private InjectableDomain injectableDomain = null;
    private boolean union;
    private boolean error;
    private boolean time;
}
