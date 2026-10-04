package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun AuthDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (email: String, pass: String) -> Unit,
    onEmailSignUp: (email: String, pass: String, name: String, phone: String) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    if (!isOpen) return

    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // رأس النافذة مع زر الإغلاق
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = TextMuted)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "عقارات دسوق AI",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark
                        )
                        Text(
                            text = if (isSignUpMode) "إنشاء حساب جديد" else "تسجيل الدخول",
                            fontSize = 12.sp,
                            color = DesoukGold,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Box(modifier = Modifier.size(40.dp))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 1. زر تسجيل الدخول المباشر بواسطة Google
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !isLoading) { onGoogleSignIn() }
                        .testTag("google_sign_in_button"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.2.dp, Color(0xFFDADCE0)),
                    shadowElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEA4335)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "متابعة باستخدام حساب Google",
                            color = Color(0xFF3C4043),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // خط فاصل: أو بالبريد الإلكتروني
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                    Text(
                        text = "  أو بالبريد الإلكتروني  ",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE0E0E0))
                }

                Spacer(modifier = Modifier.height(14.dp))

                // تبديل بين تسجيل دخول / إنشاء حساب
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SurfaceBackground, RoundedCornerShape(10.dp))
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isSignUpMode) DesoukNavyDark else Color.Transparent)
                            .clickable { isSignUpMode = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تسجيل الدخول",
                            color = if (!isSignUpMode) Color.White else DesoukNavyDark,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSignUpMode) DesoukNavyDark else Color.Transparent)
                            .clickable { isSignUpMode = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "حساب جديد",
                            color = if (isSignUpMode) Color.White else DesoukNavyDark,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (isSignUpMode) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("الاسم بالكامل") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DesoukNavyDark) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف (اختياري)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DesoukNavyDark) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("البريد الإلكتروني") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = DesoukNavyDark) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("كلمة المرور") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = DesoukNavyDark) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage,
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (isSignUpMode) {
                            onEmailSignUp(email, password, name, phone)
                        } else {
                            onEmailSignIn(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading && email.isNotBlank() && password.isNotBlank()
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            text = if (isSignUpMode) "إنشاء الحساب الآن" else "تسجيل الدخول",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
