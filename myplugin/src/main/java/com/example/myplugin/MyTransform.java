package com.example.myplugin;


import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.example.myplugin.casper.android.instrument.Main;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.gradle.api.Project;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MyTransform extends Transform {
    private Project mProject;
    private static LinkedList<File> filelist ;
    public MyTransform(Project mProject) {
        this.mProject = mProject;
        filelist = new LinkedList<>();
    }

    @Override
    public String getName() {
        return "MyTransform";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @Override
    public boolean isIncremental() {
        return false;
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        Collection<TransformInput> inputs = transformInvocation.getInputs();
        for (TransformInput input : inputs) {
            Collection<JarInput> jarInputs = input.getJarInputs();
            for (JarInput jarInput : jarInputs) {
                String destName = jarInput.getFile().getName();
                String absolutePath = jarInput.getFile().getAbsolutePath();
                //重命名输出文件（同目录copyFile会冲突）
                byte[] md5Name = DigestUtils.md5(absolutePath);
                if (destName.endsWith(".jar")) {
                    destName = destName.substring(0, destName.length() - 4);
                }
//                File modifyJar = ModifyUtils.modifyJar(jarInput.getFile(), transformInvocation.getContext().getTemporaryDir());
                File modifyJar = null;
                if (modifyJar == null) {
                    modifyJar = jarInput.getFile();
                }
//                System.out.println("jar input name:"+jarInput.getName());
//                System.out.println("jar input file name:"+jarInput.getFile().getName());
//                System.out.println("jar input absolute Path"+jarInput.getFile().getAbsolutePath());

                //获取输出文件
                File dest = transformInvocation.getOutputProvider()
                        .getContentLocation(destName + "_" + md5Name,
                                jarInput.getContentTypes(), jarInput.getScopes(),
                                Format.JAR);
                //中间可以将jarInput.file进行操作

                //copy 到输出目录
                FileUtils.copyFile(modifyJar, dest);


            }
            Collection<DirectoryInput> directoryInputs = input.getDirectoryInputs();
            for (DirectoryInput directoryInput : directoryInputs) {
                File dest = transformInvocation.getOutputProvider()
                        .getContentLocation(directoryInput.getName(),
                                directoryInput.getContentTypes(),
                                directoryInput.getScopes(), Format.DIRECTORY);
                System.out.println("directory dest absolute path:"+dest.getAbsolutePath());
//                System.out.println("directory dest name:"+dest.getName());
                File dir = directoryInput.getFile();
                //插桩
                System.out.println("在这里插桩");
                System.out.println("**********************dir:"+dir.getAbsolutePath());
                Main.instrument(dir.getAbsolutePath(),dest.getAbsolutePath());
                System.out.println("插桩结束");

//                FileUtils.copyDirectory(directoryInput.getFile(),dest);

            }
        }
        System.out.println("This is My Transform!");
    }


}
