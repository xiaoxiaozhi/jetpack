package com.example.jetpack.topics.camera

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView

/**
 * https://www.jianshu.com/p/b037249e6d31 双缓冲机制，更新过于频繁需求会导致view卡顿使用SurfaceView在子线程更新界面
 * [SurfaceView及TextureView对比](https://www.jianshu.com/p/d9ccd6d3abb2)
 * TODO 预览尺寸和摄像头输出尺寸 还没有调好
 */
class SurfaceViewTemplate(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :
    SurfaceView(context, attrs, defStyleAttr), SurfaceHolder.Callback, Runnable {

    private lateinit var mSurfaceHolder: SurfaceHolder

    //绘图的Canvas
    private lateinit var mCanvas: Canvas

    //子线程标志位
    private val mIsDrawing = false
    private var aspectRatio = 0f

    init {
        initView()
    }

    /**
     * 初始化View
     */
    private fun initView() {
        mSurfaceHolder = holder
        //注册回调方法
        mSurfaceHolder.addCallback(this)
        //设置一些参数方便后面绘图
        isFocusable = true
        keepScreenOn = true
        isFocusableInTouchMode = true
    }

    /**
     * Sets the aspect ratio for this view. The size of the view will be
     * measured based on the ratio calculated from the parameters.
     *
     * @param width  Camera resolution horizontal size
     * @param height Camera resolution vertical size
     */
    private fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        aspectRatio = width.toFloat() / height.toFloat()
//        [setFixedSize含义](https://blog.csdn.net/u011386173/article/details/79082839)
        holder.setFixedSize(
            width,
            height
        ) //告诉系统真实的Video size的大小。并不具备设置SurfaceView控件大小的功能。//使用LayoutParams修改SurfaceView大小
//        requestLayout()
//        println("requestLayout---------")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        println("onSizeChanged------w---$w h---$h  oldw---$oldh  oldh---$oldh")
    }

    /**，
     * [onMeasure解析](https://www.cnblogs.com/touchmore/articles/7755496.html)
     * widthMeasureSpec和heightMeasureSpec不是具体的宽和高，而是由宽、高和各自方向上对应的模式来合成的一个值：
     * MeasureSpec.getSize(measureSpec); 取值  MeasureSpec.getMode(measureSpec) 取模 MeasureSpec.makeMeasureSpec(specSize,specMode);合成
     *
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        println(
            "onMeasure------width---${MeasureSpec.getSize(widthMeasureSpec)} height---$${
                MeasureSpec.getSize(
                    heightMeasureSpec
                )
            }"
        )
    }

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context) : this(context, null)

    override fun surfaceCreated(holder: SurfaceHolder) {
        println("${this::class.java.simpleName}------surfaceCreated")
        //创建
        setAspectRatio(1920, 1080)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        println("${this::class.java.simpleName}------surfaceChanged")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        println("${this::class.java.simpleName}------surfaceDestroyed")
    }

    override fun run() {//run 方法好像还没调用
        //获得canvas对象
        mCanvas = mSurfaceHolder.lockCanvas();
        //绘制背景
        mCanvas.drawColor(Color.WHITE);

    }


}