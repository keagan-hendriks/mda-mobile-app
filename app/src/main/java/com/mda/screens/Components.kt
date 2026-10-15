package com.mda.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------------------------------------------------------------
// Shared building blocks. UI teammate: change colors / sizes HERE
// and every screen updates.
// ---------------------------------------------------------------

object AppColors {
    val Ink = Color(0xFF0F172A)
    val Muted = Color(0xFF64748B)
    val Border = Color(0xFFE2E8F0)
    val Surface = Color(0xFFF8FAFC)
    val ButtonFill = Color(0xFFE2E8F0)
    val Link = Color(0xFF4F46E5)
    val Danger = Color(0xFFEF4444)
    val Success = Color(0xFF10B981)
}

/** Scrollable white page with side padding. Every screen starts with this. */
@Composable
fun ScreenColumn(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        content = content
    )
}

@Composable
fun ScreenHeader(title: String, subtitle: String) {
    Column(Modifier.padding(top = 16.dp, bottom = 16.dp)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = AppColors.Ink)
        Text(subtitle, fontSize = 14.sp, color = AppColors.Muted)
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        color = AppColors.Muted,
        letterSpacing = 0.5.sp,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    color: Color = AppColors.Surface,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = color,
        border = BorderStroke(1.dp, AppColors.Border),
        content = content
    )
}

@Composable
fun AppButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.ButtonFill,
            contentColor = AppColors.Ink
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

@Composable
fun OutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = AppColors.Ink
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, AppColors.Border),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
    ) {
        Text(text, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
    }
}

/** Label + text box. Set isPassword = true to get the Show/Hide toggle. */
@Composable
fun FormField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    isPassword: Boolean = false
) {
    var visible by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
        Text(
            label,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = AppColors.Ink,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = AppColors.Muted) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            visualTransformation =
                if (isPassword && !visible) PasswordVisualTransformation() else VisualTransformation.None,
            trailingIcon = if (isPassword) {
                {
                    TextButton(onClick = { visible = !visible }) {
                        Text(if (visible) "Hide" else "Show", fontSize = 12.sp)
                    }
                }
            } else null
        )
    }
}
