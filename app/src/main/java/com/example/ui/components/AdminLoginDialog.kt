package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun AdminLoginDialog(
    errorMessage: String? = null,
    currentRole: String = "User",
    onDismiss: () -> Unit,
    onLoginWithRole: (email: String, name: String, phone: String, pin: String, role: String) -> Unit,
    onAuthenticate: (String) -> Unit = {}
) {
    var selectedRole by remember { mutableStateOf("Admin") } // "Admin" or "User"
    var pin by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(if (selectedRole == "Admin") "admin@desouk-aqar.com" else "user@desouk.com") }
    var name by remember { mutableStateOf(if (selectedRole == "Admin") "Super Admin (الإدارة العامة)" else "مستخدم عقارات دسوق") }
    var phone by remember { mutableStateOf(if (selectedRole == "Admin") "01000000000" else "01099887766") }
    var isPinVisible by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (selectedRole == "Admin") DesoukNavyDark else Color(0xFF1565C0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (selectedRole == "Admin") Icons.Default.AdminPanelSettings else Icons.Default.Person,
                        contentDescription = selectedRole,
                        tint = DesoukGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "تسجيل الدخول والتحقق من الدور",
                        color = DesoukNavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "التحقق من دور (Admin vs User) في Firestore",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Role Switcher Tabs (Admin vs User)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Admin Role Tab
                    val isAdminSelected = selectedRole == "Admin"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isAdminSelected) DesoukNavyDark else Color.Transparent)
                            .clickable {
                                selectedRole = "Admin"
                                email = "admin@desouk-aqar.com"
                                name = "Super Admin (الإدارة العامة)"
                                phone = "01000000000"
                            }
                            .padding(vertical = 8.dp)
                            .testTag("select_admin_role_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AdminPanelSettings,
                                contentDescription = null,
                                tint = if (isAdminSelected) DesoukGold else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مشرف (Admin)",
                                color = if (isAdminSelected) Color.White else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // User Role Tab
                    val isUserSelected = selectedRole == "User"
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isUserSelected) Color(0xFF1565C0) else Color.Transparent)
                            .clickable {
                                selectedRole = "User"
                                email = "user@desouk.com"
                                name = "أحمد دسوقي (مستخدم عادي)"
                                phone = "01099887766"
                            }
                            .padding(vertical = 8.dp)
                            .testTag("select_user_role_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (isUserSelected) Color.White else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "مستخدم (User)",
                                color = if (isUserSelected) Color.White else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Information Box based on chosen role
                if (selectedRole == "Admin") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFF9E6))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = DesoukNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سيتم تخزين دور 'Admin' لحسابك في مجموعة Users في Firestore، وفتح واجهة لوحة التحكم بصلاحيات كاملة.",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني للأدمن") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = pin,
                        onValueChange = { if (it.length <= 16) pin = it },
                        label = { Text("رمز اعتماد الأدمن (Master PIN)") },
                        placeholder = { Text("أدخل رمز 123456...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Key,
                                contentDescription = null,
                                tint = DesoukGold
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { isPinVisible = !isPinVisible }) {
                                Icon(
                                    imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (isPinVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                if (pin.isNotBlank()) {
                                    onLoginWithRole(email, name, phone, pin, "Admin")
                                    onAuthenticate(pin)
                                }
                            }
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("admin_pin_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Text(
                        text = "الرمز الافتراضي المعتمد للـ Admin: 123456",
                        color = TextMuted,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFE8F4FD))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF1565C0),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "سيتم تخزين دور 'User' في Firestore. لن تظهر واجهة لوحة التحكم لهذا الحساب، وستقتصر صلاحياته على تصفح وإضافة العقارات الشخصية.",
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("اسم المستخدم") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = TextMuted) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                // Error Message
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = BadgeForSaleRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedRole == "Admin") {
                        onLoginWithRole(email, name, phone, pin, "Admin")
                        onAuthenticate(pin)
                    } else {
                        onLoginWithRole(email, name, phone, "", "User")
                    }
                },
                enabled = if (selectedRole == "Admin") pin.isNotBlank() else name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedRole == "Admin") DesoukNavyDark else Color(0xFF1565C0)
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_role_auth_button")
            ) {
                Icon(
                    imageVector = if (selectedRole == "Admin") Icons.Default.Lock else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedRole == "Admin") "تأكيد الدخول كـ Admin" else "تأكيد الدخول كـ User",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(18.dp)
    )
}
