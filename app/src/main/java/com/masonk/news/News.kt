package com.masonk.news

data class News(
    val title: String,
    val link: String,
    var imageUrl: String? = null,
)

// List<NewItem>를 List<News>로 바꾸는 함수
// 확장 함수
fun List<NewsItem>.transform(): List<News> {
    return this.map {
        News(
            title = it.title ?: "",
            link = it.link ?: "",
            imageUrl = null
        )
    }
}
