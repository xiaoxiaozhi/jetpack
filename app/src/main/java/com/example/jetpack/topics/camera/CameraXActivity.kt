package com.example.jetpack.topics.camera

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.display.DisplayManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.camera2.interop.Camera2CameraInfo
import androidx.camera.core.*
import androidx.camera.core.ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.camera.view.video.OutputFileOptions
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.util.Consumer
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.jetpack.*
import com.example.jetpack.databinding.ActivityCameraBinding
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 *
 * 1. 添加依赖
 *    [依赖](https://developer.android.google.cn/training/camerax/architecture#dependencies)
 *    CameraX 具有以下最低版本要求： Android API 级别 21 Android 架构组件 1.1.1 对于能够感知生命周期的 Activity，请使用 FragmentActivity 或 AppCompatActivity。
 *    添加权限 <uses-permission android:name="android.permission.CAMERA"></uses-permission> 需要用户同意，您的应用需要 CAMERA 权限。如需将图片保存到文件中，除非所用设备搭载 Android 10 或更高版本，否则应用还需要 WRITE_EXTERNAL_STORAGE 权限
 * 2. 预览
 *    如果不设置分辨率 preview.setTargetResolution(Size())，CameraX 会自动确定要使用的最佳分辨率。所有这些操作均由库进行处理
 *    2.1 添加PreviewView
 *        用于渲染预览流的实现模式，有两种模式①PERFORMANCE 性能模式 使用 SurfaceView 来显示视频流性能更强：②COMPATIBLE 兼容模式 使用TextureView 能够缩放或者旋转，性能没有前者强
 *        预览图片的缩放类型
 *    2.2 请求ProcessCameraProvider。一个进程只能存在一个相机提供者
 *    2.3 选择相机并绑定生命周期和用例。 查看 bindPreview()
 *        创建Preview 预览流
 *        创建CameraSelector 筛选摄像头
 * 3. 拍照
 *    图片拍摄和图片分析用例的默认宽高比为 4:3。配置ImageCapture.OutputFileOptions.Builder(几个构建函数)保存为 File 保存为 content:// 保存outputStream
 *    [对象从 YUV_420_888 格式转换为 RGB Bitmap对象的示例代码](https://github.com/android/camera-samples/blob/3730442b49189f76a1083a98f3acf3f5f09222a3/CameraUtils/lib/src/main/java/com/example/android/camera/utils/YuvToRgbConverter.kt)
 *    note:获取yuv格式(失败) 溢栈上说用这个方法获取imageCapture.takePicture(executor, new ImageCapture.OnImageCapturedCallback) 实际打印得到的格式发现是0x100 也就是jpg，目前没有方法设置拍照格式
 *    note:获取jpg格式 imageCapture?.takePicture(outputFileOptions, cameraExecutor, object:ImageCapture.OnImageSavedCallback)
 * 4. 分析图片
 *    图片拍摄和图片分析用例的默认宽高比为 4:3。构建分析用例ImageAnalysis 对用例进行配置然后设置分析器ImageAnalysis.Analyzer。在分析其中执行业务逻辑代码最后调用ImageProxy.close，才能分析下一帧图片
 *    4.1 构建 ImageAnalysis 用例。 ImageAnalysis.Builder 来构建 ImageAnalysis 对象，借助 ImageAnalysis.Builder 可以设置图像输出，图像流控制
 *        背压策略 默认0 只分析最新的图像，忽略setImageQueueDepth(int)设置的值; 设置为 1 。图像队列达到setImageQueueDepth设置的值就不会产生新的图像，注意一旦阻塞其它UserCase也接收不到新图像
 *    4.2 创建ImageAnalysis.Analyzer：接收图片，在该
 *        Image:通过ByteBuffers直接访问图像的二进制数据，与Bitmap不同Image不能作为UI资源使用
 *    [YUV和RGB色彩空间介绍](https://juejin.cn/post/7138611459063447560)
 *    [官方提供库：libyuv YUV和RGB互相转换](https://chromium.googlesource.com/libyuv/libyuv/)
 * 5. 录视频
 *    VideoCapture用例一定可以和Preview一起使用，能不能和其它用例一同使用取决于硬件级别
 *    - 获取质量选择器 QualitySelector TODO，QualitySelector.fromOrderedList 这种方式到底0选择了哪种质量？？？
 *    - 创建Recorder Recorder.Builder().setQualitySelector(QualitySelector) 在这里Recorder与QualitySelector 绑定在一起. 录制配置例如 存储位置(共享或者专属空间)、运行线程
 *    - 创建VideoCapture用例 VideoCapture.withOutput(recorder)
 *    - 配置视频保存位置，Recorder.prepareRecording() 保存位置可以使FIle也可以是uri。prepareRecording可以多次调用同时保存几份视频
 *    - 开始录制 prepareRecording.start()  并添加录制状态回调函数，是否启动录音prepareRecording.withAudioEnabled() 需要录音权限
 *    - Recording：由prepareRecording.start()返回，为录制活动提供 pause(暂停)、resume(恢复)、stop(完成)。在stop调用之前prepareRecording不能重新生成一个Recording
 *                 close()效果等同于stop()
 *    [VideoCapture工作原理图](https://developer.android.google.cn/static/images/training/camera/camerax/videocapture-use-case.png)
 *
 * 6. 控制相机输出
 *    ProcessCameraProvider.bindToLifecyle() 返回的 Camera 获取 CameraControl 和 CameraInfo 的实例。前者配置相机功能，后者获取相机功能状态
 *    6.1 CameraControl 支持的相机功能：
 *        变焦：setZoomRatio() 用于按变焦比例设置变焦。不许在CameraInfo.getZoomState().getValue().getMinZoomRatio() 到 CameraInfo.getZoomState().getValue().getMaxZoomRatio() 的范围内否则报异常
 *             setLinearZoom() 使用 0 到 1.0 之间的线性变焦值设置当前变焦操作。
 *        手电筒：CameraControl.enableTorch(boolean) 可以启用或停用手电筒（也称为手电）。
 *        对焦和测光（点按即可对焦）
 *        曝光补偿
 *    6.2 CameraInfo 获取相机功能状态
 *        变焦状态：CameraInfo.getZoomState()会返回当前变焦状态的 LiveData
 *        手电筒状态:CameraInfo.getTorchState() 可用于查询当前的手电筒状态
 *    note:如果 LifecycleOwner 被停止或销毁，Camera 就会关闭，之后变焦、手电筒、对焦和测光以及曝光补偿控件的所有状态更改均会还原成默认值。
 * 7. 相机分辨率
 *    7.1 自动分辨率
 *        根据UserCase设置的宽高比自动设置最佳分辨率，跟Preview大小无关。如果没有找到适配该宽高比的分辨率则会选择次佳分辨率
 *        ImageCapture和ImageAnalysis用例的默认宽高比为 4:3。
 *        [没看明白官网给的图表是干什么的](https://developer.android.com/training/camerax/configuration#automatic-resolution)
 *    7.2 指定分辨率
 *        横竖屏切换的时候宽高会随之变化，例如竖屏时指定 width480 x height640；旋转 90 度变横屏时分辨率就要指定 width640 x height480， Size(width,height)
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
 *                [官方CameraXConfig](https://developer.android.google.cn/training/camerax/configuration)
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
 * Preview: 把预览流显示到PreviewView上面去 ，调用setTargetRotation保持显示的方向要与屏幕的方向一致
 *
 * ViewPort:CameraX 使用它来计算输出作物矩形
 *
 * Display 逻辑显示器
 *
 * TODO UseCaseGroup、ViewPort
 * Rational(numerator: Int, denominator: Int)表示一个有理数
 */
/** Helper type alias used for analysis use case callbacks */
typealias LumaListener = (luma: Double) -> Unit

class CameraXActivity : AppCompatActivity() {
    private val cameraProviderFuture by lazy { ProcessCameraProvider.getInstance(this) }
    lateinit var binding: ActivityCameraBinding
    private val TAG: String = "CameraActivity"
    private var imageCapture: ImageCapture? = null
    private val openSetting = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var cameraSelector: CameraSelector? = null
    private var lensFacing: Int = CameraSelector.LENS_FACING_BACK
    private var imageAnalyzer: ImageAnalysis? = null
    private lateinit var videoCapture: androidx.camera.video.VideoCapture<Recorder>
    private var currentRecording: Recording? = null

    /** Blocking camera operations are performed using this executor */
    private val cameraExecutor: ExecutorService by lazy { Executors.newSingleThreadExecutor() }
    private val cameraManager: CameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }
    private val displayManager by lazy { getSystemService(Context.DISPLAY_SERVICE) as DisplayManager }

    /**
     * We need a display listener for orientation changes that do not trigger a configuration
     * change, for example if we choose to override config change in manifest or for 180-degree
     * orientation changes.
     */
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) = Unit
        override fun onDisplayRemoved(displayId: Int) = Unit
        override fun onDisplayChanged(displayId: Int) = binding.previewView?.let { view ->
            if (displayId == binding.previewView.display.displayId) {
                Log.d(TAG, "Rotation changed: ${view.display.rotation}")
                imageCapture?.targetRotation = view.display.rotation
                imageAnalyzer?.targetRotation = view.display.rotation
            }
        } ?: Unit
    }
    private val treeResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            println("documentTree uri----${activityResult.data?.data}")
            activityResult.data?.data?.let { DocumentsContract.deleteDocument(contentResolver, it) }

        }
    private val documentResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { activityResult ->
            println("document uri----${activityResult.data?.data}")
            activityResult.data?.data?.let {
                DocumentFile.fromSingleUri(this, it)?.let { document ->
                    val name = document.name
                    val type = document.type
                    val uri = document.uri
                    println("名字---$name 类型----$type uri----$uri 是文件吗？----${document.isFile}")
                }
            }
        }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        if (!haveStoragePermission(Manifest.permission.CAMERA)) {
//            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
//                if (!it) {
//                    showRational()
//                }
//            }.launch(Manifest.permission.CAMERA)
//        }
        havePermissions(
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
        ).takeIf {
            it.isNotEmpty()
        }?.forEach { permission ->
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {
                if (!it) {
                    showRational()
                }
            }.launch(permission)
        }
        cameraManager.cameraIdList.forEach {
            val characteristics = cameraManager.getCameraCharacteristics(it)
            val relativePosition = characteristics.get(CameraCharacteristics.LENS_FACING)
            println("identifier----$it  relativePosition---$relativePosition")
            getCameraStreamMap(it)// k30pro 摄像头标识符 前置1  后置0
        }
        //配置所有UserCase
        config()
//        //获取摄像头基本信息
//        getCameraInfo(cameraProviderFuture)
//        //preview基本信息
//        previewInfo()
        //切换摄像头
        binding.button1.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else {
                CameraSelector.LENS_FACING_BACK
            }
            config()
        }
        //拍照片
        binding.button3.setOnClickListener {
            takePicture()
        }
        //显示拍照
        binding.image1.setOnClickListener {
            //android 24 之后其它应用没有对照片的访问权限，需要使用 FileProvider 把file://转成content://
            //[通过FileProvider解决问题](https://inthecheesefactory.com/blog/how-to-share-access-to-file-with-fileprovider-on-android-nougat/en)
            binding.image1.tag?.let {
                it as Uri
                startActivity(Intent().apply {
                    action = Intent.ACTION_VIEW
                    val contentUri = FileProvider.getUriForFile(
                        this@CameraXActivity, BuildConfig.APPLICATION_ID + ".provider", File(it.path)
                    )
                    println("uri path =" + it.path + " contentUri = " + contentUri)
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION//显示图片，必不可少
                    //                    addCategory(Intent.CATEGORY_APP_GALLERY)//加这个不行 TODO 怎么隐式指定系统相册显示图片呢
                    setDataAndType(contentUri, "image/*")
                })
            }
            //显式指定小米系统相册显示图片 ComponentInfo{com.miui.gallery/com.miui.gallery.activity.ExternalPhotoPageActivity}
        }
        //用系统相册打开指定文件夹
        binding.button2.setOnClickListener {
            //自从android24 intent 不支持file://之后就没法
//            val intent = Intent()
//            intent.action = Intent.ACTION_VIEW
//            intent.setDataAndType(
//                Uri.parse("file://storage/emulated/0/Android/data/com.example.jetpack/files/Pictures"), "image/*"
//            )
//            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
//            startActivity(intent)
            //利用 DocumentTree显示指定文件夹的图片
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "image/*"//必不可少
                putExtra(
                    DocumentsContract.EXTRA_INITIAL_URI,
                    getParticularUri("Android%2Fdata%2Fcom.example.jetpack%2Ffiles%2FPictures")
                )
                flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                treeResult.launch(this)
            }//好像不能在Document界面删除 ，只能返回uri使用DocumentsContract.deleteDocument删除
        }
        //录视频
        binding.button4.setOnClickListener {
            startRecording()
        }
        //
        binding.button5.setOnClickListener {
            currentRecording?.stop()
        }
    }

    private fun config() {
        binding.previewView.post {
            println("previewView.height----${binding.previewView.height}  previewView.width----${binding.previewView.width}")
            val aspectRatio = aspectRatio(binding.previewView.width, binding.previewView.height)
            cameraProviderFuture.addListener(Runnable {
                cameraProvider = cameraProviderFuture.get()

                cameraSelector = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.DEFAULT_BACK_CAMERA
                } else {
                    CameraSelector.DEFAULT_FRONT_CAMERA
                } //摄像头选择器

                binding.previewView.implementationMode = PreviewView.ImplementationMode.PERFORMANCE// PreviewView选择模式
                binding.previewView.scaleType = PreviewView.ScaleType.FIT_CENTER //FIT_CENTER 默认缩放；类型

                //2. 预览UserCase
                println("rotation---${binding.previewView.display?.rotation}")//display 在控件绘制完成前，display=null 所以要在post配置
                preview = Preview.Builder().apply {
                    setTargetAspectRatio(aspectRatio) //不能与setTargetResolution 一同设置，默认4:3  k30pro 4:3时输出分辨率一直是1600X1200 16:9时输出分辨率1920X1080 这个值是固定的吗？？？好像不能自定义宽高比
//                    when (binding.previewView.display?.rotation) {
//                        0 -> setTargetResolution(Size(480, 720))//0 竖屏
//                        3 -> setTargetResolution(Size(720, 480))//3 横屏
//                    }
                    setTargetRotation(binding.previewView.display.rotation)// 屏幕旋转的时候要重新设置rotation通过重新创建preview的形式
                }.build()
                preview!!.setSurfaceProvider(binding.previewView.surfaceProvider)
                //3. 拍摄UserCase
                imageCapture = ImageCapture.Builder().apply {
                    setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)//拍照模式 1 优先考虑延迟而不是质量 0优先考虑质量而不是延迟.默认1
//                    setFlashMode(ImageCapture.FLASH_MODE_AUTO)//拍照时是否使用闪光灯，0 自动 1始终打开 2始终关闭 默认2
//                    setIoExecutor(cameraExecutor)//拍照时执行IO操作的执行器，如果不设置将使用默认IO执行器
//                    setJpegQuality()//数值必须在[1. .100]范围内，越大质量越高.如果不设置会根据拍照模式。拍照模式1相当于95质量。拍照模式0相当于100质量
//                    setTargetName(UUID.randomUUID().toString())//UserCase的唯一标识符，它是一个UUID
//                    setTargetAspectRatio(aspectRatio)//默认4:3 哪边是宽哪边是高，是根据rotation决定的
//                    setTargetResolution()//TODO
                    setTargetRotation(binding.previewView.display.rotation)
                }.build()
                //4. 图像分析
                imageAnalyzer = ImageAnalysis.Builder().setTargetAspectRatio(aspectRatio)
                    .setTargetRotation(binding.previewView.display.rotation)
                    .setOutputImageFormat(OUTPUT_IMAGE_FORMAT_YUV_420_888)//仅支持 YUV_420_888 和 RGBA_8888。默认格式为 YUV_420_888。
//                    .setBackgroundExecutor(cameraExecutor)//默认有执行器，也可以设置
                    .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)//设置背压策略，默认0
                    .setImageQueueDepth(6)//当背压策略设置为 STRATEGY_BLOCK_PRODUCER 时有效默认值为6
                    .build().also {
                        it.setAnalyzer(cameraExecutor) { image ->
                            //ImageProxy是 Media.Image 的封装容器
                            runBlocking {
                                println("Analyzer format---${image.format}")
                                delay(5 * 1000)
                                image.close()
                            }
                        }//设置分析器接受图像
//                      it.clearAnalyzer()//停止分析数据
                    }
                //5. 视频
                val qualitySelector = QualitySelector.fromOrderedList(
                    listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
                    FallbackStrategy.lowerQualityOrHigherThan(Quality.SD)//回退策略，返回最接近质量，先从低一点开始不支持就返回高一点的质量
                )//返回列表中受支持的Quality，如果列表中的Quality都不支持，就从回退策略中选
//                val qualitySelector = queryResolution()//另一种获取qualitySelector的方法
                println("get the actual resolution----${qualitySelector}")
                val recorder =
                    Recorder.Builder().setExecutor(cameraExecutor).setQualitySelector(qualitySelector).build()
                videoCapture = androidx.camera.video.VideoCapture.withOutput(recorder)
                //绑定所有UserCase
                cameraProvider!!.unbindAll()
                val camera = cameraProvider!!.bindToLifecycle(
                    this, cameraSelector!!, preview, imageCapture, videoCapture
                )//调用bindToLifecycle的时候最好包含所有用例  k30pro添加超过三个用例的时候报错。
                println("preview size----${preview!!.attachedSurfaceResolution}")
//                println("imageCapture size----${imageCapture!!.attachedSurfaceResolution}")

                //6. 控制相机输出
                val cameraControl = camera.cameraControl.apply {
//                    startFocusAndMetering()
                }//配置相机功能
                val cameraInfo = camera.cameraInfo//相机常用功能状态

            }, ContextCompat.getMainExecutor(this))

        }

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        config()
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
                    observeZoomState(info.zoomState)//获取缩放信息
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


    private fun takePicture() {
//        contentResolver.loadThumbnail()
//        ThumbnailUtils.createImageThumbnail()
        val file = File(getExternalFilesDir(Environment.DIRECTORY_PICTURES), Date().time.toString() + ".jpg").apply {
            if (!exists()) createNewFile() else println("${this.absolutePath} 存在")
        }
        val metadata = ImageCapture.Metadata().apply {
            // 前置摄像头拍摄的照片要做镜像翻转
            isReversedHorizontal = (cameraSelector?.lensFacing ?: 1) == CameraSelector.LENS_FACING_FRONT
        }

        val outputFileOptions =
            ImageCapture.OutputFileOptions.Builder(file).setMetadata(metadata).build()// 保存为 File 保存为 content:// 保存
        imageCapture?.takePicture(outputFileOptions, cameraExecutor, object : ImageCapture.OnImageSavedCallback {
            override fun onError(error: ImageCaptureException) {
                println("takePicture Exception----${error.message}")
            }

            override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                Log.d(TAG, "takePicture succeeded: ${output.savedUri}  getPath---${output.savedUri?.path}")
                binding.image1.post {
                    binding.image1.tag = output.savedUri
                    Glide.with(this@CameraXActivity).load(output.savedUri).into(binding.image1)
                }

//                BitmapFactory.decodeFile()
            }
        }) ?: println("imageCapture  is null")

//        imageCapture?.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
//
//            override fun onCaptureSuccess(image: ImageProxy) {
//                super.onCaptureSuccess(image)
//                println("takePicture format---${image.format}")// 得到0x100 十进制100 也就是jpg。目前没有办法设置格式
//            }
//
//            override fun onError(exception: ImageCaptureException) {
//                super.onError(exception)
//            }
//        })
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdownNow()
    }

    private fun getCameraStreamMap(identifier: String): List<Camera2Activity.Companion.FormatItem> {
        val availableCameras: MutableList<Camera2Activity.Companion.FormatItem> = mutableListOf()

        val characteristics = cameraManager.getCameraCharacteristics(identifier)
        characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)//此摄像头设备支持的可用流配置
            .let {
                println("摄像头返回输出流支持的Format列表 ${it?.outputFormats}")
                it?.getOutputSizes(ImageFormat.JPEG)?.forEach { size ->
                    println("摄像头返回输出流支持的尺寸列表 $size")
                }//预览界面的宽高比要和摄像头输出的类似，如果有多个输出满足宽高比，则选择分辨率搞得那个 [camera1的文章解释了预览宽高比](https://blog.csdn.net/wq892373445/article/details/124216827)
            }

        return availableCameras
    }

    /**
     * 计算Preview纵横比
     * previewRatio 更接近4:3 还是 16:9，以此来判断使用哪个比例
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        println("previewRatio----$previewRatio")
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {

        private const val TAG = "CameraXBasic"
        private const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val PHOTO_EXTENSION = ".jpg"
        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss"

        /** Helper function used to create a timestamped file */
        private fun createFile(baseFolder: File, format: String, extension: String) = File(
            baseFolder, SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension
        )
        //        val config = CameraXConfig.Provider {
//            CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig()) .setCameraExecutor(ContextCompat.getMainExecutor(
//                this)) .setSchedulerHandler(mySchedulerHandler) .build();
//        }
    }

    private fun queryResolution(): QualitySelector? = cameraProvider?.availableCameraInfos?.filter {
        Camera2CameraInfo.from(it)
            .getCameraCharacteristic(CameraCharacteristics.LENS_FACING) == CameraMetadata.LENS_FACING_BACK
    }?.run {
        val supportedQualities = QualitySelector.getSupportedQualities(this[0])
        val filteredQualities =
            arrayListOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD).filter { supportedQualities.contains(it) }
        QualitySelector.from(filteredQualities[0])
    }

    private fun startRecording() {
        // create MediaStoreOutputOptions for our recorder: resulting our recording!
        val name = "CameraX-recording-" + SimpleDateFormat(FILENAME_FORMAT).format(System.currentTimeMillis()) + ".mp4"
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_MOVIES),
            SimpleDateFormat(FILENAME_FORMAT).format(System.currentTimeMillis()) + ".mp4"
        ).apply {
            if (!exists()) createNewFile() else println("${this.absolutePath} 存在")
        }
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val mediaStoreOutput = MediaStoreOutputOptions.Builder(
            contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()//输出到共享存储

        val fileOutput = FileOutputOptions.Builder(file).build()//输出到转有哦文件夹

        currentRecording = videoCapture.output.prepareRecording(this, mediaStoreOutput).withAudioEnabled()
            .start(cameraExecutor) { event ->
                val stats = event.recordingStats
                val size = stats.numBytesRecorded / 1000
                val time = java.util.concurrent.TimeUnit.NANOSECONDS.toSeconds(stats.recordedDurationNanos)
                var text = "${event.getNameString()}: recorded ${size}KB, in ${time}second"
                if (event is VideoRecordEvent.Finalize) println("${text}\nFile saved to: ${event.outputResults.outputUri}")
//                    println("videoRecordStatus----${event.getNameString()}")
            }
    }

}

