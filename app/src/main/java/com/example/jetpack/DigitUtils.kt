package com.example.jetpack

import java.text.DecimalFormat

object DigitUtils {
    /**
     * 小数点保留两位
     */
    fun m2(distance: Double): String {
        val df = DecimalFormat("0.00")
        return df.format(distance)
    }

    /**
     * 小数点保留两位
     */
    fun m1(distance: Double): String {
        val df = DecimalFormat("0.0")
        return df.format(distance)
    }
}