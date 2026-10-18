package com.github.vorratshaltung.ui.products

import android.webkit.CookieManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.github.vorratshaltung.data.remote.OpenFoodFactsClient
import com.github.vorratshaltung.data.remote.OpenFoodFactsProduct
import kotlinx.coroutines.delay
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductImageSearchDialog(
    initialQuery: String,
    onDismiss: () -> Unit,
    onImageSelected: (product: OpenFoodFactsProduct) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: OpenFoodFacts, 1: Google Images

    // OpenFoodFacts state
    var searchQuery by remember { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<OpenFoodFactsProduct>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var searchTrigger by remember { mutableStateOf(0) }

    // Google Images WebView state
    var googleQuery by remember { mutableStateOf(initialQuery) }
    var webViewUrl by remember {
        val encoded = try { URLEncoder.encode(initialQuery.ifBlank { "food" }, "UTF-8") } catch (_: Exception) { "food" }
        mutableStateOf("https://www.google.com/search?q=$encoded&tbm=isch&hl=de&gl=de")
    }
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery, searchTrigger) {
        if (searchQuery.length >= 2) {
            isLoading = true
            delay(400)
            results = OpenFoodFactsClient.searchProducts(searchQuery)
            isLoading = false
        } else {
            results = emptyList()
            isLoading = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Produktbild suchen") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            ) {
                PrimaryTabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("OpenFoodFacts") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Google Bilder") }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (selectedTab == 0) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Suchbegriff (min. 2 Zeichen)") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else if (results.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(if (searchQuery.length < 2) "Bitte mindestens 2 Zeichen eingeben." else "Keine Treffer gefunden.")
                                if (searchQuery.length >= 2) {
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(onClick = { searchTrigger++ }) {
                                        Text("Erneut versuchen")
                                    }
                                }
                            }
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(results) { product ->
                                Column(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { onImageSelected(product) }
                                        .padding(4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    AsyncImage(
                                        model = product.imageUrl,
                                        contentDescription = product.name,
                                        modifier = Modifier
                                            .size(100.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = product.name,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                } else {
                    OutlinedTextField(
                        value = googleQuery,
                        onValueChange = { googleQuery = it },
                        label = { Text("Suchbegriff für Google Bilder") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val encoded = try { URLEncoder.encode(googleQuery.ifBlank { "food" }, "UTF-8") } catch (_: Exception) { "food" }
                                webViewUrl = "https://www.google.com/search?q=$encoded&tbm=isch&hl=de&gl=de"
                                selectedImageUrl = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Google Bilder suchen")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.userAgentString = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36"
                                    settings.useWideViewPort = true
                                    settings.loadWithOverviewMode = true
                                    settings.setSupportZoom(true)
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false
                                    setInitialScale(50) // Zoom out to 50% so more images are visible
                                    
                                    CookieManager.getInstance().setAcceptCookie(true)
                                    CookieManager.getInstance().setCookie(".google.com", "CONSENT=YES+DE.de+V14+BX")

                                    addJavascriptInterface(object {
                                        @JavascriptInterface
                                        fun onImageClicked(url: String) {
                                            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                                                selectedImageUrl = url
                                            }
                                        }
                                    }, "AndroidApp")

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            view?.evaluateJavascript(
                                                """
                                                (function() {
                                                    var meta = document.querySelector('meta[name="viewport"]');
                                                    if (!meta) {
                                                        meta = document.createElement('meta');
                                                        meta.name = 'viewport';
                                                        document.getElementsByTagName('head')[0].appendChild(meta);
                                                    }
                                                    meta.content = 'width=device-width, initial-scale=0.5, maximum-scale=3.0, user-scalable=yes';

                                                    document.querySelectorAll('img').forEach(function(img) {
                                                        img.addEventListener('click', function(e) {
                                                            var src = img.src;
                                                            if (src && src.startsWith('http')) {
                                                                AndroidApp.onImageClicked(src);
                                                            }
                                                        }, true);
                                                    });
                                                })();
                                                """.trimIndent(),
                                                null
                                            )
                                        }
                                    }
                                    loadUrl(webViewUrl)
                                }
                            },
                            update = { webView ->
                                if (webView.url != webViewUrl) {
                                    webView.loadUrl(webViewUrl)
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    if (!selectedImageUrl.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AsyncImage(
                                model = selectedImageUrl,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Text(
                                text = "Bild ausgewählt!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            OutlinedButton(
                                onClick = {
                                    selectedImageUrl?.let { url ->
                                        onImageSelected(
                                            OpenFoodFactsProduct(
                                                name = googleQuery,
                                                imageUrl = url,
                                                calories = null,
                                                protein = null,
                                                carbohydrates = null,
                                                fat = null
                                            )
                                        )
                                    }
                                }
                            ) {
                                Text("Übernehmen")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 1 && !selectedImageUrl.isNullOrBlank()) {
                TextButton(
                    onClick = {
                        selectedImageUrl?.let { url ->
                            onImageSelected(
                                OpenFoodFactsProduct(
                                    name = googleQuery,
                                    imageUrl = url,
                                    calories = null,
                                    protein = null,
                                    carbohydrates = null,
                                    fat = null
                                )
                            )
                        }
                    }
                ) {
                    Text("Übernehmen")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Schließen")
                }
            }
        }
    )
}
