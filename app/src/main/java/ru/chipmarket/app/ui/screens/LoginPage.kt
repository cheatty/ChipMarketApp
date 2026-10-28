package ru.chipmarket.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import ru.chipmarket.app.navigation.Screen
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.storageUrl
import ru.chipmarket.app.ui.theme.design

@Composable
fun LoginPage(vm: StoreViewModel, fg: Color, panel: Color) {
    val email = remember { mutableStateOf(vm.repository.lastEmail) }
    val password = remember { mutableStateOf("") }
    val username = remember { mutableStateOf("") }
    val showPassword = remember { mutableStateOf(false) }
    val register = vm.registrationMode.value
    val canSend = email.value.contains("@") && password.value.isNotEmpty() && (!register || username.value.length >= 3) && !vm.loading.value

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TextButton(onClick = { vm.goBack() }, modifier = Modifier.align(Alignment.Start)) { Text("Назад") }
        Text("ChipMarket", color = fg, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        AsyncImage(storageUrl + "logo.png", "Логотип ChipMarket", Modifier.size(123.dp))
        Text(
            if (register) "Регистрация" else "Вход",
            color = fg,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth()
        )
        if (register) {
            OutlinedTextField(
                value = username.value,
                onValueChange = { username.value = it.trim() },
                label = { Text("Ваш ник") },
                prefix = { Text("@") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Электронная почта") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation = if (showPassword.value) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword.value = !showPassword.value }) {
                    Icon(Icons.Outlined.Visibility, "Показать пароль", tint = design.gray)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )
        if (!register) {
            TextButton(onClick = { vm.resetPassword(email.value) }, modifier = Modifier.align(Alignment.End)) {
                Text("Забыли пароль?")
            }
        }
        val info = vm.authMessage.value
        if (info != null) {
            val good = info.startsWith("Регистрация") || info.startsWith("Письмо")
            Text(info, color = if (good) design.green else Color(0xFFB3261E), fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
        }
        Button(
            onClick = { if (register) vm.signUp(username.value, email.value, password.value) else vm.signIn(email.value, password.value) },
            enabled = canSend,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text(if (register) "Зарегистрироваться" else "Войти", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(if (register) "Уже есть аккаунт?" else "Нет аккаунта?", color = design.gray, fontSize = 14.sp)
            TextButton(onClick = {
                vm.registrationMode.value = !register
                vm.authMessage.value = null
            }) {
                Text(if (register) "Войти" else "Зарегистрироваться")
            }
        }
        OutlinedButton(onClick = { vm.navigate(Screen.WorkerRegister) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Outlined.Storefront, null)
            Text("Стать продавцом", modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(Modifier.height(16.dp))
    }
}
