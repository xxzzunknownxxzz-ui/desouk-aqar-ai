package com.example.data

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object ReferralAndShareManager {

    const val APP_BASE_URL = "https://desouk-aqar.com/app"
    const val PROPERTY_BASE_URL = "https://desouk-aqar.com/property"

    fun shareApp(context: Context, referralCode: String) {
        val shareLink = if (referralCode.isNotBlank()) "$APP_BASE_URL?ref=$referralCode" else APP_BASE_URL
        val text = """
            🏠 اكتشف عقارات دسوق AI

            ابحث عن الشقق والعقارات للبيع والإيجار بسهولة، واستخدم المساعد العقاري الذكي للعثور على العقار المناسب بدسوق.

            ${if (referralCode.isNotBlank()) "كود الدعوة: $referralCode\n" else ""}جرّب التطبيق من هنا:
            $shareLink
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "تطبيق عقارات دسوق AI")
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة تطبيق عقارات دسوق AI عبر"))
    }

    fun copyInviteLink(context: Context, referralCode: String) {
        val link = if (referralCode.isNotBlank()) "$APP_BASE_URL?ref=$referralCode" else APP_BASE_URL
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("رابط دعوة عقارات دسوق", link)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ رابط الدعوة بنجاح 📋", Toast.LENGTH_SHORT).show()
    }

    fun copyReferralCode(context: Context, referralCode: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("كود دعوة عقارات دسوق", referralCode)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "تم نسخ كود الدعوة: $referralCode 📋", Toast.LENGTH_SHORT).show()
    }

    fun shareOnWhatsApp(context: Context, referralCode: String) {
        val shareLink = if (referralCode.isNotBlank()) "$APP_BASE_URL?ref=$referralCode" else APP_BASE_URL
        val text = "🏠 جرب تطبيق عقارات دسوق AI وابحث عن شقق وعقارات للبيع والإيجار الذكي!\nكود الدعوة: $referralCode\n$shareLink"
        try {
            val waIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=${Uri.encode(text)}")
            }
            context.startActivity(waIntent)
        } catch (_: Exception) {
            shareApp(context, referralCode)
        }
    }

    fun shareProperty(context: Context, property: PropertyEntity, referralCode: String = "") {
        val link = "$PROPERTY_BASE_URL/${property.id}" + if (referralCode.isNotBlank()) "?ref=$referralCode" else ""
        val text = """
            🏠 ${property.title}

            📍 الموقع: ${property.district} - ${property.location}
            💰 السعر: ${property.price.toLong()} ${property.priceUnit}
            📐 المساحة: ${property.area.toInt()} م²
            ${if (property.rooms > 0) "🛏️ عدد الغرف: ${property.rooms}\n" else ""}${if (property.floor > 0) "🏢 الدور: ${property.floor}\n" else ""}
            شاهد التفاصيل وتواصل مباشرة عبر تطبيق عقارات دسوق AI:
            $link
        """.trimIndent()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, property.title)
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, "مشاركة تفاصيل العقار"))
    }
}
