package com.example.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark

/**
 * أيقونات ثلاثية الأبعاد احترافية بتأثيرات الإضاءة والعمق الحقيقي (3D Realistic Icons)
 * تتضمن ظلال أرضية، أسطح مائلة، تدرجات إضاءة علوية وجانبية، وتوهجات زجاجية ولمعان حقيقي.
 */

// 1. أيقونة المنزل 3D (الرئيسية)
@Composable
fun Icon3DHome(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // 1. Soft 3D Ground Shadow
        drawOval(
            color = if (isSelected) Color(0x350D274A) else Color(0x18000000),
            topLeft = Offset(w * 0.15f, h * 0.84f),
            size = Size(w * 0.70f, h * 0.14f)
        )

        // Palette
        val roofFront = if (isSelected) listOf(Color(0xFFFFD54F), Color(0xFFFFA000)) else listOf(Color(0xFF90A4AE), Color(0xFF607D8B))
        val roofSide = if (isSelected) listOf(Color(0xFFFFB300), Color(0xFFFF8F00)) else listOf(Color(0xFF78909C), Color(0xFF455A64))
        val wallFront = if (isSelected) listOf(Color(0xFF1E88E5), Color(0xFF1565C0)) else listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5))
        val wallSide = if (isSelected) listOf(Color(0xFF1565C0), Color(0xFF0D47A1)) else listOf(Color(0xFFB0BEC5), Color(0xFF90A4AE))

        // Left/Front Wall
        val frontWall = Path().apply {
            moveTo(w * 0.22f, h * 0.48f)
            lineTo(w * 0.65f, h * 0.48f)
            lineTo(w * 0.65f, h * 0.82f)
            lineTo(w * 0.22f, h * 0.82f)
            close()
        }
        drawPath(
            path = frontWall,
            brush = Brush.verticalGradient(wallFront, startY = h * 0.48f, endY = h * 0.82f)
        )

        // Right Wall (Isometric depth side)
        val rightWall = Path().apply {
            moveTo(w * 0.65f, h * 0.48f)
            lineTo(w * 0.82f, h * 0.38f)
            lineTo(w * 0.82f, h * 0.72f)
            lineTo(w * 0.65f, h * 0.82f)
            close()
        }
        drawPath(
            path = rightWall,
            brush = Brush.verticalGradient(wallSide, startY = h * 0.38f, endY = h * 0.82f)
        )

        // Front Roof (Gable Triangle)
        val roofTriangle = Path().apply {
            moveTo(w * 0.15f, h * 0.48f)
            lineTo(w * 0.43f, h * 0.16f)
            lineTo(w * 0.72f, h * 0.48f)
            close()
        }
        drawPath(
            path = roofTriangle,
            brush = Brush.verticalGradient(roofFront, startY = h * 0.16f, endY = h * 0.48f)
        )

        // Side Roof (3D sloping plane)
        val sideRoof = Path().apply {
            moveTo(w * 0.43f, h * 0.16f)
            lineTo(w * 0.65f, h * 0.08f)
            lineTo(w * 0.88f, h * 0.38f)
            lineTo(w * 0.72f, h * 0.48f)
            close()
        }
        drawPath(
            path = sideRoof,
            brush = Brush.verticalGradient(roofSide, startY = h * 0.08f, endY = h * 0.48f)
        )

        // Specular highlight line along roof ridge
        drawLine(
            color = Color.White.copy(alpha = 0.85f),
            start = Offset(w * 0.43f, h * 0.16f),
            end = Offset(w * 0.65f, h * 0.08f),
            strokeWidth = 2f,
            cap = StrokeCap.Round
        )

        // Front Door (3D recessed entrance)
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = if (isSelected) listOf(Color(0xFF0D47A1), Color(0xFF0A2240)) else listOf(Color(0xFF546E7A), Color(0xFF37474F)),
                startY = h * 0.60f,
                endY = h * 0.82f
            ),
            topLeft = Offset(w * 0.38f, h * 0.60f),
            size = Size(w * 0.16f, h * 0.22f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        // Door Knob (Golden dot)
        drawCircle(
            color = Color(0xFFFFD54F),
            radius = 1.5f,
            center = Offset(w * 0.50f, h * 0.72f)
        )

        // Front Window (Glowing warm glass)
        drawRoundRect(
            color = if (isSelected) Color(0xFFFFF9C4) else Color.White,
            topLeft = Offset(w * 0.36f, h * 0.30f),
            size = Size(w * 0.14f, h * 0.14f),
            cornerRadius = CornerRadius(2f, 2f)
        )
    }
}

// 2. أيقونة البحث 3D (عدسة مجسمة بزجاج عاكس ومقبض أسطواني)
@Composable
fun Icon3DSearch(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Ground shadow for handle and lens
        drawOval(
            color = if (isSelected) Color(0x300D274A) else Color(0x18000000),
            topLeft = Offset(w * 0.20f, h * 0.78f),
            size = Size(w * 0.65f, h * 0.16f)
        )

        // Colors
        val rimGradient = if (isSelected) listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)) else listOf(Color(0xFF90A4AE), Color(0xFF546E7A))
        val glassGradient = if (isSelected) listOf(Color(0x9964B5F6), Color(0x331976D2)) else listOf(Color(0x70ECEFF1), Color(0x20B0BEC5))
        val handleGradient = if (isSelected) listOf(Color(0xFFFFD54F), Color(0xFFFFA000), Color(0xFFE65100)) else listOf(Color(0xFFB0BEC5), Color(0xFF78909C), Color(0xFF455A64))

        val centerLens = Offset(w * 0.42f, h * 0.40f)
        val radiusLens = w * 0.28f

        // Handle (3D cylinder oriented towards bottom right)
        val handleStart = Offset(w * 0.62f, h * 0.60f)
        val handleEnd = Offset(w * 0.85f, h * 0.83f)

        // Handle Shadow
        drawLine(
            color = Color.Black.copy(alpha = 0.25f),
            start = Offset(handleStart.x + 1f, handleStart.y + 2f),
            end = Offset(handleEnd.x + 1f, handleEnd.y + 2f),
            strokeWidth = 7f,
            cap = StrokeCap.Round
        )

        // Handle Body
        drawLine(
            brush = Brush.linearGradient(handleGradient, start = handleStart, end = handleEnd),
            start = handleStart,
            end = handleEnd,
            strokeWidth = 6.5f,
            cap = StrokeCap.Round
        )

        // Golden Handle Ferrule
        drawLine(
            color = if (isSelected) Color(0xFFFFD54F) else Color(0xFFECEFF1),
            start = handleStart,
            end = Offset(handleStart.x + (handleEnd.x - handleStart.x) * 0.25f, handleStart.y + (handleEnd.y - handleStart.y) * 0.25f),
            strokeWidth = 7.5f,
            cap = StrokeCap.Round
        )

        // Lens Outer Metallic Rim (Thick with 3D gradient)
        drawCircle(
            brush = Brush.sweepGradient(
                colors = listOf(rimGradient[0], rimGradient[1], rimGradient[0]),
                center = centerLens
            ),
            radius = radiusLens,
            center = centerLens,
            style = Stroke(width = 4.5f)
        )

        // Inner Glass Lens (Transparent 3D bulb)
        drawCircle(
            brush = Brush.radialGradient(
                colors = glassGradient,
                center = Offset(centerLens.x - radiusLens * 0.3f, centerLens.y - radiusLens * 0.3f),
                radius = radiusLens
            ),
            radius = radiusLens - 2.5f,
            center = centerLens
        )

        // Curved 3D Glare Reflection (Specular crescent)
        val arcPath = Path().apply {
            addArc(
                oval = Rect(centerLens.x - radiusLens + 4f, centerLens.y - radiusLens + 4f, centerLens.x + radiusLens - 4f, centerLens.y + radiusLens - 4f),
                startAngleDegrees = 200f,
                sweepAngleDegrees = 90f
            )
        }
        drawPath(
            path = arcPath,
            color = Color.White.copy(alpha = 0.85f),
            style = Stroke(width = 2.5f, cap = StrokeCap.Round)
        )
    }
}

// 3. أيقونة المحادثات 3D (فقاعتا حوار مجسمتان مع انعكاس ضوئي وظلال تراكب)
@Composable
fun Icon3DChat(
    isSelected: Boolean,
    badgeCount: Int = 0,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Contact shadow
            drawOval(
                color = if (isSelected) Color(0x300D274A) else Color(0x18000000),
                topLeft = Offset(w * 0.12f, h * 0.82f),
                size = Size(w * 0.76f, h * 0.14f)
            )

            // Primary Bubble (Front/Left)
            val bubbleLeft = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.10f, h * 0.22f, w * 0.68f, h * 0.68f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                )
                // Tail
                moveTo(w * 0.20f, h * 0.64f)
                lineTo(w * 0.12f, h * 0.82f)
                lineTo(w * 0.34f, h * 0.68f)
                close()
            }

            // Secondary Bubble (Behind/Right)
            val bubbleRight = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.36f, h * 0.10f, w * 0.88f, h * 0.52f),
                        cornerRadius = CornerRadius(12f, 12f)
                    )
                )
                // Tail
                moveTo(w * 0.78f, h * 0.50f)
                lineTo(w * 0.88f, h * 0.66f)
                lineTo(w * 0.68f, h * 0.52f)
                close()
            }

            // Draw Behind Bubble
            drawPath(
                path = bubbleRight,
                brush = Brush.verticalGradient(
                    colors = if (isSelected) listOf(Color(0xFFFFD54F), Color(0xFFFFA000)) else listOf(Color(0xFFB0BEC5), Color(0xFF78909C)),
                    startY = h * 0.10f,
                    endY = h * 0.66f
                )
            )
            // Behind bubble highlight
            drawPath(
                path = bubbleRight,
                color = Color.White.copy(alpha = 0.40f),
                style = Stroke(width = 1.5f)
            )

            // Draw Front Primary Bubble with 3D gradient
            drawPath(
                path = bubbleLeft,
                brush = Brush.verticalGradient(
                    colors = if (isSelected) listOf(Color(0xFF1E88E5), Color(0xFF1565C0)) else listOf(Color(0xFF78909C), Color(0xFF455A64)),
                    startY = h * 0.22f,
                    endY = h * 0.82f
                )
            )

            // Front Bubble Specular Edge Highlight (Light from top-left)
            drawPath(
                path = bubbleLeft,
                color = Color.White.copy(alpha = 0.70f),
                style = Stroke(width = 1.8f)
            )

            // Three 3D White dots inside the primary bubble
            val dotY = h * 0.45f
            val dotRadius = 2.2f
            for (i in 0..2) {
                val dotX = w * (0.28f + i * 0.11f)
                drawCircle(color = Color.White.copy(alpha = 0.95f), radius = dotRadius, center = Offset(dotX, dotY))
            }
        }

        // Badge if available
        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-2).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(BadgeForSaleRed)
                    .shadow(elevation = 3.dp, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toString(),
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// 4. أيقونة الحساب الشخصي 3D (مجسم شخصي ناعم مع لمعان كروي)
@Composable
fun Icon3DProfile(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Ground contact shadow
        drawOval(
            color = if (isSelected) Color(0x300D274A) else Color(0x18000000),
            topLeft = Offset(w * 0.15f, h * 0.84f),
            size = Size(w * 0.70f, h * 0.14f)
        )

        val headCenter = Offset(w * 0.50f, h * 0.32f)
        val headRadius = w * 0.20f

        val primaryGrad = if (isSelected) listOf(Color(0xFF1E88E5), Color(0xFF0D47A1)) else listOf(Color(0xFF90A4AE), Color(0xFF546E7A))
        val bodyGrad = if (isSelected) listOf(Color(0xFF1976D2), Color(0xFF0D3268)) else listOf(Color(0xFF78909C), Color(0xFF37474F))

        // 3D Torso / Shoulders
        val torsoPath = Path().apply {
            moveTo(w * 0.18f, h * 0.82f)
            cubicTo(
                w * 0.20f, h * 0.58f,
                w * 0.80f, h * 0.58f,
                w * 0.82f, h * 0.82f
            )
            close()
        }
        drawPath(
            path = torsoPath,
            brush = Brush.verticalGradient(bodyGrad, startY = h * 0.58f, endY = h * 0.82f)
        )

        // Torso Rim Light
        drawPath(
            path = torsoPath,
            color = Color.White.copy(alpha = 0.50f),
            style = Stroke(width = 1.5f)
        )

        // Head Sphere with 3D radial lighting (Light from top-left)
        drawCircle(
            brush = Brush.radialGradient(
                colors = primaryGrad,
                center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f),
                radius = headRadius * 1.3f
            ),
            radius = headRadius,
            center = headCenter
        )

        // Head Specular Highlight (Glassy 3D shine spot)
        drawCircle(
            color = Color.White.copy(alpha = 0.75f),
            radius = headRadius * 0.32f,
            center = Offset(headCenter.x - headRadius * 0.35f, headCenter.y - headRadius * 0.35f)
        )
    }
}

// 5. زر إضافة عقار العائم الذهبي 3D (Deluxe 3D Sphere with Embossed Plus & Radial Glow)
@Composable
fun Icon3DFabAdd(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fab_scale"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale)
            .clickable { onClick() }
            .testTag("nav_add_property_fab"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height
            val center = Offset(w * 0.5f, h * 0.5f)
            val radius = w * 0.44f

            // Multi-tier Soft Drop Shadow
            drawCircle(
                color = Color(0x40FFA000),
                radius = radius + 4f,
                center = Offset(center.x, center.y + 4f)
            )
            drawCircle(
                color = Color(0x35000000),
                radius = radius,
                center = Offset(center.x, center.y + 6f)
            )

            // Main 3D Golden Orb Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFF9C4), // Top bright highlight
                        Color(0xFFFFD54F), // Main rich gold
                        Color(0xFFFFA000), // Mid amber
                        Color(0xFFE65100)  // Deep shadow rim
                    ),
                    center = Offset(center.x - radius * 0.35f, center.y - radius * 0.35f),
                    radius = radius * 1.4f
                ),
                radius = radius,
                center = center
            )

            // Outer Metallic Golden Rim
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = listOf(
                        Color(0xFFFFF59D),
                        Color(0xFFFFB300),
                        Color(0xFFFF8F00),
                        Color(0xFFFFF59D)
                    ),
                    center = center
                ),
                radius = radius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Curved Specular Glare Arc (Top-Left Sheen)
            val shineArc = Path().apply {
                addArc(
                    oval = Rect(center.x - radius + 3f, center.y - radius + 3f, center.x + radius - 3f, center.y + radius - 3f),
                    startAngleDegrees = 190f,
                    sweepAngleDegrees = 80f
                )
            }
            drawPath(
                path = shineArc,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            // Embossed 3D Plus Symbol '+'
            val plusThickness = 6f
            val plusLength = radius * 0.70f

            // Plus Shadow (Slight offset for 3D indentation)
            drawLine(
                color = Color(0x507F0000),
                start = Offset(center.x - plusLength, center.y + 2f),
                end = Offset(center.x + plusLength, center.y + 2f),
                strokeWidth = plusThickness,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color(0x507F0000),
                start = Offset(center.x + 2f, center.y - plusLength),
                end = Offset(center.x + 2f, center.y + plusLength),
                strokeWidth = plusThickness,
                cap = StrokeCap.Round
            )

            // Plus Main White/Ivory Body
            drawLine(
                color = Color.White,
                start = Offset(center.x - plusLength, center.y),
                end = Offset(center.x + plusLength, center.y),
                strokeWidth = plusThickness,
                cap = StrokeCap.Round
            )
            drawLine(
                color = Color.White,
                start = Offset(center.x, center.y - plusLength),
                end = Offset(center.x, center.y + plusLength),
                strokeWidth = plusThickness,
                cap = StrokeCap.Round
            )
        }
    }
}

// 6. شعار الفيلا الذهبية 3D في رأس التطبيق (3D Golden Villa Logo)
@Composable
fun Icon3DLogoVilla(
    modifier: Modifier = Modifier,
    size: Dp = 42.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height

        // Ground shadow
        drawOval(
            color = Color(0x40000000),
            topLeft = Offset(w * 0.10f, h * 0.84f),
            size = Size(w * 0.80f, h * 0.14f)
        )

        val cx = w * 0.50f
        val baseY = h * 0.80f

        // 3D Isometric Golden Villa
        val leftWall = Path().apply {
            moveTo(cx, baseY)
            lineTo(w * 0.15f, baseY - h * 0.12f)
            lineTo(w * 0.15f, baseY - h * 0.48f)
            lineTo(cx, baseY - h * 0.36f)
            close()
        }
        drawPath(
            path = leftWall,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD54F), Color(0xFFFFA000)),
                startY = baseY - h * 0.48f,
                endY = baseY
            )
        )

        val rightWall = Path().apply {
            moveTo(cx, baseY)
            lineTo(w * 0.85f, baseY - h * 0.12f)
            lineTo(w * 0.85f, baseY - h * 0.48f)
            lineTo(cx, baseY - h * 0.36f)
            close()
        }
        drawPath(
            path = rightWall,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFA000), Color(0xFFE65100)),
                startY = baseY - h * 0.48f,
                endY = baseY
            )
        )

        // 3D Roof
        val roofLeft = Path().apply {
            moveTo(cx, baseY - h * 0.36f)
            lineTo(w * 0.10f, baseY - h * 0.48f)
            lineTo(cx, baseY - h * 0.76f)
            close()
        }
        drawPath(
            path = roofLeft,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD54F)),
                startY = baseY - h * 0.76f,
                endY = baseY - h * 0.36f
            )
        )

        val roofRight = Path().apply {
            moveTo(cx, baseY - h * 0.36f)
            lineTo(w * 0.90f, baseY - h * 0.48f)
            lineTo(cx, baseY - h * 0.76f)
            close()
        }
        drawPath(
            path = roofRight,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFCA28), Color(0xFFFF8F00)),
                startY = baseY - h * 0.76f,
                endY = baseY - h * 0.36f
            )
        )

        // Top Roof Ridge Highlight (Specular Line)
        drawLine(
            color = Color.White,
            start = Offset(cx, baseY - h * 0.76f),
            end = Offset(cx, baseY - h * 0.36f),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )

        // 3D Arched Door
        val doorPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = Rect(cx - w * 0.08f, baseY - h * 0.25f, cx + w * 0.08f, baseY),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            )
        }
        drawPath(
            path = doorPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF0D274A), Color(0xFF051329)),
                startY = baseY - h * 0.25f,
                endY = baseY
            )
        )
    }
}

// أيقونة المجتمع العقاري 3D (أشخاص مجسمون مع لمسة ذهبية وتدرج عميق)
@Composable
fun Icon3DCommunity(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // الظل السفلي
            drawOval(
                color = if (isSelected) Color(0x350D274A) else Color(0x18000000),
                topLeft = Offset(w * 0.15f, h * 0.82f),
                size = Size(w * 0.70f, h * 0.14f)
            )

            // ألوان الأشخاص (ذهبي وأزرق داكن)
            val primaryColor = if (isSelected) Color(0xFFD4AF37) else Color(0xFF64748B)
            val centerColor = if (isSelected) Color(0xFF0D274A) else Color(0xFF475569)

            // الشخص الأيسر (خلفي)
            drawCircle(
                color = primaryColor.copy(alpha = 0.85f),
                radius = w * 0.13f,
                center = Offset(w * 0.25f, h * 0.38f)
            )
            val leftBody = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.10f, h * 0.54f, w * 0.40f, h * 0.82f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                )
            }
            drawPath(leftBody, color = primaryColor.copy(alpha = 0.85f))

            // الشخص الأيمن (خلفي)
            drawCircle(
                color = primaryColor.copy(alpha = 0.85f),
                radius = w * 0.13f,
                center = Offset(w * 0.75f, h * 0.38f)
            )
            val rightBody = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.60f, h * 0.54f, w * 0.90f, h * 0.82f),
                        cornerRadius = CornerRadius(10f, 10f)
                    )
                )
            }
            drawPath(rightBody, color = primaryColor.copy(alpha = 0.85f))

            // الشخص الأوسط (أمامي ومجسم)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isSelected) listOf(Color(0xFFFFDF7A), Color(0xFFD4AF37), Color(0xFF997A15))
                             else listOf(Color(0xFF94A3B8), Color(0xFF475569)),
                    center = Offset(w * 0.48f, h * 0.28f),
                    radius = w * 0.18f
                ),
                radius = w * 0.17f,
                center = Offset(w * 0.50f, h * 0.30f)
            )

            val centerBody = Path().apply {
                addRoundRect(
                    RoundRect(
                        rect = Rect(w * 0.28f, h * 0.50f, w * 0.72f, h * 0.84f),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                )
            }
            drawPath(
                path = centerBody,
                brush = Brush.verticalGradient(
                    colors = if (isSelected) listOf(Color(0xFF1E3A8A), Color(0xFF0D274A))
                             else listOf(Color(0xFF64748B), Color(0xFF334155)),
                    startY = h * 0.50f,
                    endY = h * 0.84f
                )
            )

            // إطار ذهبي لامع للشخص الأوسط عند التحديد
            if (isSelected) {
                drawPath(
                    path = centerBody,
                    color = Color(0xFFFFDF7A),
                    style = Stroke(width = 2.5f)
                )
            }
        }
    }
}

// Helper Rect constructor
private fun Rect(left: Float, top: Float, right: Float, bottom: Float): androidx.compose.ui.geometry.Rect {
    return androidx.compose.ui.geometry.Rect(left, top, right, bottom)
}

// أيقونة المفضلة 3D (قلب مجسم مع تدرج وظل عميق)
@Composable
fun Icon3DFavorites(
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // الظل السفلي
            drawOval(
                color = if (isSelected) Color(0x35EF4444) else Color(0x18000000),
                topLeft = Offset(w * 0.20f, h * 0.84f),
                size = Size(w * 0.60f, h * 0.12f)
            )

            val heartColor = if (isSelected) Color(0xFFEF4444) else Color(0xFF64748B)
            val highlightColor = if (isSelected) Color(0xFFFF8B8B) else Color(0xFF94A3B8)

            val path = Path().apply {
                moveTo(w * 0.5f, h * 0.80f)
                cubicTo(w * 0.15f, h * 0.55f, 0f, h * 0.35f, w * 0.25f, h * 0.15f)
                cubicTo(w * 0.40f, h * 0.05f, w * 0.5f, h * 0.25f, w * 0.5f, h * 0.28f)
                cubicTo(w * 0.5f, h * 0.25f, w * 0.60f, h * 0.05f, w * 0.75f, h * 0.15f)
                cubicTo(w * 1.0f, h * 0.35f, w * 0.85f, h * 0.55f, w * 0.5f, h * 0.80f)
                close()
            }
            drawPath(path, color = heartColor)

            // إضاءة ولمعة 3D علوية
            drawCircle(
                color = highlightColor.copy(alpha = 0.6f),
                radius = w * 0.08f,
                center = Offset(w * 0.32f, h * 0.28f)
            )
        }
    }
}
