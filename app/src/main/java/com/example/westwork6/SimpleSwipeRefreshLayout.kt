package com.example.westwork6

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.core.view.ViewCompat

class SimpleSwipeRefreshLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {

    var isRefreshing = false
    private var isGestureEnabled = true
    private var refreshListener: (() -> Unit)? = null

    private var touchStartY = 0f
    private var isDragging = false
    private var currentOffsetTop = 0

    private val progressBar = ProgressBar(context).apply {
        visibility = View.GONE
    }

    init {
        addView(progressBar)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        if (!isGestureEnabled || isRefreshing) return false
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStartY = ev.y
                isDragging = true
            }
            MotionEvent.ACTION_MOVE -> {
                val diffY = ev.y - touchStartY
                if (diffY > 50 && !isDragging) {
                    isDragging = true
                    return true
                }
            }
        }
        return false
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (isRefreshing) return false

        when (event.action) {
            MotionEvent.ACTION_MOVE -> {
                if (isDragging) {
                    val diffY = (event.y - touchStartY).toInt()
                    if (diffY > 0) {
                        moveSpinner(diffY)
                    }
                }
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                if (isDragging) {
                    if (currentOffsetTop > progressBar.height) {
                        startRefresh()
                    } else {
                        reset()
                    }
                    isDragging = false
                }
            }
        }
        return true
    }

    private fun moveSpinner(offset: Int) {
        currentOffsetTop = offset / 2
        progressBar.visibility = View.VISIBLE
        progressBar.translationY = currentOffsetTop.toFloat()
        getChildAt(1).translationY = currentOffsetTop.toFloat()
    }

    private fun startRefresh() {
        isRefreshing = true
        refreshListener?.invoke()
        progressBar.visibility = View.VISIBLE
        progressBar.translationY = currentOffsetTop.toFloat()
        ViewCompat.offsetTopAndBottom(getChildAt(1), progressBar.height - currentOffsetTop)
        currentOffsetTop = progressBar.height
    }

    private fun reset() {
        isRefreshing = false
        currentOffsetTop = 0
        progressBar.visibility = View.GONE
        getChildAt(1).translationY = 0f
    }

    override fun onLayout(p0: Boolean, p1: Int, p2: Int, p3: Int, p4: Int) {
        progressBar.layout(
            width / 2 - progressBar.measuredWidth / 2,
            -progressBar.measuredHeight,
            width / 2 + progressBar.measuredWidth / 2,
            0
        )

        val child = getChildAt(1)
        child.layout(0, currentOffsetTop, child.measuredWidth, currentOffsetTop + child.measuredHeight)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        measureChild(progressBar, widthMeasureSpec, heightMeasureSpec)
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            measureChild(child, widthMeasureSpec, heightMeasureSpec)
        }
    }

    fun setOnRefreshListener(listener: () -> Unit) {
        this.refreshListener = listener
    }

    @JvmName("setRefreshing1")
    fun setRefreshing(refreshing: Boolean) {
        this.isRefreshing = refreshing
        if (!refreshing) reset()
    }

    fun setGestureEnabled(enabled: Boolean) {
        this.isGestureEnabled = enabled
    }
}
