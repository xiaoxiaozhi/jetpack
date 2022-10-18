package com.example.jetpack.bestpractice.mypractices

import android.content.AttributionSource
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.coordinatorlayout.widget.CoordinatorLayout

/**
 * [CoordinatorLayout详解，其中有关于自定义Behavior的内容](https://blog.csdn.net/qq_33209777/article/details/105141612)
 */
class FollowBehavior(context: Context, attrs: AttributeSet) : CoordinatorLayout.Behavior<TextView>(context, attrs) {
    /**
     * dependency是在CoordinatorLayout布局中的存在的子view类型
     */
    override fun layoutDependsOn(parent: CoordinatorLayout, child: TextView, dependency: View): Boolean {
        return dependency is Button
    }

    /**
     * child 是观察者， dependency是被观察者
     */
    override fun onDependentViewChanged(parent: CoordinatorLayout, child: TextView, dependency: View): Boolean {
        child.x = dependency.x + 200;
        child.y = dependency.y + 200;
        child.text = "观察者:" + dependency.x + "," + dependency.y;
        return true;
    }
}
//btn.setOnTouchListener(new View.OnTouchListener() {
//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_MOVE) {
//            v.setX(event.getRawX() - v.getWidth() / 2);
//            v.setY(event.getRawY() - v.getHeight() / 2 - getStatusBarHeight(getApplicationContext()));
//        }
//        return true;
//    }
//});

