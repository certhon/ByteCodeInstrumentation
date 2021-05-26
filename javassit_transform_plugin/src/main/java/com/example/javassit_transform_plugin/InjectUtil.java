package com.example.javassit_transform_plugin;

import com.android.build.gradle.AppExtension;
import com.example.costannotation.MethodCost;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

class InjectUtil {

    private static final String COST_SUFFIX = "$$Impl";

    static final ClassPool sClassPool = ClassPool.getDefault();


    static void injectCost(File baseClassPath, Project project) throws NotFoundException {
        System.out.println("baseClassPath getPath "+baseClassPath.getAbsolutePath());
        System.out.println("baseClassPath getName "+baseClassPath.getName());
        //把类路径添加到classpool
        try {
            System.out.println("把类路径添加到classpool :"+baseClassPath.getPath());
            sClassPool.appendClassPath(baseClassPath.getPath());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        //添加Android相关的类
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
        sClassPool.appendClassPath((android.getBootClasspath()).get(0).toString());
        System.out.println("android.getBootClasspath() :"+android.getBootClasspath().toString());

        if (baseClassPath.isDirectory()) {
            //遍历文件获取类
            System.out.println("baseClassPath.isDirectory() :");

            File[] fs = baseClassPath.listFiles();	//遍历path下的文件和目录，放在File数组中
            for(File f:fs){					//遍历File[]数组
                System.out.println("遍历filepath  :"+f.getName());

                if (check(f)) {
                    System.out.println("find class : ${classFile.path}"+f.getPath());
                    String className = convertClass(baseClassPath.getPath(), f.getPath());
                    System.out.println("className : ${className}  "+className);
                    inject(baseClassPath.getPath(),className);
                }
        }

    }}

    /**
     * 向目标类注入耗时计算代码,生成同名的代理方法，在代理方法中调用原方法计算耗时
     * @param baseClassPath 写回原路径
     * @param clazz
     */
    private static void inject(String baseClassPath, String clazz) throws NotFoundException {
        CtClass ctClass = null;
        try {
            ctClass = sClassPool.get(clazz);
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        //解冻
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            if (ctMethod.hasAnnotation(MethodCost.class)) {
                System.out.println( "before ${ctMethod.name}"+ctMethod.getName());
                //把原方法改名，生成一个同名的代理方法，添加耗时计算
                String name = ctMethod.getName();
                String newName = name + COST_SUFFIX;
                System.out.println( "after ${newName}"+newName);
                String body = generateBody(ctClass, ctMethod, newName);
                System.out.println( "generateBody : ${body}"+body);
                //原方法改名
                ctMethod.setName(newName);
                //生成代理方法
                try {
                    CtMethod make = CtNewMethod.make(ctMethod.getModifiers(), ctMethod.getReturnType(), name, ctMethod.getParameterTypes(), ctMethod.getExceptionTypes(), body, ctClass);
                    //把代理方法添加进来
                    ctClass.addMethod(make);
                } catch (CannotCompileException e) {
                    e.printStackTrace();
                } catch (NotFoundException e) {
                    e.printStackTrace();
                }

            }
        }

        try {
            ctClass.writeFile(baseClassPath);
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ctClass.detach();//释放
    }

    /**
     * 生成代理方法体，包含原方法的调用和耗时打印
     * @param ctClass
     * @param ctMethod
     * @param newName
     * @return
     */
    private static String generateBody(CtClass ctClass, CtMethod ctMethod, String newName) throws NotFoundException {
        //方法返回类型
            String returnType = ctMethod.getReturnType().getName();
        System.out.println(returnType);
        //生产的方法返回值
        String methodResult = "${newName}($$);";
        if (!"void".equals(returnType)){
            //处理返回值
            methodResult = "${returnType} result = "+ methodResult;
        }
        System.out.println(methodResult);
        return "{long costStartTime = System.currentTimeMillis();" +
                //调用原方法 xxx$$Impl() $$表示方法接收的所有参数
                methodResult +
                "android.util.Log.e(\"METHOD_COST\", \"${ctClass.name}.${ctMethod.name}() 耗时：\" + (System.currentTimeMillis() - costStartTime) + \"ms\");" +
                //处理一下返回值 void 类型不处理
                ("void".equals(returnType) ? "}" : "return result;}");

    }

    private static String convertClass(String baseClassPath, String classPath) {
        //截取包之后的路径
        String packagePath = classPath.substring(baseClassPath.length() + 1);
        //把 / 替换成.
        String clazz = packagePath.replaceAll("/", ".");
        //去掉.class 扩展名

        return clazz.substring(0, packagePath.length() - ".class".length());
    }


    //过滤掉一些生成的类
    private static boolean check(File file) {
        if (file.isDirectory()) {
            System.out.println("文件夹跳过   :"+file.getName());

            return false;
        }

        String  filePath = file.getPath();
        System.out.println("文件名字   :"+file.getName());

        return !filePath.contains("R$") &&
                !filePath.contains("R.class") &&
                !filePath.contains("BuildConfig.class");
    }

}