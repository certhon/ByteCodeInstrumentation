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
        System.out.println("开始执行测试方法的代码");
        try {
            Thread.sleep(10);//模拟耗时操作
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("执行结束测试方法的代码");
    }
}
