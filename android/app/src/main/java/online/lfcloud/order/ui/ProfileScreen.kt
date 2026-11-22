package online.lfcloud.order.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import online.lfcloud.order.OrderViewModel
import online.lfcloud.order.data.Account
import online.lfcloud.order.ui.theme.Bg
import online.lfcloud.order.ui.theme.Danger
import online.lfcloud.order.ui.theme.HairLine
import online.lfcloud.order.ui.theme.Ink
import online.lfcloud.order.ui.theme.Muted
import online.lfcloud.order.ui.theme.MutedLight
import online.lfcloud.order.ui.theme.Soft2
import online.lfcloud.order.ui.theme.Teal

/** 默认头像：圆底 + 人头轮廓，不读取任何用户图片。 */
@Composable
fun DefaultAvatar(sizeDp: Int) {
    Box(
        Modifier.size(sizeDp.dp).clip(CircleShape).background(Soft2),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.size((sizeDp * 0.52f).dp)) {
            val w = size.width
            val h = size.height
            val stroke = 1.8.dp.toPx()
            drawCircle(
                color = Muted,
                radius = w * 0.17f,
                center = Offset(w / 2f, h * 0.33f),
                style = Stroke(width = stroke)
            )
            val path = Path().apply {
                moveTo(w * 0.17f, h * 0.90f)
                cubicTo(w * 0.22f, h * 0.60f, w * 0.78f, h * 0.60f, w * 0.83f, h * 0.90f)
            }
            drawPath(path, color = Muted, style = Stroke(width = stroke, cap = StrokeCap.Round))
        }
    }
}

@Composable
fun ProfileScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onEditField: (String) -> Unit,
    onEditPassword: () -> Unit
) {
    val a = vm.account

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.fillMaxWidth().height(50.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text("账号", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {

            Column(
                Modifier.fillMaxWidth().padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DefaultAvatar(86)
                Spacer(Modifier.height(14.dp))
                Text(a.username, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
            }

            Spacer(Modifier.height(18.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            ProfileRow("用户名", a.username, true) { onEditField(Account.FIELD_NAME) }
            ProfileRow("用户 ID", a.uid, false) {}
            ProfileRow("手机号", a.phone, true) { onEditField(Account.FIELD_PHONE) }
            ProfileRow("邮箱", a.email, true) { onEditField(Account.FIELD_EMAIL) }

            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            ProfileRow("登录密码", "修改", true, valueMuted = true) { onEditPassword() }

            Spacer(Modifier.height(8.dp))
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))

            ProfileRow("物品总数", vm.itemTotal().toString(), false) {}
            ProfileRow("写入权限", vm.writeHolderText(), false) {}

            Spacer(Modifier.height(40.dp))
        }
    }
}

@Composable
private fun ProfileRow(
    label: String,
    value: String,
    clickable: Boolean,
    valueMuted: Boolean = false,
    onClick: () -> Unit
) {
    Column {
        Row(
            Modifier.fillMaxWidth()
                .then(if (clickable) Modifier.clickable(onClick = onClick) else Modifier)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 14.5.sp, color = Ink, modifier = Modifier.width(84.dp))
            Text(
                value, fontSize = 14.sp,
                color = if (valueMuted) Muted else Ink,
                maxLines = 1,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
            if (clickable) {
                Text("  ›", fontSize = 14.sp, color = MutedLight)
            }
        }
        Box(Modifier.fillMaxWidth().padding(start = 16.dp).height(1.dp).background(HairLine))
    }
}

/** 单字段修改页：用户名 / 手机号 / 邮箱共用。 */
@Composable
fun AccountFieldEditScreen(
    field: String,
    current: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onBack: () -> Unit,
    onSave: (String) -> Boolean
) {
    var value by remember { mutableStateOf(current) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text("修改$field", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
            Text(
                "保存", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                color = if (value.isBlank()) MutedLight else Teal,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .clickable {
                        if (onSave(value)) onBack() else error = "不能为空"
                    }
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            Spacer(Modifier.height(20.dp))
            Text(field, fontSize = 12.5.sp, color = Muted)
            Spacer(Modifier.height(6.dp))
            Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
                BasicTextField(
                    value = value,
                    onValueChange = { value = it; error = null },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                    textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                    cursorBrush = SolidColor(Teal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error!!, fontSize = 11.5.sp, color = Danger)
            }
        }
    }
}

@Composable
fun PasswordEditScreen(
    onBack: () -> Unit,
    onSave: (String, String) -> Boolean
) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().background(Bg)) {
        Box(Modifier.fillMaxWidth().height(52.dp)) {
            Icon(
                Icons.Filled.ArrowBack, contentDescription = "返回", tint = Ink,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
                    .size(22.dp).clickable(onClick = onBack)
            )
            Text("修改密码", fontSize = 16.5.sp, fontWeight = FontWeight.SemiBold, color = Ink,
                modifier = Modifier.align(Alignment.Center))
            Text(
                "保存", fontSize = 14.5.sp, fontWeight = FontWeight.SemiBold,
                color = if (next.isBlank() || confirm.isBlank()) MutedLight else Teal,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
                    .clickable {
                        when {
                            current.isBlank() -> error = "请输入当前密码"
                            next != confirm -> error = "两次输入不一致"
                            !onSave(current, next) -> error = "新密码至少 8 位"
                            else -> onBack()
                        }
                    }
            )
        }

        Column(Modifier.padding(horizontal = 16.dp)) {
            PasswordField("当前密码", current) { current = it; error = null }
            PasswordField("新密码", next) { next = it; error = null }
            PasswordField("确认新密码", confirm) { confirm = it; error = null }
            if (error != null) {
                Spacer(Modifier.height(6.dp))
                Text(error!!, fontSize = 11.5.sp, color = Danger)
            }
        }
    }
}

@Composable
private fun PasswordField(label: String, value: String, onChange: (String) -> Unit) {
    Column {
        Spacer(Modifier.height(20.dp))
        Text(label, fontSize = 12.5.sp, color = Muted)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(44.dp), contentAlignment = Alignment.CenterStart) {
            BasicTextField(
                value = value,
                onValueChange = onChange,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                visualTransformation = PasswordVisualTransformation(),
                textStyle = TextStyle(fontSize = 16.sp, color = Ink),
                cursorBrush = SolidColor(Teal),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(HairLine))
    }
}
