[md动画设计准则](https://material.io/design/motion/understanding-motion.html#principles)
属性动画 PropertyAnimationActivity   
矢量图动画 AnimatedVectorActivity   
显示或隐藏动画-->淡入淡出、卡片翻转、揭露动画(AnimatedVectorActivity)
投掷动画 FlingActivity FlingAnimation 即指尖离开屏幕后的惯性动画  例如快速滑动下拉列表   例子fling-animation-demo  [文章链接](https://juejin.cn/post/6982514357389230111)  
缩放动画，通过动画将视图从缩略图过渡到填满整个屏幕的完整尺寸图片，这对于照片图库等应用来说十分有用 
弹簧动画 TODO 待总结    
布局过渡动画、共享元素过渡动画 TransitionActivity 

##### 1. 创建动画

在android studio 中创建动画，res/animator 右键 <set> <objectAnimator>  note：右键res和Drawable 无法创建这两个标签 
