package com.example.jetpack.topics.appdatafiles.contentprovider

import android.content.ContentProvider
import android.content.ContentProviderOperation
import android.content.ContentProviderResult
import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri


class MyContentProvider : ContentProvider() {
    override fun onCreate(): Boolean {
        TODO("Not yet implemented")
    }

    override fun query(uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?): Cursor? {
        TODO("Not yet implemented")
    }

    override fun getType(uri: Uri): String? {
        TODO("Not yet implemented")
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        TODO("Not yet implemented")
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("Not yet implemented")
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
}