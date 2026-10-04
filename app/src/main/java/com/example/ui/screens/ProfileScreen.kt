package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.PropertyEntity
import com.example.data.ReferralAndShareManager
import com.example.data.SavedSearchItem
import com.example.data.SavedSearchManager
import com.example.data.UserSession
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ProfileScreen(
    userSession: UserSession,
    userProperties: List<PropertyEntity>,
    favoritesCount: Int,
    onOpenRoleLogin: () -> Unit = {},
    onOpenAdminGate: () -> Unit = {},
    onOpenNotifications: () -> Unit = {},
    onOpenAuthDialog: () -> Unit = {},
    onUpdateProfile: (name: String, phone: String) -> Unit = { _, _ -> },
    onLogout: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onNavigateToAdd: () -> Unit = {},
    onNavigateToSearchWithCriteria: ((SavedSearchItem) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val savedSearchManager = remember { SavedSearchManager(context) }
    val savedSearches by savedSearchManager.savedSearches.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }

    var editName by remember { mutableStateOf(userSession.name) }
    var editPhone by remember { mutableStateOf(userSession.phone) }

    // حساب نسبة اكتمال الملف الشخصي
    val profileProgress = remember(userSession) {
        var p = 0.25f
        if (userSession.isLoggedIn) p += 0.25f
        if (userSession.name.isNotBlank() && userSession.name != "مستخدم عقارات دسوق") p += 0.25f
        if (userSession.phone.isNotBlank() || userSession.email.isNotBlank()) p += 0.25f
        p
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        // 1. بطاقة المستخدم الرئيسية
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // الصورة أو الرمز التعريفي
                    Box(
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(if (userSession.role == "Admin") DesoukNavyDark else Color(0xFF1565C0)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (userSession.photoUrl.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(context).data(userSession.photoUrl).crossfade(true).build(),
                                contentDescription = "صورة المستخدم",
                                modifier = Modifier.fillMaxSize().clip(CircleShape)
                            )
                        } else {
                            Text(
                                text = userSession.name.take(1).ifBlank { "م" },
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = userSession.name,
                                fontSize = 16.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DesoukNavyDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // شارة الدور
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (userSession.role == "Admin") DesoukGold else Color(0xFFE8F5E9))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (userSession.role == "Admin") "Admin 🛡️" else "مستخدم 👤",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (userSession.role == "Admin") DesoukNavyDark else Color(0xFF2E7D32)
                                )
                            }
                        }

                        if (userSession.email.isNotBlank()) {
                            Text(
                                text = userSession.email,
                                fontSize = 12.sp,
                                color = TextMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (userSession.uid.isNotBlank()) {
                            Text(
                                text = "معرّف الحساب: ${userSession.uid.take(12)}...",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    if (userSession.isLoggedIn) {
                        IconButton(onClick = {
                            editName = userSession.name
                            editPhone = userSession.phone
                            showEditProfileDialog = true
                        }) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = DesoukNavyDark)
                        }
                    } else {
                        Button(
                            onClick = onOpenAuthDialog,
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(text = "دخول", color = DesoukNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // شريط إكمال الملف الشخصي (#14)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "اكتمال الملف الشخصي", fontSize = 11.5.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                        Text(text = "${(profileProgress * 100).toInt()}%", fontSize = 11.5.sp, color = DesoukNavyDark, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { profileProgress },
                        modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                        color = DesoukGold,
                        trackColor = Color(0xFFEEEEEE)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. إحصائيات الحساب الأربعة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AccountStatBox(
                title = "المفضلة",
                count = favoritesCount.toString(),
                icon = Icons.Default.Favorite,
                iconColor = BadgeForSaleRed,
                modifier = Modifier.weight(1f)
            )
            AccountStatBox(
                title = "أبحاث محفوظة",
                count = savedSearches.size.toString(),
                icon = Icons.Default.Bookmark,
                iconColor = Color(0xFF1976D2),
                modifier = Modifier.weight(1f)
            )
            AccountStatBox(
                title = "إعلاناتي",
                count = userProperties.size.toString(),
                icon = Icons.Default.Home,
                iconColor = Color(0xFF388E3C),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 3. قسم "ادعُ أصدقاءك" وتتبع الإحالات (#17, #21, #22)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.2.dp, DesoukGold.copy(alpha = 0.7f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFF8E1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFFF57F17), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ادعُ أصدقاءك إلى عقارات دسوق AI 🎁",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark
                        )
                        Text(
                            text = "شارك التطبيق مع أصحابك وساعدهم يلاقوا العقار المناسب.",
                            fontSize = 11.5.sp,
                            color = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // مربع كود الإحالة الخاص بالمستخدم
                val refCode = userSession.referralCode.ifBlank { "DESOUK-2026" }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SurfaceBackground,
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "كود الدعوة الخاص بك:", fontSize = 11.sp, color = TextMuted)
                            Text(text = refCode, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                        }
                        Button(
                            onClick = { ReferralAndShareManager.copyReferralCode(context, refCode) },
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = DesoukGold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "نسخ", fontSize = 11.5.sp, color = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // أزرار المشاركة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { ReferralAndShareManager.shareApp(context, refCode) },
                        modifier = Modifier.weight(1f).height(40.dp).testTag("share_app_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "مشاركة التطبيق 📤", color = DesoukNavyDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { ReferralAndShareManager.shareOnWhatsApp(context, refCode) },
                        modifier = Modifier.weight(1f).height(40.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32))
                    ) {
                        Text(text = "واتساب 💬", color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 4. قسم عمليات البحث المحفوظة (#23, #24)
        if (savedSearches.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "🔍 عمليات البحث المحفوظة (${savedSearches.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    savedSearches.take(4).forEach { searchItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { onNavigateToSearchWithCriteria?.invoke(searchItem) }
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = searchItem.title,
                                    fontSize = 12.5.sp,
                                    color = DesoukNavyDark,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            IconButton(
                                onClick = { savedSearchManager.deleteSearch(searchItem.id) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = TextMuted, modifier = Modifier.size(16.dp))
                            }
                        }
                        HorizontalDivider(color = Color(0xFFF0F0F0))
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 5. قسم الإعدادات والأمان والخصوصية (#40, #41)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "⚙️ الإعدادات والحساب",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )

                Spacer(modifier = Modifier.height(10.dp))

                // زر مركز الإشعارات
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "مركز الإشعارات والتنبيهات",
                    subtitle = "متابعة أحدث العقارات المطابقة وتحديثات الأسعار",
                    iconColor = Color(0xFF1976D2),
                    onClick = onOpenNotifications
                )

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // سياسة الخصوصية
                ProfileMenuItem(
                    icon = Icons.Default.PrivacyTip,
                    title = "سياسة الخصوصية والأمان",
                    subtitle = "بياناتك مشفرة ولا يتم مشاركة أي معلومات خاصة",
                    iconColor = Color(0xFF388E3C),
                    onClick = { showPrivacyPolicyDialog = true }
                )

                // خيار لوحة الإدارة
                if (userSession.role == "Admin") {
                    HorizontalDivider(color = Color(0xFFF0F0F0))
                    ProfileMenuItem(
                        icon = Icons.Default.AdminPanelSettings,
                        title = "لوحة تحكم الإدارة (Super Admin)",
                        subtitle = "إدارة مصادر العقارات وتدقيق البيانات والإحصائيات",
                        iconColor = DesoukGold,
                        onClick = onOpenAdminGate
                    )
                }

                HorizontalDivider(color = Color(0xFFF0F0F0))

                // زر تسجيل الخروج
                if (userSession.isLoggedIn) {
                    ProfileMenuItem(
                        icon = Icons.Default.ExitToApp,
                        title = "تسجيل الخروج",
                        subtitle = "إنهاء الجلسة الحالية والعودة لصفحة الدخول",
                        iconColor = Color(0xFFD32F2F),
                        onClick = { showLogoutDialog = true }
                    )

                    HorizontalDivider(color = Color(0xFFF0F0F0))

                    // زر حذف الحساب
                    ProfileMenuItem(
                        icon = Icons.Default.DeleteForever,
                        title = "حذف الحساب نهائيًا",
                        subtitle = "حذف حسابك وكافة البيانات المرتبطة به بأمان",
                        iconColor = Color(0xFFC2185B),
                        onClick = { showDeleteAccountDialog = true }
                    )
                } else {
                    ProfileMenuItem(
                        icon = Icons.Default.Lock,
                        title = "تسجيل الدخول / إنشاء حساب",
                        subtitle = "سجل دخولك لحفظ العقارات ومزامنتها على السحابة",
                        iconColor = DesoukNavyDark,
                        onClick = onOpenAuthDialog
                    )
                }
            }
        }
    }

    // تأكيد تسجيل الخروج (#5)
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(text = "تأكيد تسجيل الخروج", fontWeight = FontWeight.Bold, color = DesoukNavyDark) },
            text = { Text("هل أنت متأكد من رغبتك في تسجيل الخروج؟ لن يتم حذف أي بيانات أو عقارات محفوظة.") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("نعم، تسجيل الخروج", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // تأكيد حذف الحساب (#41)
    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(text = "⚠️ تحذير: حذف الحساب نهائيًا", fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F)) },
            text = {
                Text(
                    text = "هل أنت متأكد تمامًا من رغبتك في حذف حسابك؟\nسيتم حذف بيانات الحساب المسجلة بشكل نهائي ولا يمكن التراجع عن هذه الخطوة.",
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                ) {
                    Text("تأكيد الحذف النهائي", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // حوار تعديل الملف الشخصي
    if (showEditProfileDialog) {
        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text(text = "تعديل الملف الشخصي", fontWeight = FontWeight.Bold, color = DesoukNavyDark) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("الاسم") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPhone,
                        onValueChange = { editPhone = it },
                        label = { Text("رقم الهاتف") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showEditProfileDialog = false
                        onUpdateProfile(editName, editPhone)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark)
                ) {
                    Text("حفظ التعديلات", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("إلغاء", color = TextSecondary)
                }
            }
        )
    }

    // سياسة الخصوصية
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text(text = "سياسة الخصوصية والأمان", fontWeight = FontWeight.Bold, color = DesoukNavyDark) },
            text = {
                Text(
                    text = """
                        🛡️ نلتزم في "عقارات دسوق AI" بأعلى معايير حماية البيانات والخصوصية:
                        • لا نقوم بمشاركة أي بيانات اتصال أو هواتف مع أي طرف ثالث.
                        • كلمات المرور وبيانات المصادقة مشفرة بالكامل عبر Firebase Authentication.
                        • يتم استخدام اهتمامات البحث فقط لتنبيهك بالعقارات المناسبة في دسوق.
                        • يحق لك حذف حسابك وكافة بياناتك المسجلة في أي وقت بضغطة واحدة.
                    """.trimIndent(),
                    fontSize = 12.5.sp,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark)
                ) {
                    Text("إغلاق", color = Color.White)
                }
            }
        )
    }
}

@Composable
fun AccountStatBox(
    title: String,
    count: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 1.5.dp
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = count, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
            Text(text = title, fontSize = 11.sp, color = TextMuted)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    iconColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 13.5.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
            Text(text = subtitle, fontSize = 11.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = Color(0xFFB0BEC5),
            modifier = Modifier.size(16.dp)
        )
    }
}
