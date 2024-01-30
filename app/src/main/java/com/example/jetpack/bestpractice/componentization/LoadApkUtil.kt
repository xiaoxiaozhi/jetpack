package com.example.jetpack.bestpractice.componentization

import android.content.Context
import android.util.Log
import dalvik.system.DexClassLoader
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.Modifier


/**
 * DexClassLoader和PathClassLoader 在Android8之后没有区别
 * 一般来说程序员使用DexClassLoader  系统使用PathClassLoader
 * BaseDexClassLoader 加载完APK后会把dex文件存放在DexPathList pathList的内部数组Element[] dexElements;
 * 想办法把插件apk的dex加载到 系统中
 * 1. 取得系统的dexElements
 * 2. 自定义的加载器加载插件APK后也会生成一个dexElements，取到
 * 3. 合并后赋给系统的dexElements
 * 4. 执行插件中的方法----发现kt结尾的文件会报错，待议
 *
 */
class LoadApkUtil {
    fun combineDex(context: Context) {
        //1.取得系统的dexElements
        val classLoaderclass = Class.forName("dalvik.system.BaseDexClassLoader");
        val pathlistField = classLoaderclass.getDeclaredField("pathList")
        pathlistField.isAccessible = true

        val pathClassLoader = context.classLoader
        val pathList = pathlistField.get(pathClassLoader)// BaseDexclassLoader:pathlist

        val dexPathlistclass = Class.forName("dalvik.system.DexPathList")
        val dexElementsField: Field = dexPathlistclass.getDeclaredField("dexElements")
        dexElementsField.isAccessible = true
        val dexElements = dexElementsField.get(pathList) as Array<Any>//获取

        // 2.取到插件的dexElements
        val pluginClassLoader = DexClassLoader(
            File(context.cacheDir, "app-debug.apk").absolutePath, context.cacheDir.absolutePath, null, pathClassLoader
        )
        val pluginPathList = pathlistField.get(pluginClassLoader)
        val pluginDexElements = dexElementsField.get(pluginPathList) as Array<Any>//取到
        //3. 合并
        val newArray = java.lang.reflect.Array.newInstance(
            dexElementsField.type.componentType, dexElements.size + pluginDexElements.size
        )
        System.arraycopy(dexElements, 0, newArray, 0, dexElements.size)
        System.arraycopy(pluginDexElements, 0, newArray, dexElements.size - 1, pluginDexElements.size)
        dexElementsField.set(pathList, newArray)

        //4. 调用插件方法
        val pluginClass = Class.forName("com.kotlincode.myCoroutine.Abig")
        val pluginMethod = pluginClass.getDeclaredMethod("ppp")
        pluginMethod.isAccessible = true
        Log.i(TAG, "m2----${pluginMethod.invoke(pluginClass.newInstance())}")
        //TODO kt结尾的文件怎么 调用方法呢？一直报错

//        printMethods(pluginClass)
//        pluginClass.newInstance()
//        Log.i(TAG,"m2----${pluginMethod.invoke(pluginClass.newInstance(), 11)}")
//        pluginClass.getConstructor()

//        pluginClass.declaredConstructors.forEach {
//            println("构造函数名称---${it.name} 参数 ")
//            it.parameterTypes.forEach { parameter ->
//                println("参数---${parameter.name}  ")
//            }
//        }
//        pluginClass.getDeclaredConstructor()
    }

    fun printMethods(obj: Class<out Any>) {
//    val cl: Class<*> = obj.javaClass
        val methods = obj.declaredMethods
        for (m in methods) {
            val retType = m.returnType
            val name = m.name
            val modifiers = Modifier.toString(m.modifiers)
            print(modifiers + "\t" + retType.simpleName + "\t" + name)
            val paramTypes = m.parameterTypes
            for (j in paramTypes.indices) {
                if (j == 0) print("(")
                print("\t" + paramTypes[j].name)
            }
            print(" )")
            //            m.invoke(obj,)
            println("")
        }
    }

    companion object {
        const val TAG = "LoadClass111"
    }
}