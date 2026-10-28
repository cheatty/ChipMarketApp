package ru.chipmarket.app.data.model

data class Product(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val price: Int,
    val oldPrice: Int?,
    val imageUrl: String?,
    val images: List<String>,
    val sellerId: String?,
    val seller: Seller?,
    val stock: Int,
    val details: String,
    val kind: String
)

data class Seller(
    val id: String,
    val username: String,
    val shopName: String
)

data class NewProduct(
    val name: String,
    val category: String,
    val description: String,
    val price: Int,
    val oldPrice: Int?,
    val stock: Int,
    val details: String,
    val kind: String
)

data class Order(
    val id: String,
    val status: String,
    val total: Int,
    val createdAt: String,
    val paymentMethod: String
)

data class OrderLine(
    val productId: String?,
    val title: String,
    val price: Int,
    val quantity: Int
)

data class Profile(
    val fullName: String = "",
    val phone: String = "",
    val address: String = ""
)

data class Session(
    val userId: String,
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val email: String
)
