package com.example.myplugin;

import com.android.build.gradle.AppExtension;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class MyPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        System.out.println("This is My Plugin!!");

        AppExtension extension = (AppExtension)project.getExtensions().getByType(AppExtension.class);
        extension.registerTransform(new MyTransform(project));
    }
}
