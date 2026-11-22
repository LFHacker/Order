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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import online.lfcloud.order.ui.theme.TealSofter

private val ModuleQuickIcons = listOf("monitor", "drawer", "chest", "shelf", "box")

@Composable
fun ModuleEditScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val isNew = vm.editingModuleId == null
    val workspace = vm.workspaces.firstOrNull { it.id == vm.moduleWorkspaceId }

    if (showPicker) {
        IconPickerScreen(
            selected = vm.mdIcon,
            onPick = { vm.mdIcon = it; showPicker = false },
            onBack = { showPicker = false }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(
                if (isNew) "新建模块" else "编辑模块",
                fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                "保存",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (vm.mdName.isBlank()) MutedLight else Teal,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterEnd).padding(horizontal = 16.dp)
                    .clickable { if (vm.saveModule()) onSaved() }
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(10.dp))
            MiniLabel("所属工作区")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val wsIcon = IconLibrary.find(workspace?.iconId)
                if (wsIcon != null) {
                    Icon(IconLibrary.vector(wsIcon.id), contentDescription = null,
                        tint = Ink2, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                }
                Text(
                    workspace?.name ?: "",
                    fontSize = 14.sp, color = Ink, fontWeight = FontWeight.Medium
                )
                Text(
                    "  ·  ${vm.moduleCount(workspace?.id)} 个模块",
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
                Text("${vm.mdName.length} / 40", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
                if (vm.mdName.isEmpty()) {
                }
                BasicTextField(
                    value = vm.mdName,
                    onValueChange = { if (it.length <= 40) { vm.mdName = it; vm.mdError = null } },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                    cursorBrush = SolidColor(Teal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
            if (vm.mdError != null) {
                Spacer(Modifier.height(6.dp))
                Text(vm.mdError!!, fontSize = 11.5.sp, color = Danger)
            }

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniLabel("图标")
            }
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                IconCell(
                    selected = vm.mdIcon == null,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.mdIcon = null }
                ) { Text("无", fontSize = 11.5.sp, color = MutedLight) }
                ModuleQuickIcons.forEach { id ->
                    IconCell(
                        selected = vm.mdIcon == id,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.mdIcon = if (vm.mdIcon == id) null else id }
                    ) {
                        Icon(IconLibrary.vector(id), contentDescription = null,
                            tint = if (vm.mdIcon == id) Teal else Ink2,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(11.dp))
            Row(
                Modifier.fillMaxWidth().height(44.dp).clickable { showPicker = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("全部 ${IconLibrary.all().size} 个图标", fontSize = 13.sp, color = Teal)
                Icon(Icons.Filled.KeyboardArrowRight, contentDescription = null,
                    tint = Teal, modifier = Modifier.size(18.dp))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniLabel("描述")
                Text("${vm.mdDesc.length} / 200", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(76.dp)) {
                if (vm.mdDesc.isEmpty()) {
                }
                BasicTextField(
                    value = vm.mdDesc,
                    onValueChange = { if (it.length <= 200) vm.mdDesc = it },
                    textStyle = TextStyle(fontSize = 15.sp, color = Ink, lineHeight = 24.sp),
                    cursorBrush = SolidColor(Teal),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            Spacer(Modifier.height(40.dp))

        }
    }
}
