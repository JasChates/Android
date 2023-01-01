package com.example.jaschates.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.example.jaschates.R
import com.example.jaschates.adapter.RandomChatRecyclerAdapter
import com.example.jaschates.data.ChatRoomModel
import com.example.jaschates.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.createRandomChatRoomImage.setOnClickListener {
            startActivity(Intent(context, CreateChatRoomActivity::class.java))
        }
        createRecyclerView()
        return binding.root
    }

    private fun createRecyclerView() {
        database.child("randomChat").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomModel: ArrayList<ChatRoomModel> = arrayListOf()
                for (dataSnapshot in snapshot.children) {
                    val item = dataSnapshot.getValue(ChatRoomModel::class.java)
                    if (item!!.user["host"].toString().isNotEmpty() && item.user["member"].toString().isNotEmpty()) {
                        continue
                    }
                    chatRoomModel.add(item)
                }

                val adapter = RandomChatRecyclerAdapter(chatRoomModel, context!!)
                binding.homeRecycler.adapter = adapter
//                binding.homeRecycler.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                binding.homeRecycler.orientation = ViewPager2.ORIENTATION_HORIZONTAL

                adapter.setItemClickListener(object: RandomChatRecyclerAdapter.OnItemClickListener{
                    override fun onClick(v: View, position: Int) {
                        // 채팅방 입장
                        val chatRoom = chatRoomModel[position]
                        val reference = FirebaseDatabase.getInstance().getReference("randomChat").child(chatRoom.user["host"].toString())
                            .child("user").child("member")

                        reference.setValue(auth.currentUser?.uid.toString()).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "채팅방에 입장했습니다.", Toast.LENGTH_SHORT).show()

                                val intent = Intent(context, RandomChatActivity::class.java)
                                intent.putExtra("chatRoom", chatRoomModel[position])
                                intent.putExtra("uid", auth.currentUser?.uid)
                                startActivity(intent)
                            }
                        }
                    }
                })
            }


            override fun onCancelled(error: DatabaseError) {}


        })
    }
}
