package com.example.jetpack.architecturecomponent.uilibs.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.jetpack.R

class RepoAdapter : PagingDataAdapter<Repo, RepoAdapter.ViewHolder>(COMPARATOR) {
    //[DiffUtil工具使用](https://blog.csdn.net/zxt0601/article/details/52562770) TODO
    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<Repo>() {
            override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
                //判断主键如果返回false则会直接去更新ui 如果返回true 则会执行areContentsTheSame
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
                //用于判断要显示的内容是否完全一致
                return oldItem == newItem // == 判断内容是否一致
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name_text)
        val description: TextView = itemView.findViewById(R.id.description_text)
        val starCount: TextView = itemView.findViewById(R.id.star_count_text)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.repo_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val repo = getItem(position)
        if (repo != null) {
            holder.name.text = repo.name
            holder.description.text = repo.description
            holder.starCount.text = repo.starCount.toString()
        }
    }

}
