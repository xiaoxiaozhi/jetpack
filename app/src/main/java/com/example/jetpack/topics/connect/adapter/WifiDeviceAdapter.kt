package com.example.jetpack.topics.connect.adapter

import android.net.wifi.p2p.WifiP2pDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.example.jetpack.R

class WifiDeviceAdapter : RecyclerView.Adapter<WifiDeviceAdapter.MyViewHolder>() {
    private lateinit var click: (WifiP2pDevice) -> Unit

    val lists = mutableListOf<WifiP2pDevice>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.wifi_device_item_layout, parent, false)
        )
    }

    fun setDataList(temp: List<WifiP2pDevice>) {
        lists.clear()
        lists.addAll(temp)
        notifyDataSetChanged()
    }

    fun setOnClick(action: (WifiP2pDevice) -> Unit) {
        click = action
    }

    override fun getItemCount(): Int {
        return lists.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = lists[position]
        holder.info.text = "${data.deviceName}  ${data.deviceAddress} ${data.primaryDeviceType}"
        holder.itemView.setOnClickListener {
            if (::click.isInitialized) click
        }
    }

    class MyViewHolder(itemView: View) : ViewHolder(itemView) {
        val info = itemView.findViewById<TextView>(R.id.wifiInfo)
    }
}