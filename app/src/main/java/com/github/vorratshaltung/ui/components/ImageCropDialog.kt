package com.github.vorratshaltung.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class DragMode {
    NONE,
    MOVE,
    RESIZE_TL,
    RESIZE_TR,
    RESIZE_BL,
    RESIZE_BR
}

@Composable
fun ImageCropDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onCropped: (Uri) -> Unit
) {
    val context = LocalContext.current
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    var cropRect by remember { mutableStateOf<Rect?>(null) }
    var containerSize by remember { mutableStateOf(Size.Zero) }
    var dragMode by remember { mutableStateOf(DragMode.NONE) }

    LaunchedEffect(imageUri) {
        isLoading = true
        val loadedBitmap = withContext(Dispatchers.IO) {
            try {
                val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
        bitmap = loadedBitmap
        isLoading = false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Bild zuschneiden") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .background(Color.Black)
                    .clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading || bitmap == null) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    val bmp = bitmap!!
                    val imgBitmap = remember(bmp) { bmp.asImageBitmap() }

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = imgBitmap,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, pan, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(1f, 5f)
                                        offset += pan
                                    }
                                }
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offset.x,
                                    translationY = offset.y
                                ),
                            contentScale = ContentScale.Fit
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(containerSize) {
                                    detectDragGestures(
                                        onDragStart = { pos ->
                                            val rect = cropRect ?: return@detectDragGestures
                                            val handleRadius = 60f
                                            val tl = rect.topLeft
                                            val tr = rect.topRight
                                            val bl = rect.bottomLeft
                                            val br = rect.bottomRight

                                            dragMode = when {
                                                (pos - tl).getDistance() < handleRadius -> DragMode.RESIZE_TL
                                                (pos - tr).getDistance() < handleRadius -> DragMode.RESIZE_TR
                                                (pos - bl).getDistance() < handleRadius -> DragMode.RESIZE_BL
                                                (pos - br).getDistance() < handleRadius -> DragMode.RESIZE_BR
                                                rect.contains(pos) -> DragMode.MOVE
                                                else -> DragMode.NONE
                                            }
                                        },
                                        onDragEnd = { dragMode = DragMode.NONE },
                                        onDragCancel = { dragMode = DragMode.NONE },
                                        onDrag = { change, dragAmount ->
                                            val rect = cropRect ?: return@detectDragGestures
                                            val minSize = 100f
                                            val maxW = containerSize.width
                                            val maxH = containerSize.height

                                            cropRect = when (dragMode) {
                                                DragMode.MOVE -> {
                                                    val newLeft = (rect.left + dragAmount.x).coerceIn(0f, maxW - rect.width)
                                                    val newTop = (rect.top + dragAmount.y).coerceIn(0f, maxH - rect.height)
                                                    Rect(newLeft, newTop, newLeft + rect.width, newTop + rect.height)
                                                }
                                                DragMode.RESIZE_TL -> {
                                                    val newLeft = (rect.left + dragAmount.x).coerceIn(0f, rect.right - minSize)
                                                    val newTop = (rect.top + dragAmount.y).coerceIn(0f, rect.bottom - minSize)
                                                    Rect(newLeft, newTop, rect.right, rect.bottom)
                                                }
                                                DragMode.RESIZE_TR -> {
                                                    val newRight = (rect.right + dragAmount.x).coerceIn(rect.left + minSize, maxW)
                                                    val newTop = (rect.top + dragAmount.y).coerceIn(0f, rect.bottom - minSize)
                                                    Rect(rect.left, newTop, newRight, rect.bottom)
                                                }
                                                DragMode.RESIZE_BL -> {
                                                    val newLeft = (rect.left + dragAmount.x).coerceIn(0f, rect.right - minSize)
                                                    val newBottom = (rect.bottom + dragAmount.y).coerceIn(rect.top + minSize, maxH)
                                                    Rect(newLeft, rect.top, rect.right, newBottom)
                                                }
                                                DragMode.RESIZE_BR -> {
                                                    val newRight = (rect.right + dragAmount.x).coerceIn(rect.left + minSize, maxW)
                                                    val newBottom = (rect.bottom + dragAmount.y).coerceIn(rect.top + minSize, maxH)
                                                    Rect(rect.left, rect.top, newRight, newBottom)
                                                }
                                                DragMode.NONE -> rect
                                            }
                                            change.consume()
                                        }
                                    )
                                }
                        ) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                containerSize = size

                                if (cropRect == null) {
                                    val w = size.width * 0.75f
                                    val h = size.height * 0.75f
                                    val left = (size.width - w) / 2f
                                    val top = (size.height - h) / 2f
                                    cropRect = Rect(left, top, left + w, top + h)
                                }

                                val rect = cropRect!!
                                val scrimColor = Color.Black.copy(alpha = 0.6f)

                                drawRect(scrimColor, topLeft = Offset.Zero, size = Size(size.width, rect.top))
                                drawRect(scrimColor, topLeft = Offset(0f, rect.bottom), size = Size(size.width, size.height - rect.bottom))
                                drawRect(scrimColor, topLeft = Offset(0f, rect.top), size = Size(rect.left, rect.height))
                                drawRect(scrimColor, topLeft = Offset(rect.right, rect.top), size = Size(size.width - rect.right, rect.height))

                                drawRect(
                                    color = Color.White,
                                    topLeft = rect.topLeft,
                                    size = rect.size,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                                )

                                val handleRadius = 12.dp.toPx()
                                listOf(rect.topLeft, rect.topRight, rect.bottomLeft, rect.bottomRight).forEach { corner ->
                                    drawCircle(
                                        color = Color.White,
                                        radius = handleRadius,
                                        center = corner
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val bmp = bitmap
                    val rect = cropRect
                    if (bmp != null && rect != null && containerSize.width > 0f) {
                        try {
                            val containerW = containerSize.width
                            val containerH = containerSize.height
                            val imgAspect = bmp.width.toFloat() / bmp.height.toFloat()
                            val containerAspect = containerW / containerH

                            val renderedW: Float
                            val renderedH: Float
                            if (imgAspect > containerAspect) {
                                renderedW = containerW
                                renderedH = containerW / imgAspect
                            } else {
                                renderedH = containerH
                                renderedW = containerH * imgAspect
                            }

                            val scaledW = renderedW * scale
                            val scaledH = renderedH * scale
                            val scaledLeft = (containerW - scaledW) / 2f + offset.x
                            val scaledTop = (containerH - scaledH) / 2f + offset.y

                            val relX = (rect.left - scaledLeft) / scaledW
                            val relY = (rect.top - scaledTop) / scaledH
                            val relW = rect.width / scaledW
                            val relH = rect.height / scaledH

                            val finalX = (relX * bmp.width).toInt().coerceIn(0, bmp.width - 1)
                            val finalY = (relY * bmp.height).toInt().coerceIn(0, bmp.height - 1)
                            val finalW = (relW * bmp.width).toInt().coerceIn(1, bmp.width - finalX)
                            val finalH = (relH * bmp.height).toInt().coerceIn(1, bmp.height - finalY)

                            val croppedBmp = Bitmap.createBitmap(bmp, finalX, finalY, finalW, finalH)

                            val file = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
                            FileOutputStream(file).use { out ->
                                croppedBmp.compress(Bitmap.CompressFormat.JPEG, 90, out)
                            }
                            val croppedUri = Uri.fromFile(file)
                            onCropped(croppedUri)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onCropped(imageUri)
                        }
                    } else {
                        onDismiss()
                    }
                }
            ) {
                Text("Anwenden")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Abbrechen")
            }
        }
    )
}
