package com.example.jetpack

import java.math.RoundingMode
import java.text.DecimalFormat

object NumberUtil {
    private val format = DecimalFormat("0.##")

    /**
     * 保留两位小数,无四舍五入
     */
    fun getTwoDigits(number: Float): String = format.run {
        roundingMode = RoundingMode.FLOOR
        format.format(number)
    }

}