package com.example.jaschates

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.example.jaschates.databinding.ActivityVideoCallBinding
import com.remotemonster.sdk.RemonCall

class VideoCallActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoCallBinding
    var remonCall: RemonCall? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video_call)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        remonCall = RemonCall.builder()
            .context(this)
            .serviceId("SERVICED1")
            .key("1234567890")
            .videoCodec("VP8")
            .videoWidth(640)
            .videoHeight(400)
            .localView(binding.localView)
            .remoteView(binding.remoteView)
            .build()
        val channelId = intent.getStringExtra("channelId")
        remonCall?.connect(channelId)
        remonCall?.onClose {
            // 상대방이 화상통화를 종료할 경우
            finish()
        }
    }

    override fun onDestroy() {
        remonCall?.close()
        super.onDestroy()
    }

}