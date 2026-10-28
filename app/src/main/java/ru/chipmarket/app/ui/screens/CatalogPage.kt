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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
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
import ru.chipmarket.app.ui.components.ProductCard
import ru.chipmarket.app.ui.theme.design

@Composable
fun CatalogPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val categories = listOf("Все") + vm.products.value.map { it.category }.filter { it.isNotBlank() }.distinct().sorted()
    val list = vm.products.value.filter { vm.category.value == "Все" || it.category == vm.category.value }
    val blue = if (vm.darkMode.value) design.darkBlue else design.blue

    Column(Modifier.fillMaxSize()) {
        PageTitle("ChipMarket", fg)
        Row(
            Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(44.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(panel)
                .clickable { vm.navigate(Screen.Search) }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Search, null, tint = design.gray)
            Text("Поиск товаров", color = design.gray, fontSize = 13.sp, modifier = Modifier.padding(start = 9.dp))
        }
        Row(
            Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            for (category in categories) {
                val selected = vm.category.value == category
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = if (selected) blue else panel,
                    modifier = Modifier.clickable { vm.category.value = category }
                ) {
                    Text(
                        category,
                        color = if (selected) Color.White else fg,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }
        }
        Text("Товары", color = fg, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.padding(start = 16.dp, bottom = 8.dp))
        if (list.isEmpty()) {
            EmptyText("Товары пока не добавлены")
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                items(list) { ProductCard(it, vm, fg, panel) }
            }
        }
    }
}
