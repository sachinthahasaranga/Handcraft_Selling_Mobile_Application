package com.example.catchycraft

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class inqurieAdapter(private  val userList:ArrayList<Product>, val context: Context): RecyclerView.Adapter<inqurieAdapter.CustomViewHolder>() {
    private var onClickListener: OnClickListener? = null
    override fun getItemCount(): Int {
        return userList.size
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context).inflate(R.layout.inquries_card_view,parent,false)

        return CustomViewHolder(layoutInflater)
    }
    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val user :Product=userList[position]
        holder.pramotionTile.text=user.title
        holder.contact.text=user.phoneNumber
        holder.discription.text= user.description
        Glide.with(context).load(user.url).into( holder.image);


        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, user )
            }
        }

    }
    public class CustomViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val pramotionTile: TextView =itemView.findViewById(R.id.inquirie_title)
        val discription: TextView =itemView.findViewById(R.id.inquirie_discription)
        val contact: TextView =itemView.findViewById(R.id.inquirie_contact_field)
        val image: ImageView =itemView.findViewById(R.id.image_view_inquiries)


    }
    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    // onClickListener Interface
    interface OnClickListener {
        fun onClick(position: Int, model: Product)
    }


}