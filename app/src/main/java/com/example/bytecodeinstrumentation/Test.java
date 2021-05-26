package com.example.bytecodeinstrumentation;

import com.example.costannotation.MethodCost;

public class Test {
    public int addTest() {
        int a1 = 200;
        int a2 = 404;
        int a3 = 500;
        return a1 + a2 + a3;
    }

    //测试方法耗时
    @MethodCost
    public void test(){
        try {
            Thread.sleep(5);//模拟耗时操作
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
