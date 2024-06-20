package com.elvira.handspeak.view.article

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.dicoding.picodiploma.storyspace.view.ViewModelFactory
import com.elvira.handspeak.databinding.ActivityArticleBinding

class ArticleActivity : AppCompatActivity() {
    private val viewModel: ArticleViewModel by viewModels{
        ViewModelFactory.getInstance()
    }

    private lateinit var binding: ActivityArticleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArticleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val articleId = intent.getStringExtra(ARTICLE_ID).toString()
        viewModel.getDetailArticle(articleId)

        viewModel.articleDetail.observe(this) {article ->
            binding.tvTitle.text = article.title
            binding.tvContent.text = article.content
            Log.e("ArticleActivity", "$article.image")
            Glide.with(this)
                .load(article.image)
                .into(binding.imgArticle)

        }
    }

    companion object {
        const val ARTICLE_ID = "article_id"
    }
}