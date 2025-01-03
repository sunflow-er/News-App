package com.masonk.news

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.masonk.news.databinding.ItemNewsBinding

class NewsAdapter(private val onClick: (String) -> Unit) : ListAdapter<News, NewsAdapter.NewsHolder>(diffUtil) {

    // 뷰홀더
    inner class NewsHolder(private val binding: ItemNewsBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(news: News) {
            // 제목
            binding.titleTextView.text = news.title

            // 이미지
            Glide.with(binding.thumbnailImageView)
                .load(news.imageUrl)
                .into(binding.thumbnailImageView)

            // 클릭 시, 해당 뉴스 전문을 보여주는 웹뷰 액티비티로 이동
            binding.root.setOnClickListener {
                onClick(news.link)
            }
        }
    }

    companion object {
        val diffUtil = object: DiffUtil.ItemCallback<News>() {
            override fun areItemsTheSame(oldItem: News, newItem: News): Boolean {
                return oldItem === newItem // 참조 비교: 두 객체가 메모리에서 동일한 위치를 가리키는지 확인
            }

            override fun areContentsTheSame(oldItem: News, newItem: News): Boolean {
                return oldItem == newItem // 값 비교: 두 객체의 내용이 같은지 확인
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsHolder {
        return NewsHolder(
            ItemNewsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }

    override fun onBindViewHolder(holder: NewsHolder, position: Int) {
        holder.bind(currentList[position])
    }


}