package com.example.jetpack.topics.camera

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.camera2.*
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.hardware.camera2.params.SessionConfiguration.SESSION_REGULAR
import android.media.ImageReader
import android.media.MediaCodec
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil.setContentView
import androidx.lifecycle.lifecycleScope
import com.example.jetpack.AndroidObject
import com.example.jetpack.NumberUtil
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityCamera2Binding
import com.example.jetpack.haveStoragePermission
import kotlinx.coroutines.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


/**
 * [camer2架构以及类详解](https://blog.csdn.net/gwplovekimi/article/details/106076963)
 * Camera2是一个底层摄像头api，它取代了已经废弃的 Camera 类，除非有特殊需求，我们推荐CameraX(CameraX是对camera2的封装)

 * 一台设备有多个摄像头，每个摄像头都是一个CameraDevice，每个摄像头又可以输出多个流， 这样做是因为不同流的用处不一样，可以根据每个流的用处做配置由于硬件限制CameraDevice只能有单个配置处于活动状态
 * CaptureRequest就是一个配置，指定相机属性，如自动对焦，光圈，效果和曝光。CameraCaptureSession负责管理配置，
 * 使用CameraDevice创建CameraCaptureSession。CameraCaptureSession从CameraDevice接收原始数据流，并分发给CaptureRequest(配置)每个CaptureRequest都可以更改将接收原始映像的活动配置和输出管道集。
 * 由于硬件的限制，在任何给定的时间内，只有一个CameraCaptureSession处于活跃状态，在CameraCaptureSession的生命周期会有很多CaptureRequest。
 * camera --->CameraCaptureSession---->(CaptureRequest、CaptureRequest、CaptureRequest)   --->代表数据流
 * 1.创建 CameraCaptureSession
 *   要创建一个相机会话，提供一个或多个输出目标缓冲区(ImageReader、SurfaceView)您的应用程序可以写输出帧。每个缓冲区代表一个管道
 *   SessionConfiguration(type,....) 第一个参数代表什么
 * 2.创建CaptureRequest
 *   使用CaptureRequest配置每个帧的输出配置，创建完了至少要制定一个缓冲区addTarget(surface)。 创建session要缓冲区怎么创建request也要缓冲区
 * 3.Camera 镜头和性能
 *   示例代码是获取前置后置额外摄像头的第一个并组成列表，感觉这章什么都没说
 * 4.同时使用多个 CaptureRequest
 *   并行流或流水线处理时，性能开销会成倍增加。
 *   在选择应用程序的输出类型时，如果目标是最大限度地提高兼容性，那么使用 ImageFormat.YUV _ 420 _ 888进行帧分析，输出格式是图片请使用 ImageFormat.JPEG 进行静态图像。
 *   对于预览和录制方案，您可能会使用 SurfaceView、 TextureView、 MediaRecorder、 MediaCodec 或 RenderScript。分配。
 *   在这些情况下，使用 ImageFormat.PRIVATE。
 * 5.摄像头预览
 *   摄像头传感器的自然方向是横向![前置示例图](/smail_front_landscape.png)为了传感器的长边缘与设备的长边缘保持一致前置摄像头的传感器逆时针旋转270度，后置摄像头的传感器顺时针旋转90度
 *   我怎么觉得都一样呢
 *   5.1 计算摄像机预览正确方向：
 *       rotation = (sensorOrientationDegrees - deviceOrientationDegrees * sign + 360) % 360sign 表示前置摄像头的 1 ， -1 表示后置摄像头
 *       前置摄像头旋转：传感器自然方向/smail_front_landscape.png---->逆时针270转到屏幕竖屏----->屏幕当前方向(逆时针)   前置摄像头逆时针，后置摄像头顺时针
 *       后置摄像头旋转：传感器自然方向/smail_back_landscape.png---->顺时针90转到屏幕竖屏----->屏幕当前方向(逆时针)
 *   5.2 纵横比
 *       相机传感器图像缓冲区的方向和比例必须与取景器UI元素的方向和纵横比相匹配
 *
 * Image:当输出格式选择ImageFormat.PRIVATE，从 ImageReader 或 ImageWriter 获得这种格式的 Image 时，getPlanes ()方法将返回一个空的刨面数组
 *
 * CameraCharacteristics：CameraDevice属性类由cameraManager.getCameraCharacteristics(cameraID)获取 cameraManager.cameraIdList.forEach{id-> }
 * note： [切换摄像头](https://developer.android.google.cn/training/camera2/camera-enumeration#switch-cameras)
 * note:  [掘金Camera2文章](https://juejin.cn/post/6844904062798790663#heading-21)
 * TODO 预览(摄像头分辨率和屏幕宽高之间的关系)、拍照、录视频、转换摄像头、
 * TODO 摄像头旋转角度和预览角度关系不明白，先拍一张照片看看摄像头是什么角度
 * TODO 暂停、继续、删除CaptureRequest 怎么实现
 * [android 13支持](https://developer.android.google.cn/training/camera2/hdr-video-capture#device_prerequisites)
 * attention：先设根据摄像头输出置好SurfaceView宽高，再来设置preview用例。
 */
class Camera2Activity : AppCompatActivity() {
    /** [HandlerThread] where all camera operations run */
    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private var session: CameraCaptureSession? = null
    private lateinit var device: CameraDevice

    /** [Handler] corresponding to [cameraThread] */
    private val cameraHandler = Handler(cameraThread.looper)
    private lateinit var availableCameras: List<FormatItem>
    private lateinit var characteristics: CameraCharacteristics


    //1.获取CameraManager
    private val cameraManager: CameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private lateinit var binding: ActivityCamera2Binding
    private val openSetting = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private var isPreview = false
    private val imageReader: ImageReader by lazy {
        ImageReader.newInstance(binding.surface.width, binding.surface.height, ImageFormat.YV12, 1)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = setContentView(this, R.layout.activity_camera2)
        if (!haveStoragePermission(Manifest.permission.CAMERA)) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (!it) {
                    showRational(Manifest.permission.CAMERA)
                } else {
                    //退出再进，这只是例子，不做过多逻辑上的判断
                }
            }.launch(Manifest.permission.CAMERA)
        } else {
            binding.surface.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(holder: SurfaceHolder) {
//                    printCamerasInfo()
                    initCamera()
                }

                /**
                 *
                 */
                override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                    Log.i(TAG, "surfaceChanged---format $format width $width height $height")
                }

                override fun surfaceDestroyed(holder: SurfaceHolder) {

                }

            })
            binding.button1.setOnClickListener {
                session?.also { session ->
                    isPreview = if (isPreview) {
                        session.stopRepeating()
                        false
                    } else {
//                    binding.surface.setAspectRatio(//设置流分辨率，不是外观，如果和外观不匹配会发生形变
//                     1080, 1080
//                    )
                        //2. 创建请求 CaptureRequest
                        val captureRequest = device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                            .apply { addTarget(binding.surface.holder.surface) }//设置请求的surface必须属于 createCaptureSession(targets) targets的列表中
                        // This will keep sending the capture request as frequently as possible until the
                        // session is torn down or session.stopRepeating() is called
                        session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)
                        true
                    }
                } ?: Log.e(TAG, "session is null!!!!!!!!")
            }


            binding.photograph.setOnClickListener {
                session?.also { session ->
                    //2. 创建请求 CaptureRequest
                    val captureRequest = device.createCaptureRequest(
                        CameraDevice.TEMPLATE_STILL_CAPTURE
                    )
                        .apply { addTarget(imageReader.surface) }//设置请求的surface必须属于 createCaptureSession(targets) targets的列表中
                    session.capture(
                        captureRequest.build(), null, cameraHandler
                    )//如果有一个setRepeatingRequest，这时候再来capture ，讲不知道capture在什么时候执行，也许会错过想要捕获的帧
//                imageReader.setOnImageAvailableListener()
                }

            }
            binding.button2.setOnClickListener {
                startActivity(Intent(this@Camera2Activity, CameraViewFinderActivity::class.java))
            }
        }
    }


    /**
     * 获取相机信息,并打印每个相机的信息
     */
    /**
     * 获取相机信息,并打印每个相机的信息
     */
    private fun printCamerasInfo() {
        for (identifier in cameraManager.cameraIdList) {
            println("摄像头标识符 = $identifier")
            val characteristics = cameraManager.getCameraCharacteristics(identifier)
            //支持的硬件级别每个级别都会在前一个级别上添加其他功能 LEGACY 0<LIMITED 1<FULL 2<LEVEL_3  3
            val level = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL)
            //摄像头支持的一些功能
            val capabilities = characteristics.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
            capabilities?.forEach {
                //遍历功能， 还可以查询 例如capabilities.contains(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW)
//                Log.i(TAG, "$it")
            } ?: Log.w(TAG, "REQUEST_AVAILABLE_CAPABILITIES is null")
            //摄像头前置韩式后置
            val relativePosition = characteristics.get(CameraCharacteristics.LENS_FACING)//FRONT0 BACK1 EXTERNAL2
                .apply {
                    Log.i(
                        TAG, "摄像头位置 LENS_FACING -----${
                            when (this) {
                                0 -> "前置"
                                1 -> "后置"
                                else -> "EXTERNAL"
                            }
                        }"
                    )
                }//与identifier 值一样，只不过一个是String，一个是Int
            Log.i(TAG, "摄像头方向 ${characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)}")
            //此摄像头设备支持的可用流配置
            characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).also {
                //支持的数据格式      支持的类型都在这两个类中ImageFormat PixelFormat it.isOutputSupportedFor(type) 查询是否支持这个格式
                it?.outputFormats?.map(::format2String)
                //预览界面的宽高比要和摄像头输出的类似，如果有多个输出满足宽高比，则选择分辨率搞得那个 [camera1的文章解释了预览宽高比](https://blog.csdn.net/wq892373445/article/details/124216827)
//                Log.i(TAG, "JPEG格式支持的尺寸列表${it?.getOutputSizes(ImageFormat.JPEG)?.toList().toString()}")
                it?.getOutputSizes(MediaCodec::class.java)?.forEach { size ->
                    Log.i(
                        TAG,
                        "size=$size AspectRatio= ${NumberUtil.getTwoDigits(size.width.toFloat() / size.height.toFloat())}"
                    )
                }
                //以下两个API 的 format 和 Size 是 it?.outputFormats 和it?.getOutputSizes(type) 的子集
                //对于这种格式帧 最小持续时间
                it?.getOutputMinFrameDuration(ImageFormat.JPEG, Size(720, 480))// 例如 NV21 720P 时候的最小帧速率
                //拖延时间，由原始数据到指定格式消耗的时间。JPEG RAW16 RAW_PRIVATE 总会产生拖延。YUV_420_888 PRIVATE 不会产生拖延时间
                it?.getOutputStallDuration(ImageFormat.JPEG, Size(720, 480))
//                Log.i(TAG, "根据用例确定输出大小${
//                    it?.getOutputSizes(SurfaceView::javaClass.javaClass)?.maxBy { size ->
//                        size.width * size.height
//                    }
//                }") 输出null 为什么
            }
        }
    }

    /**
     * 默认后置摄像头1
     *    前置摄像头0
     */
    private fun initCamera(orientation: Int = 1) {
        lifecycleScope.launch {
            val identifier = cameraManager.run {
                cameraIdList.first { identifier ->
                    getCameraCharacteristics(identifier).get(CameraCharacteristics.LENS_FACING) == orientation
                }
            }//固定摄像头标识符都是从0开始的整数。可插拔不是
            characteristics = cameraManager.getCameraCharacteristics(identifier)
            //跟局SurfaceView的大小算出最合适的分辨率

            val size = getPreviewOutputSize1(
                Size(binding.surface.width, binding.surface.height), characteristics, SurfaceHolder::class.java
            )
            Log.i(TAG, "getPreviewOutputSize----$size")
            val smartSize = SmartSize(size.width, size.height)
            //
            if (binding.surface.display.rotation == 0) {
                (binding.surface.layoutParams as ConstraintLayout.LayoutParams).apply {
                    width = smartSize.short
                    height = smartSize.long
                    binding.surface.requestLayout()

                }
            } else {
                (binding.surface.layoutParams as ConstraintLayout.LayoutParams).apply {
                    width = smartSize.long
                    height = smartSize.short
                    binding.surface.requestLayout()
                }
            }
            binding.surface.setAspectRatio(//设置流分辨率，不是外观，如果和外观不匹配会发生形变
                size.width, size.height
            )

            //打开摄像头获取CameraDevice
            device = openCamera(cameraManager, identifier, cameraHandler)
            // Creates list of Surfaces where the camera will output frames
            val targets = listOf(binding.surface.holder.surface)//TODO 转换由特定 Surface 的像素格式和大小控制????
            //1. 创建 CameraCaptureSession
            // Start a capture session using our open camera and list of Surfaces where frames will go
            session = createCaptureSession(device, targets, cameraHandler)


            Log.i(TAG, "initCamera success!!!")
        }
    }

    @SuppressLint("MissingPermission")
    suspend fun openCamera(manager: CameraManager, cameraId: String, handler: Handler? = null): CameraDevice =
        suspendCancellableCoroutine { cont ->
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(device: CameraDevice) = cont.resume(device)

                override fun onDisconnected(device1: CameraDevice) {
                    println("Camera $cameraId has been disconnected")
                    device1.close()
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            device.createCaptureSession(
                SessionConfiguration(SESSION_REGULAR, targets.map {
                    OutputConfiguration(it)
                }, AndroidObject.executorService, object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        cont.resume(session)
                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        val exc = RuntimeException("Camera ${device.id} session configuration failed")
                        println(exc.message)
                        cont.resumeWithException(exc)
                    }

                })
            )
        } else {
            device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

                override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

                override fun onConfigureFailed(session: CameraCaptureSession) {
                    val exc = RuntimeException("Camera ${device.id} session configuration failed")
                    println(exc.message)
                    cont.resumeWithException(exc)
                }
            }, handler)
        }
    }


    /** Helper function used to convert a lens orientation enum into a human-readable string */
    private fun lensOrientationString(value: Int) = when (value) {
        CameraCharacteristics.LENS_FACING_BACK -> "Back"
        CameraCharacteristics.LENS_FACING_FRONT -> "Front"
        CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
        else -> "Unknown"
    }

    private fun showRational(permission: String) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
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

    private fun format2String(type: Int) = when (type) {
        ImageFormat.RAW_SENSOR -> Log.i(TAG, "ImageFormat.RAW_SENSOR")
        ImageFormat.PRIVATE -> Log.i(TAG, "ImageFormat.PRIVATE")
        ImageFormat.RAW_PRIVATE -> Log.i(TAG, "ImageFormat.RAW_PRIVATE")
        ImageFormat.YUV_420_888 -> Log.i(TAG, "ImageFormat.YUV_420_888")
        ImageFormat.RAW10 -> Log.i(TAG, "ImageFormat.RAW10")
        ImageFormat.JPEG -> Log.i(TAG, "ImageFormat.JPEG")
        ImageFormat.YV12 -> Log.i(TAG, "ImageFormat.YV12")
        ImageFormat.NV21 -> Log.i(TAG, "ImageFormat.NV21")//PixelFormat.YCbCr_420_SP值一样
        ImageFormat.HEIC -> Log.i(TAG, "ImageFormat.HEIC")
        PixelFormat.RGBA_8888 -> Log.i(TAG, "  PixelFormat.RGBA_8888")
        else -> Log.i(TAG, "其它格式0X${type.toString(16)}")
    }

    /**
     * Computes rotation required to transform the camera sensor output orientation to the
     * device's current orientation in degrees.
     *
     * @param characteristics The CameraCharacteristics to query for the sensor orientation.
     * @param surfaceRotationDegrees The current device orientation as a Surface constant.
     * @return Relative rotation of the camera sensor output.
     */
    public fun computeRelativeRotation(characteristics: CameraCharacteristics, surfaceRotationDegrees: Int): Int {
        val sensorOrientationDegrees = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)!!

        // Reverse device orientation for back-facing cameras.
        val sign =
            if (characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) 1 else -1

        // Calculate desired orientation relative to camera orientation to make
        // the image upright relative to the device orientation.
        return (sensorOrientationDegrees - surfaceRotationDegrees * sign + 360) % 360
    }

    companion object {
        public data class FormatItem(val relativePosition: Int, val cameraId: String, val format: Int)

        private const val TAG = "Camera2Activity"
    }

}