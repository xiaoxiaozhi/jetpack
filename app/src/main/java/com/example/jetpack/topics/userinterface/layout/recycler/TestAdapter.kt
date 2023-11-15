package com.example.jetpack.topics.userinterface.layout.recycler

import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.selection.ItemDetailsLookup
import androidx.recyclerview.selection.ItemKeyProvider
import androidx.recyclerview.selection.SelectionTracker
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpack.R

class TestAdapter(private val list: List<Person>) : RecyclerView.Adapter<TestAdapter.MyViewHolder>() {

    var tracker: SelectionTracker<Person>? = null
    private var isClick = false

    // 自定义ViewHolder
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_name)
        private val tvTelephone: TextView = itemView.findViewById(R.id.tv_telephone)

        // 绑定数据
        fun bind(person: Person, isActivated: Boolean) {
            tvName.text = person.name
            tvTelephone.text = person.telephone
            itemView.isActivated = isActivated
        }

        // 获取 ItemDetails
        fun getItemDetails() = object : ItemDetailsLookup.ItemDetails<Person>() {

            override fun getPosition(): Int = absoluteAdapterPosition

            override fun getSelectionKey(): Person = list[absoluteAdapterPosition]
        }
    }

    // 自定义 ItemKeyProvider
    class MyKeyProvider(private val adapter: TestAdapter) : ItemKeyProvider<Person>(SCOPE_MAPPED) {

        override fun getKey(position: Int): Person = adapter.getItem(position)

        override fun getPosition(key: Person): Int = adapter.getPosition(key)

    }

    fun getItem(position: Int): Person = list[position]

    fun getPosition(person: Person): Int = list.indexOf(person)

    // 自定义 ItemDetailsLookup
    class MyItemDetailsLookup(private val recyclerView: RecyclerView) : ItemDetailsLookup<Person>() {
        override fun getItemDetails(e: MotionEvent): ItemDetails<Person>? {
            val view = recyclerView.findChildViewUnder(e.x, e.y)
            return if (view != null) (recyclerView.getChildViewHolder(view) as MyViewHolder).getItemDetails() else null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_telephone, parent, false)
        )
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = list[position]
        tracker?.let {
            holder.bind(data, it.isSelected(data))
        }
        //高频刷新情况下，Click时间失效，解决方法是 用一个标志 放在 if(!isClick)notifyDataSetChanged  这个判断放在调用notifyDataSetChanged的地方
        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    isClick = true
                    Log.i("xiazai", "Item: ACTION_DOWN")
                }

                MotionEvent.ACTION_MOVE -> Log.i("xiazai", "Item: ACTION_MOVE")
                MotionEvent.ACTION_UP -> {
                    isClick = false
                    Log.i("xiazai", "Item: ACTION_UP")
                }

                MotionEvent.ACTION_CANCEL -> {
                    isClick = false
                    Log.i("xiazai", "Item: ACTION_CANCEL")
                }
            }
            false
        }
        holder.itemView.setOnClickListener {

        }
    }

    override fun getItemCount(): Int = list.size

}
