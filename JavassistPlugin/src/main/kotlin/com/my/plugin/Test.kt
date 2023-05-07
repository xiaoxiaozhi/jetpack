package com.my.plugin

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import org.gradle.api.Plugin
import org.gradle.api.Project

class Test : Plugin<Project> {
    override fun apply(project: Project) {
//        project.task("atguigu") {
//            it.doLast {
//                println("自定义atguigu插件")
//            }
//        }
        project.extensions.getByType(AppExtension::class.java).registerTransform(ModifyTransform(project))

    }
}
//class Test implements Plugin<Project> {
//
//    @Override
//    void apply(Project project) {
//        project.extensions.getByType(AppExtension::class.java).registerTransform(ModifyTransform(project))
//        project.task("atguigu") {
//            doLast {
//                println("自定义atguigu插件")
//            }
//        }
//    }
//}