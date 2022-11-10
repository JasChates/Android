package com.example.jaschates.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jaschates.R
import com.example.jaschates.adapter.RecyclerViewAdapter
import com.example.jaschates.data.UserDTO
import com.example.jaschates.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserListBinding
    var array: MutableList<UserDTO> = arrayListOf()
    private var uids: MutableList<String> = arrayListOf()
    private val fireStore = FirebaseFirestore.getInstance()
    private val myUid = FirebaseAuth.getInstance().currentUser?.uid

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_list)

        val adapter = RecyclerViewAdapter(array, this, uids)
        fireStore.collection("users").get()
            .addOnSuccessListener { result ->
                array.clear()
                uids.clear()
                for (task in result) {
                    val item = task.toObject(UserDTO::class.java)
                    if (myUid != task.id) {
                        array.add(item)
                        uids.add(task.id)
                    }
                }

                adapter.notifyDataSetChanged()
            }

        Log.d("TAG", "onCreate: $array")
        binding.peopleListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.peopleListRecyclerview.adapter = adapter
        watchingMyUidVideoRequest()
    }

    private fun watchingMyUidVideoRequest() {
        val myUid = FirebaseAuth.getInstance().uid
        FirebaseFirestore.getInstance().collection("users").document(myUid!!)
            .addSnapshotListener { value, error ->
                var userDTO = value?.toObject(UserDTO::class.java)
                if (userDTO?.channel != null) {
                    showJoinDialog(userDTO.channel!!)
                }
            }
    }

    private fun showJoinDialog(channel: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("${channel} 방에 참여하시겠습니까?")
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            startActivity(Intent(this, VideoCallActivity::class.java)
                .putExtra("channelId", channel))
            removeChannelStr()
        }
        builder.setNegativeButton("No") { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }

    private fun removeChannelStr() {
        val map: MutableMap<String, Any> = mutableMapOf<String, Any>()
        map["channel"] = FieldValue.delete()
        FirebaseFirestore.getInstance().collection("users").document(myUid!!).update(map)
    }

    fun createVideoChatRoom(position: Int, channel: String, uidList: MutableList<String>) {
        Log.d("TAG", "createVideoChatRoom: $position")
        Log.d("TAG", "createVideoChatRoom: $uidList")

        val tsDoc = fireStore.collection("users").document(uidList[position])
        fireStore.runTransaction { transition ->
            val userDTO = transition.get(tsDoc).toObject(UserDTO::class.java)

            userDTO?.channel = channel

            transition.set(tsDoc, userDTO!!)
        }
    }
}