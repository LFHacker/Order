package online.lfcloud.order

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import online.lfcloud.order.data.ModuleRepository
import online.lfcloud.order.data.PositionRepository
import online.lfcloud.order.data.WorkspaceRepository
import online.lfcloud.order.model.FilterState
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.ModuleSort
import online.lfcloud.order.model.Position
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
        workspaces = workspaces.filterNot { it.id == id }
        modules = modules.filterNot { it.workspaceId == id }
        positions = positions.filterNot { it.moduleId in moduleIds }
        wsRepo.save(workspaces)
        mdRepo.save(modules)
        posRepo.save(positions)
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
        modules = modules.filterNot { it.id == id }
        positions = positions.filterNot { it.moduleId == id }
        mdRepo.save(modules)
        posRepo.save(positions)
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
        positions = positions.filterNot { it.id == id }
        posRepo.save(positions)
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
}
