package com.masonk.news

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.masonk.news.databinding.ActivityMainBinding
import com.tickaroo.tikxml.TikXml
import com.tickaroo.tikxml.retrofit.TikXmlConverterFactory
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding

    // 리사이클러뷰 어댑터
    private lateinit var newsAdapter: NewsAdapter

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://news.google.com/")
        .addConverterFactory(
            TikXmlConverterFactory.create(
                // custom XML Converter
                TikXml.Builder()
                    .exceptionOnUnreadXml(false)
                    .build()
            )
        )
        .build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 리사이클러뷰 어댑터 생성
        newsAdapter = NewsAdapter()

        // 리사이클러뷰 설정
        binding.newsRecyclerView.apply {
            // 레이아웃 매니저
            layoutManager = LinearLayoutManager(this@MainActivity)

            // 어댑터
            adapter = newsAdapter
        }

        // NewsService 인터페이스 구현체
        val newsService = retrofit.create(NewsService::class.java)

        // 메인 피드 정보 가져오기
        newsService.mainFeed().enqueue(object : Callback<Rss> {
            override fun onResponse(p0: Call<Rss>, p1: Response<Rss>) {
                Log.d("MainActivity", "${p1.body()?.channel?.newsItemList}")

                // List<NewsItem>
                val newsItemList = p1.body()?.channel?.newsItemList.orEmpty()

                // List<News>
                val newsList = newsItemList.transform()

                // 리사이클러뷰에 반영/업데이트
                newsAdapter.submitList(newsList)

                // 뉴스리스트 순회
                newsList.forEachIndexed { index, news ->
                    // 네트워크 관련 작업은 별도의 스레드에서 실행
                    Thread {
                        try {
                            // Jsoup을 사용하여 news.link에 연결하고 HTML 문서를 가져옴
                            val document = Jsoup.connect(news.link).get()

                            // property 속성이 og:로 시작하는 모든 meta태그를 선택
                            val elements = document.select("meta[property^=og:]")

                            // property 속성이 og:image인 노드를 찾음
                            val ogImageElement = elements.find { node ->
                                node.attr("property") == "og:image"
                            }

                            // content 속성 값을 가져옴
                            val imageUrl = ogImageElement?.attr("content")

                            // news 객체의 imageUrl 속성에 imageUrl 값을 설정
                            news.imageUrl = imageUrl
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        // UI 스레드에서 실행
                        // If the current thread is not the UI thread, the action is posted to the event queue of the UI thread.
                        runOnUiThread {
                            // newsAdapter에 특정 위치의 아이템이 변경되었음을 알림
                            // 해당 위치의 아이템을 다시 그리도록 요청
                            newsAdapter.notifyItemChanged(index)
                        }
                    }.start()
                }


            }

            override fun onFailure(p0: Call<Rss>, p1: Throwable) {
                p1.printStackTrace()
            }

        })
    }
}