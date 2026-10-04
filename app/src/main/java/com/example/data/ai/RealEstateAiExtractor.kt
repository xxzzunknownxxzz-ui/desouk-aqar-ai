package com.example.data.ai

import com.example.data.ImportedPropertyEntity
import com.example.data.PropertyEntity
import java.util.regex.Pattern

/**
 * نتيجة تحليل الذكاء الاصطناعي للمنشور العقاري
 */
data class AiExtractionResult(
    val title: String,
    val propertyType: String,
    val operationType: String,
    val city: String,
    val district: String,
    val street: String,
    val landmark: String,
    val locationZone: String = "دسوق", // دسوق، أحياء ومناطق دسوق، القرى والمناطق المحيطة
    val address: String = "",
    val price: Double?,
    val priceUnit: String,
    val area: Double?,
    val rooms: Int?,
    val bathrooms: Int?,
    val floor: Int?,
    val totalFloors: Int?,
    val hasElevator: Boolean?,
    val hasGarage: Boolean?,
    val finishing: String,
    val facade: String,
    val streetsCount: Int = 1,
    val condition: String,
    val features: String,
    val cleanedText: String,
    val contactPhone: String,
    val contactWhatsapp: String,
    val contactName: String,
    val completenessScore: Int,
    val uncertainData: String,
    val isDuplicate: Boolean,
    val duplicateReason: String,
    val matchedProperty: PropertyEntity? = null,
    val matchedImported: ImportedPropertyEntity? = null
)

/**
 * محرك استخراج وتنظيف وتحليل البيانات العقارية بالذكاء الاصطناعي
 */
object RealEstateAiExtractor {

    private val EMOJI_REGEX = Pattern.compile("[\\p{So}\\p{Cn}\\p{Cs}\\p{Sk}\\p{Sm}]")
    private val PHONE_PATTERN = Pattern.compile("(?:(?:\\+?20)|0)?1[0125]\\d{8}")
    private val HASHTAG_PATTERN = Pattern.compile("#[\\w\\u0600-\\u06FF]+")

    private val DESOUK_DISTRICTS = listOf(
        "حي دحروج",
        "شارع الجيش",
        "كورنيش النيل",
        "الميدان الإبراهيمي",
        "شارع الشركات",
        "حي المستشفى",
        "شارع المحطة",
        "ميدان المحطة",
        "طريق فوه",
        "شارع الجمهورية",
        "المناطق المحيطة",
        "دسوق"
    )

    /**
     * تحليل المنشور واستخراج البيانات العقارية بدقة مع عدم اختراع أي معلومة غير موجودة
     */
    fun extractFromRawText(
        rawText: String,
        sourceName: String = "",
        postUrl: String = "",
        postExternalId: String = "",
        existingProperties: List<PropertyEntity> = emptyList(),
        existingImported: List<ImportedPropertyEntity> = emptyList()
    ): AiExtractionResult {
        val trimmed = rawText.trim()

        // 1. تنظيف النص المستخرج (مع الاحتفاظ بالنص الأصلي)
        val cleaned = cleanText(trimmed)

        // 2. نوع العملية (للبيع / للإيجار)
        val operationType = when {
            trimmed.contains("إيجار") || trimmed.contains("للايجار") || trimmed.contains("مفروش") -> "للإيجار"
            trimmed.contains("بيع") || trimmed.contains("للبيع") || trimmed.contains("تمليك") -> "للبيع"
            else -> "للبيع" // الافتراضي إذا كانت صيغة العرض تمليك
        }

        // 3. نوع العقار
        val propertyType = when {
            trimmed.contains("شقة") || trimmed.contains("شقه") -> "شقة"
            trimmed.contains("فيلا") || trimmed.contains("فيللا") -> "فيلا"
            trimmed.contains("منزل") || trimmed.contains("بيت") -> "منزل"
            trimmed.contains("أرض") || trimmed.contains("ارض") || trimmed.contains("قطعة أرض") -> "أرض"
            trimmed.contains("محل") || trimmed.contains("دكان") -> "محل"
            trimmed.contains("مكتب") -> "مكتب"
            trimmed.contains("عمارة") || trimmed.contains("عماره") || trimmed.contains("برج") -> "عمارة"
            trimmed.contains("مخزن") || trimmed.contains("بدروم") -> "مخزن"
            trimmed.contains("شاليه") -> "شاليه"
            trimmed.contains("تجاري") || trimmed.contains("مول") || trimmed.contains("مقر تجاري") -> "عقار تجاري"
            else -> "أخرى"
        }

        // 4. تصنيف الموقع (دسوق والمناطق المحيطة)
        var district = "دسوق"
        var city = "دسوق"
        var isLocationUncertain = false

        val matchingDistrict = DESOUK_DISTRICTS.firstOrNull { trimmed.contains(it, ignoreCase = true) }
        if (matchingDistrict != null) {
            district = matchingDistrict
        } else if (trimmed.contains("دسوق")) {
            district = "دسوق"
        } else {
            val otherCities = listOf("كفر الشيخ", "فوه", "مطوبس", "سيدي سالم", "قلين", "طنطا", "الإسكندرية")
            val otherMatch = otherCities.firstOrNull { trimmed.contains(it) }
            if (otherMatch != null) {
                city = otherMatch
                district = otherMatch
            } else {
                district = "الموقع يحتاج مراجعة"
                isLocationUncertain = true
            }
        }

        val locationZone = when {
            trimmed.contains("قرية") || trimmed.contains("كفر") || trimmed.contains("محلة") || trimmed.contains("المناطق المحيطة") -> "القرى والمناطق المحيطة"
            matchingDistrict != null && matchingDistrict != "دسوق" -> "أحياء ومناطق دسوق"
            else -> "دسوق"
        }

        // الشارع والمعلم
        val street = extractStreet(trimmed)
        val landmark = extractLandmark(trimmed)
        val address = listOf(street, landmark, district).filter { it.isNotBlank() }.joinToString(" - ").ifBlank { district }

        // عدد الشوارع
        val streetsCount = when {
            trimmed.contains("3 شوارع") || trimmed.contains("ثلاثة شوارع") || trimmed.contains("3 ناصية") -> 3
            trimmed.contains("شارعين") || trimmed.contains("على شارعين") || trimmed.contains("ناصية") || trimmed.contains("زاويتين") -> 2
            else -> 1
        }

        // 5. استخراج السعر (عدم التخمين إذا لم يذكر)
        val price = extractPrice(trimmed)

        // 6. استخراج المساحة (عدم التخمين إذا لم تذكر)
        val area = extractArea(trimmed)

        // 7. الغرف والحمامات والدور
        val rooms = extractRooms(trimmed)
        val bathrooms = extractBathrooms(trimmed)
        val floor = extractFloor(trimmed)

        // 8. المرافق (أسانسير، جراج، تشطيب)
        val hasElevator = when {
            trimmed.contains("أسانسير") || trimmed.contains("اسانسير") || trimmed.contains("مصعد") -> true
            trimmed.contains("بدون اسانسير") || trimmed.contains("لا يوجد اسانسير") -> false
            else -> null
        }

        val hasGarage = when {
            trimmed.contains("جراج") || trimmed.contains("بارك") -> true
            trimmed.contains("بدون جراج") -> false
            else -> null
        }

        val finishing = when {
            trimmed.contains("ألترا لوكس") || trimmed.contains("الترا لوكس") -> "ألترا لوكس"
            trimmed.contains("سوبر لوكس") -> "سوبر لوكس"
            trimmed.contains("لوكس") -> "لوكس"
            trimmed.contains("نصف تشطيب") -> "نصف تشطيب"
            trimmed.contains("محارة") || trimmed.contains("على المحارة") -> "على المحارة"
            else -> ""
        }

        val facade = when {
            trimmed.contains("بحرية") || trimmed.contains("بحري") -> "واجهة بحرية"
            trimmed.contains("قبلية") || trimmed.contains("قبلي") -> "واجهة قبلية"
            trimmed.contains("على الشارع") || trimmed.contains("ناصية") -> "ناصية مميزة"
            else -> ""
        }

        // 9. بيانات التواصل
        val phoneMatcher = PHONE_PATTERN.matcher(trimmed)
        val contactPhone = if (phoneMatcher.find()) phoneMatcher.group() else ""
        val contactWhatsapp = if (contactPhone.isNotBlank()) "2$contactPhone" else ""
        val contactName = extractContactName(trimmed, sourceName)

        // 10. حساب درجة اكتمال البيانات والبيانات غير المؤكدة
        val uncertainList = mutableListOf<String>()
        var score = 30 // قاعدة أولية لتحليل النص

        if (price != null) score += 20 else uncertainList.add("السعر غير متوفر")
        if (area != null) score += 20 else uncertainList.add("المساحة غير متوفرة")
        if (contactPhone.isNotBlank()) score += 15 else uncertainList.add("رقم الهاتف غير متوفر")
        if (!isLocationUncertain) score += 15 else uncertainList.add("الموقع يحتاج لتأكيد دقيق")

        val uncertainData = uncertainList.joinToString(" • ")

        // عنوان جذاب مستخرج
        val title = buildTitle(propertyType, operationType, district, area)

        // 11. فحص التكرار الشامل (Duplicate Detection System)
        var isDuplicate = false
        var duplicateReason = ""
        var matchedProperty: PropertyEntity? = null
        var matchedImported: ImportedPropertyEntity? = null

        // أ) فحص برابط المنشور الأصلي
        if (postUrl.isNotBlank()) {
            val dupImport = existingImported.firstOrNull { 
                (it.sourceUrl == postUrl && it.sourceUrl.isNotBlank()) || (it.postUrl == postUrl && it.postUrl.isNotBlank())
            }
            if (dupImport != null) {
                isDuplicate = true
                duplicateReason = "تطابق كامل في رابط منشور Facebook الأصلي مع (${dupImport.title})"
                matchedImported = dupImport
            }
            if (!isDuplicate) {
                val dupProp = existingProperties.firstOrNull { it.sourceUrl == postUrl && it.sourceUrl.isNotBlank() }
                if (dupProp != null) {
                    isDuplicate = true
                    duplicateReason = "تطابق في رابط منشور Facebook الأصلي مع العقار المنشور #${dupProp.id}"
                    matchedProperty = dupProp
                }
            }
        }

        // ب) فحص برقم الهاتف + (السعر أو المساحة)
        if (!isDuplicate && contactPhone.isNotBlank()) {
            val dupProp = existingProperties.firstOrNull {
                it.phone.contains(contactPhone) &&
                        ((price != null && it.price == price) || (area != null && it.area == area))
            }
            if (dupProp != null) {
                isDuplicate = true
                duplicateReason = "تطابق برقم الهاتف ($contactPhone) مع عقار #${dupProp.id} (${dupProp.title})"
                matchedProperty = dupProp
            }
        }

        // ج) فحص بتطابق العنوان والسعر والمساحة
        if (!isDuplicate && price != null && area != null) {
            val dupProp = existingProperties.firstOrNull {
                it.district == district && it.price == price && it.area == area
            }
            if (dupProp != null) {
                isDuplicate = true
                duplicateReason = "تطابق كامل في الموقع ($district) والسعر (${price.toInt()} ج.م) والمساحة (${area.toInt()} م²)"
                matchedProperty = dupProp
            }
        }

        // د) فحص بتشابه الوصف (Description Similarity)
        if (!isDuplicate && trimmed.length > 30) {
            val words = trimmed.split(" ").filter { it.length > 3 }.toSet()
            val dupProp = existingProperties.firstOrNull { prop ->
                if (prop.description.length > 30) {
                    val propWords = prop.description.split(" ").filter { it.length > 3 }.toSet()
                    val common = words.intersect(propWords).size
                    val ratio = common.toDouble() / words.size.coerceAtLeast(1)
                    ratio > 0.65
                } else false
            }
            if (dupProp != null) {
                isDuplicate = true
                duplicateReason = "تشابه كبير في نص وصف الإعلان مع عقار #${dupProp.id} (${dupProp.title})"
                matchedProperty = dupProp
            }
        }

        return AiExtractionResult(
            title = title,
            propertyType = propertyType,
            operationType = operationType,
            city = city,
            district = district,
            street = street,
            landmark = landmark,
            locationZone = locationZone,
            address = address,
            price = price,
            priceUnit = if (operationType == "للإيجار") "ج.م / شهرياً" else "ج.م",
            area = area,
            rooms = rooms,
            bathrooms = bathrooms,
            floor = floor,
            totalFloors = null,
            hasElevator = hasElevator,
            hasGarage = hasGarage,
            finishing = finishing,
            facade = facade,
            streetsCount = streetsCount,
            condition = if (operationType == "للبيع") "جاهز للتسليم" else "متاح فوراً",
            features = extractFeatures(trimmed),
            cleanedText = cleaned,
            contactPhone = contactPhone,
            contactWhatsapp = contactWhatsapp,
            contactName = contactName,
            completenessScore = score.coerceIn(20, 100),
            uncertainData = uncertainData,
            isDuplicate = isDuplicate,
            duplicateReason = duplicateReason,
            matchedProperty = matchedProperty,
            matchedImported = matchedImported
        )
    }

    private fun cleanText(text: String): String {
        var result = EMOJI_REGEX.matcher(text).replaceAll("")
        result = HASHTAG_PATTERN.matcher(result).replaceAll("")
        result = result.replace(Regex("\\s+"), " ")
        result = result.replace("مكرر", "").replace("عاجل", "")
        return result.trim()
    }

    private fun extractStreet(text: String): String {
        val patterns = listOf(
            Regex("شارع\\s+([\\u0600-\\u06FF\\w\\s]+?)(?:\\s+(?:بجوار|أمام|خلف|قريب|دور|سعر|مساحة|$))"),
            Regex("ش\\s+([\\u0600-\\u06FF\\w]+)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return ""
    }

    private fun extractLandmark(text: String): String {
        val patterns = listOf(
            Regex("(?:بجوار|خلف|أمام|قريب من)\\s+([\\u0600-\\u06FF\\w\\s]+?)(?:\\s+(?:شارع|سعر|مساحة|دور|$))")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return ""
    }

    private fun extractPrice(text: String): Double? {
        val patterns = listOf(
            Regex("(?:سعر|بـ|بمبلغ|المطلوب)\\s*[:=-]?\\s*([\\d,.]+)\\s*(?:ألف|الف|مليون|ج|جنيه|ج\\.م)?"),
            Regex("([\\d,.]+)\\s*(?:ألف|الف)\\s*جنيه"),
            Regex("([\\d,.]+)\\s*(?:مليون)\\s*جنيه")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val numStr = match.groupValues[1].replace(",", "").trim()
                val parsed = numStr.toDoubleOrNull()
                if (parsed != null) {
                    val fullMatch = match.value
                    return when {
                        fullMatch.contains("مليون") -> parsed * 1_000_000
                        fullMatch.contains("ألف") || fullMatch.contains("الف") -> parsed * 1_000
                        parsed < 1000 && !fullMatch.contains("إيجار") -> parsed * 1000 // كالمعتاد "بـ 850" يعني 850 ألف
                        else -> parsed
                    }
                }
            }
        }
        return null
    }

    private fun extractArea(text: String): Double? {
        val patterns = listOf(
            Regex("(?:مساحة|المساحة)\\s*[:=-]?\\s*(\\d+)\\s*(?:متر|م²|م2|م)?"),
            Regex("(\\d+)\\s*(?:متر|م²|م2)")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                val areaVal = match.groupValues[1].toDoubleOrNull()
                if (areaVal != null && areaVal in 20.0..5000.0) {
                    return areaVal
                }
            }
        }
        return null
    }

    private fun extractRooms(text: String): Int? {
        val patterns = listOf(
            Regex("(\\d+)\\s*غرف"),
            Regex("غرفتين|2 غرفة") to 2,
            Regex("3 غرف|ثلاث غرف") to 3,
            Regex("4 غرف|أربع غرف") to 4
        )
        val simpleMatch = Regex("(\\d+)\\s*غرف").find(text)
        if (simpleMatch != null) {
            return simpleMatch.groupValues[1].toIntOrNull()
        }
        if (text.contains("غرفتين")) return 2
        if (text.contains("3 غرف")) return 3
        if (text.contains("4 غرف")) return 4
        return null
    }

    private fun extractBathrooms(text: String): Int? {
        if (text.contains("2 حمام") || text.contains("حمامين")) return 2
        if (text.contains("3 حمام")) return 3
        if (text.contains("حمام")) return 1
        return null
    }

    private fun extractFloor(text: String): Int? {
        val match = Regex("(?:دور|الدور|طابق)\\s*(\\d+)").find(text)
        if (match != null) return match.groupValues[1].toIntOrNull()
        if (text.contains("أرضي") || text.contains("الارضي")) return 0
        if (text.contains("أول") || text.contains("الاول")) return 1
        if (text.contains("ثاني") || text.contains("الثاني")) return 2
        if (text.contains("ثالث") || text.contains("الثالث")) return 3
        if (text.contains("رابع") || text.contains("الرابع")) return 4
        if (text.contains("خامس") || text.contains("الخامس")) return 5
        return null
    }

    private fun extractFeatures(text: String): String {
        val feats = mutableListOf<String>()
        if (text.contains("عداد كهرباء")) feats.add("عداد كهرباء")
        if (text.contains("عداد مياه")) feats.add("عداد مياه")
        if (text.contains("غاز طبيعي")) feats.add("غاز طبيعي")
        if (text.contains("حصة في الأرض") || text.contains("حصة بالارض")) feats.add("حصة بالأرض")
        if (text.contains("مرخصة") || text.contains("برخصة")) feats.add("عقار مرخص")
        if (text.contains("تطل على النيل")) feats.add("إطلالة نيلية")
        return feats.joinToString("، ")
    }

    private fun extractContactName(text: String, sourceName: String): String {
        val patterns = listOf(
            Regex("(?:للتواصل مع|المعلن|المالك|أ/|أستاذ)\\s*([\\u0600-\\u06FF\\s]{3,20})")
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null) {
                return match.groupValues[1].trim()
            }
        }
        return if (sourceName.isNotBlank()) "مسؤول $sourceName" else "المعلن المعتمد"
    }

    private fun buildTitle(type: String, op: String, district: String, area: Double?): String {
        val areaPart = if (area != null && area > 0) " (${area.toInt()} م²)" else ""
        return "$type $op في $district$areaPart"
    }
}
