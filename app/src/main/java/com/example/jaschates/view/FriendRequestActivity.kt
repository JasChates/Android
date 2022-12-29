package com.example.jaschates.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jaschates.R
import com.example.jaschates.adapter.FriendRequestAdapter
import com.example.jaschates.databinding.ActivityFriendRequestBinding
import com.google.firebase.auth.FirebaseAuth

class FriendRequestActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFriendRequestBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFriendRequestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val uid = FirebaseAuth.getInstance().currentUser?.uid.toString()

        binding.friendRequestRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.friendRequestRecyclerView.adapter = FriendRequestAdapter(uid, this)

        binding.exitFriendRequestImage.setOnClickListener { finish() }
    }
}