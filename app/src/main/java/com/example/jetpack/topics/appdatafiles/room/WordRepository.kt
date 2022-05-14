package com.example.jetpack.topics.appdatafiles.room

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WordRepository @Inject constructor(private val wordDao: WordDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.
    val allWords: Flow<List<Word>> = wordDao.getAlphabetizedWords()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread //表示只应在工作线程上调用带注释的方法。如果带注释的元素是一个类，那么应该在工作线程上调用该类中的所有方法。
    suspend fun insert(word: Word) {
        wordDao.insert(word)
    }
}