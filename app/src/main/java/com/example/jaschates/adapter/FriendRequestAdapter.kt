package com.example.jaschates.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jaschates.data.User
import com.example.jaschates.databinding.ItemFriendRequestBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FriendRequestAdapter(private val auth: String, private val context: Context): RecyclerView.Adapter<FriendRequestAdapter.ViewHolder>() {

    private val list = arrayListOf<User>()
    private val firebaseDatabase = FirebaseDatabase.getInstance().reference
    init {
        firebaseDatabase.child("friend").child(auth)
            .addListenerForSingleValueEvent(object :ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (i in snapshot.children) {
                        val data = i.value as HashMap<String, Boolean>
                        getUserInfo(data)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun getUserInfo(hashMap: HashMap<String, Boolean>) {
        firebaseDatabase.child("users").child(hashMap.keys.elementAt(0))
            .addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val data = snapshot.getValue(User::class.java)
                    list.add(data!!)

                    notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemFriendRequestBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(private val binding: ItemFriendRequestBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            binding.friendRequestEmail.text = user.email
            binding.friendRequestName.text = user.name
            Glide.with(context)
                .load(user.profileImageUrl)
                .apply(RequestOptions().circleCrop())
                .into(binding.friendRequestProfile)

            binding.requestAcceptButton.setOnClickListener {
                firebaseDatabase.child("friendList").child(auth).child(user.uid.toString()).child(user.uid.toString()).setValue(true)
                    .addOnSuccessListener {
                        // 상대방에게도 적용
                        firebaseDatabase.child("friendList").child(user.uid.toString()).child(auth).child(auth).setValue(true)
                        // 데이터 지우기
                        firebaseDatabase.child("friend").child(auth).child(user.uid.toString()).removeValue()
                        Toast.makeText(context, "친구요청을 수락했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }

            binding.requestCancelButton.setOnClickListener {
                firebaseDatabase.child("friend").child(auth).child(user.uid.toString()).removeValue()
                    .addOnSuccessListener {
                        Toast.makeText(context, "친구요청을 거절했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}