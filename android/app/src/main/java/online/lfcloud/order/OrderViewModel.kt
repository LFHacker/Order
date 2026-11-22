package online.lfcloud.order

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import online.lfcloud.order.data.Account
import online.lfcloud.order.data.AccountRepository
import online.lfcloud.order.data.ItemRepository
import online.lfcloud.order.data.ModuleRepository
import online.lfcloud.order.data.PositionRepository
import online.lfcloud.order.data.WorkspaceRepository
import online.lfcloud.order.model.Attachment
import online.lfcloud.order.model.FilterState
import online.lfcloud.order.model.Item
import online.lfcloud.order.model.ItemEvent
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.ModuleSort
import online.lfcloud.order.model.Position
import online.lfcloud.order.model.Relation
import online.lfcloud.order.model.SortOption
import online.lfcloud.order.model.Workspace

class OrderViewModel(app: Application) : AndroidViewModel(app) {

    private val wsRepo = WorkspaceRepository(app)

    var workspaces by mutableStateOf<List<Workspace>>(wsRepo.load())
        private set

    private val mdRepo = ModuleRepository(app)

    var modules by mutableStateOf<List<Module>>(mdRepo.load(workspaces))
        private set

    private val posRepo = PositionRepository(app)

    var positions by mutableStateOf<List<Position>>(posRepo.load(modules))
        private set

    // ---------- 工作区列表状态 ----------
    var query by mutableStateOf("")
    var sort by mutableStateOf(SortOption.RECENT)
    var filter by mutableStateOf(FilterState())

    // ---------- 工作区表单 ----------
    var editingWsId by mutableStateOf<String?>(null)
        private set
    var wsName by mutableStateOf("")
    var wsIcon by mutableStateOf<String?>(null)
    var wsDesc by mutableStateOf("")
    var wsError by mutableStateOf<String?>(null)

    // ---------- 模块列表状态 ----------
    var moduleQuery by mutableStateOf("")
    var moduleSort by mutableStateOf(ModuleSort.RECENT)
    var moduleFilter by mutableStateOf(FilterState())
    var openedWorkspaceId by mutableStateOf<String?>(null)
        private set

    // ---------- 模块表单 ----------
    var editingModuleId by mutableStateOf<String?>(null)
        private set
    var moduleWorkspaceId by mutableStateOf<String?>(null)
        private set
    var mdName by mutableStateOf("")
    var mdIcon by mutableStateOf<String?>(null)
    var mdDesc by mutableStateOf("")
    var mdError by mutableStateOf<String?>(null)

    // ---------- 方位 ----------
    var openedModuleId by mutableStateOf<String?>(null)
        private set
    var editingPositionId by mutableStateOf<String?>(null)
        private set
    var positionModuleId by mutableStateOf<String?>(null)
        private set
    var posName by mutableStateOf("")
    var posError by mutableStateOf<String?>(null)

    // ---------- 物品 ----------
    private val itemRepo = ItemRepository(app)

    var items by mutableStateOf<List<Item>>(itemRepo.loadItems(positions))
        private set
    var attachments by mutableStateOf<List<Attachment>>(itemRepo.loadAttachments())
        private set
    var relations by mutableStateOf<List<Relation>>(itemRepo.loadRelations())
        private set
    var events by mutableStateOf<List<ItemEvent>>(itemRepo.loadEvents())
        private set


    init {
        // 旧数据迁移：还没主图、但有图片附件的物品，取最新一张图片作为主图
        val migrated = items.map { item ->
            if (item.imageUri == null) {
                val newest = attachments
                    .filter { it.itemId == item.id && it.kind == "image" && it.uri != null }
                    .maxByOrNull { it.addedAt }
                if (newest?.uri != null) item.copy(imageUri = newest.uri) else item
            } else {
                item
            }
        }
        if (migrated != items) {
            items = migrated
            itemRepo.saveItems(items)
        }
    }
    var openedPositionId by mutableStateOf<String?>(null)
        private set
    var openedItemId by mutableStateOf<String?>(null)
        private set
    var editingItemId by mutableStateOf<String?>(null)
        private set
    var itemPositionId by mutableStateOf<String?>(null)
        private set
    var itName by mutableStateOf("")
    var itCategory by mutableStateOf("其他")
    var itQuantity by mutableStateOf("1")
    var itUnit by mutableStateOf("件")
    var itNote by mutableStateOf("")
    var itError by mutableStateOf<String?>(null)

    // ---------- 账号与设置 ----------
    private val accountRepo = AccountRepository(app)

    var account by mutableStateOf(accountRepo.load())
        private set
    var themeMode by mutableStateOf(accountRepo.loadTheme())
        private set

    fun itemTotal(): Int = items.size

    fun writeHolderText(): String = "本机可写"

    fun versionName(): String = runCatching {
        val ctx = getApplication<Application>()
        ctx.packageManager.getPackageInfo(ctx.packageName, 0).versionName ?: ""
    }.getOrDefault("")
    fun cacheSizeBytes(): Long = accountRepo.cacheSize(getApplication())

    fun clearCache() = accountRepo.clearCache(getApplication())

    fun setTheme(value: String) {
        themeMode = value
        accountRepo.saveTheme(value)
    }

    fun updateAccountField(field: String, value: String): Boolean {
        val v = value.trim()
        if (v.isEmpty()) return false
        account = when (field) {
            Account.FIELD_NAME -> account.copy(username = v)
            Account.FIELD_PHONE -> account.copy(phone = v)
            Account.FIELD_EMAIL -> account.copy(email = v)
            else -> account
        }
        accountRepo.save(account)
        return true
    }

    fun updatePassword(current: String, next: String): Boolean {
        if (next.length < 8) return false
        account = account.copy(password = next)
        accountRepo.save(account)
        return true
    }

    // ---------- 工作区 ----------

    fun visibleWorkspaces(): List<Workspace> {
        val q = query.trim()
        return workspaces
            .asSequence()
            .filter { q.isEmpty() || it.name.contains(q, true) || it.description.contains(q, true) }
            .filter { filter.matches(it.itemCount, it.updatedAt) }
            .sortedWith(
                when (sort) {
                    SortOption.RECENT -> compareByDescending { it.updatedAt }
                    SortOption.NAME -> compareBy { it.name }
                    SortOption.ITEMS -> compareByDescending { it.itemCount }
                    SortOption.CREATED -> compareByDescending { it.createdAt }
                }
            )
            .toList()
    }

    fun totalItems(): Int = workspaces.sumOf { it.itemCount }

    fun openWorkspace(id: String) {
        openedWorkspaceId = id
        moduleQuery = ""
        moduleFilter = FilterState()
        moduleSort = ModuleSort.RECENT
    }

    fun currentWorkspace(): Workspace? = workspaces.firstOrNull { it.id == openedWorkspaceId }

    fun startCreateWorkspace() {
        editingWsId = null
        wsName = ""
        wsIcon = null
        wsDesc = ""
        wsError = null
    }

    fun startEditWorkspace(id: String) {
        val w = workspaces.firstOrNull { it.id == id } ?: return
        editingWsId = id
        wsName = w.name
        wsIcon = w.iconId
        wsDesc = w.description
        wsError = null
    }

    fun saveWorkspace(): Boolean {
        val name = wsName.trim()
        if (name.isEmpty()) { wsError = "请输入名称"; return false }
        if (name.length > 40) { wsError = "名称最多 40 个字"; return false }
        if (workspaces.any { it.name == name && it.id != editingWsId }) {
            wsError = "已有同名工作区"; return false
        }
        wsError = null
        val now = System.currentTimeMillis()
        val id = editingWsId
        workspaces = if (id == null) {
            workspaces + Workspace(
                name = name, iconId = wsIcon, description = wsDesc.trim(),
                itemCount = 0, updatedAt = now, createdAt = now,
                isCurrent = workspaces.isEmpty()
            )
        } else {
            workspaces.map {
                if (it.id == id) it.copy(name = name, iconId = wsIcon,
                    description = wsDesc.trim(), updatedAt = now) else it
            }
        }
        wsRepo.save(workspaces)
        return true
    }

    fun deleteWorkspace(id: String) {
        val moduleIds = modules.filter { it.workspaceId == id }.map { it.id }.toSet()
        val posIds = positions.filter { it.moduleId in moduleIds }.map { it.id }.toSet()
        val itemIds = items.filter { it.positionId in posIds }.map { it.id }.toSet()
        workspaces = workspaces.filterNot { it.id == id }
        modules = modules.filterNot { it.workspaceId == id }
        positions = positions.filterNot { it.moduleId in moduleIds }
        items = items.filterNot { it.positionId in posIds }
        attachments = attachments.filterNot { it.itemId in itemIds }
        relations = relations.filterNot { it.fromItemId in itemIds || it.toItemId in itemIds }
        events = events.filterNot { it.itemId in itemIds }
        wsRepo.save(workspaces)
        mdRepo.save(modules)
        posRepo.save(positions)
        itemRepo.saveItems(items)
        itemRepo.saveAttachments(attachments)
        itemRepo.saveRelations(relations)
        itemRepo.saveEvents(events)
    }

    fun setCurrentWorkspace(id: String) {
        workspaces = workspaces.map {
            if (it.id == id) it.copy(isCurrent = true, updatedAt = System.currentTimeMillis())
            else it.copy(isCurrent = false)
        }
        wsRepo.save(workspaces)
    }

    // ---------- 模块 ----------

    fun modulesOf(workspaceId: String?): List<Module> =
        if (workspaceId == null) emptyList() else modules.filter { it.workspaceId == workspaceId }

    fun visibleModules(workspaceId: String?): List<Module> {
        val q = moduleQuery.trim()
        return modulesOf(workspaceId)
            .asSequence()
            .filter { q.isEmpty() || it.name.contains(q, true) || it.description.contains(q, true) }
            .filter { moduleFilter.matches(it.itemCount, it.updatedAt) }
            .sortedWith(
                when (moduleSort) {
                    ModuleSort.RECENT -> compareByDescending { it.updatedAt }
                    ModuleSort.NAME -> compareBy { it.name }
                    ModuleSort.ITEMS -> compareByDescending { it.itemCount }
                    ModuleSort.POSITIONS -> compareByDescending { it.positionCount }
                }
            )
            .toList()
    }

    fun moduleCount(workspaceId: String?): Int = modulesOf(workspaceId).size

    fun itemsInWorkspace(workspaceId: String?): Int = modulesOf(workspaceId).sumOf { it.itemCount }

    fun startCreateModule(workspaceId: String) {
        editingModuleId = null
        moduleWorkspaceId = workspaceId
        mdName = ""
        mdIcon = null
        mdDesc = ""
        mdError = null
    }

    fun startEditModule(id: String) {
        val m = modules.firstOrNull { it.id == id } ?: return
        editingModuleId = id
        moduleWorkspaceId = m.workspaceId
        mdName = m.name
        mdIcon = m.iconId
        mdDesc = m.description
        mdError = null
    }

    fun saveModule(): Boolean {
        val name = mdName.trim()
        val wsId = moduleWorkspaceId ?: return false
        if (name.isEmpty()) { mdError = "请输入名称"; return false }
        if (name.length > 40) { mdError = "名称最多 40 个字"; return false }
        if (modules.any { it.workspaceId == wsId && it.name == name && it.id != editingModuleId }) {
            mdError = "该工作区已有同名模块"; return false
        }
        mdError = null
        val now = System.currentTimeMillis()
        val id = editingModuleId
        modules = if (id == null) {
            modules + Module(
                workspaceId = wsId, name = name, iconId = mdIcon,
                description = mdDesc.trim(), positionCount = 0, itemCount = 0,
                updatedAt = now, createdAt = now
            )
        } else {
            modules.map {
                if (it.id == id) it.copy(name = name, iconId = mdIcon,
                    description = mdDesc.trim(), updatedAt = now) else it
            }
        }
        mdRepo.save(modules)
        return true
    }

    fun deleteModule(id: String) {
        val posIds = positions.filter { it.moduleId == id }.map { it.id }.toSet()
        val itemIds = items.filter { it.positionId in posIds }.map { it.id }.toSet()
        modules = modules.filterNot { it.id == id }
        positions = positions.filterNot { it.moduleId == id }
        items = items.filterNot { it.positionId in posIds }
        attachments = attachments.filterNot { it.itemId in itemIds }
        relations = relations.filterNot { it.fromItemId in itemIds || it.toItemId in itemIds }
        events = events.filterNot { it.itemId in itemIds }
        mdRepo.save(modules)
        posRepo.save(positions)
        itemRepo.saveItems(items)
        itemRepo.saveAttachments(attachments)
        itemRepo.saveRelations(relations)
        itemRepo.saveEvents(events)
    }

    // ---------- 方位 ----------

    fun openModule(id: String) {
        openedModuleId = id
    }

    fun currentModule(): Module? = modules.firstOrNull { it.id == openedModuleId }

    fun positionsOf(moduleId: String?): List<Position> =
        if (moduleId == null) emptyList()
        else positions.filter { it.moduleId == moduleId }.sortedBy { it.sortOrder }

    fun positionCountOf(moduleId: String?): Int = positionsOf(moduleId).size

    fun itemsInModule(moduleId: String?): Int = positionsOf(moduleId).sumOf { it.itemCount }

    fun startCreatePosition(moduleId: String) {
        editingPositionId = null
        positionModuleId = moduleId
        posName = ""
        posError = null
    }

    fun startEditPosition(id: String) {
        val p = positions.firstOrNull { it.id == id } ?: return
        editingPositionId = id
        positionModuleId = p.moduleId
        posName = p.name
        posError = null
    }

    fun savePosition(): Boolean {
        val name = posName.trim()
        val moduleId = positionModuleId ?: return false
        if (name.isEmpty()) { posError = "请输入名称"; return false }
        if (name.length > 40) { posError = "名称最多 40 个字"; return false }
        if (positions.any { it.moduleId == moduleId && it.name == name && it.id != editingPositionId }) {
            posError = "该模块已有同名方位"; return false
        }
        posError = null
        val now = System.currentTimeMillis()
        val id = editingPositionId
        positions = if (id == null) {
            val nextOrder = positionsOf(moduleId).size
            positions + Position(
                moduleId = moduleId, name = name, sortOrder = nextOrder,
                itemCount = 0, updatedAt = now, createdAt = now
            )
        } else {
            positions.map {
                if (it.id == id) it.copy(name = name, updatedAt = now) else it
            }
        }
        posRepo.save(positions)
        return true
    }

    fun deletePosition(id: String) {
        val itemIds = items.filter { it.positionId == id }.map { it.id }.toSet()
        positions = positions.filterNot { it.id == id }
        items = items.filterNot { it.positionId == id }
        attachments = attachments.filterNot { it.itemId in itemIds }
        relations = relations.filterNot { it.fromItemId in itemIds || it.toItemId in itemIds }
        events = events.filterNot { it.itemId in itemIds }
        posRepo.save(positions)
        itemRepo.saveItems(items)
        itemRepo.saveAttachments(attachments)
        itemRepo.saveRelations(relations)
        itemRepo.saveEvents(events)
    }

    /** 拖动排序：把 from 位置的方位移动到 to 位置，然后重排 sortOrder。 */
    fun reorderPositions(moduleId: String, from: Int, to: Int) {
        val list = positionsOf(moduleId).toMutableList()
        if (from !in list.indices || to !in list.indices || from == to) return
        val moved = list.removeAt(from)
        list.add(to, moved)
        val newOrder = list.mapIndexed { index, p -> p.copy(sortOrder = index) }
        positions = positions.map { p ->
            newOrder.firstOrNull { it.id == p.id } ?: p
        }
        posRepo.save(positions)
    }

    // ---------- 物品 ----------

    fun openPosition(id: String) {
        openedPositionId = id
    }

    fun currentPosition(): Position? = positions.firstOrNull { it.id == openedPositionId }

    fun itemsOf(positionId: String?): List<Item> =
        if (positionId == null) emptyList()
        else items.filter { it.positionId == positionId }.sortedByDescending { it.updatedAt }

    fun itemCountInPosition(positionId: String?): Int = itemsOf(positionId).size

    fun openItem(id: String) {
        openedItemId = id
    }

    fun currentItem(): Item? = items.firstOrNull { it.id == openedItemId }

    fun itemById(id: String?): Item? = items.firstOrNull { it.id == id }

    /** 物品的完整路径：工作区 / 模块 / 方位。 */
    fun pathOf(positionId: String?): List<String> {
        val position = positions.firstOrNull { it.id == positionId } ?: return emptyList()
        val module = modules.firstOrNull { it.id == position.moduleId }
        val workspace = workspaces.firstOrNull { it.id == module?.workspaceId }
        return listOfNotNull(workspace?.name, module?.name, position.name)
    }

    fun startCreateItem(positionId: String) {
        editingItemId = null
        itemPositionId = positionId
        itName = ""
        itCategory = "其他"
        itQuantity = "1"
        itUnit = "件"
        itNote = ""
        itError = null
    }

    fun startEditItem(id: String) {
        val item = items.firstOrNull { it.id == id } ?: return
        editingItemId = id
        itemPositionId = item.positionId
        itName = item.name
        itCategory = item.category
        itQuantity = item.quantity.toString()
        itUnit = item.quantityUnit
        itNote = item.note
        itError = null
    }

    fun saveItem(): Boolean {
        val name = itName.trim()
        val posId = itemPositionId ?: return false
        if (name.isEmpty()) { itError = "请输入名称"; return false }
        if (name.length > 40) { itError = "名称最多 40 个字"; return false }
        itError = null
        val now = System.currentTimeMillis()
        val qty = itQuantity.toIntOrNull()?.coerceAtLeast(1) ?: 1
        val id = editingItemId
        items = if (id == null) {
            items + Item(
                positionId = posId, name = name, category = itCategory, quantity = qty,
                quantityUnit = itUnit.ifBlank { "件" }, note = itNote.trim(),
                createdAt = now, updatedAt = now
            )
        } else {
            items.map {
                if (it.id == id) it.copy(
                    name = name, category = itCategory, quantity = qty,
                    quantityUnit = itUnit.ifBlank { "件" }, note = itNote.trim(), updatedAt = now
                ) else it
            }
        }
        itemRepo.saveItems(items)
        return true
    }

    fun deleteItem(id: String) {
        items = items.filterNot { it.id == id }
        attachments = attachments.filterNot { it.itemId == id }
        events = events.filterNot { it.itemId == id }
        relations = relations.filterNot { it.fromItemId == id || it.toItemId == id }
        itemRepo.saveItems(items)
        itemRepo.saveAttachments(attachments)
        itemRepo.saveEvents(events)
        itemRepo.saveRelations(relations)
    }

    fun setItemCover(itemId: String, uri: String) {
        items = items.map {
            if (it.id == itemId) it.copy(imageUri = uri, updatedAt = System.currentTimeMillis()) else it
        }
        itemRepo.saveItems(items)
    }

    fun updateItemStatus(id: String, status: String) {
        items = items.map { if (it.id == id) it.copy(status = status) else it }
        itemRepo.saveItems(items)
    }

    // ---------- 附件 ----------

    fun attachmentsOf(itemId: String?): List<Attachment> =
        if (itemId == null) emptyList() else attachments.filter { it.itemId == itemId }

    fun addAttachment(itemId: String, name: String, uri: String?, kind: String) {
        attachments = attachments + Attachment(
            itemId = itemId, displayName = name, uri = uri, kind = kind,
            addedAt = System.currentTimeMillis()
        )
        itemRepo.saveAttachments(attachments)
    }

    fun deleteAttachment(id: String) {
        attachments = attachments.filterNot { it.id == id }
        itemRepo.saveAttachments(attachments)
    }

    // ---------- 关系（双向） ----------

    fun relationsOf(itemId: String?): List<Relation> =
        if (itemId == null) emptyList()
        else relations.filter { it.fromItemId == itemId || it.toItemId == itemId }
            .sortedByDescending { it.createdAt }

    /** 取关系里"对面那一件"物品。 */
    fun otherSide(rel: Relation, itemId: String): Item? =
        itemById(if (rel.fromItemId == itemId) rel.toItemId else rel.fromItemId)

    fun addRelation(itemId: String, targetId: String, type: String, note: String) {
        if (itemId == targetId) return
        val exists = relations.any {
            (it.fromItemId == itemId && it.toItemId == targetId) ||
                (it.fromItemId == targetId && it.toItemId == itemId)
        }
        if (exists) return
        relations = relations + Relation(
            fromItemId = itemId, toItemId = targetId, type = type, note = note
        )
        itemRepo.saveRelations(relations)
    }

    fun deleteRelation(id: String) {
        relations = relations.filterNot { it.id == id }
        itemRepo.saveRelations(relations)
    }

    /** 可被关联的候选物品：排除自己，排除已有关联的。 */
    fun relationCandidates(itemId: String?): List<Item> {
        if (itemId == null) return emptyList()
        val linked = relationsOf(itemId).mapNotNull { otherSide(it, itemId)?.id }.toSet()
        return items.filter { it.id != itemId && it.id !in linked }
    }

    // ---------- 事件 ----------

    fun eventsOf(itemId: String?): List<ItemEvent> =
        if (itemId == null) emptyList()
        else events.filter { it.itemId == itemId }.sortedByDescending { it.at }

    fun addEvent(itemId: String, type: String, note: String) {
        val now = System.currentTimeMillis()
        val endAt = if (type == "借出") null else null
        events = events + ItemEvent(itemId = itemId, type = type, at = now, endAt = endAt, note = note)
        itemRepo.saveEvents(events)
        when (type) {
            "借出" -> updateItemStatus(itemId, "借出中")
            "归还" -> updateItemStatus(itemId, "在位")
            "盘点" -> updateItemStatus(itemId, "在位")
        }
    }

    fun deleteEvent(id: String) {
        events = events.filterNot { it.id == id }
        itemRepo.saveEvents(events)
    }
}
