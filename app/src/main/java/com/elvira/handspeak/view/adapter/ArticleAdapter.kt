package com.elvira.handspeak.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elvira.handspeak.data.response.DataItem
import com.elvira.handspeak.databinding.ItemRowArticleBinding
import com.elvira.handspeak.view.article.ArticleActivity

class ArticleAdapter: RecyclerView.Adapter<ArticleAdapter.ArticleViewHolder>() {
    private var articles = listOf<DataItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val binding = ItemRowArticleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ArticleViewHolder(binding)
    }

    fun submitList(newArticles: List<DataItem>) {
        articles = newArticles
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        holder.bind(articles[position])
    }

    override fun getItemCount(): Int {
        return articles.size
    }

    class ArticleViewHolder(private val binding: ItemRowArticleBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(articleItem: DataItem) {
            binding.tvItemTitle.text = articleItem.title
            binding.tvSeeMore.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ArticleActivity::class.java)
                intent.putExtra("article_id", articleItem.id)
                context.startActivity(intent)
            }
            Glide.with(itemView)
                .load(articleItem.image)
                .into(binding.imgItemArticle)
        }
    }
}