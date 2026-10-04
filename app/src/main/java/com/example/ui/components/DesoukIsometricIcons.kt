package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * أيقونات ثلاثية الأبعاد آيزومترية (3D Isometric Icons)
 * مطابقة تماماً للتصاميم المعمارية والعقارية في الصورة المرجعية لتطبيق عقارات دسوق:
 * 1. شقق: مبنى سكني متكامل مع نوافذ وشرفات
 * 2. منازل: فيلا سكنية أنيقة مع سقف قرميدي ومدخنة وحديقة
 * 3. محلات: متجر تجاري مع مظلة مقلمة وواجهة عرض زجاجية
 * 4. مكاتب: برج إداري شاهق بواجهات زجاجية عاكسة
 * 5. أراضي: قطعة أرض خضراء مع مساحة وسور وشجرة وعلامة مساحية
 */

@Composable
fun IsometricApartmentIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Soft ground shadow
            drawOval(
                color = Color(0x250D47A1),
                topLeft = Offset(w * 0.12f, h * 0.74f),
                size = Size(w * 0.76f, h * 0.22f)
            )

            // Geometry constants
            val cx = w * 0.5f
            val baseY = h * 0.78f
            val bHeight = h * 0.46f
            val isoW = w * 0.34f
            val isoH = h * 0.16f

            // 2. Left Wall (Shadow side - deep blue)
            val leftWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx - isoW, baseY - isoH)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(
                path = leftWall,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF1E88E5), Color(0xFF1565C0)),
                    startY = baseY - bHeight - isoH,
                    endY = baseY
                )
            )

            // 3. Right Wall (Light side - sky/cyan blue)
            val rightWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx + isoW, baseY - isoH)
                lineTo(cx + isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(
                path = rightWall,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF64B5F6), Color(0xFF2196F3)),
                    startY = baseY - bHeight - isoH,
                    endY = baseY
                )
            )

            // 4. Roof Top Face (Lightest blue/white)
            val roofTop = Path().apply {
                moveTo(cx, baseY - bHeight)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - (isoH * 2) - bHeight)
                lineTo(cx + isoW, baseY - isoH - bHeight)
                close()
            }
            drawPath(
                path = roofTop,
                color = Color(0xFFE3F2FD)
            )

            // 5. Roof Penthouse / Elevator Structure
            val phW = isoW * 0.45f
            val phH = isoH * 0.45f
            val phHeight = bHeight * 0.2f
            val phBaseY = baseY - bHeight - (isoH * 0.6f)

            val phLeft = Path().apply {
                moveTo(cx, phBaseY)
                lineTo(cx - phW, phBaseY - phH)
                lineTo(cx - phW, phBaseY - phH - phHeight)
                lineTo(cx, phBaseY - phHeight)
                close()
            }
            drawPath(phLeft, Color(0xFF1976D2))

            val phRight = Path().apply {
                moveTo(cx, phBaseY)
                lineTo(cx + phW, phBaseY - phH)
                lineTo(cx + phW, phBaseY - phH - phHeight)
                lineTo(cx, phBaseY - phHeight)
                close()
            }
            drawPath(phRight, Color(0xFF42A5F5))

            val phTop = Path().apply {
                moveTo(cx, phBaseY - phHeight)
                lineTo(cx - phW, phBaseY - phH - phHeight)
                lineTo(cx, phBaseY - (phH * 2) - phHeight)
                lineTo(cx + phW, phBaseY - phH - phHeight)
                close()
            }
            drawPath(phTop, Color(0xFFBBDEFB))

            // Antenna on roof
            drawLine(
                color = Color(0xFF90CAF9),
                start = Offset(cx, phBaseY - (phH * 2) - phHeight),
                end = Offset(cx, phBaseY - (phH * 2) - phHeight - (h * 0.1f)),
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 2.5f,
                center = Offset(cx, phBaseY - (phH * 2) - phHeight - (h * 0.1f))
            )

            // 6. Windows on Right Wall (Glowing warm light)
            val floors = 4
            for (floor in 1..floors) {
                val fy = baseY - (bHeight * (floor.toFloat() / (floors + 1)))
                // Window 1
                drawIsometricWindow(
                    cx = cx + (isoW * 0.32f),
                    cy = fy - (isoH * 0.32f),
                    w = isoW * 0.25f,
                    h = bHeight * 0.12f,
                    color = Color(0xFFFFF9C4),
                    frameColor = Color(0xFFFFFFFF)
                )
                // Window 2
                drawIsometricWindow(
                    cx = cx + (isoW * 0.72f),
                    cy = fy - (isoH * 0.72f),
                    w = isoW * 0.25f,
                    h = bHeight * 0.12f,
                    color = Color(0xFFFFEE58),
                    frameColor = Color(0xFFFFFFFF)
                )
            }

            // 7. Balconies / Windows on Left Wall
            for (floor in 1..floors) {
                val fy = baseY - (bHeight * (floor.toFloat() / (floors + 1)))
                drawIsometricLeftWindow(
                    cx = cx - (isoW * 0.38f),
                    cy = fy - (isoH * 0.38f),
                    w = isoW * 0.32f,
                    h = bHeight * 0.12f,
                    color = Color(0xFFBBDEFB),
                    frameColor = Color(0xFF90CAF9)
                )
            }

            // 8. Entrance door at base
            val doorPath = Path().apply {
                val dx = cx + (isoW * 0.35f)
                val dy = baseY - (isoH * 0.35f)
                val dw = isoW * 0.28f
                val dh = bHeight * 0.22f
                moveTo(dx - (dw / 2), dy - (dh * 0.2f))
                lineTo(dx + (dw / 2), dy + (dh * 0.2f))
                lineTo(dx + (dw / 2), dy - dh + (dh * 0.2f))
                lineTo(dx - (dw / 2), dy - dh - (dh * 0.2f))
                close()
            }
            drawPath(doorPath, Color(0xFF0D47A1))
        }
    }
}

@Composable
fun IsometricHouseIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Soft Shadow
            drawOval(
                color = Color(0x25004D40),
                topLeft = Offset(w * 0.1f, h * 0.72f),
                size = Size(w * 0.8f, h * 0.24f)
            )

            val cx = w * 0.5f
            val baseY = h * 0.75f
            val bHeight = h * 0.32f
            val isoW = w * 0.35f
            val isoH = h * 0.16f

            // 2. House Body - Left Wall (Shadow side - warm teal)
            val leftWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx - isoW, baseY - isoH)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(
                path = leftWall,
                color = Color(0xFF00897B)
            )

            // 3. House Body - Right Wall (Light side - vibrant teal)
            val rightWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx + isoW, baseY - isoH)
                lineTo(cx + isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(
                path = rightWall,
                color = Color(0xFF26A69A)
            )

            // 4. Chimney on Roof
            val chimX = cx - (isoW * 0.4f)
            val chimY = baseY - bHeight - (isoH * 0.8f)
            val chimW = isoW * 0.22f
            val chimH = isoH * 0.22f
            val chimHeight = h * 0.16f

            val chimLeft = Path().apply {
                moveTo(chimX, chimY)
                lineTo(chimX - chimW, chimY - chimH)
                lineTo(chimX - chimW, chimY - chimH - chimHeight)
                lineTo(chimX, chimY - chimHeight)
                close()
            }
            drawPath(chimLeft, Color(0xFFB71C1C))

            val chimRight = Path().apply {
                moveTo(chimX, chimY)
                lineTo(chimX + chimW, chimY - chimH)
                lineTo(chimX + chimW, chimY - chimH - chimHeight)
                lineTo(chimX, chimY - chimHeight)
                close()
            }
            drawPath(chimRight, Color(0xFFE53935))

            // Chimney Smoke puff
            drawCircle(
                color = Color(0x60ECEFF1),
                radius = 3.5f,
                center = Offset(chimX, chimY - chimHeight - 6f)
            )
            drawCircle(
                color = Color(0x40ECEFF1),
                radius = 5.5f,
                center = Offset(chimX - 4f, chimY - chimHeight - 14f)
            )

            // 5. Pitched Roof - Triangular Gable (Left side)
            val gableHeight = h * 0.24f
            val leftGable = Path().apply {
                moveTo(cx, baseY - bHeight)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx - (isoW * 0.5f), baseY - (isoH * 0.5f) - bHeight - gableHeight)
                close()
            }
            drawPath(leftGable, Color(0xFFD84315))

            // 6. Pitched Roof - Right Slope (Facing light - warm orange/terracotta)
            val rightRoof = Path().apply {
                moveTo(cx, baseY - bHeight)
                lineTo(cx - (isoW * 0.5f), baseY - (isoH * 0.5f) - bHeight - gableHeight)
                lineTo(cx + (isoW * 0.5f), baseY - (isoH * 1.5f) - bHeight - gableHeight)
                lineTo(cx + isoW, baseY - isoH - bHeight)
                close()
            }
            drawPath(
                path = rightRoof,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFF8A65), Color(0xFFFF5722)),
                    startY = baseY - bHeight - gableHeight - (isoH * 1.5f),
                    endY = baseY - bHeight
                )
            )

            // Roof Overhang highlights
            drawLine(
                color = Color(0xFFFFCCBC),
                start = Offset(cx - (isoW * 0.5f), baseY - (isoH * 0.5f) - bHeight - gableHeight),
                end = Offset(cx + (isoW * 0.5f), baseY - (isoH * 1.5f) - bHeight - gableHeight),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )

            // 7. Front Door (Right wall)
            val doorW = isoW * 0.32f
            val doorH = bHeight * 0.55f
            val doorX = cx + (isoW * 0.42f)
            val doorY = baseY - (isoH * 0.42f)

            val doorPath = Path().apply {
                moveTo(doorX - (doorW / 2), doorY)
                lineTo(doorX + (doorW / 2), doorY - (isoH * 0.25f))
                lineTo(doorX + (doorW / 2), doorY - doorH - (isoH * 0.25f))
                lineTo(doorX - (doorW / 2), doorY - doorH)
                close()
            }
            drawPath(doorPath, Color(0xFF4E342E))

            // Golden doorknob
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 2.5f,
                center = Offset(doorX + (doorW * 0.25f), doorY - (doorH * 0.45f))
            )

            // 8. Attic Round Window on Gable
            drawCircle(
                color = Color(0xFFFFF9C4),
                radius = 4.5f,
                center = Offset(cx - (isoW * 0.5f), baseY - (isoH * 0.5f) - bHeight - (gableHeight * 0.45f))
            )
            drawCircle(
                color = Color(0xFFFFFFFF),
                radius = 4.5f,
                center = Offset(cx - (isoW * 0.5f), baseY - (isoH * 0.5f) - bHeight - (gableHeight * 0.45f)),
                style = Stroke(width = 1.5f)
            )

            // 9. Side Window (Left wall)
            val winW = isoW * 0.35f
            val winH = bHeight * 0.35f
            val winX = cx - (isoW * 0.5f)
            val winY = baseY - (isoH * 0.5f) - (bHeight * 0.35f)

            val winPath = Path().apply {
                moveTo(winX - (winW / 2), winY)
                lineTo(winX + (winW / 2), winY + (isoH * 0.22f))
                lineTo(winX + (winW / 2), winY - winH + (isoH * 0.22f))
                lineTo(winX - (winW / 2), winY - winH)
                close()
            }
            drawPath(winPath, Color(0xFFFFF176))
            drawPath(winPath, Color(0xFFFFFFFF), style = Stroke(width = 1.5f))
        }
    }
}

@Composable
fun IsometricShopIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Soft Shadow
            drawOval(
                color = Color(0x25C2185B),
                topLeft = Offset(w * 0.1f, h * 0.74f),
                size = Size(w * 0.8f, h * 0.22f)
            )

            val cx = w * 0.5f
            val baseY = h * 0.78f
            val bHeight = h * 0.38f
            val isoW = w * 0.34f
            val isoH = h * 0.16f

            // 2. Shop Body - Left Wall (Shadowed coral/red)
            val leftWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx - isoW, baseY - isoH)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(leftWall, Color(0xFFD32F2F))

            // 3. Shop Body - Right Wall (Front storefront)
            val rightWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx + isoW, baseY - isoH)
                lineTo(cx + isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(rightWall, Color(0xFFFF5252))

            // 4. Shop Roof
            val roof = Path().apply {
                moveTo(cx, baseY - bHeight)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - (isoH * 2) - bHeight)
                lineTo(cx + isoW, baseY - isoH - bHeight)
                close()
            }
            drawPath(roof, Color(0xFFFFCDD2))

            // 5. Classic Striped Awning (المظلة المخططة أحمر وأبيض للمحل)
            val awningBaseY = baseY - (bHeight * 0.6f)
            val awningOverhang = isoW * 0.28f
            val awningHeight = h * 0.12f

            // 4 striped segments on the front awning
            val stripes = 4
            for (i in 0 until stripes) {
                val t0 = i.toFloat() / stripes
                val t1 = (i + 1).toFloat() / stripes
                val stripeColor = if (i % 2 == 0) Color(0xFFC62828) else Color(0xFFFFFFFF)

                val stripePath = Path().apply {
                    val p0x = cx + (isoW * t0)
                    val p0y = awningBaseY - (isoH * t0)
                    val p1x = cx + (isoW * t1)
                    val p1y = awningBaseY - (isoH * t1)

                    moveTo(p0x, p0y)
                    lineTo(p1x, p1y)
                    lineTo(p1x + (awningOverhang * 0.7f), p1y + (awningHeight * 0.7f))
                    lineTo(p0x + (awningOverhang * 0.7f), p0y + (awningHeight * 0.7f))
                    close()
                }
                drawPath(stripePath, stripeColor)
            }

            // 6. Large Glass Showcase Window underneath awning
            val winX = cx + (isoW * 0.55f)
            val winY = baseY - (isoH * 0.55f)
            val winW = isoW * 0.65f
            val winH = bHeight * 0.38f

            val showcasePath = Path().apply {
                moveTo(winX - (winW / 2), winY)
                lineTo(winX + (winW / 2), winY - (isoH * 0.4f))
                lineTo(winX + (winW / 2), winY - winH - (isoH * 0.4f))
                lineTo(winX - (winW / 2), winY - winH)
                close()
            }
            drawPath(
                path = showcasePath,
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA)),
                    start = Offset(winX - (winW / 2), winY - winH),
                    end = Offset(winX + (winW / 2), winY)
                )
            )
            // Showcase window frame
            drawPath(showcasePath, Color(0xFFFFFFFF), style = Stroke(width = 1.5f))

            // 7. Store Signboard on Roof Top ("SHOP" / ترويسة المحل الذهبية)
            val signPath = Path().apply {
                val sx = cx + (isoW * 0.5f)
                val sy = baseY - bHeight - (isoH * 0.5f)
                val sw = isoW * 0.65f
                val sh = h * 0.1f
                moveTo(sx - (sw / 2), sy)
                lineTo(sx + (sw / 2), sy - (isoH * 0.35f))
                lineTo(sx + (sw / 2), sy - sh - (isoH * 0.35f))
                lineTo(sx - (sw / 2), sy - sh)
                close()
            }
            drawPath(signPath, Color(0xFFFFD54F))
            drawPath(signPath, Color(0xFFFFA000), style = Stroke(width = 1.2f))
        }
    }
}

@Composable
fun IsometricOfficeIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Soft Shadow
            drawOval(
                color = Color(0x25311B92),
                topLeft = Offset(w * 0.12f, h * 0.74f),
                size = Size(w * 0.76f, h * 0.22f)
            )

            val cx = w * 0.5f
            val baseY = h * 0.8f
            val bHeight = h * 0.54f
            val isoW = w * 0.32f
            val isoH = h * 0.15f

            // 2. High-rise Glass Tower - Left Face (Deep Indigo/Purple)
            val leftWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx - isoW, baseY - isoH)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(
                path = leftWall,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF5E35B1), Color(0xFF311B92)),
                    startY = baseY - bHeight - isoH,
                    endY = baseY
                )
            )

            // 3. High-rise Glass Tower - Right Face (Electric Violet/Cyan Glass)
            val rightWall = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx + isoW, baseY - isoH)
                lineTo(cx + isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - bHeight)
                close()
            }
            drawPath(
                path = rightWall,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF7E57C2), Color(0xFF512DA8)),
                    startY = baseY - bHeight - isoH,
                    endY = baseY
                )
            )

            // 4. Slanted Modern Roof Top (Architectural Angular Cap)
            val roof = Path().apply {
                moveTo(cx, baseY - bHeight)
                lineTo(cx - isoW, baseY - isoH - bHeight)
                lineTo(cx, baseY - (isoH * 2) - bHeight - (h * 0.08f))
                lineTo(cx + isoW, baseY - isoH - bHeight)
                close()
            }
            drawPath(
                path = roof,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFD1C4E9), Color(0xFFB39DDB)),
                    startY = baseY - (isoH * 2) - bHeight - (h * 0.08f),
                    endY = baseY - bHeight
                )
            )

            // 5. Glass Grid Facade (Curtain wall grid lines)
            val floors = 6
            for (floor in 1..floors) {
                val t = floor.toFloat() / (floors + 1)
                val fy = baseY - (bHeight * t)
                // Left floor line
                drawLine(
                    color = Color(0x50FFFFFF),
                    start = Offset(cx, fy),
                    end = Offset(cx - isoW, fy - isoH),
                    strokeWidth = 1.2f
                )
                // Right floor line
                drawLine(
                    color = Color(0x60FFFFFF),
                    start = Offset(cx, fy),
                    end = Offset(cx + isoW, fy - isoH),
                    strokeWidth = 1.2f
                )
            }

            // Vertical mullions on right face
            val verticalLines = 3
            for (i in 1..verticalLines) {
                val frac = i.toFloat() / (verticalLines + 1)
                val vx = cx + (isoW * frac)
                val vy = baseY - (isoH * frac)
                drawLine(
                    color = Color(0x60FFFFFF),
                    start = Offset(vx, vy),
                    end = Offset(vx, vy - bHeight),
                    strokeWidth = 1.2f
                )
            }

            // Diagonal Light Reflection Sheen (بريق الزجاج العاكس)
            val sheen = Path().apply {
                moveTo(cx + (isoW * 0.2f), baseY - (bHeight * 0.2f))
                lineTo(cx + (isoW * 0.5f), baseY - (bHeight * 0.2f) - (isoH * 0.3f))
                lineTo(cx + (isoW * 0.8f), baseY - (bHeight * 0.85f) - (isoH * 0.6f))
                lineTo(cx + (isoW * 0.5f), baseY - (bHeight * 0.85f) - (isoH * 0.3f))
                close()
            }
            drawPath(sheen, Color(0x35FFFFFF))

            // 6. Communications Spire on Top
            val spireTip = Offset(cx, baseY - (isoH * 2) - bHeight - (h * 0.18f))
            drawLine(
                color = Color(0xFFD1C4E9),
                start = Offset(cx, baseY - (isoH * 2) - bHeight - (h * 0.08f)),
                end = spireTip,
                strokeWidth = 2f,
                cap = StrokeCap.Round
            )
            // Red Beacon at tip
            drawCircle(
                color = Color(0xFFFF1744),
                radius = 2.5f,
                center = spireTip
            )
        }
    }
}

@Composable
fun IsometricLandIcon(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val w = this.size.width
            val h = this.size.height

            // 1. Soft Shadow
            drawOval(
                color = Color(0x251B5E20),
                topLeft = Offset(w * 0.08f, h * 0.7f),
                size = Size(w * 0.84f, h * 0.26f)
            )

            val cx = w * 0.5f
            val baseY = h * 0.72f
            val soilHeight = h * 0.16f
            val isoW = w * 0.38f
            val isoH = h * 0.18f

            // 2. Soil Cutaway - Left Earth Layer (Dark soil)
            val leftSoil = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx - isoW, baseY - isoH)
                lineTo(cx - isoW, baseY - isoH + soilHeight)
                lineTo(cx, baseY + soilHeight)
                close()
            }
            drawPath(
                path = leftSoil,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF5D4037), Color(0xFF3E2723)),
                    startY = baseY - isoH,
                    endY = baseY + soilHeight
                )
            )

            // 3. Soil Cutaway - Right Earth Layer (Rich brown loam)
            val rightSoil = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx + isoW, baseY - isoH)
                lineTo(cx + isoW, baseY - isoH + soilHeight)
                lineTo(cx, baseY + soilHeight)
                close()
            }
            drawPath(
                path = rightSoil,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF795548), Color(0xFF4E342E)),
                    startY = baseY - isoH,
                    endY = baseY + soilHeight
                )
            )

            // 4. Lush Grass Surface - Top Diamond (Green Lawn Terrain)
            val topGrass = Path().apply {
                moveTo(cx, baseY)
                lineTo(cx - isoW, baseY - isoH)
                lineTo(cx, baseY - (isoH * 2))
                lineTo(cx + isoW, baseY - isoH)
                close()
            }
            drawPath(
                path = topGrass,
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF81C784), Color(0xFF43A047)),
                    startY = baseY - (isoH * 2),
                    endY = baseY
                )
            )

            // 5. White Boundary Fence / Survey Grid Lines (تحديد معالم الأرض)
            val fenceColor = Color(0xCCFFFFFF)
            // Plot perimeter border
            drawPath(
                path = topGrass,
                color = fenceColor,
                style = Stroke(width = 1.5f)
            )

            // Plot division grid line
            drawLine(
                color = Color(0x60FFFFFF),
                start = Offset(cx, baseY),
                end = Offset(cx, baseY - (isoH * 2)),
                strokeWidth = 1f
            )

            // 6. Miniature 3D Tree on the left of the plot
            val treeX = cx - (isoW * 0.42f)
            val treeY = baseY - (isoH * 0.85f)

            // Tree Trunk
            drawLine(
                color = Color(0xFF6D4C41),
                start = Offset(treeX, treeY),
                end = Offset(treeX, treeY - (h * 0.16f)),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Tree Foliage (Lush round green cloud)
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = w * 0.11f,
                center = Offset(treeX, treeY - (h * 0.22f))
            )
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = w * 0.08f,
                center = Offset(treeX + 2f, treeY - (h * 0.24f))
            )

            // 7. Surveyor Flag / Pin in the center of plot (عصا المساحة مع العلم الأحمر)
            val pinX = cx + (isoW * 0.25f)
            val pinY = baseY - (isoH * 1.1f)
            val poleH = h * 0.26f

            // Pin Pole
            drawLine(
                color = Color(0xFFECEFF1),
                start = Offset(pinX, pinY),
                end = Offset(pinX, pinY - poleH),
                strokeWidth = 2.2f,
                cap = StrokeCap.Round
            )
            // Red Surveyor Flag
            val flag = Path().apply {
                moveTo(pinX, pinY - poleH)
                lineTo(pinX + (w * 0.16f), pinY - poleH + (h * 0.05f))
                lineTo(pinX, pinY - poleH + (h * 0.1f))
                close()
            }
            drawPath(flag, Color(0xFFE53935))

            // Gold pin top
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = 2.5f,
                center = Offset(pinX, pinY - poleH)
            )
        }
    }
}

private fun DrawScope.drawIsometricWindow(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    color: Color,
    frameColor: Color
) {
    val win = Path().apply {
        moveTo(cx - (w / 2), cy)
        lineTo(cx + (w / 2), cy - (h * 0.35f))
        lineTo(cx + (w / 2), cy - h - (h * 0.35f))
        lineTo(cx - (w / 2), cy - h)
        close()
    }
    drawPath(win, color)
    drawPath(win, frameColor, style = Stroke(width = 1.2f))
}

private fun DrawScope.drawIsometricLeftWindow(
    cx: Float,
    cy: Float,
    w: Float,
    h: Float,
    color: Color,
    frameColor: Color
) {
    val win = Path().apply {
        moveTo(cx - (w / 2), cy)
        lineTo(cx + (w / 2), cy + (h * 0.35f))
        lineTo(cx + (w / 2), cy - h + (h * 0.35f))
        lineTo(cx - (w / 2), cy - h)
        close()
    }
    drawPath(win, color)
    drawPath(win, frameColor, style = Stroke(width = 1.2f))
}
