package com.example.westwork6

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.example.westwork6.R.id
import com.example.westwork6.R.layout

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var mTextView: TextView
    private lateinit var button: Button
    private lateinit var imageView: ImageView

    private val imageUrls = listOf(
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/d7bc7b2b-3c94-4bc9-8ffb-792b35af8340.jpg",
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/5b0e9c75-834a-4fa1-8de5-e5f367d27815.jpg",
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/75c78e90-e48d-457f-af37-5ec09189f630.jpg",
        "https://img.benesse-cms.jp/pet-cat/item/image/normal/10fd3341-5038-4b39-8254-4350d1a771ea.jpg?",
    )
    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout.activity_main)

        swipeRefreshLayout = findViewById(id.swipeLayout)
        mTextView = findViewById(id.text_test)
        button = findViewById(id.button)
        imageView = findViewById(id.image_view)

        // 设置下拉刷新图标的大小 只支持两种： DEFAULT 和 LARGE
        swipeRefreshLayout.setSize(CircularProgressDrawable.LARGE)

        // 设置刷新图标的颜色，在手指下滑刷新时使用第一个颜色，在刷新中，会一个个颜色进行切换
        swipeRefreshLayout.setColorSchemeColors(
            Color.BLACK, Color.GREEN, Color.RED, Color.YELLOW, Color.BLUE
        )

        // 设置刷新图标的颜色，传入资源 ID
        swipeRefreshLayout.setColorSchemeResources(R.color.red, R.color.blue, R.color.green)

        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white)

        // 设置监听器, 需要重写 onRefresh() 方法
        swipeRefreshLayout.setOnRefreshListener(this)

        button.setOnClickListener {
            val intent = Intent(this, MyActivity::class.java)
            startActivity(intent)
        }

        loadImage()
    }

    override fun onRefresh() {
        Log.e("test", "===是否==正在刷新中====${swipeRefreshLayout.isRefreshing}")

        mTextView.text = "正在刷新中......"
        loadImage()

        Handler().postDelayed({
            mTextView.text = "刷新完成"
            // 完成数据更新后调用 setRefreshing(false)，否则刷新图标会一直转圈
            swipeRefreshLayout.isRefreshing = false
        }, 2000)
    }

    private fun loadImage() {
        Glide.with(this)
            .load(imageUrls[currentIndex])
            .into(imageView)

        currentIndex = (currentIndex + 1) % imageUrls.size
    }
}
