package online.lfcloud.order

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.Position
import online.lfcloud.order.model.Workspace
import online.lfcloud.order.ui.DeleteModuleDialog
import online.lfcloud.order.ui.DeletePositionDialog
import online.lfcloud.order.ui.DeleteWorkspaceDialog
import online.lfcloud.order.ui.ModuleEditScreen
import online.lfcloud.order.ui.ModuleListScreen
import online.lfcloud.order.ui.ModuleSortFilterSheet
import online.lfcloud.order.ui.PositionEditScreen
import online.lfcloud.order.ui.PositionListScreen
import online.lfcloud.order.ui.SortFilterSheet
import online.lfcloud.order.ui.WorkspaceEditScreen
import online.lfcloud.order.ui.WorkspaceListScreen
import online.lfcloud.order.ui.theme.OrderTheme

private sealed interface Screen {
    data object Workspaces : Screen
    data object Modules : Screen
    data object Positions : Screen
    data object EditWorkspace : Screen
    data object EditModule : Screen
    data object EditPosition : Screen
}

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
private fun OrderApp(vm: OrderViewModel = viewModel()) {
    var screen by remember { mutableStateOf<Screen>(Screen.Workspaces) }
    var wsSheet by remember { mutableStateOf(false) }
    var mdSheet by remember { mutableStateOf(false) }
    var wsToDelete by remember { mutableStateOf<Workspace?>(null) }
    var mdToDelete by remember { mutableStateOf<Module?>(null) }
    var posToDelete by remember { mutableStateOf<Position?>(null) }

    Box(Modifier.fillMaxSize().background(Color.White).systemBarsPadding()) {
        when (screen) {
            Screen.Workspaces -> WorkspaceListScreen(
                vm = vm,
                onOpenModules = { ws -> vm.openWorkspace(ws.id); screen = Screen.Modules },
                onEdit = { id -> vm.startEditWorkspace(id); screen = Screen.EditWorkspace },
                onCreate = { vm.startCreateWorkspace(); screen = Screen.EditWorkspace },
                onOpenSortFilter = { wsSheet = true },
                onDeleteRequest = { wsToDelete = it }
            )

            Screen.Modules -> ModuleListScreen(
                vm = vm,
                onBack = { screen = Screen.Workspaces },
                onCreate = {
                    vm.openedWorkspaceId?.let {
                        vm.startCreateModule(it); screen = Screen.EditModule
                    }
                },
                onOpenPositions = { m -> vm.openModule(m.id); screen = Screen.Positions },
                onEdit = { id -> vm.startEditModule(id); screen = Screen.EditModule },
                onOpenSortFilter = { mdSheet = true },
                onDeleteRequest = { mdToDelete = it }
            )

            Screen.Positions -> PositionListScreen(
                vm = vm,
                onBack = { screen = Screen.Modules },
                onCreate = {
                    vm.openedModuleId?.let {
                        vm.startCreatePosition(it); screen = Screen.EditPosition
                    }
                },
                onEdit = { id -> vm.startEditPosition(id); screen = Screen.EditPosition },
                onDeleteRequest = { posToDelete = it }
            )

            Screen.EditWorkspace -> WorkspaceEditScreen(
                vm = vm,
                onBack = { screen = Screen.Workspaces },
                onSaved = { screen = Screen.Workspaces }
            )

            Screen.EditModule -> ModuleEditScreen(
                vm = vm,
                onBack = { screen = Screen.Modules },
                onSaved = { screen = Screen.Modules }
            )

            Screen.EditPosition -> PositionEditScreen(
                vm = vm,
                onBack = { screen = Screen.Positions },
                onSaved = { screen = Screen.Positions }
            )
        }

        if (wsSheet) {
            SortFilterSheet(
                currentSort = vm.sort,
                currentFilter = vm.filter,
                onApply = { s, f -> vm.sort = s; vm.filter = f; wsSheet = false },
                onDismiss = { wsSheet = false }
            )
        }

        if (mdSheet) {
            ModuleSortFilterSheet(
                currentSort = vm.moduleSort,
                currentFilter = vm.moduleFilter,
                onApply = { s, f -> vm.moduleSort = s; vm.moduleFilter = f; mdSheet = false },
                onDismiss = { mdSheet = false }
            )
        }

        wsToDelete?.let { target ->
            DeleteWorkspaceDialog(
                target = target,
                onConfirm = { vm.deleteWorkspace(target.id); wsToDelete = null },
                onDismiss = { wsToDelete = null }
            )
        }

        mdToDelete?.let { target ->
            DeleteModuleDialog(
                target = target,
                onConfirm = { vm.deleteModule(target.id); mdToDelete = null },
                onDismiss = { mdToDelete = null }
            )
        }

        posToDelete?.let { target ->
            DeletePositionDialog(
                target = target,
                onConfirm = { vm.deletePosition(target.id); posToDelete = null },
                onDismiss = { posToDelete = null }
            )
        }
    }
}
