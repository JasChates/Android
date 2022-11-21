package com.example.jaschates.view

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.jaschates.R
import com.example.jaschates.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

private lateinit var auth: FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        auth = Firebase.auth

        val email = binding.etLoginId
        val password = binding.etLoginPassword

        binding.profileButton.setOnClickListener {
            if (email.text.isEmpty() && password.text.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 제대로 입력해주세요.", Toast.LENGTH_SHORT).show()
                Log.d("Email", "$email, $password")
                email.setText("")
                password.setText("")
            } else {
                autoLogin()
                signIn(email.text.toString(), password.text.toString())
            }
        }

        //회원가입창 인텐트
        binding.btnRegistration.setOnClickListener {
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun autoLogin() {
        // 자동로그인 여부를 정한다.
        val uid: SharedPreferences? = getSharedPreferences("uid", MODE_PRIVATE)
        val able: SharedPreferences? = getSharedPreferences("able", MODE_PRIVATE)
        // shared preferences edit
        val uidEdit = uid?.edit()
        val ableEdit = able?.edit()

        if (binding.loginAbleCheck.isChecked) {
            uidEdit?.putString("uid", auth.uid)?.apply()
            ableEdit?.putBoolean("able", true)?.apply()
        } else {
            uidEdit?.clear()?.apply()
            ableEdit?.clear()?.apply()
        }
    }

    private fun signIn(email: String, password: String) {
        // [START sign_in_with_email]
        val intentList = Intent(this, MainActivity::class.java)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("로그인", "성공")
                    val user = auth.currentUser
                    updateUI(user)
                    finish()
                    startActivity(intentList)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "정확한 아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    Log.d("로그인", "실패")
                    updateUI(null)
                }
            }
        // [END sign_in_with_email]
    }

    private fun updateUI(user: FirebaseUser?) {

    }

    public override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        val uid: SharedPreferences? = getSharedPreferences("uid", MODE_PRIVATE)
        val able: SharedPreferences? = getSharedPreferences("able", MODE_PRIVATE)

        val getUid = uid?.getString("uid", null)
        val getAble = able?.getBoolean("able", false)
        if (getUid != null && getAble != false)
            moveMainPage(currentUser)
        else {
            uid?.edit()?.clear()?.apply()
            able?.edit()?.clear()?.apply()
        }
        if (currentUser != null) {
            reload();
        }
        // SharedPreferences 사용
        // 체크박스가 체크됨 -> true
        // 안됨 -> false
        // 로그아웃을 함 -> false
    }

    private fun moveMainPage(user: FirebaseUser?){
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun reload() {

    }
}