package com.masonk.news

import android.os.Bundle
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.masonk.news.databinding.ActivityWebViewBinding

class WebViewActivity : AppCompatActivity() {
    private lateinit var binding : ActivityWebViewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent에서 URL 정보 가져오기
        val url = intent.getStringExtra("url")

        // 웹뷰 설정
        binding.webView.apply {
            webViewClient = WebViewClient()
            settings.javaScriptEnabled = true
        }

        if (url.isNullOrEmpty()) {
            Toast.makeText(this, "잘못된 URL 입니다.", Toast.LENGTH_SHORT).show()

            // 액티비티 종료
            finish()
        } else {
            // 웹뷰에 URL 로드
            binding.webView.loadUrl(url)
        }



    }
}