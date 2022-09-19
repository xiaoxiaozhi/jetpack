#### 相机实例工作流程

例子所在CameraXActivity

1. 配置config():准备好ProcessCameraProvider、PreviewView、CameraSelector、用例 Preview、
2. 点击按钮:
3. 在屏幕横竖转换的时候重新配置
4. 在activity退出时销毁相关类，防止内存泄漏。ProcessCameraProvider
5. 获取相机支持的分辨率有两种方法：①CameraInfo获取支持分辨率 ②相机的Characteristics获取支持分辨率。 两者效果一直

  ```kotlin
   //获取CameraInfo
provider.bindToLifecycle(requireActivity(), camSelector)//绑定后通过Camera获取CameraInfo
//或
cameraProvider?.availableCameraInfos?.filter {}//调用cameraProvider获得CameraInfo
//使用CameraInfo获取分辨率
QualitySelector.getSupportedQualities(camera.cameraInfo)
 ```

```kotlin
val characteristics = cameraManager.getCameraCharacteristics(identifier)
characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)//此摄像头设备支持的可用流配置
    .let {
        println("摄像头返回输出流支持的Format列表 ${it?.outputFormats}")
    }
```

- [ ] Display是什么东西？可能跟双屏幕有关

#### 关键类解释

[What is an Android window?](https://stackoverflow.com/questions/9451755/what-is-an-android-window)

[Display](https://developer.android.google.cn/reference/android/view/Display?hl=en)    
提供有关逻辑显示器的大小和密度的信息。应用程序显示面积只是display的一部分即只有应用程序窗口部分，不包含装饰器部分(状态栏、导航栏)。  
使用WindowMetrics.getBounds()得到app窗口边界.每个view都绑定了一个display，且只有view创建的时候display才不为空