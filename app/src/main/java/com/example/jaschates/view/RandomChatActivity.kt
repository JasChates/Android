package com.example.jaschates.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jaschates.R
import com.example.jaschates.data.ChatRoomModel
import com.example.jaschates.data.Friend
import com.example.jaschates.databinding.ActivityRandomChatBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_message.*
import java.text.SimpleDateFormat
import java.util.*

class RandomChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRandomChatBinding
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid: String? = null
    private lateinit var destinationUid: String
    private lateinit var uid: String
    private lateinit var hostUid: String
    private lateinit var recyclerView: RecyclerView

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRandomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sendImage = binding.sendImage
        val chatEditText = binding.randomChat
        val chatRoomModel = intent.getSerializableExtra("chatRoom") as ChatRoomModel

        binding.randomChatRoomName.text = chatRoomModel.title

        //메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        hostUid = intent.getStringExtra("hostUid").toString()
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = binding.randomChatActivityRecyclerview
        destinationUid = if (chatRoomModel.user["host"] == uid) uid
        else chatRoomModel.user["member"].toString()

        sendImage.setOnClickListener {
            val comment = ChatRoomModel.Comment(uid, chatEditText.text.toString(), curTime)
            if (chatEditText.text.isNotEmpty()) {
                if (chatRoomUid == null) {
                    sendImage.isEnabled = false

                    checkChatRoom()
                    Handler().postDelayed({
                        fireDatabase.child("randomChat").child(hostUid)
                            .child("comment").push().setValue(comment)
                        chatEditText.text = null
                    }, 1000L)
                } else {
                    fireDatabase.child("randomChat").child(hostUid)
                        .child("comment").push().setValue(comment)
                    chatEditText.text = null
                }
            } else Log.d("TAG", "onCreate: text length is 0")
        }
        checkChatRoom()

        binding.outImage.setOnClickListener {
            if (chatRoomModel.user["host"] == uid) { // 나가는 사람이 호스트 일때
                val reference = FirebaseDatabase.getInstance().reference.child("randomChat").child(uid!!)
                reference.removeValue().addOnSuccessListener {
                    Toast.makeText(this, "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                val reference = FirebaseDatabase.getInstance().reference.child("randomChat").child(chatRoomModel.user["host"].toString())
                    .child("user").child("member")
                reference.setValue("").addOnSuccessListener {
                    Toast.makeText(this, "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        binding.callImage.setOnClickListener {
            val channelNumber = (1000..1000000).random().toString()
            showJoinDialog("channelNumber")
        }
    }

    fun showJoinDialog(channel: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("${channel} 방에 참여하시겠습니까?")
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            startActivity(Intent(this, VideoCallActivity::class.java)
                .putExtra("channelId", channel))
        }
        builder.setNegativeButton("No") { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }

    private fun checkChatRoom() {
        fireDatabase.child("randomChat")
            .addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        val model = item.getValue(ChatRoomModel::class.java)
                        if (model?.user?.get("host")==hostUid) {
                            chatRoomUid = item.key.toString()
                            binding.sendImage.isEnabled = true
                            recyclerView.layoutManager = LinearLayoutManager(this@RandomChatActivity)
                            recyclerView.adapter = RandomChatAdapter()
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    inner class RandomChatAdapter : RecyclerView.Adapter<RandomChatAdapter.CommentViewHolder>() {
        private val comments = ArrayList<ChatRoomModel.Comment>()
        private var friend: Friend? = null  // 유저 정보를 불러오기 위함

        init {
            fireDatabase.child("users").child(destinationUid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}

                    override fun onDataChange(snapshot: DataSnapshot) {
                        friend = snapshot.getValue<Friend>()
                        getMessageList()
                    }
                })
        }

        private fun getMessageList() {
            fireDatabase.child("randomChat").child(hostUid).child("comment")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}

                    override fun onDataChange(snapshot: DataSnapshot) {
                        comments.clear()
                        for (data in snapshot.children) {
                            Log.d("TAG", "onDataChange recyclerview: $data")
                            val item = data.getValue<ChatRoomModel.Comment>()
                            comments.add(item!!)
                        }
                        notifyDataSetChanged()
                        //메세지를 보낼 시 화면을 맨 밑으로 내림
                        recyclerView.scrollToPosition(comments.size - 1)
                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
            return CommentViewHolder(view)
        }

        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if (comments[position].uid.equals(destinationUid)) { // 본인 채팅
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            } else { // 상대방 채팅
                Glide.with(holder.itemView.context)
                    .load(friend?.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(holder.imageView_profile)
                holder.textView_name.text = friend?.name
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT
            }
        }

        inner class CommentViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val textView_message: TextView = view.findViewById(R.id.messageItem_textView_message)
            val textView_name: TextView = view.findViewById(R.id.messageItem_textview_name)
            val imageView_profile: ImageView = view.findViewById(R.id.messageItem_imageview_profile)
            val layout_destination: LinearLayout =
                view.findViewById(R.id.messageItem_layout_destination)
            val layout_main: LinearLayout = view.findViewById(R.id.messageItem_linearlayout_main)
            val textView_time: TextView = view.findViewById(R.id.messageItem_textView_time)
        }

        override fun getItemCount(): Int {
            return comments.size
        }

    }
}