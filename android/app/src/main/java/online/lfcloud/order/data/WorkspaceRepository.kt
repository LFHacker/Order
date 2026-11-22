package online.lfcloud.order.data

import android.content.Context
import online.lfcloud.order.model.Workspace
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/**
 * 本地存储：先用一个 JSON 文件落地，等 C++ 核心接上来再替换为 SQLite。
 * 写入立即落盘，符合"本地优先"的设计。
 */
class WorkspaceRepository(context: Context) {

    private val file = File(context.filesDir, "workspaces.json")

    fun load(): MutableList<Workspace> {
        if (!file.exists()) {
            val seed = seedData()
            save(seed)
            return seed.toMutableList()
        }
        return runCatching {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Workspace(
                    id = o.getString("id"),
                    name = o.getString("name"),
                    iconId = if (o.isNull("iconId")) null else o.getString("iconId"),
                    description = o.optString("description", ""),
                    itemCount = o.optInt("itemCount", 0),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    isCurrent = o.optBoolean("isCurrent", false)
                )
            }.toMutableList()
        }.getOrElse { seedData().toMutableList() }
    }

    fun save(list: List<Workspace>) {
        val arr = JSONArray()
        list.forEach { w ->
            arr.put(JSONObject().apply {
                put("id", w.id)
                put("name", w.name)
                put("iconId", w.iconId ?: JSONObject.NULL)
                put("description", w.description)
                put("itemCount", w.itemCount)
                put("updatedAt", w.updatedAt)
                put("createdAt", w.createdAt)
                put("isCurrent", w.isCurrent)
            })
        }
        runCatching { file.writeText(arr.toString()) }
    }

    private fun seedData(): List<Workspace> {
        val now = System.currentTimeMillis()
        val hour = 60L * 60L * 1000L
        val day = 24L * hour
        return listOf(
            Workspace(name = "家", iconId = "home", description = "主卧、客厅与阳台", itemCount = 126,
                updatedAt = now - 2 * hour, createdAt = now - 90 * day, isCurrent = true),
            Workspace(name = "公司", iconId = "building", description = "工位与储物柜", itemCount = 24,
                updatedAt = now - day, createdAt = now - 60 * day),
            Workspace(name = "书房", iconId = null, description = "书架与抽屉", itemCount = 12,
                updatedAt = now - 3 * day, createdAt = now - 45 * day),
            Workspace(name = "储藏室", iconId = null, description = "", itemCount = 0,
                updatedAt = now - 14 * day, createdAt = now - 30 * day)
        )
    }
}
