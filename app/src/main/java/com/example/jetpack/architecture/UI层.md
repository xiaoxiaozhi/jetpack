1. UI 层  
&emsp;&emsp;1.1 UI数据类，应该定义成不可修改，只能通过修改数据源来修改数据，违反这一原则会导致信息源和数据类型信息不一致 
```
data class NewsUiState(
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false,
    val newsItems: List<NewsItemUiState> = listOf(),
    val userMessages: List<Message> = listOf()
)
```      
&emsp;&emsp;1.2 ViewModel //处理UI元素传过来的事件、通知UI显示数据、通知数据层修改数据(存疑)//
3. 使用单向数据流管理数据类    
数据向下和事件向上的模式称为单向数据流(UDF).  
![UDF](https://developer.android.google.cn/topic/libraries/architecture/images/mad-arch-ui-udf-in-action.png)  
//用户标记文章(UI事件)--->ViewModel(通知数据层修改状态)--->data layer(更新数据)--->ViewModel(接收数据层的数据通知UI层更新并显示数据)//存疑  

TODO 看不懂 先学习 LiveData ViewModel