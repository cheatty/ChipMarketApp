package ru.chipmarket.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.chipmarket.app.navigation.Screen
import ru.chipmarket.app.presentation.StoreViewModel
import ru.chipmarket.app.ui.components.BottomNav
import ru.chipmarket.app.ui.screens.CartPage
import ru.chipmarket.app.ui.screens.CatalogPage
import ru.chipmarket.app.ui.screens.CheckoutPage
import ru.chipmarket.app.ui.screens.FavoritesPage
import ru.chipmarket.app.ui.screens.LoginPage
import ru.chipmarket.app.ui.screens.OrdersPage
import ru.chipmarket.app.ui.screens.ProductPage
import ru.chipmarket.app.ui.screens.ProfilePage
import ru.chipmarket.app.ui.screens.PublishProductPage
import ru.chipmarket.app.ui.screens.SearchPage
import ru.chipmarket.app.ui.screens.SuccessPage
import ru.chipmarket.app.ui.screens.WorkerDashboardPage
import ru.chipmarket.app.ui.screens.WorkerRegisterPage
import ru.chipmarket.app.ui.theme.design

@Composable
fun ChipMarketApp(vm: StoreViewModel = viewModel()) {
    vm.darkMode.value = isSystemInDarkTheme()
    val background = if (vm.darkMode.value) design.darkBg else design.lightBg
    val fg = if (vm.darkMode.value) Color(0xFFF7F8FA) else Color(0xFF242A31)
    val panel = if (vm.darkMode.value) design.darkCard else Color(0xFFF3F5F7)
    val colors = if (vm.darkMode.value) {
        darkColorScheme(primary = design.darkBlue, background = background, surface = background)
    } else {
        lightColorScheme(primary = design.blue, background = background, surface = background)
    }
    val tabs = listOf(Screen.Catalog, Screen.Search, Screen.Favorites, Screen.Cart, Screen.Profile)

    BackHandler(enabled = vm.screen.value != Screen.Catalog) { vm.goBack() }

    MaterialTheme(colorScheme = colors) {
        val snackbar = remember { SnackbarHostState() }
        LaunchedEffect(vm.message.value) {
            val text = vm.message.value
            if (text != null) {
                snackbar.showSnackbar(text)
                vm.message.value = null
            }
        }
        Scaffold(
            containerColor = background,
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = { if (vm.screen.value in tabs) BottomNav(vm) }
        ) { padding ->
            Box(Modifier.fillMaxSize().padding(padding).background(background)) {
                when (vm.screen.value) {
                    Screen.Catalog -> CatalogPage(vm, fg, panel)
                    Screen.Search -> SearchPage(vm, fg, panel)
                    Screen.Product -> ProductPage(vm, fg, panel)
                    Screen.Favorites -> FavoritesPage(vm, fg, panel)
                    Screen.Cart -> CartPage(vm, fg, panel)
                    Screen.Checkout -> CheckoutPage(vm, fg, panel)
                    Screen.Success -> SuccessPage(vm, fg, panel)
                    Screen.Profile -> ProfilePage(vm, fg, panel)
                    Screen.Login -> LoginPage(vm, fg, panel)
                    Screen.Orders -> OrdersPage(vm, fg, panel)
                    Screen.WorkerRegister -> WorkerRegisterPage(vm, fg)
                    Screen.WorkerDashboard -> WorkerDashboardPage(vm, fg, panel)
                    Screen.PublishProduct -> PublishProductPage(vm, fg)
                }
            }
        }
        if (vm.authPrompt.value) {
            AlertDialog(
                onDismissRequest = { vm.authPrompt.value = false },
                title = { Text("Нужен аккаунт") },
                text = { Text("Войдите или зарегистрируйтесь, чтобы продолжить.") },
                confirmButton = { TextButton(onClick = { vm.chooseAuth(false) }) { Text("Войти") } },
                dismissButton = { TextButton(onClick = { vm.chooseAuth(true) }) { Text("Зарегистрироваться") } }
            )
        }
    }
}
