package com.github.vorratshaltung.data.remote

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class OpenFoodFactsProduct(
    val name: String,
    val imageUrl: String?,
    val calories: Int?,
    val protein: Float?,
    val carbohydrates: Float?,
    val fat: Float?
)

object OpenFoodFactsClient {
    suspend fun searchProducts(query: String): List<OpenFoodFactsProduct> = withContext(Dispatchers.IO) {
        val results = mutableListOf<OpenFoodFactsProduct>()
        try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://de.openfoodfacts.org/cgi/search.pl?search_terms=$encodedQuery&search_simple=1&action=process&json=1"
            Log.d("OpenFoodFacts", "Querying URL: $urlString")

            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("User-Agent", "VorratshaltungApp - Android - Version 1.0 (Contact: info@vorratshaltung.app)")
            connection.connectTimeout = 5000
            connection.readTimeout = 5000

            val responseCode = connection.responseCode
            Log.d("OpenFoodFacts", "Response Code: $responseCode")

            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d("OpenFoodFacts", "Response length: ${response.length}")
                val json = JSONObject(response)
                val productsArray = json.optJSONArray("products")
                Log.d("OpenFoodFacts", "Products count: ${productsArray?.length() ?: 0}")

                if (productsArray != null) {
                    for (i in 0 until productsArray.length()) {
                        val p = productsArray.optJSONObject(i) ?: continue
                        val name = p.optString("product_name_de", "")
                            .ifBlank { p.optString("product_name", "") }
                            .ifBlank { p.optString("generic_name_de", "") }
                            .ifBlank { "Unbekanntes Produkt" }

                        val imageUrl = p.optString("image_front_url", "")
                            .ifBlank { p.optString("image_url", "") }
                            .ifBlank { p.optString("image_front_small_url", "") }
                            .ifBlank { p.optString("image_small_url", null) }

                        val nutriments = p.optJSONObject("nutriments")
                        val kcal = nutriments?.optDouble("energy-kcal_100g", 0.0)?.toInt()?.takeIf { it > 0 }
                            ?: nutriments?.optDouble("energy-kcal", 0.0)?.toInt()?.takeIf { it > 0 }
                        val protein = nutriments?.optDouble("proteins_100g", 0.0)?.toFloat()?.takeIf { it > 0f }
                        val carbs = nutriments?.optDouble("carbohydrates_100g", 0.0)?.toFloat()?.takeIf { it > 0f }
                        val fat = nutriments?.optDouble("fat_100g", 0.0)?.toFloat()?.takeIf { it > 0f }

                        results.add(
                            OpenFoodFactsProduct(
                                name = name,
                                imageUrl = imageUrl?.takeIf { it.isNotBlank() },
                                calories = kcal,
                                protein = protein,
                                carbohydrates = carbs,
                                fat = fat
                            )
                        )
                    }
                }
            } else {
                Log.e("OpenFoodFacts", "Error response code: $responseCode")
            }
        } catch (e: Exception) {
            Log.e("OpenFoodFacts", "Exception during search", e)
        }
        Log.d("OpenFoodFacts", "Returning ${results.size} results")
        results
    }
}
