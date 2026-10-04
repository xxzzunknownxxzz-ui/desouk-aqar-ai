package com.example.data.ai

import com.example.data.PropertyEntity

data class ExtractedPostData(
    val propertyType: String = "",
    val dealType: String = "",
    val location: String = "",
    val rooms: String = "",
    val floor: String = "",
    val price: String = "",
    val area: String = ""
)

sealed class AiQueryState {
    data class Success(
        val reply: String,
        val matchingProperties: List<PropertyEntity>
    ) : AiQueryState()

    data class ClarificationNeeded(
        val question: String,
        val suggestionChips: List<String>
    ) : AiQueryState()

    data class NoMatch(
        val message: String,
        val suggestedPostType: String = "🔎 مطلوب عقار",
        val prefilledContent: String
    ) : AiQueryState()
}

object CommunityAiMatcher {

    /**
     * استخراج المعلومات تلقائياً بالذكاء الاصطناعي من نص المنشور
     * مثل: "شقة للبيع في شارع الجيش بدسوق 3 غرف الدور الرابع والسعر مليون ونصف"
     */
    fun extractStructuredData(text: String): ExtractedPostData {
        val clean = text.trim()
        if (clean.isBlank()) return ExtractedPostData()

        // 1. نوع العقار
        val propType = when {
            clean.contains("شقة") || clean.contains("شقق") || clean.contains("استوديو") -> "شقة"
            clean.contains("محل") || clean.contains("محلات") || clean.contains("تجاري") -> "محل تجاري"
            clean.contains("مكتب") || clean.contains("إداري") || clean.contains("اداري") || clean.contains("عيادة") -> "مكتب إداري"
            clean.contains("أرض") || clean.contains("ارض") || clean.contains("قطعة أرض") -> "أرض"
            clean.contains("منزل") || clean.contains("بيت") || clean.contains("عمارة") || clean.contains("فيلا") -> "منزل / عمارة"
            else -> "عقار سكنى"
        }

        // 2. الحالة (للبيع أم للإيجار أم مطلوب)
        val dealType = when {
            clean.contains("مطلوب") || clean.contains("محتاج") || clean.contains("بدور على") || clean.contains("ابحث عن") -> "مطلوب عقار"
            clean.contains("إيجار") || clean.contains("ايجار") || clean.contains("للايجار") || clean.contains("للإيجار") || clean.contains("مفروش") -> "للإيجار"
            clean.contains("بيع") || clean.contains("للبيع") || clean.contains("تمليك") || clean.contains("شراء") -> "للبيع"
            else -> "معروض"
        }

        // 3. الموقع داخل دسوق
        val locations = listOf(
            "شارع الجيش", "كورنيش النيل", "الميدان الإبراهيمي", "ميدان العارف", "دحروج",
            "الصفا", "حي الصفا", "شارع الشركات", "الزهور", "مساكن الزهور", "المحطة",
            "شارع المحطة", "المستشفى العام", "طريق بسيون", "طريق فوه", "شارع المدارس",
            "سعد زغلول", "شارع بورسعيد", "كوبري عاطف السادات"
        )
        val matchedLocation = locations.firstOrNull { clean.contains(it) } ?: if (clean.contains("دسوق")) "مدينة دسوق" else "دسوق"

        // 4. عدد الغرف
        val rooms = when {
            clean.contains("استوديو") || clean.contains("غرفة واحدة") -> "1 غرفة"
            clean.contains("غرفتين") || clean.contains("2 غرفة") || clean.contains("2 نوم") -> "2 غرف"
            clean.contains("3 غرف") || clean.contains("ثلاث غرف") || clean.contains("3 نوم") || clean.contains("تلات غرف") -> "3 غرف"
            clean.contains("4 غرف") || clean.contains("أربع غرف") || clean.contains("اربع غرف") -> "4 غرف"
            clean.contains("5 غرف") || clean.contains("خمس غرف") -> "5 غرف"
            else -> ""
        }

        // 5. الدور
        val floor = when {
            clean.contains("أرضي") || clean.contains("ارضي") -> "الدور الأرضي"
            clean.contains("أول") || clean.contains("اول") -> "الدور الأول"
            clean.contains("ثاني") || clean.contains("تاني") -> "الدور الثاني"
            clean.contains("ثالث") || clean.contains("تالت") -> "الدور الثالث"
            clean.contains("رابع") -> "الدور الرابع"
            clean.contains("خامس") -> "الدور الخامس"
            clean.contains("سادس") -> "الدور السادس"
            clean.contains("أخير") || clean.contains("روف") -> "الدور الأخير / روف"
            else -> ""
        }

        // 6. السعر بالجنيه
        var price = ""
        val pricePatterns = listOf(
            Regex("""(\d+(?:\.\d+)?)\s*(?:مليون|ملايين)"""),
            Regex("""(\d+(?:,\d+)*)\s*(?:ألف|الف|جنيه|ج)""")
        )

        when {
            clean.contains("مليون ونصف") || clean.contains("مليون ونص") -> price = "1,500,000 جنيه"
            clean.contains("مليون وربع") -> price = "1,250,000 جنيه"
            clean.contains("مليون ومائتين") || clean.contains("مليون و200") -> price = "1,200,000 جنيه"
            clean.contains("2 مليون") || clean.contains("اتنين مليون") -> price = "2,000,000 جنيه"
            clean.contains("مليون") && !clean.contains("أقل من مليون") -> price = "1,000,000 جنيه"
            else -> {
                val numMatch = Regex("""(\d[\d,.]*)\s*(?:جنيه|ج|جنية)""").find(clean)
                if (numMatch != null) {
                    price = "${numMatch.groupValues[1]} جنيه"
                } else {
                    val justDigits = Regex("""\b(\d{3,7})\b""").find(clean)
                    if (justDigits != null) {
                        price = "${justDigits.groupValues[1]} جنيه"
                    }
                }
            }
        }

        // 7. المساحة
        val areaMatch = Regex("""(\d+)\s*(?:متر|م2|م)""").find(clean)
        val area = if (areaMatch != null) "${areaMatch.groupValues[1]} م²" else ""

        return ExtractedPostData(
            propertyType = propType,
            dealType = dealType,
            location = matchedLocation,
            rooms = rooms,
            floor = floor,
            price = price,
            area = area
        )
    }

    /**
     * معالجة استفسار المستخدم العام أو بالعامية المصرية والبحث في قاعدة بيانات العقارات الحقيقية
     * أمثلة:
     * - "عاوز شقة 3 غرف في دسوق إيجار لحد 7000 جنيه"
     * - "عاوز محل للبيع في دسوق بأقل من مليون"
     * - "محتاج أرض في دسوق"
     */
    fun processAiAssistantQuery(
        rawQuery: String,
        allProperties: List<PropertyEntity>
    ): AiQueryState {
        val q = rawQuery.trim().lowercase()

        // استخراج نوع الصفقة
        val isRent = q.contains("إيجار") || q.contains("ايجار") || q.contains("للايجار") || q.contains("للإيجار")
        val isSale = q.contains("بيع") || q.contains("للبيع") || q.contains("شراء") || q.contains("تمليك")

        // استخراج نوع العقار
        val isApartment = q.contains("شقة") || q.contains("شقق")
        val isShop = q.contains("محل") || q.contains("محلات") || q.contains("تجاري")
        val isOffice = q.contains("مكتب") || q.contains("إداري") || q.contains("اداري") || q.contains("عيادة")
        val isLand = q.contains("أرض") || q.contains("ارض") || q.contains("أراضي")
        val isHouse = q.contains("منزل") || q.contains("بيت") || q.contains("عمارة") || q.contains("فيلا")

        // استخراج عدد الغرف
        val targetRooms = when {
            q.contains("غرفتين") || q.contains("2 غرفة") || q.contains("2 نوم") -> 2
            q.contains("3 غرف") || q.contains("تلات غرف") || q.contains("ثلاث غرف") || q.contains("3 نوم") -> 3
            q.contains("4 غرف") || q.contains("أربع غرف") || q.contains("4 نوم") -> 4
            q.contains("غرفة") || q.contains("1 غرفة") -> 1
            else -> null
        }

        // استخراج الحد الأقصى للميزانية
        val maxPrice = extractMaxPrice(q)

        // استخراج المنطقة
        val districts = listOf(
            "شارع الجيش", "كورنيش النيل", "الميدان الإبراهيمي", "دحروج", "الصفا",
            "شارع الشركات", "الزهور", "المحطة", "المستشفى العام", "طريق بسيون", "طريق فوه"
        )
        val matchedDistrict = districts.firstOrNull { q.contains(it.lowercase()) }

        // التحقق إذا كان الطلب عاماً جداً وينقصه معلومات أساسية
        // مثل أن يقول فقط "عاوز شقة" أو "محتاج عقار" بدون تحديد إيجار أم بيع
        if (!isRent && !isSale && (isApartment || isShop || isOffice)) {
            val typeStr = if (isApartment) "الشقة" else if (isShop) "المحل" else "المكتب"
            return AiQueryState.ClarificationNeeded(
                question = "أهلاً بك! بخصوص $typeStr المطلوبة في دسوق، هل تبحث عن **إيجار شهري** أم **شراء وتمليك**؟",
                suggestionChips = listOf("🔑 إيجار", "🏠 شراء وتمليك", "في شارع الجيش", "ميزانية محددة")
            )
        }

        // فحص العقارات الحقيقية الموجودة في قاعدة البيانات
        val filtered = allProperties.filter { prop ->
            var matches = true

            // تصنيف العقار
            if (isApartment && prop.category != "شقق" && !prop.title.contains("شقة")) matches = false
            if (isShop && prop.category != "محلات" && !prop.title.contains("محل")) matches = false
            if (isOffice && prop.category != "مكاتب" && !prop.title.contains("مكتب")) matches = false
            if (isLand && prop.category != "أراضي" && !prop.title.contains("أرض")) matches = false
            if (isHouse && prop.category != "منازل" && !prop.title.contains("منزل") && !prop.title.contains("عمارة")) matches = false

            // نوع العرض (بيع أم إيجار)
            if (isRent && prop.type != "إيجار" && prop.priceUnit != "ج/شهرياً" && prop.priceUnit != "جنيه/شهرياً") matches = false
            if (isSale && prop.type != "بيع" && prop.type != "تمليك" && prop.priceUnit.contains("شهرياً")) matches = false

            // عدد الغرف
            if (targetRooms != null && prop.rooms != null && prop.rooms > 0) {
                if (prop.rooms != targetRooms) matches = false
            }

            // المنطقة
            if (matchedDistrict != null) {
                val propLoc = (prop.district + " " + prop.location).lowercase()
                if (!propLoc.contains(matchedDistrict.lowercase())) matches = false
            }

            // الميزانية
            if (maxPrice != null && prop.price > 0) {
                if (prop.price > maxPrice) matches = false
            }

            matches
        }

        // إذا وجدنا عقارات مطابقة
        if (filtered.isNotEmpty()) {
            val countStr = if (filtered.size == 1) "عقار واحد مطابق" else "${filtered.size} عقارات مطابقة"
            val rentSaleWord = if (isRent) "للإيجار" else if (isSale) "للبيع" else ""
            val replyText = """
                وجدت لك **$countStr** $rentSaleWord في قاعدة بيانات دسوق مطابقة تماماً لمواصفات طلبك:
            """.trimIndent()
            return AiQueryState.Success(
                reply = replyText,
                matchingProperties = filtered.take(6)
            )
        }

        // إذا لم نجد نتائج مطابقة، نعرض الرسالة الدقيقة مع زر نشر الطلب
        val querySummary = buildString {
            if (isApartment) append("شقة ")
            if (isShop) append("محل ")
            if (isLand) append("أرض ")
            if (isRent) append("للإيجار ")
            if (isSale) append("للبيع ")
            if (matchedDistrict != null) append("في $matchedDistrict ")
            if (targetRooms != null) append("($targetRooms غرف) ")
            if (maxPrice != null) append("بسعر حتى $maxPrice جنيه")
        }.trim()

        return AiQueryState.NoMatch(
            message = "لم أجد حاليًا عقارًا مطابقًا لطلبك في قاعدة البيانات المتاحة، ويمكنك نشر طلبك في المجتمع العقاري ليتواصل معك أصحاب المكاتب والملاك مباشرة.",
            suggestedPostType = "🔎 مطلوب عقار",
            prefilledContent = if (querySummary.isNotBlank()) "مطلوب $querySummary في دسوق" else rawQuery
        )
    }

    /**
     * استخراج الميزانية القصوى من النصوص العامية
     */
    private fun extractMaxPrice(text: String): Double? {
        // "بأقل من مليون" أو "لحد مليون"
        if (text.contains("أقل من مليون") || text.contains("اقل من مليون") || text.contains("لحد مليون")) return 1_000_000.0
        if (text.contains("أقل من 2 مليون") || text.contains("اقل من اتنين مليون")) return 2_000_000.0
        if (text.contains("أقل من 500 ألف") || text.contains("نصف مليون")) return 500_000.0

        // "لحد 7000 جنيه" أو "بحد أقصى 6000" أو "أقل من 8000"
        val maxRegex = Regex("""(?:لحد|أقل من|اقل من|حد أقصى|حد اقصى|سعر|بـ|بحدود|ميزانية)\s*(\d+(?:,\d+)?)\s*(?:جنيه|ج|ألف|الف)?""")
        val match = maxRegex.find(text)
        if (match != null) {
            val numStr = match.groupValues[1].replace(",", "")
            val num = numStr.toDoubleOrNull()
            if (num != null) {
                // إذا كتب "لحد 7000" وكان إيجار
                return if (text.contains("ألف") || text.contains("الف")) num * 1000.0 else num
            }
        }

        // فحص الأرقام الصريحة
        val numMatch = Regex("""\b(\d{4,8})\b""").find(text)
        if (numMatch != null) {
            return numMatch.groupValues[1].toDoubleOrNull()
        }

        return null
    }

    /**
     * المطابقة الذكية بين منشورات "مطلوب عقار" والعقارات المعروضة في التطبيق
     * لاقتراحها تلقائياً للمستخدم
     */
    fun findMatchingPropertiesForRequest(
        postContent: String,
        allProperties: List<PropertyEntity>
    ): List<PropertyEntity> {
        val extracted = extractStructuredData(postContent)
        val clean = postContent.lowercase()

        val isRent = clean.contains("إيجار") || clean.contains("ايجار")
        val isSale = clean.contains("بيع") || clean.contains("شراء") || clean.contains("تمليك")

        val results = allProperties.filter { prop ->
            var score = 0

            // تطابق النوع
            if (extracted.propertyType.isNotBlank() && (prop.category.contains(extracted.propertyType) || prop.title.contains(extracted.propertyType))) {
                score += 3
            }

            // تطابق الموقع
            if (extracted.location.isNotBlank() && extracted.location != "دسوق" && extracted.location != "مدينة دسوق") {
                if (prop.district.contains(extracted.location) || prop.location.contains(extracted.location)) {
                    score += 3
                }
            }

            // تطابق نوع العرض
            if (isRent && (prop.type == "إيجار" || prop.priceUnit.contains("شهرياً"))) {
                score += 2
            } else if (isSale && (prop.type == "بيع" || prop.type == "تمليك")) {
                score += 2
            }

            score >= 3
        }

        return results.take(3)
    }
}
