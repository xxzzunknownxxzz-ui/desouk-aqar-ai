package com.example.data.ai

import com.example.data.PropertyEntity

/**
 * الأنواع المختلفة لنوايا واستفسارات المستخدم في المساعد العقاري الذكي
 */
enum class AiIntent {
    SEARCH_PROPERTY,         // بحث عن عقار بمواصفات
    COMPARE_PROPERTIES,      // مقارنة بين عقارين أو أكثر
    VALUATE_PROPERTY,        // تحليل سعر العقار وسعر المتر
    INVESTMENT_ANALYSIS,     // تحليل الاستثمار والعائد الإيجاري
    BUYER_ASSISTANT,         // ساعدني أشتري (مرشد الشراء التفاعلي)
    RENTAL_ASSISTANT,        // ساعدني أستأجر
    SELLER_ASSISTANT,        // ساعدني أبيع عقاري
    IMPROVE_LISTING,         // تحسين الإعلان بالذكاء الاصطناعي
    EVALUATE_LISTING_QUALITY,// تقييم جودة الإعلان واكتماله
    PROPERTY_CHECKLIST,      // قائمة فحص المعاينة (الكهرباء، السباكة، الدهانات...)
    PRE_PURCHASE_QUESTIONS,  // إيه اللي أسأل عنه قبل ما أشتري (قانونيًا وفنيًا)
    SAVE_SEARCH,             // حفظ البحث وتفعيل التنبيهات
    SHOW_FAVORITES,          // عرض المفضلة
    SEARCH_FAVORITES,        // البحث والمقارنة داخل المفضلة
    BROKER_CLIENT_MATCH,     // مساعد الوسيط: مطابقة العملاء بالعقارات
    ADMIN_ANALYSIS,          // مساعد الإدارة: تقرير إحصائي للمنظومة
    ADMIN_AUDIT_MISSING,     // اكتشاف البيانات الناقصة في العقارات
    ADMIN_AUDIT_DUPLICATES,  // اكتشاف الإعلانات المكررة
    SEARCH_MAP,              // البحث على الخريطة
    GENERAL_QA               // أسئلة واستفسارات عامة عن السوق
}

/**
 * معايير البحث المستخرجة آليًا من استفسار المستخدم باللغة الطبيعية
 */
data class AiSearchCriteria(
    val category: String? = null,        // شقق, منازل, محلات, أراضي, وحدات إدارية
    val type: String? = null,            // للبيع, للإيجار
    val district: String? = null,        // كورنيش النيل, شارع الجيش, دحروج, الصفا, etc.
    val street: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val minArea: Double? = null,
    val maxArea: Double? = null,
    val rooms: Int? = null,
    val bathrooms: Int? = null,
    val floor: Int? = null,
    val finishing: String? = null,       // سوبر لوكس, ألترا لوكس, نصف تشطيب, etc.
    val hasElevator: Boolean? = null,
    val hasGarage: Boolean? = null,
    val hasMeters: Boolean? = null,
    val sortBy: String? = null,          // price_asc, price_desc, area_desc, match_score
    val rawQuery: String = ""
)

/**
 * نتيجة تطابق العقار مع نظام الـ Match Score
 */
data class PropertyMatchResult(
    val property: PropertyEntity,
    val matchScore: Int,                 // من 0 إلى 100
    val matchSummary: String,            // مثل: "تطابق مرتفع (95%) مع السعر والمساحة والمنطقة"
    val differences: List<String> = emptyList(), // في البحث المرن: "أعلى بـ 100 ألف ج"
    val isExact: Boolean = true
)

/**
 * بيانات مقارنة العقارات جنبًا إلى جنب
 */
data class AiComparisonData(
    val properties: List<PropertyEntity>,
    val comparisonPoints: List<ComparisonRow> = emptyList(),
    val summaryRecommendation: String = ""
)

data class ComparisonRow(
    val title: String,
    val values: List<String>
)

/**
 * بيانات تحليل السعر وسعر المتر الاسترشادي
 */
data class AiValuationData(
    val property: PropertyEntity?,
    val calculatedPricePerMeter: Double,
    val averagePricePerMeterInDistrict: Double,
    val priceDifferencePercentage: Double,
    val analysisText: String,
    val disclaimer: String = "هذا تحليل استرشادي مبني على البيانات المتاحة داخل التطبيق وليس تقييمًا رسميًا."
)

/**
 * بيانات التحليل الاستثماري والعائد الإيجاري
 */
data class AiInvestmentData(
    val property: PropertyEntity,
    val purchasePrice: Double,
    val pricePerMeter: Double,
    val estimatedMonthlyRent: Double,
    val annualRentalYieldPercentage: Double,
    val estimatedPaybackYears: Double,
    val marketComparisonSummary: String,
    val isLimitedData: Boolean = false,
    val disclaimer: String = "التحليل الاستثماري مبني على متوسط أسعار الإيجار الفعلي المسجل بتطبيق دسوق، وليس ضمانًا مستقبليًا للأرباح."
)

/**
 * عناصر قائمة فحص المعاينة وقائمة ما قبل الشراء
 */
data class ChecklistItem(
    val id: String,
    val title: String,
    val description: String,
    val category: String, // قانوني, كهرباء, سباكة, تشطيب, مرافق
    var isChecked: Boolean = false
)

/**
 * الأزرار والإجراءات السريعة التفاعلية المرفقة مع رد المساعد
 */
data class AiQuickAction(
    val id: String,
    val label: String,
    val queryToTrigger: String,
    val iconName: String = "default"
)

/**
 * الحمولة الشاملة لرد المساعد العقاري الذكي
 */
data class AiResponsePayload(
    val intent: AiIntent,
    val responseText: String,
    val matchedProperties: List<PropertyMatchResult> = emptyList(),
    val comparisonData: AiComparisonData? = null,
    val valuationData: AiValuationData? = null,
    val investmentData: AiInvestmentData? = null,
    val checklistItems: List<ChecklistItem> = emptyList(),
    val quickActions: List<AiQuickAction> = emptyList(),
    val isFlexibleFallback: Boolean = false,
    val searchCriteria: AiSearchCriteria? = null
)
