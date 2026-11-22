package online.lfcloud.order.ui

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.Categories
import online.lfcloud.order.model.Item
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
    val item = vm.currentItem() ?: return
    var menu by remember { mutableStateOf(false) }
    val attCount = vm.attachmentsOf(item.id).size
    val relCount = vm.relationsOf(item.id).size
    val evCount = vm.eventsOf(item.id).size
    val path = vm.pathOf(item.positionId)

    Column(Modifier.fillMaxSize().background(Color.White)) {

        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text("物品详情", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
            Box(Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)) {
                Icon(Icons.Filled.MoreVert, contentDescription = "更多", tint = Ink,
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

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {

            // 大图 / 类别块
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(118.dp)
                    .clip(RoundedCornerShape(12.dp)).background(Color(0xFFF2F7F6)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    IconLibrary.vector(Categories.iconOf(item.category)),
                    contentDescription = null, tint = Color(0xFF7FA39C),
                    modifier = Modifier.size(44.dp)
                )
                Box(
                    Modifier.align(Alignment.BottomStart).padding(9.dp)
                        .clip(RoundedCornerShape(5.dp)).background(Color(0x8C111111))
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(item.category, fontSize = 11.sp, color = Color.White)
                }
            }

            Text(item.name, fontSize = 21.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.3).sp, color = Ink,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 11.dp))

            Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically) {
                path.forEachIndexed { i, seg ->
                    if (i > 0) Text(" / ", fontSize = 13.sp, color = Muted)
                    Text(
                        seg, fontSize = 13.sp,
                        color = if (i == path.lastIndex) Ink else Muted,
                        fontWeight = if (i == path.lastIndex) FontWeight.Medium else FontWeight.Normal
                    )
                }
            }

            // 横向入口条
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EntryTab("基本信息", null, true, {})
                EntryTab("附件", attCount, false, onOpenAttachments)
                EntryTab("关系", relCount, false, onOpenRelations)
                EntryTab("事件", evCount, false, onOpenEvents)
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            // 横排指标
            Row(Modifier.fillMaxWidth().padding(top = 18.dp, bottom = 4.dp)) {
                StatCell(item.category, "类别", Modifier.weight(1f))
                StatCell("${item.quantity} ${item.quantityUnit}", "数量", Modifier.weight(1f))
                StatCell(
                    vm.eventsOf(item.id).firstOrNull { it.type == "购入" }?.let { "已记录" } ?: "未记录",
                    "购入", Modifier.weight(1f)
                )
            }

            SectionLabel("备注")
            Box(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8FAFA))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                Text(
                    item.note.ifBlank { "还没有写备注" },
                    fontSize = 14.sp,
                    color = if (item.note.isBlank()) Color(0xFFB4B4B4) else Color(0xFF3A3A3A),
                    lineHeight = 22.sp
                )
            }

            SectionLabel("状态")
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(10.dp)).background(Color(0xFFF8FAFA))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(7.dp).clip(RoundedCornerShape(4.dp)).background(Teal))
                Spacer(Modifier.width(8.dp))
                Text(item.status, fontSize = 14.sp, color = Ink)
                Spacer(Modifier.weight(1f))
                Text(
                    vm.eventsOf(item.id).firstOrNull()?.let { "最近：${it.type}" } ?: "暂无事件",
                    fontSize = 12.5.sp, color = Muted
                )
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun EntryTab(label: String, count: Int?, active: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.padding(end = 22.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    label, fontSize = 14.sp,
                    color = if (active) Ink else Muted,
                    fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
                )
                if (count != null) {
                    Spacer(Modifier.width(3.dp))
                    Text("$count", fontSize = 11.sp, color = Color(0xFFB8B8B8))
                    Text(" ›", fontSize = 12.sp, color = Color(0xFFC6C6C6))
                }
            }
        }
    }
}

@Composable
private fun StatCell(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Ink)
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.5.sp, color = Muted)
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontSize = 12.5.sp, color = Muted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp))
}
