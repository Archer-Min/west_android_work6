package com.example.westwork6


import SimpleGlide
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.westwork6.databinding.ActivityMyBinding


class MyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMyBinding
    private lateinit var swipeRefreshLayout: SimpleSwipeRefreshLayout
    private lateinit var imageView: ImageView

    private val imageUrls = listOf(
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/d7bc7b2b-3c94-4bc9-8ffb-792b35af8340.jpg",
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/5b0e9c75-834a-4fa1-8de5-e5f367d27815.jpg",
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/75c78e90-e48d-457f-af37-5ec09189f630.jpg",
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/10fd3341-5038-4b39-8254-4350d1a771ea.jpg?",
    )
    private var currentImageIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        swipeRefreshLayout = binding.swipeLayout
        imageView = binding.image

        // 使用自定义的 SimpleSwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            onRefresh()
        }

        // 设置背景颜色和指示器颜色
        swipeRefreshLayout.setBackgroundColor(Color.TRANSPARENT) // 背景颜色可以设置为透明

        loadNextImage()
    }

    private fun onRefresh() {
        Log.e("test", "===是否==正在刷新中====${swipeRefreshLayout.isRefreshing}")

        Handler().postDelayed({
            swipeRefreshLayout.setRefreshing(false)
        }, 2000)

        loadNextImage()
    }

    private fun loadNextImage() {
        SimpleGlide.with(this)
            .load(imageUrls[currentImageIndex])
            .into(imageView)

        currentImageIndex = (currentImageIndex + 1) % imageUrls.size
    }
}