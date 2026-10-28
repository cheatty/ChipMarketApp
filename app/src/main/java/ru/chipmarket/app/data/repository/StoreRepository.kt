package ru.chipmarket.app.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import ru.chipmarket.app.BuildConfig
import ru.chipmarket.app.data.local.SessionStorage
import ru.chipmarket.app.data.model.NewProduct
import ru.chipmarket.app.data.model.Order
import ru.chipmarket.app.data.model.OrderLine
import ru.chipmarket.app.data.model.Product
import ru.chipmarket.app.data.model.Profile
import ru.chipmarket.app.data.model.Seller
import ru.chipmarket.app.data.model.Session
import ru.chipmarket.app.data.remote.SupabaseClient
import ru.chipmarket.app.data.remote.SupabaseException
import java.io.ByteArrayOutputStream
import java.util.UUID

class StoreRepository(context: Context) {
    private val resolver = context.contentResolver
    private val storage = SessionStorage(context)
    private val baseUrl = BuildConfig.SUPABASE_URL.trimEnd('/')
    private val client = SupabaseClient(baseUrl, BuildConfig.SUPABASE_PUBLISHABLE_KEY) { session?.accessToken }

    val session: Session?
        get() = storage.load()

    val configured get() = client.configured
    val lastEmail get() = storage.lastEmail()

    private fun userId(): String = session?.userId ?: throw Exception("Войдите в аккаунт")

    private fun rows(path: String, auth: Boolean = true): List<JSONObject> {
        val array = JSONArray(client.request(path, auth = auth))
        val list = mutableListOf<JSONObject>()
        for (i in 0 until array.length()) list.add(array.getJSONObject(i))
        return list
    }

    private fun post(path: String, body: JSONObject, upsert: Boolean = false): String =
        client.request(path, "POST", body, upsert = upsert)

    private fun saveSession(json: JSONObject, email: String) {
        val user = json.getJSONObject("user")
        val expiresAt = System.currentTimeMillis() + json.optLong("expires_in", 3600) * 1000
        val newSession = Session(
            user.getString("id"),
            json.getString("access_token"),
            json.getString("refresh_token"),
            expiresAt,
            user.optString("email", email)
        )
        storage.save(newSession)
    }

    private fun login(email: String, password: String): JSONObject {
        val body = JSONObject().put("email", email).put("password", password)
        return JSONObject(client.request("/auth/v1/token?grant_type=password", "POST", body, auth = false))
    }

    suspend fun signIn(email: String, password: String) = withContext(Dispatchers.IO) {
        val result = try {
            login(email, password)
        } catch (e: SupabaseException) {
            if (e.code != "invalid_credentials") throw e
            login(email, client.oldPassword(email, password))
        }
        saveSession(result, email)
    }

    suspend fun signUp(email: String, password: String, name: String, username: String,
                       seller: Boolean, shopName: String = ""): Boolean = withContext(Dispatchers.IO) {
        val data = JSONObject()
            .put("full_name", name)
            .put("username", username)
            .put("shop_name", shopName)
            .put("account_type", if (seller) "worker" else "customer")
        val body = JSONObject().put("email", email).put("password", password).put("data", data)
        val result = JSONObject(client.request("/auth/v1/signup", "POST", body, auth = false))
        if (result.has("access_token")) {
            saveSession(result, email)
            true
        } else {
            false
        }
    }

    suspend fun resetPassword(email: String) = withContext(Dispatchers.IO) {
        client.request("/auth/v1/recover", "POST", JSONObject().put("email", email), auth = false)
    }

    suspend fun refreshSession() = withContext(Dispatchers.IO) {
        val current = session ?: return@withContext
        if (current.expiresAt > System.currentTimeMillis() + 60000) return@withContext
        try {
            val body = JSONObject().put("refresh_token", current.refreshToken)
            val result = JSONObject(client.request("/auth/v1/token?grant_type=refresh_token", "POST", body, auth = false))
            saveSession(result, current.email)
        } catch (e: SupabaseException) {
            signOut()
        }
    }

    fun signOut() = storage.clear()

    private fun sellers(): Map<String, Seller> {
        val result = mutableMapOf<String, Seller>()
        val array = JSONArray(client.request("/rest/v1/rpc/chipmarket_public_sellers", "POST", JSONObject(), auth = false))
        for (i in 0 until array.length()) {
            val row = array.getJSONObject(i)
            val seller = Seller(row.optString("id"), row.optString("username"), row.optString("shop_name"))
            result[seller.id] = seller
        }
        return result
    }

    suspend fun products(): List<Product> = withContext(Dispatchers.IO) {
        val sellers = try { sellers() } catch (e: Exception) { emptyMap() }
        val fields = "id,name,category,description,price,old_price,image_url,image_urls,seller_id,stock,details,kind"
        rows("/rest/v1/products?select=$fields&order=id.desc", auth = false).map { row ->
            val images = mutableListOf<String>()
            val array = row.optJSONArray("image_urls")
            if (array != null) {
                for (i in 0 until array.length()) images.add(array.getString(i))
            }
            val image = row.optString("image_url").takeIf { it.startsWith("https://") }
            if (images.isEmpty() && image != null) images.add(image)
            val sellerId = if (row.isNull("seller_id")) null else row.getString("seller_id")
            Product(
                id = row.getString("id"),
                title = row.optString("name"),
                category = row.optString("category"),
                description = row.optString("description"),
                price = row.getDouble("price").toInt(),
                oldPrice = if (row.isNull("old_price")) null else row.getInt("old_price"),
                imageUrl = image,
                images = images,
                sellerId = sellerId,
                seller = sellers[sellerId],
                stock = row.optInt("stock"),
                details = row.optString("details"),
                kind = row.optString("kind")
            )
        }
    }

    suspend fun mySeller(): Seller? = withContext(Dispatchers.IO) {
        val list = rows("/rest/v1/users?select=id&auth_user_id=eq.${userId()}")
        if (list.isEmpty()) null else sellers()[list[0].getString("id")]
    }

    suspend fun registerSeller(username: String, shopName: String, name: String, phone: String) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("p_username", username)
            .put("p_shop_name", shopName)
            .put("p_owner_name", name)
            .put("p_phone", phone)
        post("/rest/v1/rpc/chipmarket_register_seller_v2", body)
    }

    suspend fun registerCustomer(username: String) = withContext(Dispatchers.IO) {
        post("/rest/v1/rpc/chipmarket_set_customer_username", JSONObject().put("p_username", username))
    }

    private fun photoToJpeg(uri: Uri): ByteArray {
        val original = resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
            ?: throw Exception("Не удалось открыть фото")
        val scale = minOf(1f, 1600f / maxOf(original.width, original.height))
        val bitmap = Bitmap.createScaledBitmap(original, (original.width * scale).toInt(), (original.height * scale).toInt(), true)
        val output = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, output)
        return output.toByteArray()
    }

    suspend fun publishProduct(product: NewProduct, photos: List<Uri>) = withContext(Dispatchers.IO) {
        val user = userId()
        val urls = JSONArray()
        for (photo in photos) {
            val path = "products/$user/${UUID.randomUUID()}.jpg"
            client.upload(path, photoToJpeg(photo))
            urls.put("$baseUrl/storage/v1/object/public/chipmarket-assets/$path")
        }
        val body = JSONObject()
            .put("p_name", product.name)
            .put("p_category", product.category)
            .put("p_description", product.description)
            .put("p_price", product.price)
            .put("p_old_price", product.oldPrice ?: JSONObject.NULL)
            .put("p_stock", product.stock)
            .put("p_details", product.details)
            .put("p_kind", product.kind)
            .put("p_images", urls)
        post("/rest/v1/rpc/chipmarket_publish_product", body)
    }

    suspend fun cart(): Map<String, Int> = withContext(Dispatchers.IO) {
        val result = mutableMapOf<String, Int>()
        for (row in rows("/rest/v1/chipmarket_cart_items?select=product_id,quantity&user_id=eq.${userId()}")) {
            result[row.getString("product_id")] = row.getInt("quantity")
        }
        result
    }

    suspend fun setCart(productId: String, quantity: Int) = withContext(Dispatchers.IO) {
        val user = userId()
        if (quantity <= 0) {
            client.request("/rest/v1/chipmarket_cart_items?user_id=eq.$user&product_id=eq.$productId", "DELETE")
        } else {
            val body = JSONObject().put("user_id", user).put("product_id", productId).put("quantity", quantity)
            post("/rest/v1/chipmarket_cart_items?on_conflict=user_id,product_id", body, upsert = true)
        }
    }

    suspend fun favorites(): Set<String> = withContext(Dispatchers.IO) {
        rows("/rest/v1/chipmarket_favorites?select=product_id&user_id=eq.${userId()}")
            .map { it.getString("product_id") }
            .toSet()
    }

    suspend fun addFavorite(productId: String) = withContext(Dispatchers.IO) {
        val body = JSONObject().put("user_id", userId()).put("product_id", productId)
        post("/rest/v1/chipmarket_favorites?on_conflict=user_id,product_id", body, upsert = true)
    }

    suspend fun removeFavorite(productId: String) = withContext(Dispatchers.IO) {
        client.request("/rest/v1/chipmarket_favorites?user_id=eq.${userId()}&product_id=eq.$productId", "DELETE")
    }

    suspend fun profile(): Profile = withContext(Dispatchers.IO) {
        val list = rows("/rest/v1/chipmarket_profiles?select=full_name,phone,address&user_id=eq.${userId()}")
        if (list.isEmpty()) {
            Profile()
        } else {
            Profile(list[0].optString("full_name"), list[0].optString("phone"), list[0].optString("address"))
        }
    }

    suspend fun saveProfile(profile: Profile) = withContext(Dispatchers.IO) {
        val body = JSONObject()
            .put("user_id", userId())
            .put("full_name", profile.fullName)
            .put("phone", profile.phone)
            .put("address", profile.address)
        post("/rest/v1/chipmarket_profiles?on_conflict=user_id", body, upsert = true)
    }

    suspend fun role(): String = withContext(Dispatchers.IO) {
        val text = client.request("/rest/v1/rpc/chipmarket_my_role").replace("\"", "").trim()
        if (text.isEmpty() || text == "null") "User" else text
    }

    suspend fun orders(): List<Order> = withContext(Dispatchers.IO) {
        val path = "/rest/v1/chipmarket_orders?select=id,status,total,created_at,payment_method" +
            "&user_id=eq.${userId()}&order=created_at.desc"
        rows(path).map {
            Order(it.getString("id"), it.getString("status"), it.getInt("total"),
                it.getString("created_at"), it.getString("payment_method"))
        }
    }

    suspend fun orderLines(orderId: String): List<OrderLine> = withContext(Dispatchers.IO) {
        rows("/rest/v1/chipmarket_order_items?select=product_id,title,price,quantity&order_id=eq.$orderId").map {
            val productId = if (it.isNull("product_id")) null else it.getString("product_id")
            OrderLine(productId, it.getString("title"), it.getInt("price"), it.getInt("quantity"))
        }
    }

    suspend fun placeOrder(address: String, payment: String): String = withContext(Dispatchers.IO) {
        val body = JSONObject().put("p_address", address).put("p_payment_method", payment)
        post("/rest/v1/rpc/chipmarket_place_order", body).replace("\"", "").trim()
    }
}
