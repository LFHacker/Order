package online.lfcloud.order.ui

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.Attachment
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.Teal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import online.lfcloud.order.ui.theme.Bg
import online.lfcloud.order.ui.theme.Soft2
import online.lfcloud.order.ui.theme.MutedLight

@Composable
fun AttachmentScreen(
    vm: OrderViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val item = vm.currentItem() ?: return
    val list = vm.attachmentsOf(item.id)
    val images = list.filter { it.kind == "image" }
    val files = list.filter { it.kind != "image" }
    var preview by remember { mutableStateOf<Attachment?>(null) }
    var menuFor by remember { mutableStateOf<Attachment?>(null) }
    var cropUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            cropUri = uri
        }
    }

    Column(Modifier.fillMaxSize().background(Bg)) {

        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(item.name, fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
            Text(
                "上传", fontSize = 14.sp, color = Teal,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).clickable {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("附件", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("${images.size} 张图片 · ${files.size} 个文件", fontSize = 12.5.sp, color = Muted)
            Spacer(Modifier.height(12.dp))
        }

        if (list.isEmpty()) {
            EmptyHint(
                title = "还没有附件",
                cta = "选一张图片",
                iconId = "box",
                onCreate = {
                    picker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
            )
        } else {
            Column(Modifier.fillMaxSize()) {
                if (images.isNotEmpty()) {
                    Text("图片", fontSize = 12.5.sp, color = Muted,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp))
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        images.chunked(3).forEach { rowItems ->
                            Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                                rowItems.forEach { att ->
                                    Box(
                                        Modifier.weight(1f).aspectRatio(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Soft2)
                                            .clickable { preview = att }
                                    ) {
                                        val bmp = rememberUriBitmap(att.uri)
                                        if (bmp != null) {
                                            Image(bmp, contentDescription = att.displayName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize())
                                        } else {
                                            Icon(
                                                IconLibrary.vector("box"), contentDescription = null,
                                                tint = Muted,
                                                modifier = Modifier.size(22.dp).align(Alignment.Center)
                                            )
                                        }
                                    }
                                }
                                repeat(3 - rowItems.size) { Spacer(Modifier.weight(1f)) }
                            }
                            Spacer(Modifier.height(9.dp))
                        }
                    }
                }

                if (files.isNotEmpty()) {
                    Text("文件", fontSize = 12.5.sp, color = Muted,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp))
                    files.forEach { att ->
                        Column {
                            Row(
                                Modifier.fillMaxWidth().clickable { preview = att }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier.size(34.dp).clip(RoundedCornerShape(9.dp))
                                        .background(Soft2),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(IconLibrary.vector("layers"), contentDescription = null,
                                        tint = Muted, modifier = Modifier.size(17.dp))
                                }
                                Spacer(Modifier.width(11.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(att.displayName, fontSize = 14.sp, color = Ink, maxLines = 1)
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        "${formatSize(att.sizeBytes)} · ${formatDate(att.addedAt)}添加",
                                        fontSize = 12.sp, color = Muted
                                    )
                                }
                            }
                            Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp)
                                .background(HairLine))
                        }
                    }
                }

                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        Modifier.size(34.dp).clip(RoundedCornerShape(9.dp))
                            .background(Bg)
                            .clickable {
                                picker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = Teal,
                            modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(11.dp))
                    Text("添加图片", fontSize = 14.sp, color = Teal)
                }

                
            }
        }
    }

    cropUri?.let { uri ->
        ImageCropDialog(
            uri = uri,
            aspectRatio = 1f,
            onDone = { path ->
                vm.addAttachment(item.id, "图片", path, "image")
                cropUri = null
            },
            onCancel = { cropUri = null }
        )
    }

    // 全屏预览
    preview?.let { att ->
        Dialog(
            onDismissRequest = { preview = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(Modifier.fillMaxSize().background(Color(0xFF0C0C0C)).clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { preview = null }) {
                val bmp = rememberUriBitmap(att.uri)
                if (bmp != null) {
                    Image(bmp, contentDescription = att.displayName,
                        contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize().padding(12.dp))
                } else {
                    Column(Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(IconLibrary.vector("layers"), contentDescription = null,
                            tint = Color(0xFF6E8B86), modifier = Modifier.size(56.dp))
                        Spacer(Modifier.height(12.dp))
                        Text(att.displayName, fontSize = 14.sp, color = MutedLight)
                    }
                }
                Column(Modifier.align(Alignment.BottomCenter).padding(bottom = 36.dp)) {
                    Text(att.displayName, fontSize = 13.sp, color = Color(0xFFD8D8D8))
                    Spacer(Modifier.height(6.dp))
                    Text("轻点空白处关闭", fontSize = 11.sp, color = Color(0xFF8A8A8A))
                }
                Text(
                    "删除", fontSize = 14.sp, color = Danger,
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 44.dp, end = 20.dp)
                        .clickable {
                            vm.deleteAttachment(att.id)
                            preview = null
                        }
                )
            }
        }
    }
}

/** 读一张图：支持 file://（裁剪后保存的）和 content://（相册选的）。 */
@Composable
fun rememberUriBitmap(uri: String?): ImageBitmap? {
    val context = LocalContext.current
    var bmp by remember(uri) { mutableStateOf<ImageBitmap?>(null) }
    LaunchedEffect(uri) {
        bmp = if (uri == null) null else withContext(Dispatchers.IO) {
            runCatching {
                val parsed = Uri.parse(uri)
                val stream = if (parsed.scheme == "file") {
                    parsed.path?.let { java.io.FileInputStream(it) }
                } else {
                    context.contentResolver.openInputStream(parsed)
                }
                stream?.use { ins ->
                    android.graphics.BitmapFactory.decodeStream(ins)?.asImageBitmap()
                }
            }.getOrNull()
        }
    }
    return bmp
}

fun queryName(context: android.content.Context, uri: Uri): String? =
    runCatching {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) c.getString(idx) else null
            } else null
        }
    }.getOrNull()

fun formatSize(bytes: Long): String = when {
    bytes <= 0 -> "未知大小"
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.US, "%.1f MB", bytes / 1048576.0)
}

fun formatDate(millis: Long): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(millis))
