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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.FilterState
import online.lfcloud.order.model.Workspace
import online.lfcloud.order.model.relativeText
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Ink2
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal
import online.lfcloud.order.ui.theme.TealSofter

@Composable
fun WorkspaceListScreen(
    vm: OrderViewModel,
    onOpenModules: (Workspace) -> Unit,
    onEdit: (String) -> Unit,
    onCreate: () -> Unit,
    onOpenSortFilter: () -> Unit,
    onDeleteRequest: (Workspace) -> Unit
) {
    var searching by remember { mutableStateOf(false) }
    val visible = vm.visibleWorkspaces()
    val now = System.currentTimeMillis()
    val recent = visible.filter { now - it.updatedAt < 7L * 24 * 3600 * 1000 }
    val earlier = visible.filter { now - it.updatedAt >= 7L * 24 * 3600 * 1000 }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        if (searching) {
            SimpleSearchBar(
                query = vm.query,
                placeholder = "搜索工作区",
                onQueryChange = { vm.query = it },
                onCancel = { searching = false; vm.query = "" }
            )
        } else {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(Modifier.weight(1f)) {
                    Text("工作区", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.6).sp, color = Ink)
                    Spacer(Modifier.height(3.dp))
                    Text("${vm.workspaces.size} 个 · ${vm.totalItems()} 件物品 · 已同步",
                        fontSize = 12.5.sp, color = Muted)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).clickable { searching = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索", tint = Ink,
                            modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(4.dp))
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(Teal)
                            .clickable(onClick = onCreate),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = "新建工作区", tint = Color.White,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }
            SortFilterBar(
                sortLabel = vm.sort.label,
                filterCount = vm.filter.activeCount,
                onOpen = onOpenSortFilter
            )
        }

        when {
            vm.workspaces.isEmpty() -> EmptyHint(
                title = "还没有工作区",
                cta = "新建工作区",
                iconId = "home",
                onCreate = onCreate
            )

            visible.isEmpty() -> NoResultHint {
                vm.query = ""
                vm.filter = FilterState()
            }

            else -> LazyColumn(Modifier.fillMaxSize()) {
                if (recent.isNotEmpty()) {
                    item { SectionHeader("最近使用") }
                    items(recent, key = { it.id }) { w ->
                        WorkspaceRow(
                            w = w,
                            onClick = { onOpenModules(w) },
                            onRename = { onEdit(w.id) },
                            onDelete = { onDeleteRequest(w) },
                            onSetCurrent = { vm.setCurrentWorkspace(w.id) }
                        )
                    }
                }
                if (earlier.isNotEmpty()) {
                    item { SectionHeader("更早") }
                    items(earlier, key = { it.id }) { w ->
                        WorkspaceRow(
                            w = w,
                            onClick = { onOpenModules(w) },
                            onRename = { onEdit(w.id) },
                            onDelete = { onDeleteRequest(w) },
                            onSetCurrent = { vm.setCurrentWorkspace(w.id) }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WorkspaceRow(
    w: Workspace,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onSetCurrent: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Column {
        Box {
            Row(
                Modifier.fillMaxWidth()
                    .combinedClickable(onClick = onClick, onLongClick = { menu = true })
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = IconLibrary.find(w.iconId)
                        if (icon != null) {
                            Icon(IconLibrary.vector(icon.id), contentDescription = null,
                                tint = Ink2, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(w.name, fontSize = 16.5.sp, fontWeight = FontWeight.Medium, color = Ink)
                        if (w.isCurrent) {
                            Spacer(Modifier.width(7.dp))
                            MiniTag("当前")
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text("${w.itemCount} 件物品 · ${w.updatedAt.relativeText()}",
                        fontSize = 12.5.sp, color = Muted)
                }
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null,
                    tint = Color(0xFFCFCFCF), modifier = Modifier.size(18.dp))
            }

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("重命名") }, onClick = { menu = false; onRename() })
                DropdownMenuItem(text = { Text("设为当前") }, onClick = { menu = false; onSetCurrent() })
                DropdownMenuItem(
                    text = { Text("删除", color = Danger) },
                    onClick = { menu = false; onDelete() }
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(HairLine))
    }
}

@Composable
fun MiniTag(text: String) {
    Box(
        Modifier.clip(RoundedCornerShape(3.dp)).background(TealSofter)
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text(text, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Teal)
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(text, fontSize = 12.5.sp, color = Muted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp))
}

@Composable
fun SortFilterBar(sortLabel: String, filterCount: Int, onOpen: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(sortLabel, fontSize = 13.sp, color = Ink2, modifier = Modifier.clickable(onClick = onOpen))
            Text(
                if (filterCount > 0) "筛选 · $filterCount" else "筛选",
                fontSize = 13.sp,
                fontWeight = if (filterCount > 0) FontWeight.Medium else FontWeight.Normal,
                color = if (filterCount > 0) Teal else Ink2,
                modifier = Modifier.clickable(onClick = onOpen)
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
    }
}

@Composable
fun SimpleSearchBar(
    query: String,
    placeholder: String,
    onQueryChange: (String) -> Unit,
    onCancel: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f).height(40.dp).clip(RoundedCornerShape(10.dp))
                .background(Color(0xFFF6F6F6)).padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Muted,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) Text(placeholder, fontSize = 14.sp, color = MutedLight)
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Ink),
                    cursorBrush = SolidColor(Teal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text("取消", fontSize = 14.sp, color = Teal, fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onCancel))
    }
}

@Composable
fun EmptyHint(
    title: String,
    cta: String,
    iconId: String,
    onCreate: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(90.dp))
        Box(
            Modifier.size(92.dp).clip(RoundedCornerShape(28.dp)).background(TealSofter),
            contentAlignment = Alignment.Center
        ) {
            Icon(IconLibrary.vector(iconId), contentDescription = null, tint = Teal,
                modifier = Modifier.size(42.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text(title, fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(24.dp))
        Row(
            Modifier.width(190.dp).height(50.dp).clip(RoundedCornerShape(25.dp))
                .background(Teal).clickable(onClick = onCreate),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, tint = Color.White,
                modifier = Modifier.size(17.dp))
            Spacer(Modifier.width(8.dp))
            Text(cta, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        }
    }
}

@Composable
fun NoResultHint(onReset: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("没有符合条件的结果", fontSize = 15.sp, color = Ink2)
        Spacer(Modifier.height(18.dp))
        Text("清空筛选", fontSize = 14.sp, color = Teal, fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onReset))
    }
}
