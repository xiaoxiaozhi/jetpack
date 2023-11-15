package com.example.jetpack.topics.userinterface.layout.recycler

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.selection.SelectionPredicates
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.selection.StorageStrategy
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpack.R

/**
 * 1. 时间轴
 * 2. 间隔
 * 3. 复选 recyclerview-selection
 * StaggeredGridLayoutManager 这是什么
 * RecyclerView.ItemAnimator 未列表添加动画
 * recyclerView.findChildViewUnder(e.x, e.y)，通过屏幕坐标获取哪个item
 * getBindingAdapterPosition() getAbsoluteAdapterPosition()这两个方法是干什么的
 * 不同的viewType对应不同的布局，这时候都写在一个Adapter里面会最严重增加耦合性，此时需要使用MergeAdapter
 * [MergeAdapter相关文章](https://cloud.tencent.com/developer/article/1620018)
 * [MergeAdapter改名为ContactAdapter](https://juejin.cn/post/6962754669756022791) 调用第一个adapter的更新，更新不了，ContactAdapter也更新不了，值得探究
 * addItemDecoration 添加分割线，适用于GridView吗？
 * ContactAdapter 中的一个子adapter调用notifyItemChanged(0) 不起作用无法刷新界面 是什么原因呢
 * [RecyclerView在高速刷新的时候 没有点击事件](https://blog.csdn.net/fu908323236/article/details/78061162) 该方法还是偶尔失效
 * 加上100毫秒刷一次的判断发现不再失效，效果还能接受
 *   val tmpTime = System.currentTimeMillis()
 *             if (tmpTime - lastTime > 100) {
 *                 lastTime = tmpTime
 *                 downloadingAdapter.setData(it)
 *             }
 * [局部刷新而不是整条item刷新](https://blog.csdn.net/qijingwang/article/details/122821149) 局部刷新
 * 一种情况：有checkbox的item 点击checkbox的点击事件会被box拦截， 如果想统一 点击事件 到holder.itemView，加个透明蒙板或者设置checkbox为enable
 *
 *
 */
class RecyclerActivity : AppCompatActivity() {
    var tracker: SelectionTracker<Person>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler)

        // 固定数据
        val list: List<Person> = ArrayList(
            listOf(
                Person(1L, "Mike", "86100100"),
                Person(2L, "Jane", "86100101"),
                Person(3L, "John", "86100102"),
                Person(4L, "Amy", "86100103"),
            )
        )

        // 设置 RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.rv_telephone)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        val adapter = TestAdapter(list)
        recyclerView.adapter = adapter

        // 实例化 tracker
        tracker = SelectionTracker.Builder(
            "test-selection",
            recyclerView,
            TestAdapter.MyKeyProvider(adapter),
            TestAdapter.MyItemDetailsLookup(recyclerView),
            StorageStrategy.createParcelableStorage(Person::class.java)
        ).withSelectionPredicate(SelectionPredicates.createSelectAnything()).build()
        adapter.tracker = tracker
        // 监听数据
        tracker?.addObserver(object : SelectionTracker.SelectionObserver<Person>() {
            override fun onSelectionChanged() {
                super.onSelectionChanged()
                for (item in tracker?.selection!!) {
                    Log.i("RecyclerActivity", "item:$item")
                }
            }
        })
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        tracker?.onRestoreInstanceState(savedInstanceState)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        tracker?.onSaveInstanceState(outState)
    }
}