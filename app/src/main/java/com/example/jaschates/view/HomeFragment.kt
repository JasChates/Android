package com.example.jaschates.view

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jaschates.R
import com.example.jaschates.adapter.RandomChatRecyclerAdapter
import com.example.jaschates.data.ChatRoomModel
import com.example.jaschates.databinding.DialogCreateRandomChattingBinding
import com.example.jaschates.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {
    companion object{
        fun newInstance() : HomeFragment {
            return HomeFragment()
        }
    }

    //이미지 등록
    private val getContent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_create_random_chatting)
            if(result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                if (uri != null) {
//                    dialog.findViewById<ImageView>(R.id.title_image).setImageURI(null)
                    dialog.findViewById<ImageView>(R.id.title_image).setImageURI(uri) //이미지 뷰를 바꿈
                    Log.d("이미지", "성공")
                }
            }
            else Log.d("이미지", "실패")
        }

    private lateinit var binding: FragmentHomeBinding
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    var profileCheck = false

    //뷰가 생성되었을 때
    //프레그먼트와 레이아웃을 연결시켜주는 부분
    @SuppressLint("UseRequireInsteadOfGet")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?)
            : View {
        auth = FirebaseAuth.getInstance()
        database = Firebase.database.reference
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.createRandomChatRoomImage.setOnClickListener {
            activity?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val dialogBinding = DialogCreateRandomChattingBinding.inflate(layoutInflater)
            val dialogBuilder = AlertDialog.Builder(context)
                .setView(dialogBinding.root)

            val dialog = dialogBuilder.show()
            dialogLogic(dialogBinding, dialog)
        }

        return binding.root
    }

    private fun dialogLogic(dialogBinding: DialogCreateRandomChattingBinding, dialog: Dialog) {
        dialogBinding.titleImage.setOnClickListener {
            val intentImage = Intent(Intent.ACTION_PICK)
            intentImage.type = MediaStore.Images.Media.CONTENT_TYPE

            getContent.launch(intentImage)
            profileCheck = true
        }
        dialogBinding.createChattingRoom.setOnClickListener {
            // 데이터베이스 삽입 로직
            val chatRoomModel = ChatRoomModel()

            chatRoomModel.user["host"] = auth.uid.toString()
            chatRoomModel.user["member"] = ""
            database.child("randomChat").child(auth.uid.toString()).setValue(chatRoomModel).addOnSuccessListener {
                // recycler view 생성
                createRecyclerView()

//                    database.child("randomChat").child(auth.uid.toString()).child("comment").push()
                dialog.dismiss()
                val intent = Intent(context, RandomChatActivity::class.java)
                startActivity(intent)
            }
        }
        createRecyclerView()

        dialogBinding.cancelCreate.setOnClickListener {
            dialog.dismiss()
        }
    }

    private fun createRecyclerView() {
        database.child("randomChat").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatRoomModel : ArrayList<ChatRoomModel> = arrayListOf()
                for (snapshot in snapshot.children){
                    val item = snapshot.getValue(ChatRoomModel::class.java)
                    chatRoomModel.add(item!!)
                }

                val adapter = RandomChatRecyclerAdapter(chatRoomModel, requireContext())
                binding.homeRecycler.adapter = adapter
                binding.homeRecycler.layoutManager = LinearLayoutManager(context)
            }

            override fun onCancelled(error: DatabaseError) {}

        })
    }

    private fun setImage(dialogBinding: DialogCreateRandomChattingBinding): ActivityResultLauncher<Intent> {
        //이미지 등록
        var imageUri: Uri?
        val getContent =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                if(result.resultCode == RESULT_OK) {
                    imageUri = result.data?.data //이미지 경로 원본
                    dialogBinding.titleImage.setImageURI(imageUri)  //이미지 뷰를 바꿈
                    Log.d("이미지", "성공")
                }
                else{
                    Log.d("이미지", "실패")
                }
            }

        return getContent
    }
}
