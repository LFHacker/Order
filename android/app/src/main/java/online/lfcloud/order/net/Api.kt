package online.lfcloud.order.net

import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** 服务端返回的业务错误：code 与 API 文档的错误码一致。 */
class ApiException(
    val code: String,
    override val message: String,
    val status: Int = 0
) : Exception(message) {
    /** 给用户看的一句话；技术细节留在 code 里。 */
    fun friendly(): String = when (code) {
        "UNAUTHORIZED" -> message.ifBlank { "账号或密码不正确" }
        "CONFLICT" -> message.ifBlank { "该账号已被注册" }
        "BAD_REQUEST" -> message.ifBlank { "填写内容不合规" }
        "RATE_LIMITED" -> "操作太频繁，请稍后再试"
        "NETWORK" -> "连接不上服务器，请检查网络"
        else -> message.ifBlank { "请求失败（$code）" }
    }
}

/**
 * 与 order-api 通信的最小 HTTP 客户端：HttpURLConnection + JSON 信封。
 * 信封：{ok, data, server_time} / {ok:false, error:{code,message}}
 */
object Api {

    /** 服务地址。Caddy 把 /order-api 前缀剥掉后转发给 Flask。 */
    const val BASE = "https://api.l-f-cloud.online:8443/order-api"

    fun post(path: String, body: JSONObject, token: String? = null): JSONObject =
        request("POST", path, body, token)

    fun get(path: String, token: String? = null): JSONObject =
        request("GET", path, null, token)

    private fun request(
        method: String,
        path: String,
        body: JSONObject?,
        token: String?
    ): JSONObject {
        val conn = try {
            (URL(BASE + path).openConnection() as HttpURLConnection).apply {
                requestMethod = method
                connectTimeout = 15_000
                readTimeout = 20_000
                useCaches = false
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Content-Type", "application/json; charset=utf-8")
                if (token != null) setRequestProperty("Authorization", "Bearer $token")
                if (body != null) doOutput = true
            }
        } catch (e: Exception) {
            throw ApiException("NETWORK", "连接不上服务器，请检查网络")
        }

        try {
            if (body != null) {
                conn.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val status = conn.responseCode
            val stream = if (status in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader()?.use { it.readText() } ?: ""
            val json = runCatching { JSONObject(text) }.getOrNull()
                ?: throw ApiException("BAD_RESPONSE", "服务器返回内容无法解析", status)

            if (!json.optBoolean("ok", false)) {
                val err = json.optJSONObject("error")
                throw ApiException(
                    err?.optString("code").orEmpty().ifEmpty { "UNKNOWN" },
                    err?.optString("message").orEmpty(),
                    status
                )
            }
            return json.optJSONObject("data") ?: JSONObject()
        } catch (e: ApiException) {
            throw e
        } catch (e: Exception) {
            throw ApiException("NETWORK", "网络异常：${e.message ?: "未知错误"}")
        } finally {
            conn.disconnect()
        }
    }
}
