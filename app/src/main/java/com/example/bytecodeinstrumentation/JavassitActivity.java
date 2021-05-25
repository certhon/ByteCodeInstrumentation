package com.example.bytecodeinstrumentation;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class JavassitActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    hello();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });

    }

    public  void hello() throws Exception{

        Class<?> aClass = Class.forName("com.example.bytecodeinstrumentation.Test");
        Test hello = (Test) aClass.newInstance();
        hello.test();


//        ClassPool cp = ClassPool.getDefault();
//        //获取要修改的类
//        CtClass cc = cp.get("com.example.bytecodeinstrumentation.Test");
//        //获取要修改的方法
//        CtMethod m = cc.getDeclaredMethod("test");
//        //定义局部变量
//        m.addLocalVariable("time",CtClass.longType);
//        m.insertBefore("time = System.currentTimeMillis();");
//        //在sayHi方法结束前插入代码
//        m.insertAfter("time =  System.currentTimeMillis() - time;System.out.println(\"耗时：\"+time+\"毫秒\");");
//        //保存.class文件
//        cc.writeFile();
//        //测试调用
//        Class c = cc.toClass();
//        Test hello = (Test) c.newInstance();
//        hello.test();
    }
}
