package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.EmptyText
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.theme.design

@Composable
fun OrdersPage(vm: StoreViewModel, fg: Color, panel: Color) {
    Column(Modifier.fillMaxSize()) {
        PageTitle("Мои заказы", fg, onBack = { vm.goBack() })
        if (vm.orders.value.isEmpty()) {
            EmptyText("Заказов пока нет")
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(vm.orders.value) { order ->
                Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(10.dp)).padding(16.dp)) {
                    Text("Заказ ${order.id.take(8)}", color = fg, fontWeight = FontWeight.Bold)
                    Text(order.createdAt.take(10), color = design.gray, fontSize = 12.sp)
                    Text("Статус: " + if (order.status == "created") "Оформлен" else order.status, color = fg, fontSize = 13.sp)
                    for (line in vm.orderItems.value[order.id] ?: emptyList()) {
                        Text("${line.title} × ${line.quantity}", color = fg, fontSize = 12.sp)
                    }
                    Text("Банк: ${order.paymentMethod}", color = design.gray, fontSize = 12.sp)
                    Text(design.rubles(order.total), color = design.green, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
