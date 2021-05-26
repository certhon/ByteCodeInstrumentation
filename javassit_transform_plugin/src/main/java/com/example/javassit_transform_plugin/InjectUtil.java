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
import javassist.NotFoundException;

class InjectUtil {

    private static final String COST_SUFFIX = "$$Impl";

    static final ClassPool sClassPool = ClassPool.getDefault();


    static void injectCost(File baseClassPath, Project project) throws NotFoundException, CannotCompileException {
        System.out.println("baseClassPath getPath " + baseClassPath.getAbsolutePath());
        System.out.println("baseClassPath getName " + baseClassPath.getName());
        //把类路径添加到classpool
        try {
            System.out.println("把类路径添加到classpool :" + baseClassPath.getPath());
            sClassPool.appendClassPath(baseClassPath.getPath());
            sClassPool.appendClassPath(baseClassPath.getAbsolutePath());
        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        //添加Android相关的类
        AppExtension android = project.getExtensions().getByType(AppExtension.class);
//        sClassPool.appendClassPath((android.getBootClasspath()).get(0).toString());
//        System.out.println("安卓路径 android.getBootClasspath() :" + android.getBootClasspath().toString());
        traverseFile(baseClassPath, baseClassPath);
//        if (baseClassPath.isDirectory()) {
//            //遍历文件获取类
//            System.out.println("baseClassPath.isDirectory() :");
//
//            File[] fs = baseClassPath.listFiles();	//遍历path下的文件和目录，放在File数组中
//            for(File f:fs){					//遍历File[]数组
//                System.out.println("遍历filepath  :"+f.getName());
//
//                if (check(f)) {
//                    System.out.println("find class : ${classFile.path}"+f.getPath());
//                    String className = convertClass(baseClassPath.getPath(), f.getPath());
//                    System.out.println("className : ${className}  "+className);
//                    inject(baseClassPath.getPath(),className);
//                }
//        }

    }

    private static void traverseFile(File baseClassPath, File file) throws NotFoundException, CannotCompileException {
        File[] fs = file.listFiles();
        for (File f : fs) {
            if (f.isDirectory()) {    //若是目录，则递归
                if (f.getName().contains("META-INF")) {
                    System.out.println("文件夹 META-INF 跳过  :" + f.getName());

                    continue;
                }
                System.out.println("文件夹 继续遍历  :" + f.getName());

                traverseFile(baseClassPath, f);
            } else if (f.isFile()) {
                System.out.println("遍历filepath  :" + f.getName());

                if (check(f)) {
//                    System.out.println("find class : ${classFile.path}" + f.getPath());
                    //把.class去掉  然后把\替换成.
                    String className = convertClass(baseClassPath.getPath(), f.getPath());

                    System.out.println("className : ${className}  " + className);

                    //代码插入
                    inject(file.getPath(), className);
                }
            }
        }
    }

    /**
     * 向目标类注入耗时计算代码,生成同名的代理方法，在代理方法中调用原方法计算耗时
     *
     * @param baseClassPath 写回原路径
     * @param clazz
     */
    private static void inject(String baseClassPath, String clazz) throws NotFoundException, CannotCompileException {
        CtClass ctClass = null;
        try {
            ctClass = sClassPool.get(clazz);
//            System.out.println("准备注入代码" + ctClass.getName());

        } catch (NotFoundException e) {
            e.printStackTrace();
        }
        //解冻
        if (ctClass.isFrozen()) {
            ctClass.defrost();
        }
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            //找到声明了MethodCost注解的方法

            if (ctMethod.hasAnnotation(MethodCost.class)) {
                System.out.println("准备注入代码" + ctClass.getName());

                //生成一个本地变量 long类型 名字为 start  即 long start
                ctMethod.addLocalVariable("start", CtClass.longType);

                //在方法前面给start赋值为当前时间
                ctMethod.insertBefore("{start = System.currentTimeMillis();}");

                String end = "System.out.println(\"" + ctClass.getName() + "类的\"" + "\"" + ctMethod.getName() + "方法useTime=\" +(System.currentTimeMillis()-start));";
                System.out.println(end);
                //在方法结尾 计算时间差 并打印 处理
                ctMethod.insertAfter(end);
            }
        }
        try {
            //保存class
            ctClass.writeFile(baseClassPath);
            System.out.println("writeFile success");
        } catch (CannotCompileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("detach ctClass");

        ctClass.detach();//释放
    }

    /**
     * 生成代理方法体，包含原方法的调用和耗时打印
     *
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
        if (!"void".equals(returnType)) {
            //处理返回值
            methodResult = "${returnType} result = " + methodResult;
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
        System.out.println("classPath   :" + classPath);

        String packagePath = classPath.substring(baseClassPath.length() + 1);
        //把 / 替换成.
        String clazz = packagePath.replaceAll("/", ".");
        //去掉.class 扩展名

        String substring = clazz.substring(0, packagePath.length() - ".class".length());
        System.out.println("convertClass   :" + substring);

        return substring;
    }


    //过滤掉一些生成的类
    private static boolean check(File file) {
        if (file.getName().contains("META-INF")) {
            System.out.println("META-INF跳过   :" + file.getName());

            return false;
        }
        if (file.isDirectory()) {
            System.out.println("文件夹跳过   :" + file.getName());

            return false;
        }

        String filePath = file.getPath();
//        System.out.println("文件名字   :" + file.getName());

        return !filePath.contains("R$") &&
                !filePath.contains("R.class") &&
                !filePath.contains("BuildConfig.class");
    }

}