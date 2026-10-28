package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.PageTitle
import ru.chipmarket.app.ui.theme.design

@Composable
fun WorkerRegisterPage(vm: StoreViewModel, fg: Color) {
    val username = remember { mutableStateOf("") }
    val shopName = remember { mutableStateOf("") }
    val name = remember { mutableStateOf("") }
    val phone = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val canSend = username.value.length >= 3 && shopName.value.isNotBlank() && name.value.isNotBlank() &&
        email.value.contains("@") && password.value.isNotEmpty() && !vm.loading.value

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        PageTitle("Стать продавцом", fg, onBack = { vm.goBack() })
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Создать магазин", color = fg, fontSize = 21.sp, fontWeight = FontWeight.Bold)
            Text(
                "После регистрации вы сможете публиковать товары. Название магазина и username будут видны покупателям.",
                color = design.gray,
                fontSize = 13.sp
            )
            OutlinedTextField(username.value, { username.value = it.trim() }, label = { Text("Username продавца") },
                prefix = { Text("@") }, singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(shopName.value, { shopName.value = it }, label = { Text("Название магазина") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(name.value, { name.value = it }, label = { Text("Имя и фамилия") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(phone.value, { phone.value = it }, label = { Text("Телефон") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(email.value, { email.value = it }, label = { Text("Электронная почта") },
                singleLine = true, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(password.value, { password.value = it }, label = { Text("Пароль") },
                singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            val error = vm.authMessage.value
            if (error != null) Text(error, color = Color(0xFFB3261E), fontSize = 13.sp)
            Button(
                onClick = { vm.signUpSeller(username.value, shopName.value, name.value, phone.value, email.value, password.value) },
                enabled = canSend,
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Создать магазин")
            }
        }
    }
}
