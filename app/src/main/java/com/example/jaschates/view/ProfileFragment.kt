package com.example.jaschates.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.jaschates.data.Friend
import com.example.jaschates.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_create_chat_room.*
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    companion object {
        private var imageUri: Uri? = null
        private val fireStorage = FirebaseStorage.getInstance().reference
        private val fireDatabase = FirebaseDatabase.getInstance().reference
        private val user = Firebase.auth.currentUser
        private val uid = user?.uid.toString()
        fun newInstance(): ProfileFragment {
            return ProfileFragment()
        }
    }

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == AppCompatActivity.RESULT_OK) {
                imageUri = result.data?.data //이미지 경로 원본
                profile_imageview.setImageURI(imageUri) //이미지 뷰를 바꿈

                //기존 사진을 삭제 후 새로운 사진을 등록
                fireStorage.child("userImages/$uid/photo").delete().addOnSuccessListener {
                    fireStorage.child("userImages/$uid/photo").putFile(imageUri!!)
                        .addOnSuccessListener {
                            fireStorage.child("userImages/$uid/photo").downloadUrl.addOnSuccessListener {
                                val photoUri: Uri = it
                                println("$photoUri")
                                fireDatabase.child("users/$uid/profileImageUrl")
                                    .setValue(photoUri.toString())
                                Toast.makeText(requireContext(),
                                    "프로필사진이 변경되었습니다.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                }
                Log.d("이미지", "성공")
            } else {
                Log.d("이미지", "실패")
            }
        }

    private lateinit var binding: FragmentProfileBinding


    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        //view 선언을 안하고 return에 바로 적용시키면 glide가 작동을 안함
        binding = FragmentProfileBinding.inflate(layoutInflater)

        //프로필 구현
        fireDatabase.child("users").child(uid).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                val userProfile = snapshot.getValue<Friend>()
                println(userProfile)
                Glide.with(requireContext()).load(userProfile?.profileImageUrl)
                    .apply(RequestOptions().circleCrop())
                    .into(binding.profileImageview)
                binding.profileTextviewEmail.text = userProfile?.email
                binding.profileTextviewName.setText("${userProfile?.name}")
            }
        })
        //프로필사진 바꾸기
        binding.profileImageview.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)

            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
            getContent.launch(intentImage)
        }

        binding.profileButton.setOnClickListener {
            if (binding.profileTextviewName.text!!.isNotEmpty()) {
                fireDatabase.child("users/$uid/name").setValue(binding.profileTextviewName.text.toString())
                binding.profileTextviewName.clearFocus()
                Toast.makeText(requireContext(), "이름이 변경되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
        // 로그아웃
        binding.logoutButton.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, LoginActivity::class.java)
            startActivity(intent)
        }
        return binding.root
    }
}
