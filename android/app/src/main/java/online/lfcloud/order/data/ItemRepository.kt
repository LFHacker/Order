package online.lfcloud.order.data

import android.content.Context
import online.lfcloud.order.model.Attachment
import online.lfcloud.order.model.Item
import online.lfcloud.order.model.ItemEvent
import online.lfcloud.order.model.Position
import online.lfcloud.order.model.Relation
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 物品、附件、关系、事件的本地存储。 */
class ItemRepository(context: Context) {

    private val itemsFile = File(context.filesDir, "items.json")
    private val attsFile = File(context.filesDir, "attachments.json")
    private val relsFile = File(context.filesDir, "relations.json")
    private val evsFile = File(context.filesDir, "events.json")

    // ---------- 物品 ----------

    fun loadItems(positions: List<Position>): MutableList<Item> {
        if (!itemsFile.exists()) {
            val seed = seedItems(positions)
            saveItems(seed)
            return seed.toMutableList()
        }
        return runCatching {
            val arr = JSONArray(itemsFile.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Item(
                    id = o.getString("id"),
                    positionId = o.getString("positionId"),
                    name = o.getString("name"),
                    category = o.optString("category", "其他"),
                    quantity = o.optInt("quantity", 1),
                    quantityUnit = o.optString("quantityUnit", "件"),
                    note = o.optString("note", ""),
                    status = o.optString("status", "在位"),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis()),
                    updatedAt = o.optLong("updatedAt", System.currentTimeMillis())
                )
            }.toMutableList()
        }.getOrElse { seedItems(positions).toMutableList() }
    }

    fun saveItems(list: List<Item>) {
        val arr = JSONArray()
        list.forEach { putItem(arr, it) }
        runCatching { itemsFile.writeText(arr.toString()) }
    }

    private fun putItem(arr: JSONArray, i: Item) {
        arr.put(JSONObject().apply {
            put("id", i.id); put("positionId", i.positionId); put("name", i.name)
            put("category", i.category); put("quantity", i.quantity)
            put("quantityUnit", i.quantityUnit); put("note", i.note); put("status", i.status)
            put("createdAt", i.createdAt); put("updatedAt", i.updatedAt)
        })
    }

    // ---------- 附件 ----------

    fun loadAttachments(): MutableList<Attachment> {
        if (!attsFile.exists()) {
            val seed = seedAttachments()
            saveAttachments(seed)
            return seed.toMutableList()
        }
        return runCatching {
            val arr = JSONArray(attsFile.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Attachment(
                    id = o.getString("id"),
                    itemId = o.getString("itemId"),
                    displayName = o.optString("displayName", "附件"),
                    uri = if (o.isNull("uri")) null else o.getString("uri"),
                    kind = o.optString("kind", "image"),
                    sizeBytes = o.optLong("sizeBytes", 0),
                    addedAt = o.optLong("addedAt", System.currentTimeMillis())
                )
            }.toMutableList()
        }.getOrElse { mutableListOf() }
    }

    fun saveAttachments(list: List<Attachment>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("itemId", it.itemId); put("displayName", it.displayName)
                put("uri", it.uri ?: JSONObject.NULL); put("kind", it.kind)
                put("sizeBytes", it.sizeBytes); put("addedAt", it.addedAt)
            })
        }
        runCatching { attsFile.writeText(arr.toString()) }
    }

    // ---------- 关系 ----------

    fun loadRelations(): MutableList<Relation> {
        if (!relsFile.exists()) {
            val seed = seedRelations()
            saveRelations(seed)
            return seed.toMutableList()
        }
        return runCatching {
            val arr = JSONArray(relsFile.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                Relation(
                    id = o.getString("id"),
                    fromItemId = o.getString("fromItemId"),
                    toItemId = o.getString("toItemId"),
                    type = o.optString("type", "配套使用"),
                    note = o.optString("note", ""),
                    createdAt = o.optLong("createdAt", System.currentTimeMillis())
                )
            }.toMutableList()
        }.getOrElse { mutableListOf() }
    }

    fun saveRelations(list: List<Relation>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("fromItemId", it.fromItemId); put("toItemId", it.toItemId)
                put("type", it.type); put("note", it.note); put("createdAt", it.createdAt)
            })
        }
        runCatching { relsFile.writeText(arr.toString()) }
    }

    // ---------- 事件 ----------

    fun loadEvents(): MutableList<ItemEvent> {
        if (!evsFile.exists()) {
            val seed = seedEvents()
            saveEvents(seed)
            return seed.toMutableList()
        }
        return runCatching {
            val arr = JSONArray(evsFile.readText())
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                ItemEvent(
                    id = o.getString("id"),
                    itemId = o.getString("itemId"),
                    type = o.optString("type", "盘点"),
                    at = o.optLong("at", System.currentTimeMillis()),
                    endAt = if (o.isNull("endAt")) null else o.getLong("endAt"),
                    note = o.optString("note", "")
                )
            }.toMutableList()
        }.getOrElse { mutableListOf() }
    }

    fun saveEvents(list: List<ItemEvent>) {
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().apply {
                put("id", it.id); put("itemId", it.itemId); put("type", it.type)
                put("at", it.at); put("endAt", it.endAt ?: JSONObject.NULL); put("note", it.note)
            })
        }
        runCatching { evsFile.writeText(arr.toString()) }
    }

    // ---------- 种子 ----------

    private fun seedItems(positions: List<Position>): List<Item> {
        val now = System.currentTimeMillis()
        val hour = 3600_000L
        val day = 24 * hour
        fun posOf(name: String) = positions.firstOrNull { it.name == name }

        val out = mutableListOf<Item>()
        posOf("第二层")?.let { p ->
            out += Item(positionId = p.id, name = "螺丝刀套装", category = "工具", quantity = 1,
                quantityUnit = "套", note = "十字和一字各一组，常用的是 PH2。放在抽屉第二层靠里一侧。",
                status = "在位", createdAt = now - 250 * day, updatedAt = now - 3 * day)
            out += Item(positionId = p.id, name = "电工胶带", category = "配件", quantity = 1,
                note = "黑色", createdAt = now - 60 * day, updatedAt = now - 7 * day)
            out += Item(positionId = p.id, name = "插线板", category = "配件", quantity = 1,
                note = "5 位 3 米线", createdAt = now - 90 * day, updatedAt = now - 14 * day)
            out += Item(positionId = p.id, name = "记号笔", category = "工具", quantity = 2,
                quantityUnit = "支", createdAt = now - 120 * day, updatedAt = now - 30 * day)
            out += Item(positionId = p.id, name = "备用钥匙", category = "其他", quantity = 1,
                quantityUnit = "把", note = "大门和储物间各一把", createdAt = now - 200 * day,
                updatedAt = now - 90 * day)
        }
        posOf("第一层")?.let { p ->
            out += Item(positionId = p.id, name = "电池", category = "配件", quantity = 8,
                quantityUnit = "节", note = "5 号 / 7 号各 4 节", createdAt = now - 100 * day,
                updatedAt = now - 5 * day)
            out += Item(positionId = p.id, name = "数据线", category = "配件", quantity = 3,
                quantityUnit = "根", createdAt = now - 80 * day, updatedAt = now - 10 * day)
        }
        posOf("左边")?.let { p ->
            out += Item(positionId = p.id, name = "显示器支架", category = "配件", quantity = 1,
                createdAt = now - 60 * day, updatedAt = now - 20 * day)
        }
        return out
    }

    private fun seedAttachments(): List<Attachment> = emptyList()

    private fun seedRelations(): List<Relation> = emptyList()

    private fun seedEvents(): List<ItemEvent> = emptyList()
}
