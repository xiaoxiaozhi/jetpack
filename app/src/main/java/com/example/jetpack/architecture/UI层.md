#### 1. 例子分析  
 UI数据类，应该定义成不可修改，只能通过修改数据源来修改数据，违反这一原则会导致信息源和数据类型信息不一致 
```
data class NewsUiState(
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false,
    val newsItems: List<NewsItemUiState> = listOf(),
    val userMessages: List<Message> = listOf()
)
```      
#### 2. UI层架构
- 使用数据，使其可视化。  
- UI元素+UI状态 = UI layer。UI状态类应该定义为不可修改，推荐使用ViewModel管理 
- 用户输入数据，更新数据层以及UI层。 

#### 3. 使用单向数据流管理状态  
- 使用ViewModel作为状态容器控制UI状态
- 状态向下流动、事件向上流动的这种模式称为单向数据流 (UDF) ![详情请查看图片](https://developer.android.google.cn/topic/libraries/architecture/images/mad-arch-ui-udf-in-action.png) 
- 逻辑类型  
  - 业务逻辑 例如添加标签，应该属于domain 层或者数据层，绝不会写在ViewModel(TODO 不理解)
  - 界面逻辑 决定着如何在屏幕上显示状态变 应该位于界面中，而不是在ViewModel

#### 4. 公开界面状态
定义界面状态并确定如何管理相应状态的提供后，下一步是将提供的状态发送给界面。由于您使用 UDF 管理状态的提供，因此您可以将提供的状态视为数据流
换句话说，随着时间的推移，将提供状态的多个版本 使用 LiveData 或 StateFlow 等数据流容器
```kotlin
class NewsViewModel() : ViewModel() {
    val uiState: StateFlow<NewsUiState> 
}
```
- 界面状态对象应处理彼此相关的状态 如果您在两个不同的数据流中分别公开新闻报道列表和书签数量，可能会发现其中一个已更新，但另一个没有更新。当您使用单个数据流时，这两个元素都会保持最新状态
```kotlin
data class NewsUiState(
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false,
    val newsItems: List<NewsItemUiState> = listOf()
)

val NewsUiState.canBookmarkNews: Boolean get() = isSignedIn && isPremium
```
- 不使用单个数据流的情况，某些UI状态更新频率明显高于其他状态，不能将这些状态捆绑在一起；对象中的状态字段特别多，只是其中一个更新的话，界面中其它也要更新，需要使用Flow 的distinctUntilChanged()方法解决

#### 5. 界面事件
- 用户通过与应用互动（例如，点按屏幕或生成手势）来生成用户事件，ViewModel 通常负责处理特定用户事件的业务逻辑
- 界面事件：界面可以直接处理的界面行为逻辑。例如转到其他屏幕或显示 Snackbar。