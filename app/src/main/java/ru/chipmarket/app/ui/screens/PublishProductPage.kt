package ru.chipmarket.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.chipmarket.app.data.model.NewProduct
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.PageTitle

@Composable
fun PublishProductPage(vm: StoreViewModel, fg: Color) {
    val name = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("Электроника") }
    val kind = remember { mutableStateOf("") }
    val description = remember { mutableStateOf("") }
    val details = remember { mutableStateOf("") }
    val price = remember { mutableStateOf("") }
    val oldPrice = remember { mutableStateOf("") }
    val stock = remember { mutableStateOf("1") }
    val photos = remember { mutableStateOf<List<Uri>>(emptyList()) }
    val error = remember { mutableStateOf("") }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
        photos.value = it.take(5)
    }
    val numberKeyboard = KeyboardOptions(keyboardType = KeyboardType.Number)

    Column(Modifier.fillMaxSize()) {
        PageTitle("Новый товар", fg, onBack = { vm.goBack() })
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Информация о товаре", color = fg, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            OutlinedTextField(name.value, { name.value = it }, label = { Text("Название") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Категория", color = fg)
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (option in listOf("Электроника", "Для дома", "Одежда", "Продукты", "Спорт")) {
                    FilterChip(selected = category.value == option, onClick = { category.value = option }, label = { Text(option) })
                }
            }
            OutlinedTextField(kind.value, { kind.value = it }, label = { Text("Тип или модель") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(description.value, { description.value = it }, label = { Text("Описание") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(details.value, { details.value = it }, label = { Text("Характеристики") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(price.value, { price.value = it }, label = { Text("Цена, ₽") }, keyboardOptions = numberKeyboard,
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(oldPrice.value, { oldPrice.value = it }, label = { Text("Старая цена, ₽ (необязательно)") },
                keyboardOptions = numberKeyboard, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(stock.value, { stock.value = it }, label = { Text("Количество на складе") }, keyboardOptions = numberKeyboard,
                singleLine = true, modifier = Modifier.fillMaxWidth())
            Text("Фотографии (${photos.value.size}/5)", color = fg)
            OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                Text(if (photos.value.isEmpty()) "Выбрать фото" else "Изменить фото")
            }
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (uri in photos.value) {
                    AsyncImage(uri, "Фото товара", Modifier.size(88.dp), contentScale = ContentScale.Crop)
                }
            }
            if (error.value.isNotEmpty()) Text(error.value, color = Color(0xFFB3261E), fontSize = 13.sp)
            Button(
                onClick = {
                    val newPrice = price.value.toIntOrNull() ?: 0
                    val count = stock.value.toIntOrNull() ?: -1
                    error.value = when {
                        name.value.length < 3 -> "Введите название товара"
                        description.value.length < 10 -> "Описание должно быть не короче 10 символов"
                        newPrice <= 0 -> "Укажите цену"
                        count < 0 -> "Укажите количество на складе"
                        photos.value.isEmpty() -> "Добавьте хотя бы одно фото"
                        else -> ""
                    }
                    if (error.value.isEmpty()) {
                        val product = NewProduct(name.value.trim(), category.value, description.value.trim(), newPrice,
                            oldPrice.value.toIntOrNull(), count, details.value.trim(), kind.value.trim())
                        vm.publishProduct(product, photos.value)
                    }
                },
                enabled = !vm.loading.value,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Выложить товар")
            }
        }
    }
}
