package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.chipmarket.app.navigation.Screen
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.theme.design

@Composable
fun SuccessPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val green = if (vm.darkMode.value) design.darkGreen else design.green
    Column(Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(54.dp))
        Icon(Icons.Outlined.CheckCircle, null, tint = green, modifier = Modifier.size(78.dp))
        Spacer(Modifier.height(18.dp))
        Text("Заказ оформлен", color = fg, fontSize = 25.sp, fontWeight = FontWeight.Bold)
        Text("Спасибо за покупку!", color = design.gray, fontSize = 14.sp)
        Text("Заказ № ${vm.successOrder.value.take(8)}", color = design.gray, fontSize = 12.sp)
        Spacer(Modifier.height(30.dp))
        Text("Ваши товары", color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(12.dp))
        for (line in vm.successLines.value) {
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${line.title} × ${line.quantity}", color = fg, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text(design.rubles(line.price * line.quantity), color = fg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
        HorizontalDivider()
        Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Итого", color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(design.rubles(vm.successTotal.value), color = green, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
        Text(
            "Выбранный банк: ${vm.successPayment.value}",
            color = fg,
            fontSize = 13.sp,
            modifier = Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(16.dp)
        )
        Spacer(Modifier.weight(1f))
        Button(onClick = { vm.navigate(Screen.Catalog) }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
            Text("Вернуться в каталог")
        }
    }
}
