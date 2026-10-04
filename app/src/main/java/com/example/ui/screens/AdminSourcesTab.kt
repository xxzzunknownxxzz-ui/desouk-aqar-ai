package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.ImportLogEntity
import com.example.data.ImportedAdStatus
import com.example.data.ImportedPropertyEntity
import com.example.data.PropertySourceEntity
import com.example.data.SourceType
import com.example.data.SourcesDashboardStats
import com.example.data.ai.AiExtractionResult
import com.example.data.ai.RealEstateAiExtractor
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

/**
 * شاشة وتبويب "مصادر العقارات" الإدارية
 * تشمل إدارة المصادر، إحصائيات Firestore الثمانية، قائمة المراجعة، الفحص الذكي للتكرار، واستيراد المنشورات
 */
@Composable
fun AdminSourcesTab(
    sources: List<PropertySourceEntity>,
    importedProperties: List<ImportedPropertyEntity>,
    reviewQueue: List<ImportedPropertyEntity>,
    importLogs: List<ImportLogEntity>,
    stats: SourcesDashboardStats,
    onAddSource: (name: String, type: String, url: String, notes: String) -> Unit,
    onToggleSourceActive: (Long, Boolean) -> Unit,
    onDeleteSource: (Long) -> Unit,
    onSyncSource: (PropertySourceEntity) -> Unit,
    onImportRawPost: (rawText: String, sourceName: String, postUrl: String, postExternalId: String, onSuccess: () -> Unit) -> Unit,
    onApproveAndPublish: (ImportedPropertyEntity, adminNotes: String) -> Unit,
    onReject: (ImportedPropertyEntity, reason: String) -> Unit,
    onRequestReview: (Long, notes: String) -> Unit,
    onUpdateAndPublish: (ImportedPropertyEntity) -> Unit,
    onDeleteImported: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSubSection by remember { mutableIntStateOf(0) } // 0: الإحصائيات والمصادر, 1: قائمة المراجعة, 2: أداة الاستيراد بالذكاء الاصطناعي, 3: سجل العمليات
    var showAddSourceDialog by remember { mutableStateOf(false) }
    var editingImportedItem by remember { mutableStateOf<ImportedPropertyEntity?>(null) }
    var syncFeedbackMessage by remember { mutableStateOf<String?>(null) }

    // فلاتر البحث لقائمة المراجعة والإعلانات المستوردة
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf("الكل") }
    var selectedOpFilter by remember { mutableStateOf("الكل") }
    var selectedDistrictFilter by remember { mutableStateOf("الكل") }
    var selectedStatusFilter by remember { mutableStateOf("الكل") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // بطاقة تنبيه الأمان والامتثال لسياسات Meta والخصوصية
        ComplianceBanner()

        // 8 بطاقات إحصائيات مستندة إلى مجموعات Firestore
        FirestoreStatsGrid(stats = stats)

        // شريط التنقل الفرعي داخل مصادر العقارات
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val sections = listOf(
                "المصادر المعتمدة (${sources.size})" to Icons.Default.Share,
                "قائمة المراجعة (${reviewQueue.size})" to Icons.Default.ListAlt,
                "استيراد منشور (AI)" to Icons.Default.AutoAwesome,
                "سجل الاستيراد (${importLogs.size})" to Icons.Default.History
            )
            sections.forEachIndexed { index, (label, icon) ->
                val isSelected = activeSubSection == index
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DesoukNavyDark else Color.White)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) DesoukGold else Color(0xFFE2E8F0),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { activeSubSection = index }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("admin_sources_subsec_$index"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isSelected) DesoukGold else TextMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }

        // عرض رسالة نجاح المزامنة إن وجدت
        syncFeedbackMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = msg, color = Color(0xFF1B5E20), fontSize = 12.sp, modifier = Modifier.weight(1f))
                    IconButton(onClick = { syncFeedbackMessage = null }, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(14.dp))
                    }
                }
            }
        }

        when (activeSubSection) {
            0 -> {
                // قسم المصادر المعتمدة
                SourcesManagementSection(
                    sources = sources,
                    onAddNewSource = { showAddSourceDialog = true },
                    onToggleActive = onToggleSourceActive,
                    onDeleteSource = onDeleteSource,
                    onSyncSource = { source ->
                        onSyncSource(source)
                        syncFeedbackMessage = "تمت مزامنة المصدر '${source.name}' بنجاح، وتمت إضافة إعلانات جديدة للمراجعة."
                    }
                )
            }
            1 -> {
                // قسم الإعلانات قيد المراجعة مع البحث والفلترة
                ReviewQueueSection(
                    items = importedProperties,
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    selectedType = selectedTypeFilter,
                    onTypeSelect = { selectedTypeFilter = it },
                    selectedOp = selectedOpFilter,
                    onOpSelect = { selectedOpFilter = it },
                    selectedDistrict = selectedDistrictFilter,
                    onDistrictSelect = { selectedDistrictFilter = it },
                    selectedStatus = selectedStatusFilter,
                    onStatusSelect = { selectedStatusFilter = it },
                    onApproveAndPublish = onApproveAndPublish,
                    onReject = onReject,
                    onRequestReview = onRequestReview,
                    onEditAndPublish = { item -> editingImportedItem = item },
                    onDelete = onDeleteImported
                )
            }
            2 -> {
                // قسم استيراد منشور يدوي / رسمي وتحليله بالـ AI
                ManualPostIngestionSection(
                    sources = sources,
                    onImport = { rawText, sourceName, postUrl, postExternalId ->
                        onImportRawPost(rawText, sourceName, postUrl, postExternalId) {
                            syncFeedbackMessage = "تم استيراد المنشور وتحليله بالذكاء الاصطناعي وإضافته لقائمة المراجعة بنجاح!"
                            activeSubSection = 1
                        }
                    }
                )
            }
            3 -> {
                // قسم سجل العمليات والاستيراد
                ImportLogsSection(logs = importLogs)
            }
        }
    }

    // Modal: إضافة مصدر عقاري جديد
    if (showAddSourceDialog) {
        AddSourceDialog(
            onDismiss = { showAddSourceDialog = false },
            onSubmit = { name, type, url, notes ->
                onAddSource(name, type, url, notes)
                showAddSourceDialog = false
                syncFeedbackMessage = "تمت إضافة المصدر '$name' بنجاح وهو متاح للمزامنة الآن."
            }
        )
    }

    // Modal: تعديل بيانات الإعلان المستورد قبل نشره
    if (editingImportedItem != null) {
        EditImportedPropertyDialog(
            property = editingImportedItem!!,
            onDismiss = { editingImportedItem = null },
            onSaveAndPublish = { updated ->
                onUpdateAndPublish(updated)
                editingImportedItem = null
                syncFeedbackMessage = "تم تحديث الإعلان واعتماده ونشره في التطبيق بنجاح."
            }
        )
    }
}

/**
 * بطاقة التنبيه بالامتثال لسياسات Meta والأمان
 */
@Composable
fun ComplianceBanner() {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F4F8)),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(DesoukNavyDark.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "نظام استيراد مصرح به وآمن 100%",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                Text(
                    text = "متوافق مع سياسات Meta: لا يطلب كلمة مرور أو توكن حساب شخصي، لا يقوم بأي scraping غير مصرح به، ويخضع لاعتماد Super Admin قبل النشر.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * البطاقات الإحصائية الثمانية المطلوبة بالكامل المستندة إلى Firestore
 */
@Composable
fun FirestoreStatsGrid(stats: SourcesDashboardStats) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "إحصائيات مصادر العقارات (Firestore Live)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = "محدث لحظياً", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }

            // الصف الأول (4 بطاقات)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniCard(
                    title = "المصادر النشطة",
                    value = "${stats.activeSourcesCount}",
                    color = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "المنشورات المستوردة",
                    value = "${stats.totalImportedPostsCount}",
                    color = DesoukNavyDark,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "إعلانات جديدة",
                    value = "${stats.newAdsCount}",
                    color = DesoukGold,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "قيد المراجعة",
                    value = "${stats.inReviewCount}",
                    color = Color(0xFFE65100),
                    modifier = Modifier.weight(1f)
                )
            }

            // الصف الثاني (4 بطاقات)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatMiniCard(
                    title = "الإعلانات المنشورة",
                    value = "${stats.publishedCount}",
                    color = Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "الإعلانات المكررة",
                    value = "${stats.duplicateCount}",
                    color = Color(0xFF8E24AA),
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "إعلانات مرفوضة",
                    value = "${stats.rejectedCount}",
                    color = BadgeForSaleRed,
                    modifier = Modifier.weight(1f)
                )
                StatMiniCard(
                    title = "إعلانات مؤرشفة",
                    value = "${stats.archivedCount}",
                    color = Color(0xFF546E7A),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun StatMiniCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.08f))
            .border(1.dp, color.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 9.5.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 1. قسم إدارة مصادر العقارات
 */
@Composable
fun SourcesManagementSection(
    sources: List<PropertySourceEntity>,
    onAddNewSource: () -> Unit,
    onToggleActive: (Long, Boolean) -> Unit,
    onDeleteSource: (Long) -> Unit,
    onSyncSource: (PropertySourceEntity) -> Unit
) {
    val context = LocalContext.current
    val timeFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")) }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "مصادر العقارات المعتمدة (${sources.size})",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                Text(
                    text = "إدارة المصادر المسموح بها رسميًا ومتابعة إحصائياتها",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
            Button(
                onClick = onAddNewSource,
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("admin_add_source_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("إضافة مصدر", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        if (sources.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "لا توجد مصادر مضافة حتى الآن", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            sources.forEach { source ->
                SourceItemCard(
                    source = source,
                    timeFormat = timeFormat,
                    onToggleActive = { onToggleActive(source.id, it) },
                    onDelete = { onDeleteSource(source.id) },
                    onSync = { onSyncSource(source) },
                    onOpenUrl = {
                        if (source.url.isNotBlank()) {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SourceItemCard(
    source: PropertySourceEntity,
    timeFormat: SimpleDateFormat,
    onToggleActive: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onSync: () -> Unit,
    onOpenUrl: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        modifier = Modifier.fillMaxWidth().testTag("source_card_${source.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = source.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(text = source.type, fontSize = 10.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (source.isActive) "نشط" else "معطل",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (source.isActive) Color(0xFF2E7D32) else TextMuted
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = source.isActive,
                        onCheckedChange = onToggleActive,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2E7D32)
                        ),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            if (source.url.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenUrl() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = source.url,
                        fontSize = 11.sp,
                        color = Color(0xFF1976D2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(13.dp))
                }
            }

            // إحصائيات هذا المصدر
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${source.totalImportedPosts}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    Text(text = "إجمالي المنشورات", fontSize = 9.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${source.newAdsCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DesoukGold)
                    Text(text = "جديد", fontSize = 9.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${source.duplicateAdsCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF8E24AA))
                    Text(text = "مكرر", fontSize = 9.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "${source.needsReviewCount}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                    Text(text = "يحتاج مراجعة", fontSize = 9.sp, color = TextMuted)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "آخر مزامنة: ${timeFormat.format(Date(source.lastSyncTime))}",
                    fontSize = 10.sp,
                    color = TextMuted
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onSync,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE3F2FD)),
                        shape = RoundedCornerShape(8.dp),
                        enabled = source.isActive,
                        modifier = Modifier.height(32.dp).testTag("sync_source_btn_${source.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = Color(0xFF1565C0), modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("مزامنة الآن", color = Color(0xFF1565C0), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp).testTag("delete_source_btn_${source.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف المصدر", tint = BadgeForSaleRed, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * 2. قسم الإعلانات قيد المراجعة والبحث والفلترة
 */
@Composable
fun ReviewQueueSection(
    items: List<ImportedPropertyEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedType: String,
    onTypeSelect: (String) -> Unit,
    selectedOp: String,
    onOpSelect: (String) -> Unit,
    selectedDistrict: String,
    onDistrictSelect: (String) -> Unit,
    selectedStatus: String,
    onStatusSelect: (String) -> Unit,
    onApproveAndPublish: (ImportedPropertyEntity, adminNotes: String) -> Unit,
    onReject: (ImportedPropertyEntity, reason: String) -> Unit,
    onRequestReview: (Long, notes: String) -> Unit,
    onEditAndPublish: (ImportedPropertyEntity) -> Unit,
    onDelete: (Long) -> Unit
) {
    // تصفية الإعلانات
    val filteredItems = items.filter { item ->
        val matchSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.district.contains(searchQuery, ignoreCase = true) ||
                item.contactPhone.contains(searchQuery) ||
                item.originalRawText.contains(searchQuery, ignoreCase = true)

        val matchType = selectedType == "الكل" || item.propertyType == selectedType
        val matchOp = selectedOp == "الكل" || item.operationType == selectedOp
        val matchDistrict = selectedDistrict == "الكل" || item.district == selectedDistrict
        val matchStatus = selectedStatus == "الكل" || item.status == selectedStatus

        matchSearch && matchType && matchOp && matchDistrict && matchStatus
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // شريط البحث
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = { Text("بحث بالمصدر، الهاتف، المنطقة، أو النص الأصلي...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = DesoukNavyDark) },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = TextMuted)
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = DesoukGold,
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.fillMaxWidth().testTag("review_search_input"),
            singleLine = true
        )

        // شريط الفلاتر السريعة
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("الكل", ImportedAdStatus.IN_REVIEW, ImportedAdStatus.ACTIVE, ImportedAdStatus.DUPLICATE, ImportedAdStatus.REJECTED).forEach { st ->
                FilterChip(
                    selected = selectedStatus == st,
                    onClick = { onStatusSelect(st) },
                    label = { Text(st, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DesoukNavyDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("الكل", "شقة", "محل", "عمارة", "منزل", "أرض").forEach { tp ->
                FilterChip(
                    selected = selectedType == tp,
                    onClick = { onTypeSelect(tp) },
                    label = { Text(tp, fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DesoukGold,
                        selectedLabelColor = DesoukNavyDark
                    )
                )
            }
            listOf("الكل", "للبيع", "للإيجار").forEach { op ->
                FilterChip(
                    selected = selectedOp == op,
                    onClick = { onOpSelect(op) },
                    label = { Text(op, fontSize = 11.sp) }
                )
            }
        }

        Text(
            text = "النتائج المعروضة: ${filteredItems.size} إعلان",
            fontSize = 11.sp,
            color = TextMuted
        )

        if (filteredItems.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(28.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "لا توجد إعلانات مطابقة لمعايير البحث المحددة", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            filteredItems.forEach { item ->
                ReviewPropertyCard(
                    item = item,
                    onApproveAndPublish = { notes -> onApproveAndPublish(item, notes) },
                    onReject = { reason -> onReject(item, reason) },
                    onRequestReview = { notes -> onRequestReview(item.id, notes) },
                    onEditAndPublish = { onEditAndPublish(item) },
                    onDelete = { onDelete(item.id) }
                )
            }
        }
    }
}

/**
 * بطاقة إعلان قيد المراجعة مع كافة البيانات المستخرجة والنص الأصلي الخام والأزرار الخمسة
 */
@Composable
fun ReviewPropertyCard(
    item: ImportedPropertyEntity,
    onApproveAndPublish: (notes: String) -> Unit,
    onReject: (reason: String) -> Unit,
    onRequestReview: (notes: String) -> Unit,
    onEditAndPublish: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var showRawText by remember { mutableStateOf(false) }
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectReason by remember { mutableStateOf("") }
    var showReviewNotesDialog by remember { mutableStateOf(false) }
    var reviewNotesText by remember { mutableStateOf("") }
    var showApproveConfirmDialog by remember { mutableStateOf(false) }

    val formatter = remember { NumberFormat.getNumberInstance(Locale("ar")) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth().testTag("review_card_${item.id}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Title, Category Badges & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "المصدر: ${item.sourceName}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (item.status) {
                                ImportedAdStatus.IN_REVIEW -> Color(0xFFFFF3E0)
                                ImportedAdStatus.ACTIVE -> Color(0xFFE8F5E9)
                                ImportedAdStatus.DUPLICATE -> Color(0xFFF3E5F5)
                                ImportedAdStatus.REJECTED -> Color(0xFFFFEBEE)
                                else -> Color(0xFFF1F5F9)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.status,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (item.status) {
                            ImportedAdStatus.IN_REVIEW -> Color(0xFFE65100)
                            ImportedAdStatus.ACTIVE -> Color(0xFF2E7D32)
                            ImportedAdStatus.DUPLICATE -> Color(0xFF8E24AA)
                            ImportedAdStatus.REJECTED -> BadgeForSaleRed
                            else -> TextMuted
                        }
                    )
                }
            }

            // Badges: Operation & Price
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (item.operationType == "للبيع") BadgeForSaleRed.copy(alpha = 0.15f) else BadgeForRentTeal.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.operationType,
                            color = if (item.operationType == "للبيع") BadgeForSaleRed else BadgeForRentTeal,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(text = item.district, color = Color(0xFF0284C7), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Price display with strict non-hallucination notice
                if (item.price != null && item.price > 0) {
                    Text(
                        text = "${formatter.format(item.price)} ${item.priceUnit}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "السعر غير مذكور في المنشور",
                            fontSize = 10.sp,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Extracted specs grid (مساحة، غرف، دور، إلخ)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (item.area != null && item.area > 0) "${item.area.toInt()} م²" else "غير محدد", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    Text(text = "المساحة", fontSize = 9.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (item.rooms != null && item.rooms > 0) "${item.rooms}" else "-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    Text(text = "الغرف", fontSize = 9.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (item.floor != null) "دور ${item.floor}" else "-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    Text(text = "الطابق", fontSize = 9.sp, color = TextMuted)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (item.hasElevator == true) "يوجد" else if (item.hasElevator == false) "لا يوجد" else "-", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    Text(text = "أسانسير", fontSize = 9.sp, color = TextMuted)
                }
            }

            // درجة اكتمال البيانات
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "درجة اكتمال البيانات المستخرجة:", fontSize = 11.sp, color = TextMuted)
                    Text(text = "${item.completenessScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (item.completenessScore >= 80) Color(0xFF2E7D32) else Color(0xFFE65100))
                }
                Spacer(modifier = Modifier.height(3.dp))
                LinearProgressIndicator(
                    progress = { item.completenessScore / 100f },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = if (item.completenessScore >= 80) Color(0xFF2E7D32) else Color(0xFFE65100),
                    trackColor = Color(0xFFE2E8F0)
                )
            }

            // تنبيه البيانات غير المؤكدة
            if (item.uncertainData.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "بيانات تحتاج تأكيد: ${item.uncertainData}", fontSize = 10.5.sp, color = Color(0xFF92400E))
                    }
                }
            }

            // فحص التكرار (Duplicate Alert)
            if (item.isDuplicate) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFAF5FF))
                        .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFF7E22CE), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "تنبيه تكرار: ${item.duplicateMatchReason}", fontSize = 10.5.sp, color = Color(0xFF6B21A8), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // بيانات التواصل المستخرجة
            if (item.contactPhone.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "رقم التواصل: ${item.contactPhone}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    if (item.contactName.isNotBlank()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "(${item.contactName})", fontSize = 11.sp, color = TextMuted)
                    }
                }
            }

            // النص الأصلي الخام (حقل منفصل غير معدل)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showRawText = !showRawText },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "النص الأصلي الخام للمنشور", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    Text(text = if (showRawText) "إخفاء" else "عرض", fontSize = 10.5.sp, color = Color(0xFF1E40AF), fontWeight = FontWeight.Bold)
                }

                AnimatedVisibility(visible = showRawText) {
                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        Text(
                            text = item.originalRawText,
                            fontSize = 11.sp,
                            color = TextPrimary,
                            lineHeight = 16.sp
                        )
                        if (item.postUrl.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "رابط المصدر: ${item.postUrl}",
                                fontSize = 10.sp,
                                color = Color(0xFF2563EB),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.clickable {
                                    try {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(item.postUrl)))
                                    } catch (_: Exception) {}
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))

            // الأزرار الخمسة المطلوبة لإدارة الإعلان:
            // "اعتماد ونشر", "تعديل ثم نشر", "طلب مراجعة", "رفض", "حذف"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // اعتماد ونشر
                Button(
                    onClick = { showApproveConfirmDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("approve_ad_btn_${item.id}")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("اعتماد ونشر", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // تعديل ثم نشر
                Button(
                    onClick = onEditAndPublish,
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("edit_publish_ad_btn_${item.id}")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("تعديل ثم نشر", color = DesoukNavyDark, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // طلب مراجعة
                OutlinedButton(
                    onClick = { showReviewNotesDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("request_review_ad_btn_${item.id}")
                ) {
                    Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFE65100), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طلب مراجعة", color = Color(0xFFE65100), fontSize = 11.sp)
                }

                // رفض
                OutlinedButton(
                    onClick = { showRejectDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("reject_ad_btn_${item.id}")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = BadgeForSaleRed, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("رفض", color = BadgeForSaleRed, fontSize = 11.sp)
                }

                // حذف
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).testTag("delete_imported_ad_btn_${item.id}")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }

    // Modal تأكيد الاعتماد والنشر
    if (showApproveConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showApproveConfirmDialog = false },
            title = { Text("اعتماد ونشر الإعلان", fontWeight = FontWeight.Bold) },
            text = {
                Text("هل ترغب في اعتماد هذا العقار ونشره فوراً في قائمة عقارات دسوق ليصبح متاحاً للجمهور؟")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApproveAndPublish("تم الاعتماد والنشر بعد المراجعة")
                        showApproveConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("نعم، اعتماد ونشر", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showApproveConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal رفض الإعلان
    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            title = { Text("رفض الإعلان المستورد", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("يرجى كتابة سبب رفض هذا الإعلان:")
                    OutlinedTextField(
                        value = rejectReason,
                        onValueChange = { rejectReason = it },
                        placeholder = { Text("مثال: بيانات وهمية / خارج نطاق دسوق / مكرر...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReject(rejectReason.ifBlank { "بيانات غير مكتملة أو غير مطابقة" })
                        showRejectDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BadgeForSaleRed)
                ) {
                    Text("تأكيد الرفض", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Modal طلب مراجعة
    if (showReviewNotesDialog) {
        AlertDialog(
            onDismissRequest = { showReviewNotesDialog = false },
            title = { Text("طلب إعادة تدقيق ومراجعة", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أضف ملاحظات المراجعة لفريق العمل:")
                    OutlinedTextField(
                        value = reviewNotesText,
                        onValueChange = { reviewNotesText = it },
                        placeholder = { Text("مثال: يرجى الاتصال بالمالك للتأكد من السعر النهائي والدور...") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestReview(reviewNotesText.ifBlank { "يحتاج تدقيق إضافي" })
                        showReviewNotesDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                ) {
                    Text("حفظ الملاحظة", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewNotesDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * 3. أداة الاستيراد المباشر وتحليل النص بالذكاء الاصطناعي
 */
@Composable
fun ManualPostIngestionSection(
    sources: List<PropertySourceEntity>,
    onImport: (rawText: String, sourceName: String, postUrl: String, postExternalId: String) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    var selectedSource by remember { mutableStateOf(sources.firstOrNull()?.name ?: "إدخال يدوي مصرح به") }
    var postUrl by remember { mutableStateOf("") }
    var postExternalId by remember { mutableStateOf("") }

    // Live AI preview
    var previewResult by remember { mutableStateOf<AiExtractionResult?>(null) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "استيراد منشور وتحليله بمحرك الذكاء الاصطناعي",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
            }
            Text(
                text = "يتم استقبال النص الخام، استخراج العقار، تصنيف الموقع بدسوق، تنظيف الشوائب والهاشتاجات، وفحص التكرار تلقائياً دون اختراع أي معلومة.",
                fontSize = 11.sp,
                color = TextMuted,
                lineHeight = 15.sp
            )

            // اختيار المصدر
            Text(text = "اسم المصدر المعتمد:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val availableSources = listOf("إدخال يدوي مصرح به") + sources.map { it.name }
                availableSources.forEach { src ->
                    FilterChip(
                        selected = selectedSource == src,
                        onClick = { selectedSource = src },
                        label = { Text(src, fontSize = 11.sp) }
                    )
                }
            }

            // رابط المنشور أو المعرف (إن وجد)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = postUrl,
                    onValueChange = { postUrl = it },
                    label = { Text("رابط المنشور (اختياري)", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = postExternalId,
                    onValueChange = { postExternalId = it },
                    label = { Text("معرف المنشور ID", fontSize = 11.sp) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(0.7f),
                    singleLine = true
                )
            }

            // النص الخام الكامل
            Text(text = "النص الأصلي الكامل للمنشور (Raw Text):", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
            OutlinedTextField(
                value = rawText,
                onValueChange = {
                    rawText = it
                    if (it.length > 20) {
                        previewResult = RealEstateAiExtractor.extractFromRawText(it, selectedSource, postUrl, postExternalId)
                    } else {
                        previewResult = null
                    }
                },
                placeholder = { Text("الصق هنا نص المنشور العقاري بالكامل كما هو منشور على الصفحة أو المصدر المعتمد...", fontSize = 12.sp) },
                shape = RoundedCornerShape(10.dp),
                minLines = 4,
                maxLines = 8,
                modifier = Modifier.fillMaxWidth().testTag("raw_post_input_field")
            )

            // المعاينة الحية لنتائج التحليل
            previewResult?.let { res ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "نتائج التحليل الأولي: ${res.title}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                        }
                        Text(text = "• النوع والعملية: ${res.propertyType} (${res.operationType})", fontSize = 11.sp, color = TextPrimary)
                        Text(text = "• الموقع: ${res.district} - ${res.street.ifBlank { "شارع رئيسي" }}", fontSize = 11.sp, color = TextPrimary)
                        Text(text = "• السعر: ${if (res.price != null) "${res.price} ${res.priceUnit}" else "غير مذكور (تم تركه فارغاً)"}", fontSize = 11.sp, color = if (res.price != null) Color(0xFF2E7D32) else Color(0xFFE65100))
                        Text(text = "• المساحة: ${if (res.area != null) "${res.area} م²" else "غير مذكورة"}", fontSize = 11.sp, color = TextPrimary)
                        Text(text = "• الهاتف: ${res.contactPhone.ifBlank { "غير متوفر" }}", fontSize = 11.sp, color = TextPrimary)
                        Text(text = "• درجة الاكتمال: ${res.completenessScore}%", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (res.completenessScore >= 80) Color(0xFF2E7D32) else Color(0xFFE65100))
                        if (res.isDuplicate) {
                            Text(text = "⚠️ تنبيه تكرار محتمل: ${res.duplicateReason}", fontSize = 11.sp, color = Color(0xFF7E22CE), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // زر الحفظ والإرسال لقائمة المراجعة
            Button(
                onClick = {
                    if (rawText.isNotBlank()) {
                        onImport(rawText, selectedSource, postUrl, postExternalId)
                        rawText = ""
                        postUrl = ""
                        postExternalId = ""
                        previewResult = null
                    }
                },
                enabled = rawText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth().testTag("submit_raw_post_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("معالجة وحفظ في قائمة المراجعة", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

/**
 * 4. قسم سجل العمليات والمزامنة
 */
@Composable
fun ImportLogsSection(logs: List<ImportLogEntity>) {
    val timeFormat = remember { SimpleDateFormat("yyyy/MM/dd - hh:mm:ss a", Locale("ar")) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "سجل عمليات الاستيراد والمزامنة السحابية (${logs.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = DesoukNavyDark
        )

        if (logs.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(text = "لا توجد سجلات مسجلة حتى الآن", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            logs.forEach { log ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = log.sourceName, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                            Text(text = timeFormat.format(Date(log.timestamp)), fontSize = 10.sp, color = TextMuted)
                        }
                        Text(text = "العملية: ${log.action}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF0369A1))
                        Text(text = log.details, fontSize = 11.sp, color = TextPrimary)
                        if (log.aiResult.isNotBlank()) {
                            Text(text = "نتيجة AI: ${log.aiResult}", fontSize = 10.5.sp, color = Color(0xFF2E7D32))
                        }
                    }
                }
            }
        }
    }
}

/**
 * نافذة إضافة مصدر عقاري جديد
 */
@Composable
fun AddSourceDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, type: String, url: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SourceType.FB_OFFICIAL_PAGE) }
    var url by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val types = listOf(
        SourceType.FB_OFFICIAL_PAGE,
        SourceType.FB_AUTHORIZED_GROUP,
        SourceType.REAL_ESTATE_PORTAL,
        SourceType.OFFICIAL_IMPORT_FILE,
        SourceType.AUTHORIZED_MANUAL
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("إضافة مصدر عقاري جديد", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المصدر (مثال: الصفحة الرسمية لعقارات دسوق)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_source_name_field")
                )

                Text("نوع المصدر المصرح به:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                types.forEach { type ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selectedType == type) Color(0xFFE2E8F0) else Color.Transparent)
                            .clickable { selectedType = type }
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type,
                            fontSize = 12.sp,
                            fontWeight = if (selectedType == type) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedType == type) DesoukNavyDark else TextPrimary
                        )
                    }
                }

                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("رابط المصدر (إذا كان مسموحًا)") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("add_source_url_field")
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية أو شروط الاستخدام") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSubmit(name, selectedType, url, notes)
                    }
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                modifier = Modifier.testTag("confirm_add_source_button")
            ) {
                Text("حفظ المصدر", color = DesoukNavyDark, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}

/**
 * نافذة تعديل بيانات الإعلان المستورد قبل نشره
 */
@Composable
fun EditImportedPropertyDialog(
    property: ImportedPropertyEntity,
    onDismiss: () -> Unit,
    onSaveAndPublish: (ImportedPropertyEntity) -> Unit
) {
    var title by remember { mutableStateOf(property.title) }
    var priceText by remember { mutableStateOf(property.price?.toInt()?.toString() ?: "") }
    var areaText by remember { mutableStateOf(property.area?.toInt()?.toString() ?: "") }
    var district by remember { mutableStateOf(property.district) }
    var street by remember { mutableStateOf(property.street) }
    var roomsText by remember { mutableStateOf(property.rooms?.toString() ?: "3") }
    var phone by remember { mutableStateOf(property.contactPhone) }
    var finishing by remember { mutableStateOf(property.finishing) }
    var notes by remember { mutableStateOf(property.reviewNotes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("تعديل الإعلان ثم نشره", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("عنوان الإعلان") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = priceText,
                        onValueChange = { priceText = it },
                        label = { Text("السعر (ج.م)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = areaText,
                        onValueChange = { areaText = it },
                        label = { Text("المساحة (م²)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = district,
                        onValueChange = { district = it },
                        label = { Text("المنطقة / الحي") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = street,
                        onValueChange = { street = it },
                        label = { Text("الشارع") },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = roomsText,
                        onValueChange = { roomsText = it },
                        label = { Text("الغرف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(0.7f)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.3f)
                    )
                }
                OutlinedTextField(
                    value = finishing,
                    onValueChange = { finishing = it },
                    label = { Text("التشطيب") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = property.copy(
                        title = title.trim(),
                        price = priceText.toDoubleOrNull(),
                        area = areaText.toDoubleOrNull(),
                        district = district.trim(),
                        street = street.trim(),
                        rooms = roomsText.toIntOrNull(),
                        contactPhone = phone.trim(),
                        finishing = finishing.trim(),
                        reviewNotes = "تم التعديل بواسطة الأدمن قبل النشر"
                    )
                    onSaveAndPublish(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
            ) {
                Text("اعتماد ونشر التعديلات", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
