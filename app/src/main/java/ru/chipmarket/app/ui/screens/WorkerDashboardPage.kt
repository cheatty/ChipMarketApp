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
import androidx.compose.material3.Button
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
import ru.chipmarket.app.ui.components.EmptyText
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.components.ProductImage
import ru.chipmarket.app.ui.theme.design

@Composable
fun WorkerDashboardPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val seller = vm.seller.value
    val myProducts = vm.products.value.filter { seller != null && it.sellerId == seller.id }

    Column(Modifier.fillMaxSize()) {
        PageTitle(seller?.shopName ?: "Мои товары", fg, onBack = { vm.goBack() })
        if (seller != null) {
            Text("Продавец @${seller.username}", color = design.gray, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
        }
        Button(
            onClick = { vm.navigate(Screen.PublishProduct) },
            modifier = Modifier.fillMaxWidth().padding(16.dp).height(50.dp)
        ) {
            Text("Выложить товар")
        }
        Text("Опубликовано: ${myProducts.size}", color = design.gray, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 16.dp))
        if (myProducts.isEmpty()) {
            EmptyText("Вы пока не добавили товары")
            return@Column
        }
        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(myProducts) { product ->
                Row(
                    Modifier.fillMaxWidth()
                        .background(panel, RoundedCornerShape(10.dp))
                        .clickable { vm.openProduct(product) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ProductImage(product, panel, Modifier.size(74.dp))
                    Column(Modifier.weight(1f).padding(start = 12.dp)) {
                        Text(product.title, color = fg, fontWeight = FontWeight.Bold, maxLines = 2)
                        Text(product.category, color = design.gray, fontSize = 12.sp)
                        Text(design.rubles(product.price), color = design.green, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
