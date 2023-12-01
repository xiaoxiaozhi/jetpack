package com.example.jetpack.topics.userinterface.layout.textview

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import com.example.jetpack.R

/**
 * 界面指南--->使用文本和表情符号
 * 1. 即时下载字体
 * 2. 将字体添加为xml资源
 * 3. 添加对表情符号的支持
 * 4. 放大文本
 * 5. 使用span设置文本样式
 *    有三种span，他们各有用处参见 https://developer.android.google.cn/develop/ui/views/text-and-emoji/spans?hl=zh-cn#create-and-apply
 *
 *    5.1 SpannedString
 *        如果您不准备在创建后修改文本或标记，请使用 SpannedString
 *    5.2 SpannableString
 *        如果您需要将少量 span 附加到单个文本对象，并且文本本身是只读的，请使用 SpannableString
 *    5.3 SpannableStringBuilder
 *        如果您需要在创建后修改文本，并且需要将大量span 附加到文本，请使用 SpannableStringBuilder
 *
 */
class TextViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_view)
        val spannable = SpannableStringBuilder("Text is spantastic!")
        spannable.setSpan(
            ForegroundColorSpan(Color.RED),
            8, // start
            12, // end
            Spannable.SPAN_EXCLUSIVE_INCLUSIVE
        )
    }
}