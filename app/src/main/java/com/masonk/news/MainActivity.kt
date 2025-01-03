package com.masonk.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
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
    private lateinit var binding: ActivityMainBinding

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

        // NewsService 인터페이스 구현체
        val newsService = retrofit.create(NewsService::class.java)

        // 리사이클러뷰 어댑터 생성
        newsAdapter = NewsAdapter { url ->
            // onClick(String)

            // WebViewActivity로 이동하기 위한 Intent 생성
            val intent = Intent(this, WebViewActivity::class.java).apply {
                // Intent에 URL 정보를 추가
                putExtra("url", url)
            }
            // WebViewActivity 시작
            startActivity(intent)
        }

        // 리사이클러뷰 설정
        binding.newsRecyclerView.apply {
            // 레이아웃 매니저
            layoutManager = LinearLayoutManager(this@MainActivity)

            // 어댑터
            adapter = newsAdapter
        }

        // 홈 칩
        binding.homeChip.setOnClickListener {
            // 칩 그룹 안에 있는 모든 칩 체크 해제
            binding.chipGroup.clearCheck()

            // 홈 칩 체크
            binding.homeChip.isChecked = true

            // API 호출, 리스트 변경
            newsService.homeNews().submitList()
        }

        // 정치 칩
        binding.politicsChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.politicsChip.isChecked = true

            // API 호출, 리스트 변경
            newsService.politicsNews().submitList()
        }

        // 경제 칩
        binding.economyChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.economyChip.isChecked = true

            newsService.economyNews().submitList()
        }

        // 사회 칩
        binding.societyChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.societyChip.isChecked = true

            newsService.societyNews().submitList()
        }

        // IT 칩
        binding.itChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.itChip.isChecked = true

            newsService.itNews().submitList()
        }

        // 검색
        // EditText에 입력된 텍스트에 대해 특정 액션이 발생했을 때
        binding.searchEditText.setOnEditorActionListener { v, actionId, event ->
            // 소프트 키보드에서 검색 버튼을 눌렀을 때
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                // 칩 그룹의 모든 칩 체크 해제
                binding.chipGroup.clearCheck()

                // 키보드 내리기
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager // 형변환
                imm.hideSoftInputFromWindow(v.windowToken, 0)

                // 포커스 해제
                // 이를 통해 사용자가 다른 UI 요소와 상호작용할 수 있음
                binding.searchEditText.clearFocus()

                // 검색
                newsService.search(binding.searchEditText.text.toString()).submitList()

                // 람다 리턴
                // 이벤트 처리가 완료되었음을 나타냄
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }

        // 스포츠 칩
        binding.sportsChip.setOnClickListener {
            binding.chipGroup.clearCheck()
            binding.sportsChip.isChecked = true

            newsService.sportsNews().submitList()
        }

        // 메인 홈 정보 가져오기
        binding.homeChip.isChecked = true
        newsService.homeNews().submitList()
    }

    // 확장 함수
    // API 호출 및 리사이클러뷰 리스트 갱신
    private fun Call<Rss>.submitList() {
        this.enqueue(object : Callback<Rss> {
            override fun onResponse(p0: Call<Rss>, p1: Response<Rss>) {
                Log.d("MainActivity", "${p1.body()?.channel?.newsItemList}")

                // List<NewsItem>
                val newsItemList = p1.body()?.channel?.newsItemList.orEmpty()

                // List<News>
                val newsList = newsItemList.transform()

                // 검색 결과 없을 경우 not_found Lottie 애니메이션 띄우기
                binding.notFoundView.isVisible = newsList.isEmpty()

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

                        // UI작업은 UI 스레드에서 실행
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