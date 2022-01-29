https://developer.android.google.cn/guide/components/activities/parcelables-and-bundles?hl=en   
1. Activity之间传递数据。   
   Intent 通过Intent().putExtra()传递数据，把数据放在Bundle中，传递原始数据和序列化数据，这种方法数据限制在几Kb之内否则会报错
2. 进程之间传递数据  
   类似于在活动之间发送数据，建议不要使用自定义的序列化数据，这样做会报错，因为系统无法对它不知道的类进行解密。
   如果非要发送则需要确保在发送和接收应用程序中都存在完全相同版本的自定义类  
3. 对于SavedInstanceState的具体情况，数据量应该保持较小，我们建议您将保存的状态保持在小于50K的数据。