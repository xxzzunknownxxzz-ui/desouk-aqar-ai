package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.data.ImportedPropertyEntity
import com.example.data.PropertyEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/**
 * البيانات المستخرجة من إعلان فيسبوك (سواء عبر الرابط أو الصورة أو النص)
 */
data class FacebookExtractedAd(
    val title: String = "",
    val propertyType: String = "شقة", // شقة، فيلا، منزل، أرض، محل، مكتب، عمارة، مخزن، شاليه، عقار تجاري، أخرى
    val listingType: String = "للبيع", // للبيع، للإيجار
    val location: String = "دسوق", // دسوق، أحياء ومناطق دسوق، القرى والمناطق المحيطة
    val address: String = "",
    val price: Double? = null,
    val priceUnit: String = "ج.م",
    val area: Double? = null,
    val rooms: Int? = null,
    val bathrooms: Int? = null,
    val floor: Int? = null,
    val finishing: String = "", // حالة التشطيب
    val streetsCount: Int = 1, // عدد الشوارع
    val facade: String = "", // واجهة العقار
    val description: String = "",
    val phone: String = "",
    val ownerName: String = "",
    val sourceUrl: String = "",
    val images: String = "img_property_apartment",
    val completenessScore: Int = 80,
    val isDuplicate: Boolean = false,
    val duplicateReason: String = "",
    val matchedProperty: PropertyEntity? = null,
    val matchedImported: ImportedPropertyEntity? = null
)

/**
 * نتيجة قراءة رابط فيسبوك
 */
sealed class FacebookUrlReadResult {
    data class Success(val rawText: String, val postTitle: String = "", val imageUrl: String = "") : FacebookUrlReadResult()
    data class CannotAccess(val friendlyMessage: String) : FacebookUrlReadResult()
    data class InvalidUrl(val errorMessage: String) : FacebookUrlReadResult()
}

/**
 * خدمة استيراد عقار من فيسبوك بالذكاء الاصطناعي
 */
class FacebookImportAiService {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(false) // لمنع التوجيه الصامت لصفحة login
        .build()

    private val geminiClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * 1. التحقق من صحة رابط فيسبوك
     */
    fun validateFacebookUrl(url: String): Boolean {
        val trimmed = url.trim()
        if (trimmed.isBlank()) return false
        val regex = Regex("^(https?://)?([a-zA-Z0-9_-]+\\.)?(facebook\\.com|fb\\.watch|fb\\.me|m\\.facebook\\.com)/.+$", RegexOption.IGNORE_CASE)
        return regex.containsMatchIn(trimmed)
    }

    /**
     * 2. محاولة قراءة المعلومات المتاحة من الرابط بطريقة قانونية وآمنة
     * دون كشط ودون انتهاك ودون طلب تسجيل دخول
     */
    suspend fun tryReadPublicFacebookUrl(url: String): FacebookUrlReadResult = withContext(Dispatchers.IO) {
        val trimmed = url.trim()
        if (!validateFacebookUrl(trimmed)) {
            return@withContext FacebookUrlReadResult.InvalidUrl("يرجى إدخال رابط منشور Facebook صالح.")
        }

        try {
            val fullUrl = if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
                "https://$trimmed"
            } else trimmed

            val request = Request.Builder()
                .url(fullUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .header("Accept-Language", "ar,en-US;q=0.7,en;q=0.3")
                .build()

            val response = httpClient.newCall(request).execute()
            val code = response.code

            // إذا أعاد فيسبوك توجيهاً لصفحة تسجيل الدخول أو كود 301/302 أو خطأ بالصلاحيات
            if (code in 300..399 || code == 401 || code == 403) {
                return@withContext FacebookUrlReadResult.CannotAccess(
                    "تعذر قراءة محتوى هذا المنشور. يمكنك رفع صورة الإعلان أو إدخال بيانات العقار يدويًا."
                )
            }

            val body = response.body?.string() ?: ""
            if (body.contains("login_form") || body.contains("Log in to Facebook") || body.contains("تسجيل الدخول إلى فيسبوك")) {
                return@withContext FacebookUrlReadResult.CannotAccess(
                    "تعذر قراءة محتوى هذا المنشور. يمكنك رفع صورة الإعلان أو إدخال بيانات العقار يدويًا."
                )
            }

            // استخراج Open Graph tags المتاحة للعامة
            val ogTitle = extractMetaContent(body, "og:title")
            val ogDesc = extractMetaContent(body, "og:description")
            val ogImage = extractMetaContent(body, "og:image")

            val combinedText = buildString {
                if (ogTitle.isNotBlank() && !ogTitle.contains("Facebook") && !ogTitle.contains("فيسبوك")) {
                    append(ogTitle).append("\n")
                }
                if (ogDesc.isNotBlank() && !ogDesc.contains("Log in") && !ogDesc.contains("تسجيل الدخول")) {
                    append(ogDesc)
                }
            }.trim()

            if (combinedText.length >= 15) {
                FacebookUrlReadResult.Success(rawText = combinedText, postTitle = ogTitle, imageUrl = ogImage)
            } else {
                FacebookUrlReadResult.CannotAccess(
                    "تعذر قراءة محتوى هذا المنشور. يمكنك رفع صورة الإعلان أو إدخال بيانات العقار يدويًا."
                )
            }
        } catch (e: Exception) {
            FacebookUrlReadResult.CannotAccess(
                "تعذر قراءة محتوى هذا المنشور. يمكنك رفع صورة الإعلان أو إدخال بيانات العقار يدويًا."
            )
        }
    }

    private fun extractMetaContent(html: String, property: String): String {
        val pattern = Pattern.compile("<meta\\s+(?:property|name)=[\"']$property[\"']\\s+content=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(html)
        if (matcher.find()) {
            return matcher.group(1)?.replace("&amp;", "&")?.replace("&#39;", "'")?.trim() ?: ""
        }
        val pattern2 = Pattern.compile("<meta\\s+content=[\"'](.*?)[\"']\\s+(?:property|name)=[\"']$property[\"']", Pattern.CASE_INSENSITIVE)
        val matcher2 = pattern2.matcher(html)
        if (matcher2.find()) {
            return matcher2.group(1)?.replace("&amp;", "&")?.replace("&#39;", "'")?.trim() ?: ""
        }
        return ""
    }

    /**
     * 3. تحليل نص الإعلان واستخراج البيانات المنظمة مع نظام منع البيانات الوهمية
     */
    suspend fun analyzeAdText(
        rawText: String,
        sourceUrl: String = "",
        existingProperties: List<PropertyEntity> = emptyList(),
        existingImported: List<ImportedPropertyEntity> = emptyList()
    ): FacebookExtractedAd = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        // إذا كان Gemini متاحاً، نستخرج الحقول منه بصيغة JSON موثقة
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "DEFAULT_API_KEY") {
            try {
                val geminiAd = callGeminiForTextExtraction(rawText, sourceUrl, apiKey)
                if (geminiAd != null) {
                    val dupCheck = checkDuplicates(geminiAd, sourceUrl, existingProperties, existingImported)
                    return@withContext geminiAd.copy(
                        isDuplicate = dupCheck.first,
                        duplicateReason = dupCheck.second,
                        matchedProperty = dupCheck.third,
                        matchedImported = dupCheck.fourth
                    )
                }
            } catch (_: Exception) {
                // استخدام الخبير المحلي
            }
        }

        // المحرك الخبير المحلي الدقيق
        val localResult = RealEstateAiExtractor.extractFromRawText(
            rawText = rawText,
            sourceName = "فيسبوك",
            postUrl = sourceUrl,
            existingProperties = existingProperties,
            existingImported = existingImported
        )

        FacebookExtractedAd(
            title = localResult.title,
            propertyType = localResult.propertyType,
            listingType = localResult.operationType,
            location = localResult.locationZone,
            address = localResult.address,
            price = localResult.price,
            priceUnit = localResult.priceUnit,
            area = localResult.area,
            rooms = localResult.rooms,
            bathrooms = localResult.bathrooms,
            floor = localResult.floor,
            finishing = localResult.finishing,
            streetsCount = localResult.streetsCount,
            facade = localResult.facade,
            description = localResult.cleanedText.ifBlank { rawText },
            phone = localResult.contactPhone,
            ownerName = localResult.contactName,
            sourceUrl = sourceUrl,
            images = "img_property_apartment",
            completenessScore = localResult.completenessScore,
            isDuplicate = localResult.isDuplicate,
            duplicateReason = localResult.duplicateReason,
            matchedProperty = localResult.matchedProperty,
            matchedImported = localResult.matchedImported
        )
    }

    /**
     * 4. ميزة "إضافة إعلان من صورة": قراءة سكرين شوت الإعلان بـ OCR + AI
     */
    suspend fun analyzeAdScreenshot(
        bitmap: Bitmap,
        sourceUrl: String = "",
        existingProperties: List<PropertyEntity> = emptyList(),
        existingImported: List<ImportedPropertyEntity> = emptyList()
    ): FacebookExtractedAd = withContext(Dispatchers.IO) {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (_: Exception) { "" }

        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY" && apiKey != "DEFAULT_API_KEY") {
            try {
                val geminiAd = callGeminiForImageExtraction(bitmap, sourceUrl, apiKey)
                if (geminiAd != null) {
                    val dupCheck = checkDuplicates(geminiAd, sourceUrl, existingProperties, existingImported)
                    return@withContext geminiAd.copy(
                        isDuplicate = dupCheck.first,
                        duplicateReason = dupCheck.second,
                        matchedProperty = dupCheck.third,
                        matchedImported = dupCheck.fourth
                    )
                }
            } catch (_: Exception) {
                // Fallback to local heuristic
            }
        }

        // إذا تعذر استدعاء Gemini (أو offline)، نقدم نموذجاً مستخرجاً آمناً لا يخترع أي بيانات
        val fallbackAd = FacebookExtractedAd(
            title = "عقار مستورد من صورة الإعلان",
            propertyType = "شقة",
            listingType = "للبيع",
            location = "دسوق",
            address = "دسوق",
            price = null, // لا نخترع السعر
            priceUnit = "ج.م",
            area = null, // لا نخترع المساحة
            rooms = null,
            bathrooms = null,
            floor = null,
            finishing = "",
            streetsCount = 1,
            facade = "",
            description = "تم استخراج هذا الإعلان من لقطة شاشة مرفوعة. يرجى مراجعة وتدقيق البيانات قبل النشر.",
            phone = "", // لا نخترع الهاتف
            ownerName = "معلن فيسبوك",
            sourceUrl = sourceUrl,
            images = "img_property_apartment",
            completenessScore = 50
        )
        val dupCheck = checkDuplicates(fallbackAd, sourceUrl, existingProperties, existingImported)
        fallbackAd.copy(
            isDuplicate = dupCheck.first,
            duplicateReason = dupCheck.second,
            matchedProperty = dupCheck.third,
            matchedImported = dupCheck.fourth
        )
    }

    /**
     * استدعاء Gemini Multimodal لقراءة صورة السكرين شوت واستخراج JSON
     */
    private fun callGeminiForImageExtraction(
        bitmap: Bitmap,
        sourceUrl: String,
        apiKey: String
    ): FacebookExtractedAd? {
        val base64Image = bitmapToBase64(bitmap)
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val rootJson = JSONObject()

        // تعليمات النظام
        val systemInstruction = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", getExtractionPromptInstruction()))
        systemInstruction.put("parts", sysParts)
        rootJson.put("systemInstruction", systemInstruction)

        // محتوى الطلب (الصورة + النص التوجيهي)
        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        contentObj.put("role", "user")
        val partsArray = JSONArray()

        partsArray.put(JSONObject().put("text", "اقرأ صورة الإعلان العقاري بدقة واستخرج الحقول بصيغة JSON فقط طبقاً للتعليمات."))

        val inlineData = JSONObject()
        inlineData.put("mimeType", "image/jpeg")
        inlineData.put("data", base64Image)
        partsArray.put(JSONObject().put("inlineData", inlineData))

        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        // إعدادات التوليد بصيغة JSON
        val genConfig = JSONObject()
        genConfig.put("responseMimeType", "application/json")
        genConfig.put("temperature", 0.1)
        rootJson.put("generationConfig", genConfig)

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        geminiClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val resBody = response.body?.string() ?: return null
            val resJson = JSONObject(resBody)
            val candidates = resJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val text = candidates.getJSONObject(0).optJSONObject("content")
                ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: return null

            return parseJsonToFacebookExtractedAd(text, sourceUrl)
        }
    }

    /**
     * استدعاء Gemini لتحليل النص واستخراج JSON
     */
    private fun callGeminiForTextExtraction(
        rawText: String,
        sourceUrl: String,
        apiKey: String
    ): FacebookExtractedAd? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val rootJson = JSONObject()
        val systemInstruction = JSONObject()
        val sysParts = JSONArray()
        sysParts.put(JSONObject().put("text", getExtractionPromptInstruction()))
        systemInstruction.put("parts", sysParts)
        rootJson.put("systemInstruction", systemInstruction)

        val contentsArray = JSONArray()
        val contentObj = JSONObject()
        contentObj.put("role", "user")
        val partsArray = JSONArray()
        partsArray.put(JSONObject().put("text", "النص المستخرج من منشور فيسبوك:\n$rawText\nاستخرج الحقول بصيغة JSON طبقاً للتعليمات."))
        contentObj.put("parts", partsArray)
        contentsArray.put(contentObj)
        rootJson.put("contents", contentsArray)

        val genConfig = JSONObject()
        genConfig.put("responseMimeType", "application/json")
        genConfig.put("temperature", 0.1)
        rootJson.put("generationConfig", genConfig)

        val request = Request.Builder()
            .url(url)
            .post(rootJson.toString().toRequestBody(jsonMediaType))
            .build()

        geminiClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val resBody = response.body?.string() ?: return null
            val resJson = JSONObject(resBody)
            val candidates = resJson.optJSONArray("candidates") ?: return null
            if (candidates.length() == 0) return null
            val text = candidates.getJSONObject(0).optJSONObject("content")
                ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text") ?: return null

            return parseJsonToFacebookExtractedAd(text, sourceUrl)
        }
    }

    private fun getExtractionPromptInstruction(): String {
        return """
            أنت نظام ذكاء اصطناعي خبير ومسؤول عن استخراج بيانات الإعلانات العقارية لمدينة "دسوق" في مصر.
            مهمتك استخراج البيانات الحقيقية فقط والموجودة نصاً في الإعلان بدون أي اختلاق أو تخمين.
            
            قواعد صارمة لمنع البيانات الوهمية:
            - لا تخترع السعر: إذا لم يذكر في المنشور، اتركه null أو فارغاً.
            - لا تخترع العنوان: إذا لم يذكر، اتركه فارغاً واكتب في المنطقة "دسوق".
            - لا تخترع رقم الهاتف: استخرجه فقط إذا كان ظاهراً كأرقام هاتف مصرية، وإلا اتركه فارغاً.
            - لا تخترع المساحة: إذا لم تذكر بالمتر، اتركها null.
            - إذا لم توجد معلومة اتركها فارغة تماماً واكتب في ملاحظاتك "غير متوفر".
            
            أخرج البيانات بصيغة JSON حصراً بهذا الهيكل:
            {
              "title": "عنوان جذاب مختصر مثل: شقة للبيع في حي دحروج",
              "property_type": "شقة" (الخيارات: شقة، فيلا، منزل، أرض، محل، مكتب، عمارة، مخزن، شاليه، عقار تجاري، أخرى),
              "listing_type": "للبيع" (الخيارات: للبيع، للإيجار),
              "location": "دسوق" (الخيارات: دسوق، أحياء ومناطق دسوق، القرى والمناطق المحيطة),
              "address": "العنوان أو اسم الشارع إن وجد",
              "price": 1250000.0 (رقم فقط أو null إذا لم يذكر),
              "area": 150.0 (رقم فقط أو null إذا لم تذكر),
              "rooms": 3 (رقم أو null),
              "bathrooms": 2 (رقم أو null),
              "floor": 4 (رقم أو null),
              "finishing": "سوبر لوكس" (أو فارغ),
              "streets_count": 1 (عدد الشوارع المحيطة),
              "facade": "واجهة بحرية" (أو فارغ),
              "description": "الوصف الكامل للإعلان",
              "phone": "رقم الهاتف الظاهر فقط أو فارغ",
              "owner_name": "اسم المعلن إن كان ظاهراً أو فارغ"
            }
        """.trimIndent()
    }

    private fun parseJsonToFacebookExtractedAd(jsonString: String, sourceUrl: String): FacebookExtractedAd {
        return try {
            val json = JSONObject(jsonString)
            val pType = json.optString("property_type", "شقة")
            val lType = json.optString("listing_type", "للبيع")
            val loc = json.optString("location", "دسوق")
            val addr = json.optString("address", "")
            val priceVal = if (json.has("price") && !json.isNull("price")) json.optDouble("price", -1.0) else null
            val areaVal = if (json.has("area") && !json.isNull("area")) json.optDouble("area", -1.0) else null
            val roomsVal = if (json.has("rooms") && !json.isNull("rooms")) json.optInt("rooms", -1) else null
            val bathsVal = if (json.has("bathrooms") && !json.isNull("bathrooms")) json.optInt("bathrooms", -1) else null
            val floorVal = if (json.has("floor") && !json.isNull("floor")) json.optInt("floor", -1) else null

            FacebookExtractedAd(
                title = json.optString("title", "$pType $lType في $loc"),
                propertyType = pType,
                listingType = lType,
                location = loc,
                address = addr,
                price = if (priceVal != null && priceVal > 0) priceVal else null,
                priceUnit = if (lType == "للإيجار") "ج.م / شهرياً" else "ج.م",
                area = if (areaVal != null && areaVal > 0) areaVal else null,
                rooms = if (roomsVal != null && roomsVal >= 0) roomsVal else null,
                bathrooms = if (bathsVal != null && bathsVal >= 0) bathsVal else null,
                floor = if (floorVal != null && floorVal >= 0) floorVal else null,
                finishing = json.optString("finishing", ""),
                streetsCount = json.optInt("streets_count", 1),
                facade = json.optString("facade", ""),
                description = json.optString("description", ""),
                phone = json.optString("phone", ""),
                ownerName = json.optString("owner_name", ""),
                sourceUrl = sourceUrl,
                images = "img_property_apartment",
                completenessScore = 85
            )
        } catch (_: Exception) {
            FacebookExtractedAd(
                title = "عقار مستورد من فيسبوك",
                sourceUrl = sourceUrl
            )
        }
    }

    /**
     * 5. نظام كشف التكرار (Duplicate Detection System):
     * يقارن الإعلان الجديد مع العقارات الحالية برقم الهاتف، رابط المنشور، الصور، العنوان، السعر، المساحة، وتشابه الوصف
     */
    fun checkDuplicates(
        ad: FacebookExtractedAd,
        sourceUrl: String,
        existingProperties: List<PropertyEntity>,
        existingImported: List<ImportedPropertyEntity>
    ): Quadruple<Boolean, String, PropertyEntity?, ImportedPropertyEntity?> {
        val checkUrl = (sourceUrl.ifBlank { ad.sourceUrl }).trim()

        // 1. فحص برابط المنشور الأصلي
        if (checkUrl.isNotBlank()) {
            val dupImport = existingImported.firstOrNull { 
                (it.sourceUrl == checkUrl && it.sourceUrl.isNotBlank()) || (it.postUrl == checkUrl && it.postUrl.isNotBlank())
            }
            if (dupImport != null) {
                return Quadruple(true, "تطابق كامل في رابط منشور Facebook الأصلي مع (${dupImport.title})", null, dupImport)
            }
            val dupProp = existingProperties.firstOrNull { it.sourceUrl == checkUrl && it.sourceUrl.isNotBlank() }
            if (dupProp != null) {
                return Quadruple(true, "تطابق في رابط منشور Facebook الأصلي مع العقار #${dupProp.id} (${dupProp.title})", dupProp, null)
            }
        }

        // 2. فحص برقم الهاتف
        if (ad.phone.isNotBlank()) {
            val phoneClean = ad.phone.replace("+20", "").replace(" ", "").trim()
            if (phoneClean.length >= 10) {
                val dupProp = existingProperties.firstOrNull { it.phone.contains(phoneClean) }
                if (dupProp != null) {
                    val reason = if (ad.price != null && dupProp.price == ad.price) {
                        "تطابق في رقم الهاتف ($phoneClean) والسعر (${ad.price.toInt()} ج.م) مع عقار #${dupProp.id}"
                    } else {
                        "تطابق برقم هاتف المعلن ($phoneClean) مع عقار #${dupProp.id} (${dupProp.title})"
                    }
                    return Quadruple(true, reason, dupProp, null)
                }
            }
        }

        // 3. فحص بالعنوان + السعر + المساحة
        if (ad.price != null && ad.area != null) {
            val dupProp = existingProperties.firstOrNull {
                (it.district == ad.location || it.location.contains(ad.location)) &&
                        it.price == ad.price &&
                        it.area == ad.area
            }
            if (dupProp != null) {
                return Quadruple(
                    true,
                    "تطابق كامل في الموقع (${ad.location}) والسعر (${ad.price.toInt()} ج.م) والمساحة (${ad.area.toInt()} م²) مع عقار #${dupProp.id}",
                    dupProp,
                    null
                )
            }
        }

        // 4. فحص بتشابه الوصف
        if (ad.description.length > 30) {
            val newWords = ad.description.split(" ").filter { it.length > 3 }.toSet()
            val dupProp = existingProperties.firstOrNull { prop ->
                if (prop.description.length > 30) {
                    val propWords = prop.description.split(" ").filter { it.length > 3 }.toSet()
                    val common = newWords.intersect(propWords).size
                    val ratio = common.toDouble() / newWords.size.coerceAtLeast(1)
                    ratio > 0.70
                } else false
            }
            if (dupProp != null) {
                return Quadruple(
                    true,
                    "تشابه كبير في نص وصف الإعلان مع عقار #${dupProp.id} (${dupProp.title})",
                    dupProp,
                    null
                )
            }
        }

        return Quadruple(false, "", null, null)
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        // تقليص الحجم لضمان سرعة الإرسال وكفاءة الذاكرة
        val scaled = if (bitmap.width > 1200 || bitmap.height > 1200) {
            val ratio = 1200f / maxOf(bitmap.width, bitmap.height)
            Bitmap.createScaledBitmap(bitmap, (bitmap.width * ratio).toInt(), (bitmap.height * ratio).toInt(), true)
        } else {
            bitmap
        }
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
