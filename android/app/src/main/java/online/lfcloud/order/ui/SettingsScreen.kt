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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.ui.theme.Bg
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.Teal
import online.lfcloud.order.ui.theme.TealDeep
import java.util.Locale

private val ThemeOptions = listOf("跟随系统", "浅色", "深色")

@Composable
fun SettingsScreen(
    vm: OrderViewModel,
    onBack: () -> Unit
) {
    var showTheme by remember { mutableStateOf(false) }
    var showCache by remember { mutableStateOf(false) }
    var cacheText by remember { mutableStateOf(formatBytes(vm.cacheSizeBytes())) }
    var showUpdate by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text("设置", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {

            SectionLabel("外观", first = true)
            SettingRow("主题", vm.themeMode, true) { showTheme = true }

            SectionLabel("存储")
            SettingRow("缓存占用", cacheText, true) { showCache = true }
            SettingRow("回收站保留", "30 天", false) {}

            SectionLabel("关于")
            SettingRow("版本", vm.versionName(), false) {}
            SettingRow("检查更新", "已是最新", true) { showUpdate = true }

            Spacer(Modifier.height(30.dp))
        }
    }

    if (showTheme) {
        ThemeSheet(
            current = vm.themeMode,
            onPick = { vm.setTheme(it); showTheme = false },
            onDismiss = { showTheme = false }
        )
    }

    if (showCache) {
        AlertDialog(
            onDismissRequest = { showCache = false },
            containerColor = Bg,
            title = { Text("清理缓存？", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink) },
            text = {
                Text("将清除 $cacheText 的临时文件，已保存的物品与图片不受影响。",
                    fontSize = 13.sp, color = Muted, lineHeight = 20.sp)
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.clearCache()
                    cacheText = formatBytes(vm.cacheSizeBytes())
                    showCache = false
                }) {
                    Text("清理", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Teal)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCache = false }) {
                    Text("取消", fontSize = 14.sp, color = Muted)
                }
            }
        )
    }

    if (showUpdate) {
        AlertDialog(
            onDismissRequest = { showUpdate = false },
            containerColor = Bg,
            title = { Text("已是最新", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink) },
            text = {
                Text("当前版本 ${vm.versionName()}，没有可用的更新。",
                    fontSize = 13.sp, color = Muted, lineHeight = 20.sp)
            },
            confirmButton = {
                TextButton(onClick = { showUpdate = false }) {
                    Text("好", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Teal)
                }
            }
        )
    }
}

@Composable
private fun SectionLabel(text: String, first: Boolean = false) {
    Text(
        text, fontSize = 12.5.sp, color = Muted,
        modifier = Modifier.padding(
            start = 16.dp, end = 16.dp,
            top = if (first) 6.dp else 18.dp, bottom = 8.dp
        )
    )
}

@Composable
private fun SettingRow(label: String, value: String, clickable: Boolean, onClick: () -> Unit) {
    Column {
        Row(
            Modifier.fillMaxWidth()
                .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.5.sp, color = Ink, modifier = Modifier.weight(1f))
            Text(value, fontSize = 13.5.sp, color = Muted)
            if (clickable) Text("  ›", fontSize = 14.sp, color = Muted)
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(HairLine))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ThemeSheet(current: String, onPick: (String) -> Unit, onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Bg
    ) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            Text("主题", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink,
                modifier = Modifier.padding(bottom = 6.dp))
            ThemeOptions.forEach { option ->
                Column {
                    Row(
                        Modifier.fillMaxWidth().height(48.dp).clickable { onPick(option) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            option, fontSize = 14.5.sp,
                            color = if (option == current) TealDeep else Ink,
                            fontWeight = if (option == current) FontWeight.SemiBold
                            else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        if (option == current) {
                            Text("✓", fontSize = 15.sp, color = Teal)
                        }
                    }
                    Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> String.format(Locale.US, "%.1f MB", bytes / 1048576.0)
}
