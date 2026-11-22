package online.lfcloud.order.net

import android.content.Context
import org.json.JSONObject
import java.util.UUID

data class AuthResult(val token: String, val userId: String, val username: String)

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
            username = user.optString("username")
        )
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

    fun save(auth: AuthResult) {
        prefs.edit()
            .putString("token", auth.token)
            .putString("username", auth.username)
            .putString("user_id", auth.userId)
            .apply()
    }

    fun clear() {
        prefs.edit().remove("token").remove("username").remove("user_id").apply()
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
