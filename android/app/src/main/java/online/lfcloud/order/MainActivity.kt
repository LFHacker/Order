package online.lfcloud.order

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import online.lfcloud.order.model.Workspace
import online.lfcloud.order.ui.DeleteWorkspaceDialog
import online.lfcloud.order.ui.SortFilterSheet
import online.lfcloud.order.ui.WorkspaceEditScreen
import online.lfcloud.order.ui.WorkspaceListScreen
import online.lfcloud.order.ui.theme.OrderTheme

private enum class Route { List, Edit }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            OrderTheme {
                Surface(Modifier.fillMaxSize().background(Color.White), color = Color.White) {
                    OrderApp()
                }
            }
        }
    }
}

@Composable
private fun OrderApp(vm: WorkspaceViewModel = viewModel()) {
    var route by remember { mutableStateOf(Route.List) }
    var showSheet by remember { mutableStateOf(false) }
    var deleteTarget by remember { mutableStateOf<Workspace?>(null) }

    androidx.compose.foundation.layout.Box(
        Modifier.fillMaxSize().background(Color.White).systemBarsPadding()
    ) {
        when (route) {
            Route.List -> WorkspaceListScreen(
                vm = vm,
                onOpenEdit = { id ->
                    if (id == null) vm.startCreate() else vm.startEdit(id)
                    route = Route.Edit
                },
                onOpenSortFilter = { showSheet = true },
                onDeleteRequest = { deleteTarget = it }
            )

            Route.Edit -> WorkspaceEditScreen(
                vm = vm,
                onBack = { route = Route.List },
                onSaved = { route = Route.List }
            )
        }

        if (showSheet) {
            SortFilterSheet(
                currentSort = vm.sort,
                currentFilter = vm.filter,
                onApply = { s, f ->
                    vm.sort = s
                    vm.filter = f
                    showSheet = false
                },
                onDismiss = { showSheet = false }
            )
        }

        deleteTarget?.let { target ->
            DeleteWorkspaceDialog(
                target = target,
                onConfirm = {
                    vm.delete(target.id)
                    deleteTarget = null
                },
                onDismiss = { deleteTarget = null }
            )
        }
    }
}
