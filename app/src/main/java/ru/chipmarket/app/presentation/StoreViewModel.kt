package ru.chipmarket.app.presentation

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.chipmarket.app.data.model.NewProduct
import ru.chipmarket.app.data.model.Order
import ru.chipmarket.app.data.model.OrderLine
import ru.chipmarket.app.data.model.Product
import ru.chipmarket.app.data.model.Profile
import ru.chipmarket.app.data.model.Seller
import ru.chipmarket.app.data.repository.StoreRepository
import ru.chipmarket.app.navigation.Screen

class StoreViewModel(application: Application) : AndroidViewModel(application) {
    val repository = StoreRepository(application)

    val screen = mutableStateOf(Screen.Catalog)
    val darkMode = mutableStateOf(false)
    val loading = mutableStateOf(false)
    val message = mutableStateOf<String?>(null)
    val authMessage = mutableStateOf<String?>(null)
    val authPrompt = mutableStateOf(false)
    val registrationMode = mutableStateOf(false)

    val products = mutableStateOf<List<Product>>(emptyList())
    val cart = mutableStateOf<Map<String, Int>>(emptyMap())
    val favorites = mutableStateOf<Set<String>>(emptySet())
    val profile = mutableStateOf(Profile())
    val userRole = mutableStateOf("User")
    val seller = mutableStateOf<Seller?>(null)
    val orders = mutableStateOf<List<Order>>(emptyList())
    val orderItems = mutableStateOf<Map<String, List<OrderLine>>>(emptyMap())
    val selectedProduct = mutableStateOf<Product?>(null)
    val query = mutableStateOf("")
    val category = mutableStateOf("Все")

    val successOrder = mutableStateOf("")
    val successLines = mutableStateOf<List<OrderLine>>(emptyList())
    val successTotal = mutableStateOf(0)
    val successPayment = mutableStateOf("")

    private val previousScreen = mutableStateOf(Screen.Catalog)
    private val afterLogin = mutableStateOf(Screen.Catalog)

    val signedIn get() = repository.session != null
    val isSeller get() = userRole.value == "Worker"

    init {
        refresh()
    }

    private fun runTask(forLogin: Boolean = false, block: suspend () -> Unit) {
        viewModelScope.launch {
            loading.value = true
            try {
                block()
            } catch (e: Exception) {
                val text = e.message ?: "Ошибка. Проверьте интернет."
                if (forLogin) authMessage.value = text else message.value = text
            }
            loading.value = false
        }
    }

    private suspend fun loadData() {
        if (!repository.configured) {
            message.value = "Добавьте адрес и ключ Supabase в supabase.properties"
            return
        }
        repository.refreshSession()
        products.value = repository.products()
        if (signedIn) {
            cart.value = repository.cart()
            favorites.value = repository.favorites()
            profile.value = repository.profile()
            userRole.value = repository.role()
            seller.value = if (isSeller) repository.mySeller() else null
            orders.value = repository.orders()
            val items = mutableMapOf<String, List<OrderLine>>()
            for (order in orders.value.take(20)) items[order.id] = repository.orderLines(order.id)
            orderItems.value = items
        } else {
            clearUserData()
        }
    }

    private fun clearUserData() {
        cart.value = emptyMap()
        favorites.value = emptySet()
        profile.value = Profile()
        userRole.value = "User"
        seller.value = null
        orders.value = emptyList()
        orderItems.value = emptyMap()
    }

    fun refresh() = runTask { loadData() }

    fun openProduct(product: Product) {
        if (screen.value != Screen.Product) previousScreen.value = screen.value
        selectedProduct.value = product
        screen.value = Screen.Product
    }

    fun goBack() {
        screen.value = when (screen.value) {
            Screen.Product -> previousScreen.value
            Screen.Checkout -> Screen.Cart
            Screen.Success -> Screen.Orders
            Screen.Orders -> Screen.Profile
            Screen.PublishProduct -> Screen.WorkerDashboard
            Screen.WorkerDashboard -> Screen.Profile
            Screen.Login -> afterLogin.value
            Screen.WorkerRegister -> if (signedIn) Screen.Profile else Screen.Login
            else -> Screen.Catalog
        }
    }

    fun navigate(target: Screen) {
        val needLogin = listOf(Screen.Checkout, Screen.Orders, Screen.WorkerDashboard, Screen.PublishProduct)
        if (target in needLogin && !signedIn) {
            showAuth()
            return
        }
        if ((target == Screen.WorkerDashboard || target == Screen.PublishProduct) && !isSeller) {
            message.value = "Этот раздел только для продавцов"
            return
        }
        if (target == Screen.Login) {
            registrationMode.value = false
            afterLogin.value = screen.value
        }
        screen.value = target
    }

    fun showAuth() {
        afterLogin.value = screen.value
        authPrompt.value = true
    }

    fun chooseAuth(register: Boolean) {
        authPrompt.value = false
        registrationMode.value = register
        authMessage.value = null
        screen.value = Screen.Login
    }

    fun signIn(email: String, password: String) = runTask(forLogin = true) {
        authMessage.value = null
        repository.signIn(email.trim(), password)
        loadData()
        screen.value = afterLogin.value
    }

    fun signUp(username: String, email: String, password: String) = runTask(forLogin = true) {
        authMessage.value = null
        if (username.trim().length < 3) throw Exception("Ник должен быть не короче 3 символов")
        val active = repository.signUp(email.trim(), password, username.trim(), username.trim(), seller = false)
        if (active) {
            repository.registerCustomer(username.trim())
            loadData()
            screen.value = afterLogin.value
        } else {
            registrationMode.value = false
            authMessage.value = "Регистрация создана. Подтвердите почту по ссылке из письма и войдите."
        }
    }

    fun signUpSeller(username: String, shopName: String, name: String, phone: String,
                     email: String, password: String) = runTask(forLogin = true) {
        authMessage.value = null
        if (username.trim().length < 3) throw Exception("Username должен быть не короче 3 символов")
        if (shopName.isBlank() || name.isBlank()) throw Exception("Заполните название магазина и имя")
        val active = repository.signUp(email.trim(), password, name.trim(), username.trim(), seller = true, shopName = shopName.trim())
        if (!active) throw Exception("Подтвердите почту по ссылке из письма и войдите")
        repository.registerSeller(username.trim(), shopName.trim(), name.trim(), phone.trim())
        repository.saveProfile(Profile(name.trim(), phone.trim(), ""))
        loadData()
        screen.value = Screen.WorkerDashboard
    }

    fun signOut() {
        repository.signOut()
        clearUserData()
        screen.value = Screen.Catalog
    }

    fun resetPassword(email: String) = runTask(forLogin = true) {
        if (!email.contains("@")) throw Exception("Введите электронную почту")
        repository.resetPassword(email.trim())
        authMessage.value = "Письмо для восстановления отправлено"
    }

    fun changeCart(product: Product, delta: Int) {
        if (!signedIn) {
            showAuth()
            return
        }
        val quantity = ((cart.value[product.id] ?: 0) + delta).coerceIn(0, 99)
        runTask {
            repository.setCart(product.id, quantity)
            cart.value = repository.cart()
        }
    }

    fun toggleFavorite(product: Product) {
        if (!signedIn) {
            showAuth()
            return
        }
        runTask {
            if (product.id in favorites.value) repository.removeFavorite(product.id)
            else repository.addFavorite(product.id)
            favorites.value = repository.favorites()
        }
    }

    fun saveProfile(newProfile: Profile) = runTask {
        repository.saveProfile(newProfile)
        profile.value = newProfile
        message.value = "Профиль сохранён"
    }

    fun placeOrder(address: String, payment: String) = runTask {
        successOrder.value = repository.placeOrder(address, payment)
        successLines.value = repository.orderLines(successOrder.value)
        successTotal.value = successLines.value.sumOf { it.price * it.quantity }
        successPayment.value = payment
        loadData()
        screen.value = Screen.Success
    }

    fun publishProduct(product: NewProduct, photos: List<Uri>) = runTask {
        repository.refreshSession()
        repository.publishProduct(product, photos)
        products.value = repository.products()
        message.value = "Товар опубликован"
        screen.value = Screen.WorkerDashboard
    }

    fun similarProducts(product: Product): List<Product> =
        products.value.filter { it.id != product.id && it.category == product.category }.take(8)
}
