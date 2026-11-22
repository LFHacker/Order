package online.lfcloud.order.ui

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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.WorkspaceViewModel
import online.lfcloud.order.model.Workspace
import online.lfcloud.order.model.relativeText
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Ink2
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal
import androidx.compose.foundation.ExperimentalFoundationApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WorkspaceListScreen(
    vm: WorkspaceViewModel,
    onOpenEdit: (String?) -> Unit,
    onOpenSortFilter: () -> Unit,
    onDeleteRequest: (Workspace) -> Unit
) {
    var searching by remember { mutableStateOf(false) }
    val visible = vm.visible()
    val recent = visible.filter { System.currentTimeMillis() - it.updatedAt < 7L * 24 * 3600 * 1000 }
    val earlier = visible.filter { System.currentTimeMillis() - it.updatedAt >= 7L * 24 * 3600 * 1000 }

    Column(Modifier.fillMaxSize().background(androidx.compose.ui.graphics.Color.White)) {

        if (searching) {
            SearchBar(
                query = vm.query,
                onQueryChange = { vm.query = it },
                onCancel = {
                    searching = false
                    vm.query = ""
                }
            )
        } else {
            ListHeader(
                count = vm.workspaces.size,
                items = vm.totalItems(),
                onSearch = { searching = true },
                onCreate = { onOpenEdit(null) }
            )
            Toolbar(
                sortLabel = vm.sort.label,
                filterCount = vm.filter.activeCount,
                onSortFilter = onOpenSortFilter
            )
        }

        when {
            vm.workspaces.isEmpty() -> EmptyState(onCreate = { onOpenEdit(null) })

            visible.isEmpty() -> NoResultState {
                vm.query = ""
                vm.resetFilter()
            }

            else -> LazyColumn(Modifier.fillMaxSize()) {
                if (recent.isNotEmpty()) {
                    item { SectionHeader("最近使用") }
                    items(recent, key = { it.id }) { w ->
                        WorkspaceRow(
                            w = w,
                            onClick = { onOpenEdit(w.id) },
                            onRename = { onOpenEdit(w.id) },
                            onDelete = { onDeleteRequest(w) },
                            onSetCurrent = { vm.setCurrent(w.id) }
                        )
                    }
                }
                if (earlier.isNotEmpty()) {
                    item { SectionHeader("更早") }
                    items(earlier, key = { it.id }) { w ->
                        WorkspaceRow(
                            w = w,
                            onClick = { onOpenEdit(w.id) },
                            onRename = { onOpenEdit(w.id) },
                            onDelete = { onDeleteRequest(w) },
                            onSetCurrent = { vm.setCurrent(w.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ListHeader(count: Int, items: Int, onSearch: () -> Unit, onCreate: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 10.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        Column(Modifier.weight(1f)) {
            Text("工作区", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp, color = Ink)
            Spacer(Modifier.height(3.dp))
            Text("$count 个 · $items 件物品 · 已同步", fontSize = 12.5.sp, color = Muted)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(36.dp).clip(CircleShape).clickable(onClick = onSearch),
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
                Icon(Icons.Filled.Add, contentDescription = "新建工作区", tint = androidx.compose.ui.graphics.Color.White,
                    modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun Toolbar(sortLabel: String, filterCount: Int, onSortFilter: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                sortLabel,
                fontSize = 13.sp,
                color = Ink2,
                modifier = Modifier.clickable(onClick = onSortFilter)
            )
            Text(
                if (filterCount > 0) "筛选 · $filterCount" else "筛选",
                fontSize = 13.sp,
                fontWeight = if (filterCount > 0) FontWeight.Medium else FontWeight.Normal,
                color = if (filterCount > 0) Teal else Ink2,
                modifier = Modifier.clickable(onClick = onSortFilter)
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, onCancel: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 10.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            Modifier.weight(1f).height(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(androidx.compose.ui.graphics.Color(0xFFF6F6F6))
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Search, contentDescription = null, tint = Muted,
                modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("搜索工作区", fontSize = 14.sp, color = MutedLight)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 14.sp, color = Ink),
                    cursorBrush = SolidColor(Teal),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
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
private fun SectionHeader(text: String) {
    Text(
        text,
        fontSize = 12.5.sp,
        color = Muted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp)
    )
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
                            Icon(
                                IconLibrary.vector(icon.id),
                                contentDescription = null,
                                tint = Ink2,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(w.name, fontSize = 16.5.sp, fontWeight = FontWeight.Medium, color = Ink)
                        if (w.isCurrent) {
                            Spacer(Modifier.width(7.dp))
                            CurrentTag()
                        }
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "${w.itemCount} 件物品 · ${w.updatedAt.relativeText()}",
                        fontSize = 12.5.sp, color = Muted
                    )
                }
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null,
                    tint = androidx.compose.ui.graphics.Color(0xFFCFCFCF),
                    modifier = Modifier.size(18.dp))
            }

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("重命名") }, onClick = { menu = false; onRename() })
                DropdownMenuItem(text = { Text("设为当前") }, onClick = { menu = false; onSetCurrent() })
                DropdownMenuItem(text = { Text("删除", color = androidx.compose.ui.graphics.Color(0xFFC9503C)) },
                    onClick = { menu = false; onDelete() })
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(HairLine))
    }
}

@Composable
private fun CurrentTag() {
    Box(
        Modifier.clip(RoundedCornerShape(3.dp))
            .background(androidx.compose.ui.graphics.Color(0xFFF0FBF9))
            .padding(horizontal = 5.dp, vertical = 1.dp)
    ) {
        Text("当前", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Teal)
    }
}

@Composable
private fun EmptyState(onCreate: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(90.dp))
        Box(
            Modifier.size(96.dp).clip(RoundedCornerShape(30.dp)).background(Color0),
            contentAlignment = Alignment.Center
        ) {
            Icon(IconLibrary.vector("home"), contentDescription = null, tint = Teal,
                modifier = Modifier.size(44.dp))
        }
        Spacer(Modifier.height(22.dp))
        Text("还没有工作区", fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(9.dp))
        Text(
            "工作区是物品的容器。\n先建一个「家」或「公司」，再把物品放进去。",
            fontSize = 13.sp, color = Muted, lineHeight = 22.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(26.dp))
        Row(
            Modifier.width(196.dp).height(50.dp).clip(RoundedCornerShape(25.dp))
                .background(Teal).clickable(onClick = onCreate),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Add, contentDescription = null,
                tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("新建工作区", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                color = androidx.compose.ui.graphics.Color.White)
        }
    }
}

private val Color0 = androidx.compose.ui.graphics.Color(0xFFF0FBF9)

@Composable
private fun NoResultState(onReset: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("没有符合条件的工作区", fontSize = 15.sp, color = Ink2)
        Spacer(Modifier.height(8.dp))
        Text("试试放宽筛选条件", fontSize = 12.5.sp, color = Muted)
        Spacer(Modifier.height(18.dp))
        Text("清空筛选", fontSize = 14.sp, color = Teal, fontWeight = FontWeight.Medium,
            modifier = Modifier.clickable(onClick = onReset))
    }
}
