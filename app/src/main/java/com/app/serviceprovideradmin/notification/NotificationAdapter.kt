package com.app.serviceprovideradmin.notification

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.serviceprovideradmin.R
import com.bumptech.glide.Glide

class NotificationAdapter(private val context: Context, private val list: List<NotificationDataClass>) :RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {
    class ViewHolder(itemview:View):RecyclerView.ViewHolder(itemview) {
        val image=itemview.findViewById<ImageView>(R.id.img)
        val title=itemview.findViewById<TextView>(R.id.title)
        val description=itemview.findViewById<TextView>(R.id.description)
        val id=itemview.findViewById<TextView>(R.id.idss)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
         val view=LayoutInflater.from(context).inflate(R.layout.notificationrecycler,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.title.text=list[position].title
        holder.description.text=list[position].description
        Glide.with(context).load(list[position].imageUrl).into(holder.image)
        holder.id.text=list[position].id
    }


}