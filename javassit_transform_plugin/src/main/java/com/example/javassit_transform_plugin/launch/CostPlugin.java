package com.example.javassit_transform_plugin.launch;

import com.android.build.gradle.AppExtension;
import com.example.javassit_transform_plugin.ClassTransform;
import com.example.javassit_transform_plugin.extension.CostExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * 方法耗时插件，用来注册Transform
 */
class CostPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("CostPlugin开始执行");
       AppExtension android =  project.getExtensions().getByType(AppExtension.class);
        //创建cost extension
        project.getExtensions().create(CostExtension.EXTENSION_NAME,  CostExtension.class);
        //注册transform
        android.registerTransform(new ClassTransform(project));
//        android.registerTransform(new MyClassTransform(project));
        System.out.println("CostPlugin开始结束");
    }
}
