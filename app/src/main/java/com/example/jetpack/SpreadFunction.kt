package com.example.jetpack

import android.content.pm.PackageManager
import android.net.Uri
import android.os.storage.StorageManager
import android.provider.DocumentsContract
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService

fun AppCompatActivity.haveStoragePermission(permission: String) =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

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
