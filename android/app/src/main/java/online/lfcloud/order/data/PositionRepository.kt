package online.lfcloud.order.data

import android.content.Context
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.Position
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 方位的本地存储。方位是模块下的容器，sortOrder 决定顺序。 */
class PositionRepository(context: Context) {

    private val file = File(context.filesDir, "positions.json")

    fun load(modules: List<Module>): MutableList<Position> {
        if (!file.exists()) {
            val seed = seedData(modules)
            save(seed)
            return seed.toMutableList()
        }
        return runCatching {
            val arr = JSONArray(file.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Position(
                    id = o.getString("id"),
                    moduleId = o.getString("moduleId"),
                    name = o.getString("name"),
                    sortOrder = o.optInt("sortOrder", 0),
                    itemCount = o.optInt("itemCount", 0),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis()),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            }.toMutableList()
        }.getOrElse { seedData(modules).toMutableList() }
    }

    fun save(list: List<Position>) {
        val arr = JSONArray()
        list.forEach { p ->
            arr.put(JSONObject().apply {
                put("id", p.id)
                put("moduleId", p.moduleId)
                put("name", p.name)
                put("sortOrder", p.sortOrder)
                put("itemCount", p.itemCount)
                put("updatedAt", p.updatedAt)
                put("createdAt", p.createdAt)
            })
        }
        runCatching { file.writeText(arr.toString()) }
    }

    private fun seedData(modules: List<Module>): List<Position> {
        val now = System.currentTimeMillis()
        val hour = 60L * 60L * 1000L
        val day = 24L * hour
        val out = mutableListOf<Position>()

        fun add(module: Module?, namesAndCounts: List<Pair<String, Int>>, ageDays: Long) {
            val m = module ?: return
            namesAndCounts.forEachIndexed { i, (name, count) ->
                out += Position(
                    moduleId = m.id, name = name, sortOrder = i, itemCount = count,
                    createdAt = now - ageDays * day, updatedAt = now - ageDays * day + i * hour
                )
            }
        }

        fun modOf(moduleName: String) = modules.firstOrNull { it.name == moduleName }

        add(modOf("桌面"), listOf("左边" to 12, "中间" to 20, "右边" to 10), 80)
        add(modOf("抽屉"), listOf("第一层" to 8, "第二层" to 6, "第三层" to 4, "底层" to 0), 70)
        add(
            modOf("衣柜"),
            listOf("上层" to 20, "中层" to 25, "下层" to 15, "左侧" to 0, "右侧" to 0, "挂杆" to 0),
            60
        )
        add(modOf("床头柜"), listOf("台面" to 4, "抽屉" to 2), 30)
        add(modOf("工位"), listOf("台面" to 15, "抽屉" to 5), 50)
        add(modOf("储物柜"), listOf("第一层" to 2, "第二层" to 2, "第三层" to 0), 50)

        val bookModule = modules.firstOrNull { it.name == "书架" && it.positionCount > 0 }
        add(
            bookModule,
            listOf("第一层" to 3, "第二层" to 3, "第三层" to 3, "第四层" to 2, "第五层" to 1),
            44
        )

        return out
    }
}
