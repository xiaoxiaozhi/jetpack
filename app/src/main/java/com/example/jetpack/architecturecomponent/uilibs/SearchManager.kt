package com.example.jetpack.architecturecomponent.uilibs

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner

/**
 * 保存 数据
 * 1. 使用本地数据库处理大型数据实现数据持久化，而ViewModel 保存 页面的临时数据
 */
class SearchManager(registryOwner: SavedStateRegistryOwner) :
    SavedStateRegistry.SavedStateProvider {
    companion object {
        private const val PROVIDER = "search_manager"
        private const val QUERY = "query"
    }

    private var query: String? = "123"
    override fun saveState(): Bundle {

        return bundleOf(QUERY to query)
    }

    init {
        // Register a LifecycleObserver for when the Lifecycle hits ON_CREATE
        registryOwner.lifecycle.addObserver(LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_CREATE) {
                val registry = registryOwner.savedStateRegistry

                // Register this object for future calls to saveState()
                registry.registerSavedStateProvider(PROVIDER, this)

                // 执行saveState() 保存一些内容。SavedStateRegistry将数据存储在与onSaveInstanceState()相同的Bundle中，
                // 因此应用相同的注意事项和数据限制

                saveState()

                // Get the previously saved state and restore it
                val state = registry.consumeRestoredStateForKey(PROVIDER)

                // Apply the previously saved state
                query = state?.getString(QUERY)
                println("SearchManager()----${query}")
            }
        })
    }

}