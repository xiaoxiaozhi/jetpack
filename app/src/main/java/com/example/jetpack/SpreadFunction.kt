package com.example.jetpack

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.video.Quality
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

fun AppCompatActivity.haveStoragePermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

/**
 * 不能这样搞，如果两个权限都要询问，第二个弹出后会让第一个返回false
 */
fun AppCompatActivity.havePermissions(permissions: Array<String>) = permissions.filter {
    ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
}

val AppCompatActivity.storageManager: StorageManager
    get() = getSystemService<StorageManager>() as StorageManager

fun AppCompatActivity.getRootDocumentUri() = storageManager.storageVolumes[0].createOpenDocumentTreeIntent()
    .getParcelableExtra<Uri>(DocumentsContract.EXTRA_INITIAL_URI)

fun AppCompatActivity.getImageDocumentUri(): Uri {
    var uri = storageManager.storageVolumes[0].createOpenDocumentTreeIntent()
        .getParcelableExtra<Uri>(DocumentsContract.EXTRA_INITIAL_URI)
    uri = Uri.parse(uri.toString().replace("/root/", "/document/") + "%3ADCIM%2FCamera")
    return uri
}

// %3A转换成“：”，%2F转换成“/”
fun AppCompatActivity.getParticularUri(path: String?): Uri {
//        startDir = "DCIM%2FCamera";
    val intent = storageManager.primaryStorageVolume.createOpenDocumentTreeIntent()
    var uri = intent.getParcelableExtra<Uri>("android.provider.extra.INITIAL_URI")
    uri = if (path == null) {
        Uri.parse(uri.toString().replace("/root/", "/document/"))
    } else {
        Uri.parse(uri.toString().replace("/root/", "/document/") + "%3A" + path)
    }
    println("getUri----$uri")
    return uri
}

fun Quality.qualityToString(): String {
    return when (this) {
        Quality.UHD -> "UHD"
        Quality.FHD -> "FHD"
        Quality.HD -> "HD"
        Quality.SD -> "SD"
        else -> throw IllegalArgumentException()
    }
}

fun VideoRecordEvent.getNameString(): String {
    return when (this) {
        is VideoRecordEvent.Status -> "Status"
        is VideoRecordEvent.Start -> "Started"
        is VideoRecordEvent.Finalize -> "Finalized"
        is VideoRecordEvent.Pause -> "Paused"
        is VideoRecordEvent.Resume -> "Resumed"
        else -> throw IllegalArgumentException("Unknown VideoRecordEvent: $this")
    }
}


val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")