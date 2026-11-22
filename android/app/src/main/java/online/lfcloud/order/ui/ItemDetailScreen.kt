package online.lfcloud.order.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
    val images = attachments.filter { it.kind == "image" }
    val attCount = attachments.size
    val relCount = vm.relationsOf(item.id).size
    val evCount = vm.eventsOf(item.id).size
    val path = vm.pathOf(item.positionId)

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
            vm.addAttachment(
                item.id,
                queryName(context, uri) ?: "图片",
                uri.toString(),
                "image"
            )
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
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(220.dp)
                    .clip(RoundedCornerShape(14.dp))
            ) {
                val first = images.firstOrNull()
                val bmp = rememberUriBitmap(first?.uri)
                if (bmp != null) {
                    Image(bmp, contentDescription = null, contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(14.dp))
                            .clickable { onOpenAttachments() })
                    Box(
                        Modifier.align(Alignment.BottomEnd).padding(10.dp)
                            .clip(RoundedCornerShape(6.dp)).background(Color(0x99000000))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("${images.size}", fontSize = 11.sp, color = Color.White)
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

            Text(
                item.name, fontSize = 22.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp, color = Ink,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp)
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
