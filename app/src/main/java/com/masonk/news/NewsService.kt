package com.masonk.news

import retrofit2.Call
import retrofit2.http.GET

interface NewsService {
    @GET("/rss?hl=ko&gl=KR&ceid=KR:ko")
    fun mainFeed(): Call<Rss>
}