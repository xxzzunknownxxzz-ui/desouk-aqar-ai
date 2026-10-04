package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PropertyEntity
import com.example.data.PropertyReviewEntity
import com.example.ui.components.PropertyImageLoader
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
fun PropertyDetailScreen(
    property: PropertyEntity,
    reviews: List<PropertyReviewEntity> = emptyList(),
    averageRating: Double? = null,
    onAddReview: (name: String, role: String, rating: Int, comment: String) -> Unit = { _, _, _, _ -> },
    onReportProperty: (propertyId: Long, title: String, reason: String, details: String) -> Unit = { _, _, _, _ -> },
    onBack: () -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    onDelete: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var showAddReviewDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .verticalScroll(scrollState)
            .padding(bottom = 24.dp)
    ) {
        // Hero Image with Top Overlay Controls
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
        ) {
            PropertyImageLoader(
                imageResName = property.imageResName,
                contentDescription = property.title,
                modifier = Modifier.fillMaxSize()
            )

            // Top gradient overlay for buttons visibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent)
                        )
                    )
            )

            // Top Bar: Back, Share, Favorite
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 36.dp, start = 16.dp, end = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.85f), CircleShape)
                        .testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = DesoukNavyDark
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Share Button
                    IconButton(
                        onClick = { shareProperty(context, property) },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                            .testTag("detail_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "مشاركة",
                            tint = DesoukNavyDark
                        )
                    }

                    // Favorite Button
                    IconButton(
                        onClick = { onFavoriteToggle(property.id, !property.isFavorite) },
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color.White.copy(alpha = 0.85f), CircleShape)
                            .testTag("detail_favorite_button")
                    ) {
                        Icon(
                            imageVector = if (property.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (property.isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة",
                            tint = if (property.isFavorite) BadgeForSaleRed else DesoukNavyDark
                        )
                    }
                }
            }

            // Bottom Badges on Image
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DesoukNavyDark.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = property.category,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Type badge (للبيع / للإيجار)
                val isForSale = property.type == "للبيع"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isForSale) BadgeForSaleRed else BadgeForRentTeal)
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = property.type,
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Details Container
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Price & Title
            val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(property.price.toLong())
            Text(
                text = "$formattedPrice ${property.priceUnit}",
                color = DesoukGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = property.title,
                color = DesoukNavyDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Location with Maps Link
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DesoukGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = property.location,
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }

                // Map button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DesoukNavyDark.copy(alpha = 0.08f))
                        .clickable { openMap(context, property.latitude, property.longitude, property.title) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("open_map_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = DesoukNavyDark,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "الخريطة",
                        color = DesoukNavyDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Quick Rating & Reliability Badge
            val displayRating = averageRating ?: if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 4.8
            val reviewCount = if (reviews.isNotEmpty()) reviews.size else 0

            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFFFF9E6))
                    .border(1.dp, DesoukGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = DesoukGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f", displayRating),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = DesoukNavyDark
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (reviewCount > 0) "($reviewCount تقييم)" else "(تقييم جديد)",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "تقييمات ومعاينات موثقة",
                        fontSize = 11.sp,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Key Specs Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SpecCard(
                    title = "المساحة",
                    value = "${property.area.toInt()} م²",
                    icon = Icons.Default.SquareFoot,
                    modifier = Modifier.weight(1f)
                )

                if (property.rooms > 0) {
                    SpecCard(
                        title = "الغرف",
                        value = "${property.rooms}",
                        icon = Icons.Default.Bed,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (property.bathrooms > 0) {
                    SpecCard(
                        title = "الحمامات",
                        value = "${property.bathrooms}",
                        icon = Icons.Default.Bathtub,
                        modifier = Modifier.weight(1f)
                    )
                }

                if (property.floor > 0) {
                    SpecCard(
                        title = "الطابق",
                        value = "${property.floor}",
                        icon = Icons.Default.Layers,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Description Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "تفاصيل العقار",
                        color = DesoukNavyDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (property.description.isNotBlank()) property.description else "عقار ممتاز بموقع استراتيجي في مدينة دسوق.",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Reviews & Ratings Section (نظام التقييمات والموثوقية)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("property_reviews_section"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Header with title and Add Review button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "آراء وتقييمات العقار",
                                    color = DesoukNavyDark,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(DesoukNavyDark.copy(alpha = 0.08f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "${reviews.size}",
                                        color = DesoukNavyDark,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "تجارب حقيقية من زوار ومعاينين",
                                    color = TextMuted,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFE3F2FD))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "سحابي Firestore ☁️",
                                        color = Color(0xFF1565C0),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showAddReviewDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_review_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = DesoukNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تقييم العقار",
                                color = DesoukNavyDark,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Rating Summary Box
                    val avg = averageRating ?: if (reviews.isNotEmpty()) reviews.map { it.rating }.average() else 5.0
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(DesoukNavyDark.copy(alpha = 0.04f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format(Locale.US, "%.1f", avg),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = DesoukNavyDark
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row {
                                    val roundedAvg = avg.toInt().coerceIn(1, 5)
                                    for (i in 1..5) {
                                        Icon(
                                            imageVector = if (i <= roundedAvg) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = DesoukGold,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (reviews.isEmpty()) "بانتظار أول تقييم ميداني" else "بناءً على ${reviews.size} تقييم موثق",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "موثوق 100%",
                                color = Color(0xFF2E7D32),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Reviews List
                    if (reviews.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "لا توجد مراجعات مضافة بعد",
                                    color = TextMuted,
                                    fontSize = 13.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "كن أول من يشارك تجربته مع هذا العقار لمساعدة أهالي دسوق!",
                                    color = DesoukNavyDark,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            reviews.forEach { review ->
                                ReviewItemCard(review = review)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Contact & Action Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "التواصل مع صاحب العقار",
                        color = DesoukNavyDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Phone Call Button
                        Button(
                            onClick = { dialPhone(context, property.phone) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("call_owner_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Call, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "اتصال هاتفي", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }

                        // WhatsApp Chat Button
                        Button(
                            onClick = {
                                openWhatsAppChat(
                                    context,
                                    property.whatsapp,
                                    "مرحباً، أستفسر عن إعلان: ${property.title} في ${property.location}"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("whatsapp_owner_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(text = "واتساب  WhatsApp", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // User added delete option
            if (property.isUserAdded) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        onDelete(property.id)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("delete_property_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = BadgeForSaleRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null, tint = BadgeForSaleRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "حذف إعلاني", color = BadgeForSaleRed, fontWeight = FontWeight.Bold)
                }
            }

            // زر الإبلاغ عن الإعلان
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { showReportDialog = true }
                    .padding(vertical = 8.dp)
                    .testTag("report_property_button"),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "الإبلاغ عن الإعلان ⚠️",
                    fontSize = 12.sp,
                    color = Color(0xFFEF4444),
                    fontWeight = FontWeight.Medium
                )
            }

            // Review Dialog Modal
            if (showAddReviewDialog) {
                AddReviewDialog(
                    onDismiss = { showAddReviewDialog = false },
                    onSubmit = { name, role, rating, comment ->
                        onAddReview(name, role, rating, comment)
                    }
                )
            }

            // Report Property Dialog
            if (showReportDialog) {
                ReportPropertyDialog(
                    propertyTitle = property.title,
                    onDismiss = { showReportDialog = false },
                    onSubmit = { reason, details ->
                        onReportProperty(property.id, property.title, reason, details)
                        android.widget.Toast.makeText(context, "تم إرسال البلاغ للإدارة للمراجعة", android.widget.Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

@Composable
fun SpecCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = DesoukGold,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                color = DesoukNavyDark,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                color = TextMuted,
                fontSize = 10.sp
            )
        }
    }
}

private fun dialPhone(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun openWhatsAppChat(context: Context, phone: String, message: String) {
    try {
        val cleanPhone = phone.replace("+", "").replace(" ", "").trim()
        val uri = Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {}
}

private fun openMap(context: Context, lat: Double, lng: Double, label: String) {
    try {
        val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(label)})")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (_: Exception) {
        try {
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
        } catch (_: Exception) {}
    }
}

private fun shareProperty(context: Context, property: PropertyEntity) {
    com.example.data.ReferralAndShareManager.shareProperty(context, property)
}

@Composable
fun ReviewItemCard(
    review: PropertyReviewEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DesoukNavyDark),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.userName.take(1).ifBlank { "ع" },
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = review.userName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DesoukNavyDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "موثق",
                                tint = Color(0xFF2E7D32),
                                modifier = Modifier.size(13.dp)
                            )
                        }
                        Text(
                            text = review.userRole,
                            fontSize = 10.sp,
                            color = TextMuted
                        )
                    }
                }

                // Stars
                Row {
                    for (i in 1..5) {
                        Icon(
                            imageVector = if (i <= review.rating) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = null,
                            tint = DesoukGold,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.comment,
                fontSize = 12.sp,
                color = TextPrimary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(6.dp))
            val dateStr = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(review.timestamp))
            Text(
                text = dateStr,
                fontSize = 10.sp,
                color = TextMuted
            )
        }
    }
}

@Composable
fun AddReviewDialog(
    onDismiss: () -> Unit,
    onSubmit: (name: String, role: String, rating: Int, comment: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("زائر معاينة") }
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val roles = listOf("زائر معاينة", "مشتري موثق", "مستأجر سابق", "مهتم بالعقار")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.RateReview,
                    contentDescription = null,
                    tint = DesoukGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "تقييم العقار وتجربة المعاينة",
                    color = DesoukNavyDark,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // شريط التخزين السحابي في Firestore
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "يتم حفظ التقييم والمراجعة سحابياً في Firestore مباشرة ☁️",
                        fontSize = 11.sp,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Interactive Stars
                Text(
                    text = "حدد تقييمك العام:",
                    fontSize = 12.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (star in 1..5) {
                        IconButton(
                            onClick = { rating = star },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = if (star <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Star $star",
                                tint = DesoukGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
                val ratingLabel = when (rating) {
                    5 -> "ممتاز جداً ⭐⭐⭐⭐⭐"
                    4 -> "جيد جداً ⭐⭐⭐⭐"
                    3 -> "جيد ⭐⭐⭐"
                    2 -> "مقبول ⭐⭐"
                    else -> "يحتاج تحسين ⭐"
                }
                Text(
                    text = ratingLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Reviewer Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("الاسم أو اللقب (اختياري)") },
                    placeholder = { Text("مثال: مهندس أحمد، أبو عمر...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("review_name_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesoukGold,
                        unfocusedBorderColor = Color(0xFFD0D7DE)
                    ),
                    singleLine = true
                )

                // Role Chips
                Text(
                    text = "صفة تجربتك مع العقار:",
                    fontSize = 12.sp,
                    color = TextPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    roles.forEach { role ->
                        val isSelected = selectedRole == role
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) DesoukNavyDark else DesoukNavyDark.copy(alpha = 0.08f))
                                .clickable { selectedRole = role }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = role,
                                color = if (isSelected) Color.White else DesoukNavyDark,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                // Comment
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("تفاصيل تجربتك ورأيك") },
                    placeholder = { Text("اكتب انطباعك عن الموقع، التشطيب، الواجهة، أمانة المعلن، السعر...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("review_comment_input"),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesoukGold,
                        unfocusedBorderColor = Color(0xFFD0D7DE)
                    ),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (comment.isNotBlank()) {
                        onSubmit(
                            name.ifBlank { "مواطن من دسوق" },
                            selectedRole,
                            rating,
                            comment
                        )
                        onDismiss()
                    }
                },
                enabled = comment.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_review_button")
            ) {
                Text("نشر التقييم", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

/**
 * نافذة الإبلاغ عن الإعلانات المخالفة لحماية أمان المنصة والمستخدمين
 */
@Composable
fun ReportPropertyDialog(
    propertyTitle: String,
    onDismiss: () -> Unit,
    onSubmit: (reason: String, details: String) -> Unit
) {
    val reasons = listOf(
        "إعلان وهمي",
        "سعر غير صحيح",
        "محتوى مخالف",
        "عقار غير موجود",
        "احتيال",
        "أخرى"
    )
    var selectedReason by remember { mutableStateOf(reasons.first()) }
    var details by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الإبلاغ عن الإعلان",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = DesoukNavyDark
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "عقار: $propertyTitle",
                    fontSize = 12.5.sp,
                    color = TextPrimary,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "اختر سبب البلاغ:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )

                reasons.forEach { reason ->
                    val isSelected = selectedReason == reason
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) Color(0xFFFEE2E2) else Color(0xFFF8FAFC))
                            .border(1.dp, if (isSelected) Color(0xFFEF4444) else Color(0xFFE2E8F0), RoundedCornerShape(8.dp))
                            .clickable { selectedReason = reason }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isSelected) "●  $reason" else "○  $reason",
                            fontSize = 13.sp,
                            color = if (isSelected) Color(0xFFB91C1C) else TextPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text("تفاصيل إضافية (اختياري)") },
                    placeholder = { Text("اكتب أي معلومات توضح سبب البلاغ...") },
                    modifier = Modifier.fillMaxWidth().height(90.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFEF4444),
                        unfocusedBorderColor = Color(0xFFD0D7DE)
                    ),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(selectedReason, details.trim())
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("submit_report_button")
            ) {
                Text("إرسال البلاغ 📤", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = TextMuted)
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

