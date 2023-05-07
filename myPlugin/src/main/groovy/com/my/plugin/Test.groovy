package com.my.plugin

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Test implements Plugin<Project> {
    @Override
    public void apply(Project project) {
//        project.task("atguigu"){
//            doLast{
//                println("自定义atguigu插件")
//            }
//        }
        AppExtension android = project.extensions.getByType(AppExtension)
        android.registerTransform(new ModifyTransform(project))
    }
}
