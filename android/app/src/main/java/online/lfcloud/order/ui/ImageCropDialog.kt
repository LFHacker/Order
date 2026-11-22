package online.lfcloud.order.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import online.lfcloud.order.ui.theme.Teal
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * 选完图先裁剪：裁成显示区的比例再落盘，避免整张图塞进固定大小的位置。
 */
@Composable
fun ImageCropDialog(
    uri: Uri,
    aspectRatio: Float,
    onDone: (String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var source by remember(uri) { mutableStateOf<Bitmap?>(null) }
    var userScale by remember(uri) { mutableStateOf(1f) }
    var offsetX by remember(uri) { mutableStateOf(0f) }
    var offsetY by remember(uri) { mutableStateOf(0f) }
    var saving by remember(uri) { mutableStateOf(false) }

    LaunchedEffect(uri) {
        source = withContext(Dispatchers.IO) {
            runCatching {
                var bmp: Bitmap? = null
                context.contentResolver.openInputStream(uri)?.use { ins ->
                    bmp = BitmapFactory.decodeStream(ins)
                }
                val b = bmp
                if (b != null && (b.width > 2400 || b.height > 2400)) {
                    Bitmap.createScaledBitmap(b, b.width / 2, b.height / 2, true)
                } else {
                    b
                }
            }.getOrNull()
        }
    }

    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(Modifier.fillMaxSize().background(Color(0xFF0C0C0C))) {

            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("取消", fontSize = 15.sp, color = Color(0xFFB9C9C6),
                    modifier = Modifier.clickable(onClick = onCancel))
                Text("裁剪图片", fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    color = Color.White)
                Text(
                    if (saving) "保存中" else "完成",
                    fontSize = 15.sp, fontWeight = FontWeight.SemiBold,
                    color = if (saving) Color(0xFF7A8B88) else Teal,
                    modifier = Modifier.clickable(enabled = !saving) {
                        if (source != null) saving = true
                    }
                )
            }

            val bmp = source
            if (bmp == null) {
                Box(Modifier.fillMaxWidth().height(220.dp), contentAlignment = Alignment.Center) {
                    Text("读取图片…", fontSize = 14.sp, color = Color(0xFF7A8B88))
                }
            } else {
                val imageBmp = bmp.asImageBitmap()

                BoxWithConstraints(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    val boxW = with(density) { maxWidth.toPx() }
                    val boxH = boxW / aspectRatio
                    val baseScale = max(boxW / bmp.width, boxH / bmp.height)
                    val effScale = baseScale * userScale
                    val dispW = bmp.width * effScale
                    val dispH = bmp.height * effScale
                    val maxOffX = max(0f, (dispW - boxW) / 2f)
                    val maxOffY = max(0f, (dispH - boxH) / 2f)
                    val offX = offsetX.coerceIn(-maxOffX, maxOffX)
                    val offY = offsetY.coerceIn(-maxOffY, maxOffY)

                    Box(
                        Modifier.fillMaxWidth().height(with(density) { boxH.toDp() })
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.Black)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    userScale = (userScale * zoom).coerceIn(1f, 4f)
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffX, maxOffX)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffY, maxOffY)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            imageBmp, contentDescription = null,
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .size(
                                    with(density) { dispW.toDp() },
                                    with(density) { dispH.toDp() }
                                )
                                .offset { IntOffset(offX.roundToInt(), offY.roundToInt()) }
                        )
                        Canvas(Modifier.fillMaxSize()) {
                            drawRect(
                                color = Color(0x66FFFFFF),
                                topLeft = Offset(1f, 1f),
                                size = Size(size.width - 2f, size.height - 2f),
                                style = Stroke(width = 1.5f)
                            )
                            for (i in 1..2) {
                                val x = size.width * i / 3f
                                val y = size.height * i / 3f
                                drawLine(Color(0x33FFFFFF), Offset(x, 0f), Offset(x, size.height), 1f)
                                drawLine(Color(0x33FFFFFF), Offset(0f, y), Offset(size.width, y), 1f)
                            }
                        }
                    }

                    if (saving) {
                        LaunchedEffect(Unit) {
                            val srcLeft = (boxW - dispW) / 2f + offX
                            val srcTop = (boxH - dispH) / 2f + offY
                            val srcX = ((-srcLeft) / effScale)
                                .coerceIn(0f, max(0f, bmp.width - boxW / effScale))
                            val srcY = ((-srcTop) / effScale)
                                .coerceIn(0f, max(0f, bmp.height - boxH / effScale))
                            val srcW = (boxW / effScale).coerceAtMost(bmp.width - srcX)
                            val srcH = (boxH / effScale).coerceAtMost(bmp.height - srcY)
                            val path = withContext(Dispatchers.IO) {
                                runCatching {
                                    val cropped = Bitmap.createBitmap(
                                        bmp, srcX.toInt(), srcY.toInt(),
                                        srcW.toInt().coerceAtLeast(1),
                                        srcH.toInt().coerceAtLeast(1)
                                    )
                                    val dir = File(context.filesDir, "images").apply { mkdirs() }
                                    val out = File(dir, "img_${System.currentTimeMillis()}.jpg")
                                    FileOutputStream(out).use { fos ->
                                        cropped.compress(Bitmap.CompressFormat.JPEG, 88, fos)
                                    }
                                    "file://${out.absolutePath}"
                                }.getOrNull()
                            }
                            if (path != null) onDone(path) else saving = false
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "双指缩放，拖动调整位置",
                    fontSize = 12.sp, color = Color(0xFF6E8B86),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}
