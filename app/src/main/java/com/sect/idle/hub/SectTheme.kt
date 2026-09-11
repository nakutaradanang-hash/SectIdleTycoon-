package com.sect.idle.hub

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Xianxia Immortal Cultivation Color Palette
val JadeCyan = Color(0xFF00E5FF)
val JadeDark = Color(0xFF00838F)
val CelestialGold = Color(0xFFFFD700)
val CelestialAmber = Color(0xFFFFB300)
val LotusPink = Color(0xFFFF4081)
val SpiritPurple = Color(0xFFB388FF)
val VoidBlack = Color(0xFF0A0914)
val TwilightSurface = Color(0xFF141226)
val TwilightElevated = Color(0xFF1F1C38)
val TwilightBorder = Color(0xFF322E54)
val CloudMistWhite = Color(0xFFF3F0FA)
val TextMuted = Color(0xFF9E9DB5)
val DangerRed = Color(0xFFFF5252)
val SuccessGreen = Color(0xFF69F0AE)

private val XianxiaDarkColorScheme = darkColorScheme(
    primary = JadeCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D40),
    onPrimaryContainer = Color(0xFF80CBC4),
    secondary = CelestialGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF5D4037),
    onSecondaryContainer = Color(0xFFFFE082),
    tertiary = SpiritPurple,
    onTertiary = Color.Black,
    background = VoidBlack,
    onBackground = CloudMistWhite,
    surface = TwilightSurface,
    onSurface = CloudMistWhite,
    surfaceVariant = TwilightElevated,
    onSurfaceVariant = TextMuted,
    outline = TwilightBorder,
    error = DangerRed,
    onError = Color.White
)

val XianxiaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

val XianxiaTypography = Typography(
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.5.sp,
        color = CloudMistWhite
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        color = CloudMistWhite
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        color = CloudMistWhite
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        color = CloudMistWhite
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        color = CloudMistWhite
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        color = TextMuted
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.3.sp
    )
)

@Composable
fun SectHubTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = XianxiaDarkColorScheme,
        shapes = XianxiaShapes,
        typography = XianxiaTypography,
        content = content
    )
}
