package com.example.jetpack.topics.anim

import android.animation.Animator
import android.view.ViewGroup
import androidx.transition.Transition
import androidx.transition.TransitionValues

/**
 * 自定义转场动画
 */
class MyTransition : Transition() {
    override fun captureStartValues(transitionValues: TransitionValues) {
        TODO("Not yet implemented")
    }

    override fun captureEndValues(transitionValues: TransitionValues) {
        TODO("Not yet implemented")
    }

    override fun createAnimator(
        sceneRoot: ViewGroup,
        startValues: TransitionValues?,
        endValues: TransitionValues?
    ): Animator? {
        return super.createAnimator(sceneRoot, startValues, endValues)
    }
}