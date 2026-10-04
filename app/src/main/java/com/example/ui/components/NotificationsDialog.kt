package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppNotification
import com.example.data.NotificationType
import com.example.data.UserInterests
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * نافذة مركز الإشعارات والتنبيهات:
 * تنبيه المستخدم عند وجود عقارات جديدة مطابقة لاحتياجاته أو عند الرد على رسائله.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsDialog(
    notifications: List<AppNotification>,
    userInterests: UserInterests? = null,
    onUpdateInterests: ((UserInterests) -> Unit)? = null,
    onTestPush: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onNotificationClick: (AppNotification) -> Unit = {},
    onMarkAllAsRead: () -> Unit = {},
    onClearAll: () -> Unit = {}
) {
    var selectedFilter by remember { mutableStateOf("الكل") }

    val filteredNotifications = remember(notifications, selectedFilter) {
        when (selectedFilter) {
            "عقارات مطابقة 🏠" -> notifications.filter { it.type == NotificationType.MATCHING_PROPERTY }
            "ردود الرسائل 💬" -> notifications.filter { it.type == NotificationType.MESSAGE_REPLY }
            "غير مقروءة 🔔" -> notifications.filter { !it.isRead }
            else -> notifications
        }
    }

    val unreadCount = notifications.count { !it.isRead }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // شريط العنوان العلوي الفاخر
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("notifications_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = DesoukNavyDark
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مركز الإشعارات والتنبيهات",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark
                        )
                        if (unreadCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = CircleShape,
                                color = DesoukGold
                            ) {
                                Text(
                                    text = unreadCount.toString(),
                                    color = DesoukNavyDark,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "عقارات مطابقة لاحتياجاتك • ردود المحادثات",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                // زر تحديد الكل كمقروء
                IconButton(
                    onClick = onMarkAllAsRead,
                    enabled = unreadCount > 0,
                    modifier = Modifier.testTag("notifications_mark_all_read_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "تحديد الكل كمقروء",
                        tint = if (unreadCount > 0) DesoukGold else Color(0xFFB0BEC5)
                    )
                }
            }

            // بطاقة ربط اهتمامات المستخدم وإشعارات Firebase Cloud Messaging الدفعية (FCM)
            if (userInterests != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F7FF)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBDEFB))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(DesoukNavyDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = DesoukGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "إشعارات الدفع الفورية (FCM)",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DesoukNavyDark
                                    )
                                    Text(
                                        text = "تنبيهك فور توفر عقار يطابق معاييرك",
                                        fontSize = 11.sp,
                                        color = TextMuted
                                    )
                                }
                            }

                            Switch(
                                checked = userInterests.isPushNotificationsEnabled,
                                onCheckedChange = { isEnabled ->
                                    onUpdateInterests?.invoke(userInterests.copy(isPushNotificationsEnabled = isEnabled))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = DesoukNavyDark,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCFD8DC)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // تفاصيل الاهتمامات المحددة
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "📍 ${userInterests.preferredDistrict} • 💰 حتى ${(userInterests.maxPrice / 1000).toInt()} ألف ج.م • 🏠 ${userInterests.preferredCategory}",
                                fontSize = 11.sp,
                                color = DesoukNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // زر تجربة إشعار الدفع
                        Button(
                            onClick = { onTestPush?.invoke() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp)
                                .testTag("test_fcm_push_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = DesoukNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اختبار إشعار دفع مطابق (FCM Test)",
                                color = DesoukNavyDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // فلاتر الإشعارات الأفقية
            val filters = listOf(
                "الكل (${notifications.size})",
                "عقارات مطابقة 🏠",
                "ردود الرسائل 💬",
                "غير مقروءة 🔔"
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filterName ->
                    val isSelected = selectedFilter.startsWith(filterName.substringBefore(" (")) || selectedFilter == filterName
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) DesoukNavyDark else SurfaceBackground,
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) DesoukGold else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier.clickable {
                            selectedFilter = when {
                                filterName.startsWith("الكل") -> "الكل"
                                filterName.startsWith("عقارات مطابقة") -> "عقارات مطابقة 🏠"
                                filterName.startsWith("ردود الرسائل") -> "ردود الرسائل 💬"
                                else -> "غير مقروءة 🔔"
                            }
                        }
                    ) {
                        Text(
                            text = filterName,
                            color = if (isSelected) DesoukGold else TextSecondary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // قائمة الإشعارات
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp, horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = null,
                            tint = Color(0xFFB0BEC5),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "لا توجد إشعارات جديدة حالياً",
                            color = DesoukNavyDark,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "سننبهك فوراً عند رصد عقارات تناسب متطلباتك أو عند الرد على رسائلك",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { notif ->
                        NotificationItemCard(
                            notification = notif,
                            onClick = { onNotificationClick(notif) }
                        )
                    }

                    item {
                        // زر مسح الإشعارات
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Row(
                                modifier = Modifier
                                    .clickable { onClearAll() }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "مسح جميع الإشعارات",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * كرت كل إشعار
 */
@Composable
private fun NotificationItemCard(
    notification: AppNotification,
    onClick: () -> Unit
) {
    val isUnread = !notification.isRead

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("notification_item_${notification.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnread) Color(0xFFF8FAFC) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isUnread) 1.5.dp else 1.dp,
            color = if (isUnread) DesoukGold.copy(alpha = 0.8f) else Color(0xFFECEFF1)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnread) 3.dp else 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            // أيقونة نوع الإشعار بتدرج مميز
            val iconBrush = when (notification.type) {
                NotificationType.MATCHING_PROPERTY -> Brush.linearGradient(
                    listOf(Color(0xFF00897B), Color(0xFF004D40))
                )
                NotificationType.MESSAGE_REPLY -> Brush.linearGradient(
                    listOf(DesoukNavyDark, Color(0xFF0D47A1))
                )
                NotificationType.SYSTEM -> Brush.linearGradient(
                    listOf(DesoukGold, Color(0xFFFFA000))
                )
            }

            val iconVector = when (notification.type) {
                NotificationType.MATCHING_PROPERTY -> Icons.Default.Apartment
                NotificationType.MESSAGE_REPLY -> Icons.AutoMirrored.Filled.Chat
                NotificationType.SYSTEM -> Icons.Default.NotificationsActive
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBrush),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconVector,
                    contentDescription = null,
                    tint = if (notification.type == NotificationType.SYSTEM) DesoukNavyDark else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = notification.title,
                        color = DesoukNavyDark,
                        fontSize = 13.5.sp,
                        fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold
                    )

                    if (isUnread) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(DesoukGold)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = notification.message,
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (notification.categoryBadge.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (notification.type) {
                                NotificationType.MATCHING_PROPERTY -> Color(0xFFE0F2F1)
                                NotificationType.MESSAGE_REPLY -> Color(0xFFE3F2FD)
                                NotificationType.SYSTEM -> Color(0xFFFFF8E1)
                            }
                        ) {
                            Text(
                                text = notification.categoryBadge,
                                color = when (notification.type) {
                                    NotificationType.MATCHING_PROPERTY -> Color(0xFF00695C)
                                    NotificationType.MESSAGE_REPLY -> Color(0xFF1565C0)
                                    NotificationType.SYSTEM -> Color(0xFFF57F17)
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = notification.timeAgo,
                        color = TextMuted,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}
