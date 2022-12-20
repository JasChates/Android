package com.example.jaschates.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.jaschates.data.ChatRoomModel
import com.example.jaschates.databinding.ItemChatBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class RandomChatRecyclerAdapter(private val list: List<ChatRoomModel>, private val context: Context): RecyclerView.Adapter<RandomChatRecyclerAdapter.ViewHolder>() {
    val commentList = ArrayList<ChatRoomModel.Comment>()
    val database = FirebaseDatabase.getInstance().reference
    init {
        database.child("randomChat").addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                for(data in snapshot.children){
                    val item = data.getValue(ChatRoomModel.Comment::class.java)
                    commentList.add(item!!)
                }

                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RandomChatRecyclerAdapter.ViewHolder {
        val binding = ItemChatBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
        holder.itemView.setOnClickListener {
            itemClickListener.onClick(it, position)
        }
    }

    interface OnItemClickListener{
        fun onClick(v: View, position: Int)
    }

    fun setItemClickListener(onItemClickListener: OnItemClickListener){
        this.itemClickListener = onItemClickListener
    }

    private lateinit var itemClickListener : OnItemClickListener

    override fun getItemCount(): Int = list.size



    inner class ViewHolder(private val binding: ItemChatBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ChatRoomModel) {
            binding.chatTextviewTitle.text = item.title
            binding.chatItemTextviewLastmessage.text = item.description
            Glide
                .with(context)
                .load(item.titleImage)
                .into(binding.chatItemImageview)
        }
    }
}

