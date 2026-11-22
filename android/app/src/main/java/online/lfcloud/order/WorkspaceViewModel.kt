package online.lfcloud.order

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import online.lfcloud.order.data.WorkspaceRepository
import online.lfcloud.order.model.FilterState
import online.lfcloud.order.model.SortOption
import online.lfcloud.order.model.Workspace

class WorkspaceViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WorkspaceRepository(app)

    var workspaces by mutableStateOf<List<Workspace>>(repo.load())
        private set

    var query by mutableStateOf("")
    var sort by mutableStateOf(SortOption.RECENT)
    var filter by mutableStateOf(FilterState())

    var editingId by mutableStateOf<String?>(null)
        private set
    var formName by mutableStateOf("")
    var formIcon by mutableStateOf<String?>(null)
    var formDesc by mutableStateOf("")
    var nameError by mutableStateOf<String?>(null)

    fun visible(): List<Workspace> {
        val q = query.trim()
        return workspaces
            .asSequence()
            .filter { q.isEmpty() || it.name.contains(q, true) || it.description.contains(q, true) }
            .filter { filter.matches(it) }
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

    fun startCreate() {
        editingId = null
        formName = ""
        formIcon = null
        formDesc = ""
        nameError = null
    }

    fun startEdit(id: String) {
        val w = workspaces.firstOrNull { it.id == id } ?: return
        editingId = id
        formName = w.name
        formIcon = w.iconId
        formDesc = w.description
        nameError = null
    }

    fun save(): Boolean {
        val name = formName.trim()
        if (name.isEmpty()) {
            nameError = "请输入名称"
            return false
        }
        if (name.length > 40) {
            nameError = "名称最多 40 个字"
            return false
        }
        val dup = workspaces.any { it.name == name && it.id != editingId }
        if (dup) {
            nameError = "已有同名工作区"
            return false
        }
        nameError = null
        val now = System.currentTimeMillis()
        val id = editingId
        workspaces = if (id == null) {
            workspaces + Workspace(
                name = name,
                iconId = formIcon,
                description = formDesc.trim(),
                itemCount = 0,
                updatedAt = now,
                createdAt = now,
                isCurrent = workspaces.isEmpty()
            )
        } else {
            workspaces.map {
                if (it.id == id) it.copy(
                    name = name,
                    iconId = formIcon,
                    description = formDesc.trim(),
                    updatedAt = now
                ) else it
            }
        }
        persist()
        return true
    }

    fun delete(id: String) {
        workspaces = workspaces.filterNot { it.id == id }
        persist()
    }

    fun setCurrent(id: String) {
        workspaces = workspaces.map {
            if (it.id == id) it.copy(isCurrent = true, updatedAt = System.currentTimeMillis())
            else it.copy(isCurrent = false)
        }
        persist()
    }

    fun resetFilter() {
        filter = FilterState()
    }

    private fun persist() = repo.save(workspaces)
}
