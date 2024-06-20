package com.elvira.handspeak.view.main

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.dicoding.picodiploma.storyspace.view.ViewModelFactory
import com.elvira.handspeak.HistoryActivity
import com.elvira.handspeak.R
import com.elvira.handspeak.databinding.ActivityMainBinding
import com.elvira.handspeak.view.MenuActivity
import com.elvira.handspeak.view.adapter.ArticleAdapter

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels{
        ViewModelFactory.getInstance()
    }

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ArticleAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = ArticleAdapter()
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.adapter = adapter

        viewModel.articleResponse.observe(this) { articles ->
            if (articles != null) {
                adapter.submitList(articles)
            }
        }

        val historyBtn: View = findViewById(R.id.imageView2)
        historyBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, HistoryActivity::class.java)
            startActivity(intent)
        }

        val menuBtn: View = findViewById(R.id.floatingCameraButton)
        menuBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, MenuActivity::class.java)
            startActivity(intent)
        }
    }
    companion object {
        const val REQUIRED_PERMISSION = Manifest.permission.CAMERA
        private const val TAG = "MainActivity"
        private const val DATA_ID = "1"
    }
}