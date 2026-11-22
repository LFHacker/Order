package online.lfcloud.order.data

import android.content.Context
import org.json.JSONObject
import java.io.File

/** 账号资料。当前没有服务端登录，先存本地，字段与将来接口一致。 */
data class Account(
    val username: String = "李沣",
    val uid: String = "u_8f3c2a1b",
    val phone: String = "138 8888 8888",
    val email: String = "3209391487@qq.com",
    val password: String = ""
) {
    companion object {
        const val FIELD_NAME = "用户名"
        const val FIELD_PHONE = "手机号"
        const val FIELD_EMAIL = "邮箱"
    }
}

class AccountRepository(context: Context) {

    private val file = File(context.filesDir, "account.json")
    private val prefs = context.getSharedPreferences("order_settings", Context.MODE_PRIVATE)

    fun load(): Account {
        if (!file.exists()) return Account()
        return runCatching {
            val o = JSONObject(file.readText())
            Account(
                username = o.optString("username", "李沣"),
                uid = o.optString("uid", "u_8f3c2a1b"),
                phone = o.optString("phone", "138 8888 8888"),
                email = o.optString("email", "3209391487@qq.com"),
                password = o.optString("password", "")
            )
        }.getOrElse { Account() }
    }

    fun save(account: Account) {
        runCatching {
            file.writeText(
                JSONObject().apply {
                    put("username", account.username)
                    put("uid", account.uid)
                    put("phone", account.phone)
                    put("email", account.email)
                    put("password", account.password)
                }.toString()
            )
        }
    }

    /** 主题：跟随系统 / 浅色 / 深色 */
    fun loadTheme(): String = prefs.getString("theme", "跟随系统") ?: "跟随系统"

    fun saveTheme(value: String) = prefs.edit().putString("theme", value).apply()

    /** 缓存目录大小（字节） */
    fun cacheSize(context: Context): Long {
        fun walk(dir: File?): Long {
            if (dir == null || !dir.exists()) return 0L
            var total = 0L
            dir.listFiles()?.forEach { f ->
                total += if (f.isDirectory) walk(f) else f.length()
            }
            return total
        }
        return walk(context.cacheDir) + walk(File(context.filesDir, "images").parentFile?.let {
            File(context.filesDir, "cache")
        })
    }

    fun clearCache(context: Context) {
        context.cacheDir?.listFiles()?.forEach { it.deleteRecursively() }
    }
}
