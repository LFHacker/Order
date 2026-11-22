package online.lfcloud.order.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.Categories
import online.lfcloud.order.model.Item
import online.lfcloud.order.model.relativeText
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.Teal
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import online.lfcloud.order.ui.theme.Bg
import online.lfcloud.order.ui.theme.Soft2
import online.lfcloud.order.ui.theme.MutedLight

@Composable
fun ItemListScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpen: (Item) -> Unit,
    onEdit: (String) -> Unit,
    onDeleteRequest: (Item) -> Unit
) {
    val position = vm.currentPosition()
    val module = vm.modules.firstOrNull { it.id == position?.moduleId }
    val list = vm.itemsOf(position?.id)

    Column(Modifier.fillMaxSize().background(Bg)) {

        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(
                "${module?.name ?: ""} · ${position?.name ?: ""}",
                fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                Icons.Filled.Add, contentDescription = "新建物品", tint = Ink,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .size(20.dp).clickable(onClick = onCreate)
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("物品", fontSize = 25.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("${list.size} 件 · 已同步", fontSize = 12.5.sp, color = Muted)
            Spacer(Modifier.height(12.dp))
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

        if (list.isEmpty()) {
            EmptyHint(
                title = "这一格还空着",
                cta = "新建物品",
                iconId = "box",
                onCreate = onCreate
            )
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(list, key = { it.id }) { item ->
                    ItemRow(
                        item = item,
                        onClick = { onOpen(item) },
                        onEdit = { onEdit(item.id) },
                        onDelete = { onDeleteRequest(item) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ItemRow(
    item: Item,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Column {
        Box {
            Row(
                Modifier.fillMaxWidth()
                    .combinedClickable(onClick = onClick, onLongClick = { menu = true })
                    .padding(horizontal = 16.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(46.dp).clip(RoundedCornerShape(10.dp))
                        .background(Soft2),
                    contentAlignment = Alignment.Center
                ) {
                    val thumb = rememberUriBitmap(item.imageUri)
                    if (thumb != null) {
                        Image(thumb, contentDescription = null, contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize())
                    } else {
                        Icon(
                            IconLibrary.vector(Categories.iconOf(item.category)),
                            contentDescription = null, tint = Muted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(item.name, fontSize = 16.5.sp, fontWeight = FontWeight.Medium, color = Ink)
                        if (item.quantity > 1) {
                            Spacer(Modifier.width(6.dp))
                            Text("× ${item.quantity}", fontSize = 13.sp, color = Muted)
                        }
                        if (item.status == "借出中") {
                            Spacer(Modifier.width(7.dp))
                            MiniTag("借出中")
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text("${item.category} · ${item.updatedAt.relativeText()}",
                        fontSize = 12.5.sp, color = Muted)
                }
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null,
                    tint = MutedLight, modifier = Modifier.size(18.dp))
            }

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("编辑") }, onClick = { menu = false; onEdit() })
                DropdownMenuItem(
                    text = { Text("删除", color = Danger) },
                    onClick = { menu = false; onDelete() }
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(HairLine))
    }
}

