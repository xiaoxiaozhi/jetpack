package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.content.Intent
import android.view.View
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class DataBindingViewModel(val savedStateHandle: SavedStateHandle) : ViewModel(), Observable {
    val data: MutableLiveData<String>
        get() {
            if (savedStateHandle.contains("dataBinding")) {
                savedStateHandle.set("dataBinding", "123")
            }
            return savedStateHandle.getLiveData("dataBinding")
        }
    val dataInt: Int = 0


    //事件处理---方法引用
    fun onClickFriend(view: View) {
        println("事件处理之方法引用")
    }

    //事件处理---lambda表达式
    fun onClickBind() {
        println("事件处理监听器绑定(lambda)")
    }

    fun intentDataBinding2(view: View) {
        view.apply {
            context.startActivity(Intent(context, DataBinding2Activity::class.java))
        }
    }

    /**
     * A ViewModel that is also an Observable,
     * to be used with the Data Binding Library.
     */

    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()

    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.add(callback)
    }

    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback
    ) {
        callbacks.remove(callback)
    }

    /**
     * Notifies observers that all properties of this instance have changed.
     */
    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    /**
     * Notifies observers that a specific property has changed. The getter for the
     * property that changes should be marked with the @Bindable annotation to
     * generate a field in the BR class to be used as the fieldId parameter.
     *
     * @param fieldId The generated BR id for the Bindable field.
     */
    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }


}