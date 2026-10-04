package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistrictPickerSheet(
    currentDistrict: String,
    onDistrictSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val districts = listOf(
        "دسوق - كفر الشيخ",
        "دسوق - شارع الجيش",
        "دسوق - شارع الشركات",
        "دسوق - الميدان الإبراهيمي",
        "دسوق - كورنيش النيل",
        "دسوق - دحروج",
        "دسوق - حي الصفا",
        "دسوق - طريق فوه",
        "دسوق - المستشفى العام",
        "دسوق - المناطق المحيطة"
    )

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
                Text(
                    text = "اختر المنطقة في دسوق",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                Box(modifier = Modifier.size(40.dp))
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                items(districts) { district ->
                    val isSelected = district == currentDistrict ||
                            (district == "دسوق - كفر الشيخ" && (currentDistrict == "دسوق" || currentDistrict.isEmpty()))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onDistrictSelected(district)
                                onDismiss()
                            }
                            .background(if (isSelected) DesoukGold.copy(alpha = 0.12f) else Color.Transparent)
                            .padding(horizontal = 20.dp, vertical = 14.dp)
                            .testTag("district_option_$district"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = DesoukGold,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Box(modifier = Modifier.size(20.dp))
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = district,
                                fontSize = 15.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) DesoukNavyDark else TextPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFF1F5F9)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = DesoukNavyDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
