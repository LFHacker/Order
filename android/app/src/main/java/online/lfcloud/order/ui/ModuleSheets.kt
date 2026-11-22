package online.lfcloud.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import online.lfcloud.order.model.FilterState
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.Position
import online.lfcloud.order.model.ModuleSort
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Ink2
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.Teal
import online.lfcloud.order.ui.theme.TealDeep
import online.lfcloud.order.ui.theme.TealSofter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModuleSortFilterSheet(
    currentSort: ModuleSort,
    currentFilter: FilterState,
    onApply: (ModuleSort, FilterState) -> Unit,
    onDismiss: () -> Unit
) {
    var sort by remember { mutableStateOf(currentSort) }
    var filter by remember { mutableStateOf(currentFilter) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {

            Row(
                Modifier.fillMaxWidth().padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("排序与筛选", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text("重置", fontSize = 13.sp, color = Teal,
                    modifier = Modifier.clickable {
                        sort = ModuleSort.RECENT
                        filter = FilterState()
                    })
            }

            SheetLabel("排序")
            ModuleSort.entries.forEach { option ->
                SheetOption(option.label, sort == option) { sort = option }
            }

            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
            Spacer(Modifier.height(6.dp))

            SheetLabel("筛选")
            SheetOption("不限更新时间", filter.days == 0) { filter = filter.copy(days = 0) }
            listOf(1 to "今天", 7 to "近 7 天", 30 to "近 30 天").forEach { (d, label) ->
                SheetOption("更新时间：$label", filter.days == d) {
                    filter = filter.copy(days = if (filter.days == d) 0 else d)
                }
            }
            SheetOption("仅显示有物品的模块", filter.onlyWithItems) {
                filter = filter.copy(onlyWithItems = !filter.onlyWithItems, onlyEmpty = false)
            }
            SheetOption("仅显示空模块", filter.onlyEmpty) {
                filter = filter.copy(onlyEmpty = !filter.onlyEmpty, onlyWithItems = false)
            }

            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
                SheetButton("重置", false, Modifier.weight(1f)) {
                    sort = ModuleSort.RECENT
                    filter = FilterState()
                }
                SheetButton(
                    if (filter.activeCount > 0) "应用（${filter.activeCount}）" else "应用",
                    true, Modifier.weight(1f)
                ) { onApply(sort, filter) }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun SheetLabel(text: String) {
    Text(text, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Muted,
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp))
}

@Composable
private fun SheetOption(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().height(44.dp).clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text, fontSize = 14.sp,
            color = if (selected) TealDeep else Ink2,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
        if (selected) {
            Icon(Icons.Filled.Check, contentDescription = null, tint = Teal,
                modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SheetButton(text: String, filled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier.height(48.dp).clip(RoundedCornerShape(14.dp))
            .background(if (filled) Teal else TealSofter)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
            color = if (filled) Color.White else TealDeep)
    }
}

@Composable
fun DeleteModuleDialog(
    target: Module,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("删除「${target.name}」？",
                fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Ink)
        },
        text = {
            Column {
                Text(
                    "该模块下有 ${target.positionCount} 个方位、${target.itemCount} 件物品。\n" +
                        "删除后模块与其中的物品一起进入回收站，30 天内可恢复。",
                    fontSize = 13.sp, color = Muted, lineHeight = 21.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
                Spacer(Modifier.height(12.dp))
                Text(
                    "如果只想取消这个模块、物品还想留着，先把物品移到其它模块再删。",
                    fontSize = 11.5.sp, color = Muted, lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", fontSize = 14.sp, color = TealDeep)
            }
        }
    )
}

@Composable
fun DeletePositionDialog(
    target: Position,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Text("删除「${target.name}」？",
                fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Ink)
        },
        text = {
            Column {
                Text(
                    "该方位下有 ${target.itemCount} 件物品。\n" +
                        "删除后方位与其中的物品一起进入回收站，30 天内可恢复。",
                    fontSize = 13.sp, color = Muted, lineHeight = 21.sp
                )
                Spacer(Modifier.height(12.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
                Spacer(Modifier.height(12.dp))
                Text(
                    "和工作区、模块的删除一致：整块进回收站，不影响同一模块里的其它方位。",
                    fontSize = 11.5.sp, color = Muted, lineHeight = 18.sp
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Danger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", fontSize = 14.sp, color = TealDeep)
            }
        }
    )
}
