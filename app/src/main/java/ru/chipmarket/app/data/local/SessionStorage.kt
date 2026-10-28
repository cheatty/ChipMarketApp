package ru.chipmarket.app.data.local

import android.content.Context
import ru.chipmarket.app.data.model.Session

class SessionStorage(context: Context) {
    private val prefs = context.getSharedPreferences("chipmarket_auth", Context.MODE_PRIVATE)

    fun load(): Session? {
        val userId = prefs.getString("user", null) ?: return null
        val access = prefs.getString("access", null) ?: return null
        val refresh = prefs.getString("refresh", null) ?: return null
        val expiresAt = prefs.getLong("expires_at", 0)
        val email = prefs.getString("email", "") ?: ""
        return Session(userId, access, refresh, expiresAt, email)
    }

    fun save(session: Session) {
        prefs.edit()
            .putString("user", session.userId)
            .putString("access", session.accessToken)
            .putString("refresh", session.refreshToken)
            .putLong("expires_at", session.expiresAt)
            .putString("email", session.email)
            .putString("last_email", session.email)
            .apply()
    }

    fun clear() {
        val email = lastEmail()
        prefs.edit().clear().putString("last_email", email).apply()
    }

    fun lastEmail(): String = prefs.getString("last_email", "") ?: ""
}
