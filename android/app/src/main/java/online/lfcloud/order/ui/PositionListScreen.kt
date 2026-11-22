package online.lfcloud.order.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.Position
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.Teal
import kotlin.math.roundToInt

private val RowHeight = 62.dp

@Composable
fun PositionListScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onDeleteRequest: (Position) -> Unit
) {
    val module = vm.currentModule()
    val moduleId = module?.id
    val list = vm.positionsOf(moduleId)
    val density = LocalDensity.current
    val rowPx = with(density) { RowHeight.toPx() }

    var dragIndex by remember { mutableStateOf<Int?>(null) }
    var dragDelta by remember { mutableStateOf(0f) }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(
                module?.name ?: "",
                fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Icon(
                Icons.Filled.Add, contentDescription = "新建方位", tint = Ink,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .size(20.dp).clickable(onClick = onCreate)
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Text("方位", fontSize = 25.sp, fontWeight = FontWeight.Bold,
                letterSpacing = (-0.6).sp, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("${list.size} 个方位 · ${vm.itemsInModule(moduleId)} 件物品",
                fontSize = 12.5.sp, color = Muted)
            Spacer(Modifier.height(8.dp))
        }

        if (list.isNotEmpty()) {
            Text(
                "按住右侧手柄拖动可以调整顺序",
                fontSize = 12.sp, color = Color(0xFFB0B0B0),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp)
            )
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
        }

        if (list.isEmpty()) {
            Column(Modifier.fillMaxSize()) {
                EmptyHint(
                    title = "这个模块还没有方位",
                    desc = "方位是模块里的位置划分——\n抽屉有「第一层」，桌面有「左边」「右边」。",
                    cta = "新建方位",
                    iconId = "layers",
                    onCreate = onCreate
                )
            }
        } else {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                list.forEachIndexed { index, p ->
                    val dragging = dragIndex == index
                    Box(
                        Modifier
                            .zIndex(if (dragging) 1f else 0f)
                            .graphicsLayer { translationY = if (dragging) dragDelta else 0f }
                    ) {
                        PositionRow(
                            index = index,
                            position = p,
                            dragging = dragging,
                            onOpen = { onEdit(p.id) },
                            onRename = { onEdit(p.id) },
                            onDelete = { onDeleteRequest(p) },
                            onDragStart = { dragIndex = index; dragDelta = 0f },
                            onDrag = { dragDelta += it },
                            onDragEnd = {
                                val from = dragIndex
                                if (from != null && moduleId != null) {
                                    val target = (from + (dragDelta / rowPx).roundToInt())
                                        .coerceIn(0, list.size - 1)
                                    if (target != from) vm.reorderPositions(moduleId, from, target)
                                }
                                dragIndex = null
                                dragDelta = 0f
                            },
                            onDragCancel = { dragIndex = null; dragDelta = 0f }
                        )
                    }
                }
                Spacer(Modifier.height(40.dp))
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PositionRow(
    index: Int,
    position: Position,
    dragging: Boolean,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDragStart: () -> Unit,
    onDrag: (Float) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }

    Column {
        Box {
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(RowHeight)
                    .background(if (dragging) Color(0xFFFAFDFC) else Color.Transparent)
                    .combinedClickable(onClick = onOpen, onLongClick = { menu = true })
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${index + 1}",
                    fontSize = 12.5.sp, color = Color(0xFFB8B8B8),
                    modifier = Modifier.width(18.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(position.name, fontSize = 16.5.sp, fontWeight = FontWeight.Medium,
                    color = Ink, modifier = Modifier.weight(1f))
                Text(
                    if (position.itemCount > 0) "${position.itemCount} 件" else "空",
                    fontSize = 13.sp,
                    color = if (position.itemCount > 0) Muted else Color(0xFFC0C0C0)
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    Modifier
                        .size(28.dp)
                        .pointerInput(position.id) {
                            detectDragGesturesAfterLongPress(
                                onDragStart = { onDragStart() },
                                onDrag = { change, amount ->
                                    change.consume()
                                    onDrag(amount.y)
                                },
                                onDragEnd = { onDragEnd() },
                                onDragCancel = { onDragCancel() }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(Modifier.size(15.dp)) {
                        val stroke = 1.6.dp.toPx()
                        listOf(0.25f, 0.5f, 0.75f).forEach { fy ->
                            drawLine(
                                color = Color(0xFFD2D2D2),
                                start = androidx.compose.ui.geometry.Offset(0f, size.height * fy),
                                end = androidx.compose.ui.geometry.Offset(size.width, size.height * fy),
                                strokeWidth = stroke,
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        }
                    }
                }
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
