package com.app.serviceprovideradmin.bookings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.serviceprovideradmin.R
import com.bumptech.glide.Glide

class BookingRecycler(private val context: Context, private val list:List<Bookmodelclass>) : RecyclerView.Adapter<BookingRecycler.Viewholder>() {
    class Viewholder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val id=itemView.findViewById<TextView>(R.id.ids)
        val coverimg=itemView.findViewById<ImageView>(R.id.coverimg)
        val title=itemView.findViewById<TextView>(R.id.title)
        val offer=itemView.findViewById<TextView>(R.id.offer)
        val price=itemView.findViewById<TextView>(R.id.price)
        val status=itemView.findViewById<Button>(R.id.status)
        val name=itemView.findViewById<TextView>(R.id.name)
        val number=itemView.findViewById<TextView>(R.id.number)
        val email=itemView.findViewById<TextView>(R.id.email)
        val time=itemView.findViewById<TextView>(R.id.time)
        val date=itemView.findViewById<TextView>(R.id.date)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingRecycler.Viewholder {
        val view= LayoutInflater.from(context).inflate(R.layout.bookings,parent,false)
        return Viewholder(view)
    }

    override fun onBindViewHolder(holder: BookingRecycler.Viewholder, position: Int) {
        Glide.with(context).load(list[position].coverImage).into(holder.coverimg)
        holder.title.text=list[position].title
        holder.offer.text=list[position].offer
        holder.price.text=list[position].price
        holder.status.text=list[position].status
        holder.id.text=list[position].id
        holder.name.text=list[position].name
        holder.number.text=list[position].number
        holder.email.text=list[position].email
        holder.time.text=list[position].time
        holder.date.text=list[position].date

    }

    override fun getItemCount(): Int {
        return list.size
    }
}