package com.example.jetpack.architecturecomponent.uilibs.paging

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.flow.Flow

class MainViewModel : ViewModel() {

    fun getPagingData(): Flow<PagingData<Repo>> {
        //cachedIn 这是用于将服务器返回的数据在viewModelScope这个作用域内进行缓存，假如手机横竖屏发生了旋转导致Activity重新创建，
        // Paging 3就可以直接读取缓存中的数据，而不用重新发起网络请求了。
        return Repository.getPagingData().cachedIn(viewModelScope)//
    }

}
