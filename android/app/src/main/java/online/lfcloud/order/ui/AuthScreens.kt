package online.lfcloud.order.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.ui.theme.Bg
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Soft2
import online.lfcloud.order.ui.theme.Teal

@Composable
fun LoginScreen(
    vm: OrderViewModel,
    onGoRegister: () -> Unit
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val canSubmit = account.isNotBlank() && password.isNotBlank() && !vm.authBusy

    Column(Modifier.fillMaxSize().background(Bg)) {

        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 48.dp)) {
            Text("物序", fontSize = 30.sp, fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-1).sp, color = Teal)
            Spacer(Modifier.height(8.dp))
            Text("物品的空间化管理", fontSize = 13.sp, color = Muted)
        }

        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 36.dp)) {
            FieldLine(
                label = "邮箱或手机号",
                value = account,
                placeholder = "",
                onChange = { account = it; vm.clearAuthError() }
            )
            Spacer(Modifier.height(22.dp))
            FieldLine(
                label = "密码",
                value = password,
                placeholder = "",
                password = true,
                onChange = { password = it; vm.clearAuthError() }
            )
        }

        if (vm.authError != null) {
            Spacer(Modifier.height(12.dp))
            Text(vm.authError!!, fontSize = 12.5.sp, color = Danger,
                modifier = Modifier.padding(horizontal = 24.dp))
        }

        Spacer(Modifier.height(28.dp))
        PrimaryButton(
            text = "登录",
            enabled = canSubmit,
            loading = vm.authBusy,
            onClick = { vm.login(account, password) }
        )

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("", fontSize = 13.5.sp)
            Text(
                "没有账号？注册",
                fontSize = 13.5.sp, color = Teal,
                modifier = Modifier.clickable { onGoRegister() }
            )
        }
    }
}

@Composable
fun RegisterScreen(
    vm: OrderViewModel,
    onBack: () -> Unit
) {
    var account by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val canSubmit = account.isNotBlank() && password.length >= 8 &&
        password == confirm && !vm.authBusy

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text("注册", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
        }

        Column(Modifier.padding(start = 24.dp, end = 24.dp, top = 22.dp)) {
            FieldLine("邮箱或手机号", account, "") { account = it; localError = null; vm.clearAuthError() }
            Spacer(Modifier.height(22.dp))
            FieldLine("设置密码（至少 8 位）", password, "", password = true) {
                password = it; localError = null; vm.clearAuthError()
            }
            Spacer(Modifier.height(22.dp))
            FieldLine("确认密码", confirm, "", password = true) {
                confirm = it; localError = null; vm.clearAuthError()
            }
        }

        val error = localError ?: vm.authError
        if (error != null) {
            Spacer(Modifier.height(12.dp))
            Text(error, fontSize = 12.5.sp, color = Danger,
                modifier = Modifier.padding(horizontal = 24.dp))
        }

        Spacer(Modifier.height(30.dp))
        PrimaryButton(
            text = "创建账号",
            enabled = canSubmit,
            loading = vm.authBusy,
            onClick = {
                when {
                    account.trim().length < 3 -> localError = "账号至少 3 个字符"
                    password.length < 8 -> localError = "密码至少 8 位"
                    password != confirm -> localError = "两次输入的密码不一致"
                    else -> vm.register(account, password)
                }
            }
        )
    }
}

@Composable
private fun FieldLine(
    label: String,
    value: String,
    placeholder: String,
    password: Boolean = false,
    onChange: (String) -> Unit
) {
    Column {
        Text(label, fontSize = 12.5.sp, color = Muted)
        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(40.dp), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                Text(placeholder, fontSize = 16.sp, color = MutedLight)
            }
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (password) KeyboardType.Password else KeyboardType.Text
                ),
                visualTransformation = if (password) PasswordVisualTransformation()
                else androidx.compose.ui.text.input.VisualTransformation.None,
                textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                cursorBrush = SolidColor(Teal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit
) {
    Box(
        Modifier.fillMaxWidth().padding(horizontal = 24.dp).height(50.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (enabled) Teal else Soft2)
            .then(if (enabled) Modifier.clickable(onClick = onClick) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = Color.White, strokeWidth = 2.dp,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(
                text, fontSize = 15.5.sp, fontWeight = FontWeight.SemiBold,
                color = if (enabled) Color.White else Muted,
                letterSpacing = 0.3.sp
            )
        }
    }
}
