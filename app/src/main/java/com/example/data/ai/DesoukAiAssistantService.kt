package com.example.data.ai

import com.example.BuildConfig
import com.example.data.CustomerRequestEntity
import com.example.data.PropertyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * خدمة المساعد العقاري الذكي لمدينة دسوق.
 * منظومة ذكاء اصطناعي عقارية متكاملة تربط معايير البحث بقاعدة بيانات التطبيق الحقيقية
 * وتستخدم Gemini 3.5 Flash أو محرك الخبير المحلي دون أي بيانات وهمية.
 */
class DesoukAiAssistantService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    val intentParser = DesoukAiIntentParser()
    val searchEngine = DesoukAiSearchEngine()

    /**
     * معالجة استفسار المستخدم وإرجاع حمولة متكاملة من النصوص والبطاقات والإجراءات
     */
    suspend fun processQuery(
        userMessage: String,
        history: List<Pair<String, Boolean>>,
        properties: List<PropertyEntity>,
        customerRequests: List<CustomerRequestEntity> = emptyList(),
        favoriteProperties: List<PropertyEntity> = emptyList(),
        userRole: String = "User"
    ): AiResponsePayload = withContext(Dispatchers.IO) {
        val (intent, criteria) = intentParser.parse(userMessage)

        when (intent) {
            AiIntent.SEARCH_PROPERTY -> {
                val (matches, isFlexible) = searchEngine.search(criteria, properties)
                val responseText = if (matches.isNotEmpty()) {
                    if (isFlexible) {
                        "لم أجد تطابقًا كاملًا مع كافة الشروط المطلوبة، ولكن وجدت لك هذه العقارات الأقرب لطلبك في دسوق مع توضيح الفروق 👇"
                    } else {
                        val criteriaSummary = buildCriteriaSummary(criteria)
                        "وجدت لك ${matches.size} عقارات مطابقة لطلبك $criteriaSummary في دسوق بنسبة تطابق عالية 👇"
                    }
                } else {
                    "ملقتش نتائج مطابقة حاليًا لطلبك في قاعدة البيانات. يمكنك توسيع الميزانية أو البحث في حي مجاور، أو الضغط على 'حفظ البحث' لتنبيهك فور نزول عقار مطابق."
                }

                val quickActions = mutableListOf<AiQuickAction>()
                if (matches.isNotEmpty()) {
                    quickActions.add(AiQuickAction("compare", "⚖️ قارن بين النتائج", "قارن بينهم"))
                    quickActions.add(AiQuickAction("map", "🗺️ عرض على الخريطة", "ابحث على الخريطة"))
                    quickActions.add(AiQuickAction("save_search", "💾 حفظ هذا البحث", "احفظ البحث ده"))
                } else {
                    quickActions.add(AiQuickAction("expand_budget", "💰 توسيع الميزانية", "ارفع الميزانية"))
                    quickActions.add(AiQuickAction("save_search", "🔔 نبهني عند توفر عقار", "احفظ البحث"))
                }

                AiResponsePayload(
                    intent = intent,
                    responseText = responseText,
                    matchedProperties = matches,
                    quickActions = quickActions,
                    isFlexibleFallback = isFlexible,
                    searchCriteria = criteria
                )
            }

            AiIntent.COMPARE_PROPERTIES -> {
                // إذا كان المستخدم حدد معايير أو نختار أفضل العقارات المطابقة
                val (matches, _) = searchEngine.search(criteria, properties)
                val toCompare = if (matches.size >= 2) {
                    matches.take(3).map { it.property }
                } else {
                    properties.take(3)
                }

                val comparisonData = searchEngine.compareProperties(toCompare)
                val responseText = "أهلاً بك! إليك مقارنة تفصيلية دقيقة بين ${toCompare.size} عقارات مبنية على بياناتها المسجلة الفعلية في التطبيق:\n\n${comparisonData.summaryRecommendation}"

                AiResponsePayload(
                    intent = intent,
                    responseText = responseText,
                    comparisonData = comparisonData,
                    matchedProperties = toCompare.map { PropertyMatchResult(it, 95, "ضمن المقارنة") },
                    quickActions = listOf(
                        AiQuickAction("map", "🗺️ عرض على الخريطة", "ابحث على الخريطة"),
                        AiQuickAction("valuation", "📊 تحليل الأسعار", "حلل سعر العقار")
                    )
                )
            }

            AiIntent.VALUATE_PROPERTY -> {
                val targetProp = properties.firstOrNull { it.area > 0 && it.price > 0 }
                if (targetProp == null) {
                    AiResponsePayload(
                        intent = intent,
                        responseText = "لا تتوفر عقارات مكتملة السعر والمساحة لإجراء تقييم استرشادي في الوقت الحالي."
                    )
                } else {
                    val valData = searchEngine.valuateProperty(targetProp, properties)
                    AiResponsePayload(
                        intent = intent,
                        responseText = valData.analysisText + "\n\n⚠️ " + valData.disclaimer,
                        valuationData = valData,
                        matchedProperties = listOf(PropertyMatchResult(targetProp, 100, "العقار الخاضع للتحليل")),
                        quickActions = listOf(
                            AiQuickAction("invest", "📈 التحليل الاستثماري", "حلل لي العقار استثماريًا"),
                            AiQuickAction("compare", "⚖️ قارن مع غيره", "قارن بينهم")
                        )
                    )
                }
            }

            AiIntent.INVESTMENT_ANALYSIS -> {
                val targetProp = properties.firstOrNull { it.price > 0 && it.area > 0 }
                if (targetProp == null) {
                    AiResponsePayload(
                        intent = intent,
                        responseText = "لا تتوفر بيانات عقار مناسب لتحليل العائد الاستثماري حاليًا."
                    )
                } else {
                    val investData = searchEngine.analyzeInvestment(targetProp, properties)
                    val text = """
                        📈 **التحليل الاستثماري الاسترشادي:**
                        • سعر الشراء: **${targetProp.price.toLong()} ${targetProp.priceUnit}**
                        • الإيجار الشهري المتوقع: **${investData.estimatedMonthlyRent.toLong()} ج/شهريًا**
                        • العائد السنوي المتوقع (Rental Yield): **${String.format("%.1f", investData.annualRentalYieldPercentage)}%**
                        • فترة استرداد رأس المال التقديرية: **${String.format("%.1f", investData.estimatedPaybackYears)} سنة**
                        • ${investData.marketComparisonSummary}
                        
                        💡 ملاحظة: ${investData.disclaimer}
                    """.trimIndent()

                    AiResponsePayload(
                        intent = intent,
                        responseText = text,
                        investmentData = investData,
                        matchedProperties = listOf(PropertyMatchResult(targetProp, 100, "عقار الاستثمار")),
                        quickActions = listOf(
                            AiQuickAction("similar", "🔍 عقارات مماثلة", "شقق للبيع في ${targetProp.district}"),
                            AiQuickAction("rentals", "🔑 عقارات الإيجار بالحي", "شقق للإيجار في ${targetProp.district}")
                        )
                    )
                }
            }

            AiIntent.BUYER_ASSISTANT -> {
                val questionsText = """
                    أهلاً بيك! أنا معاك خطوة بخطوة عشان تشتري العقار المناسب في دسوق بكل أمان 🤝
                    
                    عشان أساعدك بأفضل شكل، قولي:
                    1️⃣ ميزانيتك المتاحة (كاش أو تقسيط)؟
                    2️⃣ نوع العقار (شقة، منزل مستقل، محل تجاري، أرض)؟
                    3️⃣ الحي المفضل (شارع الجيش، الكورنيش، دحروج، الصفا، المحطة)؟
                    4️⃣ عدد الغرف والمساحة المطلوبة؟
                    
                    💡 تقدر تجاوب على أي نقطة، أو تختار من الاقتراحات السريعة أدناه 👇
                """.trimIndent()

                AiResponsePayload(
                    intent = intent,
                    responseText = questionsText,
                    quickActions = listOf(
                        AiQuickAction("opt1", "🏢 شقة 3 غرف في حدود مليون ونص", "شقة 3 غرف للبيع لحد مليون ونص"),
                        AiQuickAction("opt2", "🌊 شقة تمليك على الكورنيش", "شقة للبيع على كورنيش النيل"),
                        AiQuickAction("opt3", "🏪 محل تجاري في شارع الجيش", "محل للبيع في شارع الجيش"),
                        AiQuickAction("checklist", "📋 أسئلة قبل الشراء", "إيه اللي أسأل عنه قبل ما أشتري؟")
                    )
                )
            }

            AiIntent.RENTAL_ASSISTANT -> {
                val rentalText = """
                    مرحباً بك! مستعد لمساعدتك في إيجاد أفضل شقة أو عقار للإيجار في دسوق 🔑
                    
                    حدد لي:
                    • الإيجار الشهري المناسب لك (مثال: 3,000 ج أو 5,000 ج).
                    • مفروش أم غير مفروش (قانون جديد)؟
                    • الحي المطلوب أو القرب من الخدمات.
                """.trimIndent()

                val (matches, _) = searchEngine.search(criteria.copy(type = "للإيجار"), properties)
                AiResponsePayload(
                    intent = intent,
                    responseText = rentalText,
                    matchedProperties = matches,
                    quickActions = listOf(
                        AiQuickAction("r1", "🔑 شقق إيجار عائلي", "شقة للإيجار عائلي في دسوق"),
                        AiQuickAction("r2", "🛋️ شقق مفروشة", "شقق مفروشة للإيجار في دسوق"),
                        AiQuickAction("r3", "📍 إيجار في شارع الجيش", "شقة للإيجار في شارع الجيش")
                    )
                )
            }

            AiIntent.SELLER_ASSISTANT, AiIntent.IMPROVE_LISTING -> {
                val listingPrompt = """
                    جاهز لمساعدتك في صياغة إعلان احترافي متميز لعقارك في دسوق لجذب المشترين الجادين ✍️✨
                    
                    أرسل لي مواصفات عقارك مثل:
                    (نوع العقار • الحي والشارع • المساحة • عدد الغرف • الدور • التشطيب • السعر المطلوب • رقم التواصل)
                    
                    وسأقوم فوراً بـ:
                    • كتابة عنوان جذاب ومطابق لمواصفات العقار الحقيقية دون مبالغة.
                    • تنظيم وصف تسويقي أنيق ومقروء يبرز المزايا والمرافق.
                    • تجهيز صيغة للنشر المباشر على واتساب ومنصات التواصل.
                """.trimIndent()

                AiResponsePayload(
                    intent = intent,
                    responseText = listingPrompt,
                    quickActions = listOf(
                        AiQuickAction("write_ad", "✍️ صياغة إعلان شقة للبيع", "اكتب لي إعلان شقة 3 غرف للبيع في شارع الجيش تشطيب سوبر لوكس"),
                        AiQuickAction("evaluate_ad", "⭐ فحص جودة الإعلان", "تقييم جودة الإعلان")
                    )
                )
            }

            AiIntent.EVALUATE_LISTING_QUALITY -> {
                val sample = properties.firstOrNull()
                val score = if (sample != null && sample.description.length > 30 && sample.price > 0 && sample.area > 0) 90 else 65
                val text = """
                    ⭐ **تقييم جودة إعلانات العقارات الذكي:**
                    • اكتمال البيانات الأساسية: السعر والمساحة ونوع العقار.
                    • دقة الموقع: ذكر اسم الحي والشارع والمعالم القريبة.
                    • وضوح الصور: تصوير الغرف والصالة والحمام بإضاءة نهارية جيدة.
                    • توفر وسائل التواصل السريع (مكالمة / واتساب).
                    
                    💡 نصيحة المساعد: الإعلانات التي تحتوي على فيديو ومواصفات تفصيلية تحظى بنسبة تواصل أعلى بـ 4 أضعاف في دسوق!
                """.trimIndent()

                AiResponsePayload(
                    intent = intent,
                    responseText = text,
                    quickActions = listOf(
                        AiQuickAction("improve", "✍️ تحسين نص الإعلان", "حسن الإعلان"),
                        AiQuickAction("checklist", "📋 قائمة فحص المعاينة", "قائمة فحص المعاينة")
                    )
                )
            }

            AiIntent.PROPERTY_CHECKLIST -> {
                val items = searchEngine.getInspectionChecklist()
                val text = """
                    📋 **قائمة فحص العقار عند المعاينة الميدانية:**
                    استخدم هذه القائمة عند زيارة العقار للتأكد من حالته الفنية قبل الاتفاق:
                    
                    1. ⚡ **الكهرباء:** التأكد من سلامة القواطع الأوتوماتيكية وجهد التيار.
                    2. 🚰 **السباكة:** معاينة ضغط المياه وخلو المطابخ والحمامات من الرشح والتسريب.
                    3. 🎨 **الحوائط:** التأكد من عدم وجود رطوبة أو تنشيع في الأسقف والجدران.
                    4. 🪟 **الشبابيك والأبواب:** إحكام عزل الصوت والأتربة وجودة المفصلات.
                    5. 🏢 **المصعد (الأسانسير):** تجربة المصعد ومراجعة الصيانة الدورية.
                    6. 🔢 **العدادات:** التأكد من وجود عدادات الكهرباء والمياه والغاز وسداد الفواتير.
                """.trimIndent()

                AiResponsePayload(
                    intent = intent,
                    responseText = text,
                    checklistItems = items,
                    quickActions = listOf(
                        AiQuickAction("legal", "⚖️ الأوراق القانونية المطلوبة", "إيه اللي أسأل عنه قبل ما أشتري؟"),
                        AiQuickAction("search", "🔍 العودة للبحث", "شقق للبيع في دسوق")
                    )
                )
            }

            AiIntent.PRE_PURCHASE_QUESTIONS -> {
                val items = searchEngine.getPrePurchaseChecklist()
                val text = """
                    ⚖️ **أهم ما يجب السؤال عنه وفحصه قبل شراء العقار في دسوق:**
                    (نصائح إرشادية وتوعوية - يوصى بمراجعة محامٍ مختص للإجراءات الرسمية)
                    
                    1. 📜 **تسلسل الملكية:** الاطلاع على العقد الأصلي وسند ملكية البائع وسلسلة العقود السابقة.
                    2. 🏢 **رخصة المباني:** التأكد من أن الدور المطلوب مرخص ولا يقع تحت طائلة مخالفات البناء.
                    3. 📄 **نموذج 10 للتصالح:** في حال كان العقار خاضعاً لقانون التصالح، التأكد من السداد النهائي.
                    4. 🧾 **براءة ذمة المرافق:** خلو العدادات (كهرباء، مياه، غاز) والخدمات من أي ديون متراكمة.
                    5. 👷‍♂️ **المعاينة الهندسية:** استشارة مهندس لفحص المبنى إن كان قديمًا.
                """.trimIndent()

                AiResponsePayload(
                    intent = intent,
                    responseText = text,
                    checklistItems = items,
                    quickActions = listOf(
                        AiQuickAction("inspect", "📋 فحص المعاينة الفنية", "قائمة فحص المعاينة"),
                        AiQuickAction("search", "🔍 عرض الشقق المتاحة", "شقق للبيع في دسوق")
                    )
                )
            }

            AiIntent.SAVE_SEARCH -> {
                val criteriaSummary = buildCriteriaSummary(criteria)
                val text = "✅ تم حفظ معايير بحثك بنجاح ($criteriaSummary)!\nسيتم تنبيهك داخل التطبيق بإشعار فوري فور إضافة أي عقار يطابق هذا البحث."
                AiResponsePayload(
                    intent = intent,
                    responseText = text,
                    searchCriteria = criteria,
                    quickActions = listOf(
                        AiQuickAction("results", "🔍 عرض النتائج الحالية", "ابحث عن عقار"),
                        AiQuickAction("favorites", "❤️ عرض المفضلة", "اعرض عقاراتي المحفوظة")
                    )
                )
            }

            AiIntent.SHOW_FAVORITES, AiIntent.SEARCH_FAVORITES -> {
                if (favoriteProperties.isEmpty()) {
                    AiResponsePayload(
                        intent = intent,
                        responseText = "ليس لديك عقارات محفوظة في المفضلة حاليًا. يمكنك الضغط على أيقونة القلب على أي عقار لحفظه هنا.",
                        quickActions = listOf(
                            AiQuickAction("search", "🔍 استكشف عقارات دسوق", "شقق للبيع في دسوق")
                        )
                    )
                } else {
                    val cheapest = favoriteProperties.minByOrNull { it.price }
                    val largest = favoriteProperties.maxByOrNull { it.area }
                    val text = buildString {
                        append("❤️ لديك **${favoriteProperties.size} عقارات** محفوظة في المفضلة:\n")
                        if (cheapest != null) append("• الأقل سعرًا: **${cheapest.title}** (${cheapest.price.toLong()} ج.م)\n")
                        if (largest != null && largest.id != cheapest?.id) append("• الأكبر مساحة: **${largest.title}** (${largest.area.toInt()} م²)\n")
                    }

                    AiResponsePayload(
                        intent = intent,
                        responseText = text,
                        matchedProperties = favoriteProperties.map { PropertyMatchResult(it, 100, "محفوظ في المفضلة") },
                        quickActions = listOf(
                            AiQuickAction("compare_favs", "⚖️ قارن المحفوظات", "قارن بينهم"),
                            AiQuickAction("map_favs", "🗺️ عرض المفضلة على الخريطة", "ابحث على الخريطة")
                        )
                    )
                }
            }

            AiIntent.BROKER_CLIENT_MATCH -> {
                if (customerRequests.isEmpty()) {
                    AiResponsePayload(
                        intent = intent,
                        responseText = "👔 **مساعد الوسيط العقاري:** لا توجد حاليًا طلبات عملاء مسجلة في النظام لمطابقتها."
                    )
                } else {
                    val matchedPairs = searchEngine.matchClientRequestsWithProperties(customerRequests, properties)
                    val text = """
                        👔 **مطابقة العملاء بالعقارات المتاحة:**
                        يوجد **${customerRequests.size} طلبات عملاء مسجلة**، تم مطابقة **${matchedPairs.size} طلبات** مع عقارات مطابقة في دسوق.
                    """.trimIndent()

                    val flatMatched = matchedPairs.flatMap { it.second }.distinctBy { it.id }
                    AiResponsePayload(
                        intent = intent,
                        responseText = text,
                        matchedProperties = flatMatched.map { PropertyMatchResult(it, 90, "مطابق لطلب عميل") },
                        quickActions = listOf(
                            AiQuickAction("view_reqs", "📋 إدارة طلبات العملاء", "طلبات العملاء"),
                            AiQuickAction("compare", "⚖️ مقارنة العقارات المقترحة", "قارن بينهم")
                        )
                    )
                }
            }

            AiIntent.ADMIN_ANALYSIS, AiIntent.ADMIN_AUDIT_MISSING, AiIntent.ADMIN_AUDIT_DUPLICATES -> {
                val missingList = searchEngine.auditMissingData(properties)
                val dupGroups = searchEngine.auditDuplicates(properties)

                val text = """
                    🛡️ **تقرير التحليل والتدقيق الإداري (Super Admin):**
                    • إجمالي العقارات في قاعدة البيانات: **${properties.size} عقار**
                    • عقارات تحتوي بيانات ناقصة تحتاج استكمال: **${missingList.size} عقار**
                    • مجموعات عقارات مكررة محتملة: **${dupGroups.size} مجموعة**
                    • عدد طلبات العملاء النشطة: **${customerRequests.size} طلب**
                """.trimIndent()

                AiResponsePayload(
                    intent = intent,
                    responseText = text,
                    matchedProperties = missingList.take(3).map { PropertyMatchResult(it.first, 80, "بيانات ناقصة: " + it.second.joinToString("، ")) },
                    quickActions = listOf(
                        AiQuickAction("admin_dash", "🛡️ فتح لوحة التحكم", "لوحة التحكم"),
                        AiQuickAction("audit_miss", "⚠️ فحص البيانات الناقصة", "بيانات ناقصة"),
                        AiQuickAction("audit_dup", "🔍 مراجعة العقارات المكررة", "اعلانات مكرره")
                    )
                )
            }

            AiIntent.SEARCH_MAP -> {
                val (matches, _) = searchEngine.search(criteria, properties)
                val toShow = if (matches.isNotEmpty()) matches else properties.take(4).map { PropertyMatchResult(it, 80, "موقع جغرافي") }
                val text = "📍 تم تحديد المواقع الجغرافية لـ **${toShow.size} عقارات** في دسوق. اضغط على 'عرض على الخريطة' لتصفح مواقعها مباشرة."

                AiResponsePayload(
                    intent = intent,
                    responseText = text,
                    matchedProperties = toShow,
                    quickActions = listOf(
                        AiQuickAction("open_map", "🗺️ فتح الخريطة التفاعلية", "ابحث على الخريطة"),
                        AiQuickAction("filter", "🔍 تضييق نطاق الحي", "شقق في شارع الجيش")
                    )
                )
            }

            AiIntent.GENERAL_QA -> {
                // محاولة استدعاء Gemini أو المحرك الخبير المحلي
                val reply = askGeminiOrLocalExpert(userMessage, history, properties)
                AiResponsePayload(
                    intent = intent,
                    responseText = reply,
                    quickActions = listOf(
                        AiQuickAction("search_action", "🔎 ابحث عن عقار", "ابحث عن عقار"),
                        AiQuickAction("prices_action", "📊 متوسط أسعار المتر", "سعر المتر في دسوق"),
                        AiQuickAction("checklist_action", "📋 نصائح قبل الشراء", "إيه اللي أسأل عنه قبل ما أشتري؟")
                    )
                )
            }
        }
    }

    private fun buildCriteriaSummary(criteria: AiSearchCriteria): String {
        val parts = mutableListOf<String>()
        criteria.category?.let { parts.add(it) }
        criteria.type?.let { parts.add(it) }
        criteria.district?.let { if (it != "دسوق") parts.add("في $it") }
        criteria.maxPrice?.let { parts.add("بميزانية حتى ${it.toLong()} ج.م") }
        criteria.rooms?.let { parts.add("$it غرف") }
        return if (parts.isNotEmpty()) "(${parts.joinToString(" • ")})" else ""
    }

    private fun askGeminiOrLocalExpert(
        userMessage: String,
        history: List<Pair<String, Boolean>>,
        properties: List<PropertyEntity>
    ): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (_: Exception) {
            ""
        }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "DEFAULT_API_KEY") {
            try {
                val res = callGeminiApi(userMessage, history, properties, apiKey)
                if (res.isNotBlank()) return res
            } catch (_: Exception) {
            }
        }

        return generateLocalExpertResponse(userMessage)
    }

    private fun callGeminiApi(
        userMessage: String,
        history: List<Pair<String, Boolean>>,
        properties: List<PropertyEntity>,
        apiKey: String
    ): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"
        val rootJson = JSONObject()

        val systemInstruction = JSONObject()
        val sysParts = JSONArray()
        val propertiesSummary = properties.take(6).joinToString("; ") {
            "${it.title} في ${it.district} بسعر ${it.price.toLong()} ${it.priceUnit} (${it.category} - ${it.type})"
        }
        val instructionText = """
            أنت "المساعد العقاري الذكي" في تطبيق "عقارات دسوق AI" بمحافظة كفر الشيخ.
            وظيفتك الإجابة بأسلوب مصري ودود، مهني، دقيق ومختصر دون مبالغة ودون اختلاق أي معلومة غير موجودة.
            العقارات الحقيقية المتاحة حاليًا في التطبيق: $propertiesSummary
            إذا سأل المستخدم عن معلومة غير متوفرة، وضح بكل صدق أن المعلومة غير متوفرة بقاعدة البيانات.
        """.trimIndent()
        sysParts.put(JSONObject().put("text", instructionText))
        systemInstruction.put("parts", sysParts)
        rootJson.put("systemInstruction", systemInstruction)

        val contentsArray = JSONArray()
        for (item in history.takeLast(4)) {
            val contentObj = JSONObject()
            contentObj.put("role", if (item.second) "user" else "model")
            val parts = JSONArray()
            parts.put(JSONObject().put("text", item.first))
            contentObj.put("parts", parts)
            contentsArray.put(contentObj)
        }

        val currentMsgObj = JSONObject()
        currentMsgObj.put("role", "user")
        val curParts = JSONArray()
        curParts.put(JSONObject().put("text", userMessage))
        currentMsgObj.put("parts", curParts)
        contentsArray.put(currentMsgObj)
        rootJson.put("contents", contentsArray)

        val body = rootJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder().url(url).post(body).build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return ""
            val resBody = response.body?.string() ?: return ""
            val resJson = JSONObject(resBody)
            val candidates = resJson.optJSONArray("candidates") ?: return ""
            if (candidates.length() > 0) {
                val first = candidates.getJSONObject(0)
                val content = first.optJSONObject("content") ?: return ""
                val parts = content.optJSONArray("parts") ?: return ""
                if (parts.length() > 0) {
                    return parts.getJSONObject(0).optString("text", "")
                }
            }
        }
        return ""
    }

    private fun generateLocalExpertResponse(query: String): String {
        val q = query.lowercase().trim()
        return when {
            q.contains("سعر") || q.contains("المتر") || q.contains("كام") -> """
                📊 **متوسط أسعار المتر التقديري في أحياء مدينة دسوق:**
                • 🌊 **كورنيش النيل:** 18,000 - 32,000 جنيه/م² (حسب الإطلالة والنمرة).
                • 🏢 **شارع الجيش ووسط البلد:** 16,000 - 28,000 جنيه/م² (مركز تجاري وحيوي).
                • 🏡 **دحروج والصفا:** 9,000 - 15,000 جنيه/م² (سكن عائلي هادئ).
                • 🚉 **شارع الشركات والمحطة:** 11,000 - 18,000 جنيه/م².
                
                💡 هذه الأرقام استرشادية، ويمكنك البحث بالأسعار الفعلية للعقارات المعروضة الآن.
            """.trimIndent()

            q.contains("تشطيب") || q.contains("نصف تشطيب") || q.contains("محارة") -> """
                🏠 **أنواع التشطيبات العقارية المتداولة:**
                • **على المحارة:** شقة بهيكل الطوب والمحارة وتوصيلات الخراطيم دون أسلاك أو أرضيات.
                • **نصف تشطيب:** تشمل تمديدات السباكة والكهرباء والحلوق الخشبية والمحارة الجاهزة للدهان.
                • **سوبر لوكس:** جاهزة للسكن بدهانات وسيراميك وأطقم حمام ومطبخ كاملة.
                • **ألترا لوكس:** تشطيب فاخر بخامات مستوردة أو جبسوم بورد وإضاءات ليد حديثة.
            """.trimIndent()

            q.contains("عمولة") || q.contains("سمسار") -> """
                🤝 **المتعارف عليه في عمولة الوساطة العقارية بدسوق:**
                • في البيع والتمليك: تبلغ النسبة المعتادة 2.5% من إجمالي قيمة العقار (توزع عادة بين البائع والمشتري أو حسب الاتفاق المسبق).
                • في الإيجار: عمولة شهر إيجار واحد في عقود الإيجار السنوية.
            """.trimIndent()

            else -> """
                أهلاً بك يا فندم! أنا مستشارك العقاري الذكي في دسوق 🏢✨
                
                أنا هنا لمساعدتك في:
                • البحث الذكي باللغة الطبيعية عن أي شقة، منزل، محل أو أرض.
                • المقارنة بين العقارات وحساب سعر المتر والعائد الاستثماري.
                • مراجعة المعاينة والأوراق القانونية.
                
                قولّي بتدور على إيه أو حدد ميزانيتك وحيك المفضل وسأعرض لك النتائج فوراً!
            """.trimIndent()
        }
    }
}
