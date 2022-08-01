package com.example.jetpack.topics.camera

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.camera.camera2.Camera2Config
import androidx.camera.camera2.Camera2Config.defaultConfig
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.example.jetpack.databinding.ActivityCameraBinding
import com.example.jetpack.haveStoragePermission
import com.google.common.util.concurrent.ListenableFuture

/**
 *
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
 * ProcessCameraProvider：将相机的生命周期绑定到app进程，进程中只能存在一个 ProcessCameraProvider。生命周期的状态将决定摄像机何时打开、启动、停止和关闭。生命周期Started时用例接收相机数据
 * 打开和运行的摄像头设备，将被限定在第一个参数提供的生命周期内 ProcessCameraProvider.bindToLifecycle(LificycleOwner、 CameraSelector、 Preview)。配齐这三个参数就能预览
 * 其他轻量级资源，比如静态摄像机特性，可以在第一次使用 getInstance (Context)检索这个提供程序时检索和缓存，并将持续到进程的生命周期。
 * 具体方法解析看下面代码 cameraProviderInfo()
 *
 * CameraInfo:保存有 带摄像头方向信息的选择器、相机状态、曝光状态、传感器角度、手电筒状态、缩放状态、是否有手电筒、是否支持焦点操作、是否支持快门延迟isZslSupported
 *
 * CameraSelector: 根据选择器的摄像头方向选择一个或一组相同方向的摄像头。例如 CameraSelector.DEFAULT_BACK_CAMERA.filter(mutableListOf(info))//DEFAULT_BACK_CAMERA 是一个后置摄像头的CameraSelect，
 *                 调用filter()输出 和 CameraSelect相同方向的摄像头
 *
 * CameraXConfig: 配置ProcessCameraProvider在Application继承CameraXConfig.Provider只要在 ProcessCameraProvider.getInstance之前调用就会生效。看了下CameraXConfig.Build()能设置的选项感觉没什么用处
 *
 * PreviewView:显示图像的控件，拥有生命周期功能。
 * 有两种模式 PERFORMANCE 性能模式 使用 SurfaceView 来显示视频流性能更强：COMPATIBLE 兼容模式 使用TextureView 能够缩放或者旋转，性能没有前者强
 * 缩放类型 当预览视频分辨率与目标预览视图的尺寸不同时，视频内容需要通过裁剪或保持原始的高宽比来适应视图。PreviewView 为此提供了以下缩放模式: 默认缩放模式是FILL_CENTER
 *        缩放 FIT_CENTER, FIT_START,  FIT_END 完整的视频内容被缩放(向上或向下)到目标预览视图中可以显示的最大尺寸。然而，当完整的视频帧可见时，屏幕的某些部分可能是空白的，这三种模式对应视频帧将对准目标视图的中心、开始或结束。
 *        剪裁 FILL_CENTER, FILL_START, FILL_END 如果一个视频不匹配预览视图的高宽比，只有一部分内容是可见的，但视频填补了整个预览视图。
 *        显示视频有以下几个步骤
 *        缩放视频 FIT_*** 计算方法min(dst.width/src.width, dst.height/src.height). 最后用视频的 高*scale = 显示高度 宽 * scale = 显示宽度
 *        剪裁视频 FILL_** 计算方法max(dst.width/src.width, dst.height/src.height)  最后用视频的 高*scale = 显示高度 宽 * scale = 显示宽度
 *        将缩放后视频与显示控件 PreviewView对齐
 *        ***_CENTER 居中对齐  ***_START 左上角对齐  ***_END 右下角对齐
 * note：当SurfaceView来显示预览的时候一个不可见的View重叠在它上面(view.gone()或者view.invisiable都是不可见)，如果视图变得可见，它将不会显示在 SurfaceView 的顶部。解决办法是在设置view可见后立即调用ViewParent#requestTransparentRegion(View)
 *       或者控制View的透明度来控制可见不可见 View.opacity= 1F or 0f
 *
 * Preview: 把预览流显示到PreviewView上面去  preview.setSurfaceProvider(previewView.surfaceProvider) 显示的方向要与屏幕的方向一致(TODO 是自动呢还是要手动实现，官网没说)
 *
 * ViewPort:CameraX 使用它来计算输出作物矩形
 *
 * TODO 视频输出宽高在哪里设置？  点击按钮切换前后摄像头
 */
class CameraXActivity : AppCompatActivity() {
    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(this) }
    lateinit var binding: ActivityCameraBinding
    private val TAG: String = "CameraActivity"
    private var imageCapture: ImageCapture? = null
    private val openSetting = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (!haveStoragePermission(Manifest.permission.CAMERA)) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (!it) {
                    showRational()
                }
            }.launch(Manifest.permission.CAMERA)
        }
        //获取摄像头基本信息
        getCameraInfo(cameraProviderFuture)
        //preview基本信息
        previewInfo()
        //2. 预览----------------------------------------------------------------
        binding.button1.setOnClickListener {
            preview()
        }
        //3. 拍摄----------------------------------------------------------------
    }

    private fun preview() {
        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun bindPreview(cameraProvider: ProcessCameraProvider) {
        //创建 Preview
        var preview: Preview = Preview.Builder()
//            .setTargetRotation()    TODO 怎么在屏幕rotation改变的时候改变这个
//            .setTargetAspectRatio() TODO 这个是配置视频流还是显示
            .build()
        var cameraSelector: CameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

        binding.previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE// PreviewView选择模式
        binding.previewView.scaleType = PreviewView.ScaleType.FIT_CENTER //FIT_CENTER 默认缩放；类型

        preview.setSurfaceProvider(binding.previewView.surfaceProvider)
        var camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview)
    }

    private fun previewInfo() {
        var preview: Preview = Preview.Builder().build()

        display?.apply {
            println("rotation----$rotation")
        }

    }

    companion object {
        private const val TAG = "CameraXBasic"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
//        val config = CameraXConfig.Provider {
//            CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig()) .setCameraExecutor(ContextCompat.getMainExecutor(
//                this)) .setSchedulerHandler(mySchedulerHandler) .build();
//        }
    }

    private fun showRational() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCEPT_HANDOVER)) {
            println("用户拒绝后显示原因")//
        } else {
            println("用户点击了禁止再次访问")// 这时候要 导航到权限设置窗口，手动设置
            // 必须在 LifecycleOwners的STARTED 之前调用 registerForActivityResult. 否则报错 推荐用委托形式
            openSetting.launch(Intent().apply {
                action = android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.parse("package:$packageName")
            })
        }
    }

    private fun getCameraInfo(cameraProviderFuture: ListenableFuture<ProcessCameraProvider>) {
        cameraProviderFuture.addListener({
            val cameraProcess = cameraProviderFuture.get()
            //ProcessCameraProvider 类解析
            cameraProviderInfo(cameraProcess)
            //CameraInfo类解析
            cameraProcess.also {
                it.availableCameraInfos.forEach { info ->
                    val facing = if (CameraSelector.DEFAULT_BACK_CAMERA.filter(mutableListOf(info)).size == 1) {
                        "后置"
                    } else {
                        "前置"
                    }
                    println("$facing  是否有闪光灯${info.hasFlashUnit()}")
                    observeState(info.cameraState)//摄像头开关状态
                    info.exposureState//曝光状态 这是用来做什么的
                    val rotationDegrees = info.sensorRotationDegrees// 摄像头旋转角度
                    if (info.hasFlashUnit()) observeTorchState(info.torchState)//闪光灯状态 TODO 手动开关没反应
                    observeZoomState(info.zoomState)//获取缩放信息 TODO 怎么改变缩放比例
//                    info.isFocusMeteringSupported()//是否支持自动聚焦、自动白平衡、自动曝光
//                    info.isZslSupported //是否支持延迟快门 这个api怎么没有
                }
            }
        }, mainExecutor)
    }

    private fun cameraProviderInfo(cameraProvider: ProcessCameraProvider) {
        // 把UseCase(ImageAnalysis, ImageCapture, Preview)绑定到LifecycleOwner. 一个UseCase只能绑定到一个生命周期和CameraSelect
//        cameraProvider.bindToLifecycle()

        //给单例模式的ProcessCameraProvider配置相机程序，在ProcessCameraProvider.getInstance()之前调用，只能出现一次，多次调用会报错IllegalStateException.由于只能出现一次，不推荐这种做法
        //应该 在Application [控制单例配置](https://developer.android.google.cn/reference/androidx/camera/lifecycle/ProcessCameraProvider#getInstance(android.content.Context))
//        ProcessCameraProvider.configureInstance()

        //可用的摄像头包括设备上所有可用的摄像头，或者只包括那些通过 CameraXConfig.Builder.setUtiableCamerasLimiter (CameraSelector)选择的摄像头
        cameraProvider.availableCameraInfos

        //如果不可能子类化 Application 类，比如在库代码中，那么可以在第一次调用 getInstance (context)之前调用
//        ProcessCameraProvider.configureInstance(
//            CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig()).setCameraExecutor(myExecutor)
//                .setSchedulerHandler(mySchedulerHandler).build()
//        ); }
        ProcessCameraProvider.getInstance(this)

        //检查此提供程序是否支持至少一个满足相机选择器要求的相机。 如果此方法返回 true，那么可以使用相机选择器绑定用例并检索 Camera 实例。
//        cameraProvider.hasCamera()
        //如果 UseCase 绑定到生命周期，返回 true，否则返回 false。
        //
        //在用 bindToLificycle 绑定用例之后，用例将保持绑定状态，直到生命周期结束或者调用 unbind (UseCase)或 unbindAll ()取消绑定
//        cameraProvider.isBound()

        //生命周期与用例解绑，将关闭所有打开的摄像头
//        cameraProvider.unbind()

        //从生命周期中解除所有用例的绑定，并从 CameraX 中删除它们。 这将启动关闭所有当前打开的摄像头。
//        cameraProvider.unbindAll()


    }

    private fun getCameraInfo(state: CameraState.Type?) = when (state) {
        CameraState.Type.CLOSED -> "摄像头已关闭"
        CameraState.Type.CLOSING -> "正在关闭摄像机"
        CameraState.Type.OPEN -> "摄像头已打开"
        CameraState.Type.OPENING -> "摄像头正在打开"
        CameraState.Type.PENDING_OPEN -> "摄像头等待打开信号"//如果它试图打开一个不可用的摄像设备，它将从 CLOSED 状态移动到此状态。
        else -> "unknown"
    }

    private fun observeState(state: LiveData<CameraState>) = state.observe(this) {
        println("相机状态------${it.type}")
    }

    private fun observeTorchState(torchState: LiveData<Int>) = torchState.observe(this) {
        val state = if (it == 0) {
            "关"
        } else {
            "开"
        }
        println("闪光灯------$state")
    }

    private fun observeZoomState(zoomState: LiveData<ZoomState>) = zoomState.observe(this) {
        //线性缩放比例0---1.0
        println("当前缩放比例----${it.zoomRatio} 线性缩放比例----${it.linearZoom} 最大缩放比例----${it.maxZoomRatio} 最小缩放比例----${it.minZoomRatio} ")
    }
}

