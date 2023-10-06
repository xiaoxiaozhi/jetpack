package com.example.compiler

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*

/**
 * [对照表](https://kotlinlang.org/docs/ksp-reference.html#program-elements)
 * [kotlin语法树](https://kotlinlang.org/docs/ksp-overview.html#how-ksp-looks-at-source-files),Resolver遍历 看这个
 * TypeElement----KSClassDeclaration   表示类或接口程序元素。提供对有关类型及其成员的信息的访问
 * QualifiedNameable----KSDeclaration  类的完全限定名 包名+类名
 * KSAnnotated----AnnotatedConstruct
 * PackageElement----KSFile            表示一个包程序元素。提供对有关包及其成员的信息的访问
 * ExecuteableElement----KSFunctionDeclaration表示某个类或接口的方法、构造方法或初始化程序（静态或实例），包括注释类型元素
 * TypeElement----KSClassDeclaration   表示一个类或接口程序元素。提供对有关类型及其成员的信息的访问。注意，枚举类型是一种类，而注解类型是一种接口
 * VariableElement----KSVariableParameter/KSPropertyDeclaration 表示一个字段、enum 常量、方法或构造方法参数、局部变量或异常参数
 * TODO 目前掌握直接遍历KSFile获取 注解值和类全限定名 未掌握 resolver.getSymbolsWithAnnotation()和KSVisitorVoid。这两种遍历kotlin语法树方式
 *
 */
class Processor(val codeGenerator: CodeGenerator, val logger: KSPLogger, val options: Map<String, String>) :
    SymbolProcessor {
    //    val functions = mutableListOf<KSClassDeclaration>()
    val visitor = FindFunctionsVisitor()

    override fun process(resolver: Resolver): List<KSAnnotated> {
//        val symbols: Sequence<KSClassDeclaration> =
//            resolver.getSymbolsWithAnnotation(com.example.annotation.Function::class.java.name)
//                .filterIsInstance<KSClassDeclaration>()
//        logger.warn("sd")
//
//        resolver.getAllFiles().forEach { it.accept(visitor, Unit) }
//        logger.warn("test warn run")
//        return emptyList()

        resolver.getAllFiles().forEach { ksFile ->
            //所有被注解过的类,内部类不在此范畴
            logger.warn("KSFile-----${ksFile.packageName.asString()}--${ksFile.fileName}--${ksFile.filePath}")
            //类注解没有在这里
            ksFile.annotations.takeIf { it.count() > 0 }?.forEach { ksAnnotation ->
                logger.warn("ksAnnotation-----${ksAnnotation.shortName}")
            } ?: logger.warn("Sequence<KSAnnotation>-----is empty")
            //类的注解在这里

            ksFile.declarations.flatMap { ksDeclare ->
                logger.warn("qualifiedName---${ksDeclare.qualifiedName?.asString()}")//类的完全限定名
                ksDeclare.annotations
            }.forEach { ksAnnotation ->
                logger.warn("annotation-----${ksAnnotation.shortName.asString()} value---${ksAnnotation.arguments.first().value}")//注解值
            }
            ksFile.accept(visitor, Unit)
        }
        return emptyList()
    }

    inner class FindFunctionsVisitor : KSVisitorVoid() {
        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.qualifiedName?.asString()
            classDeclaration.isCompanionObject
            classDeclaration.getDeclaredFunctions().map {
                it.accept(this, Unit)
            }
        }

        override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
//        function.add(function)

        }

        override fun visitFile(file: KSFile, data: Unit) {
            file.declarations.map { it.accept(this, Unit) }
        }
    }
}


