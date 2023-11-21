package com.example.jetpack.architecturecomponent.uilibs.databinding

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import androidx.databinding.ObservableField
import  com.example.jetpack.BR

/**
 * 实现 BaseObservable 的数据类负责在属性更改时发出通知,具体操作过程是向 getter 分配 Bindable 注释，然后在 setter 中调用 notifyPropertyChanged() 方法，
 * 1. 每一个都要写  @get:Bindable以及set方法非常麻烦，dataBinding提供了ObservableField<T> 可观察字段。提供相同的功能且不用继承BaseObservable [可观察字段类型](https://developer.android.google.cn/topic/libraries/data-binding/observability#observable_fields)
 * note:BR类需要手动导入 import  包名.BR
 */
class Student : BaseObservable() {
    @get:Bindable
    public var age: Int = 1
        set(value) {
            field = value
            notifyPropertyChanged(BR.age)//只更新当前属性
        }

    @get:Bindable
    public var name: String = ""
        set(value) {
            field = value
            notifyChange() //更新全部属性
        }


    var height: ObservableField<Int> =
        ObservableField(0)//使用get或者set为height更新数据。height =  ObservableField(190) 这种形式不能更新UI，相当于换了一个内存实体
    var photo: ObservableField<Int> = ObservableField(0)
    var content: ObservableField<String> = ObservableField()
    var content1: String? = null
}