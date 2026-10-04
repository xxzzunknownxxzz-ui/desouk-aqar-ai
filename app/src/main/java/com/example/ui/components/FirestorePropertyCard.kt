package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.FirestoreProperty
import com.example.ui.theme.BadgeForRentTeal
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

/**
 * كرت عرض العقار المسترجع من قاعدة بيانات Firebase Firestore
 */
@Composable
fun FirestorePropertyCard(
    property: FirestoreProperty,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val imageResId = rememberPropertyDrawable(context, property.imageResName)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.965f else 1.0f,
        animationSpec = spring(dampingRatio = 0.75f, stiffness = 400f),
        label = "firestore_card_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, color = DesoukNavyDark.copy(alpha = 0.12f))
            ) { onClick() }
            .testTag("firestore_property_${property.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // صورة العقار مع شارة Firestore وشارة البيع/الإيجار
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = property.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentScale = ContentScale.Crop
                )

                // تدرج داكن ناعم
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.4f),
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )

                // شارة مصدر البيانات Firebase Firestore (أعلى اليمين في RTL)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DesoukNavyDark.copy(alpha = 0.88f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DesoukGold),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "قاعدة Firestore",
                            tint = DesoukGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Firestore Cloud",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // شارة نوع المعاملة (بيع أو إيجار)
                val badgeColor = if (property.type.contains("بيع")) BadgeForSaleRed else BadgeForRentTeal
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                ) {
                    Text(
                        text = property.type,
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // السعر في أسفل الصورة
                Text(
                    text = "${property.price.toLong()} ${property.priceUnit}",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                )
            }

            // تفاصيل العقار
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = property.title,
                    color = DesoukNavyDark,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = DesoukGold,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${property.district} • ${property.location}",
                        color = TextMuted,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // معلومات سريعة: المساحة والغرف
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (property.area > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SquareFoot,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${property.area.toInt()} م²",
                                color = TextPrimary,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (property.rooms > 0) {
                        Text(
                            text = "${property.rooms} غرف",
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (property.floor > 0) {
                        Text(
                            text = "الدور ${property.floor}",
                            color = TextPrimary,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * تحديد معرف الصورة المتوافق
 */
private fun rememberPropertyDrawable(context: android.content.Context, resName: String): Int {
    val identifier = context.resources.getIdentifier(resName, "drawable", context.packageName)
    return if (identifier != 0) identifier else R.drawable.img_property_apartment
}
