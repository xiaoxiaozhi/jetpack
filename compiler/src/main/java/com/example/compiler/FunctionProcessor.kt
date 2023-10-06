package com.example.compiler

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * https://blog.csdn.net/lyabc123456/article/details/128531692
 * SymbolProcessorProvider 入口
 * codeGenerator：可以用来生成代码文件
 * logger：可以用来输出日志
 * options：可以用来接受命令行或Gradle插件中的配置参数
 */
class FunctionProcessor : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return Processor(environment.codeGenerator, environment.logger, environment.options)
    }
}