package com.example.jaschates.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.jaschates.R
import com.example.jaschates.data.UserDTO
import com.example.jaschates.view.UserListActivity
import com.example.jaschates.view.VideoCallActivity

class RecyclerViewAdapter(
    private val array: MutableList<UserDTO>,
    private val context: UserListActivity,
    private val uids: MutableList<String>
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_people, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemEmail.text = array[position].email
        holder.onClickListener()
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemEmail = view.findViewById<TextView>(R.id.item_email)

        fun onClickListener() {
            val pos = adapterPosition

            if (pos != RecyclerView.NO_POSITION) {
                itemView.setOnClickListener {
                    val channelNumber = (1000..1000000).random().toString()

                    val intent = Intent(context, VideoCallActivity::class.java)
                    intent.putExtra("channelId", "jaschates")
                    context.startActivity(intent)

                    UserListActivity().createVideoChatRoom(pos, channelNumber, uids)
//                    itemClick?.onItemClick(itemView, pos)
                }
            }
        }
    }
}

