package com.example.jetpack.architecturecomponent.uilibs.databinding

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.*

/**
 * TODO [medium的博客理论不一样不一样，有空再看](https://medium.com/@georgemount007)
 * DataBinding会自动查找与属性同名的 setter和Getter。如果要设置属性与方法关联或者自定义方法请使用一下方法
 * 1. 视图属性Setter方法
 *    1.1 @BindingMethods
 *        1.1.1 将控件现有的属性和方法对调发现没用,结论只能给缺乏属性的方法设置参数
 *        1.1.2 自定义属性和方法(单参数，方法是多参数会报错，xml属性好像都只能接收单参数)，试验结果可以
 *        1.1.3 自定义一个方法指定到现有的属性，试验结果 不可以这样
 *        1.1.4 自定义一个属性指定到现有的方法中,试验结果 可以
 *    1.2 @BindAdapter 属性Setter提供自定义方法 TODO 我在属性的setter方法中定义逻辑就好了，为什么还要专门提供一个自定义方法？？？
 *        2.1 使用这个注解可以实现 1.3自定义一个方法指定到现有的属性 note：1.该方法必须是一个静态方法 2. 只有伴随类和object表达式才能拥有静态方法
 *        2.2 自定义属性和方法 与1.2 不同的是，自定义方法可以不在自定义类中
 *    1.3 属性值转换
 * 2. 视图属性Getter方法
 *    2.1  @InverseBindingAdapter 视图提供数据 光有这个还不行，监听器每个双向绑定都会生成“合成事件特性”。该特性与基本特性具有相同的名称，
 *         但包含后缀 "AttrChanged"。InverseBindingListener 调用onChange()视图数据才会提供给变量
 *    2.2 @InverseBindingMethods
 *
 * 3. 防止无限循环
 *    使用双向数据绑定时，请注意不要引入无限循环。当用户更改特性时，系统会调用使用 @InverseBindingAdapter 注释的方法，并且该值将分配给后备属性。
 *    继而调用使用 @BindingAdapter 注释的方法，从而触发对使用 @InverseBindingAdapter 注释的方法的另一个调用，依此类推。
 */
@BindingMethods(
    value = [
        //1.1 对调不成功
        BindingMethod(type = MyTextView::class, attribute = "text", method = "setBackground"),
        BindingMethod(type = MyTextView::class, attribute = "background", method = "setText"),
        //1.2.
        BindingMethod(type = MyTextView::class, attribute = "showText111", method = "showText"),
        //1.3.
        BindingMethod(
            type = MyTextView::class,
            attribute = "android:alpha",
            method = "setAlpha1"
        ),
        //1.4.
        BindingMethod(
            type = MyTextView::class,
            attribute = "cameraDistance",
            method = "setCameraDistance"
        )
    ]
)

object Relation {
    //2.1
//    @BindingAdapter("android:text")
//    @JvmStatic
////    fun <T : TextView> setLowCaseText(view: T, char: CharSequence) {
////        view.text = char.toString().lowercase()
////    } //note: 使用@BindAdapter 方法的第一个参数必须是View类型，方法参数类型约束在这里不起作用。
//    fun setLowCaseText(view: TextView, char: CharSequence?) {
//        char?.apply {
//            if (view.text != char) {
//                view.text = toString().lowercase()
//            }
//        }
//    }//note:对子类不起作用, 如果双向绑定使用了android:text 这个方法会导致异常getTextBeforeCursor on inactive InputConnection 原因不明

    //2.2
    @BindingAdapter("app:imgId")
    @JvmStatic
    fun setImgToView(img: ImageView, id: Drawable) {
        img.setImageDrawable(id)
        println("--------------id = ${id}---------------")
    }//note: app:imgId="@{@drawable/dog}" 属性传入的值要用 @{}的形式，否则报错 AAPT: xx: attribute xx not found.
    //      函数参数需要int @{这个值就必须是int} 如果参数需要Drawable @{这个值就必须是Drawable}

    @BindingAdapter(value = ["imageUrl", "placeholder", "error"], requireAll = false)
    @JvmStatic
    fun loadImage(view: ImageView, url: String, placeholder: Drawable, error: Drawable) {
        //还能设置多个属性 ，所有属性都设置了 这个BindAdapter才起作用
    }

    //1.3
    @BindingConversion
    @JvmStatic
    fun convertColorToDrawable(color: String): Drawable {
        println("convertColorToDrawable------------------${color}")
        return ColorDrawable(Color.parseColor(color))
    }//note:查看编译后的代码 被转化成 @BindingAdapter("android:background")。相当于2.2第一个参数不用再传类型，在哪个控件上用
    //      只要函数返回结果是 属性需要的就可以 //build/generated/source/kapt/debug/包名/databinding/不具名+BindingImpl

    //2.1 @InverseBindingAdapter 提供了如何从控件获取值
    @InverseBindingAdapter(attribute = "myName")
    @JvmStatic
    fun getTime(view: MyButton): String? {
        println("从控件获取值----${view.myName}")
        return view.myName
    }

    //2.1 只提供@InverseBindingAdapter还不够，还需要调用onChange()之后控件才会提供值
    @BindingAdapter("app:myNameAttrChanged")
    @JvmStatic
    fun setListeners(
        view: MyButton,
        attrChange: InverseBindingListener
    ) {
        view.setOnClickListener {//view的事件----引起view属性更改事件----调用@InverseBindingAdapter------把值传递给变量
//            if (view.myName == "123") {
            println("setOnClickListener----更新")
//
//            } else {
//
//            }
            view.myName = "123"
            attrChange.onChange()//
        }
    }

    //3. 防止无限循环
    @BindingAdapter("app:myName")
    @JvmStatic
    fun setMyName(view: MyButton, str: String?) {
        str?.apply {
            if (view.myName != str) {//如果新值和旧值不一样才赋值
                println("set myName-----${str}")
                view.myName = str
            }
        }
    }

}