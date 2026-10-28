package ru.chipmarket.app.ui.theme

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.util.Locale

class AppDesign {
    val blue = Color(0xFF386EAE)
    val darkBlue = Color(0xFF8CB9EC)
    val green = Color(0xFF158C4B)
    val darkGreen = Color(0xFF70D49B)
    val lightBg = Color.White
    val darkBg = Color(0xFF191E24)
    val darkCard = Color(0xFF282E37)
    val gray = Color(0xFF7B8794)

    fun rubles(value: Int): String {
        val text = String.format(Locale.US, "%,d", value).replace(',', ' ')
        return "$text ₽"
    }
}

val design = AppDesign()

fun deliveryText(): String {
    val months = listOf("янв", "фев", "мар", "апр", "мая", "июн", "июл", "авг", "сен", "окт", "ноя", "дек")
    val date = LocalDate.now().plusDays(10)
    return "Придёт ${date.dayOfMonth} ${months[date.monthValue - 1]}"
}
