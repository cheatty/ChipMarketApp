package ru.chipmarket.app.data.remote

import android.util.Base64
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

class SupabaseException(val status: Int, val code: String, text: String) : Exception(text)

class SupabaseClient(
    private val baseUrl: String,
    private val key: String,
    private val token: () -> String?
) {
    val configured: Boolean
        get() = baseUrl.startsWith("https://") && key.isNotBlank()

    fun request(
        path: String,
        method: String = "GET",
        body: JSONObject? = null,
        auth: Boolean = true,
        upsert: Boolean = false
    ): String {
        val connection = open(path, method, auth)
        connection.setRequestProperty("Accept", "application/json")
        if (upsert) connection.setRequestProperty("Prefer", "resolution=merge-duplicates")
        if (body != null) {
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            send(connection, body.toString().toByteArray())
        }
        return read(connection)
    }

    fun upload(path: String, bytes: ByteArray) {
        val connection = open("/storage/v1/object/chipmarket-assets/$path", "POST", true)
        connection.setRequestProperty("Content-Type", "image/jpeg")
        send(connection, bytes)
        read(connection)
    }

    private fun open(path: String, method: String, auth: Boolean): HttpURLConnection {
        if (!configured) throw Exception("Не настроено подключение к Supabase")
        val connection = URL(baseUrl + path).openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.connectTimeout = 15000
        connection.readTimeout = 30000
        connection.setRequestProperty("apikey", key)
        val accessToken = token()
        if (auth && accessToken != null) {
            connection.setRequestProperty("Authorization", "Bearer $accessToken")
        }
        return connection
    }

    private fun send(connection: HttpURLConnection, bytes: ByteArray) {
        connection.doOutput = true
        connection.outputStream.use { it.write(bytes) }
    }

    private fun read(connection: HttpURLConnection): String {
        try {
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            if (status !in 200..299) throw makeError(status, text)
            return text
        } catch (e: SupabaseException) {
            throw e
        } catch (e: Exception) {
            throw Exception("Нет соединения с сервером. Проверьте интернет.")
        } finally {
            connection.disconnect()
        }
    }

    private fun makeError(status: Int, text: String): SupabaseException {
        val json = try { JSONObject(text) } catch (e: Exception) { JSONObject() }
        val code = json.optString("error_code", json.optString("code", ""))
        val serverText = json.optString("msg", json.optString("message", "Ошибка сервера ($status)"))
        val message = when (code) {
            "invalid_credentials" -> "Неверная почта или пароль"
            "email_exists", "user_already_exists" -> "Этот адрес уже зарегистрирован"
            "email_not_confirmed" -> "Подтвердите почту по ссылке из письма"
            "weak_password" -> "Слишком простой пароль"
            else -> serverText
        }
        return SupabaseException(status, code, message)
    }

    fun oldPassword(email: String, password: String): String {
        val salt = "ChipMarket/Auth/v1:${baseUrl.lowercase()}:${email.trim().lowercase()}"
        val spec = PBEKeySpec(password.toCharArray(), salt.toByteArray(), 120000, 256)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
    }
}
