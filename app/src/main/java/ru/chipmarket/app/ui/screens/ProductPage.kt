package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.chipmarket.app.data.model.Product
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.components.ProductImage
import ru.chipmarket.app.ui.theme.deliveryText
import ru.chipmarket.app.ui.theme.design

@Composable
fun ProductPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val product = vm.selectedProduct.value ?: return
    val photo = remember(product.id) { mutableStateOf(product.images.firstOrNull() ?: product.imageUrl) }
    val liked = product.id in vm.favorites.value
    val green = if (vm.darkMode.value) design.darkGreen else design.green
    val quantity = vm.cart.value[product.id] ?: 0

    Column(Modifier.fillMaxSize()) {
        PageTitle("Товар", fg, onBack = { vm.goBack() }) {
            IconButton(onClick = { vm.toggleFavorite(product) }) {
                Icon(
                    if (liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    "Избранное",
                    tint = if (liked) Color.Red else fg
                )
            }
        }
        LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp)) {
            item {
                AsyncImage(
                    photo.value,
                    product.title,
                    Modifier.fillMaxWidth().height(300.dp).clip(RoundedCornerShape(14.dp)).background(panel),
                    contentScale = ContentScale.Fit
                )
                if (product.images.size > 1) {
                    Row(
                        Modifier.horizontalScroll(rememberScrollState()).padding(top = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        for (url in product.images) {
                            AsyncImage(
                                url,
                                "Фото товара",
                                Modifier.size(74.dp).clip(RoundedCornerShape(9.dp)).background(panel).clickable { photo.value = url },
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }
            }
            item {
                Text(product.title, color = fg, fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.padding(top = 12.dp))
                Text(product.category, color = design.gray, fontSize = 14.sp)
            }
            item { PriceBlock(product, vm.darkMode.value, fg) }
            item {
                Title("Оплата картой", fg)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PayCard("ChipBank", "Сразу", fg, panel, Modifier.weight(1f))
                    PayCard("ChipРассрочка", "4 × ${design.rubles(product.price / 4)}", fg, panel, Modifier.weight(1f))
                }
            }
            if (product.seller != null) {
                item {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 16.dp).background(panel, RoundedCornerShape(11.dp)).padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Storefront, null, tint = design.blue)
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(product.seller.shopName, color = fg, fontWeight = FontWeight.Bold)
                            Text("Продавец @${product.seller.username}", color = design.gray, fontSize = 12.sp)
                        }
                    }
                }
            }
            item {
                Title("Описание", fg)
                Text(product.description, color = fg, fontSize = 14.sp, lineHeight = 21.sp)
            }
            if (product.details.isNotBlank()) {
                item {
                    Title("Характеристики", fg)
                    Column(Modifier.fillMaxWidth().background(panel, RoundedCornerShape(12.dp)).padding(horizontal = 14.dp)) {
                        for (line in product.details.lines().filter { it.isNotBlank() }) {
                            Row(Modifier.padding(vertical = 9.dp)) {
                                Text(line.substringBefore(":"), color = design.gray, fontSize = 13.sp, modifier = Modifier.weight(1f))
                                Text(line.substringAfter(":", "").trim(), color = fg, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            item {
                Text(
                    if (product.stock > 0) "В наличии: ${product.stock}" else "Нет в наличии",
                    color = if (product.stock > 0) green else Color(0xFFB3261E),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 14.dp)
                )
            }
            val similar = vm.similarProducts(product)
            if (similar.isNotEmpty()) {
                item {
                    Title("Вам также может понравиться", fg)
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        for (item in similar) {
                            Column(Modifier.width(156.dp).clickable { vm.openProduct(item) }) {
                                ProductImage(item, panel, Modifier.fillMaxWidth().height(120.dp))
                                Text(item.title, color = fg, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                Text(design.rubles(item.price), color = green, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        Button(
            onClick = { vm.changeCart(product, 1) },
            enabled = product.stock > 0,
            colors = ButtonDefaults.buttonColors(containerColor = design.green, contentColor = Color.White),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp).height(54.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val text = when {
                    product.stock <= 0 -> "Нет в наличии"
                    quantity > 0 -> "В корзине · $quantity шт."
                    else -> "В корзину · ${design.rubles(product.price)}"
                }
                Text(text, fontWeight = FontWeight.Bold)
                if (product.stock > 0) Text(deliveryText(), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PriceBlock(product: Product, darkMode: Boolean, fg: Color) {
    val green = if (darkMode) design.darkGreen else design.green
    Row(
        Modifier.fillMaxWidth()
            .padding(top = 16.dp)
            .background(if (darkMode) Color(0xFF17392A) else Color(0xFFE5F6EC), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(design.rubles(product.price), color = green, fontWeight = FontWeight.Bold, fontSize = 28.sp)
                if (product.oldPrice != null) {
                    Text(
                        design.rubles(product.oldPrice),
                        color = design.gray,
                        fontSize = 15.sp,
                        textDecoration = TextDecoration.LineThrough,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
            Text("по ChipКарте", color = green, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        if (product.oldPrice != null && product.oldPrice > product.price) {
            val discount = (product.oldPrice - product.price) * 100 / product.oldPrice
            Text(
                "−$discount%",
                color = green,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.background(if (darkMode) Color(0xFF24553D) else Color(0xFFD1EFDD), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 9.dp)
            )
        }
    }
    if (product.oldPrice != null) {
        Row(Modifier.padding(top = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(design.rubles(product.oldPrice), color = fg, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text("обычной картой", color = design.gray, fontSize = 13.sp, modifier = Modifier.padding(start = 14.dp))
        }
    }
}

@Composable
fun PayCard(title: String, subtitle: String, fg: Color, panel: Color, modifier: Modifier) {
    Row(modifier.background(panel, RoundedCornerShape(11.dp)).padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Outlined.CreditCard, null, tint = design.blue, modifier = Modifier.size(22.dp))
        Column(Modifier.padding(start = 8.dp)) {
            Text(title, color = fg, fontWeight = FontWeight.Medium, fontSize = 13.sp)
            Text(subtitle, color = design.gray, fontSize = 11.sp)
        }
    }
}

@Composable
fun Title(text: String, fg: Color) {
    Text(text, color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
}
