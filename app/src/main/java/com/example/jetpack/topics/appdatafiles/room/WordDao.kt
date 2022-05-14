package com.example.jetpack.topics.appdatafiles.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WordDao {
    @Query("SELECT * FROM word_table ORDER BY word ASC")
    fun getAlphabetizedWords(): Flow<List<Word>>

    @Insert(onConflict = OnConflictStrategy.IGNORE, entity = Word::class)
    suspend fun insert(word: Word)

    @Insert(onConflict = OnConflictStrategy.IGNORE, entity = Word::class)
    suspend fun insert(words: List<Word>)

    @Query("DELETE FROM word_table") //使用Query执行更复杂的插入、更新和删除操作。
    suspend fun deleteAll()

}