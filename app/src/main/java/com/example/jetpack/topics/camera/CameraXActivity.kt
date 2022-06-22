package com.example.jetpack.topics.camera

import android.Manifest
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.example.jetpack.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * 1. 添加依赖
 *    [依赖](https://developer.android.google.cn/training/camerax/architecture#dependencies)
 *    CameraX 具有以下最低版本要求： Android API 级别 21 Android 架构组件 1.1.1 对于能够感知生命周期的 Activity，请使用 FragmentActivity 或 AppCompatActivity。
 *    添加权限 <uses-permission android:name="android.permission.CAMERA"></uses-permission> 需要用户同意，您的应用需要 CAMERA 权限。如需将图片保存到文件中，除非所用设备搭载 Android 10 或更高版本，否则应用还需要 WRITE_EXTERNAL_STORAGE 权限
 * 2. 预览
 *    2.1 添加PreviewView
 *        用于渲染预览流的实现模式 [该模式有两种](https://developer.android.google.cn/training/camerax/preview#implementation-mode)
 *        预览图片的缩放类型
 *    2.2 请求ProcessCameraProvider。一个进程只能存在一个相机提供者
 *    2.3 选择相机并绑定生命周期和用例。 查看 bindPreview()
 *        创建Preview 预览流
 *        创建CameraSelector 筛选摄像头
 * 3. 分析图片
 *    3.1 构建 ImageAnalysis 用例。 ImageAnalysis.Builder 来构建 ImageAnalysis 对象，借助 ImageAnalysis.Builder 可以设置图像输出，图像流控制
 *    3.2
 * 4. 拍照
 *
 *
 */
class CameraXActivity : AppCompatActivity() {
    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    lateinit var binding: ActivityCameraBinding
    private val TAG: String = "CameraActivity"
    private var imageCapture: ImageCapture? = null

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //2. 预览----------------------------------------------------------------
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
        //3. 拍摄----------------------------------------------------------------
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //创建 Preview
        var preview: Preview = Preview.Builder()
            .build()
//            .apply { setSurfaceProvider(binding.previewView.surfaceProvider) } TODO 这个方法是干什么的

        var cameraSelector: CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(binding.previewView.getSurfaceProvider())

        var camera = cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
//    private fun takePhoto() {
//        // Get a stable reference of the modifiable image capture use case
//        val imageCapture = imageCapture ?: {
//            ImageCapture.Builder()
//                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
//                // We request aspect ratio but no resolution to match preview config, but letting
//                // CameraX optimize for whatever specific resolution best fits our use cases
//                .setTargetAspectRatio(screenAspectRatio)
//                // Set initial target rotation, we will have to call this again if rotation changes
//                // during the lifecycle of this use case
//                .setTargetRotation(rotation)
//                .build()
//        }
//
//        // Create time-stamped output file to hold the image
//        val photoFile = File(
//            filesDir.toString(),
//            SimpleDateFormat(
//                FILENAME_FORMAT, Locale.US
//            ).format(System.currentTimeMillis()) + ".jpg"
//        )
//
//        // Create output options object which contains file + metadata
//        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
//
//        // Set up image capture listener, which is triggered after photo has
//        // been taken
//        imageCapture.takePicture(
//            outputOptions,
//            ContextCompat.getMainExecutor(this),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(exc: ImageCaptureException) {
//                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
//                }
//
//                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
//                    val savedUri = Uri.fromFile(photoFile)
//                    val msg = "Photo capture succeeded: $savedUri"
//                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
//                    Log.d(TAG, msg)
//                }
//            })
//    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }
}