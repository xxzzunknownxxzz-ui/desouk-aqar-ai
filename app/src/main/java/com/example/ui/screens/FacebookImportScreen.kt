package com.example.ui.screens

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ImportedAdStatus
import com.example.data.ImportedPropertyEntity
import com.example.data.PropertyEntity
import com.example.data.ai.FacebookExtractedAd
import com.example.data.ai.FacebookImportAiService
import com.example.data.ai.FacebookUrlReadResult
import com.example.ui.theme.BadgeForRentTeal
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * شاشة استيراد عقار من فيسبوك بالذكاء الاصطناعي ومراجعة الإعلان قبل النشر
 */
@Composable
fun FacebookImportScreen(
    aiService: FacebookImportAiService,
    existingProperties: List<PropertyEntity>,
    importedProperties: List<ImportedPropertyEntity>,
    onSaveAndPublish: (FacebookExtractedAd, isDuplicateOverride: Boolean, onSuccess: (Long) -> Unit) -> Unit,
    onSaveAsDraft: (FacebookExtractedAd, onSuccess: (Long) -> Unit) -> Unit,
    onSaveAsNeedsReview: (FacebookExtractedAd, onSuccess: (Long) -> Unit) -> Unit,
    onViewExistingProperty: (Long) -> Unit,
    onDeleteImportedAd: (Long) -> Unit,
    onBack: () -> Unit,
    initialStep: Int = 0,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // 0: استيراد جديد, 1: مراجعة الإعلان قبل النشر, 2: قسم العقارات المستوردة
    var currentStep by remember(initialStep) { mutableIntStateOf(initialStep) }

    // مدخلات الاستيراد
    var facebookUrlInput by remember { mutableStateOf("") }
    var manualTextInput by remember { mutableStateOf("") }
    var showManualTextEntry by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // حالات التحميل والرسائل
    var isAnalyzing by remember { mutableStateOf(false) }
    var analysisStatusText by remember { mutableStateOf("") }
    var friendlyRestrictedMessage by remember { mutableStateOf<String?>(null) }
    var inputErrorMessage by remember { mutableStateOf<String?>(null) }
    var successToastMessage by remember { mutableStateOf<String?>(null) }

    // البيانات المستخرجة للمراجعة
    var currentAd by remember { mutableStateOf(FacebookExtractedAd()) }

    // حوار كشف التكرار
    var showDuplicateDialog by remember { mutableStateOf(false) }
    var pendingPublishAd by remember { mutableStateOf<FacebookExtractedAd?>(null) }
    var isDraftActionPending by remember { mutableStateOf(false) }

    // منتقي الصور للسكرين شوت (Photo Picker بدون أذونات تخزين واسعة)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedImageUri = uri
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                selectedBitmap = bitmap
                friendlyRestrictedMessage = null
                inputErrorMessage = null

                // تشغيل تحليل الصورة فورياً
                coroutineScope.launch {
                    isAnalyzing = true
                    analysisStatusText = "جاري قراءة نص الصورة بالذكاء الاصطناعي (OCR + AI)..."
                    val extracted = aiService.analyzeAdScreenshot(
                        bitmap = bitmap,
                        sourceUrl = facebookUrlInput.trim(),
                        existingProperties = existingProperties,
                        existingImported = importedProperties
                    )
                    currentAd = extracted
                    isAnalyzing = false
                    currentStep = 1 // الانتقال لشاشة مراجعة الإعلان قبل النشر
                }
            } catch (e: Exception) {
                inputErrorMessage = "تعذر قراءة ملف الصورة المختار."
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
    ) {
        // شريط العنوان العلوي
        TopFacebookImportHeader(
            currentStep = currentStep,
            onBack = {
                if (currentStep != 0) {
                    currentStep = 0
                } else {
                    onBack()
                }
            },
            onOpenImportedSection = { currentStep = 2 }
        )

        // محتوى الشاشة بناءً على الخطوة الحالية
        when (currentStep) {
            0 -> {
                // الشاشة الرئيسية 1: أضف عقارًا من فيسبوك
                FacebookImportInputSection(
                    urlInput = facebookUrlInput,
                    onUrlChange = {
                        facebookUrlInput = it
                        inputErrorMessage = null
                    },
                    manualText = manualTextInput,
                    onManualTextChange = { manualTextInput = it },
                    showManualText = showManualTextEntry,
                    onToggleManualText = { showManualTextEntry = !showManualTextEntry },
                    isAnalyzing = isAnalyzing,
                    analysisStatusText = analysisStatusText,
                    friendlyRestrictedMessage = friendlyRestrictedMessage,
                    errorMessage = inputErrorMessage,
                    selectedBitmap = selectedBitmap,
                    onAnalyzeClicked = {
                        val trimmed = facebookUrlInput.trim()
                        if (trimmed.isBlank()) {
                            inputErrorMessage = "يرجى لصق رابط منشور Facebook أولاً."
                            return@FacebookImportInputSection
                        }

                        coroutineScope.launch {
                            isAnalyzing = true
                            friendlyRestrictedMessage = null
                            inputErrorMessage = null
                            analysisStatusText = "جاري التحقق من صحة الرابط وقراءة البيانات المتاحة..."

                            when (val readResult = aiService.tryReadPublicFacebookUrl(trimmed)) {
                                is FacebookUrlReadResult.Success -> {
                                    analysisStatusText = "تم العثور على محتوى المنشور! جاري التنظيم الذكي بالـ AI..."
                                    val extracted = aiService.analyzeAdText(
                                        rawText = readResult.rawText,
                                        sourceUrl = trimmed,
                                        existingProperties = existingProperties,
                                        existingImported = importedProperties
                                    )
                                    currentAd = extracted
                                    isAnalyzing = false
                                    currentStep = 1 // الانتقال لشاشة مراجعة الإعلان قبل النشر
                                }
                                is FacebookUrlReadResult.CannotAccess -> {
                                    isAnalyzing = false
                                    // الرسالة البسيطة المحددة بدون خطأ تقني
                                    friendlyRestrictedMessage = readResult.friendlyMessage
                                }
                                is FacebookUrlReadResult.InvalidUrl -> {
                                    isAnalyzing = false
                                    inputErrorMessage = readResult.errorMessage
                                }
                            }
                        }
                    },
                    onAnalyzeManualText = {
                        if (manualTextInput.trim().isBlank()) return@FacebookImportInputSection
                        coroutineScope.launch {
                            isAnalyzing = true
                            analysisStatusText = "جاري استخراج البيانات العقارية بالذكاء الاصطناعي..."
                            val extracted = aiService.analyzeAdText(
                                rawText = manualTextInput.trim(),
                                sourceUrl = facebookUrlInput.trim(),
                                existingProperties = existingProperties,
                                existingImported = importedProperties
                            )
                            currentAd = extracted
                            isAnalyzing = false
                            currentStep = 1
                        }
                    },
                    onPickScreenshot = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onOpenDirectReview = {
                        // إتاحة المراجعة المباشرة وتعبئة البيانات يدويًا
                        currentAd = FacebookExtractedAd(
                            title = "عقار مستورد من فيسبوك",
                            sourceUrl = facebookUrlInput.trim(),
                            location = "دسوق"
                        )
                        currentStep = 1
                    }
                )
            }
            1 -> {
                // الشاشة 2: مراجعة الإعلان قبل النشر
                FacebookAdReviewScreen(
                    ad = currentAd,
                    onAdChange = { currentAd = it },
                    onPublishClicked = {
                        // فحص التكرار الشامل قبل النشر
                        val dupCheck = aiService.checkDuplicates(
                            ad = currentAd,
                            sourceUrl = currentAd.sourceUrl,
                            existingProperties = existingProperties,
                            existingImported = importedProperties
                        )
                        if (dupCheck.first) {
                            currentAd = currentAd.copy(
                                isDuplicate = true,
                                duplicateReason = dupCheck.second,
                                matchedProperty = dupCheck.third,
                                matchedImported = dupCheck.fourth
                            )
                            pendingPublishAd = currentAd
                            isDraftActionPending = false
                            showDuplicateDialog = true
                        } else {
                            onSaveAndPublish(currentAd, false) {
                                successToastMessage = "تم حفظ ونشر العقار بنجاح! يظهر الآن في أحدث العقارات."
                                currentStep = 2
                            }
                        }
                    },
                    onSaveDraftClicked = {
                        val dupCheck = aiService.checkDuplicates(
                            ad = currentAd,
                            sourceUrl = currentAd.sourceUrl,
                            existingProperties = existingProperties,
                            existingImported = importedProperties
                        )
                        if (dupCheck.first) {
                            currentAd = currentAd.copy(
                                isDuplicate = true,
                                duplicateReason = dupCheck.second,
                                matchedProperty = dupCheck.third,
                                matchedImported = dupCheck.fourth
                            )
                            pendingPublishAd = currentAd
                            isDraftActionPending = true
                            showDuplicateDialog = true
                        } else {
                            onSaveAsDraft(currentAd) {
                                successToastMessage = "تم حفظ الإعلان كمسودة بنجاح."
                                currentStep = 2
                            }
                        }
                    },
                    onCancelClicked = {
                        currentStep = 0
                    }
                )
            }
            2 -> {
                // الشاشة 3: قسم العقارات المستوردة
                ImportedPropertiesListSection(
                    importedList = importedProperties,
                    onPublishItem = { item ->
                        val ad = FacebookExtractedAd(
                            title = item.title,
                            propertyType = item.propertyType,
                            listingType = item.listingType,
                            location = item.location,
                            address = item.address,
                            price = item.price,
                            area = item.area,
                            rooms = item.rooms,
                            bathrooms = item.bathrooms,
                            floor = item.floor,
                            finishing = item.finishing,
                            streetsCount = item.streetsCount,
                            facade = item.facade,
                            description = item.description,
                            phone = item.phone,
                            ownerName = item.ownerName,
                            sourceUrl = item.sourceUrl.ifBlank { item.postUrl },
                            images = item.images,
                            completenessScore = item.completenessScore,
                            isDuplicate = item.isDuplicate
                        )
                        onSaveAndPublish(ad, false) {
                            successToastMessage = "تم نشر العقار بنجاح في أحدث العقارات!"
                        }
                    },
                    onEditItem = { item ->
                        currentAd = FacebookExtractedAd(
                            title = item.title,
                            propertyType = item.propertyType,
                            listingType = item.listingType,
                            location = item.location,
                            address = item.address,
                            price = item.price,
                            area = item.area,
                            rooms = item.rooms,
                            bathrooms = item.bathrooms,
                            floor = item.floor,
                            finishing = item.finishing,
                            streetsCount = item.streetsCount,
                            facade = item.facade,
                            description = item.description,
                            phone = item.phone,
                            ownerName = item.ownerName,
                            sourceUrl = item.sourceUrl.ifBlank { item.postUrl },
                            images = item.images,
                            completenessScore = item.completenessScore,
                            isDuplicate = item.isDuplicate,
                            duplicateReason = item.duplicateMatchReason
                        )
                        currentStep = 1
                    },
                    onDeleteItem = { onDeleteImportedAd(it) },
                    onAddNewImport = { currentStep = 0 }
                )
            }
        }
    }

    // حوار كشف التكرار (Duplicate Detection Dialog)
    if (showDuplicateDialog && pendingPublishAd != null) {
        val targetAd = pendingPublishAd!!
        AlertDialog(
            onDismissRequest = { showDuplicateDialog = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(18.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BadgeForSaleRed.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = BadgeForSaleRed, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "يبدو أن هذا العقار موجود بالفعل في التطبيق.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تم العثور على تطابق في قاعدة البيانات طبقاً للمعايير التالية:",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Card(
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = targetAd.duplicateReason.ifBlank { "تطابق في رقم الهاتف أو السعر أو رابط المنشور" },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB78103)
                            )
                            if (targetAd.matchedProperty != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "العقار المنشور: #${targetAd.matchedProperty.id} - ${targetAd.matchedProperty.title} (${targetAd.matchedProperty.district})",
                                    fontSize = 11.sp,
                                    color = DesoukNavyDark
                                )
                            }
                        }
                    }
                    Text(
                        text = "يمكنك عرض العقار الموجود لتفاديه، أو المتابعة بالإضافة إذا كان إعلاناً محدثاً.",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showDuplicateDialog = false
                            if (isDraftActionPending) {
                                onSaveAsDraft(targetAd) { currentStep = 2 }
                            } else {
                                onSaveAndPublish(targetAd, true) { currentStep = 2 }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).testTag("override_duplicate_button")
                    ) {
                        Text("إضافة رغم التكرار", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    if (targetAd.matchedProperty != null) {
                        OutlinedButton(
                            onClick = {
                                showDuplicateDialog = false
                                onViewExistingProperty(targetAd.matchedProperty.id)
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("view_existing_property_button")
                        ) {
                            Text("عرض العقار الموجود", fontSize = 10.sp, color = DesoukNavyDark)
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDuplicateDialog = false },
                    modifier = Modifier.testTag("cancel_duplicate_dialog_button")
                ) {
                    Text("إلغاء", color = Color.Gray, fontSize = 12.sp)
                }
            }
        )
    }

    // تنبيه نجاح الإجراء
    if (successToastMessage != null) {
        AlertDialog(
            onDismissRequest = { successToastMessage = null },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = BadgeForRentTeal)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تمت العملية بنجاح", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Text(successToastMessage ?: "", fontSize = 13.sp, color = TextPrimary)
            },
            confirmButton = {
                Button(
                    onClick = { successToastMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("حسناً")
                }
            }
        )
    }
}

/**
 * الشريط العلوي الخاص بميزة استيراد عقار من فيسبوك
 */
@Composable
private fun TopFacebookImportHeader(
    currentStep: Int,
    onBack: () -> Unit,
    onOpenImportedSection: () -> Unit
) {
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
                        onClick = onBack,
                        modifier = Modifier.testTag("facebook_import_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentStep) {
                                    1 -> "مراجعة الإعلان قبل النشر"
                                    2 -> "العقارات المستوردة"
                                    else -> "استيراد عقار من فيسبوك بالذكاء الاصطناعي"
                                },
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF1877F2))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Facebook Ai",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "نظام استخراج ذكي متوافق ومحمي بالكامل",
                            color = DesoukGold.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }

                // زر الانتقال لقسم العقارات المستوردة
                if (currentStep != 2) {
                    OutlinedButton(
                        onClick = onOpenImportedSection,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = ButtonDefaults.outlinedButtonBorder.copy(brush = androidx.compose.ui.graphics.SolidColor(DesoukGold)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("open_imported_ads_button")
                    ) {
                        Icon(imageVector = Icons.Default.ListAlt, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("العقارات المستوردة", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DesoukGold)
                    }
                }
            }
        }
    }
}

/**
 * الشاشة 1: إدخال الرابط، زر التحليل، ورفع صورة الإعلان (Screenshot)
 */
@Composable
private fun FacebookImportInputSection(
    urlInput: String,
    onUrlChange: (String) -> Unit,
    manualText: String,
    onManualTextChange: (String) -> Unit,
    showManualText: Boolean,
    onToggleManualText: () -> Unit,
    isAnalyzing: Boolean,
    analysisStatusText: String,
    friendlyRestrictedMessage: String?,
    errorMessage: String?,
    selectedBitmap: Bitmap?,
    onAnalyzeClicked: () -> Unit,
    onAnalyzeManualText: () -> Unit,
    onPickScreenshot: () -> Unit,
    onOpenDirectReview: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // بطاقة الترحيب والتعليمات
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE7F3FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color(0xFF1877F2),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "أضف عقارًا من فيسبوك",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "استخراج ذكي لبيانات العقار وتنظيمها قبل النشر",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))

                // حقل إدخال الرابط
                Text(
                    text = "الصق رابط منشور Facebook هنا",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DesoukNavyDark
                )

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("facebook_url_input"),
                    placeholder = {
                        Text("https://www.facebook.com/.../posts/...", color = Color.Gray, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Link, contentDescription = null, tint = Color(0xFF1877F2))
                    },
                    trailingIcon = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (urlInput.isNotBlank()) {
                                IconButton(onClick = { onUrlChange("") }) {
                                    Icon(imageVector = Icons.Default.Clear, contentDescription = "مسح", tint = Color.Gray)
                                }
                            }
                            // زر لصق من الحافظة
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription?.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN) == true) {
                                        val text = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                        if (text.isNotBlank()) {
                                            onUrlChange(text)
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("paste_facebook_url_button")
                            ) {
                                Icon(imageVector = Icons.Default.ContentPaste, contentDescription = "لصق من الحافظة", tint = DesoukNavyDark)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1877F2),
                        unfocusedBorderColor = Color.LightGray,
                        focusedContainerColor = Color(0xFFF8FAFC),
                        unfocusedContainerColor = Color(0xFFF8FAFC)
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )

                // رسالة خطأ التحقق إن وجدت
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = BadgeForSaleRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // الزر الرئيسي: تحليل الإعلان بالذكاء الاصطناعي
                Button(
                    onClick = onAnalyzeClicked,
                    enabled = !isAnalyzing && urlInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1877F2),
                        disabledContainerColor = Color(0xFF1877F2).copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("analyze_facebook_button")
                ) {
                    if (isAnalyzing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("جاري التحليل...", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = DesoukGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "تحليل الإعلان بالذكاء الاصطناعي",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // شريط تقدم التحليل
        if (isAnalyzing) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color(0xFF1877F2))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = analysisStatusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFF1877F2),
                        trackColor = Color(0xFFE2E8F0)
                    )
                }
            }
        }

        // الرسالة الصريحة عند تعذر الوصول لرابط المنشور (كما طلب المستخدم بالضبط)
        if (friendlyRestrictedMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = friendlyRestrictedMessage,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            lineHeight = 20.sp
                        )
                    }

                    HorizontalDivider(color = Color(0xFFFDE68A))

                    Text(
                        text = "اختر الطريقة الأنسب لك لمتابعة الإضافة الآن:",
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // زر رفع سكرين شوت
                        Button(
                            onClick = onPickScreenshot,
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("pick_screenshot_fallback_button")
                        ) {
                            Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("رفع صورة الإعلان", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        // زر إدخال البيانات يدوياً
                        OutlinedButton(
                            onClick = onOpenDirectReview,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DesoukNavyDark),
                            modifier = Modifier.weight(1f).testTag("enter_manually_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إدخال يدوي", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // بطاقة الميزة: إضافة إعلان من صورة (Screenshot OCR + AI)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF3E8FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = null,
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "إضافة إعلان من صورة (Screenshot)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "قراءة نص المنشور من الصورة واستخراج السعر والمساحة تلقائياً",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }

                if (selectedBitmap != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, Color(0xFF7C3AED), RoundedCornerShape(12.dp))
                    ) {
                        Image(
                            bitmap = selectedBitmap.asImageBitmap(),
                            contentDescription = "معاينة السكرين شوت",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                }

                Button(
                    onClick = onPickScreenshot,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("upload_screenshot_button")
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedBitmap != null) "اختيار سكرين شوت آخر" else "رفع صورة الإعلان (Screenshot)",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // خيار: لصق نص المنشور يدوياً
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleManualText() },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "أو نسخ ولصق نص المنشور مباشرة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                    }
                    Text(
                        text = if (showManualText) "إخفاء" else "إظهار",
                        color = Color(0xFF1877F2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedVisibility(visible = showManualText) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = manualText,
                            onValueChange = onManualTextChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .testTag("manual_text_input"),
                            placeholder = {
                                Text("انسخ والصق نص الإعلان هنا وسيقوم الذكاء الاصطناعي بتنظيمه...", fontSize = 12.sp, color = Color.Gray)
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = DesoukNavyDark,
                                unfocusedBorderColor = Color.LightGray
                            )
                        )

                        Button(
                            onClick = onAnalyzeManualText,
                            enabled = manualText.isNotBlank() && !isAnalyzing,
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("analyze_manual_text_button")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = DesoukGold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("تحليل النص المستخرج بالذكاء الاصطناعي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // بطاقة تنبيه منع البيانات الوهمية (Anti-Hallucination Policy)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.HelpOutline, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "سياسة الذكاء الاصطناعي: لا يتم اختلاق أو تخمين أي بيانات غير موجودة (كالسعر أو المساحة أو الهاتف)، وتُترك الحقول غير المذكورة فارغة تحت وسم 'غير متوفر'.",
                    fontSize = 11.sp,
                    color = TextPrimary,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

/**
 * الشاشة 2: مراجعة الإعلان قبل النشر (نموذج قابل للتعديل بالكامل)
 */
@Composable
private fun FacebookAdReviewScreen(
    ad: FacebookExtractedAd,
    onAdChange: (FacebookExtractedAd) -> Unit,
    onPublishClicked: () -> Unit,
    onSaveDraftClicked: () -> Unit,
    onCancelClicked: () -> Unit
) {
    val propertyTypes = listOf("شقة", "فيلا", "منزل", "أرض", "محل", "مكتب", "عمارة", "مخزن", "شاليه", "عقار تجاري", "أخرى")
    val locationZones = listOf("دسوق", "أحياء ومناطق دسوق", "القرى والمناطق المحيطة")
    val finishingOptions = listOf("سوبر لوكس", "ألترا لوكس", "لوكس", "نصف تشطيب", "على المحارة", "بدون تشطيب", "غير متوفر")

    var showPropertyTypeMenu by remember { mutableStateOf(false) }
    var showLocationMenu by remember { mutableStateOf(false) }
    var showFinishingMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // الشعار الإجباري في أعلى الشاشة كما طُلب
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE))
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "تم استخراج البيانات بواسطة الذكاء الاصطناعي — يرجى مراجعتها قبل النشر.",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1E40AF),
                        lineHeight = 18.sp
                    )
                    Text(
                        text = "يمكنك تعديل أي حقل وتدقيقه لضمان صحة الإعلان في التطبيق.",
                        fontSize = 11.sp,
                        color = Color(0xFF3B82F6)
                    )
                }
            }
        }

        // بطاقة النموذج القابل للتعديل
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
                    text = "نموذج بيانات الإعلان",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = DesoukNavyDark
                )

                // 1. عنوان الإعلان
                Text("عنوان الإعلان", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = ad.title,
                    onValueChange = { onAdChange(ad.copy(title = it)) },
                    modifier = Modifier.fillMaxWidth().testTag("review_title_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // 2. نوع العقار ونوع الإعلان
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // نوع العقار
                    Column(modifier = Modifier.weight(1f)) {
                        Text("نوع العقار", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Box {
                            OutlinedButton(
                                onClick = { showPropertyTypeMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(ad.propertyType, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = showPropertyTypeMenu,
                                onDismissRequest = { showPropertyTypeMenu = false }
                            ) {
                                propertyTypes.forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type) },
                                        onClick = {
                                            onAdChange(ad.copy(propertyType = type))
                                            showPropertyTypeMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // نوع الإعلان (للبيع / للإيجار)
                    Column(modifier = Modifier.weight(1f)) {
                        Text("نوع الإعلان", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            FilterChip(
                                selected = ad.listingType == "للبيع",
                                onClick = { onAdChange(ad.copy(listingType = "للبيع", priceUnit = "ج.م")) },
                                label = { Text("للبيع", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BadgeForSaleRed,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = ad.listingType == "للإيجار",
                                onClick = { onAdChange(ad.copy(listingType = "للإيجار", priceUnit = "ج.م / شهرياً")) },
                                label = { Text("للإيجار", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = BadgeForRentTeal,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // 3. المنطقة / العنوان
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("المنطقة / النطاق", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Box {
                            OutlinedButton(
                                onClick = { showLocationMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(ad.location, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp)
                            }
                            DropdownMenu(
                                expanded = showLocationMenu,
                                onDismissRequest = { showLocationMenu = false }
                            ) {
                                locationZones.forEach { zone ->
                                    DropdownMenuItem(
                                        text = { Text(zone) },
                                        onClick = {
                                            onAdChange(ad.copy(location = zone))
                                            showLocationMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("العنوان التفصيلي / الشارع", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.address,
                            onValueChange = { onAdChange(ad.copy(address = it)) },
                            modifier = Modifier.fillMaxWidth().testTag("review_address_input"),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("اسم الشارع أو المعلم", fontSize = 11.sp) },
                            singleLine = true
                        )
                    }
                }

                // 4. السعر والمساحة
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("السعر (${ad.priceUnit})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.price?.toString()?.replace(".0", "") ?: "",
                            onValueChange = { onAdChange(ad.copy(price = it.toDoubleOrNull())) },
                            modifier = Modifier.fillMaxWidth().testTag("review_price_input"),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("غير متوفر", fontSize = 11.sp, color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("المساحة (م²)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.area?.toString()?.replace(".0", "") ?: "",
                            onValueChange = { onAdChange(ad.copy(area = it.toDoubleOrNull())) },
                            modifier = Modifier.fillMaxWidth().testTag("review_area_input"),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("غير متوفر", fontSize = 11.sp, color = Color.Gray) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                // 5. الغرف، الحمامات، الدور
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الغرف", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.rooms?.toString() ?: "",
                            onValueChange = { onAdChange(ad.copy(rooms = it.toIntOrNull())) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("غير متوفر", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الحمامات", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.bathrooms?.toString() ?: "",
                            onValueChange = { onAdChange(ad.copy(bathrooms = it.toIntOrNull())) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("غير متوفر", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("الدور", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.floor?.toString() ?: "",
                            onValueChange = { onAdChange(ad.copy(floor = it.toIntOrNull())) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("غير متوفر", fontSize = 10.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }
                }

                // 6. التشطيب، عدد الشوارع، الواجهة
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        Text("حالة التشطيب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        Box {
                            OutlinedButton(
                                onClick = { showFinishingMenu = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(ad.finishing.ifBlank { "غير متوفر" }, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 11.sp)
                            }
                            DropdownMenu(
                                expanded = showFinishingMenu,
                                onDismissRequest = { showFinishingMenu = false }
                            ) {
                                finishingOptions.forEach { opt ->
                                    DropdownMenuItem(
                                        text = { Text(opt) },
                                        onClick = {
                                            onAdChange(ad.copy(finishing = opt))
                                            showFinishingMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Column(modifier = Modifier.weight(0.8f)) {
                        Text("عدد الشوارع", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.streetsCount.toString(),
                            onValueChange = { onAdChange(ad.copy(streetsCount = it.toIntOrNull() ?: 1)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("واجهة العقار", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.facade,
                            onValueChange = { onAdChange(ad.copy(facade = it)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("واجهة بحرية/قبلية", fontSize = 10.sp) },
                            singleLine = true
                        )
                    }
                }

                // 7. رقم الهاتف واسم المعلن
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("رقم الهاتف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.phone,
                            onValueChange = { onAdChange(ad.copy(phone = it)) },
                            modifier = Modifier.fillMaxWidth().testTag("review_phone_input"),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("غير متوفر", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text("اسم المعلن", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                        OutlinedTextField(
                            value = ad.ownerName,
                            onValueChange = { onAdChange(ad.copy(ownerName = it)) },
                            modifier = Modifier.fillMaxWidth().testTag("review_owner_input"),
                            shape = RoundedCornerShape(10.dp),
                            placeholder = { Text("غير متوفر", fontSize = 11.sp) },
                            singleLine = true
                        )
                    }
                }

                // 8. رابط المنشور الأصلي (source_url)
                Text("رابط المنشور الأصلي (source_url)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = ad.sourceUrl,
                    onValueChange = { onAdChange(ad.copy(sourceUrl = it)) },
                    modifier = Modifier.fillMaxWidth().testTag("review_source_url_input"),
                    shape = RoundedCornerShape(10.dp),
                    placeholder = { Text("https://www.facebook.com/...", fontSize = 11.sp) },
                    singleLine = true
                )

                // 9. الوصف الكامل
                Text("الوصف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                OutlinedTextField(
                    value = ad.description,
                    onValueChange = { onAdChange(ad.copy(description = it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("review_description_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                // 10. اختيار صورة العقار المتاحة
                Text("صورة العقار المتاحة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val sampleImages = listOf(
                        "img_property_apartment" to "شقة",
                        "img_property_building" to "عمارة",
                        "img_property_shop" to "محل/تجاري",
                        "img_property_land" to "أرض",
                        "img_hero_building" to "فيلا/برج"
                    )
                    sampleImages.forEach { (res, label) ->
                        val isSelected = ad.images == res
                        Card(
                            modifier = Modifier
                                .size(width = 80.dp, height = 70.dp)
                                .clickable { onAdChange(ad.copy(images = res)) },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) DesoukGold else Color.LightGray
                            ),
                            colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFFFFFBEB) else Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(label, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                                if (isSelected) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // الأزرار الأربعة الإلزامية: [حفظ ونشر] [حفظ كمسودة] [تعديل البيانات] [إلغاء]
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // [حفظ ونشر]
            Button(
                onClick = onPublishClicked,
                colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("publish_facebook_ad_button")
            ) {
                Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = DesoukGold)
                Spacer(modifier = Modifier.width(8.dp))
                Text("حفظ ونشر", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // [حفظ كمسودة]
                OutlinedButton(
                    onClick = onSaveDraftClicked,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f).testTag("save_draft_facebook_ad_button")
                ) {
                    Text("حفظ كمسودة", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                }

                // [إلغاء]
                OutlinedButton(
                    onClick = onCancelClicked,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f).testTag("cancel_review_button")
                ) {
                    Text("إلغاء", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Red)
                }
            }
        }
    }
}

/**
 * الشاشة 3: قسم "العقارات المستوردة"
 * يعرض العقارات التي تم استيرادها، تاريخ الاستيراد، مصدر الإعلان، حالة الإعلان (منشور، مسودة، يحتاج مراجعة، مكرر)
 */
@Composable
private fun ImportedPropertiesListSection(
    importedList: List<ImportedPropertyEntity>,
    onPublishItem: (ImportedPropertyEntity) -> Unit,
    onEditItem: (ImportedPropertyEntity) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onAddNewImport: () -> Unit
) {
    val statusFilters = listOf("الكل", "منشور", "مسودة", "يحتاج مراجعة", "مكرر")
    var selectedFilter by remember { mutableStateOf("الكل") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredList = importedList.filter { item ->
        val matchesFilter = when (selectedFilter) {
            "الكل" -> true
            "منشور" -> item.status == "منشور" || item.status == "نشط"
            "مسودة" -> item.status == "مسودة"
            "يحتاج مراجعة" -> item.status == "يحتاج مراجعة" || item.status == "قيد المراجعة"
            "مكرر" -> item.status == "مكرر" || item.isDuplicate
            else -> item.status == selectedFilter
        }
        val matchesSearch = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.phone.contains(searchQuery) ||
                item.district.contains(searchQuery, ignoreCase = true) ||
                item.sourceUrl.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // بطاقة الترويسة والإضافة
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("العقارات المستوردة", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = DesoukNavyDark)
                Text("إجمالي الإعلانات: ${importedList.size} عقار", fontSize = 12.sp, color = TextMuted)
            }
            Button(
                onClick = onAddNewImport,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("add_new_import_button")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("+ استيراد من فيسبوك", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        // حقل البحث
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث في الإعلانات المستوردة أو رقم الهاتف...", fontSize = 12.sp) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        // فلاتر الحالة (منشور، مسودة، يحتاج مراجعة، مكرر)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            statusFilters.forEach { filter ->
                val isSelected = selectedFilter == filter
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter, fontSize = 12.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DesoukNavyDark,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // قائمة الإعلانات المستوردة
        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(imageVector = Icons.Default.ListAlt, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("لا توجد إعلانات مطابقة في هذا التصنيف", color = TextMuted, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.id }) { item ->
                    ImportedPropertyCard(
                        item = item,
                        onPublish = { onPublishItem(item) },
                        onEdit = { onEditItem(item) },
                        onDelete = { onDeleteItem(item.id) }
                    )
                }
            }
        }
    }
}

/**
 * بطاقة عرض الإعلان المستورد في قائمة العقارات المستوردة
 */
@Composable
private fun ImportedPropertyCard(
    item: ImportedPropertyEntity,
    onPublish: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("ar"))
    val dateStr = dateFormat.format(Date(item.createdAt))

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
                // شارة حالة الإعلان
                val (badgeBg, badgeText, badgeColor) = when (item.status) {
                    "منشور", "نشط" -> Triple(Color(0xFFE8F5E9), "منشور", Color(0xFF2E7D32))
                    "مسودة" -> Triple(Color(0xFFF1F5F9), "مسودة", Color(0xFF475569))
                    "مكرر" -> Triple(Color(0xFFFFEBEE), "مكرر", Color(0xFFC62828))
                    else -> Triple(Color(0xFFFFF8E1), "يحتاج مراجعة", Color(0xFFF57F17))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = badgeText, color = badgeColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // مصدر الإعلان وتاريخ الاستيراد
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = Color(0xFF1877F2), modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = item.sourcePlatform.ifBlank { "Facebook" },
                        fontSize = 11.sp,
                        color = Color(0xFF1877F2),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Text(
                text = item.title.ifBlank { "${item.propertyType} ${item.listingType} في ${item.location}" },
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = TextPrimary
            )

            // تفاصيل: السعر والمساحة والموقع
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (item.price != null && item.price > 0) {
                    val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(item.price.toLong())
                    Text("السعر: $formattedPrice ج.م", fontSize = 12.sp, color = DesoukNavyDark, fontWeight = FontWeight.Bold)
                } else {
                    Text("السعر: غير متوفر", fontSize = 11.sp, color = Color.Gray)
                }

                if (item.area != null && item.area > 0) {
                    Text("المساحة: ${item.area.toInt()} م²", fontSize = 12.sp, color = TextPrimary)
                } else {
                    Text("المساحة: غير متوفرة", fontSize = 11.sp, color = Color.Gray)
                }

                Text(item.location, fontSize = 12.sp, color = TextMuted)
            }

            if (item.phone.isNotBlank()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(item.phone, fontSize = 11.sp, color = TextPrimary)
                }
            }

            // رابط المنشور الأصلي source_url
            val sourceUrl = item.sourceUrl.ifBlank { item.postUrl }
            if (sourceUrl.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الرابط: $sourceUrl",
                        fontSize = 10.sp,
                        color = Color(0xFF1877F2),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Text("تاريخ الاستيراد: $dateStr", fontSize = 10.sp, color = TextMuted)

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

            // أزرار التحكم
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(6.dp))

                OutlinedButton(
                    onClick = onEdit,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("مراجعة وتعديل", fontSize = 11.sp)
                }

                if (item.status != "منشور" && item.status != "نشط") {
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = onPublish,
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text("نشر الآن", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}
