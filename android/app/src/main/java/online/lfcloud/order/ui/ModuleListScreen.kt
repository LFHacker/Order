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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.FilterState
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.relativeText
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Ink2
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.Teal

@Composable
fun ModuleListScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onOpenPositions: (Module) -> Unit,
    onEdit: (String) -> Unit,
    onOpenSortFilter: () -> Unit,
    onDeleteRequest: (Module) -> Unit
) {
    var searching by remember { mutableStateOf(false) }
    val workspace = vm.currentWorkspace()
    val wsId = workspace?.id
    val visible = vm.visibleModules(wsId)
    val now = System.currentTimeMillis()
    val recent = visible.filter { now - it.updatedAt < 7L * 24 * 3600 * 1000 }
    val earlier = visible.filter { now - it.updatedAt >= 7L * 24 * 3600 * 1000 }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        // 顶栏：返回 + 工作区名 + 搜索/新建
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(
                workspace?.name ?: "",
                fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Row(
                Modifier.align(Alignment.CenterEnd).padding(end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(Icons.Filled.Search, contentDescription = "搜索", tint = Ink,
                    modifier = Modifier.size(20.dp).clickable { searching = true })
                Icon(Icons.Filled.Add, contentDescription = "新建模块", tint = Ink,
                    modifier = Modifier.size(20.dp).clickable(onClick = onCreate))
            }
        }

        if (searching) {
            SimpleSearchBar(
                query = vm.moduleQuery,
                placeholder = "搜索模块",
                onQueryChange = { vm.moduleQuery = it },
                onCancel = { searching = false; vm.moduleQuery = "" }
            )
        } else {
            Column(Modifier.padding(horizontal = 16.dp)) {
                Text("模块", fontSize = 26.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.6).sp, color = Ink)
                Spacer(Modifier.height(3.dp))
                Text(
                    "${vm.moduleCount(wsId)} 个模块 · ${vm.itemsInWorkspace(wsId)} 件物品 · 已同步",
                    fontSize = 12.5.sp, color = Muted
                )
                Spacer(Modifier.height(10.dp))
            }
            SortFilterBar(
                sortLabel = vm.moduleSort.label,
                filterCount = vm.moduleFilter.activeCount,
                onOpen = onOpenSortFilter
            )
        }

        when {
            vm.moduleCount(wsId) == 0 -> EmptyHint(
                title = "这个工作区还没有模块",
                cta = "新建模块",
                iconId = "box",
                onCreate = onCreate
            )

            visible.isEmpty() -> NoResultHint {
                vm.moduleQuery = ""
                vm.moduleFilter = FilterState()
            }

            else -> LazyColumn(Modifier.fillMaxSize()) {
                if (recent.isNotEmpty()) {
                    item { SectionHeader("最近使用") }
                    items(recent, key = { it.id }) { m ->
                        ModuleRow(m,
                            posCount = vm.positionCountOf(m.id),
                            onClick = { onOpenPositions(m) },
                            onRename = { onEdit(m.id) },
                            onDelete = { onDeleteRequest(m) })
                    }
                }
                if (earlier.isNotEmpty()) {
                    item { SectionHeader("更早") }
                    items(earlier, key = { it.id }) { m ->
                        ModuleRow(m,
                            posCount = vm.positionCountOf(m.id),
                            onClick = { onOpenPositions(m) },
                            onRename = { onEdit(m.id) },
                            onDelete = { onDeleteRequest(m) })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ModuleRow(
    m: Module,
    posCount: Int,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
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
                        val icon = IconLibrary.find(m.iconId)
                        if (icon != null) {
                            Icon(IconLibrary.vector(icon.id), contentDescription = null,
                                tint = Ink2, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                        }
                        Text(m.name, fontSize = 16.5.sp, fontWeight = FontWeight.Medium, color = Ink)
                    }
                    Spacer(Modifier.height(3.dp))
                    Text(
                        moduleMeta(m, posCount),
                        fontSize = 12.5.sp, color = Muted
                    )
                }
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null,
                    tint = Color(0xFFCFCFCF), modifier = Modifier.size(18.dp))
            }

            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                DropdownMenuItem(text = { Text("重命名") }, onClick = { menu = false; onRename() })
                DropdownMenuItem(
                    text = { Text("删除", color = Danger) },
                    onClick = { menu = false; onDelete() }
                )
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(HairLine))
    }
}

fun moduleMeta(m: Module, posCount: Int): String {
    val pos = if (posCount > 0) "$posCount 个方位" else "0 个方位"
    val items = if (m.itemCount > 0) "${m.itemCount} 件物品" else "空模块"
    return "$pos · $items · ${m.updatedAt.relativeText()}"
}
