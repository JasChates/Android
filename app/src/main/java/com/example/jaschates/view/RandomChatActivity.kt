package com.example.jaschates.view

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jaschates.R
import com.example.jaschates.data.ChatModel
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
import kotlin.collections.ArrayList

class RandomChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRandomChatBinding
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid: String? = null
    private var destinationUid: String? = null
    private var uid: String? = null
    private var recyclerView: RecyclerView? = null

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRandomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sendImage = binding.send
        val chatEditText = binding.chat
        val chatRoom = intent.getSerializableExtra("chatRoom") as ChatRoomModel

        binding.chatRoomName.text = chatRoom.title

        //메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

        destinationUid = intent.getStringExtra("destinationUid")
        uid = Firebase.auth.currentUser?.uid.toString()
        recyclerView = binding.messageActivityRecyclerview

        sendImage.setOnClickListener {
            val chatModel = ChatModel()
            chatModel.users[uid.toString()] = true
            chatModel.users[destinationUid!!] = true

            val comment = ChatModel.Comment(uid, chatEditText.text.toString(), curTime)
            if (chat.text.isNotEmpty()) {
                if (chatRoomUid == null) {
                    sendImage.isEnabled = false
                    fireDatabase.child("chatrooms").push().setValue(chatModel)
                        .addOnSuccessListener {
                            //채팅방 생성
                            checkChatRoom()
                            //메세지 보내기
                            Handler().postDelayed({
                                fireDatabase.child("chatrooms").child(chatRoomUid.toString())
                                    .child("comments").push().setValue(comment)
                                chat.text = null
                            }, 1000L)
                            Log.d("chatUidNull dest", "$destinationUid")
                        }
                } else {
                    fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments")
                        .push().setValue(comment)
                    chat.text = null
                    Log.d("chatUidNotNull dest", "$destinationUid")
                }
            } else Log.d("TAG", "onCreate: messageActivity_editText length is 0")
        }
        checkChatRoom()

        binding.outImage.setOnClickListener {
            if (chatRoom.user["host"] == uid) { // 나가는 사람이 호스트 일때
                val reference = FirebaseDatabase.getInstance().reference.child("randomChat").child(uid!!)
                reference.removeValue().addOnSuccessListener {
                    Toast.makeText(this, "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            } else {
                val reference = FirebaseDatabase.getInstance().reference.child("randomChat").child(chatRoom.user["host"].toString())
                    .child("user").child("member")
                reference.setValue("").addOnSuccessListener {
                    Toast.makeText(this, "채팅방을 나갔습니다.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
        binding.callImage.setOnClickListener {
            val channelNumber = (1000..1000000).random().toString()

            showJoinDialog(channelNumber)
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
        fireDatabase.child("chatrooms").orderByChild("users/$uid").equalTo(true)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (item in snapshot.children) {
                        println(item)
                        val chatModel = item.getValue<ChatModel>()
                        if (chatModel?.users!!.containsKey(destinationUid)) {
                            chatRoomUid = item.key
                            send.isEnabled = true
                            recyclerView?.layoutManager =
                                LinearLayoutManager(this@RandomChatActivity)
                            recyclerView?.adapter = RecyclerViewAdapter()
                        }
                    }
                }
            })
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.MessageViewHolder>() {

        private val comments = ArrayList<ChatModel.Comment>()
        private var friend: Friend? = null

        init {
            fireDatabase.child("users").child(destinationUid.toString())
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        friend = snapshot.getValue<Friend>()
                        chat_room_name.text = friend?.name
                        getMessageList()
                    }
                })
        }

        fun getMessageList() {
            fireDatabase.child("chatrooms").child(chatRoomUid.toString()).child("comments")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        comments.clear()
                        for (data in snapshot.children) {
                            val item = data.getValue<ChatModel.Comment>()
                            comments.add(item!!)
                            println(comments)
                        }
                        notifyDataSetChanged()
                        //메세지를 보낼 시 화면을 맨 밑으로 내림
                        recyclerView?.scrollToPosition(comments.size - 1)
                    }
                })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
            val view: View =
                LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)

            return MessageViewHolder(view)
        }

        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = comments[position].message
            holder.textView_time.text = comments[position].time
            if (comments[position].uid.equals(uid)) { // 본인 채팅
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

        inner class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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