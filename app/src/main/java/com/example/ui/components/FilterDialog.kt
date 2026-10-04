package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RotateLeft
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun FilterDialog(
    selectedCategory: String?,
    selectedType: String?,
    selectedRooms: Int?,
    onApplyFilter: (category: String?, type: String?, rooms: Int?, maxPrice: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var tempCategory by remember { mutableStateOf(selectedCategory) }
    var tempType by remember { mutableStateOf(selectedType) }
    var tempRooms by remember { mutableStateOf(selectedRooms) }
    var maxPriceRange by remember { mutableStateOf(5000000f) }

    val categories = listOf("الكل", "شقق للبيع والايجار", "منازل للبيع", "محلات للبيع والايجار", "وحدات إدارية للبيع والايجار", "أراضي")
    val types = listOf("الكل", "للبيع", "للإيجار")
    val roomsList = listOf(null to "الكل", 1 to "1", 2 to "2", 3 to "3", 4 to "4", 5 to "+5")

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
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
                Text(
                    text = "تصفية العقارات",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                IconButton(onClick = {
                    tempCategory = null
                    tempType = null
                    tempRooms = null
                    maxPriceRange = 5000000f
                }) {
                    Icon(imageVector = Icons.Default.RotateLeft, contentDescription = "إعادة ضبط", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. النوع (بيع / إيجار)
            Text(
                text = "نوع العرض",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                types.forEach { typeOption ->
                    val isSelected = (typeOption == "الكل" && tempType == null) || (tempType == typeOption)
                    FilterChipItem(
                        text = typeOption,
                        isSelected = isSelected,
                        onClick = {
                            tempType = if (typeOption == "الكل") null else typeOption
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. التصنيف
            Text(
                text = "التصنيف العقاري",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { catOption ->
                    val isSelected = (catOption == "الكل" && tempCategory == null) ||
                            (tempCategory == catOption) ||
                            (catOption != "الكل" && tempCategory != null && (
                                (catOption.contains("شقق") && tempCategory!!.contains("شقق")) ||
                                (catOption.contains("منازل") && tempCategory!!.contains("منازل")) ||
                                (catOption.contains("محلات") && tempCategory!!.contains("محلات")) ||
                                ((catOption.contains("إداري") || catOption.contains("مكاتب")) && (tempCategory!!.contains("إداري") || tempCategory!!.contains("مكاتب"))) ||
                                ((catOption.contains("أرض") || catOption.contains("أراضي")) && (tempCategory!!.contains("أرض") || tempCategory!!.contains("أراضي")))
                            ))
                    FilterChipItem(
                        text = catOption,
                        isSelected = isSelected,
                        onClick = {
                            tempCategory = if (catOption == "الكل") null else catOption
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. عدد الغرف
            Text(
                text = "عدد الغرف",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                roomsList.forEach { (count, label) ->
                    val isSelected = tempRooms == count
                    FilterChipItem(
                        text = label,
                        isSelected = isSelected,
                        onClick = { tempRooms = count }
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 4. أقصى سعر
            val formattedPrice = NumberFormat.getNumberInstance(Locale.US).format(maxPriceRange.toLong())
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الحد الأقصى للسعر:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                Text(
                    text = "$formattedPrice ج.م",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukGold
                )
            }
            androidx.compose.material3.Slider(
                value = maxPriceRange,
                onValueChange = { maxPriceRange = it },
                valueRange = 1000f..10000000f,
                colors = SliderDefaults.colors(
                    thumbColor = DesoukGold,
                    activeTrackColor = DesoukGold,
                    inactiveTrackColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onApplyFilter(tempCategory, tempType, tempRooms, maxPriceRange.toDouble())
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("apply_filter_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "تطبيق الفلتر", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(0.6f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "إلغاء", fontSize = 15.sp, color = TextMuted)
                }
            }
        }
    }
}

@Composable
fun FilterChipItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) DesoukNavyDark else Color(0xFFF1F5F9))
            .border(
                1.dp,
                if (isSelected) DesoukNavyDark else Color(0xFFE2E8F0),
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else TextPrimary,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
