package com.example.jaschates.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import android.provider.MediaStore
import android.widget.Toast
import com.example.jaschates.R
import com.example.jaschates.data.Profile
import com.example.jaschates.data.UserDTO
import com.example.jaschates.databinding.ActivityProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage

private lateinit var auth: FirebaseAuth
lateinit var database: DatabaseReference

class ProfileActivity : AppCompatActivity() {
    private var imageUri: Uri? = null

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                imageUri = result.data?.data
                binding.registrationProfile.setImageURI(imageUri)
                Log.d("이미지", "성공")
            } else {
                Log.d("이미지", "실패")
            }
        }

    lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        auth = Firebase.auth
        database = Firebase.database.reference

        val name = binding.userName.text
        val profile = binding.registrationProfile
        val button = binding.profileRegisterBtn
        var profileCheck = false

        profile.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE
            getContent.launch(intentImage)
            profileCheck = true
        }

        val intent = Intent(this, UserListActivity::class.java)

        button.setOnClickListener {
            if (name.isEmpty() && profileCheck) {
                Toast.makeText(this, "이름과 프로필 사진을 제대로 입력해주세요.", Toast.LENGTH_SHORT).show()
                Log.d("Name", "$name")
            } else {
                if (!profileCheck) {
                    Toast.makeText(this, "프로필사진을 등록해주세요.", Toast.LENGTH_SHORT).show()
                } else if (profileCheck) {
                    FirebaseStorage.getInstance()
                        .reference.child("userImages").child("photo").putFile(imageUri!!)
                        .addOnSuccessListener {
                            var userProfile: Uri? = null
                            FirebaseStorage.getInstance().reference.child("userImages")
                                .child("photo").downloadUrl
                                .addOnSuccessListener {
                                    userProfile = it
                                    Log.d("이미지 URL", "$userProfile")
                                    val profile = Profile(name.toString(), userProfile.toString())
                                    database.child("users").setValue(profile)

                                }
                        }
                    Toast.makeText(this, "프로필 등록이 완료되었습니다.", Toast.LENGTH_SHORT).show()
                    startActivity(intent)
                }
                else{
                    Toast.makeText(this, "등록에 실패했습니다.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}