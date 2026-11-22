package online.lfcloud.order.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.Teal
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Edit
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun ItemDetailScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onOpenAttachments: () -> Unit,
    onOpenRelations: () -> Unit,
    onOpenEvents: () -> Unit
) {
    val context = LocalContext.current
    val item = vm.currentItem() ?: return
    var menu by remember { mutableStateOf(false) }
    val attachments = vm.attachmentsOf(item.id)
    val attCount = attachments.size
    val relCount = vm.relationsOf(item.id).size
    val evCount = vm.eventsOf(item.id).size
    val path = vm.pathOf(item.positionId)
    var cropUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var zoomImage by remember { mutableStateOf<String?>(null) }

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

    Column(Modifier.fillMaxSize().background(Color.White)) {

        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(
                "物品详情", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Box(Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                Icon(Icons.Filled.MoreVert, contentDescription = null, tint = Ink,
                    modifier = Modifier.size(20.dp).clickable { menu = true })
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("编辑") }, onClick = { menu = false; onEdit() })
                    DropdownMenuItem(
                        text = { Text("删除", color = Danger) },
                        onClick = { menu = false; onDelete() }
                    )
                }
            }
        }

        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
            DetailTab("基本信息", null, true, {})
            DetailTab("附件", attCount, false, onOpenAttachments)
            DetailTab("关系", relCount, false, onOpenRelations)
            DetailTab("时间线", evCount, false, onOpenEvents)
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {

            Spacer(Modifier.height(16.dp))
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .aspectRatio(1.6f)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, Color(0xFFECEFEF), RoundedCornerShape(14.dp))
            ) {
                val coverBmp = rememberUriBitmap(item.imageUri)
                if (coverBmp != null) {
                    Image(coverBmp, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                            .clickable { zoomImage = item.imageUri })
                    Box(
                        Modifier.align(Alignment.BottomEnd).padding(10.dp)
                            .size(30.dp).clip(CircleShape).background(Color(0x8C000000))
                            .clickable {
                                picker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Edit, contentDescription = null,
                            tint = Color.White, modifier = Modifier.size(15.dp))
                    }
                } else {
                    Box(
                        Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFAFAFA))
                            .clickable {
                                picker.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null,
                            tint = Color(0xFFB9C9C6), modifier = Modifier.size(34.dp))
                    }
                }
            }

            // 图片区与内容之间的分隔
            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            Text(
                item.name, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp, color = Ink,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp)
            )
            Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                path.forEachIndexed { i, seg ->
                    if (i > 0) Text(" / ", fontSize = 13.sp, color = Muted)
                    Text(seg, fontSize = 13.sp, color = Muted)
                }
            }

            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            InfoRow("类别", item.category)
            InfoRow("数量", "${item.quantity} ${item.quantityUnit}")
            InfoRow("状态", item.status)
            if (item.note.isNotBlank()) InfoRow("备注", item.note)

            Spacer(Modifier.height(24.dp))
        }
    }

    zoomImage?.let { path ->
        Dialog(
            onDismissRequest = { zoomImage = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                Modifier.fillMaxSize().background(Color(0xFF0C0C0C))
                    .clickable(interactionSource = remember { MutableInteractionSource() },
                        indication = null) { zoomImage = null },
                contentAlignment = Alignment.Center
            ) {
                val bmp = rememberUriBitmap(path)
                if (bmp != null) {
                    Image(bmp, contentDescription = null, contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize().padding(10.dp))
                }
            }
        }
    }

    cropUri?.let { uri ->
        ImageCropDialog(
            uri = uri,
            aspectRatio = 1.6f,
            onDone = { path ->
                vm.setItemCover(item.id, path)
                cropUri = null
            },
            onCancel = { cropUri = null }
        )
    }
}

@Composable
private fun DetailTab(
    label: String,
    count: Int?,
    active: Boolean,
    onClick: () -> Unit
) {
    Column(Modifier.padding(end = 24.dp).clickable(onClick = onClick)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 12.dp)
        ) {
            Text(
                label, fontSize = 14.5.sp,
                color = if (active) Ink else Muted,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
            )
            if (count != null && count > 0) {
                Spacer(Modifier.width(4.dp))
                Text("$count", fontSize = 11.sp, color = Color(0xFFB8B8B8))
            }
        }
        Spacer(Modifier.height(9.dp))
        Box(
            Modifier.height(2.dp).width(if (active) 34.dp else 0.dp)
                .background(if (active) Teal else Color.Transparent)
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(label, fontSize = 14.sp, color = Muted, modifier = Modifier.width(64.dp))
            Text(value, fontSize = 14.5.sp, color = Ink, lineHeight = 22.sp)
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(HairLine))
    }
}
