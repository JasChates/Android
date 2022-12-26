package com.example.jaschates.view

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.jaschates.data.ChatRoomModel
import com.example.jaschates.databinding.ActivityCreateChatRoomBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class CreateChatRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCreateChatRoomBinding
    var profileCheck = false
    private val auth = FirebaseAuth.getInstance()
    private lateinit var database: FirebaseDatabase

    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
                    binding.titleImage.setImageURI(uri)
                    // fire store logic
                    uploadFireStore(uri)
                    Log.d("이미지", "성공")
                }
            }
        }

    private fun uploadFireStore(uri: Uri) {
        val fileRef = FirebaseStorage.getInstance().reference.child("chatRoomImage").child(auth.uid.toString())
        fileRef.putFile(uri).addOnSuccessListener { task ->
            if (task.task.isSuccessful) {
                Log.d("이미지 storage", "uploadFireStore: 성공")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = FirebaseDatabase.getInstance()

        binding = ActivityCreateChatRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.titleImage.setOnClickListener {
            galleryLogic()
        }

        binding.createChattingRoom.setOnClickListener {
            val chatRoomModel = ChatRoomModel()
            if (binding.chattingTitleEditText.text.isNotEmpty() && binding.descriptionChattingRoomEditText.text.isNotEmpty()) {
                val ref = FirebaseStorage.getInstance().reference.child("chatRoomImage").child(auth.uid.toString())
                ref.downloadUrl.addOnSuccessListener { uri ->
                    // 데이터 삽입
                    chatRoomModel.title = binding.chattingTitleEditText.text.toString()
                    chatRoomModel.description = binding.descriptionChattingRoomEditText.text.toString()
                    chatRoomModel.user["host"] = auth.uid.toString()
                    chatRoomModel.user["member"] = ""
                    chatRoomModel.titleImage = uri.toString()
                    chatRoomModel.channelID = null

                    database.reference.child("randomChat").child(auth.uid.toString()).setValue(chatRoomModel)
                        .addOnSuccessListener {
                            val intent = Intent(this, RandomChatActivity::class.java)
                            intent.putExtra("chatRoom", chatRoomModel)
                            startActivity(intent)
                            finish()
                        }
                }
            } else Toast.makeText(this, "필수 항목입니다.", Toast.LENGTH_SHORT).show()
        }

        binding.cancelCreate.setOnClickListener { finish() }
    }

    private fun galleryLogic() {
        val intentImage = Intent(Intent.ACTION_PICK)
        intentImage.type = MediaStore.Images.Media.CONTENT_TYPE

        getContent.launch(intentImage)
        profileCheck = true
    }
}