package com.example.jaschates

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.auth.User

class RecyclerViewAdapter(private val array: MutableList<UserDTO>) :
    RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerViewAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_people, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return array.size
    }

    override fun onBindViewHolder(holder: RecyclerViewAdapter.ViewHolder, position: Int) {
        holder.itemEmail.text = array[position].email
        holder.itemView.setOnClickListener {
            val channelNumber = (1000..1000000).random().toString()
            UserListActivity().openVideoActivity("jaschates")
            UserListActivity().createVideoChatRoom(position, channelNumber)
        }
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemEmail = view.findViewById<TextView>(R.id.item_email)
    }
}

