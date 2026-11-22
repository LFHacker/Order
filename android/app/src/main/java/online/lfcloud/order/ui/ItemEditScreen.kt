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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.model.Categories
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal
import online.lfcloud.order.ui.theme.Bg
import online.lfcloud.order.ui.theme.Ink2

@Composable
fun ItemEditScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val isNew = vm.editingItemId == null
    val path = vm.pathOf(vm.itemPositionId)

    Column(Modifier.fillMaxSize().background(Bg)) {

        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text(
                if (isNew) "新建物品" else "编辑物品",
                fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center)
            )
            Text(
                "保存", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                color = if (vm.itName.isBlank()) MutedLight else Teal,
                softWrap = false, maxLines = 1,
                modifier = Modifier.align(Alignment.CenterEnd).padding(horizontal = 16.dp)
                    .clickable { if (vm.saveItem()) onSaved() }
            )
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {

            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row {
                    MiniLabel("名称")
                    Spacer(Modifier.width(3.dp))
                    Text("*", fontSize = 12.5.sp, color = Danger)
                }
                Text("${vm.itName.length} / 40", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
                if (vm.itName.isEmpty()) {
                }
                BasicTextField(
                    value = vm.itName,
                    onValueChange = { if (it.length <= 40) { vm.itName = it; vm.itError = null } },
                    singleLine = true,
                    textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                    cursorBrush = SolidColor(Teal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
            if (vm.itError != null) {
                Spacer(Modifier.height(6.dp))
                Text(vm.itError!!, fontSize = 11.5.sp, color = Danger)
            }

            Spacer(Modifier.height(20.dp))
            MiniLabel("类别")
            Spacer(Modifier.height(9.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Categories.all.forEach { c ->
                    val on = vm.itCategory == c
                    Box(
                        Modifier.height(32.dp).clip(RoundedCornerShape(8.dp))
                            .background(if (on) Color(0xFFF0FBF9) else Color.White)
                            .border(1.dp, if (on) Teal else HairLine, RoundedCornerShape(8.dp))
                            .clickable { vm.itCategory = c }
                            .padding(horizontal = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(c, fontSize = 13.sp,
                            color = if (on) Color(0xFF0B8474) else Ink2,
                            fontWeight = if (on) FontWeight.Medium else FontWeight.Normal)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            MiniLabel("位置")
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                path.forEachIndexed { i, seg ->
                    if (i > 0) Text(" / ", fontSize = 14.sp, color = Muted)
                    Text(seg, fontSize = 14.sp,
                        color = if (i == path.lastIndex) Ink else Muted,
                        fontWeight = if (i == path.lastIndex) FontWeight.Medium else FontWeight.Normal)
                }
            }
            Spacer(Modifier.height(14.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            Spacer(Modifier.height(20.dp))
            MiniLabel("数量")
            Spacer(Modifier.height(4.dp))
            Row(
                Modifier.fillMaxWidth().height(48.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.weight(1f).fillMaxHeight(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = vm.itQuantity,
                        onValueChange = { v ->
                            if (v.isEmpty() || v.all { it.isDigit() } && v.length <= 4) vm.itQuantity = v
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                        cursorBrush = SolidColor(Teal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Box(Modifier.width(1.dp).height(22.dp).background(HairLine))
                Box(
                    Modifier.weight(1f).fillMaxHeight().padding(start = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = vm.itUnit,
                        onValueChange = { if (it.length <= 4) vm.itUnit = it },
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                        cursorBrush = SolidColor(Teal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                MiniLabel("备注")
                Text("${vm.itNote.length} / 200", fontSize = 12.5.sp, color = MutedLight)
            }
            Spacer(Modifier.height(4.dp))
            Box(Modifier.fillMaxWidth().height(80.dp)) {
                if (vm.itNote.isEmpty()) {
                }
                BasicTextField(
                    value = vm.itNote,
                    onValueChange = { if (it.length <= 200) vm.itNote = it },
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
