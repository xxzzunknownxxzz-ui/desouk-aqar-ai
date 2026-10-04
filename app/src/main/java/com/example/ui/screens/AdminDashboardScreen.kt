package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AdminActivityLogEntity
import com.example.data.AppSettingEntity
import com.example.data.AppUserEntity
import com.example.data.CustomerInquiryEntity
import com.example.data.CustomerRequestEntity
import com.example.data.ImportLogEntity
import com.example.data.ImportedPropertyEntity
import com.example.data.InquiryStatus
import com.example.data.PropertyEntity
import com.example.data.PropertySourceEntity
import com.example.data.PropertyStatus
import com.example.data.RequestStatus
import com.example.data.RequestType
import com.example.data.SourcesDashboardStats
import com.example.data.UserRole
import com.example.data.ai.FacebookImportAiService
import com.example.data.ai.FacebookExtractedAd
import com.example.ui.components.FacebookImportDialog
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ListAlt
import com.example.ui.theme.BadgeForRentTeal
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminDashboardScreen(
    properties: List<PropertyEntity>,
    customerRequests: List<CustomerRequestEntity>,
    inquiries: List<CustomerInquiryEntity>,
    users: List<AppUserEntity>,
    activityLogs: List<AdminActivityLogEntity>,
    settings: List<AppSettingEntity>,
    sources: List<PropertySourceEntity> = emptyList(),
    importedProperties: List<ImportedPropertyEntity> = emptyList(),
    reviewQueue: List<ImportedPropertyEntity> = emptyList(),
    importLogs: List<ImportLogEntity> = emptyList(),
    sourcesStats: SourcesDashboardStats = SourcesDashboardStats(),
    onBackToApp: () -> Unit,
    onLogout: () -> Unit,
    aiService: FacebookImportAiService = remember { FacebookImportAiService() },
    onSaveAndPublishFacebookAd: (FacebookExtractedAd, isDuplicateOverride: Boolean, (Long) -> Unit) -> Unit = { _, _, _ -> },
    onSaveFacebookAdAsDraft: (FacebookExtractedAd, (Long) -> Unit) -> Unit = { _, _ -> },
    onSaveFacebookAdAsNeedsReview: (FacebookExtractedAd, (Long) -> Unit) -> Unit = { _, _ -> },
    onViewPropertyDetail: (Long) -> Unit = {},
    onAddSource: (name: String, type: String, url: String, notes: String) -> Unit = { _, _, _, _ -> },
    onToggleSourceActive: (Long, Boolean) -> Unit = { _, _ -> },
    onDeleteSource: (Long) -> Unit = {},
    onSyncSource: (PropertySourceEntity) -> Unit = {},
    onImportRawPost: (rawText: String, sourceName: String, postUrl: String, postExternalId: String, onSuccess: () -> Unit) -> Unit = { _, _, _, _, _ -> },
    onApproveAndPublishImported: (ImportedPropertyEntity, adminNotes: String) -> Unit = { _, _ -> },
    onRejectImported: (ImportedPropertyEntity, reason: String) -> Unit = { _, _ -> },
    onRequestReviewImported: (Long, notes: String) -> Unit = { _, _ -> },
    onUpdateAndPublishImported: (ImportedPropertyEntity) -> Unit = {},
    onDeleteImported: (Long) -> Unit = {},
    onSaveProperty: (
        id: Long,
        title: String,
        category: String,
        type: String,
        location: String,
        district: String,
        street: String,
        price: Double,
        priceUnit: String,
        rooms: Int,
        bathrooms: Int,
        area: Double,
        floor: Int,
        totalFloors: Int,
        finishing: String,
        hasElevator: Boolean,
        hasGarage: Boolean,
        hasMeters: Boolean,
        features: String,
        phone: String,
        whatsapp: String,
        videoUrl: String,
        imageResName: String,
        description: String,
        status: String,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit
    ) -> Unit,
    onChangePropertyStatus: (Long, String, String) -> Unit,
    onArchiveProperty: (Long, String) -> Unit,
    onRestoreProperty: (Long, String) -> Unit,
    onDeleteProperty: (Long, String) -> Unit,
    onUpdateRequestStatus: (Long, String) -> Unit,
    onArchiveRequest: (Long) -> Unit,
    onDeleteRequest: (Long) -> Unit,
    onCreateRequest: (
        customerName: String,
        customerPhone: String,
        requestType: String,
        propertyCategory: String,
        targetDistrict: String,
        budget: String,
        targetArea: String,
        targetRooms: String,
        details: String
    ) -> Unit,
    onReplyInquiry: (Long, String, String) -> Unit,
    onDeleteInquiry: (Long) -> Unit,
    onToggleUserBlock: (Long, String, Boolean) -> Unit,
    onDeleteUser: (Long, String) -> Unit,
    onUpdateSetting: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }
    var showFacebookImportDialog by remember { mutableStateOf(false) }
    var showAddPropertyDialog by remember { mutableStateOf(false) }
    var editingProperty by remember { mutableStateOf<PropertyEntity?>(null) }
    var showAddRequestDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "نظرة عامة" to Icons.Default.AdminPanelSettings,
        "استيراد من فيسبوك" to Icons.Default.Share,
        "العقارات المستوردة" to Icons.Default.ListAlt,
        "مصادر العقارات" to Icons.Default.CloudDownload,
        "إدارة العقارات" to Icons.Default.Home,
        "طلبات العملاء" to Icons.Default.CheckCircle,
        "الاستفسارات" to Icons.Default.QuestionAnswer,
        "المستخدمون" to Icons.Default.People,
        "سجل العمليات" to Icons.Default.History,
        "الإعدادات" to Icons.Default.Settings
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
    ) {
        // Top Admin Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = DesoukNavyDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBackToApp,
                            modifier = Modifier.testTag("admin_back_to_app_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "الرجوع للتطبيق",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "لوحة تحكم Super Admin",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(DesoukGold)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "دسوق Ai",
                                        color = DesoukNavyDark,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(
                                text = "جلسة إدارة عامة مشفرة ومؤمنة بالكامل",
                                color = DesoukGold.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { showFacebookImportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_top_facebook_import_button")
                        ) {
                            Text("📥", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استيراد العقارات من فيسبوك", color = Color.White, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Logout / Lock Button
                        Button(
                            onClick = onLogout,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_logout_button")
                        ) {
                            Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("قفل الجلسة", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Sub-Tabs Scroll
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, (label, icon) ->
                        val isSelected = selectedSubTab == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) DesoukGold else Color.White.copy(alpha = 0.12f))
                                .clickable { selectedSubTab = index }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("admin_subtab_$index"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    tint = if (isSelected) DesoukNavyDark else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = label,
                                    color = if (isSelected) DesoukNavyDark else Color.White,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Content Area based on selectedSubTab
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            when (selectedSubTab) {
                0 -> AdminOverviewTab(
                    properties = properties,
                    requests = customerRequests,
                    inquiries = inquiries,
                    users = users,
                    sourcesStats = sourcesStats,
                    onNavigateToTab = { selectedSubTab = it },
                    onAddNewProperty = {
                        editingProperty = null
                        showAddPropertyDialog = true
                    },
                    onOpenFacebookImport = { showFacebookImportDialog = true }
                )
                1 -> FacebookImportScreen(
                    aiService = aiService,
                    existingProperties = properties,
                    importedProperties = importedProperties,
                    onSaveAndPublish = onSaveAndPublishFacebookAd,
                    onSaveAsDraft = onSaveFacebookAdAsDraft,
                    onSaveAsNeedsReview = onSaveFacebookAdAsNeedsReview,
                    onViewExistingProperty = onViewPropertyDetail,
                    onDeleteImportedAd = onDeleteImported,
                    onBack = { selectedSubTab = 0 },
                    initialStep = 0
                )
                2 -> FacebookImportScreen(
                    aiService = aiService,
                    existingProperties = properties,
                    importedProperties = importedProperties,
                    onSaveAndPublish = onSaveAndPublishFacebookAd,
                    onSaveAsDraft = onSaveFacebookAdAsDraft,
                    onSaveAsNeedsReview = onSaveFacebookAdAsNeedsReview,
                    onViewExistingProperty = onViewPropertyDetail,
                    onDeleteImportedAd = onDeleteImported,
                    onBack = { selectedSubTab = 0 },
                    initialStep = 2
                )
                3 -> AdminSourcesTab(
                    sources = sources,
                    importedProperties = importedProperties,
                    reviewQueue = reviewQueue,
                    importLogs = importLogs,
                    stats = sourcesStats,
                    onAddSource = onAddSource,
                    onToggleSourceActive = onToggleSourceActive,
                    onDeleteSource = onDeleteSource,
                    onSyncSource = onSyncSource,
                    onImportRawPost = onImportRawPost,
                    onApproveAndPublish = onApproveAndPublishImported,
                    onReject = onRejectImported,
                    onRequestReview = onRequestReviewImported,
                    onUpdateAndPublish = onUpdateAndPublishImported,
                    onDeleteImported = onDeleteImported
                )
                4 -> AdminPropertiesTab(
                    properties = properties,
                    onAddNew = {
                        editingProperty = null
                        showAddPropertyDialog = true
                    },
                    onEdit = { prop ->
                        editingProperty = prop
                        showAddPropertyDialog = true
                    },
                    onChangeStatus = onChangePropertyStatus,
                    onArchive = onArchiveProperty,
                    onRestore = onRestoreProperty,
                    onDelete = onDeleteProperty
                )
                5 -> AdminRequestsTab(
                    requests = customerRequests,
                    onAddNewRequest = { showAddRequestDialog = true },
                    onUpdateStatus = onUpdateRequestStatus,
                    onArchive = onArchiveRequest,
                    onDelete = onDeleteRequest
                )
                6 -> AdminInquiriesTab(
                    inquiries = inquiries,
                    onReply = onReplyInquiry,
                    onDelete = onDeleteInquiry
                )
                7 -> AdminUsersTab(
                    users = users,
                    onToggleBlock = onToggleUserBlock,
                    onDeleteUser = onDeleteUser
                )
                8 -> AdminActivityLogTab(
                    logs = activityLogs
                )
                9 -> AdminSettingsTab(
                    settings = settings,
                    onSaveSetting = onUpdateSetting
                )
            }
        }
    }

    // Modal: Add / Edit Property Dialog
    if (showAddPropertyDialog) {
        AdminPropertyFormDialog(
            existing = editingProperty,
            onDismiss = {
                showAddPropertyDialog = false
                editingProperty = null
            },
            onSave = { id, title, category, type, location, district, street, price, priceUnit,
                       rooms, bathrooms, area, floor, totalFloors, finishing, hasElevator, hasGarage,
                       hasMeters, features, phone, whatsapp, videoUrl, imageResName, description, status, lat, lng ->
                onSaveProperty(
                    id, title, category, type, location, district, street, price, priceUnit,
                    rooms, bathrooms, area, floor, totalFloors, finishing, hasElevator, hasGarage,
                    hasMeters, features, phone, whatsapp, videoUrl, imageResName, description, status, lat, lng
                ) {
                    showAddPropertyDialog = false
                    editingProperty = null
                }
            }
        )
    }

    // Modal: Add Customer Request Dialog
    if (showAddRequestDialog) {
        AdminAddRequestDialog(
            onDismiss = { showAddRequestDialog = false },
            onSubmit = { name, phone, type, category, district, budget, area, rooms, details ->
                onCreateRequest(name, phone, type, category, district, budget, area, rooms, details)
                showAddRequestDialog = false
            }
        )
    }

    // Modal: Facebook Import Dialog (استيراد العقارات من فيسبوك)
    if (showFacebookImportDialog) {
        FacebookImportDialog(
            aiService = aiService,
            existingProperties = properties,
            importedProperties = importedProperties,
            onSaveAndPublish = onSaveAndPublishFacebookAd,
            onApproveAndPublishImported = { item, notes, onSuccess ->
                onApproveAndPublishImported(item, notes)
                onSuccess()
            },
            onUpdateImported = { item, onSuccess ->
                onUpdateAndPublishImported(item)
                onSuccess()
            },
            onDeleteImported = onDeleteImported,
            onDismiss = { showFacebookImportDialog = false }
        )
    }
}

// ========================================================
// 1. تبويب النظرة العامة والإحصائيات
// ========================================================
@Composable
fun AdminOverviewTab(
    properties: List<PropertyEntity>,
    requests: List<CustomerRequestEntity>,
    inquiries: List<CustomerInquiryEntity>,
    users: List<AppUserEntity>,
    sourcesStats: SourcesDashboardStats = SourcesDashboardStats(),
    onNavigateToTab: (Int) -> Unit,
    onAddNewProperty: () -> Unit,
    onOpenFacebookImport: () -> Unit = {}
) {
    val totalProps = properties.size
    val forSaleProps = properties.count { it.type == "للبيع" }
    val forRentProps = properties.count { it.type == "للإيجار" }
    val publishedProps = properties.count { it.status == PropertyStatus.PUBLISHED }
    val archivedProps = properties.count { it.status == PropertyStatus.ARCHIVED }
    val soldOrRentedProps = properties.count { it.status == PropertyStatus.SOLD || it.status == PropertyStatus.RENTED }
    val totalUsers = users.size
    val totalRequests = requests.size
    val newRequests = requests.count { it.status == RequestStatus.NEW }
    val totalInquiries = inquiries.size
    val newInquiries = inquiries.count { it.status == InquiryStatus.NEW }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Quick Action Bar - Facebook Import Hero
        Button(
            onClick = onOpenFacebookImport,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("admin_overview_facebook_hero_button")
        ) {
            Text("📥", fontSize = 16.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text("استيراد العقارات من فيسبوك", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onAddNewProperty,
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_quick_add_prop_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة عقار جديد", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }

            OutlinedButton(
                onClick = { onNavigateToTab(3) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("مصادر العقارات (${sourcesStats.inReviewCount} للمراجعة)", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold, fontSize = 10.5.sp)
            }
        }

        // بطاقات إحصائيات مصادر العقارات بناءً على مجموعات Firestore
        FirestoreStatsGrid(stats = sourcesStats)

        // Summary Metric Cards Grid (2 rows)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricCard(title = "إجمالي العقارات", value = "$totalProps", icon = Icons.Default.Home, color = DesoukNavyDark, modifier = Modifier.weight(1f))
            AdminMetricCard(title = "معروض للبيع", value = "$forSaleProps", icon = Icons.Default.Star, color = BadgeForSaleRed, modifier = Modifier.weight(1f))
            AdminMetricCard(title = "معروض للإيجار", value = "$forRentProps", icon = Icons.Default.LocationOn, color = BadgeForRentTeal, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricCard(title = "عقارات منشورة", value = "$publishedProps", icon = Icons.Default.Check, color = Color(0xFF2E7D32), modifier = Modifier.weight(1f))
            AdminMetricCard(title = "عقارات مؤرشفة", value = "$archivedProps", icon = Icons.Default.Archive, color = Color(0xFFE65100), modifier = Modifier.weight(1f))
            AdminMetricCard(title = "صفقات مكتملة", value = "$soldOrRentedProps", icon = Icons.Default.VerifiedUser, color = DesoukGold, modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AdminMetricCard(title = "المستخدمون", value = "$totalUsers", icon = Icons.Default.People, color = DesoukNavyDark, modifier = Modifier.weight(1f))
            AdminMetricCard(title = "طلبات العملاء", value = "$totalRequests", icon = Icons.Default.CheckCircle, color = Color(0xFF1565C0), modifier = Modifier.weight(1f))
            AdminMetricCard(title = "الاستفسارات", value = "$totalInquiries", icon = Icons.Default.QuestionAnswer, color = Color(0xFF6A1B9A), modifier = Modifier.weight(1f))
        }

        // Sale vs Rent Visual Ratio
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "نسبة البيع إلى الإيجار في دسوق",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DesoukNavyDark
                )
                Spacer(modifier = Modifier.height(10.dp))
                val totalActive = (forSaleProps + forRentProps).coerceAtLeast(1)
                val saleRatio = forSaleProps.toFloat() / totalActive.toFloat()
                LinearProgressIndicator(
                    progress = { saleRatio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = BadgeForSaleRed,
                    trackColor = BadgeForRentTeal,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "للبيع: $forSaleProps (${(saleRatio * 100).toInt()}%)", color = BadgeForSaleRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "للإيجار: $forRentProps (${((1 - saleRatio) * 100).toInt()}%)", color = BadgeForRentTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // District Breakdown (أحياء ومناطق دسوق)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "توزيع العقارات حسب أحياء دسوق والمحيط",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = DesoukNavyDark
                )
                Text(
                    text = "كثافة العرض والطلب الميداني داخل مراكز دسوق",
                    color = TextMuted,
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                val desoukDistricts = listOf(
                    "الميدان الإبراهيمي",
                    "كورنيش النيل",
                    "شارع الشركات",
                    "شارع الجيش",
                    "طريق فوه",
                    "المحرقة",
                    "حي دحروج",
                    "المناطق المحيطة"
                )

                desoukDistricts.forEach { distName ->
                    val count = properties.count { it.district.contains(distName) || it.location.contains(distName) }
                    val percent = if (totalProps > 0) (count.toFloat() / totalProps) else 0f
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = distName, fontSize = 12.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                            Text(text = "$count عقار (${(percent * 100).toInt()}%)", fontSize = 12.sp, color = DesoukNavyDark, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { percent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DesoukGold,
                            trackColor = Color(0xFFF0F0F0)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DesoukNavyDark)
            Text(text = title, fontSize = 10.sp, color = TextMuted, textAlign = TextAlign.Center, maxLines = 1)
        }
    }
}

// ========================================================
// 2. تبويب إدارة العقارات (PROPERTIES MANAGEMENT)
// ========================================================
@Composable
fun AdminPropertiesTab(
    properties: List<PropertyEntity>,
    onAddNew: () -> Unit,
    onEdit: (PropertyEntity) -> Unit,
    onChangeStatus: (Long, String, String) -> Unit,
    onArchive: (Long, String) -> Unit,
    onRestore: (Long, String) -> Unit,
    onDelete: (Long, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }
    val filters = listOf("الكل", "للبيع", "للإيجار", "منشور", "مؤرشف", "مباع/مؤجر")

    val filteredList = properties.filter { prop ->
        val matchesSearch = searchQuery.isBlank() ||
                prop.title.contains(searchQuery, ignoreCase = true) ||
                prop.district.contains(searchQuery, ignoreCase = true) ||
                prop.category.contains(searchQuery, ignoreCase = true) ||
                prop.phone.contains(searchQuery)

        val matchesFilter = when (selectedFilter) {
            "للبيع" -> prop.type == "للبيع"
            "للإيجار" -> prop.type == "للإيجار"
            "منشور" -> prop.status == PropertyStatus.PUBLISHED
            "مؤرشف" -> prop.status == PropertyStatus.ARCHIVED
            "مباع/مؤجر" -> prop.status == PropertyStatus.SOLD || prop.status == PropertyStatus.RENTED
            else -> true
        }
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search & Add Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("بحث بالعنوان، الحي، النوع، أو الهاتف...", fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("admin_property_search_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesoukGold,
                    unfocusedBorderColor = Color(0xFFD0D7DE)
                ),
                singleLine = true
            )

            Button(
                onClick = onAddNew,
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("admin_add_property_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            filters.forEach { f ->
                val isSelected = selectedFilter == f
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DesoukNavyDark else Color.White)
                        .border(1.dp, if (isSelected) DesoukNavyDark else Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .clickable { selectedFilter = f }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = f,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Text(
            text = "النتائج المعروضة: ${filteredList.size} عقار",
            fontSize = 11.sp,
            color = TextMuted
        )

        // Properties Lazy List
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "لا توجد عقارات مطابقة لمعايير البحث الحالية", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { prop ->
                    AdminPropertyCardItem(
                        property = prop,
                        onEdit = { onEdit(prop) },
                        onChangeStatus = { newStatus -> onChangeStatus(prop.id, prop.title, newStatus) },
                        onArchive = { onArchive(prop.id, prop.title) },
                        onRestore = { onRestore(prop.id, prop.title) },
                        onDelete = { onDelete(prop.id, prop.title) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminPropertyCardItem(
    property: PropertyEntity,
    onEdit: () -> Unit,
    onChangeStatus: (String) -> Unit,
    onArchive: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    var showStatusMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val formatter = NumberFormat.getNumberInstance(Locale.US)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row: Category, Type, Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (property.type == "للبيع") BadgeForSaleRed.copy(alpha = 0.12f) else BadgeForRentTeal.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = property.type,
                            color = if (property.type == "للبيع") BadgeForSaleRed else BadgeForRentTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DesoukNavyDark.copy(alpha = 0.08f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = property.category,
                            color = DesoukNavyDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Status Badge with clickable change
                Box {
                    val (statusColor, statusBg) = when (property.status) {
                        PropertyStatus.PUBLISHED -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
                        PropertyStatus.ARCHIVED -> Color(0xFFE65100) to Color(0xFFFFF3E0)
                        PropertyStatus.SOLD, PropertyStatus.RENTED -> DesoukGold to Color(0xFFFFF9E6)
                        else -> Color(0xFF616161) to Color(0xFFEEEEEE)
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
                            .clickable { showStatusMenu = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = property.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = statusColor, modifier = Modifier.size(12.dp))
                    }

                    DropdownMenu(
                        expanded = showStatusMenu,
                        onDismissRequest = { showStatusMenu = false }
                    ) {
                        listOf(
                            PropertyStatus.PUBLISHED,
                            PropertyStatus.SOLD,
                            PropertyStatus.RENTED,
                            PropertyStatus.UNAVAILABLE,
                            PropertyStatus.ARCHIVED
                        ).forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st, fontSize = 12.sp) },
                                onClick = {
                                    onChangeStatus(st)
                                    showStatusMenu = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title & Location
            Text(
                text = property.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DesoukNavyDark,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${property.district} - ${property.street.ifBlank { property.location }}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Specs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatter.format(property.price)} ${property.priceUnit}",
                    color = DesoukGold,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "${property.area.toInt()} م² | ${property.rooms} غرف | دور ${property.floor}",
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل", fontSize = 11.sp, color = DesoukNavyDark, fontWeight = FontWeight.Bold)
                }

                if (property.status == PropertyStatus.ARCHIVED) {
                    Button(
                        onClick = onRestore,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Unarchive, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استرجاع", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    OutlinedButton(
                        onClick = onArchive,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Archive, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("أرشفة", fontSize = 11.sp, color = Color(0xFFE65100), fontWeight = FontWeight.Bold)
                    }
                }

                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف نهائي", tint = BadgeForSaleRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("تأكيد حذف العقار نهائياً", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
            text = { Text("هل أنت متأكد من رغبتك في حذف '${property.title}' نهائياً من قاعدة البيانات؟ لا يمكن التراجع عن هذا الإجراء.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BadgeForSaleRed)
                ) {
                    Text("نعم، حذف نهائي", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }
}

// ========================================================
// 3. تبويب طلبات العملاء (CUSTOMER REQUESTS)
// ========================================================
@Composable
fun AdminRequestsTab(
    requests: List<CustomerRequestEntity>,
    onAddNewRequest: () -> Unit,
    onUpdateStatus: (Long, String) -> Unit,
    onArchive: (Long) -> Unit,
    onDelete: (Long) -> Unit
) {
    val context = LocalContext.current
    var selectedStatusFilter by remember { mutableStateOf("الكل") }
    val statuses = listOf("الكل", RequestStatus.NEW, RequestStatus.CONTACTED, RequestStatus.IN_PROGRESS, RequestStatus.COMPLETED, RequestStatus.CANCELLED)

    val filteredRequests = requests.filter { req ->
        if (selectedStatusFilter == "الكل") true else req.status == selectedStatusFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "طلبات العملاء العقارية (${filteredRequests.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = DesoukNavyDark
            )

            Button(
                onClick = onAddNewRequest,
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("admin_add_request_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("تسجيل طلب عميل", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
            }
        }

        // Status chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statuses.forEach { st ->
                val isSelected = selectedStatusFilter == st
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) DesoukNavyDark else Color.White)
                        .border(1.dp, if (isSelected) DesoukNavyDark else Color(0xFFE0E0E0), RoundedCornerShape(8.dp))
                        .clickable { selectedStatusFilter = st }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = st,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (filteredRequests.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد طلبات عملاء مطابقة في هذا التصنيف", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredRequests, key = { it.id }) { req ->
                    AdminRequestCardItem(
                        request = req,
                        onUpdateStatus = { st -> onUpdateStatus(req.id, st) },
                        onArchive = { onArchive(req.id) },
                        onDelete = { onDelete(req.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun AdminRequestCardItem(
    request: CustomerRequestEntity,
    onUpdateStatus: (String) -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showStatusDropdown by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Customer Name, Request Type, Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = request.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DesoukNavyDark
                    )
                    Text(
                        text = "${request.requestType} • ${request.propertyCategory ?: "عقار"}",
                        fontSize = 11.sp,
                        color = DesoukGold,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box {
                    val statusColor = when (request.status) {
                        RequestStatus.NEW -> BadgeForSaleRed
                        RequestStatus.CONTACTED, RequestStatus.IN_PROGRESS -> Color(0xFF1565C0)
                        RequestStatus.COMPLETED -> Color(0xFF2E7D32)
                        else -> TextMuted
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .clickable { showStatusDropdown = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = request.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.Settings, contentDescription = null, tint = statusColor, modifier = Modifier.size(12.dp))
                    }

                    DropdownMenu(
                        expanded = showStatusDropdown,
                        onDismissRequest = { showStatusDropdown = false }
                    ) {
                        listOf(
                            RequestStatus.NEW,
                            RequestStatus.CONTACTED,
                            RequestStatus.IN_PROGRESS,
                            RequestStatus.COMPLETED,
                            RequestStatus.CANCELLED
                        ).forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st, fontSize = 12.sp) },
                                onClick = {
                                    onUpdateStatus(st)
                                    showStatusDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Details info
            if (!request.targetDistrict.isNullOrBlank() || !request.budget.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!request.targetDistrict.isNullOrBlank()) {
                        Text(text = "📍 المنطقة: ${request.targetDistrict}", fontSize = 11.sp, color = TextPrimary)
                    }
                    if (!request.budget.isNullOrBlank()) {
                        Text(text = "💰 الميزانية: ${request.budget}", fontSize = 11.sp, color = TextPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (!request.targetArea.isNullOrBlank() || !request.targetRooms.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "📐 المواصفات: ${request.targetArea.orEmpty()} ${if (!request.targetRooms.isNullOrBlank()) "• ${request.targetRooms}" else ""}",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            if (request.details.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات: ${request.details}",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons: Call, WhatsApp, Archive, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direct Phone Call
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${request.customerPhone}"))
                        context.startActivity(dialIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اتصال", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                // WhatsApp Button
                Button(
                    onClick = {
                        val cleanPhone = request.customerPhone.replace("+", "").replace(" ", "").trim()
                        val waUrl = "https://wa.me/2$cleanPhone"
                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(waUrl))
                        context.startActivity(waIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("واتساب", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }

                IconButton(onClick = onArchive, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Archive, contentDescription = "أرشفة", tint = TextMuted, modifier = Modifier.size(18.dp))
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = BadgeForSaleRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ========================================================
// 4. تبويب الاستفسارات (INQUIRIES)
// ========================================================
@Composable
fun AdminInquiriesTab(
    inquiries: List<CustomerInquiryEntity>,
    onReply: (Long, String, String) -> Unit,
    onDelete: (Long) -> Unit
) {
    val context = LocalContext.current
    var replyingInquiry by remember { mutableStateOf<CustomerInquiryEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = "استفسارات المواطنين حول العقارات (${inquiries.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = DesoukNavyDark
        )

        if (inquiries.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد استفسارات حالياً", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(inquiries, key = { it.id }) { inq ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = inq.userName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DesoukNavyDark)
                                    Text(text = inq.propertyTitle, fontSize = 11.sp, color = DesoukGold, fontWeight = FontWeight.Bold)
                                }

                                val statusColor = if (inq.status == InquiryStatus.NEW) BadgeForSaleRed else Color(0xFF2E7D32)
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(statusColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(text = inq.status, color = statusColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = inq.inquiryText, fontSize = 12.sp, color = TextPrimary, lineHeight = 18.sp)

                            if (inq.replyNotes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFE8F5E9))
                                        .padding(8.dp)
                                ) {
                                    Text(text = "رد الإدارة: ${inq.replyNotes}", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { replyingInquiry = inq },
                                    colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("الرد والمتابعة", fontSize = 11.sp, color = DesoukNavyDark, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${inq.userPhone}"))
                                        context.startActivity(dialIntent)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("اتصال", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }

                                IconButton(onClick = { onDelete(inq.id) }, modifier = Modifier.size(36.dp)) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = BadgeForSaleRed, modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (replyingInquiry != null) {
        var replyText by remember { mutableStateOf(replyingInquiry!!.replyNotes) }
        AlertDialog(
            onDismissRequest = { replyingInquiry = null },
            title = { Text("الرد على استفسار ${replyingInquiry!!.userName}", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = "سؤال العميل: '${replyingInquiry!!.inquiryText}'", fontSize = 11.sp, color = TextMuted)
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = { Text("اكتب رد الإدارة وملاحظات التنسيق مع العميل...") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReply(replyingInquiry!!.id, InquiryStatus.REPLIED, replyText)
                        replyingInquiry = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark)
                ) {
                    Text("حفظ الرد", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { replyingInquiry = null }) {
                    Text("إلغاء", color = TextMuted)
                }
            }
        )
    }
}

// ========================================================
// 5. تبويب المستخدمين (USERS MANAGEMENT)
// ========================================================
@Composable
fun AdminUsersTab(
    users: List<AppUserEntity>,
    onToggleBlock: (Long, String, Boolean) -> Unit,
    onDeleteUser: (Long, String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = users.filter { u ->
        searchQuery.isBlank() ||
                u.name.contains(searchQuery, ignoreCase = true) ||
                u.phone.contains(searchQuery) ||
                u.email.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("بحث عن مستخدم بالاسم، الهاتف، أو البريد...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth().testTag("admin_user_search_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = DesoukGold,
                unfocusedBorderColor = Color(0xFFD0D7DE)
            ),
            singleLine = true
        )

        Text(text = "المستخدمون المسجلون (${filteredUsers.size})", fontSize = 12.sp, color = TextMuted)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredUsers, key = { it.id }) { u ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(if (u.role == UserRole.SUPER_ADMIN) DesoukGold else DesoukNavyDark),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = u.name.take(1),
                                        color = if (u.role == UserRole.SUPER_ADMIN) DesoukNavyDark else Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = u.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DesoukNavyDark)
                                        if (u.role == UserRole.SUPER_ADMIN) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(DesoukGold)
                                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                                            ) {
                                                Text(text = "Super Admin", color = DesoukNavyDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Text(text = "${u.phone} • ${u.district} - ${u.city}", fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (u.isBlocked) BadgeForSaleRed.copy(alpha = 0.12f) else Color(0xFFE8F5E9))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (u.isBlocked) "موقوف" else "نشط",
                                    color = if (u.isBlocked) BadgeForSaleRed else Color(0xFF2E7D32),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "العقارات المضافة: ${u.propertiesCount} | الطلبات: ${u.requestsCount}",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )

                        if (u.role != UserRole.SUPER_ADMIN) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onToggleBlock(u.id, u.name, u.isBlocked) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (u.isBlocked) Icons.Default.Check else Icons.Default.Block,
                                        contentDescription = null,
                                        tint = if (u.isBlocked) Color(0xFF2E7D32) else BadgeForSaleRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (u.isBlocked) "تفعيل الحساب" else "إيقاف الحساب",
                                        fontSize = 11.sp,
                                        color = if (u.isBlocked) Color(0xFF2E7D32) else BadgeForSaleRed,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { onDeleteUser(u.id, u.name) },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = BadgeForSaleRed, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("حذف المستخدم", fontSize = 11.sp, color = BadgeForSaleRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========================================================
// 6. تبويب سجل العمليات (ACTIVITY LOG)
// ========================================================
@Composable
fun AdminActivityLogTab(
    logs: List<AdminActivityLogEntity>
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.History, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "سجل النشاطات والعمليات الإدارية (Audit Trail)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = DesoukNavyDark
            )
        }
        Text(
            text = "تسجيل فوري لجميع العمليات التي تتم بواسطة Super Admin",
            fontSize = 11.sp,
            color = TextMuted
        )

        if (logs.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                Text(text = "لا توجد سجلات مسجلة بعد", color = TextMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(logs, key = { it.id }) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DesoukNavyDark.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = DesoukNavyDark,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = log.actionType,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = DesoukNavyDark
                                    )
                                    val dateStr = SimpleDateFormat("HH:mm - yyyy/MM/dd", Locale.getDefault()).format(Date(log.timestamp))
                                    Text(text = dateStr, fontSize = 10.sp, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = log.details,
                                    fontSize = 11.sp,
                                    color = TextPrimary,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ========================================================
// 7. تبويب إعدادات التطبيق والرئيسية
// ========================================================
@Composable
fun AdminSettingsTab(
    settings: List<AppSettingEntity>,
    onSaveSetting: (String, String) -> Unit
) {
    var bannerText by remember(settings) {
        mutableStateOf(settings.firstOrNull { it.key == "announcement_banner" }?.value ?: "أهلاً بكم في منصة عقارات دسوق Ai - المنصة العقارية الرسمية المعتمدة لمدينة دسوق وقراها")
    }
    var hotline by remember(settings) {
        mutableStateOf(settings.firstOrNull { it.key == "hotline" }?.value ?: "01001234567")
    }
    var whatsapp by remember(settings) {
        mutableStateOf(settings.firstOrNull { it.key == "whatsapp" }?.value ?: "201001234567")
    }
    var officeAddress by remember(settings) {
        mutableStateOf(settings.firstOrNull { it.key == "office_address" }?.value ?: "دسوق - شارع الجيش أمام الميدان الإبراهيمي، برج الصفا الإداري الدور الثاني")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "إعدادات المنصة والصفحة الرئيسية",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = DesoukNavyDark
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "نص البنر الإعلاني الرئيسي في الصفحة الرئيسية:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                OutlinedTextField(
                    value = bannerText,
                    onValueChange = { bannerText = it },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(10.dp)
                )
                Button(
                    onClick = { onSaveSetting("announcement_banner", bannerText) },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حفظ نص البنر", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "بيانات التواصل الرسمية للمكتب في دسوق:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)

                OutlinedTextField(
                    value = hotline,
                    onValueChange = { hotline = it },
                    label = { Text("رقم الخط الساخن / الهاتف الموحد") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = whatsapp,
                    onValueChange = { whatsapp = it },
                    label = { Text("رقم الواتساب الرسمي المعتمد") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = officeAddress,
                    onValueChange = { officeAddress = it },
                    label = { Text("عنوان المقر الرسمي في دسوق") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Button(
                    onClick = {
                        onSaveSetting("hotline", hotline)
                        onSaveSetting("whatsapp", whatsapp)
                        onSaveSetting("office_address", officeAddress)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حفظ بيانات التواصل الرسمية", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
            }
        }
    }
}

// ========================================================
// نافذة إضافة / تعديل عقار للأدمن (Admin Property Form Dialog)
// ========================================================
@Composable
fun AdminPropertyFormDialog(
    existing: PropertyEntity? = null,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        title: String,
        category: String,
        type: String,
        location: String,
        district: String,
        street: String,
        price: Double,
        priceUnit: String,
        rooms: Int,
        bathrooms: Int,
        area: Double,
        floor: Int,
        totalFloors: Int,
        finishing: String,
        hasElevator: Boolean,
        hasGarage: Boolean,
        hasMeters: Boolean,
        features: String,
        phone: String,
        whatsapp: String,
        videoUrl: String,
        imageResName: String,
        description: String,
        status: String,
        latitude: Double,
        longitude: Double
    ) -> Unit
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var selectedCategory by remember { mutableStateOf(existing?.category ?: "شقق للبيع") }
    var selectedType by remember { mutableStateOf(existing?.type ?: "للبيع") }
    var priceText by remember { mutableStateOf(existing?.price?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var priceUnit by remember { mutableStateOf(existing?.priceUnit ?: "ج.م") }
    var areaText by remember { mutableStateOf(existing?.area?.let { if (it > 0) it.toInt().toString() else "" } ?: "") }
    var roomsText by remember { mutableStateOf(existing?.rooms?.toString() ?: "3") }
    var bathroomsText by remember { mutableStateOf(existing?.bathrooms?.toString() ?: "1") }
    var floorText by remember { mutableStateOf(existing?.floor?.toString() ?: "3") }
    var totalFloorsText by remember { mutableStateOf(existing?.totalFloors?.toString() ?: "6") }
    var selectedDistrict by remember { mutableStateOf(existing?.district ?: "الميدان الإبراهيمي") }
    var street by remember { mutableStateOf(existing?.street ?: "") }
    var finishing by remember { mutableStateOf(existing?.finishing ?: "سوبر لوكس") }
    var hasElevator by remember { mutableStateOf(existing?.hasElevator ?: true) }
    var hasGarage by remember { mutableStateOf(existing?.hasGarage ?: false) }
    var hasMeters by remember { mutableStateOf(existing?.hasMeters ?: true) }
    var phone by remember { mutableStateOf(existing?.phone ?: "01001234567") }
    var whatsapp by remember { mutableStateOf(existing?.whatsapp ?: "201001234567") }
    var videoUrl by remember { mutableStateOf(existing?.videoUrl ?: "") }
    var imageResName by remember { mutableStateOf(existing?.imageResName ?: "img_property_apartment") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var status by remember { mutableStateOf(existing?.status ?: PropertyStatus.PUBLISHED) }

    val categoriesList = listOf(
        "شقق للبيع",
        "شقق للإيجار",
        "فلل",
        "منازل",
        "أراضي",
        "أراضي زراعية",
        "محلات",
        "وحدات إدارية للبيع",
        "وحدات إدارية للإيجار",
        "مكاتب",
        "عمارات",
        "عقارات أخرى"
    )

    val districtsList = listOf(
        "الميدان الإبراهيمي",
        "كورنيش النيل",
        "شارع الشركات",
        "شارع الجيش",
        "طريق فوه",
        "المحرقة",
        "حي دحروج",
        "المناطق المحيطة"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (existing == null) "إضافة عقار جديد (صلاحيات الأدمن)" else "تعديل العقار: ${existing.title}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = DesoukNavyDark
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("اسم/عنوان العقار *") },
                    placeholder = { Text("مثال: شقة للبيع على النيل بكورنيش دسوق") },
                    modifier = Modifier.fillMaxWidth().testTag("admin_prop_title_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Type: Sale / Rent
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("للبيع", "للإيجار").forEach { t ->
                        val isSel = selectedType == t
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) DesoukNavyDark else Color(0xFFF0F0F0))
                                .clickable {
                                    selectedType = t
                                    priceUnit = if (t == "للإيجار") "ج.م / شهرياً" else "ج.م"
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = t, color = if (isSel) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                // Category Chips
                Text(text = "نوع العقار:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    categoriesList.forEach { cat ->
                        val isSel = selectedCategory == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) DesoukGold else Color(0xFFF5F5F5))
                                .clickable { selectedCategory = cat }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = cat, color = if (isSel) DesoukNavyDark else TextPrimary, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                // District
                Text(text = "المنطقة / الحي في دسوق:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    districtsList.forEach { dist ->
                        val isSel = selectedDistrict == dist
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) DesoukNavyDark else Color(0xFFF5F5F5))
                                .clickable { selectedDistrict = dist }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(text = dist, color = if (isSel) Color.White else TextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("العنوان بالتفصيل / اسم الشارع") },
                    placeholder = { Text("مثال: شارع سعد زغلول متفرع من الميدان الإبراهيمي") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Price and Area
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("السعر *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f).testTag("admin_prop_price_input"),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        label = { Text("المساحة م² *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Rooms, Bathrooms, Floor
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = roomsText,
                        onValueChange = { roomsText = it },
                        label = { Text("الغرف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = bathroomsText,
                        onValueChange = { bathroomsText = it },
                        label = { Text("الحمامات") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = floorText,
                        onValueChange = { floorText = it },
                        label = { Text("الدور") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Finishing, Amenities Checkboxes
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasElevator, onCheckedChange = { hasElevator = it })
                        Text("أسانسير", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasGarage, onCheckedChange = { hasGarage = it })
                        Text("جراج", fontSize = 11.sp)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = hasMeters, onCheckedChange = { hasMeters = it })
                        Text("عدادات كاملة", fontSize = 11.sp)
                    }
                }

                // Phone & WhatsApp
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف للتواصل") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    OutlinedTextField(
                        value = whatsapp,
                        onValueChange = { whatsapp = it },
                        label = { Text("WhatsApp") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // Video URL
                OutlinedTextField(
                    value = videoUrl,
                    onValueChange = { videoUrl = it },
                    label = { Text("رابط فيديو العقار (YouTube أو جولة افتراضية)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("وصف العقار والمميزات") },
                    placeholder = { Text("اكتب تفاصيل العقار، الواجهة، التسجيل بالشهر العقاري، المميزات...") },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = priceText.toDoubleOrNull() ?: 0.0
                    val a = areaText.toDoubleOrNull() ?: 0.0
                    val r = roomsText.toIntOrNull() ?: 0
                    val b = bathroomsText.toIntOrNull() ?: 0
                    val fl = floorText.toIntOrNull() ?: 1
                    val totFl = totalFloorsText.toIntOrNull() ?: 5
                    if (title.isNotBlank()) {
                        onSave(
                            existing?.id ?: 0L,
                            title,
                            selectedCategory,
                            selectedType,
                            "دسوق - $selectedDistrict",
                            selectedDistrict,
                            street,
                            p,
                            priceUnit,
                            r,
                            b,
                            a,
                            fl,
                            totFl,
                            finishing,
                            hasElevator,
                            hasGarage,
                            hasMeters,
                            "موقع مميز، عدادات، حيز عمراني",
                            phone,
                            whatsapp,
                            videoUrl,
                            imageResName,
                            description,
                            status,
                            31.1306,
                            30.6482
                        )
                    }
                },
                enabled = title.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                modifier = Modifier.testTag("admin_save_property_button")
            ) {
                Text("حفظ العقار ونشره", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}

// ========================================================
// نافذة إضافة طلب عميل جديد من المكتب
// ========================================================
@Composable
fun AdminAddRequestDialog(
    onDismiss: () -> Unit,
    onSubmit: (
        name: String,
        phone: String,
        type: String,
        category: String,
        district: String,
        budget: String,
        area: String,
        rooms: String,
        details: String
    ) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var type by remember { mutableStateOf(RequestType.PURCHASE) }
    var category by remember { mutableStateOf("شقق") }
    var district by remember { mutableStateOf("الميدان الإبراهيمي") }
    var budget by remember { mutableStateOf("") }
    var area by remember { mutableStateOf("") }
    var rooms by remember { mutableStateOf("3 غرف") }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسجيل طلب عميل جديد", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DesoukNavyDark) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("اسم العميل *") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("رقم الهاتف *") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(RequestType.PURCHASE, RequestType.RENT).forEach { t ->
                        val isSel = type == t
                        Box(
                            modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(if (isSel) DesoukNavyDark else Color(0xFFF0F0F0)).clickable { type = t }.padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = t, color = if (isSel) Color.White else TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(value = budget, onValueChange = { budget = it }, label = { Text("الميزانية التقريبية") }, placeholder = { Text("مثال: 1,200,000 ج.م") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = district, onValueChange = { district = it }, label = { Text("المنطقة / الحي المطلوب") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = area, onValueChange = { area = it }, label = { Text("المساحة المطلوبة") }, placeholder = { Text("مثال: 150 م²") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp))
                OutlinedTextField(value = details, onValueChange = { details = it }, label = { Text("تفاصيل وملاحظات الطلب") }, modifier = Modifier.fillMaxWidth().height(80.dp), shape = RoundedCornerShape(10.dp))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && phone.isNotBlank()) {
                        onSubmit(name, phone, type, category, district, budget, area, rooms, details)
                    }
                },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark)
            ) {
                Text("تسجيل الطلب", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        }
    )
}
