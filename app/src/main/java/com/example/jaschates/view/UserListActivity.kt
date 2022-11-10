package com.example.jaschates

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jaschates.databinding.ActivityUserListBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase

class UserListActivity : AppCompatActivity() {
    lateinit var binding: ActivityUserListBinding
    var array: MutableList<UserDTO> = arrayListOf()
    var uids: MutableList<String> = arrayListOf()
    val myUid = FirebaseAuth.getInstance().uid
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
        binding.peopleListRecyclerview.adapter = RecyclerViewAdapter(array)
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
        var map: MutableMap<String, Any> = mutableMapOf<String, Any>()
        map["channel"] = FieldValue.delete()
        FirebaseFirestore.getInstance().collection("users").document(myUid!!).update(map)
    }

    fun openVideoActivity(channelId: String) {
        val i = Intent(UserListActivity(), VideoCallActivity::class.java)
        i.putExtra("channelId", channelId)
        startActivity(i)
    }

    fun createVideoChatRoom(position: Int, channel: String) {
        var map: MutableMap<String, Any> = mutableMapOf<String, Any>()
        map["channel"] = channel
        FirebaseFirestore.getInstance().collection("users").document(uids[position]).update(map)
    }
}