package online.lfcloud.order.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * 全局调色板。dark 是 Compose 状态，下面所有 token 都是它的 getter，
 * 所以在组合里读 token 会订阅 dark，切换主题时整树自动重组。
 */
object OrderPalette {
    var dark by mutableStateOf(false)

    val bg: Color get() = if (dark) Color(0xFF0B1512) else Color(0xFFFFFFFF)
    val soft: Color get() = if (dark) Color(0xFF15201D) else Color(0xFFF8FAFA)
    val soft2: Color get() = if (dark) Color(0xFF1B2724) else Color(0xFFF5F7F7)
    val teal: Color get() = if (dark) Color(0xFF4FD1C0) else Color(0xFF0F9D8C)
    val tealDeep: Color get() = if (dark) Color(0xFF8CE8DB) else Color(0xFF0B8474)
    val tealSofter: Color get() = if (dark) Color(0xFF12302B) else Color(0xFFF0FBF9)
    val ink: Color get() = if (dark) Color(0xFFE6F2EF) else Color(0xFF111111)
    val ink2: Color get() = if (dark) Color(0xFFC3D6D1) else Color(0xFF3A3A3A)
    val muted: Color get() = if (dark) Color(0xFF8FA5A0) else Color(0xFF8A8A8A)
    val mutedLight: Color get() = if (dark) Color(0xFF6E837E) else Color(0xFFB0B0B0)
    val line: Color get() = if (dark) Color(0xFF22302C) else Color(0xFFF0F0F0)
    val danger: Color get() = if (dark) Color(0xFFE0836F) else Color(0xFFC9503C)
}

val Bg: Color get() = OrderPalette.bg
val Soft: Color get() = OrderPalette.soft
val Soft2: Color get() = OrderPalette.soft2

val Teal: Color get() = OrderPalette.teal
val TealDeep: Color get() = OrderPalette.tealDeep
val TealSofter: Color get() = OrderPalette.tealSofter
private val TealSoft = Color(0xFFD9F2ED)
private val TealSoftDark = Color(0xFF1B4740)

val Ink: Color get() = OrderPalette.ink
val Ink2: Color get() = OrderPalette.ink2
val Muted: Color get() = OrderPalette.muted
val MutedLight: Color get() = OrderPalette.mutedLight
val HairLine: Color get() = OrderPalette.line
val Danger: Color get() = OrderPalette.danger

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, letterSpacing = (-0.6).sp),
    titleLarge = TextStyle(fontSize = 16.5.sp, fontWeight = FontWeight.Medium),
    titleMedium = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
    bodyLarge = TextStyle(fontSize = 15.sp),
    bodyMedium = TextStyle(fontSize = 13.sp),
    bodySmall = TextStyle(fontSize = 12.5.sp),
    labelSmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
)

@Composable
fun OrderTheme(content: @Composable () -> Unit) {
    val dark = OrderPalette.dark
    val scheme = if (dark) {
        darkColorScheme(
            primary = OrderPalette.teal,
            onPrimary = Color(0xFF052B26),
            primaryContainer = TealSoftDark,
            onPrimaryContainer = OrderPalette.tealDeep,
            background = OrderPalette.bg,
            onBackground = OrderPalette.ink,
            surface = OrderPalette.bg,
            onSurface = OrderPalette.ink,
            surfaceVariant = OrderPalette.soft2,
            onSurfaceVariant = OrderPalette.muted,
            outline = OrderPalette.line,
            outlineVariant = OrderPalette.line,
            error = OrderPalette.danger,
            onError = Color(0xFF2A0C06)
        )
    } else {
        lightColorScheme(
            primary = OrderPalette.teal,
            onPrimary = Color.White,
            primaryContainer = TealSoft,
            onPrimaryContainer = OrderPalette.tealDeep,
            background = Color.White,
            onBackground = OrderPalette.ink,
            surface = Color.White,
            onSurface = OrderPalette.ink,
            surfaceVariant = OrderPalette.soft,
            onSurfaceVariant = OrderPalette.muted,
            outline = OrderPalette.line,
            outlineVariant = OrderPalette.line,
            error = OrderPalette.danger,
            onError = Color.White
        )
    }
    MaterialTheme(colorScheme = scheme, typography = AppTypography, content = content)
}
