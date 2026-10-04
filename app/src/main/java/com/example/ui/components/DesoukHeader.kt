package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.DesoukNavyLight

@Composable
fun DesoukHeader(
    selectedDistrict: String,
    onMenuClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onDistrictClick: () -> Unit,
    onAiAssistantClick: () -> Unit = {},
    unreadNotificationsCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val bellTransition = rememberInfiniteTransition(label = "bell_pulse")
    val bellScale by bellTransition.animateFloat(
        initialValue = if (unreadNotificationsCount > 0) 0.92f else 1f,
        targetValue = if (unreadNotificationsCount > 0) 1.08f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bell_scale"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // Curved navy background taking full screen width
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(
                    RoundedCornerShape(
                        bottomStart = 36.dp,
                        bottomEnd = 36.dp
                    )
                )
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            DesoukNavyDark,
                            DesoukNavyLight
                        )
                    )
                )
                .statusBarsPadding()
                .padding(top = 12.dp, bottom = 32.dp, start = 16.dp, end = 16.dp)
        ) {
            // Top Bar:
            // In RTL layout:
            // 1st child (Start = Right): أيقونة الإشعارات وزر المساعد الذكي في أعلى اليمين
            // 2nd child (Center): شعار "شقق وعقارات دسوق" في المنتصف
            // 3rd child (End = Left): زر القائمة الجانبية في أعلى اليسار
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // أعلى اليمين: أيقونة الإشعارات + زر المساعد العقاري الذكي
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    IconButton(
                        onClick = onNotificationsClick,
                        modifier = Modifier
                            .size(42.dp)
                            .scale(if (unreadNotificationsCount > 0) bellScale else 1f)
                            .background(
                                if (unreadNotificationsCount > 0)
                                    Color.White.copy(alpha = 0.18f)
                                else
                                    Color.White.copy(alpha = 0.12f),
                                CircleShape
                            )
                            .border(
                                width = if (unreadNotificationsCount > 0) 1.5.dp else 0.dp,
                                color = if (unreadNotificationsCount > 0) DesoukGold else Color.Transparent,
                                shape = CircleShape
                            )
                            .testTag("notification_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(
                                        containerColor = BadgeForSaleRed,
                                        contentColor = Color.White,
                                        modifier = Modifier.offset(x = 2.dp, y = (-2).dp)
                                    ) {
                                        Text(
                                            text = if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (unreadNotificationsCount > 0) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                contentDescription = "الإشعارات",
                                tint = if (unreadNotificationsCount > 0) DesoukGold else Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    // زر المساعد العقاري الذكي في أعلى رأس التطبيق
                    IconButton(
                        onClick = onAiAssistantClick,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, DesoukGold.copy(alpha = 0.8f), CircleShape)
                            .testTag("header_ai_assistant_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "المساعد العقاري الذكي",
                            tint = DesoukGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // المنتصف: شعار "عقارات دسوق Ai" مع أيقونة الفيلا الذهبية 3D
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon3DLogoVilla(
                        size = 38.dp,
                        modifier = Modifier.testTag("header_3d_logo_villa")
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "عقارات دسوق Ai",
                        color = Color.White,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "بيتك .. بين إيديك",
                        color = DesoukGold,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                // أعلى اليسار: زر القائمة الجانبية
                IconButton(
                    onClick = onMenuClick,
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color.White.copy(alpha = 0.12f), CircleShape)
                        .testTag("menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "القائمة الجانبية",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // شريط اختيار المنطقة: "دسوق - كفر الشيخ" مع أيقونة الموقع والسهم
        // متمركز عند المنحنى السفلي للشريط الأزرق الداكن
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 20.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(24.dp))
                .clickable { onDistrictClick() }
                .testTag("district_selector"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // في RTL: أيقونة الموقع أولاً (على يمين النص)
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = DesoukGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = selectedDistrict,
                    color = DesoukNavyDark,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(6.dp))
                // السهم لأسفل (على يسار النص في RTL)
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "اختر المنطقة",
                    tint = DesoukNavyDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
