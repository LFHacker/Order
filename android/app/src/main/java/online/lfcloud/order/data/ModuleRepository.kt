package online.lfcloud.order.data

import android.content.Context
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.Workspace
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 模块的本地存储，与工作区同一套 JSON 方案。 */
class ModuleRepository(context: Context) {

    private val file = File(context.filesDir, "modules.json")

    fun load(workspaces: List<Workspace>): MutableList<Module> {
        if (!file.exists()) {
            val seed = seedData(workspaces)
            save(seed)
            return seed.toMutableList()
        }
        return runCatching {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Module(
                    id = o.getString("id"),
                    workspaceId = o.getString("workspaceId"),
                    name = o.getString("name"),
                    iconId = if (o.isNull("iconId")) null else o.getString("iconId"),
                    description = o.optString("description", ""),
                    positionCount = o.optInt("positionCount", 0),
                    itemCount = o.optInt("itemCount", 0),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            }.toMutableList()
        }.getOrElse { seedData(workspaces).toMutableList() }
    }

    fun save(list: List<Module>) {
        val arr = JSONArray()
        list.forEach { m ->
            arr.put(JSONObject().apply {
                put("id", m.id)
                put("workspaceId", m.workspaceId)
                put("name", m.name)
                put("iconId", m.iconId ?: JSONObject.NULL)
                put("description", m.description)
                put("positionCount", m.positionCount)
                put("itemCount", m.itemCount)
                put("updatedAt", m.updatedAt)
                put("createdAt", m.createdAt)
            })
        }
        runCatching { file.writeText(arr.toString()) }
    }

    private fun seedData(workspaces: List<Workspace>): List<Module> {
        val now = System.currentTimeMillis()
        val hour = 60L * 60L * 1000L
        val day = 24L * hour
        fun ws(name: String) = workspaces.firstOrNull { it.name == name }?.id

        val list = mutableListOf<Module>()
        ws("家")?.let { id ->
            list += Module(workspaceId = id, name = "桌面", iconId = "monitor", positionCount = 3,
                itemCount = 42, updatedAt = now - 2 * hour, createdAt = now - 80 * day,
                description = "靠窗的长桌，平时放电脑和工具")
            list += Module(workspaceId = id, name = "抽屉", iconId = "drawer", positionCount = 4,
                itemCount = 18, updatedAt = now - day, createdAt = now - 70 * day)
            list += Module(workspaceId = id, name = "衣柜", iconId = "chest", positionCount = 6,
                itemCount = 60, updatedAt = now - 3 * day, createdAt = now - 60 * day)
            list += Module(workspaceId = id, name = "书架", iconId = "shelf", positionCount = 0,
                itemCount = 0, updatedAt = now - 14 * day, createdAt = now - 40 * day)
            list += Module(workspaceId = id, name = "床头柜", iconId = null, positionCount = 2,
                itemCount = 6, updatedAt = now - 14 * day, createdAt = now - 30 * day)
        }
        ws("公司")?.let { id ->
            list += Module(workspaceId = id, name = "工位", iconId = "monitor", positionCount = 2,
                itemCount = 20, updatedAt = now - day, createdAt = now - 50 * day)
            list += Module(workspaceId = id, name = "储物柜", iconId = "lock", positionCount = 3,
                itemCount = 4, updatedAt = now - 4 * day, createdAt = now - 50 * day)
        }
        ws("书房")?.let { id ->
            list += Module(workspaceId = id, name = "书架", iconId = "shelf", positionCount = 5,
                itemCount = 12, updatedAt = now - 3 * day, createdAt = now - 44 * day)
        }
        return list
    }
}
