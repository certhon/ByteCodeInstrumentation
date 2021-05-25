package com.example.javassit_transform_plugin.extension;

import org.gradle.api.Project;

/**
 * 接收额外的输入，如是否需要注入代码
 */
public class CostExtension{

    public static final String EXTENSION_NAME = "cost";

    //默认注入耗时计算
    boolean injectCost = true;


    /**
     * 创建extension
     * @param project
     */
    static void create(Project project){
        project.getExtensions().create(CostExtension.EXTENSION_NAME,  CostExtension.class);
    }

    /**
     * 判断是否需要注入
     * @param project
     * @return
     */
    public static boolean checkInject(Project project){
        return ((CostExtension)project.getExtensions().getByName(CostExtension.EXTENSION_NAME)).injectCost;
    }

}