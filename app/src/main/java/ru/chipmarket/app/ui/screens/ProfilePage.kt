package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.chipmarket.app.data.model.Profile
import ru.chipmarket.app.navigation.Screen
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.EmptyText
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.components.ProductImage
import ru.chipmarket.app.ui.theme.design

@Composable
fun ProfilePage(vm: StoreViewModel, fg: Color, panel: Color) {
    val name = remember(vm.profile.value) { mutableStateOf(vm.profile.value.fullName) }
    val phone = remember(vm.profile.value) { mutableStateOf(vm.profile.value.phone) }
    val address = remember(vm.profile.value) { mutableStateOf(vm.profile.value.address) }
    val green = if (vm.darkMode.value) design.darkGreen else design.green

    Column(Modifier.fillMaxSize()) {
        PageTitle("Профиль", fg) {
            IconButton(onClick = { vm.refresh() }) { Icon(Icons.Outlined.Sync, "Обновить", tint = fg) }
        }
        if (!vm.signedIn) {
            EmptyText("Войдите, чтобы увидеть профиль")
            Button(onClick = { vm.navigate(Screen.Login) }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Войти")
            }
            OutlinedButton(onClick = { vm.navigate(Screen.WorkerRegister) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Стать продавцом")
            }
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(
                    Modifier.fillMaxWidth().background(panel, RoundedCornerShape(12.dp)).padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.AccountCircle, null, tint = design.blue, modifier = Modifier.size(45.dp))
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(vm.profile.value.fullName.ifBlank { "Покупатель ChipMarket" }, color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(vm.profile.value.phone.ifBlank { "Мой профиль" }, color = design.gray, fontSize = 12.sp)
                        Text(vm.repository.session?.email ?: "", color = design.gray, fontSize = 12.sp)
                        Text("Роль: ${if (vm.isSeller) "Продавец" else "Покупатель"}", color = design.gray, fontSize = 12.sp)
                    }
                }
            }
            item { Text("Мои заказы", color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            if (vm.orders.value.isEmpty()) {
                item { Text("Заказов пока нет", color = design.gray, fontSize = 13.sp) }
            }
            items(vm.orders.value.take(2)) { order ->
                Row(
                    Modifier.fillMaxWidth()
                        .background(panel, RoundedCornerShape(10.dp))
                        .clickable { vm.navigate(Screen.Orders) }
                        .padding(13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Заказ № ${order.id.take(8)}", color = fg, fontSize = 13.sp)
                        Text(order.createdAt.take(10), color = design.gray, fontSize = 11.sp)
                    }
                    Text(design.rubles(order.total), color = green, fontWeight = FontWeight.Bold)
                }
            }
            item {
                TextButton(onClick = { vm.navigate(Screen.Orders) }) { Text("Все заказы (${vm.orders.value.size})") }
            }
            item { Text("История заказов", color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            val bought = vm.orderItems.value.values.flatten().mapNotNull { line -> vm.products.value.find { it.id == line.productId } }.distinct().take(2)
            if (bought.isEmpty()) {
                item { Text("История появится после покупки", color = design.gray, fontSize = 13.sp) }
            } else {
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        for (product in bought) {
                            Column(Modifier.weight(1f).background(panel, RoundedCornerShape(10.dp)).padding(8.dp)) {
                                ProductImage(product, panel, Modifier.fillMaxWidth().height(105.dp))
                                Text(product.title, color = fg, fontSize = 12.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
            item { Text("Мои данные", color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            item {
                if (vm.isSeller) {
                    Button(onClick = { vm.navigate(Screen.WorkerDashboard) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Мои товары и публикация")
                    }
                } else {
                    OutlinedButton(onClick = { vm.navigate(Screen.WorkerRegister) }, modifier = Modifier.fillMaxWidth()) {
                        Text("Стать продавцом")
                    }
                }
            }
            item { OutlinedTextField(name.value, { name.value = it }, label = { Text("Имя и фамилия") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(phone.value, { phone.value = it }, label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth()) }
            item { OutlinedTextField(address.value, { address.value = it }, label = { Text("Адрес доставки") }, modifier = Modifier.fillMaxWidth()) }
            item {
                Button(onClick = { vm.saveProfile(Profile(name.value, phone.value, address.value)) }, modifier = Modifier.fillMaxWidth()) {
                    Text("Сохранить")
                }
            }
            item { HorizontalDivider() }
            item { TextButton(onClick = { vm.signOut() }) { Text("Выйти из аккаунта") } }
        }
    }
}
