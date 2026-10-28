package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.components.PickupPointMap
import ru.chipmarket.app.ui.components.ProductImage
import ru.chipmarket.app.ui.theme.deliveryText
import ru.chipmarket.app.ui.theme.design

@Composable
fun CheckoutPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val address = remember { mutableStateOf(vm.profile.value.address) }
    val bank = remember { mutableStateOf("ChipBank") }
    val list = vm.products.value.filter { (vm.cart.value[it.id] ?: 0) > 0 }
    val total = list.sumOf { it.price * (vm.cart.value[it.id] ?: 0) }
    val green = if (vm.darkMode.value) design.darkGreen else design.green

    Column(Modifier.fillMaxSize().imePadding()) {
        PageTitle("Оформление заказа", fg, onBack = { vm.goBack() })
        LazyColumn(
            Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Ваш заказ · ${vm.cart.value.values.sum()} шт.", color = fg, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(deliveryText(), color = green, fontSize = 13.sp, modifier = Modifier.padding(top = 4.dp))
            }
            items(list) { product ->
                val count = vm.cart.value[product.id] ?: 0
                Row(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(panel).padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductImage(product, panel, Modifier.size(58.dp))
                    Column(Modifier.weight(1f).padding(start = 10.dp)) {
                        Text(product.title, color = fg, fontSize = 13.sp, maxLines = 2)
                        Text("$count шт.", color = design.gray, fontSize = 12.sp)
                    }
                    Text(design.rubles(product.price * count), color = fg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
            item { Text("Способ оплаты", color = fg, fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            items(listOf("ChipBank", "Т-Банк", "ChipBank Рассрочка")) { option ->
                Row(
                    Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(panel)
                        .clickable { bank.value = option }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(selected = bank.value == option, onClick = { bank.value = option }, modifier = Modifier.size(22.dp))
                    Text(option, color = fg, modifier = Modifier.padding(start = 12.dp))
                }
            }
            item { Text("Пункт получения", color = fg, fontWeight = FontWeight.Bold, fontSize = 17.sp) }
            item {
                Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(panel).padding(12.dp)) {
                    PickupPointMap(Modifier.fillMaxWidth().height(230.dp))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = address.value,
                        onValueChange = { address.value = it },
                        label = { Text("Адрес получения") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Итого", color = fg, fontWeight = FontWeight.Bold)
            Text(design.rubles(total), color = green, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
        Button(
            onClick = { vm.placeOrder(address.value, bank.value) },
            enabled = address.value.trim().length >= 5 && list.isNotEmpty() && !vm.loading.value,
            shape = RoundedCornerShape(7.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp)
        ) {
            Text("Оформить заказ · ${design.rubles(total)}")
        }
        Text(
            "Демонстрационный заказ без списания денег",
            color = design.gray,
            fontSize = 11.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(8.dp)
        )
    }
}
