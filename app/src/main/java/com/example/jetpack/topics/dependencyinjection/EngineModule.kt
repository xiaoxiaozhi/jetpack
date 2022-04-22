package com.example.jetpack.topics.dependencyinjection

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import javax.inject.Qualifier

/**
 * 给不同子类(或者 实例)提供依赖注入
 *    - 为不同子类创建注解
 *    - 在方法上添加注解，返回相应的子类实例
 */
@Module
@InstallIn(ApplicationComponent::class)
abstract class EngineModule {
    @BindGasEngine
    @Binds
    abstract fun bindGasEngine(gasEngine: GasEngine): Engine

    @BindElectricEngine
    @Binds
    abstract fun bindElectricEngine(electricEngine: ElectricEngine): Engine

}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindGasEngine

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BindElectricEngine