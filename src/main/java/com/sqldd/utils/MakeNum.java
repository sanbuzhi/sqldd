package com.sqldd.utils;

import java.util.ArrayList;

public class MakeNum {
    //给定长度，返回最高位介于[minbit,maxbit]的随机数
    public String tools_getRandomNumber(int maxBitMaxNum, int maxBitMinBit,int Length){
        int rst = (int) (10000 + Math.random() * (90000 - 1 + 1));
        while(rst/10000 > maxBitMaxNum || rst/10000 < maxBitMinBit){
            rst = (int) (10000 + Math.random() * (90000 - 1 + 1));
        }
        return ""+rst;
    }

    public static void main(String[] args) {
        MakeNum makeNum = new MakeNum();
        MakeStr makeStr = new MakeStr();
        String numPlusStrRandom = makeNum.tools_getRandomNumber(8, 6, 5) +" "+ makeStr.tools_getStrsRandom(5);
        System.out.println(numPlusStrRandom);
    }
}
