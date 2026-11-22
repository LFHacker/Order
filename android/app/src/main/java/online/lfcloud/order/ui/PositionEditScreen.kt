package online.lfcloud.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Ink2
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal

private val Presets = listOf("上层", "中层", "下层", "左侧", "右侧", "内侧", "外侧", "顶层", "底层")

@Composable
fun PositionEditScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val isNew = vm.editingPositionId == null
    val module = vm.modules.firstOrNull { it.id == vm.positionModuleId }
    val workspace = vm.workspaces.firstOrNull { it.id == module?.workspaceId }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(
                if (isNew) "新建方位" else "编辑方位",
                fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                "保存",
                fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                color = if (vm.posName.isBlank()) MutedLight else Teal,
                softWrap = false, maxLines = 1,
                modifier = Modifier.align(Alignment.CenterEnd).padding(horizontal = 16.dp)
                    .clickable { if (vm.savePosition()) onSaved() }
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(10.dp))
            MiniLabel("所属模块")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val icon = IconLibrary.find(module?.iconId)
                if (icon != null) {
                    Icon(IconLibrary.vector(icon.id), contentDescription = null,
                        tint = Ink2, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(module?.name ?: "", fontSize = 14.sp, color = Ink, fontWeight = FontWeight.Medium)
                Text(
                    "  ·  ${workspace?.name ?: ""} · 已有 ${vm.positionCountOf(module?.id)} 个方位",
                    fontSize = 13.sp, color = Muted
                )
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    MiniLabel("名称")
                    Spacer(Modifier.width(3.dp))
                    Text("*", fontSize = 12.5.sp, color = Danger)
                }
                Text("${vm.posName.length} / 40", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
                if (vm.posName.isEmpty()) {
                }
                BasicTextField(
                    value = vm.posName,
                    onValueChange = { if (it.length <= 40) { vm.posName = it; vm.posError = null } },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                    cursorBrush = SolidColor(Teal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
            if (vm.posError != null) {
                Spacer(Modifier.height(6.dp))
                Text(vm.posError!!, fontSize = 11.5.sp, color = Danger)
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniLabel("常用")
            }
            Spacer(Modifier.height(9.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Presets.chunked(5).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { preset ->
                            val selected = vm.posName == preset
                            Box(
                                Modifier.height(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (selected) Color(0xFFF0FBF9) else Color.White)
                                    .border(1.dp, if (selected) Teal else Color(0xFFEFEFEF), RoundedCornerShape(8.dp))
                                    .clickable { vm.posName = preset; vm.posError = null }
                                    .padding(horizontal = 13.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    preset, fontSize = 13.sp,
                                    color = if (selected) Color(0xFF0B8474) else Ink2,
                                    fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            MiniLabel("位置")
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("排在最后", fontSize = 14.sp, color = Ink)
                Text("第 ${vm.positionCountOf(module?.id) + if (isNew) 1 else 0} 个",
                    fontSize = 14.sp, color = Muted)
            }
            Spacer(Modifier.height(10.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
            Spacer(Modifier.height(40.dp))
        }
    }
}
