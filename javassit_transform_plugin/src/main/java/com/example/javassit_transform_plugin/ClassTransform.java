package com.example.javassit_transform_plugin;

import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.example.javassit_transform_plugin.extension.CostExtension;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import javassist.NotFoundException;

/**
 * 编译过程中处理class文件
 */
public class ClassTransform extends Transform {

    Project mProject;

    boolean inject = false;

    public ClassTransform(Project project) {
        mProject = project;
        System.out.println("ClassTransform 创建");
    }

    @Override
    public String getName() {
        return ClassTransform.class.getName();
    }

    //输入类型，这里只处理class文件
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        System.out.println("getInputTypes 创建");

        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        System.out.println("getScopes 创建");

        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        System.out.println("---- transform start ----");
        inject = CostExtension.checkInject(mProject);
        System.out.println("injectCost = "+inject);
        Iterator<TransformInput> iterator = transformInvocation.getInputs().iterator();
        System.out.println("文件数量 = "+transformInvocation.getInputs().size());

        // 输出集合中的所有元素
        while (iterator.hasNext()) {
            TransformInput input = iterator.next();
            Iterator<DirectoryInput> directoryInputIterator = input.getDirectoryInputs().iterator();
            while (directoryInputIterator.hasNext()) {
                DirectoryInput directoryInput = directoryInputIterator.next();
                System.out.println("输入文件名 = "+directoryInput.getName());
                if (inject) {
                    try {
                        InjectUtil.injectCost(directoryInput.getFile(), mProject);
                    } catch (NotFoundException e) {
                        e.printStackTrace();
                    }
                }
                // 将input的目录复制到output指定目录 否则运行时会报ClassNotFound异常
                File contentLocation = transformInvocation.getOutputProvider().getContentLocation(directoryInput.getName(), directoryInput.getContentTypes(),
                        directoryInput.getScopes(), Format.DIRECTORY);
                FileUtils.copyDirectory(directoryInput.getFile(), contentLocation);

            }

            //不处理jar文件
            Iterator<JarInput> jarIterator1 = input.getJarInputs().iterator();
            while (jarIterator1.hasNext()) {
                JarInput jarInput = jarIterator1.next();
                // 重命名输出文件（同目录copyFile会冲突）
                String jarName = jarInput.getName();
                String md5Name = DigestUtils.md5Hex(jarInput.getFile().getAbsolutePath());
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4);
                }
                File dest = transformInvocation.getOutputProvider().getContentLocation(jarName + md5Name, jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);
                FileUtils.copyFile(jarInput.getFile(), dest);
            }


        }
        System.out.println("---- transform end ----");
    }
}