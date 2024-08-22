import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

object SimpleGlide {

    private val MAX_MEMORY_CACHE_SIZE = (Runtime.getRuntime().maxMemory() / 1024).toInt() / 8

    // 内存缓存
    private val memoryCache = object : LruCache<String, Bitmap>(MAX_MEMORY_CACHE_SIZE) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            return bitmap.byteCount / 1024
        }
    }

    fun with(activity: Activity): RequestManager {
        return RequestManager(activity)
    }

    class RequestManager(private val activity: Activity) {
        fun load(url: String): RequestBuilder {
            return RequestBuilder(activity, url)
        }
    }

    class RequestBuilder(private val activity: Activity, private val url: String) {
        private lateinit var imageView: ImageView

        fun into(imageView: ImageView) {
            this.imageView = imageView
            loadImage()
        }

        private fun loadImage() {
            CoroutineScope(Dispatchers.IO).launch {
                // 先从内存缓存中查找图片
                val cachedBitmap = memoryCache.get(url)
                if (cachedBitmap != null) {
                    withContext(Dispatchers.Main) {
                        imageView.setImageBitmap(cachedBitmap)
                    }
                } else {
                    // 如果内存缓存中没有图片，尝试从磁盘缓存中获取
                    val diskCacheBitmap = loadBitmapFromDiskCache(url)
                    if (diskCacheBitmap != null) {
                        memoryCache.put(url, diskCacheBitmap)  // 同时放入内存缓存
                        withContext(Dispatchers.Main) {
                            imageView.setImageBitmap(diskCacheBitmap)
                        }
                    } else {
                        // 如果磁盘缓存中也没有图片，则从网络加载
                        val bitmap = downloadImage(url)
                        bitmap?.let {
                            memoryCache.put(url, it)  // 放入内存缓存
                            saveBitmapToDiskCache(url, it)  // 放入磁盘缓存
                            withContext(Dispatchers.Main) {
                                imageView.setImageBitmap(it)
                            }
                        }
                    }
                }
            }
        }

        private fun downloadImage(url: String): Bitmap? {
            var connection: HttpURLConnection? = null
            return try {
                connection = URL(url).openConnection() as HttpURLConnection
                connection.connect()
                val inputStream = connection.inputStream
                BitmapFactory.decodeStream(inputStream)
            } finally {
                connection?.disconnect()
            }
        }

        private fun loadBitmapFromDiskCache(url: String): Bitmap? {
            val cacheDir = activity.cacheDir
            val fileName = url.hashCode().toString()
            val cacheFile = File(cacheDir, fileName)
            return if (cacheFile.exists()) {
                BitmapFactory.decodeFile(cacheFile.absolutePath)
            } else {
                null
            }
        }

        private fun saveBitmapToDiskCache(url: String, bitmap: Bitmap) {
            CoroutineScope(Dispatchers.IO).launch {
                val cacheDir = activity.cacheDir
                val fileName = url.hashCode().toString()
                val cacheFile = File(cacheDir, fileName)
                if (!cacheFile.exists()) {
                    cacheFile.createNewFile()
                }
                var fos: FileOutputStream? = null
                try {
                    fos = FileOutputStream(cacheFile)
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                } finally {
                    fos?.flush()
                    fos?.close()
                }
            }
        }
    }
}
