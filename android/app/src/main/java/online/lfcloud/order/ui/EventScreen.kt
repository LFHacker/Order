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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.EventTypes
import online.lfcloud.order.model.ItemEvent
import online.lfcloud.order.model.relativeText
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal

@Composable
fun EventScreen(
    vm: OrderViewModel,
    onBack: () -> Unit
) {
    val item = vm.currentItem() ?: return
    val list = vm.eventsOf(item.id)
    var showAdd by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<ItemEvent?>(null) }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(item.name, fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
            Text("记一笔", fontSize = 14.sp, color = Teal,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .clickable { showAdd = true })
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("事件", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "${list.size} 条记录 · ${vm.pathOf(item.positionId).joinToString(" / ")}",
                fontSize = 12.5.sp, color = Muted
            )
            Spacer(Modifier.height(12.dp))
        }

        if (list.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(60.dp))
                Text("还没有记录", fontSize = 15.sp, color = Ink)
                Spacer(Modifier.height(8.dp))
                Text(
                    "记下购入、借出、维修、盘点，\n以后能查到这件东西都经历过什么。",
                    fontSize = 12.5.sp, color = Muted, lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))
                Text("记一笔", fontSize = 14.sp, color = Teal, fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showAdd = true })
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                list.forEachIndexed { index, ev ->
                    Row(Modifier.padding(start = 16.dp, end = 16.dp)) {
                        Column(
                            Modifier.width(9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(Modifier.height(7.dp))
                            Box(
                                Modifier.size(8.dp).clip(RoundedCornerShape(4.dp))
                                    .background(if (index == 0) Teal else Color(0xFFCFCFCF))
                            )
                            if (index != list.lastIndex) {
                                Box(Modifier.width(1.dp).height(46.dp)
                                    .background(Color(0xFFE6E6E6)))
                            }
                        }
                        Spacer(Modifier.width(11.dp))
                        Column(Modifier.weight(1f).padding(bottom = 14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(ev.type, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                                    color = Ink)
                                if (index == 0) {
                                    Spacer(Modifier.width(7.dp))
                                    Box(
                                        Modifier.clip(RoundedCornerShape(3.dp))
                                            .background(Color(0xFFF0FBF9))
                                            .padding(horizontal = 5.dp, vertical = 1.dp)
                                    ) {
                                        Text("最新", fontSize = 10.sp, color = Teal)
                                    }
                                }
                            }
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "${ev.at.relativeText()} · ${formatDate(ev.at)}",
                                fontSize = 12.5.sp, color = Muted
                            )
                            if (ev.note.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(ev.note, fontSize = 12.5.sp, color = Color(0xFF3A3A3A))
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("删除这条", fontSize = 11.5.sp, color = Danger,
                                modifier = Modifier.clickable { confirmDelete = ev })
                        }
                    }
                    if (index != list.lastIndex) {
                        Box(Modifier.fillMaxWidth().padding(start = 36.dp).height(1.dp)
                            .background(HairLine))
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    if (showAdd) {
        AddEventDialog(
            onSave = { type, note ->
                vm.addEvent(item.id, type, note)
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }

    confirmDelete?.let { ev ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = Color.White,
            title = { Text("删除这条记录？", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink) },
            text = { Text("只删这条「${ev.type}」记录，物品本身不受影响。",
                fontSize = 13.sp, color = Muted, lineHeight = 20.sp) },
            confirmButton = {
                TextButton(onClick = { vm.deleteEvent(ev.id); confirmDelete = null }) {
                    Text("删除", fontSize = 14.sp, color = Danger, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text("取消", fontSize = 14.sp, color = Teal)
                }
            }
        )
    }
}

@Composable
private fun AddEventDialog(
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(EventTypes.all.first()) }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("记一笔", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink) },
        text = {
            Column {
                Text("事情", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    EventTypes.all.forEach { t ->
                        val on = type == t
                        Box(
                            Modifier.clip(RoundedCornerShape(7.dp))
                                .background(if (on) Color(0xFFF0FBF9) else Color(0xFFF6F6F6))
                                .clickable { type = t }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        ) {
                            Text(t, fontSize = 12.sp,
                                color = if (on) Color(0xFF0B8474) else Color(0xFF3A3A3A),
                                fontWeight = if (on) FontWeight.Medium else FontWeight.Normal)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text("备注（可选）", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(6.dp))
                Box(
                    Modifier.fillMaxWidth().height(64.dp).clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFA)).padding(10.dp)
                ) {
                    if (note.isEmpty()) {
                        Text("例如：借给王工，说好下周还", fontSize = 13.sp, color = MutedLight)
                    }
                    BasicTextField(
                        value = note,
                        onValueChange = { if (it.length <= 200) note = it },
                        textStyle = TextStyle(fontSize = 13.sp, color = Ink, lineHeight = 20.sp),
                        cursorBrush = SolidColor(Teal),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text("时间记为该事件的当前时间；借出会把物品标成「借出中」。",
                    fontSize = 11.sp, color = MutedLight, lineHeight = 17.sp)
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(type, note.trim()) }) {
                Text("保存", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Teal)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", fontSize = 14.sp, color = Muted) }
        }
    )
}
