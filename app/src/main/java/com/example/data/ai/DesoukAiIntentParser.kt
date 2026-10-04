package com.example.data.ai

import java.util.regex.Pattern

/**
 * محلل النوايا ومعايير البحث باللغة الطبيعية واللهجة المصرية
 * يفهم الأخطاء الإملائية الشائعة، التعبيرات الدارجة في دسوق، والأرقام باللغة العربية
 */
class DesoukAiIntentParser {

    // ذاكرة السياق للبحث الجاري عبر الجلسة
    private var currentCriteria: AiSearchCriteria = AiSearchCriteria()

    fun resetContext() {
        currentCriteria = AiSearchCriteria()
    }

    fun getCurrentCriteria(): AiSearchCriteria = currentCriteria

    fun parse(userText: String): Pair<AiIntent, AiSearchCriteria> {
        val clean = normalizeArabic(userText)

        // 1. فحص النوايا المتخصصة أولاً
        val intent = detectIntent(clean)

        // 2. تحديث وتجميع معايير البحث مع مراعاة التصحيحات وسياق المحادثة
        val updatedCriteria = extractSearchCriteria(clean, currentCriteria)
        currentCriteria = updatedCriteria

        return Pair(intent, updatedCriteria)
    }

    private fun detectIntent(text: String): AiIntent {
        return when {
            // مقارنة بين عقارات
            text.contains("قارن") || text.contains("مقارن") || text.contains("مين ارخص") || text.contains("مين اكبر") ->
                AiIntent.COMPARE_PROPERTIES

            // تحليل السعر وسعر المتر
            text.contains("حلل السعر") || text.contains("تحليل السعر") || text.contains("سعر المتر") ||
                    text.contains("متوسط الاسعار") || text.contains("هل السعر مناسب") || text.contains("تقييم السعر") ->
                AiIntent.VALUATE_PROPERTY

            // التحليل الاستثماري
            text.contains("استثمار") || text.contains("عائد") || text.contains("ايجار متوقع") ||
                    text.contains("استثماريا") || text.contains("ارباح العقار") ->
                AiIntent.INVESTMENT_ANALYSIS

            // مساعد الشراء التفاعلي
            text.contains("ساعدني اشتري") || text.contains("خطوات الشراء") || text.contains("مرشد الشراء") ->
                AiIntent.BUYER_ASSISTANT

            // مساعد الإيجار
            text.contains("ساعدني استاجر") || text.contains("عايز استاجر") || text.contains("ساعدني اجر") ->
                AiIntent.RENTAL_ASSISTANT

            // مساعد البيع
            text.contains("ساعدني ابيع") || text.contains("عايز ابيع") || text.contains("اعرض عقاري للبيع") ->
                AiIntent.SELLER_ASSISTANT

            // تحسين أو كتابة إعلان
            text.contains("حسن الاعلان") || text.contains("تحسين الاعلان") || text.contains("اكتب اعلان") ||
                    text.contains("صيغه اعلان") || text.contains("عنوان اعلان") || text.contains("وصف احترافي") ->
                AiIntent.IMPROVE_LISTING

            // تقييم جودة الإعلان
            text.contains("جوده الاعلان") || text.contains("مراجعه الاعلان") || text.contains("فحص الاعلان") ->
                AiIntent.EVALUATE_LISTING_QUALITY

            // قائمة فحص المعاينة
            text.contains("قائمه فحص") || text.contains("فحص العقار") || text.contains("فحص المعاينه") ||
                    text.contains("معاينه العقار") || text.contains("تشييك") ->
                AiIntent.PROPERTY_CHECKLIST

            // أسئلة ما قبل الشراء والقانونية
            text.contains("اسال عنه") || text.contains("قبل ما اشتري") || text.contains("اوراق الملكيه") ||
                    text.contains("نصائح قانونيه") || text.contains("الشهر العقاري") || text.contains("رخصه المباني") ->
                AiIntent.PRE_PURCHASE_QUESTIONS

            // حفظ البحث
            text.contains("احفظ البحث") || text.contains("حفظ البحث") || text.contains("نبهني") ||
                    text.contains("تنبيه عند توفر") || text.contains("فعل التنبيه") ->
                AiIntent.SAVE_SEARCH

            // البحث والمقارنة داخل المفضلة
            (text.contains("مفضل") || text.contains("محفوظ")) && (text.contains("ارخص") || text.contains("اكبر") || text.contains("قارن")) ->
                AiIntent.SEARCH_FAVORITES

            // عرض المفضلة
            text.contains("المفضله") || text.contains("المحفوظات") || text.contains("عقاراتي المحفوظه") ->
                AiIntent.SHOW_FAVORITES

            // مساعد الوسيط العقاري
            text.contains("مساعد الوسيط") || text.contains("طلبات العملاء") || text.contains("مطابقه العميل") ||
                    text.contains("اداره العملاء") || text.contains("سمسار") ->
                AiIntent.BROKER_CLIENT_MATCH

            // لوحة تحكم الإدارة وتحليل المنظومة
            text.contains("تحليل الاداره") || text.contains("تقرير النشاط") || text.contains("تقرير الشهر") ||
                    text.contains("احصائيات التطبيق") ->
                AiIntent.ADMIN_ANALYSIS

            // اكتشاف البيانات الناقصة
            text.contains("بيانات ناقصه") || text.contains("عقارات ناقصه") || text.contains("بدون صور") ||
                    text.contains("بدون سعر") ->
                AiIntent.ADMIN_AUDIT_MISSING

            // اكتشاف الإعلانات المكررة
            text.contains("اعلانات مكرره") || text.contains("عقارات مكرره") || text.contains("تكرار") ->
                AiIntent.ADMIN_AUDIT_DUPLICATES

            // البحث على الخريطة
            text.contains("خريطه") || text.contains("الخريطه") || text.contains("لوكيشن") || text.contains("موقع جغرافي") ->
                AiIntent.SEARCH_MAP

            // افتراضي: بحث عن عقارات إذا توفرت معايير عقارية
            text.contains("شقه") || text.contains("منزل") || text.contains("ارض") || text.contains("محل") ||
                    text.contains("مكتب") || text.contains("بيع") || text.contains("ايجار") || text.contains("مليون") ||
                    text.contains("الف") || text.contains("اوض") || text.contains("غرف") || text.contains("دسوق") ||
                    text.contains("دور") || text.contains("عايز") || text.contains("عاوز") || text.contains("بدور") ->
                AiIntent.SEARCH_PROPERTY

            else -> AiIntent.GENERAL_QA
        }
    }

    private fun extractSearchCriteria(text: String, prev: AiSearchCriteria): AiSearchCriteria {
        var category = prev.category
        var type = prev.type
        var district = prev.district
        var street = prev.street
        var minPrice = prev.minPrice
        var maxPrice = prev.maxPrice
        var minArea = prev.minArea
        var maxArea = prev.maxArea
        var rooms = prev.rooms
        var bathrooms = prev.bathrooms
        var floor = prev.floor
        var finishing = prev.finishing
        var hasElevator = prev.hasElevator
        var hasGarage = prev.hasGarage
        var hasMeters = prev.hasMeters
        var sortBy = prev.sortBy

        // 1. تصحيحات المستخدم في السياق (Context Corrections)
        if (text.contains("ايجار مش بيع") || text.contains("قصدي ايجار") || text.contains("للايجار")) {
            type = "للإيجار"
        } else if (text.contains("بيع مش ايجار") || text.contains("قصدي بيع") || text.contains("للبيع") || text.contains("تمليك")) {
            type = "للبيع"
        }

        // 2. نوع العقار (Category)
        when {
            text.contains("شقه") || text.contains("شقق") || text.contains("استوديو") -> category = "شقق"
            text.contains("منزل") || text.contains("منازل") || text.contains("بيت") || text.contains("بيوت") || text.contains("فيلا") || text.contains("عماره") -> category = "منازل"
            text.contains("ارض") || text.contains("اراضي") || text.contains("قطعه ارض") || text.contains("مباني") || text.contains("زراعيه") -> category = "أراضي"
            text.contains("وحده اداريه") || text.contains("وحدات اداريه") || text.contains("مكتب") || text.contains("مكاتب") || text.contains("عياده") -> category = "وحدات إدارية"
            text.contains("محل") || text.contains("محلات") || text.contains("تجاري") || text.contains("معرض") -> category = "محلات"
        }

        // 3. المناطق والشوارع بدسوق
        when {
            text.contains("كورنيش") || text.contains("الكورنيش") || text.contains("النيل") -> district = "كورنيش النيل"
            text.contains("شارع الجيش") || text.contains("الجيش") -> district = "شارع الجيش"
            text.contains("دحروج") -> district = "دحروج"
            text.contains("الصفا") || text.contains("حي الصفا") -> district = "الصفا"
            text.contains("الميدان") || text.contains("ابراهيم الدسوقي") || text.contains("الميدان الابراهيمي") -> district = "الميدان الإبراهيمي"
            text.contains("الشركات") || text.contains("شارع الشركات") -> district = "شارع الشركات"
            text.contains("المحطه") || text.contains("ميدان المحطه") -> district = "المحطة"
            text.contains("الزهور") || text.contains("حي الزهور") -> district = "حي الزهور"
            text.contains("بسيون") || text.contains("طريق بسيون") -> district = "طريق بسيون"
            text.contains("الاستاد") || text.contains("شارع الاستاد") -> district = "شارع الاستاد"
            text.contains("فوه") || text.contains("طريق فوه") -> district = "طريق فوه"
            text.contains("الجمهوريه") || text.contains("شارع الجمهوريه") -> district = "شارع الجمهورية"
            text.contains("النحريري") -> district = "شارع النحريري"
            text.contains("دسوق") && district == null -> district = "دسوق"
        }

        // 4. استخراج الميزانية والأسعار (Budget Parsing)
        val extractedBudget = parseBudget(text)
        if (extractedBudget.first != null) minPrice = extractedBudget.first
        if (extractedBudget.second != null) maxPrice = extractedBudget.second

        // زيادة أو تعديل الميزانية ("ارفع الميزانية لمليونين")
        if (text.contains("ارفع الميزانيه") || text.contains("زود السعر")) {
            val higherBudget = parseBudget(text)
            if (higherBudget.second != null) maxPrice = higherBudget.second
        }

        // 5. عدد الغرف (Rooms)
        when {
            text.contains("اوضتين") || text.contains("غرفتين") || text.contains("2 اوض") || text.contains("2 غرف") || text.contains("خليها غرفتين") || text.contains("خليها اوضتين") -> rooms = 2
            text.contains("3 اوض") || text.contains("3 غرف") || text.contains("ثلاث اوض") || text.contains("ثلاث غرف") || text.contains("تلات اوض") || text.contains("تلات غرف") -> rooms = 3
            text.contains("4 اوض") || text.contains("4 غرف") || text.contains("اربع اوض") || text.contains("اربع غرف") -> rooms = 4
            text.contains("5 اوض") || text.contains("5 غرف") || text.contains("خمس اوض") -> rooms = 5
            text.contains("غرفه واحده") || text.contains("اوضه واحده") || text.contains("1 غرفه") -> rooms = 1
        }

        // 6. الحمامات
        when {
            text.contains("حمامين") || text.contains("2 حمام") -> bathrooms = 2
            text.contains("3 حمام") || text.contains("3 حمامات") -> bathrooms = 3
            text.contains("1 حمام") || text.contains("حمام واحد") -> bathrooms = 1
        }

        // 7. المساحة (Area)
        val areaMatch = Pattern.compile("(\\d+)\\s*(متر|م)").matcher(text)
        if (areaMatch.find()) {
            val a = areaMatch.group(1)?.toDoubleOrNull()
            if (a != null) {
                if (text.contains("اكتر من") || text.contains("اكبر من") || text.contains("فوق")) {
                    minArea = a
                } else if (text.contains("اقل من") || text.contains("لحد")) {
                    maxArea = a
                } else {
                    minArea = a * 0.85
                    maxArea = a * 1.2
                }
            }
        }

        // 8. التشطيب والمرافق
        if (text.contains("سوبر لوكس") || text.contains("تشطيب كامل") || text.contains("متشطبه")) {
            finishing = "سوبر لوكس"
        } else if (text.contains("الترا لوكس") || text.contains("هاي لوكس")) {
            finishing = "ألترا لوكس"
        } else if (text.contains("نصف تشطيب") || text.contains("نص تشطيب")) {
            finishing = "نصف تشطيب"
        } else if (text.contains("محاره") || text.contains("على المحاره")) {
            finishing = "على المحارة"
        }

        if (text.contains("اسانسير") || text.contains("مصعد")) hasElevator = true
        if (text.contains("جراج") || text.contains("باكيه")) hasGarage = true
        if (text.contains("عدادات") || text.contains("غاز") || text.contains("مياه وكهربا")) hasMeters = true

        // 9. الترتيب (Sorting)
        when {
            text.contains("ارخص") || text.contains("اقل سعر") -> sortBy = "price_asc"
            text.contains("اغلى") || text.contains("اعلى سعر") -> sortBy = "price_desc"
            text.contains("اكبر") || text.contains("اوسع") -> sortBy = "area_desc"
            text.contains("احدث") || text.contains("جديد") -> sortBy = "newest"
        }

        return AiSearchCriteria(
            category = category,
            type = type,
            district = district,
            street = street,
            minPrice = minPrice,
            maxPrice = maxPrice,
            minArea = minArea,
            maxArea = maxArea,
            rooms = rooms,
            bathrooms = bathrooms,
            floor = floor,
            finishing = finishing,
            hasElevator = hasElevator,
            hasGarage = hasGarage,
            hasMeters = hasMeters,
            sortBy = sortBy,
            rawQuery = text
        )
    }

    /**
     * استخراج حدود الميزانية من النص المصري
     * مثل: "معايا مليون", "لحد مليون ونص", "من مليون لمليونين", "في حدود 800 الف"
     */
    private fun parseBudget(text: String): Pair<Double?, Double?> {
        var min: Double? = null
        var max: Double? = null

        // فحص "من X لـ Y"
        if (text.contains("من") && (text.contains("الي") || text.contains("ل") || text.contains("لحد"))) {
            if (text.contains("مليون") && (text.contains("مليون ونص") || text.contains("مليونين") || text.contains("2 مليون"))) {
                min = 1000000.0
                max = if (text.contains("مليون ونص")) 1500000.0 else 2000000.0
                return Pair(min, max)
            }
        }

        // صيغ الملايين الدارجة
        when {
            text.contains("مليون ونص") || text.contains("مليون ونصف") || text.contains("1.5 مليون") || text.contains("1 ونص مليون") -> {
                max = 1500000.0
            }
            text.contains("مليونين") || text.contains("2 مليون") || text.contains("اتنين مليون") -> {
                max = 2000000.0
            }
            text.contains("3 مليون") || text.contains("ثلاثه مليون") || text.contains("تلاته مليون") -> {
                max = 3000000.0
            }
            text.contains("4 مليون") || text.contains("اربعه مليون") -> {
                max = 4000000.0
            }
            text.contains("مليون وربع") || text.contains("مليون و250") -> {
                max = 1250000.0
            }
            text.contains("مليون") -> {
                max = 1000000.0
            }
            text.contains("نص مليون") || text.contains("خمسميه الف") || text.contains("500 الف") -> {
                max = 500000.0
            }
            text.contains("800 الف") || text.contains("تمنميه الف") -> {
                max = 800000.0
            }
            text.contains("700 الف") || text.contains("سبعميه الف") -> {
                max = 700000.0
            }
            text.contains("600 الف") || text.contains("ستميه الف") -> {
                max = 600000.0
            }
            text.contains("400 الف") || text.contains("اربعميه الف") -> {
                max = 400000.0
            }
            text.contains("300 الف") || text.contains("تلتوميه الف") -> {
                max = 300000.0
            }
            text.contains("200 الف") || text.contains("متين الف") -> {
                max = 200000.0
            }
        }

        // إذا كان إيجار شهري وذكر آلاف ("3 الاف", "4 الاف", "5000", "6000")
        if (text.contains("ايجار") || text.contains("شهري")) {
            val rentPattern = Pattern.compile("(\\d+)\\s*(الاف|الف|ج|جنيه)").matcher(text)
            if (rentPattern.find()) {
                val num = rentPattern.group(1)?.toDoubleOrNull()
                if (num != null && num < 50) {
                    max = num * 1000.0
                }
            }
        }

        // فحص أرقام مباشرة مثل: 1500000 أو 800000
        val directPattern = Pattern.compile("(\\d{5,8})").matcher(text)
        if (directPattern.find()) {
            val num = directPattern.group(1)?.toDoubleOrNull()
            if (num != null) max = num
        }

        if (max != null && (text.contains("في حدود") || text.contains("حوالي"))) {
            min = max * 0.8
            max = max * 1.15
        }

        return Pair(min, max)
    }

    /**
     * توحيد الحروف العربية وإزالة التشكيل والهمزات والأخطاء الشائعة
     */
    private fun normalizeArabic(text: String): String {
        var str = text.trim().lowercase()
        // توحيد الهمزات والألف
        str = str.replace(Regex("[إأآا]"), "ا")
        str = str.replace("ة", "ه")
        str = str.replace("ى", "ي")
        str = str.replace(Regex("[\\u064B-\\u065F]"), "") // إزالة التشكيل
        // تحويل الأرقام الهندية / العربية المشرقية إلى أرقام لاتينية قياسية
        val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        for (i in arabicDigits.indices) {
            str = str.replace(arabicDigits[i], ('0'.code + i).toChar())
        }
        return str
    }
}
