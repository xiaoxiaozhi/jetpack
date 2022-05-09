[直接看后台处理指南](https://developer.android.google.cn/guide/background)
##### 1. 后台任务类型
- ![后台任务决策树](https://developer.android.google.cn/images/guide/background/background.svg)
```
if(用户交互){
    即时任务
}else if(在精确时间点执行){
    精确任务
}else{
    延期任务
}
```
- 即时任务
  - 对于应在用户离开特定作用域或完成某项互动时结束的任务，我们建议使用 Kotlin 协程。
  - 对于应立即执行并需要继续处理的任务，即使放在后台运行或重启设备，也要执行，我们建议使用 WorkManager 并利用其对长时间运行的任务的支持。
  - 在特定情况下（例如使用媒体播放或主动导航功能时），您可能希望直接使用前台服务。
- 延期任务  
  - 凡是不直接与用户互动相关且日后可随时运行的任务，都可以延期执行。可延期异步任务即使在应用退出或设备重启后仍能正常运行，建议为延期任务使用 WorkManager 解决方案。
- 精确任务
  - 需要在精确时间点执行的任务可以使用 AlarmManager。


