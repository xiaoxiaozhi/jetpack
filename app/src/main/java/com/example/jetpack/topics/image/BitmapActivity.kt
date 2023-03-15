package com.example.jetpack.topics.image

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.lifecycle.*
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityBitmapBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 建议使用Glide加载位图
 * 理想情况下您只希望在内存中加载较低分辨率的版本。分辨率较低的版本应与显示该版本的界面组件的大小相匹配
 * 1. 预读取位图
 *    避免内存溢出，预先读取位图宽高和类型。设置inJustDecodeBounds = true 在解码时可避免内存分配
 * 2. 按比例缩小
 *    加载图片前缩放,
 * note:[在onCreate中获取控件宽高=0的解决方法](https://stackoverflow.com/questions/3591784/views-getwidth-and-getheight-returns-0)
 * BitmapFactory
 *    decodeByteArray()、decodeFile()、decodeResource() 等根据您的图片数据源选择最合适的解码方法,这些方法尝试为构造的位图分配内存，因此很容易导致 OutOfMemory 异常
 *
 */
class BitmapActivity : AppCompatActivity() {
    val binding: ActivityBitmapBinding by lazy { ActivityBitmapBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //1.预读取位图
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true //在解码时将 inJustDecodeBounds= true 可避免内存分配,同时 out...()类的方法将被赋值
        }
        BitmapFactory.decodeResource(resources, R.drawable.album2, options)
        val triple = Triple(options.outHeight, options.outWidth, options.outMimeType)
        println("颜色空间-----${options.outConfig.name}")
        println("Triple------$triple")

        //2.按比例缩小图片
        binding.image1.post {
            binding.image1.apply {
                setImageBitmap(decodeSampledBitmapFromResource(resources, R.drawable.album2, width, height))
            }
        }

        // BitmapFactory.Options 各字段含义
        BitmapFactory.Options().apply {
            //inBitmap = null //可以复用之前用过的bitmap,TODO 这个参数要怎么用
            inMutable = true; //是该bitmap缓存是否可变，如果设置为true，将可被inBitmap复用
//            inPreferredConfig = Bitmap.Config.RGB_565//设置颜色空间，这是一个推荐，如果图片不能按照该颜色解码，会自动的用ARGB_8888解码 [具体详情看该文章](https://blog.csdn.net/ccpat/article/details/46834089)
            inJustDecodeBounds = true //在解码时将 inJustDecodeBounds= true 可避免内存分配,同时 out...()类的方法将被赋值
            //[以下三个属性的关系](https://www.jianshu.com/p/c545f2a6cafc)
            //scale= 设备屏幕密度/drawable目录设定的屏幕密度   相当于 放缩规则 scale= inTargetDensity/inDesity;
            inTargetDensity
            inDensity
            inScaled

            //假如：图片的宽和高分别是width、height，那么图片解码生成的bitmap的宽度是：width / inSampleSize，高度是：height / inSampleSize
            //inSampleSize影响bitmap的分辨率，从而影响bitmap占用内存的大小。
            //inSampleSize = 1;
        }
    }

    private fun decodeSampledBitmapFromResource(res: Resources, resId: Int, reqWidth: Int, reqHeight: Int): Bitmap {
        // First decode with inJustDecodeBounds=true to check dimensions
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            BitmapFactory.decodeResource(res, resId, this)

            // Calculate inSampleSize
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)

            // Decode bitmap with inSampleSize set
            inJustDecodeBounds = false

            BitmapFactory.decodeResource(res, resId, this)
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        println("reqWidth----$reqWidth  reqHeight----$reqHeight")
        // Raw height and width of image
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            //请求宽高*2<=实际宽高<=请求宽高 不缩放   实际宽高>=请求宽高*2 才会缩放
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        println("inSampleSize-----$inSampleSize")
        return inSampleSize
    }
}
