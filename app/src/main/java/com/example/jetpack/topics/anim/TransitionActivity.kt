package com.example.jetpack.topics.anim

import android.app.ActivityOptions
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.transition.Fade
import android.transition.Slide
import android.view.View
import android.widget.RadioGroup
import androidx.databinding.DataBindingUtil
import androidx.transition.ChangeBounds
import androidx.transition.Scene
import androidx.transition.TransitionManager
import com.example.jetpack.R
import com.example.jetpack.databinding.ActivityTransactionBinding
import com.example.jetpack.databinding.ActivityTransitionBinding
import android.util.Pair
import android.view.Gravity
import android.view.Window
import androidx.transition.TransitionSet

/**
 *  [过度动画，内含系统定义好的过度动画](https://www.jianshu.com/p/1007f300f17a)
 *  [过渡动画，这篇文章更好](https://juejin.cn/post/6850037271714856968)
 *  1. Scene Transition(场景过渡动画 其实就是为布局变化添加动画效果)
 *     两个不同布局，存在id相同的两个控件，布局切换的时候场景过渡动画框架能够提供支持提供，以下是场景过度动画的创建方式
 *     1.1 创建场景
 *         为起始布局和结束布局创建一个 Scene 对象;从布局资源创建场景,如果文件中的视图层次结构大部分是静态的 Scene.getSceneForLayout(mSceneRoot, R.layout.scene1, getActivity());
 *         从代码中创建场景(场景)  Scene(sceneRoot, View对象)
 *         1.1.1 创建场景操作  起始场景中调用Scene.setExitAction(Runnable) 结束场景中调用Scene.setEnterAction(Runnable) 请勿使用场景操作在起始场景和结束场景的视图之间传递数据 TODO 没反应
 *
 *         1.1.3 调用 TransitionManager.go()，然后系统会运行动画以交换布局。
 *     1.2 创建过度动画
 *         1.2.1 从资源文件创建过渡 TODO 待看
 *     1.3 应用没有场景的过渡
 *         添加、修改和移除当前布局的子视图来进行更改TransitionManager.beginDelayedTransition(sceneRoot)
 *  2. Shared Element Transition(共享元素过渡动画)
 *     2.1 两个Activity共享元素过渡动画 单元素和多元素：Activity1 中给要添加共享动画的控件添加过渡名称。android:transitionName="activityTransform"，在Activity2
 *         中给要添加共享动画的控件添加相同的过渡名称，然后调用ActivityOptions获取bundle，在startActivity的时候使用
 *     Note:如果Activity2使用网络加载图片，未加载完成，过渡动画不显示，此时要在setContentView下面调用  postponeEnterTransition()延迟加载动画，再在加载完成时调用startPostponedEnterTransition()
 *  3. Activity进入、退出动画
 *     android 默认支持 爆炸式、淡入淡出、滑动式
 *     3.1 实现流程 在Activity的onCreate()方法中调用  window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)在 setContentView()前设置启用窗口过渡属性;将过渡属性赋值给windows属性
 *     3.2 跳转的时候 startActivity(Intent(this, Transition1Activity::class.java),    ActivityOptions.makeSceneTransitionAnimation()) 进入退出动画生效
 *     TODO   window.sharedElementEnterTransition 这个属性好像是共享元素动画的第二种用法带看
 *  4. 自定义过度动画
 *     TODO (参考)[https://blog.csdn.net/qibin0506/article/details/53248597]
 */
class TransitionActivity : AppCompatActivity(), RadioGroup.OnCheckedChangeListener {
    lateinit var dataBinding: ActivityTransitionBinding
    lateinit var scene1: Scene
    lateinit var scene2: Scene
    lateinit var scene3: Scene
    lateinit var scene4: Scene
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //3.1 设置Activity进入进出动画
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.requestFeature(Window.FEATURE_CONTENT_TRANSITIONS)//启用窗口过渡属性
            window.allowEnterTransitionOverlap = false//退出与进入过渡动画会有一小段交叉的过程,设置后禁止交叉
            val slide = Slide().apply {
                duration = 300//效果时长，一般Activity切换时间很短，不建议设置过长
                slideEdge = Gravity.START//滑动动画设置方向
                excludeTarget(android.R.id.statusBarBackground, true)//排除状态栏
                excludeTarget(android.R.id.navigationBarBackground, true)//排除导航栏
            }.also {
                window.exitTransition = it  //退出当前界面的过渡动画
                window.enterTransition = it  //进入当前界面的过渡动画
                window.reenterTransition = it  //重新进入界面的过渡动画
            }
//            android.transition.TransitionSet().apply {
//                addTransition(android.transition.ChangeImageTransform())
//                addTransition(android.transition.ChangeBounds())
//                addTransition(android.transition.Fade(Fade.MODE_IN))
//            }.also {
//                window.sharedElementEnterTransition = it
//            }
//

        }
        //---------------------------------------------------
        dataBinding = DataBindingUtil.setContentView<ActivityTransitionBinding>(
            this,
            R.layout.activity_transition
        )
        dataBinding.selectScene.setOnCheckedChangeListener(this)
        scene1 = Scene.getSceneForLayout(
            dataBinding.sceneRoot, //场景所在的根View
            R.layout.scene1,       //场景布局Id
            this
        )
        //2.1
        dataBinding.button1.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//判断Android版本
                //单个共享元素
                val bundle =
                    ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        dataBinding.image1,
                        "activityTransform"
                    )
                        .toBundle()
//                startActivity(Intent(this, Transition1Activity::class.java), bundle)
                //多个元素
                val imagePair = Pair<View, String>(dataBinding.image1, "activityTransform")
                val textPair = Pair<View, String>(dataBinding.tvText, "textTransform")
                val bundle2 =
                    ActivityOptions.makeSceneTransitionAnimation(
                        this,
                        imagePair, textPair
                    ).toBundle()

                startActivity(Intent(this, Transition1Activity::class.java), bundle2)

            } else {
                startActivity(Intent(this, Transition1Activity::class.java))
            }
        }
        //1.1
        scene1 = Scene.getSceneForLayout(dataBinding.sceneRoot, R.layout.scene1, this)
            .apply {
                //1.1.1
                setEnterAction { println("scene1------进入") }
                setExitAction { println("scene1------退出") }
            }
        scene2 = Scene.getSceneForLayout(dataBinding.sceneRoot, R.layout.scene2, this)
            .apply {
                setEnterAction { println("scene2------进入") }
                setExitAction { println("scene2------退出") }
            }

    }

    override fun onCheckedChanged(group: RadioGroup?, checkedId: Int) {
        when (checkedId) {
            R.id.select_scene_1 -> TransitionManager.go(scene1)//
            R.id.select_scene_2 -> TransitionManager.go(scene2)
            R.id.select_scene_3 -> {
                //1.3
                TransitionManager.beginDelayedTransition(dataBinding.sceneRoot)
                val transitionSquar =
                    dataBinding.sceneRoot.findViewById<View>(R.id.transition_square)
                println("------------------$transitionSquar")
                transitionSquar.layoutParams?.apply {
                    width = 500
                    height = 500
                }.also {
                    transitionSquar?.layoutParams = it
                }
            }


        }
    }
}