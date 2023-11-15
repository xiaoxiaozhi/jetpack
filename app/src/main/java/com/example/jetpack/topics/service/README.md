#### 0. 服务定义  
Service 是一种可在后台执行长时间运行操作而不提供界面的应用组件。与线程的区别 长时间运行，而且服务运行在主线程
#### 1. 三种服务    
- 前台
>前台服务执行一些用户能注意到的操作。例如，音频应用会使用前台服务来播放音频曲目。前台服务必须显示通知。即使用户停止与应用的交互，前台服务仍会继续运行。  
- 后台  
>后台服务执行用户不会直接注意到的操作。例如，如果应用使用某个服务来压缩其存储空间，则此服务通常是后台服务。  
注意：如果您的应用面向 API 级别 26 或更高版本，当应用本身未在前台运行时，系统会对运行后台服务施加限制。在诸如此类的大多数情况下，您的应用应改为使用计划作业。
- 绑定
>当应用组件通过调用 bindService() 绑定到服务时，服务即处于绑定状态。
>绑定服务会提供客户端-服务器接口，以便组件与服务进行交互、发送请求、接收结果，甚至是利用进程间通信 (IPC) 跨进程执行这些操作。
>仅当与另一个应用组件绑定时，绑定服务才会运行。多个组件可同时绑定到该服务，但全部取消绑定后，该服务即会被销毁。   

#### 2. 创建启动服务     
- 扩展服务类  
>启动Service或者IntentService (在android11 API30中被废弃)创建启动服务   
IntentService就是一个自带子线程的Service。而onHandleIntent就是在子线程中执行的。多次startService()，会把Intent送进队列，等全部执行完成后调用stopSelf()终止服务
onStartCommand() 返回的整数，用于描述系统应如何在系统终止服务的情况继续运行服务。
START_NOT_STICKY:除非有待传递的挂起 Intent，否则系统不会重建服务. 
START_STICKY：系统终止服务，稍后会重建服务并调用 onStartCommand()，但不会重新传递最后一个 Intent。所以onStartCommand()传进来的参数为空。
START_REDELIVER_INTENT：系统终止服务，则其会重建服务，并通过传递给服务的最后一个 Intent 调用 onStartCommand()  
- 启动服务  
>startService() 或 startForegroundService()(在android8.0 API26 此方法会创建前台服务，它会向系统发出信号，表明服务会将自行提升至前台。创建服务后，该服务必须在五秒内调用自己的 startForeground() 方法)启动服务  
希望服务返回结果，则启动服务的客户端可以为广播（通过 getBroadcast() 获得）创建一个 PendingIntent，并将其传递给启动服务的 Intent 中的服务。然后，服务便可使用广播传递结果。//TODO 没试过,我认为是服务发送一个广播把结果传递给Activity(广播作为Activity的子类)
- 停止服务
>除非必须回收内存资源，否则系统不会停止或销毁服务。服务必须通过调用stopSelf()自行停止运行，或由另一个组件通过调用stopService来停止它。
如果服务同时处理多个对 onStartCommand() 的请求，则您不应在处理完一个启动请求之后停止服务,为避免此问题，您可以使用 stopSelf(int) 确保服务停止请求始终基于最近的启动请求(具体查看StartService)
为避免浪费系统资源和消耗电池电量，请确保应用在工作完成之后停止其服务

#### 3. 创建绑定服务  
- 与 Activity 或者其他应用组件进行交互，或需要通过进程间通信 (IPC) 向其他应用公开某些应用功能，则应创建绑定服务。 
- 实现 onBind() 回调方法返回 IBinder，从而定义与服务进行通信的接口
- 多个客户端可以同时绑定到服务。完成与服务的交互后，客户端会通过调用 unbindService() 来取消绑定。

#### 4. 向用户发送通知  
- 处于运行状态时，服务可以使用***Toast通知*** 或***状态栏通知*** 来通知用户所发生的事件。  

#### 5. 前台服务  
- 前台服务是用户主动意识到的一种服务，因此在内存不足时，系统也不会考虑将其终止。前台服务必须为状态栏提供通知，将其放在运行中的标题下方。这意味着除非将服务停止或从前台移除，否则不能清除该通知。 
- 如果应用面向 Android 9（API 级别 28）或更高版本并使用前台服务，则其必须请求 FOREGROUND_SERVICE 权限，这是一种普通权限，系统会自动授予否则会报错
- 如要请求让服务在前台运行，请调用 startForeground()。此方法采用两个参数：唯一标识通知的整型数和用于状态栏的 Notification。此通知必须拥有 PRIORITY_LOW 或更高的优先级。
- 如要从前台移除服务，请调用 stopForeground()


内存不足时系统会回收启动服务；绑定服务绑定到拥有用户焦点的 Activity，不太可能会终止；前台服务几乎不会被回收   
添加 android:exported 属性并将其设置为 false，确保服务仅适用于您的应用  
从 Android 5.0（API 级别 21）开始，如果使用隐式 Intent 调用 bindService()，则系统会抛出异常。
