package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.EmptyText
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.components.ProductCard

@Composable
fun FavoritesPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val list = vm.products.value.filter { it.id in vm.favorites.value }
    Column(Modifier.fillMaxSize()) {
        PageTitle("Избранное", fg)
        if (list.isEmpty()) {
            EmptyText("В избранном пока пусто")
            if (!vm.signedIn) {
                Button(onClick = { vm.showAuth() }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text("Войти, чтобы сохранять товары")
                }
            }
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
