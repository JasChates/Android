package com.example.jaschates.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.example.jaschates.R
import com.example.jaschates.databinding.ActivityVideoCallBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.remotemonster.sdk.RemonCall
import com.remotemonster.sdk.RemonCast

class VideoCallActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoCallBinding
    var remonCall: RemonCall? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_call)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)

        remonCall = RemonCall.builder()
            .context(this)
            .serviceId("SERVICEID1")
            .key("1234567890")
            .videoCodec("VP8")
            .videoWidth(640)
            .videoHeight(480)
            .localView(binding.localView)
            .remoteView(binding.remoteView)
            .build()
        val channelId = intent.getStringExtra("channelId")
        remonCall?.connect(channelId)
//        remonCall?.onClose {
//            // 상대방이 화상통화를 종료할 경우
//            finish()
//        }
    }

    override fun onDestroy() {
        remonCall?.close()
        super.onDestroy()
    }

}