package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun DesoukDrawerContent(
    onNavigate: (NavTab) -> Unit,
    onOpenDistricts: () -> Unit,
    onOpenAdmin: () -> Unit = {},
    onCloseDrawer: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    ModalDrawerSheet(
        drawerContainerColor = Color.White,
        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier
            .width(300.dp)
            .fillMaxHeight()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .navigationBarsPadding()
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DesoukNavyDark)
                    .padding(horizontal = 20.dp, vertical = 32.dp)
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(DesoukNavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon3DLogoVilla(size = 46.dp)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "عقارات دسوق Ai",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "دليلك الأول للعقارات في مدينة دسوق",
                        color = DesoukGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Navigation Items
            DrawerMenuItem(
                icon = Icons.Default.Home,
                title = "الرئيسية",
                onClick = {
                    onNavigate(NavTab.HOME)
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Favorite,
                title = "العقارات المحفوظة (المفضلة) ❤️",
                onClick = {
                    onNavigate(NavTab.FAVORITES)
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.SmartToy,
                title = "المساعد العقاري الذكي ✨",
                onClick = {
                    onNavigate(NavTab.AI_ASSISTANT)
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Search,
                title = "البحث والتصفية",
                onClick = {
                    onNavigate(NavTab.SEARCH)
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.AddBusiness,
                title = "إضافة إعلان عقاري",
                onClick = {
                    onNavigate(NavTab.ADD)
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.AutoMirrored.Filled.Chat,
                title = "المحادثات والرسائل",
                onClick = {
                    onNavigate(NavTab.CHATS)
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Favorite,
                title = "العقارات المفضلة",
                onClick = {
                    onNavigate(NavTab.FAVORITES)
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.LocationCity,
                title = "أحياء ومناطق دسوق",
                onClick = {
                    onCloseDrawer()
                    onOpenDistricts()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Person,
                title = "حسابي",
                onClick = {
                    onNavigate(NavTab.PROFILE)
                    onCloseDrawer()
                }
            )

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                color = Color(0xFFF1F5F9)
            )

            DrawerMenuItem(
                icon = Icons.Default.AdminPanelSettings,
                title = "لوحة تحكم الإدارة (Super Admin)",
                onClick = {
                    onCloseDrawer()
                    onOpenAdmin()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Call,
                title = "تواصل عبر واتساب",
                onClick = {
                    openWhatsApp(context, "201001234567", "مرحباً، أود الاستفسار بخصوص عقارات دسوق")
                    onCloseDrawer()
                }
            )

            DrawerMenuItem(
                icon = Icons.Default.Share,
                title = "مشاركة التطبيق",
                onClick = {
                    shareApp(context)
                    onCloseDrawer()
                }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Version info at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "الإصدار 1.0 • دسوق - كفر الشيخ",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("drawer_item_${title.take(12).trim()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFF1F5F9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = DesoukNavyDark,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}

private fun openWhatsApp(context: Context, phone: String, message: String) {
    try {
        val uri = Uri.parse("https://wa.me/$phone?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun shareApp(context: Context) {
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(
            Intent.EXTRA_TEXT,
            "حمل تطبيق 'شقق وعقارات دسوق' لتصفح وبيع وشراء وتأجير العقارات بأفضل الأسعار!"
        )
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة التطبيق"))
}
