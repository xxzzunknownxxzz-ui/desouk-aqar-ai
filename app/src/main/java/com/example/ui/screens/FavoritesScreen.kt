package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.PropertyEntity
import com.example.ui.components.DesoukPropertyCard
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted

@Composable
fun FavoritesScreen(
    favorites: List<PropertyEntity>,
    onPropertyClick: (PropertyEntity) -> Unit,
    onFavoriteToggle: (Long, Boolean) -> Unit,
    onExploreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .padding(16.dp)
            .padding(bottom = 80.dp)
    ) {
        // Screen Title
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(BadgeForSaleRed.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = BadgeForSaleRed,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.size(12.dp))
            Column {
                Text(
                    text = "العقارات المفضلة",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                Text(
                    text = "${favorites.size} عقار محفوظ في قائمتك",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (favorites.isEmpty()) {
            // Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "لا توجد عقارات مفضلة حالياً",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "اضغط على رمز القلب عند تصفح العقارات لحفظها في هذه القائمة للرجوع إليها لاحقاً.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onExploreClick,
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("explore_properties_from_favorites")
                    ) {
                        Text(text = "تصفح أحدث العقارات", color = DesoukGold, fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = favorites,
                    key = { it.id }
                ) { property ->
                    DesoukPropertyCard(
                        property = property,
                        onDetailsClick = onPropertyClick,
                        onFavoriteToggle = onFavoriteToggle,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
