package com.example.jetpack.topics.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.Display
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityCamera2Binding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * [camer2架构以及类详解](https://blog.csdn.net/gwplovekimi/article/details/106076963)
 * 1. 相机捕获会话和请求
 *    一台设备有多个摄像头，每个摄像机都是一个CameraDevice，每个摄像头又可以输出多个流， 这样做是因为不同流的用处不一样，可以根据每个流的用处做配置
 *    由于硬件限制CameraDevice只能有单个配置处于活动状态

 * 1. 通过context.getSystemService(Context.CAMERA_SERVICE) 获取CameraManager.
 * 2. 调用CameraManager.open()方法在回调中得到CameraDevice.
 * 3. 通过CameraDevice.createCaptureSession() 在回调中获取CameraCaptureSession.
 * 4. 构建CaptureRequest, 有三种模式可选 预览/拍照/录像.并可以配置不同的捕捉属性，如：预览分辨率，预览目标，对焦模式、曝光模式等等。
 * 5. 通过 CameraCaptureSession发送CaptureRequest, capture表示只发一次请求, setRepeatingRequest表示不断发送请求.
 * 6. 拍照数据可以在ImageReader.OnImageAvailableListener回调中获取, CaptureCallback中则可获取拍照实际的参数和Camera当前状态.
 * note： [切换摄像头](https://developer.android.google.cn/training/camera2/camera-enumeration#switch-cameras)
 * note:  [掘金Camera2文章](https://juejin.cn/post/6844904062798790663#heading-21)
 * TODO 预览(摄像头分辨率和屏幕宽高之间的关系)、拍照、录视频、转换摄像头、
 * TODO 摄像头旋转角度和预览角度关系不明白，先拍一张照片看看摄像头是什么角度
 */
class Camera2Activity : AppCompatActivity() {
    /** [HandlerThread] where all camera operations run */
    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    /** [Handler] corresponding to [cameraThread] */
    private val cameraHandler = Handler(cameraThread.looper)
    private lateinit var availableCameras: List<FormatItem>

    //1.获取CameraManager
    private val cameraManager: CameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private lateinit var binding: ActivityCamera2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCamera2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        //1.1 获取相机信息 CameraCharacteristics
        availableCameras = getCameras()
        if (availableCameras.isNotEmpty()) {
            availableCameras.first().also { cameraItem ->
                lifecycleScope.launch(Dispatchers.Main) {
                    //2. 打开摄像头获取CameraDevice
                    val device = openCamera(cameraManager, cameraItem.cameraId, cameraHandler)
                    // Creates list of Surfaces where the camera will output frames
                    val targets = listOf(binding.surface.holder.surface)

                    //3. 创建 CameraCaptureSession
                    // Start a capture session using our open camera and list of Surfaces where frames will go
                    val session = createCaptureSession(device, targets, cameraHandler)
                    //4. 创建请求 CaptureRequest
                    val captureRequest = device.createCaptureRequest(
                        CameraDevice.TEMPLATE_PREVIEW
                    )
                        .apply { addTarget(binding.surface.holder.surface) }//设置请求的surface必须属于 createCaptureSession(targets) targets的列表中
                    // This will keep sending the capture request as frequently as possible until the
                    // session is torn down or session.stopRepeating() is called
                    session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)
                }

            }
        }
        binding.photograph.setOnClickListener {

        }
    }

    private fun getCameras(): List<FormatItem> {
        val availableCameras: MutableList<FormatItem> = mutableListOf()
        for (identifier in cameraManager.cameraIdList) {
            println("摄像头标识符 = $identifier")
            val characteristics = cameraManager.getCameraCharacteristics(identifier)
            val level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL).run {
                //支持的硬件级别每个级别都会在前一个级别上添加其他功能 LEGACY 0<LIMITED 1<FULL 2<LEVEL_3  3
                "摄像头硬件层面支持的Camera2功能等级---$this"
            }

//            characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)//摄像头支持的一些功能
//                .let {
//                    it?.forEach { item ->
//                        println("摄像头支持的功能----$item")
//                    }
//                }
            val relativePosition = characteristics.get(CameraCharacteristics.LENS_FACING)//FRONT0 BACK1 EXTERNAL2
                .run {
                    "摄像头位置 LENS_FACING -----${
                        when (this) {
                            0 -> "前置"
                            1 -> "后置"
                            else -> "s"
                        }
                    }"
                }
            val orientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)//此摄像头设备支持的可用流配置
                .let {
                    println("摄像头返回输出流支持的Format列表 ${it?.outputFormats}")
                    it?.getOutputSizes(ImageFormat.JPEG)?.forEach { size ->
                        println("摄像头返回输出流支持的尺寸列表 $size")
                    }//预览界面的宽高比要和摄像头输出的类似，如果有多个输出满足宽高比，则选择分辨率搞得那个 [camera1的文章解释了预览宽高比](https://blog.csdn.net/wq892373445/article/details/124216827)
                }
            availableCameras.add(FormatItem(relativePosition, identifier, ImageFormat.JPEG))
            println("camera info result------$level----$relativePosition---$orientation")
            println("屏幕旋转角度 ${windowManager.defaultDisplay.rotation}")
        }
        return availableCameras
    }

    //TODO getOutputSizes 通过 ImageFormat 和SurfaceView 有什么区别？？？
//    private fun getPreviewSize(
//        cameraId: String,
//        cameraManager: CameraManager,
//        display: Display
//    ): Size {
//        val characteristics = cameraManager.getCameraCharacteristics(cameraId)
//        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
//            ?.getOutputSizes(ImageFormat.JPEG)!!.sortedByDescending { it.height * it.width }!!.first{it.height}
//
//    }

    @SuppressLint("MissingPermission")
    suspend fun openCamera(manager: CameraManager, cameraId: String, handler: Handler? = null): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(device: CameraDevice) = cont.resume(device)

                override fun onDisconnected(device: CameraDevice) {
                    println("Camera $cameraId has been disconnected")
                    finish()
                }

                override fun onError(device: CameraDevice, error: Int) {
                    val msg = when (error) {
                        ERROR_CAMERA_DEVICE -> "Fatal (device)"
                        ERROR_CAMERA_DISABLED -> "Device policy"
                        ERROR_CAMERA_IN_USE -> "Camera in use"
                        ERROR_CAMERA_SERVICE -> "Fatal (service)"
                        ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                        else -> "Unknown"
                    }
                    val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                    println(exc.message)
                    if (cont.isActive) cont.resumeWithException(exc)
                }
            }, handler)

        }

    /**
     * Starts a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine
     */
    private suspend fun createCaptureSession(device: CameraDevice,
        targets: List<Surface>,
        handler: Handler? = null): CameraCaptureSession = suspendCoroutine { cont ->
        // Create a capture session using the predefined targets; this also involves defining the
        // session state callback to be notified of when the session is ready
        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                println(exc.message)
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    companion object {
        public data class FormatItem(val title: String, val cameraId: String, val format: Int)
    }
}