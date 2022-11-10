package com.example.jaschates

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jaschates.databinding.ActivityUserListBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class UserListActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserListBinding
    var array: MutableList<UserDTO> = arrayListOf()
    var uids: MutableList<String> = arrayListOf()
    val myUid = FirebaseAuth.getInstance().uid

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_list)
        FirebaseFirestore.getInstance().collection("users").get().addOnCompleteListener { task ->
            array.clear()
            uids.clear()
            for (item in task.result!!.documents) {
                if (myUid != item.id) {
                    array.add(item.toObject(UserDTO::class.java)!!)
                    uids.add(item.id)
                }
            }
            binding.peopleListRecyclerview.adapter?.notifyDataSetChanged()
        }
        binding.peopleListRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.peopleListRecyclerview.adapter = RecyclerviewAdapter(array)
        watchingMyUidVideoRequest()
    }

    fun watchingMyUidVideoRequest() {
        val myUid = FirebaseAuth.getInstance().uid
        FirebaseFirestore.getInstance().collection("users").document(myUid!!)
            .addSnapshotListener { value, error ->
                var userDTO = value?.toObject(UserDTO::class.java)
                if (userDTO?.channel != null) {
                    showJoinDialog(userDTO.channel!!)
                }
            }
    }

    fun showJoinDialog(channel: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("${channel} 방에 참여하시겠습니까?")
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            openVideoActivity(channel)
            removeChannelStr()
        }
        builder.setNegativeButton("No") { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }

    fun removeChannelStr() {
        var map: MutableMap<String, Any> = mutableMapOf()
        map["channel"] = FieldValue.delete()
        FirebaseFirestore.getInstance().collection("users").document(myUid!!).update(map)
    }

    fun openVideoActivity(channelId: String) {
        val i = Intent(this, VideoCallActivity::class.java)
        i.putExtra("channelId", channelId)
        try {
            startActivity(i)
        } catch (e: Exception) {
            Log.e("TAG", "openVideoActivity: ${e.printStackTrace()} ", e.cause )
        }
    }

    fun createVideoChatRoom(position: Int, channel: String) {
        var map: MutableMap<String, Any> = mutableMapOf()
        map["channel"] = channel
        if (uids.size != 0){
            FirebaseFirestore.getInstance().collection("users").document(uids[position]).update(map)
        }
    }
}