package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.PropertyEntity
import com.example.ui.theme.BadgeForRentTeal
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DesoukPropertyCard(
    property: PropertyEntity,
    onDetailsClick: (PropertyEntity) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    modifier: Modifier = Modifier.width(205.dp)
) {
    // حركة التصغير والتفاعل للبطاقة ككل
    val cardInteractionSource = remember { MutableInteractionSource() }
    val isCardPressed by cardInteractionSource.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (isCardPressed) 0.965f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "card_scale_${property.id}"
    )

    // حركة زر المفضلة
    val favInteractionSource = remember { MutableInteractionSource() }
    val isFavPressed by favInteractionSource.collectIsPressedAsState()
    val favScale by animateFloatAsState(
        targetValue = if (isFavPressed) 0.84f else 1.0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 500f),
        label = "fav_scale_${property.id}"
    )

    // حركة زر التفاصيل
    val btnInteractionSource = remember { MutableInteractionSource() }
    val isBtnPressed by btnInteractionSource.collectIsPressedAsState()
    val btnScale by animateFloatAsState(
        targetValue = if (isBtnPressed) 0.94f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "btn_scale_${property.id}"
    )

    Card(
        modifier = modifier
            .scale(cardScale)
            .clip(RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = cardInteractionSource,
                indication = ripple(bounded = true, color = DesoukNavyDark.copy(alpha = 0.12f))
            ) { onDetailsClick(property) }
            .testTag("property_card_${property.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // صورة العقار مع الشارات وأيقونة المفضلة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(125.dp)
            ) {
                PropertyImageLoader(
                    imageResName = property.imageResName,
                    contentDescription = property.title,
                    modifier = Modifier.fillMaxSize()
                )

                // في RTL:
                // Start = أعلى اليمين: شارة حالة العقار (للبيع / للإيجار)
                val isForSale = property.type == "للبيع"
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(top = 8.dp, start = 8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isForSale) BadgeForSaleRed else BadgeForRentTeal)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = property.type,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // End = أعلى اليسار: زر المفضلة (القلب) لحفظ العقار في Room Database
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .scale(favScale)
                        .size(36.dp)
                        .shadow(elevation = 3.dp, shape = CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable(
                            interactionSource = favInteractionSource,
                            indication = ripple(bounded = true, color = BadgeForSaleRed.copy(alpha = 0.25f))
                        ) { onFavoriteToggle(property.id, !property.isFavorite) }
                        .testTag("favorite_button_${property.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (property.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (property.isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة",
                        tint = if (property.isFavorite) BadgeForSaleRed else DesoukNavyDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // تفاصيل العقار
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                // اسم العقار
                Text(
                    text = property.title,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(4.dp))

                // الموقع
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = property.location,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // المواصفات (عدد الغرف - عدد الحمامات - المساحة)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // عدد الغرف
                    if (property.rooms > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bed,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${property.rooms} غرف",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // عدد الحمامات
                    if (property.bathrooms > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bathtub,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${property.bathrooms} حمام",
                                color = TextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // المساحة
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SquareFoot,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${property.area.toInt()} م²",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // السعر
                val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(property.price.toLong())
                Text(
                    text = "$formattedPrice ${property.priceUnit}",
                    color = DesoukNavyDark,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Start,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // زر "التفاصيل" مع حركة تصغير وضغط وتأثير Ripple ذهبي أنيق
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .scale(btnScale)
                        .clip(RoundedCornerShape(15.dp))
                        .background(DesoukNavyDark)
                        .clickable(
                            interactionSource = btnInteractionSource,
                            indication = ripple(bounded = true, color = DesoukGold.copy(alpha = 0.35f))
                        ) { onDetailsClick(property) }
                        .testTag("details_button_${property.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "التفاصيل  ‹",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PropertyImageLoader(
    imageResName: String,
    contentDescription: String?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val fallbackDrawable = when {
        imageResName.contains("land") -> R.drawable.img_property_land
        imageResName.contains("shop") -> R.drawable.img_property_shop
        imageResName.contains("apartment") -> R.drawable.img_property_apartment
        imageResName.contains("building") -> R.drawable.img_property_building
        imageResName.contains("hero") -> R.drawable.img_hero_building
        else -> R.drawable.img_property_building
    }

    if (imageResName.startsWith("content://") || imageResName.startsWith("file://") || imageResName.startsWith("http")) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(imageResName)
                .crossfade(true)
                .error(fallbackDrawable)
                .placeholder(fallbackDrawable)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        val resId = when (imageResName) {
            "img_property_land" -> R.drawable.img_property_land
            "img_property_shop" -> R.drawable.img_property_shop
            "img_property_apartment" -> R.drawable.img_property_apartment
            "img_property_building" -> R.drawable.img_property_building
            "img_hero_building" -> R.drawable.img_hero_building
            else -> fallbackDrawable
        }
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(resId)
                .crossfade(true)
                .size(400, 250) // تقليص الدقة بدقة لتناسب حجم بطاقة العرض دون إهدار الذاكرة
                .error(fallbackDrawable)
                .placeholder(fallbackDrawable)
                .memoryCacheKey("prop_thumb_${imageResName}")
                .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                .build(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}
