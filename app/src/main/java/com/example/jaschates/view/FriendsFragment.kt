package com.example.jaschates.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.jaschates.R
import com.example.jaschates.data.User
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class FriendsFragment : Fragment() {
    companion object {
        fun newInstance(): FriendsFragment {
            return FriendsFragment()
        }
    }

    private lateinit var database: DatabaseReference
    private var user: ArrayList<User> = arrayListOf()

    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        database = FirebaseDatabase.getInstance().reference
        val view = inflater.inflate(R.layout.fragment_friends, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.home_recycler)
        //this는 액티비티에서 사용가능, 프래그먼트는 requireContext()로 context 가져오기
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = RecyclerViewAdapter()

        return view
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<RecyclerViewAdapter.CustomViewHolder>() {

        val myUid = Firebase.auth.currentUser?.uid.toString()   // 내 uid
        init {
            /*FirebaseDatabase.getInstance().reference.child("users")
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        user.clear()
                        for (data in snapshot.children) {
                            val item = data.getValue<User>()
                            if (item?.uid.equals(myUid)) {
                                continue
                            } // 본인은 친구창에서 제외
                            user.add(item!!)
                        }
                        notifyDataSetChanged()
                    }
                })*/
            database.child("friendList").child(myUid).addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapShot in snapshot.children) {
                        getUser(dataSnapShot)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        private fun getUser(dataSnapShot: DataSnapshot) {
            database.child("users").child(dataSnapShot.key.toString()).addListenerForSingleValueEvent(object :ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val item = snapshot.getValue(User::class.java)
                    Log.d("TAG", "onDataChange item: ${snapshot.value}")
                    if (item?.uid.equals(myUid)) {
                        return
                    }
                    user.add(item!!)
                    notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
            return CustomViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.item_friends, parent, false))
        }

        inner class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.home_item_iv)
            val textView: TextView = itemView.findViewById(R.id.home_item_tv)
            val textViewEmail: TextView = itemView.findViewById(R.id.home_item_email)
        }

        override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
            Glide.with(holder.itemView.context).load(user[position].profileImageUrl)
                .apply(RequestOptions().circleCrop())
                .into(holder.imageView)
            holder.textView.text = user[position].name
            holder.textViewEmail.text = user[position].email

            holder.itemView.setOnClickListener {
                val intent = Intent(context, MessageActivity::class.java)
                intent.putExtra("destinationUid", user[position].uid)
                context?.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return user.size
        }
    }
}
