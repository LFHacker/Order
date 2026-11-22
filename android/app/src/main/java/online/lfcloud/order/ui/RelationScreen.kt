package online.lfcloud.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.Categories
import online.lfcloud.order.model.Item
import online.lfcloud.order.model.Relation
import online.lfcloud.order.model.RelationTypes
import online.lfcloud.order.model.relativeText
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal
import online.lfcloud.order.ui.theme.Bg
import online.lfcloud.order.ui.theme.Soft2
import online.lfcloud.order.ui.theme.Ink2

@Composable
fun RelationScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onOpenItem: (Item) -> Unit
) {
    val item = vm.currentItem() ?: return
    val list = vm.relationsOf(item.id)
    var showAdd by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf<Relation?>(null) }

    Column(Modifier.fillMaxSize().background(Bg)) {

        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(item.name, fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
            Text("添加", fontSize = 14.sp, color = Teal,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .clickable { showAdd = true })
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("关系", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("${list.size} 条关系", fontSize = 12.5.sp, color = Muted)
            Spacer(Modifier.height(10.dp))
        }

        if (list.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(60.dp))
                Text("还没有关系", fontSize = 15.sp, color = Ink)
                Spacer(Modifier.height(8.dp))
                
                Spacer(Modifier.height(20.dp))
                Text("添加关系", fontSize = 14.sp, color = Teal, fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showAdd = true })
            }
        } else {
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                list.forEach { rel ->
                    val other = vm.otherSide(rel, item.id)
                    Column {
                        Row(
                            Modifier.fillMaxWidth()
                                .combinedClickable(
                                    onClick = { other?.let { onOpenItem(it) } },
                                    onLongClick = { confirmDelete = rel }
                                )
                                .padding(horizontal = 16.dp, vertical = 13.dp)
                        ) {
                            Box(
                                Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                                    .background(Soft2),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    IconLibrary.vector(Categories.iconOf(other?.category ?: "其他")),
                                    contentDescription = null, tint = Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(11.dp))
                            Column(Modifier.weight(1f)) {
                                Text(other?.name ?: "对方已删除", fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Medium, color = Ink)
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    vm.pathOf(other?.positionId).joinToString(" / ")
                                        .ifEmpty { "—" },
                                    fontSize = 12.sp, color = Muted
                                )
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        Modifier.clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFFF0FBF9)).padding(
                                                horizontal = 6.dp, vertical = 1.dp
                                            )
                                    ) {
                                        Text(rel.type, fontSize = 10.5.sp, color = Color(0xFF0B8474))
                                    }
                                    if (rel.note.isNotBlank()) {
                                        Spacer(Modifier.width(6.dp))
                                        Text(rel.note, fontSize = 12.5.sp, color = Ink2)
                                    }
                                }
                                Spacer(Modifier.height(6.dp))
                                Text("${rel.createdAt.relativeText()}建立",
                                    fontSize = 11.5.sp, color = MutedLight)
                            }
                        }
                        
                        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp)
                            .background(HairLine))
                    }
                }
                Spacer(Modifier.height(30.dp))
            }
        }
    }

    if (showAdd) {
        AddRelationDialog(
            candidates = vm.relationCandidates(item.id),
            onPick = { target, type, note ->
                vm.addRelation(item.id, target.id, type, note)
                showAdd = false
            },
            onDismiss = { showAdd = false }
        )
    }

    confirmDelete?.let { rel ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = Bg,
            title = { Text("解除这条关系？", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink) },
            text = {
                Text("只解除「$item.name」和对方之间的关联，两边的物品都不会被删除。",
                    fontSize = 13.sp, color = Muted, lineHeight = 20.sp)
            },
            confirmButton = {
                TextButton(onClick = { vm.deleteRelation(rel.id); confirmDelete = null }) {
                    Text("解除", fontSize = 14.sp, color = Danger, fontWeight = FontWeight.SemiBold)
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
private fun AddRelationDialog(
    candidates: List<Item>,
    onPick: (Item, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var type by remember { mutableStateOf(RelationTypes.first()) }
    var selected by remember { mutableStateOf<Item?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Bg,
        title = { Text("添加关系", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink) },
        text = {
            Column {
                Text("关系类型", fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(7.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                    RelationTypes.forEach { t ->
                        val on = type == t
                        Box(
                            Modifier.clip(RoundedCornerShape(7.dp))
                                .background(if (on) Color(0xFFF0FBF9) else Soft2)
                                .clickable { type = t }
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(t, fontSize = 12.sp,
                                color = if (on) Color(0xFF0B8474) else Ink2,
                                fontWeight = if (on) FontWeight.Medium else FontWeight.Normal)
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
                Text(if (candidates.isEmpty()) "没有可关联的物品" else "选一件物品",
                    fontSize = 12.sp, color = Muted)
                Spacer(Modifier.height(6.dp))
                Column(Modifier.heightIn(max = 220.dp).verticalScroll(rememberScrollState())) {
                    candidates.forEach { c ->
                        val on = selected?.id == c.id
                        Row(
                            Modifier.fillMaxWidth().clickable { selected = c }
                                .padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                IconLibrary.vector(Categories.iconOf(c.category)),
                                contentDescription = null, tint = Muted,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(9.dp))
                            Text(c.name, fontSize = 14.sp, color = Ink, modifier = Modifier.weight(1f))
                            if (on) Text("已选", fontSize = 11.sp, color = Teal)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selected?.let { onPick(it, type, "") } },
                enabled = selected != null
            ) {
                Text("添加", fontSize = 14.sp, fontWeight = FontWeight.SemiBold,
                    color = if (selected != null) Teal else MutedLight)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", fontSize = 14.sp, color = Muted) }
        }
    )
}
