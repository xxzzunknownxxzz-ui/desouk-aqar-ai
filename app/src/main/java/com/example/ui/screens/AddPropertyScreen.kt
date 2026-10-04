package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddHome
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.PropertyDraftEntity
import com.example.ui.components.PropertyImageLoader
import com.example.ui.theme.BadgeForRentTeal
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukGoldDark
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.DesoukNavyLight
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusErrorBg
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale

@Composable
fun AddPropertyScreen(
    draft: PropertyDraftEntity? = null,
    onSaveDraft: (
        title: String,
        category: String,
        type: String,
        district: String,
        addressDetail: String,
        priceText: String,
        areaText: String,
        roomsText: String,
        bathroomsText: String,
        floorText: String,
        phone: String,
        whatsapp: String,
        description: String,
        selectedImage: String
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
    onClearDraft: () -> Unit = {},
    onAddSuccess: () -> Unit,
    onSaveProperty: (
        title: String,
        category: String,
        type: String,
        location: String,
        district: String,
        price: Double,
        priceUnit: String,
        rooms: Int,
        bathrooms: Int,
        area: Double,
        floor: Int,
        phone: String,
        whatsapp: String,
        description: String,
        imageResName: String,
        ownerName: String,
        isNegotiable: Boolean,
        imagesJson: String,
        videoUrl: String,
        finishing: String,
        addressDetail: String,
        onSuccess: () -> Unit
    ) -> Unit,
    currentUserName: String = "",
    currentUserPhone: String = "",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("شقق") }
    var type by remember { mutableStateOf("للبيع") }
    var isNegotiable by remember { mutableStateOf(true) }
    var district by remember { mutableStateOf("شارع الجيش") }
    var addressDetail by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var areaText by remember { mutableStateOf("") }
    var roomsText by remember { mutableStateOf("3") }
    var bathroomsText by remember { mutableStateOf("1") }
    var floorText by remember { mutableStateOf("2") }
    var finishing by remember { mutableStateOf("كامل التشطيب (سوبر لوكس)") }
    var phone by remember { mutableStateOf(currentUserPhone.ifBlank { "01001234567" }) }
    var whatsapp by remember { mutableStateOf(currentUserPhone.ifBlank { "201001234567" }) }
    var ownerName by remember { mutableStateOf(currentUserName.ifBlank { "معلن عقارات دسوق" }) }
    var videoUrl by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedImage by remember { mutableStateOf("img_property_apartment") }

    // صور العقار المرفوعة من المعرض
    val uploadedPhotoUris = remember { mutableStateListOf<Uri>() }
    var coverPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        if (uris.isNotEmpty()) {
            uploadedPhotoUris.addAll(uris.take(8 - uploadedPhotoUris.size))
            if (coverPhotoUri == null) {
                coverPhotoUri = uris.firstOrNull()
            }
        }
    }

    var isDraftRestored by remember { mutableStateOf(false) }
    var draftRestoredBannerVisible by remember { mutableStateOf(false) }
    var lastSavedStatus by remember { mutableStateOf<String?>(null) }

    var validationError by remember { mutableStateOf<String?>(null) }
    var showPreviewDialog by remember { mutableStateOf(false) }
    var isPublishing by remember { mutableStateOf(false) }

    // استعادة المسودة تلقائياً من Room Database عند فتح الشاشة
    LaunchedEffect(draft) {
        if (!isDraftRestored && draft != null) {
            val hasMeaningfulData = draft.title.isNotBlank() || draft.addressDetail.isNotBlank() ||
                    draft.priceText.isNotBlank() || draft.areaText.isNotBlank() || draft.description.isNotBlank()
            if (hasMeaningfulData) {
                title = draft.title
                category = draft.category
                type = draft.type
                district = draft.district
                addressDetail = draft.addressDetail
                priceText = draft.priceText
                areaText = draft.areaText
                roomsText = draft.roomsText
                bathroomsText = draft.bathroomsText
                floorText = draft.floorText
                phone = draft.phone
                whatsapp = draft.whatsapp
                description = draft.description
                selectedImage = draft.selectedImage
                draftRestoredBannerVisible = true
                lastSavedStatus = "تم استرجاع المسودة المحفوظة"
            }
            isDraftRestored = true
        }
    }

    // حفظ تلقائي دوري مع debounce بمقدار 800ms
    LaunchedEffect(
        title, category, type, district, addressDetail,
        priceText, areaText, roomsText, bathroomsText,
        floorText, phone, whatsapp, description, selectedImage
    ) {
        val hasData = title.isNotBlank() || addressDetail.isNotBlank() ||
                priceText.isNotBlank() || areaText.isNotBlank() || description.isNotBlank()
        if (hasData) {
            delay(800)
            onSaveDraft(
                title, category, type, district, addressDetail,
                priceText, areaText, roomsText, bathroomsText,
                floorText, phone, whatsapp, description, selectedImage
            )
            lastSavedStatus = "تم الحفظ تلقائياً كمسودة ✓"
        }
    }

    // حفظ تلقائي عند الخروج
    DisposableEffect(
        title, category, type, district, addressDetail,
        priceText, areaText, roomsText, bathroomsText,
        floorText, phone, whatsapp, description, selectedImage
    ) {
        onDispose {
            val hasData = title.isNotBlank() || addressDetail.isNotBlank() ||
                    priceText.isNotBlank() || areaText.isNotBlank() || description.isNotBlank()
            if (hasData) {
                onSaveDraft(
                    title, category, type, district, addressDetail,
                    priceText, areaText, roomsText, bathroomsText,
                    floorText, phone, whatsapp, description, selectedImage
                )
            }
        }
    }

    val categories = listOf(
        "شقق", "منازل", "أراضي", "محلات", "مكاتب", "وحدات إدارية", "فيلات", "عقار تجاري", "عقار آخر"
    )
    val types = listOf("للبيع", "للإيجار")
    val districts = listOf(
        "شارع الجيش",
        "شارع الشركات",
        "الميدان الإبراهيمي",
        "كورنيش النيل",
        "دحروج",
        "حي الصفا",
        "طريق فوه",
        "المستشفى العام",
        "المناطق المحيطة"
    )
    val finishingOptions = listOf(
        "كامل التشطيب (سوبر لوكس)",
        "نصف تشطيب",
        "على الطوب الأحمر",
        "أخرى"
    )

    val imagePresets = listOf(
        "img_property_apartment" to "شقة داخلية",
        "img_property_building" to "برج سكني",
        "img_property_shop" to "محل تجاري",
        "img_property_land" to "أرض زراعية/بناء",
        "img_hero_building" to "فيلا/عمارة"
    )

    fun validateForm(): Boolean {
        validationError = null
        if (title.trim().isBlank()) {
            validationError = "يرجى كتابة عنوان واضح وجذاب للإعلان"
            return false
        }
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            validationError = "يرجى إدخال سعر صحيح للعقار"
            return false
        }
        val area = areaText.toDoubleOrNull()
        if (area == null || area <= 0) {
            validationError = "يرجى إدخال المساحة بالمتر المربع"
            return false
        }
        if (district.isBlank()) {
            validationError = "يرجى تحديد المنطقة في دسوق"
            return false
        }
        if (addressDetail.trim().isBlank()) {
            validationError = "يرجى إدخال العنوان بالتفصيل"
            return false
        }
        if (description.trim().isBlank() || description.length < 10) {
            validationError = "يرجى كتابة وصف تفصيلي للعقار لا يقل عن 10 أحرف"
            return false
        }
        if (phone.trim().isBlank()) {
            validationError = "يرجى إدخال رقم هاتف للتواصل"
            return false
        }
        return true
    }

    fun submitListing() {
        if (!validateForm()) return
        isPublishing = true

        val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
        val parsedArea = areaText.toDoubleOrNull() ?: 0.0
        val parsedRooms = roomsText.toIntOrNull() ?: 0
        val parsedBathrooms = bathroomsText.toIntOrNull() ?: 0
        val parsedFloor = floorText.toIntOrNull() ?: 1
        val finalLocation = "دسوق - $district"
        val priceUnit = if (type == "للإيجار") "ج.م / شهرياً" else "ج.م"
        val imagesJson = if (uploadedPhotoUris.isNotEmpty()) {
            uploadedPhotoUris.joinToString(separator = ",", prefix = "[", postfix = "]") { "\"$it\"" }
        } else ""

        val finalImageRes = if (coverPhotoUri != null) coverPhotoUri.toString() else selectedImage

        onSaveProperty(
            title.trim(),
            category,
            type,
            finalLocation,
            district,
            parsedPrice,
            priceUnit,
            parsedRooms,
            parsedBathrooms,
            parsedArea,
            parsedFloor,
            phone.trim(),
            whatsapp.trim().ifBlank { phone.trim() },
            description.trim(),
            finalImageRes,
            ownerName.trim(),
            isNegotiable,
            imagesJson,
            videoUrl.trim(),
            finishing,
            addressDetail.trim()
        ) {
            isPublishing = false
            showPreviewDialog = false
            Toast.makeText(context, "تم نشر العقار بنجاح في عقارات دسوق AI 🎉", Toast.LENGTH_LONG).show()
            onAddSuccess()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .padding(bottom = 90.dp)
    ) {
        // رأس الشاشة
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DesoukNavyDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AddHome,
                    contentDescription = null,
                    tint = DesoukGold,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "نشر عقار جديد",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                Text(
                    text = "الموقع الأساسي: دسوق - كفر الشيخ",
                    fontSize = 12.sp,
                    color = DesoukGoldDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // شريط الحفظ التلقائي
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFE8F5E9))
                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = lastSavedStatus ?: "الحفظ التلقائي كمسودة مفعل (Room DB)",
                    fontSize = 11.5.sp,
                    color = Color(0xFF1B5E20),
                    fontWeight = FontWeight.Bold
                )
            }

            val hasAnyData = title.isNotBlank() || addressDetail.isNotBlank() ||
                    priceText.isNotBlank() || areaText.isNotBlank() || description.isNotBlank()
            if (hasAnyData) {
                Text(
                    text = "مسح المسودة",
                    fontSize = 11.sp,
                    color = Color(0xFFC62828),
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        title = ""
                        category = "شقق"
                        type = "للبيع"
                        district = "شارع الجيش"
                        addressDetail = ""
                        priceText = ""
                        areaText = ""
                        roomsText = "3"
                        bathroomsText = "1"
                        floorText = "2"
                        description = ""
                        selectedImage = "img_property_apartment"
                        uploadedPhotoUris.clear()
                        coverPhotoUri = null
                        draftRestoredBannerVisible = false
                        lastSavedStatus = "تم مسح المسودة"
                        onClearDraft()
                        Toast.makeText(context, "تم مسح المسودة والبدء من جديد", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        // تنبيه الأخطاء
        AnimatedVisibility(visible = !validationError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = StatusErrorBg),
                border = BorderStroke(1.dp, StatusError.copy(alpha = 0.5f))
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
                        text = validationError.orEmpty(),
                        fontSize = 12.5.sp,
                        color = StatusError,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 1. عنوان الإعلان
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("عنوان الإعلان *") },
            placeholder = { Text("مثال: شقة للبيع 120م² تشطيب سوبر لوكس شارع الجيش") },
            modifier = Modifier.fillMaxWidth().testTag("input_title"),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = DesoukGold,
                unfocusedIndicatorColor = Color(0xFFE2E8F0)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 2. نوع العقار
        Text(
            text = "نوع العقار *",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DesoukNavyDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = category == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DesoukNavyDark else Color.White)
                        .border(1.dp, if (isSelected) DesoukGold else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .clickable { category = cat }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .testTag("cat_$cat")
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 12.5.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 3. نوع العملية: للبيع / للإيجار
        Text(
            text = "نوع العملية *",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DesoukNavyDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            types.forEach { t ->
                val isSelected = type == t
                val activeBg = if (t == "للبيع") BadgeForSaleRed else BadgeForRentTeal
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) activeBg else Color.White)
                        .border(1.dp, if (isSelected) activeBg else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .clickable { type = t }
                        .testTag("type_$t"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = t,
                        color = if (isSelected) Color.White else DesoukNavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 4. السعر وقابلية التفاوض
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text(if (type == "للإيجار") "الإيجار الشهري (ج.م) *" else "السعر الإجمالي (ج.م) *") },
                placeholder = { Text(if (type == "للإيجار") "مثال: 4500" else "مثال: 1250000") },
                modifier = Modifier.weight(1.2f).testTag("input_price"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )

            // قابلية التفاوض
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "قابل للتفاوض؟", fontSize = 11.sp, color = TextMuted)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .clickable { isNegotiable = !isNegotiable }
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isNegotiable) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        tint = if (isNegotiable) Color(0xFF16A34A) else Color(0xFFDC2626),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isNegotiable) "قابل للتفاوض" else "سعر نهائي",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isNegotiable) Color(0xFF16A34A) else Color(0xFFDC2626)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 5. المنطقة والعنوان بالتفصيل
        Text(
            text = "المنطقة في دسوق *",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DesoukNavyDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            districts.forEach { dist ->
                val isSelected = district == dist
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DesoukGold else Color.White)
                        .border(1.dp, if (isSelected) DesoukGoldDark else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .clickable { district = dist }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = dist,
                        color = if (isSelected) DesoukNavyDark else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = addressDetail,
            onValueChange = { addressDetail = it },
            label = { Text("العنوان بالتفصيل وأقرب علامة مميزة *") },
            placeholder = { Text("مثال: شارع الجيش بجوار بنك مصر، عمارة برج النيل") },
            modifier = Modifier.fillMaxWidth().testTag("input_address"),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = DesoukGold,
                unfocusedIndicatorColor = Color(0xFFE2E8F0)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 6. المواصفات: المساحة، الغرف، الحمامات، الدور
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = areaText,
                onValueChange = { areaText = it },
                label = { Text("المساحة (م²) *") },
                placeholder = { Text("120") },
                modifier = Modifier.weight(1f).testTag("input_area"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )

            OutlinedTextField(
                value = roomsText,
                onValueChange = { roomsText = it },
                label = { Text("الغرف") },
                placeholder = { Text("3") },
                modifier = Modifier.weight(0.9f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )

            OutlinedTextField(
                value = bathroomsText,
                onValueChange = { bathroomsText = it },
                label = { Text("الحمامات") },
                placeholder = { Text("1") },
                modifier = Modifier.weight(0.9f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )

            OutlinedTextField(
                value = floorText,
                onValueChange = { floorText = it },
                label = { Text("الدور") },
                placeholder = { Text("2") },
                modifier = Modifier.weight(0.8f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(10.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 7. حالة التشطيب
        Text(
            text = "حالة التشطيب",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = DesoukNavyDark
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            finishingOptions.forEach { opt ->
                val isSelected = finishing == opt
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) DesoukNavyLight else Color.White)
                        .border(1.dp, if (isSelected) DesoukGold else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                        .clickable { finishing = opt }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = opt,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 8. رفع صور العقار (معرض + حذف + تعيين صورة رئيسية)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "صور العقار (${uploadedPhotoUris.size} صور)",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )

                    Button(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("pick_photos_button")
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "إضافة صور", color = DesoukNavyDark, fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                    }
                }

                if (uploadedPhotoUris.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uploadedPhotoUris.forEachIndexed { idx, uri ->
                            val isCover = coverPhotoUri == uri
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        2.dp,
                                        if (isCover) DesoukGold else Color(0xFFCBD5E1),
                                        RoundedCornerShape(10.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "صورة $idx",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // شارة الصورة الرئيسية
                                if (isCover) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(DesoukGold.copy(alpha = 0.9f))
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("رئيسية", color = DesoukNavyDark, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // زر جعلها رئيسية
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable { coverPhotoUri = uri }
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("اجعلها رئيسية", color = Color.White, fontSize = 8.5.sp)
                                    }
                                }

                                // زر حذف الصورة
                                IconButton(
                                    onClick = {
                                        uploadedPhotoUris.remove(uri)
                                        if (coverPhotoUri == uri) {
                                            coverPhotoUri = uploadedPhotoUris.firstOrNull()
                                        }
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(22.dp)
                                        .background(Color.Black.copy(alpha = 0.65f), CircleShape)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "حذف", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "لم يتم اختيار صور من الهاتف بعد. يمكنك اختيار صور أو استخدام نموذج جاهز:",
                        fontSize = 11.5.sp,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        imagePresets.forEach { (presetKey, label) ->
                            val isSelected = selectedImage == presetKey && uploadedPhotoUris.isEmpty()
                            Box(
                                modifier = Modifier
                                    .size(76.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(2.dp, if (isSelected) DesoukGold else Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                                    .clickable { selectedImage = presetKey }
                            ) {
                                PropertyImageLoader(imageResName = presetKey, contentDescription = label, modifier = Modifier.fillMaxSize())
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.6f))
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(label, color = Color.White, fontSize = 8.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 9. فيديو العقار
        OutlinedTextField(
            value = videoUrl,
            onValueChange = { videoUrl = it },
            label = { Text("رابط فيديو العقار (اختياري)") },
            placeholder = { Text("رابط يوتيوب أو فيديو توضيحي للعقار") },
            leadingIcon = { Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = TextMuted) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = DesoukGold,
                unfocusedIndicatorColor = Color(0xFFE2E8F0)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 10. الوصف التفصيلي
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("الوصف التفصيلي للعقار *") },
            placeholder = { Text("اذكر تفاصيل العقار: الواجهة، العدادات، المصعد، نوع العقود والتراخيص، المزايا...") },
            modifier = Modifier.fillMaxWidth().height(120.dp).testTag("input_description"),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = DesoukGold,
                unfocusedIndicatorColor = Color(0xFFE2E8F0)
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 11. بيانات التواصل
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف *") },
                placeholder = { Text("010xxxxxxxx") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = DesoukNavyDark) },
                modifier = Modifier.weight(1f).testTag("input_phone"),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )

            OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = { Text("رقم واتساب") },
                placeholder = { Text("2010xxxxxxxx") },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = ownerName,
            onValueChange = { ownerName = it },
            label = { Text("اسم المعلن / المالك") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = DesoukNavyDark) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedIndicatorColor = DesoukGold,
                unfocusedIndicatorColor = Color(0xFFE2E8F0)
            )
        )

        Spacer(modifier = Modifier.height(20.dp))

        // أزرار المعاينة والنشر
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // زر معاينة الإعلان
            OutlinedButton(
                onClick = {
                    if (validateForm()) {
                        showPreviewDialog = true
                    }
                },
                modifier = Modifier.weight(1f).height(50.dp).testTag("preview_property_btn"),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.2.dp, DesoukNavyDark)
            ) {
                Icon(Icons.Default.Visibility, contentDescription = null, tint = DesoukNavyDark, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "معاينة الإعلان 👁️", color = DesoukNavyDark, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
            }

            // زر تأكيد ونشر الإعلان المباشر
            Button(
                onClick = { submitListing() },
                enabled = !isPublishing,
                modifier = Modifier.weight(1.3f).height(50.dp).testTag("submit_property_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isPublishing) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تأكيد ونشر الإعلان 🚀", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }

    // نافذة معاينة الإعلان قبل النشر (#8: معاينة الإعلان ثم تأكيد ونشر)
    if (showPreviewDialog) {
        val parsedPrice = priceText.toDoubleOrNull() ?: 0.0
        val priceFormatted = NumberFormat.getNumberInstance(Locale.US).format(parsedPrice)

        AlertDialog(
            onDismissRequest = { showPreviewDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Visibility, contentDescription = null, tint = DesoukGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("معاينة الإعلان كما سيظهر للجميع", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // صورة العقار
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(14.dp))
                    ) {
                        if (coverPhotoUri != null) {
                            AsyncImage(
                                model = coverPhotoUri,
                                contentDescription = title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            PropertyImageLoader(imageResName = selectedImage, contentDescription = title, modifier = Modifier.fillMaxSize())
                        }

                        // شارة العملية
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (type == "للبيع") BadgeForSaleRed else BadgeForRentTeal,
                            modifier = Modifier.padding(10.dp).align(Alignment.TopStart)
                        ) {
                            Text(text = type, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }

                    Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "$priceFormatted ج.م", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = DesoukGoldDark)
                        if (isNegotiable) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "(قابل للتفاوض)", fontSize = 11.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "دسوق - $district: $addressDetail", fontSize = 12.sp, color = TextSecondary)
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text(text = "$areaText م²", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                        Text(text = "•", color = TextMuted)
                        Text(text = "$roomsText غرف", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                        Text(text = "•", color = TextMuted)
                        Text(text = "$bathroomsText حمام", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                        Text(text = "•", color = TextMuted)
                        Text(text = "الدور $floorText", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DesoukNavyDark)
                    }

                    Text(text = "التشطيب: $finishing", fontSize = 12.sp, color = TextPrimary)
                    Text(text = "المعلن: $ownerName ($phone)", fontSize = 12.sp, color = TextPrimary)
                    Text(text = description, fontSize = 12.sp, color = TextSecondary, lineHeight = 16.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { submitListing() },
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("تأكيد ونشر الإعلان الآن 🚀", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPreviewDialog = false }) {
                    Text("تعديل البيانات", color = TextMuted)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
