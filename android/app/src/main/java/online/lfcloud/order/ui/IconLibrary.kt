package online.lfcloud.order.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import online.lfcloud.order.ui.theme.Ink2

/** 一个预设图标：id 会写进数据，所以定下来之后不要改。 */
data class OrderIcon(
    val id: String,
    val label: String,
    val group: String,
    val path: String
)

/**
 * 预设图标库：24 个线性图标，四组。
 * 路径与设计稿一致（24×24 viewBox，1.7 描边）。
 */
object IconLibrary {

    val groups = listOf("住所", "办公", "收纳", "其他")

    private val all: List<OrderIcon> = listOf(
        // 住所
        OrderIcon("home", "房子", "住所", "M3 10.5L12 3l9 7.5V20a1 1 0 01-1 1h-5v-6.5H9V21H4a1 1 0 01-1-1z"),
        OrderIcon("apartment", "公寓", "住所", "M4 21V8.5h16V21M3 21h18M8 12h2.5M8 16h2.5M13.5 12H16M13.5 16H16"),
        OrderIcon("building", "写字楼", "住所", "M6 21V5h7v16M13 10h5v11M3 21h18M8.5 8.5h2M8.5 12.5h2M8.5 16.5h2"),
        OrderIcon("house", "独栋", "住所", "M4 11.8L12 5l8 6.8M5.6 11v9h12.8v-9"),
        OrderIcon("tree", "院子", "住所", "M12 20.5v-4M12 3.5l5 7.2h-3.1l4.1 5.4H6l4.1-5.4H7z"),
        OrderIcon("window", "房间", "住所", "M4 5.6h16v12.8H4zM12 5.6v12.8M4 12h16"),
        // 办公
        OrderIcon("briefcase", "公文包", "办公", "M3.5 8.6h17V19a1 1 0 01-1 1h-15a1 1 0 01-1-1zM9 8.6V6.4A1.4 1.4 0 0110.4 5h3.2A1.4 1.4 0 0115 6.4v2.2M3.5 13h17"),
        OrderIcon("shop", "店铺", "办公", "M4 9.6h16V20H4zM4 9.6L5.6 4.6h12.8L20 9.6M8.6 13.5h4.2V20H8.6z"),
        OrderIcon("factory", "工厂", "办公", "M3 20V10.6l5 3v-3l5 3V9.6h5V20zM3 20h18"),
        OrderIcon("monitor", "工位", "办公", "M3.6 5.8h16.8v10.4H3.6zM9 19.6h6M12 16.2v3.4"),
        OrderIcon("key", "钥匙", "办公", "M15.2 5.2a4.1 4.1 0 100 8.2 4.1 4.1 0 000-8.2zM12.3 12.8L4 21.2M7.3 17.9l2.1 2.1"),
        OrderIcon("pin", "地点", "办公", "M12 21s7-6.4 7-11a7 7 0 10-14 0c0 4.6 7 11 7 11zM12 8a2 2 0 100 4 2 2 0 000-4z"),
        // 收纳
        OrderIcon("box", "收纳盒", "收纳", "M3.5 8.2L12 3.9l8.5 4.3v7.9L12 20.4l-8.5-4.3zM3.5 8.2L12 12.4l8.5-4.2M12 12.4v8"),
        OrderIcon("chest", "箱子", "收纳", "M3.5 9.6h17V20h-17zM3.5 9.6L5.6 5h12.8l2.1 4.6M10 13.2h4"),
        OrderIcon("shelf", "货架", "收纳", "M3 5.6h18M3 12h18M3 18.4h18M7.2 5.6v12.8M16.8 5.6v12.8"),
        OrderIcon("drawer", "抽屉", "收纳", "M3 6.6h18v11H3zM3 12.1h18M9.5 9.3h5M9.5 15.2h5"),
        OrderIcon("warehouse", "仓库", "收纳", "M3 20V9.6L12 4.2l9 5.4V20M9.2 20v-6.4h5.6V20"),
        OrderIcon("garage", "车库", "收纳", "M4 20V10.6a8 8 0 0116 0V20M4 14.6h16"),
        // 其他
        OrderIcon("car", "车", "其他", "M5 16.4h14M6.2 16.4l1.3-4.6A2 2 0 019.4 10.4h5.2a2 2 0 011.9 1.4l1.3 4.6M8.4 16.4V19H5.6v-2.6M15.6 16.4V19h2.8v-2.6M8 13.6h8"),
        OrderIcon("bike", "自行车", "其他", "M6.4 16.6a2.9 2.9 0 100-5.8 2.9 2.9 0 000 5.8zM17.6 16.6a2.9 2.9 0 100-5.8 2.9 2.9 0 000 5.8zM6.4 13.7l3.6-5h4.4l3.2 5M10 8.7h4.6"),
        OrderIcon("clock", "时钟", "其他", "M12 3.8a8.2 8.2 0 100 16.4 8.2 8.2 0 000-16.4zM12 8v4.4l3.2 1.9"),
        OrderIcon("star", "星标", "其他", "M12 3.6l2.6 5.3 5.9.85-4.3 4.2 1.05 5.9L12 17l-5.25 2.85L7.8 13.95 3.5 9.75l5.9-.85z"),
        OrderIcon("lock", "锁", "其他", "M6.4 11h11.2v9H6.4zM8.8 11V8.4a3.2 3.2 0 016.4 0V11"),
        OrderIcon("layers", "图层", "其他", "M12 3.6L3.5 8l8.5 4.4L20.5 8zM3.5 12.4L12 16.8l8.5-4.4M3.5 16.4L12 20.8l8.5-4.4")
    )

    fun all(): List<OrderIcon> = all

    fun byGroup(group: String): List<OrderIcon> = all.filter { it.group == group }

    fun find(id: String?): OrderIcon? = all.firstOrNull { it.id == id }

    private val cache = HashMap<String, ImageVector>()

    fun vector(id: String): ImageVector = cache.getOrPut(id) {
        val entry = all.first { it.id == id }
        val nodes = PathParser().parsePathString(entry.path).toNodes()
        ImageVector.Builder(
            name = entry.id,
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f
        ).apply {
            addPath(
                pathData = nodes,
                stroke = SolidColor(Ink2),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }.build()
    }
}
