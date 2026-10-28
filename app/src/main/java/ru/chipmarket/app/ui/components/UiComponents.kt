package ru.chipmarket.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.chipmarket.app.BuildConfig
import ru.chipmarket.app.data.model.Product
import ru.chipmarket.app.navigation.Screen
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.theme.deliveryText
import ru.chipmarket.app.ui.theme.design

val storageUrl = BuildConfig.SUPABASE_URL.trimEnd('/') + "/storage/v1/object/public/chipmarket-assets/"

@Composable
fun BottomNav(vm: StoreViewModel) {
    Row(Modifier.fillMaxWidth().height(64.dp).background(if (vm.darkMode.value) design.darkBg else Color.White)) {
        NavItem(vm, Screen.Catalog, "Каталог", Icons.Outlined.GridView)
        NavItem(vm, Screen.Favorites, "Избранное", Icons.Outlined.FavoriteBorder)
        NavItem(vm, Screen.Cart, "Корзина", Icons.Outlined.ShoppingCart)
        NavItem(vm, Screen.Profile, "Профиль", Icons.Outlined.PersonOutline)
    }
}

@Composable
private fun RowScope.NavItem(vm: StoreViewModel, screen: Screen, label: String, icon: ImageVector) {
    val active = vm.screen.value == screen || (screen == Screen.Catalog && vm.screen.value == Screen.Search)
    val color = if (active) design.blue else design.gray
    Column(
        Modifier.weight(1f).fillMaxHeight().clickable { vm.navigate(screen) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, label, tint = color, modifier = Modifier.size(22.dp))
        Text(label, fontSize = 10.sp, color = color)
    }
}

@Composable
fun PageTitle(title: String, fg: Color, onBack: (() -> Unit)? = null, action: @Composable () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        if (onBack != null) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Назад", tint = fg) }
        }
        Text(title, color = fg, fontSize = 22.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        action()
    }
}

@Composable
fun SearchBox(value: String, panel: Color, onValueChange: (String) -> Unit) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text("Поиск товаров", fontSize = 13.sp) },
        leadingIcon = { Icon(Icons.Outlined.Search, null, tint = design.gray) },
        singleLine = true,
        shape = RoundedCornerShape(9.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = panel,
            unfocusedContainerColor = panel,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        ),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
    )
}

@Composable
fun ProductImage(product: Product, panel: Color, modifier: Modifier = Modifier) {
    Box(modifier.clip(RoundedCornerShape(10.dp)).background(panel), contentAlignment = Alignment.Center) {
        val image = product.imageUrl ?: product.images.firstOrNull()
        if (image != null) {
            AsyncImage(image, product.title, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
        } else {
            Icon(Icons.Outlined.Inventory2, product.title, tint = design.gray, modifier = Modifier.size(46.dp))
        }
    }
}

@Composable
fun ProductCard(product: Product, vm: StoreViewModel, fg: Color, panel: Color) {
    val quantity = vm.cart.value[product.id] ?: 0
    val liked = product.id in vm.favorites.value
    val green = if (vm.darkMode.value) design.darkGreen else design.green
    Column(Modifier.fillMaxWidth().clickable { vm.openProduct(product) }) {
        Box {
            ProductImage(product, panel, Modifier.fillMaxWidth().height(142.dp))
            IconButton(onClick = { vm.toggleFavorite(product) }, modifier = Modifier.align(Alignment.TopEnd).size(36.dp)) {
                Icon(
                    if (liked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    "В избранное",
                    tint = if (liked) Color.Red else design.gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(product.title, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (product.seller != null) {
            Text(product.seller.shopName, color = design.blue, fontSize = 10.sp, maxLines = 1)
        }
        Text(product.description, color = design.gray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text(design.rubles(product.price), color = green, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        if (product.oldPrice != null) {
            Text(design.rubles(product.oldPrice), color = design.gray, fontSize = 10.sp, textDecoration = TextDecoration.LineThrough)
        }
        if (quantity > 0) {
            Text("Добавлено · ${deliveryText()}", color = green, fontSize = 10.sp, modifier = Modifier.padding(top = 3.dp))
        }
        Spacer(Modifier.height(5.dp))
        Button(
            onClick = { vm.changeCart(product, 1) },
            shape = RoundedCornerShape(5.dp),
            contentPadding = PaddingValues(horizontal = 4.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (vm.darkMode.value) Color(0xFF263E5A) else Color(0xFFE8EEF7),
                contentColor = if (vm.darkMode.value) design.darkBlue else design.blue
            ),
            modifier = Modifier.fillMaxWidth().height(32.dp)
        ) {
            Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(15.dp))
            Spacer(Modifier.width(5.dp))
            Text(if (quantity > 0) "Добавлено · $quantity" else "В корзину", fontSize = 11.sp)
        }
    }
}

@Composable
fun ProductRow(product: Product, vm: StoreViewModel, fg: Color, panel: Color) {
    val quantity = vm.cart.value[product.id] ?: 0
    Row(
        Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(panel)
            .clickable { vm.openProduct(product) }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductImage(product, if (vm.darkMode.value) design.darkBg else Color.White, Modifier.size(70.dp))
        Column(Modifier.weight(1f).padding(start = 12.dp)) {
            Text(product.title, color = fg, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            if (product.seller != null) {
                Text("${product.seller.shopName} · @${product.seller.username}", color = design.blue, fontSize = 10.sp)
            }
            Text(product.description, color = design.gray, fontSize = 11.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(design.rubles(product.price), color = design.green, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = { vm.changeCart(product, 1) }) {
            Text(if (quantity > 0) "Ещё · $quantity" else "В корзину", fontSize = 11.sp)
        }
    }
}

@Composable
fun EmptyText(text: String) {
    Box(Modifier.fillMaxWidth().padding(36.dp), contentAlignment = Alignment.Center) {
        Text(text, color = design.gray, fontSize = 15.sp)
    }
}
