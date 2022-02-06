#### 1. 开启视图绑定  
在Android studio 3.6版本之上可用，在build.gradle 文件中添加
```
android {
         ... 
         viewBinding {
                 enabled = true 
        } 
}
```
如果您希望在生成绑定类时忽略某个布局文件，请将 tools:viewBindingIgnore="true" 属性添加到相应布局文件的根视图中：
```
<LinearLayout
            ...
            tools:viewBindingIgnore="true" >
        ...
    </LinearLayout>
    
```
#### 2. 用法  
