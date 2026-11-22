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
import online.lfcloud.order.WorkspaceViewModel
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Ink2
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal
import online.lfcloud.order.ui.theme.TealSofter

private val QuickIcons = listOf("home", "building", "house", "briefcase", "box")

@Composable
fun WorkspaceEditScreen(
    vm: WorkspaceViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val isNew = vm.editingId == null

    if (showPicker) {
        IconPickerScreen(
            selected = vm.formIcon,
            onPick = {
                vm.formIcon = it
                showPicker = false
            },
            onBack = { showPicker = false }
        )
        return
    }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        // 顶栏：标题居中，保存横排不换行
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack,
                contentDescription = "返回",
                tint = Ink,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .size(22.dp)
                    .clickable(onClick = onBack)
            )
            Text(
                if (isNew) "新建工作区" else "编辑工作区",
                fontSize = 16.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                "保存",
                fontSize = 14.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (vm.formName.isBlank()) MutedLight else Teal,
                softWrap = false,
                maxLines = 1,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 16.dp)
                    .clickable { if (vm.save()) onSaved() }
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(14.dp))

            // 名称
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    MiniLabel("名称")
                    Spacer(Modifier.width(3.dp))
                    Text("*", fontSize = 12.5.sp, color = Danger)
                }
                Text("${vm.formName.length} / 40", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
                if (vm.formName.isEmpty()) {
                    Text("例如：家", fontSize = 16.sp, color = MutedLight)
                }
                BasicTextField(
                    value = vm.formName,
                    onValueChange = {
                        if (it.length <= 40) {
                            vm.formName = it
                            vm.nameError = null
                        }
                    },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                    cursorBrush = SolidColor(Teal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
            if (vm.nameError != null) {
                Spacer(Modifier.height(6.dp))
                Text(vm.nameError!!, fontSize = 11.5.sp, color = Danger)
            }

            // 图标
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniLabel("图标")
                Text("可选", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                IconCell(
                    selected = vm.formIcon == null,
                    modifier = Modifier.weight(1f),
                    onClick = { vm.formIcon = null }
                ) {
                    Text("无", fontSize = 11.5.sp, color = MutedLight)
                }
                QuickIcons.forEach { id ->
                    IconCell(
                        selected = vm.formIcon == id,
                        modifier = Modifier.weight(1f),
                        onClick = { vm.formIcon = if (vm.formIcon == id) null else id }
                    ) {
                        Icon(
                            IconLibrary.vector(id),
                            contentDescription = null,
                            tint = if (vm.formIcon == id) Teal else Ink2,
                            modifier = Modifier.size(20.dp)
                        )
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

            // 描述
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniLabel("描述")
                Text("${vm.formDesc.length} / 200", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(76.dp)) {
                if (vm.formDesc.isEmpty()) {
                    Text("例如：主卧、客厅与阳台的收纳区域", fontSize = 15.sp, color = MutedLight)
                }
                BasicTextField(
                    value = vm.formDesc,
                    onValueChange = { if (it.length <= 200) vm.formDesc = it },
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

@Composable
fun MiniLabel(text: String) {
    Text(text, fontSize = 12.5.sp, color = Muted)
}

@Composable
fun IconCell(
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TealSofter else Color.White)
            .border(1.dp, if (selected) Teal else HairLine, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { content() }
}
