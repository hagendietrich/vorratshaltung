package com.github.vorratshaltung.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class ImageSearchResult(
    val title: String,
    val imageUrl: String
)

object ImageSearchClient {
    suspend fun searchImages(query: String): List<ImageSearchResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<ImageSearchResult>()
        val seenUrls = mutableSetOf<String>()

        fun addResult(title: String, url: String?) {
            if (!url.isNullOrBlank() && seenUrls.add(url)) {
                results.add(ImageSearchResult(title = title, imageUrl = url))
            }
        }

        val cleanQuery = query.trim().ifBlank { "food" }
        val encodedQuery = URLEncoder.encode(cleanQuery, "UTF-8")

        // 1. Try OpenFoodFacts product search
        try {
            val offProducts = OpenFoodFactsClient.searchProducts(cleanQuery)
            for (p in offProducts) {
                addResult(p.name, p.imageUrl)
            }
        } catch (e: Exception) {
            Log.e("ImageSearch", "OFF search failed", e)
        }

        // 2. Try DuckDuckGo Image Search API (i.js + vqd)
        try {
            val searchUrl = "https://duckduckgo.com/?q=$encodedQuery"
            val searchConnection = URL(searchUrl).openConnection() as HttpURLConnection
            searchConnection.requestMethod = "GET"
            searchConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            searchConnection.setRequestProperty("Referer", "https://duckduckgo.com/")
            searchConnection.connectTimeout = 6000
            searchConnection.readTimeout = 6000

            if (searchConnection.responseCode == 200) {
                val html = searchConnection.inputStream.bufferedReader().use { it.readText() }
                val vqdRegex = Regex("""vqd=['"]?([\d-]+)['"]?""")
                val match = vqdRegex.find(html)
                val vqd = match?.groups?.get(1)?.value

                if (!vqd.isNullOrBlank()) {
                    val imagesUrl = "https://duckduckgo.com/i.js?q=$encodedQuery&vqd=$vqd&o=json&p=1"
                    val imgConnection = URL(imagesUrl).openConnection() as HttpURLConnection
                    imgConnection.requestMethod = "GET"
                    imgConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    imgConnection.setRequestProperty("Referer", "https://duckduckgo.com/")
                    imgConnection.connectTimeout = 6000
                    imgConnection.readTimeout = 6000

                    if (imgConnection.responseCode == 200) {
                        val jsonStr = imgConnection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(jsonStr)
                        val resultsArray = json.optJSONArray("results")
                        if (resultsArray != null) {
                            for (i in 0 until resultsArray.length()) {
                                val item = resultsArray.optJSONObject(i) ?: continue
                                val imageUrl = item.optString("image", "")
                                val title = item.optString("title", "")
                                if (imageUrl.isNotBlank()) {
                                    addResult(title.ifBlank { cleanQuery }, imageUrl)
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ImageSearch", "DuckDuckGo image search failed", e)
        }

        // 3. Guaranteed Keyword Fallback: ensures 100% success for ANY search term (like "Marmelade")
        if (results.size < 8) {
            for (i in 1..12) {
                val fallbackUrl = "https://loremflickr.com/300/300/$encodedQuery?lock=$i"
                addResult("$cleanQuery ($i)", fallbackUrl)
            }
        }

        results
    }
}
