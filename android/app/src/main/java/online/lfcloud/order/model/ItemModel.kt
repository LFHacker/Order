package online.lfcloud.order.model

import java.util.UUID

/** 物品类别：固定五项，各带一个图标。 */
object Categories {
    val all = listOf("配件", "衣物", "工具", "书籍", "其他")

    fun iconOf(category: String): String = when (category) {
        "配件" -> "box"
        "衣物" -> "chest"
        "工具" -> "monitor"
        "书籍" -> "shelf"
        else -> "layers"
    }
}

/** 事件类型：购入 / 借出 / 归还 / 维修 / 盘点。 */
object EventTypes {
    val all = listOf("购入", "借出", "归还", "维修", "盘点")
}

data class Item(
    val id: String = UUID.randomUUID().toString(),
    val positionId: String,
    val name: String,
    val category: String = "其他",
    val quantity: Int = 1,
    val quantityUnit: String = "件",
    val note: String = "",
    val imageUri: String? = null,
    val status: String = "在位",     // 在位 / 借出中 / 维修中
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class Attachment(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val displayName: String,
    val uri: String? = null,
    val kind: String = "image",      // image / file
    val sizeBytes: Long = 0,
    val addedAt: Long = System.currentTimeMillis()
)

/** 关系是双向的：查询时按 from/to 两端都算。 */
data class Relation(
    val id: String = UUID.randomUUID().toString(),
    val fromItemId: String,
    val toItemId: String,
    val type: String = "配套使用",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ItemEvent(
    val id: String = UUID.randomUUID().toString(),
    val itemId: String,
    val type: String,
    val at: Long = System.currentTimeMillis(),
    val endAt: Long? = null,
    val note: String = ""
)

/** 关系类型预设。 */
val RelationTypes = listOf("配套使用", "存放在", "替代品", "同款")
