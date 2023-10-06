package com.my.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import org.gradle.api.Project
import org.apache.commons.io.FileUtils
import java.io.File

/**
 * 1. directoryInput.file.absolutePath = D:\workspace\jetpack\app\build\intermediates\asm_instrumented_project_classes\release
 *    自己项目的class文件都在这个文件夹下面， 例如 项目包名com.example.jetpack.test类名TestActivity，在 directoryInput.file.absolutePath下面都是以文件夹的形式存在com\example\jetpack\test\TestActivity.class
 *    所以我们要使用这个类就要先去除directoryInput.file.absolutePath 然后把\转换成.
 * 2. 使用javassist 要引入类或者jar所在的文件夹，自己项目类所在文件夹就是directoryInput.file.absolutePath， android类呢，在gradle3.2可以通过project.android.bootclasspath获取
 *    在新版gradle已经没有这个api了，不过打印旧版得到D:\AndroidSdk\platforms\android-31\android.jar。所以我们在新版gradle中直接饮用即可
 *
 *
 */
class ModifyTransform(val project: Project) : Transform() {

    val pool by lazy {
        ClassPool.getDefault().apply {
            //加入android.jar到索引，否则 遍历到onCreate(android.os.Bundle)  Bundle 会报找不到异常
            //  //新版找不到 android.bootClasspath[0].toString()但是打印旧版得到D:\AndroidSdk\platforms\android-31\android.jar
            appendClassPath("""D:\AndroidSdk\platforms\android-31\android.jar""");
        }
    }
//    val pool = ClassPool.getDefault()

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        if (transformInvocation?.isIncremental == false) {
            transformInvocation.outputProvider.deleteAll()
        }
        transformInvocation?.inputs?.forEach { input ->
//            println("TransformInput----${input.toString()}")
            input.directoryInputs.forEach { directoryInput: DirectoryInput ->
                pool.appendClassPath(directoryInput.file.absolutePath)
//                pool.insertClassPath(directoryInput.file.absolutePath)//这俩方法有什么区别呢？？？？
                transformInvocation.outputProvider.getContentLocation(
                    directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY
                ).apply {
                    findTarget(directoryInput.file, directoryInput.file.absolutePath)
                    FileUtils.copyDirectory(directoryInput.file, this)
                    println("directoryInput.file---${directoryInput.file}-${directoryInput.file.isDirectory}---dest-------$this")
                }
            }
            input.jarInputs.forEach { jarInput ->
                transformInvocation.outputProvider.getContentLocation(
                    jarInput.name, jarInput.contentTypes, jarInput.scopes, Format.JAR
                ).apply {
                    FileUtils.copyFile(jarInput.file, this)
//                    Files.copy(jarInput.file.toPath(), this.outputStream())
//                    println("jarInputs----$this-----${jarInput.file.isDirectory}")
                }
            }

        } ?: println("ModifyTransform---inputs is null")

    }

    private fun findTarget(dir: File, parentDir: String) {
        if (dir.isDirectory) {
            dir.listFiles()?.forEach {
                findTarget(it, parentDir)
            }
        } else {
            val filePath = dir.absolutePath
            if (filePath.endsWith(".class")) {
                modify(filePath, parentDir)
            }
        }
    }

    /**
     *  if (!filePath.contains("""topics\camera""")) 通过这段代码控制 对哪个包下面的Activity插桩
     */
    private fun modify(filePath: String, fileName: String) {
        println("modify1--filePath--${filePath}----fileName---${fileName}")
        // 过滤这些文件
        if (filePath.contains("R$") || filePath.contains("R.class") || filePath.contains("BuildConfig.class")) {
            return
        }
        if (!filePath.contains("""topics\camera""")) {
            return
        }
        //ObserveWorker$doWork$1.class 查看该文件夹，却找不到这样的内部类，不晓得这是什么，所以在这里过滤掉
        if (filePath.contains("$")) {
            return
        }
        if (filePath.contains("CameraSizesKt")) {//这个文件一直编译不通过，怀疑是因为里面有assert
            return
        }
        println("modify2--filePath--${filePath}----fileName---${fileName}")
        // 获取 .class 的文件名
        val className = filePath.replace(fileName, "").replace("\\", ".").replace("/", ".")
        val name = className.replace(".class", "").substring(1)

        // /Users/TaoWang/Desktop/javassist_demo/javassist_android_demo/app/build/intermediates/javac/debug/classes/com/watayouxiang/javassistdemo/MainActivity.class
        println("filePath -------------> $filePath")
        // /Users/TaoWang/Desktop/javassist_demo/javassist_android_demo/app/build/intermediates/javac/debug/classes
        println("fileName -------------> $fileName")
        // com.watayouxiang.javassistdemo.MainActivity
        println("name -------------> $name")

        // 给 .class 文件添加代码
        pool.get(name)
        val ctClass: CtClass = pool.get(name)
        addCode(ctClass, fileName)
    }

    private fun addCode(ctClass: CtClass, fileName: String) {
        ctClass.defrost()// 解冻一个类，使其可以被修改
        // 获取所有方法
        ctClass.declaredMethods.filter { method -> method.name == "onCreate" }.forEach { method ->
            println("---------------> method: " + method.name + ", 参数个数: " + method.parameterTypes.size)
            if (method.parameterTypes.size == 1) {
                method.insertBefore("{ System.out.println(\"hello javassist\"+\$1);}")
            }
            if (method.parameterTypes.size == 2) {
                method.insertBefore("{ System.out.println(\$1); System.out.println(\$2);}")
            }
            if (method.parameterTypes.size == 3) {
                method.insertBefore("{ System.out.println(\$1); System.out.println(\$2); System.out.println(\$3);}")
            }
        }

        // 将修改后的代码写回去
        ctClass.writeFile(fileName)
        // 释放资源
        ctClass.detach()
    }

    override fun getName(): String {
        return "david"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }
}