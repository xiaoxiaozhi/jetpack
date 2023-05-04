/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jetpack.topics.camera

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import kotlin.math.roundToInt

/**
 * A [SurfaceView] that can be adjusted to a specified aspect ratio and
 * performs center-crop transformation of input frames.
 * 计算好预览的分辨率，设置setFixedSize(长边，短边) 。 然后根据横屏竖屏，确定SurfaceView的宽高，竖屏Surface的宽= 短边。 横屏Surface的宽=长边
 * SurfaceView显示后 onMeasure在设置不会发生变化
 * holder.setFixedSize(width, height)。 参数名不准确，根据实际试验发现，width实际是Size的长边，height是Size的短边。例如手机竖屏，setFixedSize(1080,1920) 此时会形变，反过来正好
 * attention:不加JvmOverloads 会报错
 */
class AutoFitSurfaceView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0) :
    SurfaceView(context, attrs, defStyle) {

    private var aspectRatio = 0f

    /**
     * Sets the aspect ratio for this view. The size of the view will be
     * measured based on the ratio calculated from the parameters.
     *
     * @param width  Camera resolution horizontal size
     * @param height Camera resolution vertical size
     */
    fun setAspectRatio(width: Int, height: Int) {
        require(width > 0 && height > 0) { "Size cannot be negative" }
        aspectRatio = width.toFloat() / height.toFloat()
        Log.i(TAG, "aspectRatio----$aspectRatio")
        holder.setFixedSize(width, height)//设置SurfaceView的分辨率，尝试一次缩小2倍，会发现越来越模糊
        requestLayout()
    }

    /**
     *
     */
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        Log.i(TAG, "width----$width  height----$height")
//        if (aspectRatio == 0f) {
//            Log.d(TAG, "1Measured dimensions set: $width x $height aspectRatio $aspectRatio")
//            setMeasuredDimension(width, height)
//        } else {
//            // Performs center-crop transformation of the camera frames
//            val newWidth: Int
//            val newHeight: Int
//            val actualRatio = if (width > height) aspectRatio else 1f / aspectRatio
//            if (width < height * actualRatio) {
//                newHeight = height
//                newWidth = (height * actualRatio).roundToInt()
//            } else {
//                newWidth = width
//                newHeight = (width / actualRatio).roundToInt()
//            }
//
//            Log.d(TAG, "2Measured dimensions set: $newWidth x $newHeight  aspectRatio $aspectRatio")
//            setMeasuredDimension(newWidth, newHeight)
//            setMeasuredDimension(720, 1274)
//        }
    }

    companion object {
        private val TAG = AutoFitSurfaceView::class.java.simpleName
    }
}
