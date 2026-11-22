package online.lfcloud.order.ui

import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Teal
import online.lfcloud.order.ui.theme.TealDeep

private val Tabs = listOf("全部") + IconLibrary.groups

@Composable
fun IconPickerScreen(
    selected: String?,
    onPick: (String?) -> Unit,
    onBack: () -> Unit
) {
    var tab by remember { mutableStateOf("全部") }

    Column(Modifier.fillMaxSize().background(Color.White)) {

        Row(
            Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onBack),
                contentAlignment = Alignment.CenterStart
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.weight(1f))
            Text("选择图标", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink)
            Spacer(Modifier.weight(1f))
            Text("${IconLibrary.all().size} 个", fontSize = 13.sp, color = MutedLight,
                modifier = Modifier.width(44.dp))
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Tabs.forEach { t ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        t,
                        fontSize = 13.5.sp,
                        fontWeight = if (tab == t) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (tab == t) Ink else Muted,
                        modifier = Modifier.clickable { tab = t }
                    )
                    Spacer(Modifier.height(7.dp))
                    Box(
                        Modifier.width(if (tab == t) 18.dp else 0.dp).height(2.dp)
                            .background(if (tab == t) Teal else Color.Transparent)
                    )
                }
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {

            if (tab == "全部") {
                GroupTitle("无")
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconCell(
                        selected = selected == null,
                        modifier = Modifier.width(46.dp),
                        onClick = { onPick(null) }
                    ) { Text("无", fontSize = 11.5.sp, color = MutedLight) }
                }
            }

            val groups = if (tab == "全部") IconLibrary.groups else listOf(tab)
            groups.forEach { g ->
                GroupTitle(g)
                val list = IconLibrary.byGroup(g)
                list.chunked(6).forEach { row ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        row.forEach { icon ->
                            IconCell(
                                selected = selected == icon.id,
                                modifier = Modifier.width(46.dp),
                                onClick = { onPick(icon.id) }
                            ) {
                                Icon(
                                    IconLibrary.vector(icon.id),
                                    contentDescription = icon.label,
                                    tint = if (selected == icon.id) TealDeep else Color(0xFF4A4A4A),
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                        repeat(6 - row.size) { Spacer(Modifier.width(46.dp)) }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun GroupTitle(text: String) {
    Text(
        text,
        fontSize = 12.sp,
        color = Muted,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
    )
}
