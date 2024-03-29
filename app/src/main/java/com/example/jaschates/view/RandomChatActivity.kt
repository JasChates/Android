package com.example.jaschates.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
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
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jaschates.R
import com.example.jaschates.data.ChatRoomModel
import com.example.jaschates.data.User
import com.example.jaschates.databinding.ActivityRandomChatBinding
import com.example.jaschates.filter.BadWordFiltering
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.activity_message.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class RandomChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRandomChatBinding
    private val fireDatabase = FirebaseDatabase.getInstance().reference
    private var chatRoomUid: String? = null
    private lateinit var uid: String
    private lateinit var hostUid: String
    private lateinit var memberUid: String
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatRoomModel: ChatRoomModel
    private val badWordFiltering = BadWordFiltering()

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRandomChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val sendImage = binding.sendImage
        val chatEditText = binding.randomChat
        chatRoomModel = intent.getSerializableExtra("chatRoom") as ChatRoomModel

        binding.randomChatRoomName.text = chatRoomModel.title
        hostUid = chatRoomModel.user["host"].toString()
        uid = Firebase.auth.currentUser?.uid.toString()
        memberUid = binding.memberId.text.toString()
        recyclerView = binding.randomChatActivityRecyclerview

        //메세지를 보낸 시간
        val time = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("MM월dd일 hh:mm")
        val curTime = dateFormat.format(Date(time)).toString()

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
            outLogic()
        }
        binding.callImage.setOnClickListener {
            val channelNumber = (1000..1000000).random().toString()
            createChatRoomID(channelNumber, chatRoomModel)
        }

        watchingMyUidVideoRequest()
    }

    private fun outLogic() {
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

    fun showJoinDialog(channel: String) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("${channel} 방에 참여하시겠습니까?")
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            startActivity(Intent(this, VideoCallActivity::class.java)
                .putExtra("channelId", channel)
                .putExtra("hostId", hostUid))
        }
        builder.setNegativeButton("No") { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }

    private fun createChatRoomID(channel: String, chatRoomModel: ChatRoomModel) {
        fireDatabase.child("randomChat").child(chatRoomModel.user["host"].toString()).child("channelID").setValue(channel)
    }

    private fun watchingMyUidVideoRequest() {
        fireDatabase.child("randomChat").child(hostUid).addValueEventListener(object :ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomModel = snapshot.getValue(ChatRoomModel::class.java)
                if (chatRoomModel?.channelID != null) {
                    showJoinDialog(chatRoomModel.channelID!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
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

                            // adapter 적용
                            if (uid == hostUid) {
                                fireDatabase.child("randomChat").child(hostUid).child("user")
                                    .addChildEventListener(object :ChildEventListener {
                                        override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                                            Log.d("TAG", "onChildAdded: adapter")
                                        }

                                        override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                                            val memberUid = snapshot.value as String
                                            recyclerView.layoutManager = LinearLayoutManager(this@RandomChatActivity)
                                            recyclerView.adapter = RandomChatAdapter(memberUid)
                                        }

                                        override fun onChildRemoved(snapshot: DataSnapshot) {
                                            Log.d("TAG", "onChildRemoved: adapter")
                                        }

                                        override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                                            Log.d("TAG", "onChildMoved: adapter")
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            Log.d("TAG", "onCancelled: adapter")
                                        }
                                    })
                            } else {
                                recyclerView.layoutManager = LinearLayoutManager(this@RandomChatActivity)
                                recyclerView.adapter = RandomChatAdapter(memberUid)
                            }

                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    inner class RandomChatAdapter(memberUid: String) : RecyclerView.Adapter<RandomChatAdapter.CommentViewHolder>() {
        private val comments = ArrayList<ChatRoomModel.Comment>()
        private var member: User? = null  // 유저 정보를 불러오기 위함
        private val destinationUid =
            if (hostUid == uid) memberUid
            else hostUid

        init {
            fireDatabase.child("users").child(destinationUid)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {}

                    override fun onDataChange(snapshot: DataSnapshot) {
                        member = snapshot.getValue<User>()
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

        @RequiresApi(Build.VERSION_CODES.N)
        @SuppressLint("RtlHardcoded")
        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            holder.textView_message.textSize = 20F
            holder.textView_message.text = badWordFiltering.checkAndChange(comments[position].message.toString())
            holder.textView_time.text = comments[position].time
            if (comments[position].uid.equals(uid)) { // 본인 채팅
                holder.textView_message.setBackgroundResource(R.drawable.rightbubble)
                holder.textView_name.visibility = View.INVISIBLE
                holder.layout_destination.visibility = View.INVISIBLE
                holder.layout_main.gravity = Gravity.RIGHT
            } else { // 상대방 채팅
                Glide.with(holder.itemView.context)
                    .load(member?.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(holder.imageView_profile)
                holder.textView_name.text = member?.name
                holder.layout_destination.visibility = View.VISIBLE
                holder.textView_name.visibility = View.VISIBLE
                holder.textView_message.setBackgroundResource(R.drawable.leftbubble)
                holder.layout_main.gravity = Gravity.LEFT

                holder.itemView.setOnClickListener {
                    sendFriendRequest(member)
                }
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

    fun sendFriendRequest(member: User?) {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("${member?.name}님에게 친구요청을 보내시겠습니까?")
        builder.setPositiveButton("Yes") { dialogInterface, i ->
            val friendHash = HashMap<String, Boolean>()
            friendHash[uid] = false
            if (hostUid == uid) {   // 호스트 -> 멤버
                fireDatabase.child("friend").child(member?.uid.toString()).child(uid).setValue(friendHash)
            } else {    // 멤버 -> 호스트
                fireDatabase.child("friend").child(hostUid).child(uid).setValue(friendHash)
            }
            Toast.makeText(this, "친구 요청을 보냈습니다.", Toast.LENGTH_SHORT).show()
            dialogInterface.dismiss()
        }
        builder.setNegativeButton("No") { dialogInterface, i ->
            dialogInterface.dismiss()
        }
        builder.create().show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        outLogic()
    }
}