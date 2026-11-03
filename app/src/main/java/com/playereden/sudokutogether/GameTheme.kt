package com.playereden.sudokutogether

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val Cream = Color(0xFFF4F3EB)
val Paper = Color(0xFFFFFDF8)
val Forest = Color(0xFF214F3F)
val Ink = Color(0xFF263D36)
val Muted = Color(0xFF55665C)
val Gold = Color(0xFF8A6729)
val Rule = Color(0xFFD8DFD2)
internal val AmberLight = Color(0xFFF3E5BF)
internal val TealLight = Color(0xFFDCECE4)
internal val Brick = Color(0xFFAB4C42)

internal val GreenTint = Color(0xFFE5EDE2)
internal val GoldTint = Color(0xFFF4E7C4)
internal val NightForest = Color(0xFF193F35)
internal val OnForest = Color(0xFFF8F7ED)
internal val SoftWhite = Color(0xFFFEFDF8)

@Composable internal fun SudokuTogetherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Forest, onPrimary = OnForest, primaryContainer = GreenTint,
            onPrimaryContainer = Forest, secondary = Gold, onSecondary = Paper,
            secondaryContainer = GoldTint, onSecondaryContainer = Ink,
            background = Cream, onBackground = Ink, surface = Paper, onSurface = Ink,
            surfaceVariant = GreenTint, onSurfaceVariant = Muted,
            outline = Rule, outlineVariant = Rule.copy(alpha = .65f),
            error = Color(0xFFA84436), onError = Color.White
        ),
        typography = Typography(
            headlineLarge = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 32.sp, lineHeight = 36.sp),
            headlineMedium = TextStyle(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold, fontSize = 27.sp, lineHeight = 32.sp),
            titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 21.sp, lineHeight = 26.sp),
            titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
            bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
            bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 19.sp),
            bodySmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 17.sp),
            labelLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 19.sp),
            labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
            labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 14.sp)
        ),
        shapes = Shapes(
            extraSmall = RoundedCornerShape(7.dp), small = RoundedCornerShape(12.dp),
            medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(21.dp),
            extraLarge = RoundedCornerShape(26.dp)
        ),
        content = content
    )
}
