package com.terminplaner.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb

val Blue = Color(0xFF2196F3)
val Red = Color(0xFFE53935)
val Orange = Color(0xFFFB8C00)
val Yellow = Color(0xFFFFEB3B)
val Green = Color(0xFF4CAF50)
val Purple = Color(0xFF9C27B0)
val Pink = Color(0xFFE91E63)
val Brown = Color(0xFF795548)
val Grey = Color(0xFF9E9E9E)
val Black = Color(0xFF000000)

val presetColors = listOf(
    Red, Orange, Yellow, Green, Blue, Purple, Pink, Brown, Grey, Black
)

fun Color.toArgbInt(): Int = toArgb()
