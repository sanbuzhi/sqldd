package com.sqldd.utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class StringSubSame{

    // 交集
    public String intersection(String a, String b) {
        //  a的第i个和b的第j个最大公共子序列长度：如果第i和第j个字符相同，则为a的i-1和b的j-1最大公共子序列长度+1.
        //否则取 a的第i-1个b的j个与a的i个b的j-1个中最大的。因为对比a[i-1][j-1],要不是新增的a[i]个字符与前b[j-1]的字符产生了新的匹配，要不是增加的b[j]个字符与a[i-1]个字符产生了新的匹配。如果新增的a[i]和b[j]都贡献了，（如字符串ad和da，在i=1，j=1时，a[1]=d与下面的字符产生了匹配，同时b[1]=a与前者产生匹配）。因为是序列，新增的匹配要作为最后一位，所以只能任选一个作为新增的匹配。
        if(a==null || b==null || a.length() == 0 || b.length() == 0)
            return "";
        int[][] matrix = new int[a.length() + 1][b.length() + 1];
        for (int i = 0; i < a.length(); i++) {
            for (int j = 0; j < b.length(); j++) {
                if (a.charAt(i) == b.charAt(j)) {
                    int maxLen = (i == 0 || j == 0) ? 0 : matrix[i - 1][j - 1];
                    matrix[i][j] = maxLen + 1;
                } else {
                    int maxLen = (i == 0 || j == 0) ? 0 : Math.max(
                            matrix[i - 1][j], matrix[i][j - 1]);
                    matrix[i][j] = maxLen;
                }
            }
        }

        int i = a.length() - 1;
        int j = b.length() - 1;
        ArrayList<Character> restList = new ArrayList<Character>();
        while (i >= 0 && j >= 0) {
            if (a.charAt(i) == b.charAt(j)) {
                restList.add(a.charAt(i));
                i--;
                j--;
            } else {
                if (matrix[i - 1][j] >= matrix[i][j - 1]) {
                    i--;
                } else {
                    j--;
                }
            }
        }
        StringBuilder reBuilder = new StringBuilder(restList.size());
        for (int k = restList.size() - 1; k >= 0; k--) {
            reBuilder.append(restList.get(k));
        }
        return reBuilder.toString();
    }

    //差集
    public String difference(String a,String b){
        String s = intersection(a, b);
        StringBuilder builder = new StringBuilder(a);
        int sIdx= 0,htmlOneIdx=0;
        while(sIdx < s.length()){
            if(a.charAt(htmlOneIdx) == s.charAt(sIdx)){
                sIdx++;
                htmlOneIdx++;
            }else if(a.charAt(htmlOneIdx) != s.charAt(sIdx)){
                builder.append(a.charAt(htmlOneIdx));
                htmlOneIdx++;
            }
        }
        return builder.toString();
    }


    public static void main(String[] args) {
        StringSubSame stringSubSame = new StringSubSame();
        String str = "aaccaaeccefg";
        String pa = "aacceg";

        String commonStrLength = stringSubSame.intersection(str, pa);
        System.out.println(commonStrLength);
    }
}
