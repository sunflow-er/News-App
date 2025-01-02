package com.masonk.news

import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.PropertyElement
import com.tickaroo.tikxml.annotation.Xml

@Xml(name = "rss") // 이름 매핑
data class Rss (
    @Element(name = "channel")
    val channel: Channel
)

@Xml(name = "channel")
data class Channel(
    @PropertyElement(name = "title") // XML의 단일 속성 요소 매핑 (텍스트, 숫자)
    val title: String,
    @Element (name = "item") // XML의 복잡한 요소 매핑 (객체, 리스트)
    val newsItemList: List<NewsItem>? = null,
)

@Xml(name = "item")
data class NewsItem(
    @PropertyElement(name = "title")
    val title: String? = null,
    @PropertyElement(name = "link")
    val link: String? = null,
)
