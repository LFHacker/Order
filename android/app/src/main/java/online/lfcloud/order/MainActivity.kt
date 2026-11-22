package online.lfcloud.order

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import online.lfcloud.order.model.Item
import online.lfcloud.order.model.Module
import online.lfcloud.order.model.Position
import online.lfcloud.order.model.Workspace
import online.lfcloud.order.ui.AttachmentScreen
import online.lfcloud.order.ui.DeleteModuleDialog
import online.lfcloud.order.ui.DeletePositionDialog
import online.lfcloud.order.ui.DeleteWorkspaceDialog
import online.lfcloud.order.ui.EventScreen
import online.lfcloud.order.ui.ItemDetailScreen
import online.lfcloud.order.ui.ItemEditScreen
import online.lfcloud.order.ui.ItemListScreen
import online.lfcloud.order.ui.ModuleEditScreen
import online.lfcloud.order.ui.ModuleListScreen
import online.lfcloud.order.ui.ModuleSortFilterSheet
import online.lfcloud.order.ui.PositionEditScreen
import online.lfcloud.order.ui.PositionListScreen
import online.lfcloud.order.ui.RelationScreen
import online.lfcloud.order.ui.SortFilterSheet
import online.lfcloud.order.ui.WorkspaceEditScreen
import online.lfcloud.order.ui.WorkspaceListScreen
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.OrderTheme
import online.lfcloud.order.ui.theme.Teal

private sealed interface Screen {
    data object Workspaces : Screen
    data object Modules : Screen
    data object Positions : Screen
    data object Items : Screen
    data object ItemDetail : Screen
    data object EditWorkspace : Screen
    data object EditModule : Screen
    data object EditPosition : Screen
    data object EditItem : Screen
    data object Attachments : Screen
    data object Relations : Screen
    data object Events : Screen
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
    var itemToDelete by remember { mutableStateOf<Item?>(null) }

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
                onCreate = { vm.openedWorkspaceId?.let { vm.startCreateModule(it); screen = Screen.EditModule } },
                onOpenPositions = { m -> vm.openModule(m.id); screen = Screen.Positions },
                onEdit = { id -> vm.startEditModule(id); screen = Screen.EditModule },
                onOpenSortFilter = { mdSheet = true },
                onDeleteRequest = { mdToDelete = it }
            )

            Screen.Positions -> PositionListScreen(
                vm = vm,
                onBack = { screen = Screen.Modules },
                onCreate = { vm.openedModuleId?.let { vm.startCreatePosition(it); screen = Screen.EditPosition } },
                onOpenItems = { p -> vm.openPosition(p.id); screen = Screen.Items },
                onEdit = { id -> vm.startEditPosition(id); screen = Screen.EditPosition },
                onDeleteRequest = { posToDelete = it }
            )

            Screen.Items -> ItemListScreen(
                vm = vm,
                onBack = { screen = Screen.Positions },
                onCreate = { vm.openedPositionId?.let { vm.startCreateItem(it); screen = Screen.EditItem } },
                onOpen = { item -> vm.openItem(item.id); screen = Screen.ItemDetail },
                onEdit = { id -> vm.startEditItem(id); screen = Screen.EditItem },
                onDeleteRequest = { itemToDelete = it }
            )

            Screen.ItemDetail -> ItemDetailScreen(
                vm = vm,
                onBack = { screen = Screen.Items },
                onEdit = { vm.currentItem()?.let { vm.startEditItem(it.id); screen = Screen.EditItem } },
                onDelete = { vm.currentItem()?.let { itemToDelete = it } },
                onOpenAttachments = { screen = Screen.Attachments },
                onOpenRelations = { screen = Screen.Relations },
                onOpenEvents = { screen = Screen.Events }
            )

            Screen.Attachments -> AttachmentScreen(vm = vm, onBack = { screen = Screen.ItemDetail })

            Screen.Relations -> RelationScreen(
                vm = vm,
                onBack = { screen = Screen.ItemDetail },
                onOpenItem = { item -> vm.openItem(item.id); screen = Screen.ItemDetail }
            )

            Screen.Events -> EventScreen(vm = vm, onBack = { screen = Screen.ItemDetail })

            Screen.EditWorkspace -> WorkspaceEditScreen(
                vm = vm, onBack = { screen = Screen.Workspaces },
                onSaved = { screen = Screen.Workspaces }
            )

            Screen.EditModule -> ModuleEditScreen(
                vm = vm, onBack = { screen = Screen.Modules },
                onSaved = { screen = Screen.Modules }
            )

            Screen.EditPosition -> PositionEditScreen(
                vm = vm, onBack = { screen = Screen.Positions },
                onSaved = { screen = Screen.Positions }
            )

            Screen.EditItem -> ItemEditScreen(
                vm = vm,
                onBack = { screen = if (vm.editingItemId == null) Screen.Items else Screen.ItemDetail },
                onSaved = {
                    screen = if (vm.editingItemId == null) Screen.Items else Screen.ItemDetail
                }
            )
        }

        if (wsSheet) {
            SortFilterSheet(
                currentSort = vm.sort, currentFilter = vm.filter,
                onApply = { s, f -> vm.sort = s; vm.filter = f; wsSheet = false },
                onDismiss = { wsSheet = false }
            )
        }

        if (mdSheet) {
            ModuleSortFilterSheet(
                currentSort = vm.moduleSort, currentFilter = vm.moduleFilter,
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

        itemToDelete?.let { target ->
            val attCount = vm.attachmentsOf(target.id).size
            val relCount = vm.relationsOf(target.id).size
            AlertDialog(
                onDismissRequest = { itemToDelete = null },
                containerColor = Color.White,
                title = {
                    Text("删除「${target.name}」？",
                        fontSize = 16.5.sp, fontWeight = FontWeight.Bold, color = Ink)
                },
                text = {
                    Column {
                        Text(
                            "该物品有 $attCount 个附件、$relCount 条关联关系。\n" +
                                "删除后与附件一起进入回收站，30 天内可恢复。",
                            fontSize = 13.sp, color = Muted, lineHeight = 21.sp
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        vm.deleteItem(target.id)
                        itemToDelete = null
                        screen = Screen.Items
                    }) {
                        Text("删除", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { itemToDelete = null }) {
                        Text("取消", fontSize = 14.sp, color = Teal)
                    }
                }
            )
        }
    }
}
