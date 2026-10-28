package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.EmptyText
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.components.ProductRow
import ru.chipmarket.app.ui.components.SearchBox

@Composable
fun SearchPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val text = vm.query.value.trim()
    val results = vm.products.value.filter {
        it.title.contains(text, ignoreCase = true) || it.category.contains(text, ignoreCase = true)
    }
    Column(Modifier.fillMaxSize()) {
        PageTitle("ChipMarket", fg, onBack = { vm.goBack() })
        SearchBox(vm.query.value, panel) { vm.query.value = it }
        if (text.isEmpty()) {
            Text("Рекомендации", color = fg, fontWeight = FontWeight.Bold, modifier = Modifier.padding(16.dp))
            LazyColumn {
                items(vm.products.value.take(4)) { ProductRow(it, vm, fg, panel) }
            }
        } else if (results.isEmpty()) {
            EmptyText("Ничего не найдено")
        } else {
            LazyColumn(Modifier.padding(top = 8.dp)) {
                items(results) { ProductRow(it, vm, fg, panel) }
            }
        }
    }
}
