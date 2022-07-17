[官方文档](https://developer.android.google.cn/jetpack/guide?hl=en)
> 移动设备也是资源受限的，因此在任何时候，操作系统可能会扼杀一些应用程序进程，为新的应用程序腾出空间。进程的销毁不在用户的控制范围之内，所以不要在四大组件内保存数据
> 并且组件之间不要相互依赖

#### 1. 一般构建原则

1. 从Activity和Fragment生命周期回调方法中分离代码逻辑
2. 通过数据持久化从UI的生命周期中分离数据，这样做及时后台回收你的APP数据仍然不会丢失，当网络连接不稳定的时候，程序仍然能够继续执行

#### 2. 推荐使用的APP架构

&emsp;&emsp;本页面中提到的构建原则应用于广泛的应用程序，以使它们能够扩展、提高质量和健壮性，并使它们更易于测试，并且视为指导方针，并根据需要调整它们以适应您的需求

1. 一个应用程序最少有三层组成，UI层显示数据；数据层包含APP的业务逻辑并控制数据；domain层充当前者的交互  
   ![一个典型的三层架构APP](https://developer.android.google.cn/topic/libraries/architecture/images/mad-arch-overview.png)

- UI layer 每当数据由于用户交互(例如按下按钮)或外部输入(例如网络响应)而发生变化时，更新UI以反映更改。UI层包括UI元素和state holder后者用来保存数据(ViewModel)、提供数据、处理逻辑
- Domain layer 域层负责封装复杂的业务逻辑或重用业务逻辑。例如创建GPSInfo类处理有关GPS的一切事务
- Data layer 包含了应用程序的业务逻辑，由数据类和数据源(数据库、网络服务器)组成

2. 管理组件之间的依赖

- 建议使用 Hilt library 构建依赖 TODO 学习Hilt

3. 最佳实践

- 不要在四大组件中存储数据
- 减少类之间的依赖，app应该只依赖Android 提供的API 例如 Toast、Context。
- 明确模块之间的功能
- 不要暴露模块内部实现细节。
- 尽量让APP的每个功能都可以独立测试
- 尽可能多的数据持久化 即时在离线状态下用户也能够使用程序

4. MAD

- MAD 可以指导开发者更高效地开发出优秀的移动应用 [ MAD 最佳实践](https://mp.weixin.qq.com/s/Fq6AA2IWpDzjtiRkQZFIwA)

[解决Stack Overflow访问卡慢](https://github.com/justjavac/ReplaceGoogleCDN)

  
 
