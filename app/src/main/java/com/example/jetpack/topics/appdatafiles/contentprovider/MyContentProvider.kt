package com.example.jetpack.topics.appdatafiles.contentprovider

import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.os.Bundle
import android.util.Log

/**
 * https://developer.android.com/guide/topics/providers/content-provider-creating?hl=zh-cn
 * 一个完整的content://user_dictionary/words
 * content:// 协议头。
 * user_dictionary 授权标识。
 * words 表的路径。
 * 创建ContentProvider
 * 1. 覆写call方法
 *
 */
class MyContentProvider : ContentProvider() {
    /**
     *
     */
    override fun onCreate(): Boolean {
        return true
    }

    override fun query(p0: Uri, p1: Array<out String?>?, p2: String?, p3: Array<out String?>?, p4: String?): Cursor? {
        return null
    }

    override fun getType(p0: Uri): String? {
        return null
    }

    override fun insert(p0: Uri, p1: ContentValues?): Uri? {
        return null
    }


    override fun call(method: String, arg: String?, extras: Bundle?): Bundle? {
        Log.i(TAG, "call method")
        when (method) {
            METHOD1 -> Log.i(TAG, "call method1")
        }
        return super.call(method, arg, extras)
    }

    override fun call(authority: String, method: String, arg: String?, extras: Bundle?): Bundle? {
        Log.i(TAG, "call authority")
        return super.call(authority, method, arg, extras)
    }

    /**
     * 批量操作 增、删、改、查
     */
    override fun applyBatch(operations: ArrayList<ContentProviderOperation>): Array<ContentProviderResult> {
        //---------------------------------TODO (需要自定义吗)[https://www.jianshu.com/p/7375c21de9c5]
//        synchronized(this) {
//            val db: SQLiteDatabase = mDBHelper.getWritableDatabase()
//            try {
//                db.beginTransaction()
//                results = super.applyBatch(operations)
//                db.setTransactionSuccessful()
//            } finally {
//                db.endTransaction()
//            }
//        }
//        return results
        //---------------------------------------------------------------------
        return super.applyBatch(operations)
    }

    /**
     * 批量操作 插入
     */
    override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
        return super.bulkInsert(uri, values)
    }

    override fun delete(p0: Uri, p1: String?, p2: Array<out String?>?): Int {
        return 0
    }

    override fun update(p0: Uri, p1: ContentValues?, p2: String?, p3: Array<out String?>?): Int {
        return 0
    }


    companion object {
        const val TAG = "MyContentProvider"
        const val AUTHORITY: String = "com.example.jetpack.provider"
        const val METHOD1: String = "method1"
    }
}