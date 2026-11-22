package online.lfcloud.order.net

import android.content.Context
import org.json.JSONObject
import java.util.UUID

data class AuthResult(
    val token: String,
    val userId: String,
    val username: String,
    val expiresAtMillis: Long
)

object AuthApi {

    fun register(username: String, password: String, deviceId: String): AuthResult {
        val data = Api.post(
            "/api/v1/auth/register",
            JSONObject().apply {
                put("username", username)
                put("password", password)
                put("device_id", deviceId)
            }
        )
        return parse(data)
    }

    fun login(username: String, password: String, deviceId: String): AuthResult {
        val data = Api.post(
            "/api/v1/auth/login",
            JSONObject().apply {
                put("username", username)
                put("password", password)
                put("device_id", deviceId)
            }
        )
        return parse(data)
    }

    fun me(token: String): String {
        val data = Api.get("/api/v1/auth/me", token)
        return data.optJSONObject("user")?.optString("username").orEmpty()
    }

    fun logout(token: String) {
        Api.post("/api/v1/auth/logout", JSONObject(), token)
    }

    private fun parse(data: JSONObject): AuthResult {
        val token = data.optString("token")
        val user = data.optJSONObject("user")
        if (token.isEmpty() || user == null) {
            throw ApiException("BAD_RESPONSE", "服务器未返回 token")
        }
        return AuthResult(
            token = token,
            userId = user.optString("user_id"),
            username = user.optString("username"),
            expiresAtMillis = parseExpiry(data.optString("expires_at"))
        )
    }

    /** expires_at 为 ISO-8601（如 2026-12-22T23:51:15Z）；解析失败按 30 天处理。 */
    private fun parseExpiry(raw: String?): Long {
        if (raw.isNullOrBlank()) return System.currentTimeMillis() + 30L * 24 * 3600 * 1000
        return runCatching { java.time.Instant.parse(raw).toEpochMilli() }
            .getOrElse { System.currentTimeMillis() + 30L * 24 * 3600 * 1000 }
    }
}

/** 会话与设备标识的本地存放。 */
class SessionStore(context: Context) {

    private val prefs = context.getSharedPreferences("order_session", Context.MODE_PRIVATE)

    val token: String? get() = prefs.getString("token", null)

    var username: String
        get() = prefs.getString("username", "") ?: ""
        set(value) {
            prefs.edit().putString("username", value).apply()
        }

    var userId: String
        get() = prefs.getString("user_id", "") ?: ""
        set(value) {
            prefs.edit().putString("user_id", value).apply()
        }

    /** token 到期时间（epoch millis），0 表示未知。 */
    val expiresAt: Long get() = prefs.getLong("expires_at", 0L)

    fun save(auth: AuthResult) {
        prefs.edit()
            .putString("token", auth.token)
            .putString("username", auth.username)
            .putString("user_id", auth.userId)
            .putLong("expires_at", auth.expiresAtMillis)
            .apply()
    }

    /** 本地判断登录态：有 token 且未过期。 */
    fun hasValidSession(): Boolean {
        val t = token ?: return false
        if (t.isEmpty()) return false
        val exp = expiresAt
        return exp > System.currentTimeMillis()
    }

    fun clear() {
        prefs.edit()
            .remove("token").remove("username").remove("user_id").remove("expires_at")
            .apply()
    }

    /** 设备标识：首次安装生成一次，之后不变（服务端用它判定写入权）。 */
    fun deviceId(): String {
        val existing = prefs.getString("device_id", null)
        if (!existing.isNullOrBlank()) return existing
        val generated = "and-" + UUID.randomUUID().toString().take(8)
        prefs.edit().putString("device_id", generated).apply()
        return generated
    }
}
