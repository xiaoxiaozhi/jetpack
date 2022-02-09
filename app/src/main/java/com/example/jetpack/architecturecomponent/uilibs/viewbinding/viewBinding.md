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
```
    //在Activity中使用ViewBinding
    private lateinit var binding: ResultProfileBinding

    override fun onCreate(savedInstanceState: Bundle) {
        super.onCreate(savedInstanceState)
        binding = ResultProfileBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }
    //在Fragment中使用ViewBinding
       private var _binding: ResultProfileBinding? = null
       private val binding get() = _binding!!

       override fun onCreateView(inflater: LayoutInflater,container: ViewGroup?,savedInstanceState: Bundle?): View? {
           _binding = ResultProfileBinding.inflate(inflater, container, false)
           val view = binding.root
           return view
       }
       override fun onDestroyView() {
           super.onDestroyView()
           _binding = null
       }
       
```
