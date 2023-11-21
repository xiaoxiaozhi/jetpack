package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.content.Intent
import android.view.View
import androidx.databinding.Observable
import androidx.databinding.PropertyChangeRegistry
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class DataBindingViewModel(val savedStateHandle: SavedStateHandle) : ViewModel() ,Observable{
    val data: MutableLiveData<String>
        get() {
            if (savedStateHandle.contains("dataBinding")) {
                savedStateHandle.set("dataBinding", "123")
            }
            return savedStateHandle.getLiveData("dataBinding")
        }
    val dataInt = MutableLiveData<Int>(0)




    private val callbacks: PropertyChangeRegistry = PropertyChangeRegistry()
    override fun addOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback) {
        callbacks.add(callback)
    }
    override fun removeOnPropertyChangedCallback(
        callback: Observable.OnPropertyChangedCallback) {
        callbacks.remove(callback)
    }
    fun notifyChange() {
        callbacks.notifyCallbacks(this, 0, null)
    }

    fun notifyPropertyChanged(fieldId: Int) {
        callbacks.notifyCallbacks(this, fieldId, null)
    }
}