package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.chipmarket.app.navigation.Screen
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.EmptyText
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.components.ProductImage
import ru.chipmarket.app.ui.theme.deliveryText
import ru.chipmarket.app.ui.theme.design

@Composable
fun CartPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val list = vm.products.value.filter { (vm.cart.value[it.id] ?: 0) > 0 }
    val total = list.sumOf { it.price * (vm.cart.value[it.id] ?: 0) }
    val green = if (vm.darkMode.value) design.darkGreen else design.green

    Column(Modifier.fillMaxSize()) {
        PageTitle("Корзина", fg)
        Text("${vm.cart.value.values.sum()} товара", color = design.gray, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
        if (list.isEmpty()) {
            EmptyText("Корзина пуста")
            if (!vm.signedIn) {
                Button(onClick = { vm.showAuth() }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Войти, чтобы добавлять товары")
                }
            }
            return@Column
        }
        Text(deliveryText(), color = green, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(list) { product ->
                Row(
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(panel)
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductImage(product, panel, Modifier.size(86.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(product.title, color = fg, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(design.rubles(product.price), color = green, fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { vm.changeCart(product, -1) }) {
                                Icon(Icons.Outlined.Remove, "Уменьшить", tint = fg)
                            }
                            Text("${vm.cart.value[product.id]}", color = fg)
                            IconButton(onClick = { vm.changeCart(product, 1) }) {
                                Icon(Icons.Outlined.Add, "Увеличить", tint = fg)
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Товары", color = design.gray)
            Text(design.rubles(total), color = fg)
        }
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 5.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Доставка", color = design.gray)
            Text("Бесплатно", color = green)
        }
        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Итого", color = fg, fontWeight = FontWeight.Bold)
            Text(design.rubles(total), color = green, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
        Button(
            onClick = { vm.navigate(Screen.Checkout) },
            shape = RoundedCornerShape(7.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(48.dp)
        ) {
            Text("Оформить заказ")
        }
        Text(
            "Получение в пункте выдачи",
            color = design.gray,
            fontSize = 12.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
        )
        Spacer(Modifier.height(12.dp))
    }
}
