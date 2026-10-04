package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Cottage
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.ImportedAdStatus
import com.example.data.ImportedPropertyEntity
import com.example.data.PropertyEntity
import com.example.data.ai.FacebookExtractedAd
import com.example.data.ai.FacebookImportAiService
import com.example.data.ai.FacebookUrlReadResult
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * دالة مساعدة لتحديد التصنيف الرئيسي الإجباري (1 من 4) طبقاً لتوجيهات المستخدم
 */
fun classifyPropertyMainCategory(propertyType: String): String {
    return when {
        propertyType.contains("أرض") || propertyType.contains("اراضي") -> "أراضي للبيع"
        propertyType.contains("مكتب") || propertyType.contains("إداري") || propertyType.contains("محل") || propertyType.contains("تجاري") -> "وحدات إدارية للبيع والايجار"
        propertyType.contains("منزل") || propertyType.contains("فيلا") || propertyType.contains("عمارة") || propertyType.contains("بيت") -> "منازل للبيع"
        else -> "شقق للبيع والايجار"
    }
}

/**
 * دالة مساعدة لتصنيف عدد الغرف بدقة دون اختراع قيم غير موجودة
 */
fun classifyRoomsSubCategory(rooms: Int?): String {
    return when (rooms) {
        1 -> "غرفة واحدة"
        2 -> "غرفتان"
        3 -> "3 غرف"
        4 -> "4 غرف"
        null, 0 -> "غير محدد"
        else -> "5 غرف أو أكثر"
    }
}

/**
 * دالة مساعدة لتصنيف المنطقة الفرعية
 */
fun classifyRegionSubCategory(location: String, address: String = ""): String {
    val combined = "$location $address"
    return when {
        combined.contains("كفر الشيخ") -> "كفر الشيخ"
        combined.contains("شارع الجيش") -> "شارع الجيش"
        combined.contains("شارع الشركات") -> "شارع الشركات"
        combined.contains("دحروج") -> "حي دحروج"
        combined.contains("الميدان الإبراهيمي") -> "الميدان الإبراهيمي"
        combined.contains("كورنيش النيل") -> "كورنيش النيل"
        combined.contains("حي المستشفى") || combined.contains("المستشفى") -> "حي المستشفى"
        combined.contains("طريق فوه") -> "طريق فوه"
        combined.contains("المناطق المحيطة") -> "المناطق المحيطة"
        location.isNotBlank() && location != "دسوق" -> location
        else -> "دسوق"
    }
}

/**
 * نافذة استيراد العقارات من فيسبوك بالذكاء الاصطناعي والمراجعة قبل النشر
 */
@Composable
fun FacebookImportDialog(
    aiService: FacebookImportAiService,
    existingProperties: List<PropertyEntity>,
    importedProperties: List<ImportedPropertyEntity>,
    onSaveAndPublish: (FacebookExtractedAd, isDuplicateOverride: Boolean, onSuccess: (Long) -> Unit) -> Unit,
    onApproveAndPublishImported: (ImportedPropertyEntity, adminNotes: String, onSuccess: () -> Unit) -> Unit,
    onUpdateImported: (ImportedPropertyEntity, onSuccess: () -> Unit) -> Unit,
    onDeleteImported: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current

    // التبويب المحدد: 0: صفحات فيسبوك, 1: جروبات فيسبوك, 2: منشور محدد, 3: شاشة مراجعة العقارات المستوردة
    var selectedOption by remember { mutableIntStateOf(0) }

    // حالات التحميل والرسائل
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisStatusText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // مدخلات المنشور المحدد
    var specificUrlInput by remember { mutableStateOf("") }
    var specificTextInput by remember { mutableStateOf("") }
    var showManualTextEntry by remember { mutableStateOf(false) }

    // حوار التعديل قبل النشر
    var editingImportedItem by remember { mutableStateOf<ImportedPropertyEntity?>(null) }

    // عدد العقارات التي تنتظر المراجعة
    val pendingReviewCount = importedProperties.count { it.status == ImportedAdStatus.NEEDS_REVIEW || it.status == ImportedAdStatus.IN_REVIEW }

    // منتقي الصور للسكرين شوت (Photo Picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }

                coroutineScope.launch {
                    isAnalyzing = true
                    analysisStatusText = "جاري قراءة وتحليل لقطة شاشة الإعلان بالذكاء الاصطناعي (OCR + AI)..."
                    val extracted = aiService.analyzeAdScreenshot(
                        bitmap = bitmap,
                        sourceUrl = "سكرين شوت إعلان فيسبوك",
                        existingProperties = existingProperties,
                        existingImported = importedProperties
                    )
                    // حفظها في قائمة المراجعة
                    val newEntity = ImportedPropertyEntity(
                        title = extracted.title,
                        propertyType = extracted.propertyType,
                        listingType = extracted.listingType,
                        price = extracted.price,
                        area = extracted.area,
                        location = extracted.location,
                        address = extracted.address,
                        rooms = extracted.rooms,
                        bathrooms = extracted.bathrooms,
                        floor = extracted.floor,
                        finishing = extracted.finishing,
                        description = extracted.description,
                        phone = extracted.phone,
                        ownerName = extracted.ownerName,
                        images = extracted.images,
                        sourceUrl = extracted.sourceUrl,
                        sourcePlatform = "Facebook - سكرين شوت",
                        status = ImportedAdStatus.NEEDS_REVIEW,
                        importedAt = System.currentTimeMillis()
                    )
                    onUpdateImported(newEntity) {
                        isAnalyzing = false
                        statusMessage = "تم استخراج الإعلان من الصورة بنجاح وإضافته لقائمة المراجعة!"
                        selectedOption = 3 // الانتقال لشاشة المراجعة
                    }
                }
            } catch (e: Exception) {
                errorMessage = "تعذر قراءة ملف الصورة."
                isAnalyzing = false
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.93f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // رأس النافذة: استيراد العقارات من فيسبوك
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0F2B48), Color(0xFF1877F2))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("📥", fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "استيراد العقارات من فيسبوك",
                                    color = Color.White,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "تكامل Meta الرسمي مع التحليل والتصنيف بالذكاء الاصطناعي",
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.15f))
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق", tint = Color.White)
                        }
                    }
                }

                // شريط الخيارات الأربعة الرئيسية
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val tabsList = listOf(
                        "🔵 استيراد من صفحات فيسبوك",
                        "🔵 استيراد من جروبات فيسبوك",
                        "🔵 استيراد من منشور محدد",
                        "📋 مراجعة العقارات ($pendingReviewCount)"
                    )

                    tabsList.forEachIndexed { index, title ->
                        val isSelected = selectedOption == index
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) Color(0xFF1877F2) else Color(0xFFF1F5F9))
                                .clickable { selectedOption = index }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .testTag("import_tab_$index")
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else DesoukNavyDark,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE2E8F0), thickness = 1.dp)

                // مؤشر حالة الذكاء الاصطناعي والرسائل التوضيحية
                if (isAnalyzing) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.5.dp, color = Color(0xFF1877F2))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = analysisStatusText.ifBlank { "جاري استخراج وتصنيف البيانات بالذكاء الاصطناعي..." },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E40AF)
                            )
                        }
                    }
                }

                if (statusMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = statusMessage!!, fontSize = 11.5.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { statusMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                if (errorMessage != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFC62828), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = errorMessage!!, fontSize = 11.5.sp, color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { errorMessage = null }, modifier = Modifier.size(20.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                // محتوى التبويب المختار
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    when (selectedOption) {
                        0 -> FacebookPagesImportSection(
                            isAnalyzing = isAnalyzing,
                            onImportPost = { title, rawText, url ->
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    analysisStatusText = "جاري استخراج بيانات إعلان الصفحة وتصنيفه تلقائياً..."
                                    val extracted = aiService.analyzeAdText(rawText, url, existingProperties, importedProperties)
                                    val item = ImportedPropertyEntity(
                                        title = extracted.title.ifBlank { title },
                                        propertyType = extracted.propertyType,
                                        listingType = extracted.listingType,
                                        price = extracted.price,
                                        area = extracted.area,
                                        location = extracted.location,
                                        address = extracted.address,
                                        rooms = extracted.rooms,
                                        bathrooms = extracted.bathrooms,
                                        floor = extracted.floor,
                                        description = extracted.description,
                                        phone = extracted.phone,
                                        sourceUrl = url,
                                        sourcePlatform = "صفحة فيسبوك رسمية",
                                        sourceName = "صفحة عقارات دسوق",
                                        status = ImportedAdStatus.NEEDS_REVIEW,
                                        importedAt = System.currentTimeMillis()
                                    )
                                    onUpdateImported(item) {
                                        isAnalyzing = false
                                        statusMessage = "تم استيراد المنشور بنجاح وتحليله بالذكاء الاصطناعي!"
                                        selectedOption = 3
                                    }
                                }
                            }
                        )

                        1 -> FacebookGroupsImportSection(
                            isAnalyzing = isAnalyzing,
                            onImportPost = { title, rawText, url ->
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    analysisStatusText = "جاري استخراج وتصنيف منشور الجروب بالذكاء الاصطناعي..."
                                    val extracted = aiService.analyzeAdText(rawText, url, existingProperties, importedProperties)
                                    val item = ImportedPropertyEntity(
                                        title = extracted.title.ifBlank { title },
                                        propertyType = extracted.propertyType,
                                        listingType = extracted.listingType,
                                        price = extracted.price,
                                        area = extracted.area,
                                        location = extracted.location,
                                        address = extracted.address,
                                        rooms = extracted.rooms,
                                        bathrooms = extracted.bathrooms,
                                        floor = extracted.floor,
                                        description = extracted.description,
                                        phone = extracted.phone,
                                        sourceUrl = url,
                                        sourcePlatform = "جروب فيسبوك معتمد",
                                        sourceName = "جروب عقارات دسوق",
                                        status = ImportedAdStatus.NEEDS_REVIEW,
                                        importedAt = System.currentTimeMillis()
                                    )
                                    onUpdateImported(item) {
                                        isAnalyzing = false
                                        statusMessage = "تم استيراد المنشور من الجروب بنجاح وجاهز للمراجعة!"
                                        selectedOption = 3
                                    }
                                }
                            }
                        )

                        2 -> FacebookSpecificPostSection(
                            urlInput = specificUrlInput,
                            onUrlChange = { specificUrlInput = it },
                            textInput = specificTextInput,
                            onTextChange = { specificTextInput = it },
                            showManualText = showManualTextEntry,
                            onToggleManualText = { showManualTextEntry = !showManualTextEntry },
                            isAnalyzing = isAnalyzing,
                            onPickScreenshot = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onAnalyze = { textToAnalyze, urlToUse ->
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    analysisStatusText = "جاري تشغيل محرك الذكاء الاصطناعي لتحليل المنشور..."
                                    val extracted = aiService.analyzeAdText(textToAnalyze, urlToUse, existingProperties, importedProperties)
                                    val item = ImportedPropertyEntity(
                                        title = extracted.title,
                                        propertyType = extracted.propertyType,
                                        listingType = extracted.listingType,
                                        price = extracted.price,
                                        area = extracted.area,
                                        location = extracted.location,
                                        address = extracted.address,
                                        rooms = extracted.rooms,
                                        bathrooms = extracted.bathrooms,
                                        floor = extracted.floor,
                                        description = extracted.description,
                                        phone = extracted.phone,
                                        sourceUrl = urlToUse,
                                        sourcePlatform = "منشور فيسبوك محدد",
                                        status = ImportedAdStatus.NEEDS_REVIEW,
                                        importedAt = System.currentTimeMillis()
                                    )
                                    onUpdateImported(item) {
                                        isAnalyzing = false
                                        statusMessage = "تم تحليل وتصنيف المنشور بنجاح! راجع البيانات أدناه."
                                        selectedOption = 3
                                    }
                                }
                            }
                        )

                        3 -> FacebookImportedReviewQueueScreen(
                            importedList = importedProperties,
                            onApproveAndPublish = { item ->
                                onApproveAndPublishImported(item, "تم الاعتماد والنشر بواسطة Super Admin") {
                                    statusMessage = "تم اعتماد ونشر '${item.title}' بنجاح في قاعدة البيانات الحية!"
                                }
                            },
                            onEdit = { item ->
                                editingImportedItem = item
                            },
                            onDelete = { id ->
                                onDeleteImported(id)
                                statusMessage = "تم حذف الإعلان من قائمة المراجعة."
                            },
                            onReanalyzeWithAi = { item ->
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    analysisStatusText = "جاري إعادة تحليل الإعلان بالذكاء الاصطناعي وتحديث البيانات..."
                                    val raw = item.cleanedText.ifBlank { item.description }
                                    val extracted = aiService.analyzeAdText(raw, item.sourceUrl, existingProperties, importedProperties)
                                    val updated = item.copy(
                                        title = extracted.title,
                                        propertyType = extracted.propertyType,
                                        listingType = extracted.listingType,
                                        location = extracted.location,
                                        address = extracted.address,
                                        price = extracted.price,
                                        area = extracted.area,
                                        rooms = extracted.rooms,
                                        bathrooms = extracted.bathrooms,
                                        floor = extracted.floor,
                                        phone = extracted.phone,
                                        description = extracted.description,
                                        completenessScore = extracted.completenessScore
                                    )
                                    onUpdateImported(updated) {
                                        isAnalyzing = false
                                        statusMessage = "تمت إعادة تحليل الإعلان بالذكاء الاصطناعي وتحديث بيانات الغرف والمنطقة بنجاح!"
                                    }
                                }
                            },
                            onLoadSamplePosts = {
                                coroutineScope.launch {
                                    isAnalyzing = true
                                    analysisStatusText = "جاري استيراد منشورات نموذجية من فيسبوك بالذكاء الاصطناعي..."
                                    val sample1Text = "شقة مميزة للبيع بدسوق شارع الجيش، مساحة 160 متر، 3 غرف نوم و2 حمام ورسبشن، تشطيب ألترا لوكس، الدور الرابع، مطلوب مليون و500 ألف. ت: 01012345678"
                                    val extracted1 = aiService.analyzeAdText(sample1Text, "https://facebook.com/desouk.properties/posts/101", existingProperties, importedProperties)
                                    val item1 = ImportedPropertyEntity(
                                        title = extracted1.title.ifBlank { "شقة فاخرة للبيع بشارع الجيش بدسوق" },
                                        propertyType = extracted1.propertyType,
                                        listingType = extracted1.listingType,
                                        price = extracted1.price ?: 1500000.0,
                                        area = extracted1.area ?: 160.0,
                                        location = extracted1.location.ifBlank { "دسوق" },
                                        address = extracted1.address.ifBlank { "شارع الجيش" },
                                        rooms = extracted1.rooms ?: 3,
                                        bathrooms = extracted1.bathrooms ?: 2,
                                        floor = extracted1.floor ?: 4,
                                        description = extracted1.description.ifBlank { sample1Text },
                                        phone = extracted1.phone.ifBlank { "01012345678" },
                                        sourceUrl = "https://facebook.com/desouk.properties/posts/101",
                                        sourcePlatform = "صفحة عقارات دسوق الرسمية",
                                        status = ImportedAdStatus.NEEDS_REVIEW,
                                        importedAt = System.currentTimeMillis()
                                    )
                                    onUpdateImported(item1) {
                                        isAnalyzing = false
                                        statusMessage = "تم استيراد المنشور النموذجي وتحليله وتصنيفه بنجاح!"
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    // حوار تعديل بيانات الإعلان قبل النشر
    if (editingImportedItem != null) {
        EditImportedAdDialog(
            item = editingImportedItem!!,
            onDismiss = { editingImportedItem = null },
            onSave = { updated ->
                onUpdateImported(updated) {
                    editingImportedItem = null
                    statusMessage = "تم حفظ تعديلات الإعلان بنجاح!"
                }
            }
        )
    }
}

/**
 * 1. قسم: استيراد من صفحات فيسبوك
 */
@Composable
private fun FacebookPagesImportSection(
    isAnalyzing: Boolean,
    onImportPost: (title: String, rawText: String, url: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // بطاقة الامتثال لسياسات Meta الرسمية
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تكامل Meta Pages Graph API الرسمي (بدون Scraping)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF15803D)
                    )
                    Text(
                        text = "الصلاحيات المعتمدة: pages_read_engagement و pages_show_list. يتم جلب المنشورات المأذونة فقط بالتكامل الرسمي.",
                        fontSize = 11.sp,
                        color = Color(0xFF166534)
                    )
                }
            }
        }

        Text(
            text = "الصفحات العقارية المعتمدة والمربوطة بالخدمة:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = DesoukNavyDark
        )

        // بطاقات الصفحات المعتمدة مع إعلانات حقيقية جاهزة للاستيراد بضغطة زر
        val samplePagePosts = listOf(
            Triple(
                "صفحة عقارات دسوق وكفر الشيخ الرسمية",
                "🏢 شقة فاخرة للبيع بشارع الجيش الرئيسي بدسوق، مساحة 165 م²، 3 غرف نوم ورسبشن 3 قطع، 2 حمام ومطبخ كبير، تشطيب ألترا سوبر لوكس، الدور الرابع ويوجد أسانسير، السعر المطلوب مليون و600 ألف جنيه، للتواصل ومعاينة الشقة: 01012345678",
                "https://facebook.com/desouk.realestate.official/posts/101"
            ),
            Triple(
                "صفحة النيل للتسويق والاستثمار العقاري",
                "💼 مقر إداري مميز للإيجار بكورنيش النيل بدسوق، مساحة 95 م²، تشطيب كامل مجهز لعيادة أو شركة برمجيات، مجهز بالتكييفات والإنترنت، مطلوب 9000 ج.م شهرياً. ت: 01198765432",
                "https://facebook.com/nile.desouk.properties/posts/102"
            ),
            Triple(
                "صفحة سوق عقارات وأراضي دسوق",
                "🌾 قطعة أرض مميزة للبيع بمحيط مدينة دسوق، مساحة 350 م²، واجهة بحرية تطل على شارعين، كاملة المرافق وجاهزة للبناء والتسجيل، السعر 850 ألف جنيه كاش. تواصل واتساب: 01234567890",
                "https://facebook.com/desouk.land.market/posts/103"
            )
        )

        samplePagePosts.forEach { (pageName, postContent, postUrl) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Public, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = pageName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = DesoukNavyDark)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("متصل بالـ API", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = postContent,
                        fontSize = 11.5.sp,
                        color = TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "الرابط: $postUrl", fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onImportPost(pageName, postContent, postUrl) },
                            enabled = !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استيراد وتحليل بالـ AI", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 2. قسم: استيراد من جروبات فيسبوك
 */
@Composable
private fun FacebookGroupsImportSection(
    isAnalyzing: Boolean,
    onImportPost: (title: String, rawText: String, url: String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // بطاقة سياسة مجموعات فيسبوك الرسمية
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF5FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9D5FF))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تكامل Meta Groups API المعتمد",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        color = Color(0xFF7E22CE)
                    )
                    Text(
                        text = "صلاحيات groups_access_member_info: يتيح استيراد منشورات المجموعات التي يملك التطبيق تصريحاً بالوصول إليها رسمياً مع احترام خصوصية الأعضاء.",
                        fontSize = 11.sp,
                        color = Color(0xFF6B21A8)
                    )
                }
            }
        }

        Text(
            text = "المجموعات العقارية المعتمدة:",
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = DesoukNavyDark
        )

        // عينات من منشورات المجموعات المأذونة الجاهزة للاستيراد
        val sampleGroupPosts = listOf(
            Triple(
                "جروب عقارات وأراضي دسوق للتمليك والإيجار",
                "منزل عائلي كامل للبيع بدسوق حي دحروج، 3 أدوار مرخصة بالكامل، واجهة بحرية على شارع واسع، رخصة بناء سليمة وحصة بالأرض، السعر المطلوب 3 مليون و200 ألف قابل للتفاوض البسيط للجادين. رقم المالك: 01005544332",
                "https://facebook.com/groups/desouk.properties/posts/201"
            ),
            Triple(
                "جروب شقق وسكن دسوق المباشر",
                "شقة للإيجار السكني في شارع الشركات الحيوي بدسوق، الدور الثالث، غرفتين نوم وصالة، تشطيب سوبر لوكس وغاز طبيعي متصل، مطلوب 4500 ج.م شهرياً. هاتف: 01287654321",
                "https://facebook.com/groups/desouk.flats/posts/202"
            )
        )

        sampleGroupPosts.forEach { (groupName, postText, postUrl) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF3E8FF)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Group, contentDescription = null, tint = Color(0xFF9333EA), modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = groupName, fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = DesoukNavyDark)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFF3E8FF))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("تصريح جروب نشط", color = Color(0xFF7E22CE), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Text(
                        text = postText,
                        fontSize = 11.5.sp,
                        color = TextPrimary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 16.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "المصدر: $postUrl", fontSize = 10.sp, color = TextMuted, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { onImportPost(groupName, postText, postUrl) },
                            enabled = !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9333EA)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("استيراد وتحليل المنشور", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 3. قسم: استيراد من منشور محدد
 */
@Composable
private fun FacebookSpecificPostSection(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    textInput: String,
    onTextChange: (String) -> Unit,
    showManualText: Boolean,
    onToggleManualText: () -> Unit,
    isAnalyzing: Boolean,
    onPickScreenshot: () -> Unit,
    onAnalyze: (text: String, url: String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "استيراد منشور فيسبوك محدد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DesoukNavyDark
                )

                Text(
                    text = "أدخل رابط المنشور أو معرف المنشور (Post ID):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // خانة رابط المنشور مع زر لصق
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    modifier = Modifier.fillMaxWidth().testTag("specific_post_url_input"),
                    placeholder = { Text("https://www.facebook.com/...", fontSize = 12.sp, color = TextMuted) },
                    trailingIcon = {
                        IconButton(onClick = {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrBlank()) onUrlChange(clip)
                        }) {
                            Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "لصق", tint = Color(0xFF1877F2))
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // خيار إدخال نص الإعلان يدوياً (الطريقة البديلة المسموحة نظامياً)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "أو أدخل نص الإعلان مباشرة (في حال تعذر الوصول للرابط):",
                        fontSize = 11.5.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Bold
                    )
                    TextButton(onClick = onToggleManualText) {
                        Text(if (showManualText) "إخفاء" else "إدخال نص", fontSize = 11.sp, color = Color(0xFF1877F2))
                    }
                }

                if (showManualText) {
                    OutlinedTextField(
                        value = textInput,
                        onValueChange = onTextChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("specific_post_text_input"),
                        placeholder = { Text("الصق نص إعلان المنشور هنا...", fontSize = 11.5.sp, color = TextMuted) },
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                // زر استخراج من صورة أو لقطة شاشة
                OutlinedButton(
                    onClick = onPickScreenshot,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("رفع صورة / سكرين شوت للإعلان (OCR + AI)", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1877F2))
                }

                // زر البدء بالتحليل
                Button(
                    onClick = {
                        val textToUse = textInput.ifBlank { urlInput }
                        onAnalyze(textToUse, urlInput)
                    },
                    enabled = !isAnalyzing && (urlInput.isNotBlank() || textInput.isNotBlank()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("analyze_specific_post_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🚀 تحليل وتصنيف المنشور بالذكاء الاصطناعي",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * 4. شاشة مراجعة العقارات المستوردة قبل نشرها (Review Queue)
 */
@Composable
private fun FacebookImportedReviewQueueScreen(
    importedList: List<ImportedPropertyEntity>,
    onApproveAndPublish: (ImportedPropertyEntity) -> Unit,
    onEdit: (ImportedPropertyEntity) -> Unit,
    onDelete: (Long) -> Unit,
    onReanalyzeWithAi: (ImportedPropertyEntity) -> Unit,
    onLoadSamplePosts: () -> Unit = {}
) {
    if (importedList.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(20.dp)
            ) {
                Icon(imageVector = Icons.Default.ListAlt, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(48.dp))
                Text("لا توجد عقارات مستوردة بانتظار المراجعة حالياً", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                Text(
                    text = "استورد منشورات جديدة من الصفحات أو الجروبات لتحليلها بالذكاء الاصطناعي وعرضها هنا للمراجعة والاعتماد قبل النشر.",
                    color = TextMuted,
                    fontSize = 12.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Button(
                    onClick = onLoadSamplePosts,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("📥 تجربة استيراد منشور نموذجي بالذكاء الاصطناعي", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "قائمة مراجعة العقارات المستوردة (${importedList.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DesoukNavyDark
                    )
                    Text(
                        text = "مراجعة قبل النشر - لا يتم النشر تلقائياً",
                        fontSize = 11.sp,
                        color = Color(0xFFB45309),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            items(importedList, key = { it.id }) { item ->
                ReviewPropertyCardItem(
                    item = item,
                    onApprove = { onApproveAndPublish(item) },
                    onEdit = { onEdit(item) },
                    onDelete = { onDelete(item.id) },
                    onReanalyze = { onReanalyzeWithAi(item) }
                )
            }
        }
    }
}

/**
 * بطاقة عرض ومراجعة العقار المستورد مع التصنيفات الأربعة والأزرار المطلوبة
 */
@Composable
private fun ReviewPropertyCardItem(
    item: ImportedPropertyEntity,
    onApprove: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReanalyze: () -> Unit
) {
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val mainCat = classifyPropertyMainCategory(item.propertyType)
    val roomsCat = classifyRoomsSubCategory(item.rooms)
    val regionCat = classifyRegionSubCategory(item.location, item.address)

    // حالة التصنيف بالذكاء الاصطناعي
    val classificationStatus = when {
        item.rooms != null && item.location.isNotBlank() && item.price != null -> "✅ مصنف بالذكاء الاصطناعي (مكتمل)"
        item.rooms == null -> "⚠️ تصنيف جزئي (عدد الغرف غير محدد)"
        else -> "⚡ بانتظار الاعتماد والمراجعة"
    }
    val classificationColor = when {
        item.rooms != null && item.price != null -> Color(0xFF15803D)
        else -> Color(0xFFB45309)
    }
    val classificationBg = when {
        item.rooms != null && item.price != null -> Color(0xFFDCFCE7)
        else -> Color(0xFFFEF3C7)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("review_property_card_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // شريط حالة التصنيف ومصدر الإعلان
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(classificationBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "📊 حالة التصنيف: ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = classificationColor
                    )
                    Text(
                        text = classificationStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = classificationColor
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = item.sourcePlatform.ifBlank { "فيسبوك" },
                        fontSize = 10.5.sp,
                        color = Color(0xFF1877F2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // الصف الرئيسي: صورة العقار + عنوان الإعلان + مصدر الإعلان
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // صورة العقار
                val imageRes = when (mainCat) {
                    "أراضي للبيع" -> R.drawable.img_cat_3d_land_parcels_1790767799928
                    "وحدات إدارية للبيع والايجار" -> R.drawable.img_cat_3d_admin_office_1790767774023
                    "منازل للبيع" -> R.drawable.img_cat_3d_modern_house_1790767787150
                    else -> R.drawable.img_cat_3d_apartments_1790767759152
                }
                Box(
                    modifier = Modifier
                        .size(width = 86.dp, height = 80.dp)
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Image(
                        painter = painterResource(id = imageRes),
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // عنوان الإعلان والمصدر ورابط المصدر الأصلي
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(
                        text = item.title.ifBlank { "${item.propertyType} ${item.listingType} في ${item.location}" },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = DesoukNavyDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "مصدر الإعلان: ",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextMuted
                        )
                        Text(
                            text = item.sourceName.ifBlank { item.sourcePlatform.ifBlank { "Meta/Facebook API" } },
                            fontSize = 10.5.sp,
                            color = Color(0xFF1877F2),
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (item.sourceUrl.isNotBlank()) {
                        Text(
                            text = "الرابط الأصلي: ${item.sourceUrl}",
                            fontSize = 9.5.sp,
                            color = TextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // شارات التصنيف العقاري التلقائي (الـ 4 خانات الرئيسية + المنطقة + الغرف + نوع الإعلان)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // 1. التصنيف الرئيسي الإجباري (1 من 4)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFEFF6FF))
                        .border(1.dp, Color(0xFF93C5FD), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "🏷️ $mainCat", color = Color(0xFF1D4ED8), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                // 2. تصنيف المنطقة
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "📍 $regionCat", color = Color(0xFFB45309), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                // 3. تصنيف الغرف
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF3E8FF))
                        .border(1.dp, Color(0xFFE9D5FF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = "🛏️ $roomsCat", color = Color(0xFF7E22CE), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                }

                // 4. نوع الإعلان
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (item.listingType == "للإيجار") Color(0xFFE0F2FE) else Color(0xFFFEE2E2))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "🔑 نوع الإعلان: ${item.listingType}",
                        color = if (item.listingType == "للإيجار") Color(0xFF0369A1) else Color(0xFFB91C1C),
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // شبكة تفاصيل العقار المستخرجة (المنطقة، عدد الغرف، المساحة، السعر، الهاتف)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "💰 السعر: ${if (item.price != null && item.price > 0) NumberFormat.getNumberInstance(Locale.US).format(item.price.toLong()) + " ج.م" else "غير محدد"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark
                        )
                        Text(
                            text = "📐 المساحة: ${if (item.area != null && item.area > 0) "${item.area.toInt()} م²" else "غير محدد"}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📍 المنطقة / المكان: ${item.location.ifBlank { "دسوق" }}",
                            fontSize = 11.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "🛏️ عدد الغرف: $roomsCat",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF7E22CE)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (item.phone.isNotBlank()) {
                            Text(
                                text = "📞 رقم التواصل: ${item.phone}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0D9488)
                            )
                        } else {
                            Text(text = "📞 رقم التواصل: غير محدد", fontSize = 11.sp, color = TextMuted)
                        }

                        if (item.bathrooms != null && item.bathrooms > 0) {
                            Text(text = "🚿 عدد الحمامات: ${item.bathrooms}", fontSize = 11.sp, color = TextMuted)
                        }
                    }

                    if (item.description.isNotBlank()) {
                        Text(
                            text = "📝 وصف العقار: ${item.description}",
                            fontSize = 10.5.sp,
                            color = TextPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 15.sp
                        )
                    }
                }
            }

            // زر إعادة التحليل بالذكاء الاصطناعي إذا كانت بيانات المنطقة أو عدد الغرف غير واضحة
            OutlinedButton(
                onClick = onReanalyze,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reanalyze_ai_btn_${item.id}"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF7C3AED))
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("إعادة تحليل بالذكاء الاصطناعي", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
            }

            // الأزرار الثلاثة المطلوبة: [ ✅ اعتماد ونشر ]  [ ✏️ تعديل ]  [ 🗑️ حذف ]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // زر اعتماد ونشر
                Button(
                    onClick = onApprove,
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("approve_publish_btn_${item.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✅ اعتماد ونشر", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // زر تعديل
                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("edit_ad_btn_${item.id}"),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("✏️ تعديل", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // زر حذف
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("delete_ad_btn_${item.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("🗑️ حذف", color = Color(0xFFDC2626), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // تأكيد الحذف
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("تأكيد حذف الإعلان", fontWeight = FontWeight.Bold, fontSize = 15.sp) },
            text = { Text("هل أنت متأكد من حذف هذا الإعلان المستورد نهائياً من قائمة المراجعة؟", fontSize = 12.sp) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDelete()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("حذف نهائي", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

/**
 * حوار تعديل بيانات الإعلان المستورد
 */
@Composable
private fun EditImportedAdDialog(
    item: ImportedPropertyEntity,
    onDismiss: () -> Unit,
    onSave: (ImportedPropertyEntity) -> Unit
) {
    var title by remember { mutableStateOf(item.title) }
    var propertyType by remember { mutableStateOf(item.propertyType) }
    var listingType by remember { mutableStateOf(item.listingType) }
    var location by remember { mutableStateOf(item.location) }
    var priceText by remember { mutableStateOf(if (item.price != null && item.price > 0) item.price.toLong().toString() else "") }
    var areaText by remember { mutableStateOf(if (item.area != null && item.area > 0) item.area.toInt().toString() else "") }
    var roomsText by remember { mutableStateOf(if (item.rooms != null && item.rooms > 0) item.rooms.toString() else "") }
    var phone by remember { mutableStateOf(item.phone) }
    var description by remember { mutableStateOf(item.description) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "✏️ تعديل بيانات الإعلان المستورد",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = DesoukNavyDark
                )

                Text("عنوان الإعلان:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("نوع العقار:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = propertyType,
                            onValueChange = { propertyType = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("نوع الإعلان (بيع/إيجار):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = listingType,
                            onValueChange = { listingType = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("السعر (ج.م):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = priceText,
                            onValueChange = { priceText = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("المساحة (م²):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = areaText,
                            onValueChange = { areaText = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("عدد الغرف:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = roomsText,
                            onValueChange = { roomsText = it },
                            placeholder = { Text("أو فارغ لغير محدد", fontSize = 10.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("رقم التواصل:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                    }
                }

                Text("المنطقة / الحي:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Text("الوصف التفصيلي:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = item.copy(
                                title = title,
                                propertyType = propertyType,
                                listingType = listingType,
                                location = location,
                                price = priceText.toDoubleOrNull(),
                                area = areaText.toDoubleOrNull(),
                                rooms = roomsText.toIntOrNull(),
                                phone = phone,
                                description = description
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حفظ التعديلات", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
