package com.example.data.ai

import com.example.data.CustomerRequestEntity
import com.example.data.PropertyEntity
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * المحرك الحسابي والبحثي الذكي لعقارات دسوق
 * ينفذ المطابقة، البحث المرن، المقارنات، تقييم سعر المتر، التحليل الاستثماري، والتدقيق الإداري
 * يعتمد حصريًا على البيانات الحقيقية من قاعدة بيانات التطبيق دون أي بيانات وهمية.
 */
class DesoukAiSearchEngine {

    private val numberFormat = NumberFormat.getNumberInstance(Locale("ar", "EG"))

    /**
     * البحث الذكي مع حساب Match Score والبحث المرن في حال عدم توفر تطابق تام
     */
    fun search(criteria: AiSearchCriteria, allProperties: List<PropertyEntity>): Pair<List<PropertyMatchResult>, Boolean> {
        if (allProperties.isEmpty()) return Pair(emptyList(), false)

        val exactMatches = mutableListOf<PropertyMatchResult>()
        val flexibleMatches = mutableListOf<PropertyMatchResult>()

        for (p in allProperties) {
            val scoreResult = calculateMatchScore(p, criteria)
            if (scoreResult.isExact && scoreResult.matchScore >= 70) {
                exactMatches.add(scoreResult)
            } else if (scoreResult.matchScore >= 45) {
                flexibleMatches.add(scoreResult)
            }
        }

        // إذا وُجدت نتائج مطابقة تماماً
        if (exactMatches.isNotEmpty()) {
            val sorted = sortMatches(exactMatches, criteria.sortBy)
            return Pair(sorted.take(6), false)
        }

        // في حال عدم وجود تطابق تام: الانتقال التلقائي للبحث المرن (Flexible Search)
        val sortedFlexible = flexibleMatches
            .sortedByDescending { it.matchScore }
            .take(4)

        return Pair(sortedFlexible, true)
    }

    /**
     * حساب درجة التطابق (0-100) وتحديد الفروق الدقيقة للبحث المرن
     */
    private fun calculateMatchScore(property: PropertyEntity, criteria: AiSearchCriteria): PropertyMatchResult {
        var score = 0
        var isExact = true
        val reasons = mutableListOf<String>()
        val differences = mutableListOf<String>()

        // 1. نوع العقار (Category) - 25 نقطة
        if (criteria.category != null) {
            val normalizedCat = normalize(criteria.category)
            val propCat = normalize(property.category)
            if (propCat.contains(normalizedCat) || normalizedCat.contains(propCat)) {
                score += 25
                reasons.add(property.category)
            } else {
                isExact = false
                differences.add("نوع العقار (${property.category}) مختلف عما طلبت (${criteria.category})")
            }
        } else {
            score += 25
        }

        // 2. نوع المعاملة (بيع / إيجار) - 20 نقطة
        if (criteria.type != null) {
            val normalizedType = normalize(criteria.type)
            val propType = normalize(property.type)
            if (propType.contains(normalizedType) || normalizedType.contains(propType)) {
                score += 20
                reasons.add(property.type)
            } else {
                isExact = false
                differences.add("العقار معروض ${property.type} وليس ${criteria.type}")
            }
        } else {
            score += 20
        }

        // 3. المنطقة والشارع - 20 نقطة
        if (criteria.district != null && criteria.district != "دسوق") {
            val normalizedDist = normalize(criteria.district)
            val propDist = normalize(property.district + " " + property.location + " " + property.street)
            if (propDist.contains(normalizedDist)) {
                score += 20
                reasons.add(property.district)
            } else {
                isExact = false
                differences.add("يقع في ${property.district} بدلًا من ${criteria.district}")
            }
        } else {
            score += 20
        }

        // 4. السعر والميزانية - 15 نقطة
        if (criteria.maxPrice != null && criteria.maxPrice > 0) {
            if (property.price <= criteria.maxPrice) {
                score += 15
                reasons.add("ضمن الميزانية (${formatCurrency(property.price)})")
            } else {
                val diff = property.price - criteria.maxPrice
                val diffPercentage = (diff / criteria.maxPrice) * 100
                if (diffPercentage <= 20) {
                    score += 8
                    isExact = false
                    differences.add("أعلى من ميزانيتك بـ ${formatCurrency(diff)}")
                } else {
                    isExact = false
                    differences.add("أعلى من ميزانيتك بـ ${formatCurrency(diff)}")
                }
            }
        } else {
            score += 15
        }

        // 5. عدد الغرف - 10 نقاط
        if (criteria.rooms != null && criteria.rooms > 0) {
            if (property.rooms == criteria.rooms) {
                score += 10
                reasons.add("${property.rooms} غرف")
            } else if (abs(property.rooms - criteria.rooms) == 1) {
                score += 5
                isExact = false
                differences.add("${property.rooms} غرف بدلًا من ${criteria.rooms}")
            } else {
                isExact = false
                differences.add("${property.rooms} غرف")
            }
        } else {
            score += 10
        }

        // 6. المساحة والتشطيب والمصعد - 10 نقاط
        var featureScore = 0
        if (criteria.minArea != null && criteria.minArea > 0) {
            if (property.area >= criteria.minArea) {
                featureScore += 4
                reasons.add("${property.area.toInt()} م²")
            } else {
                isExact = false
                differences.add("المساحة ${property.area.toInt()} م² أقل مما طلبت")
            }
        } else {
            featureScore += 4
        }

        if (criteria.hasElevator == true) {
            if (property.hasElevator) {
                featureScore += 3
                reasons.add("أسانسير")
            } else {
                isExact = false
                differences.add("بدون أسانسير")
            }
        } else {
            featureScore += 3
        }

        if (criteria.finishing != null) {
            if (property.finishing.contains(criteria.finishing) || criteria.finishing.contains(property.finishing)) {
                featureScore += 3
                reasons.add(property.finishing)
            } else {
                featureScore += 1
            }
        } else {
            featureScore += 3
        }
        score += featureScore

        val finalScore = score.coerceIn(0, 100)
        val matchSummary = if (isExact) {
            "تطابق ممتاز ($finalScore%): يطابق " + reasons.take(3).joinToString(" و ")
        } else {
            "تطابق تقريبي ($finalScore%): " + if (differences.isNotEmpty()) differences.first() else "مواصفات قريبة"
        }

        return PropertyMatchResult(
            property = property,
            matchScore = finalScore,
            matchSummary = matchSummary,
            differences = differences,
            isExact = isExact
        )
    }

    private fun sortMatches(matches: List<PropertyMatchResult>, sortBy: String?): List<PropertyMatchResult> {
        return when (sortBy) {
            "price_asc" -> matches.sortedBy { it.property.price }
            "price_desc" -> matches.sortedByDescending { it.property.price }
            "area_desc" -> matches.sortedByDescending { it.property.area }
            "newest" -> matches.sortedByDescending { it.property.timestamp }
            else -> matches.sortedByDescending { it.matchScore }
        }
    }

    /**
     * مقارنة دقيقة بين عقارين أو أكثر بناء على مواصفاتهم الحقيقية
     */
    fun compareProperties(properties: List<PropertyEntity>): AiComparisonData {
        if (properties.isEmpty()) {
            return AiComparisonData(emptyList(), emptyList(), "لا توجد عقارات محددة للمقارنة.")
        }

        val rows = mutableListOf<ComparisonRow>()

        rows.add(ComparisonRow("السعر", properties.map { formatCurrency(it.price) + " " + it.priceUnit }))
        rows.add(ComparisonRow("المساحة", properties.map { "${it.area.toInt()} م²" }))
        rows.add(ComparisonRow("سعر المتر التقريبي", properties.map {
            if (it.area > 0) formatCurrency((it.price / it.area).roundToInt().toDouble()) + " ج/م²" else "غير محدد"
        }))
        rows.add(ComparisonRow("عدد الغرف", properties.map { "${it.rooms} غرف" }))
        rows.add(ComparisonRow("الحمامات", properties.map { "${it.bathrooms}" }))
        rows.add(ComparisonRow("الدور", properties.map { "الدور ${it.floor} من ${it.totalFloors}" }))
        rows.add(ComparisonRow("التشطيب", properties.map { it.finishing }))
        rows.add(ComparisonRow("الموقع والحي", properties.map { it.district + " - " + it.location }))
        rows.add(ComparisonRow("المصعد (الأسانسير)", properties.map { if (it.hasElevator) "متوفر ✅" else "غير متوفر ❌" }))
        rows.add(ComparisonRow("الجراج", properties.map { if (it.hasGarage) "متوفر ✅" else "غير متوفر ❌" }))
        rows.add(ComparisonRow("العدادات والمرافق", properties.map { if (it.hasMeters) "كاملة (غاز، كهرباء، مياه) ✅" else "تحتاج توصيل ⚠️" }))

        // توليد ملخص موضوعي دون مبالغة
        val cheapest = properties.minByOrNull { it.price }
        val largest = properties.maxByOrNull { it.area }
        val lowestMeter = properties.filter { it.area > 0 }.minByOrNull { it.price / it.area }

        val summary = StringBuilder("💡 **خلاصة المقارنة الاسترشادية:**\n")
        if (cheapest != null) {
            summary.append("• الأقل سعرًا: **${cheapest.title}** بسعر ${formatCurrency(cheapest.price)} ج.م.\n")
        }
        if (largest != null && largest.id != cheapest?.id) {
            summary.append("• الأكبر مساحة: **${largest.title}** بمساحة ${largest.area.toInt()} م².\n")
        }
        if (lowestMeter != null) {
            val meterPrice = (lowestMeter.price / lowestMeter.area).roundToInt()
            summary.append("• أفضل سعر متر: **${lowestMeter.title}** بنحو ${formatCurrency(meterPrice.toDouble())} ج/م² في ${lowestMeter.district}.\n")
        }

        return AiComparisonData(
            properties = properties,
            comparisonPoints = rows,
            summaryRecommendation = summary.toString()
        )
    }

    /**
     * تحليل سعر العقار وحساب سعر المتر استرشادياً ومقارنته بمتوسط الحي
     */
    fun valuateProperty(property: PropertyEntity, allProperties: List<PropertyEntity>): AiValuationData {
        val calculatedPricePerMeter = if (property.area > 0) property.price / property.area else 0.0

        // البحث عن عقارات مشابهة في نفس الحي ونفس الفئة ولها مساحة وسعر صحيح
        val similarInDistrict = allProperties.filter {
            it.id != property.id &&
                    it.district == property.district &&
                    it.category == property.category &&
                    it.type == property.type &&
                    it.area > 0 &&
                    it.price > 0
        }

        if (similarInDistrict.isEmpty()) {
            return AiValuationData(
                property = property,
                calculatedPricePerMeter = calculatedPricePerMeter,
                averagePricePerMeterInDistrict = 0.0,
                priceDifferencePercentage = 0.0,
                analysisText = "سعر المتر المحسوب لهذا العقار: **${formatCurrency(calculatedPricePerMeter.roundToInt().toDouble())} جنيه/م²**.\n(لا توجد حاليًا عقارات مماثلة كافية في قاعدة بيانات ${property.district} لحساب متوسط دقيق، لذا نوصي بالمعاينة والتأكد من السوق محليًا)."
            )
        }

        val avgDistrictMeterPrice = similarInDistrict
            .map { it.price / it.area }
            .average()

        val diffPercentage = if (avgDistrictMeterPrice > 0) {
            ((calculatedPricePerMeter - avgDistrictMeterPrice) / avgDistrictMeterPrice) * 100
        } else 0.0

        val comparisonNote = when {
            diffPercentage > 15 -> "سعر المتر لهذا العقار **أعلى بنحو ${diffPercentage.roundToInt()}%** من متوسط العقارات المشابهة في ${property.district} (${formatCurrency(avgDistrictMeterPrice.roundToInt().toDouble())} ج/م²)."
            diffPercentage < -15 -> "سعر المتر لهذا العقار **أقل بنحو ${abs(diffPercentage.roundToInt())}%** من متوسط ${property.district}، وهو ما قد يمثل فرصة سعرية مميزة إذا كانت الحالة الفنية ممتازة."
            else -> "سعر المتر لهذا العقار **متوافق جدًا مع متوسط أسعار ${property.district}** (متوسط الحي: ${formatCurrency(avgDistrictMeterPrice.roundToInt().toDouble())} ج/م²)."
        }

        val analysisText = """
            📊 **تحليل سعر العقار:**
            • سعر العقار الإجمالي: **${formatCurrency(property.price)} ${property.priceUnit}**
            • المساحة: **${property.area.toInt()} م²**
            • سعر المتر المحسوب: **${formatCurrency(calculatedPricePerMeter.roundToInt().toDouble())} جنيه**
            • متوسط سعر المتر في ${property.district}: **${formatCurrency(avgDistrictMeterPrice.roundToInt().toDouble())} جنيه**
            • المقارنة: $comparisonNote
        """.trimIndent()

        return AiValuationData(
            property = property,
            calculatedPricePerMeter = calculatedPricePerMeter,
            averagePricePerMeterInDistrict = avgDistrictMeterPrice,
            priceDifferencePercentage = diffPercentage,
            analysisText = analysisText
        )
    }

    /**
     * التحليل الاستثماري وحساب العائد الإيجاري المتوقع وفترة استرداد رأس المال
     */
    fun analyzeInvestment(property: PropertyEntity, allProperties: List<PropertyEntity>): AiInvestmentData {
        val pricePerMeter = if (property.area > 0) property.price / property.area else 0.0

        // جلب عقارات إيجار حقيقية في نفس المنطقة والفئة لحساب القيمة الإيجارية الفعلية
        val rentalListings = allProperties.filter {
            it.type == "للإيجار" &&
                    it.district == property.district &&
                    it.category == property.category &&
                    it.price > 0
        }

        val estimatedMonthlyRent: Double
        val isLimitedData: Boolean

        if (rentalListings.isNotEmpty()) {
            val avgRentalPrice = rentalListings.map { it.price }.average()
            estimatedMonthlyRent = avgRentalPrice
            isLimitedData = false
        } else {
            // تقدير استرشادي واقعي في دسوق (حوالي 0.35% إلى 0.45% شهرياً من قيمة العقار السكني)
            estimatedMonthlyRent = (property.price * 0.004).coerceAtLeast(2000.0)
            isLimitedData = true
        }

        val annualRent = estimatedMonthlyRent * 12
        val annualYield = if (property.price > 0) (annualRent / property.price) * 100 else 0.0
        val paybackYears = if (annualRent > 0) property.price / annualRent else 0.0

        val summary = if (isLimitedData) {
            "تم حساب الإيجار التقديري بنحو ${formatCurrency(estimatedMonthlyRent)} ج/شهريًا استنادًا للنسب السائدة بدسوق نظرًا لقلة إعلانات الإيجار المباشرة في ${property.district} حاليًا."
        } else {
            "تم حساب الإيجار التقديري بنحو ${formatCurrency(estimatedMonthlyRent)} ج/شهريًا استنادًا لـ ${rentalListings.size} إعلانات إيجار حقيقية مسجلة في ${property.district}."
        }

        return AiInvestmentData(
            property = property,
            purchasePrice = property.price,
            pricePerMeter = pricePerMeter,
            estimatedMonthlyRent = estimatedMonthlyRent,
            annualRentalYieldPercentage = annualYield,
            estimatedPaybackYears = paybackYears,
            marketComparisonSummary = summary,
            isLimitedData = isLimitedData
        )
    }

    /**
     * مطابقة الوسيط: مطابقة طلبات العملاء الحالية مع العقارات
     */
    fun matchClientRequestsWithProperties(
        requests: List<CustomerRequestEntity>,
        properties: List<PropertyEntity>
    ): List<Pair<CustomerRequestEntity, List<PropertyEntity>>> {
        val results = mutableListOf<Pair<CustomerRequestEntity, List<PropertyEntity>>>()

        for (req in requests) {
            val matched = properties.filter { prop ->
                var matches = true
                if (!req.propertyCategory.isNullOrBlank()) {
                    matches = matches && (prop.category.contains(req.propertyCategory) || req.propertyCategory.contains(prop.category))
                }
                if (!req.targetDistrict.isNullOrBlank() && req.targetDistrict != "دسوق") {
                    matches = matches && prop.district.contains(req.targetDistrict)
                }
                matches
            }
            if (matched.isNotEmpty()) {
                results.add(Pair(req, matched.take(3)))
            }
        }
        return results
    }

    /**
     * تدقيق الإدارة: اكتشاف البيانات الناقصة في العقارات
     */
    fun auditMissingData(properties: List<PropertyEntity>): List<Pair<PropertyEntity, List<String>>> {
        val audited = mutableListOf<Pair<PropertyEntity, List<String>>>()

        for (p in properties) {
            val missing = mutableListOf<String>()
            if (p.price <= 0) missing.add("السعر غير محدد")
            if (p.area <= 0) missing.add("المساحة مفقودة")
            if (p.phone.isBlank() && p.whatsapp.isBlank()) missing.add("لا توجد بيانات اتصال")
            if (p.description.length < 15) missing.add("الوصف قصير جدًا أو ناقص")
            if (p.imageResName.isBlank()) missing.add("لا توجد صور")
            if (p.street.isBlank() && p.location.isBlank()) missing.add("الموقع التفصيلي غير محدد")

            if (missing.isNotEmpty()) {
                audited.add(Pair(p, missing))
            }
        }
        return audited
    }

    /**
     * تدقيق الإدارة: اكتشاف العقارات المكررة المحتملة
     */
    fun auditDuplicates(properties: List<PropertyEntity>): List<List<PropertyEntity>> {
        val duplicateGroups = mutableListOf<List<PropertyEntity>>()
        val visitedIds = mutableSetOf<Long>()

        for (i in properties.indices) {
            val p1 = properties[i]
            if (visitedIds.contains(p1.id)) continue

            val group = mutableListOf(p1)
            for (j in i + 1 until properties.size) {
                val p2 = properties[j]
                if (visitedIds.contains(p2.id)) continue

                val samePrice = abs(p1.price - p2.price) < 1000
                val sameArea = abs(p1.area - p2.area) < 5
                val sameDistrict = p1.district == p2.district
                val sameCategory = p1.category == p2.category

                if (samePrice && sameArea && sameDistrict && sameCategory) {
                    group.add(p2)
                    visitedIds.add(p2.id)
                }
            }

            if (group.size > 1) {
                visitedIds.add(p1.id)
                duplicateGroups.add(group)
            }
        }
        return duplicateGroups
    }

    /**
     * قائمة فحص المعاينة القياسية
     */
    fun getInspectionChecklist(): List<ChecklistItem> {
        return listOf(
            ChecklistItem("elec", "شبكة الكهرباء واللوحة الرئيسية", "التأكد من سلامة القواطع الأوتوماتيكية، جهد التيار، وتوزيع المفاتيح", "كهرباء"),
            ChecklistItem("plumb", "السباكة والصرف الصحي", "معاينة ضغط المياه، عدم وجود تسريبات في الحمام والمطبخ ومحبس المياه الرئيسي", "سباكة"),
            ChecklistItem("paint", "الحوائط والدهانات والرطوبة", "فحص أسفل الجدران وزوايا الأسقف للتأكد من عدم وجود رشح مياه أو تنشيع", "تشطيب"),
            ChecklistItem("doors", "الأبواب والشبابيك والألوميتال", "تجربة فتح وغلق النوافذ وإحكام عزل الصوت والأتربة", "تشطيب"),
            ChecklistItem("floor", "الأرضيات والسيراميك والبورسلين", "التأكد من عدم وجود فراغات أو شروخ وميول تصريف البلاط", "تشطيب"),
            ChecklistItem("elev", "حالة المصعد (الأسانسير)", "معاينة عمل المصعد وسرعته وصيانته الدورية وتوافر شهادة أمان", "مرافق"),
            ChecklistItem("meters", "العدادات الرسمية (كهرباء، مياه، غاز)", "مراجعة وجود عدادات كودية أو رسمية وخلوها من المتأخرات والفواتير", "مرافق")
        )
    }

    /**
     * أسئلة ما قبل الشراء والفحص القانوني
     */
    fun getPrePurchaseChecklist(): List<ChecklistItem> {
        return listOf(
            ChecklistItem("deed", "تسلسل الملكية والعقد المسجل", "مراجعة عقد البيع الابتدائي وسند ملكية البائع وصحة التوقيع أو التسجيل بالشهر العقاري", "قانوني"),
            ChecklistItem("permit", "رخصة المباني والمطابقة", "التأكد من أن الدور المراد شراؤه داخل في رخصة البناء وليس دورًا مخالفًا", "قانوني"),
            ChecklistItem("reconcile", "نموذج 10 للتصالح (إن وجد)", "إذا كان العقار خضع لقانون التصالح، التأكد من سداد كافة الأقساط واستلام نموذج 10 النهائي", "قانوني"),
            ChecklistItem("bills", "براءة ذمة الفواتير والمرافق", "مطالعة إيصالات سداد الكهرباء والمياه والغاز وصيانة العقار حتى تاريخ الاستلام", "مرافق"),
            ChecklistItem("engineer", "معاينة هندسية استشارية", "الاستعانة بمهندس مدني لفحص الهيكل الخرساني والأساسات في حال البيوت المستقلة أو العمارات القديمة", "فني")
        )
    }

    private fun normalize(text: String): String {
        return text.trim().lowercase()
            .replace(Regex("[إأآا]"), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
    }

    private fun formatCurrency(amount: Double): String {
        return numberFormat.format(amount.toLong())
    }
}
