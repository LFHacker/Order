package online.lfcloud.order.model

import java.util.UUID

enum class SortOption(val label: String) {
    RECENT("最近更新"),
    NAME("名称 A → Z"),
    ITEMS("物品数量（多 → 少）"),
    CREATED("创建时间（新 → 旧）")
}

data class FilterState(
    val days: Int = 0,
    val onlyWithItems: Boolean = false,
    val onlyEmpty: Boolean = false
) {
    val activeCount: Int
        get() = (if (days > 0) 1 else 0) +
            (if (onlyWithItems) 1 else 0) +
            (if (onlyEmpty) 1 else 0)

    fun matches(w: Workspace, now: Long = System.currentTimeMillis()): Boolean {
        if (onlyWithItems && w.itemCount <= 0) return false
        if (onlyEmpty && w.itemCount > 0) return false
        if (days > 0) {
            val limit = now - days * 24L * 60L * 60L * 1000L
            if (w.updatedAt < limit) return false
        }
        return true
    }
}

data class Workspace(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val iconId: String? = null,
    val description: String = "",
    val itemCount: Int = 0,
    val updatedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val isCurrent: Boolean = false
)

fun Long.relativeText(now: Long = System.currentTimeMillis()): String {
    val diff = now - this
    val minute = 60_000L
    val hour = 60 * minute
    val day = 24 * hour
    return when {
        diff < 2 * minute -> "刚刚"
        diff < hour -> "${diff / minute} 分钟前"
        diff < day -> "${diff / hour} 小时前"
        diff < 2 * day -> "昨天"
        diff < 30 * day -> "${diff / day} 天前"
        else -> "${diff / (30 * day)} 个月前"
    }
}
