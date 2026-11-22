package online.lfcloud.order.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Teal = Color(0xFF0F9D8C)
val TealDeep = Color(0xFF0B8474)
val TealSoft = Color(0xFFD9F2ED)
val TealSofter = Color(0xFFF0FBF9)

val Ink = Color(0xFF111111)
val Ink2 = Color(0xFF3A3A3A)
val Muted = Color(0xFF8A8A8A)
val MutedLight = Color(0xFFB0B0B0)
val HairLine = Color(0xFFF0F0F0)
val Danger = Color(0xFFC9503C)

private val Scheme = lightColorScheme(
    primary = Teal,
    onPrimary = Color.White,
    primaryContainer = TealSoft,
    onPrimaryContainer = TealDeep,
    background = Color.White,
    onBackground = Ink,
    surface = Color.White,
    onSurface = Ink,
    surfaceVariant = TealSofter,
    onSurfaceVariant = Muted,
    outline = HairLine,
    outlineVariant = HairLine,
    error = Danger,
    onError = Color.White
)

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
    MaterialTheme(
        colorScheme = Scheme,
        typography = AppTypography,
        content = content
    )
}
