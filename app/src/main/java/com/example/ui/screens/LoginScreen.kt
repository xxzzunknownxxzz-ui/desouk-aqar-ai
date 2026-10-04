package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukGoldDark
import com.example.ui.theme.DesoukGoldSoft
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.DesoukNavyLight
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * شاشة تسجيل الدخول وإنشاء الحساب الرسمية لتطبيق عقارات دسوق AI
 * تدعم تسجيل الدخول المباشر بواسطة Google عبر Credential Manager في Firebase Authentication
 * ومزامنة وحفظ المستخدم الجديد تلقائياً في Firestore
 */
@Composable
fun LoginScreen(
    onGoogleSignIn: () -> Unit,
    onEmailSignIn: (email: String, pass: String) -> Unit,
    onEmailSignUp: (email: String, pass: String, name: String, phone: String) -> Unit,
    onContinueAsGuest: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var isSignUpMode by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    fun validateAndSubmit() {
        validationError = null
        focusManager.clearFocus()

        if (isSignUpMode) {
            if (name.trim().isBlank()) {
                validationError = "يرجى كتابة الاسم بالكامل"
                return
            }
            if (email.trim().isBlank() || !email.contains("@")) {
                validationError = "يرجى إدخال بريد إلكتروني صالح"
                return
            }
            if (password.length < 6) {
                validationError = "كلمة المرور يجب ألا تقل عن 6 أحرف أو أرقام"
                return
            }
            onEmailSignUp(email.trim(), password.trim(), name.trim(), phone.trim())
        } else {
            if (email.trim().isBlank() || !email.contains("@")) {
                validationError = "يرجى إدخال البريد الإلكتروني"
                return
            }
            if (password.isBlank()) {
                validationError = "يرجى إدخال كلمة المرور"
                return
            }
            onEmailSignIn(email.trim(), password.trim())
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // شريط التنقل العلوي
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = DesoukNavyDark
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = DesoukGoldSoft,
                    border = BorderStroke(1.dp, DesoukGold.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = DesoukGoldDark,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مصادقة Google & Firebase",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark
                        )
                    }
                }

                Spacer(modifier = Modifier.width(48.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // الهيدر واللوجو الفخم
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(DesoukNavyDark, DesoukNavyLight)
                        )
                    )
                    .shadow(10.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏠",
                    fontSize = 38.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "أهلاً بك في عقارات دسوق AI",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark,
                textAlign = TextAlign.Center
            )

            Text(
                text = "سجّل دخولك للوصول إلى العقارات المحفوظة والمساعد العقاري الذكي ومزامنة طلباتك سحابياً",
                fontSize = 13.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ==========================================
            // 1. زر تسجيل الدخول الرسمي عبر Google (Credential Manager)
            // ==========================================
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(enabled = !isLoading) { onGoogleSignIn() }
                    .testTag("google_sign_in_btn"),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.5.dp, Color(0xFFDADCE0)),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = DesoukGold,
                            strokeWidth = 2.5.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "جاري الاتصال بـ Google...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DesoukNavyDark
                        )
                    } else {
                        // شعار Google الملون الدقيق
                        GoogleLogoIcon(modifier = Modifier.size(26.dp))

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(horizontalAlignment = Alignment.Start) {
                            Text(
                                text = "تسجيل الدخول باستخدام Google",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "دخول فوري وآمن عبر Credential Manager",
                                fontSize = 10.5.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // فاصل أو
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                Text(
                    text = " أو بالبريد الإلكتروني ",
                    fontSize = 12.sp,
                    color = TextMuted,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
            }

            Spacer(modifier = Modifier.height(14.dp))

            // التبديل بين تسجيل الدخول وإنشاء حساب
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF1F5F9)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (!isSignUpMode) Color.White else Color.Transparent)
                            .clickable {
                                isSignUpMode = false
                                validationError = null
                            }
                            .testTag("tab_sign_in"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "تسجيل الدخول",
                            fontSize = 13.sp,
                            fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (!isSignUpMode) DesoukNavyDark else TextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSignUpMode) Color.White else Color.Transparent)
                            .clickable {
                                isSignUpMode = true
                                validationError = null
                            }
                            .testTag("tab_sign_up"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "إنشاء حساب جديد",
                            fontSize = 13.sp,
                            fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSignUpMode) DesoukNavyDark else TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // رسائل الخطأ والتنبيهات
            val activeError = errorMessage ?: validationError
            AnimatedVisibility(
                visible = !activeError.isNullOrBlank(),
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusErrorBg),
                    border = BorderStroke(1.dp, StatusError.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = StatusError,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeError.orEmpty(),
                            fontSize = 12.5.sp,
                            color = StatusError,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // حقول إدخال البيانات
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (isSignUpMode) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("الاسم بالكامل") },
                            placeholder = { Text("مثال: محمد أحمد") },
                            leadingIcon = {
                                Icon(Icons.Default.Person, contentDescription = null, tint = DesoukNavyLight)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("name_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedIndicatorColor = DesoukGold,
                                unfocusedIndicatorColor = Color(0xFFE2E8F0)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("رقم الهاتف (اختياري)") },
                            placeholder = { Text("010xxxxxxxx") },
                            leadingIcon = {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = DesoukNavyLight)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("phone_input"),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFFAFAFA),
                                unfocusedContainerColor = Color(0xFFFAFAFA),
                                focusedIndicatorColor = DesoukGold,
                                unfocusedIndicatorColor = Color(0xFFE2E8F0)
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("البريد الإلكتروني") },
                        placeholder = { Text("your.email@example.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = DesoukNavyLight)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedIndicatorColor = DesoukGold,
                            unfocusedIndicatorColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("كلمة المرور") },
                        placeholder = { Text("••••••••") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = DesoukNavyLight)
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "إخفاء كلمة المرور" else "إظهار كلمة المرور",
                                    tint = TextMuted
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { validateAndSubmit() }),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFFAFAFA),
                            unfocusedContainerColor = Color(0xFFFAFAFA),
                            focusedIndicatorColor = DesoukGold,
                            unfocusedIndicatorColor = Color(0xFFE2E8F0)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("login_submit_btn"),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = DesoukNavyDark,
                            contentColor = Color.White
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isSignUpMode) "إنشاء حساب ومزامنة السحابة" else "تسجيل الدخول",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // تنبيه أمان Firestore التلقائي
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = StatusSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "عند أول تسجيل دخول عبر Google، يتم إنشاء حسابك وحفظ معرف UID وسجل النشاط تلقائياً وبأمان في قاعدة بيانات السحابة Firestore.",
                        fontSize = 11.5.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // زر المتابعة كزائر أو تخطي
            OutlinedButton(
                onClick = onContinueAsGuest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("skip_guest_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFCBD5E1))
            ) {
                Text(
                    text = "تصفح التطبيق كزائر 🏠",
                    fontSize = 14.sp,
                    color = DesoukNavyLight,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

/**
 * أيقونة Google الرسمية الدقيقة بألوانها الأربعة (الأزرق، الأحمر، الأصفر، الأخضر)
 */
@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val size = size.minDimension
        val strokeWidth = size * 0.18f
        val center = Offset(size / 2f, size / 2f)
        val radius = (size - strokeWidth) / 2f

        // الألوان الأربعة الرسمية لجوجل
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)
        val blue = Color(0xFF4285F4)

        // القوس الأحمر (أعلى)
        drawArc(
            color = red,
            startAngle = 180f,
            sweepAngle = 110f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // القوس الأصفر (يسار)
        drawArc(
            color = yellow,
            startAngle = 135f,
            sweepAngle = 55f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // القوس الأخضر (أسفل)
        drawArc(
            color = green,
            startAngle = 35f,
            sweepAngle = 110f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // القوس الأزرق (يمين)
        drawArc(
            color = blue,
            startAngle = 0f,
            sweepAngle = 45f,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // الخط الأزرق الأفقي في منتصف الـ G
        drawLine(
            color = blue,
            start = Offset(center.x, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
