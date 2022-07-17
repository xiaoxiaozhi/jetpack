https://developer.android.google.cn/guide/components/loaders?hl=en  
> Loader 在API28中被废弃，在处理Activity和Fragment加载数据问题上，推荐使用 ViewModels and LiveData.   
##### Loader的作用 
1. Loader在线程中管理cursors的操作避免UI卡顿
2. cursor数据持久化，横竖屏切换也不需要重新查询
3. 观察Cursor数据变化。