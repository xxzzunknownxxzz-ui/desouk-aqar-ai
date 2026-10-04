package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bathtub
import androidx.compose.material.icons.filled.Bed
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CustomerRequestEntity
import com.example.data.PropertyEntity
import com.example.data.SavedSearchManager
import com.example.data.ai.AiComparisonData
import com.example.data.ai.AiInvestmentData
import com.example.data.ai.AiResponsePayload
import com.example.data.ai.AiValuationData
import com.example.data.ai.ChecklistItem
import com.example.data.ai.DesoukAiAssistantService
import com.example.data.ai.PropertyMatchResult
import com.example.ui.components.PropertyImageLoader
import com.example.ui.theme.BadgeForRentTeal
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AssistantChatMessage(
    val id: Long,
    val text: String,
    val isFromUser: Boolean,
    val time: String = SimpleDateFormat("hh:mm a", Locale.forLanguageTag("ar")).format(Date()),
    val payload: AiResponsePayload? = null
)

@Composable
fun AiAssistantScreen(
    properties: List<PropertyEntity>,
    onBack: () -> Unit,
    customerRequests: List<CustomerRequestEntity> = emptyList(),
    favoriteProperties: List<PropertyEntity> = emptyList(),
    currentUserRole: String = "User",
    onPropertyClick: ((PropertyEntity) -> Unit)? = null,
    onFavoriteToggle: ((Long, Boolean) -> Unit)? = null,
    onNavigateToSearch: ((String, String?) -> Unit)? = null,
    initialQuery: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val assistantService = remember { DesoukAiAssistantService() }
    val savedSearchManager = remember { SavedSearchManager(context) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    var inputText by remember { mutableStateOf("") }
    var isThinking by remember { mutableStateOf(false) }

    val welcomeText = """
        أهلاً بيك 👋
        أنا **المساعد العقاري الذكي** في عقارات دسوق AI.
        قولّي بتدور على إيه، وأنا أساعدك تلاقي العقار المناسب بكل سهولة.
        
        💡 **أمثلة:**
        • "عاوز شقة 3 غرف للبيع في دسوق لحد مليون ونص"
        • "عاوز محل تجاري للإيجار في شارع الجيش"
        • "قارن بين أرخص شقق معروضة"
        • "حلل لي سعر المتر في كورنيش النيل"
        • "ساعدني أشتري / ساعدني أستأجر"
    """.trimIndent()

    val messages = remember {
        mutableStateListOf(
            AssistantChatMessage(
                id = 1L,
                text = welcomeText,
                isFromUser = false
            )
        )
    }

    // الأزرار والتصنيفات العلوية السريعة (حسب المتطلبات: 70)
    val topActions = remember(currentUserRole) {
        val base = mutableListOf(
            "🔎 ابحث عن عقار",
            "🏠 للبيع",
            "🔑 للإيجار",
            "💰 حسب الميزانية",
            "📍 حسب المنطقة",
            "⚖️ قارن عقارات",
            "📊 حلل السعر",
            "✍️ اكتب إعلان",
            "❤️ المفضلة",
            "📋 قائمة الفحص",
            "⚖️ نصائح قبل الشراء"
        )
        if (currentUserRole == "Admin" || currentUserRole == "Super Admin") {
            base.add("🛡️ فحص البيانات الناقصة")
            base.add("🔍 اكتشاف العقارات المكررة")
            base.add("👔 مطابقة طلبات العملاء")
        }
        base
    }

    fun handleSend(queryText: String) {
        if (queryText.isBlank() || isThinking) return
        val userQuery = queryText.trim()
        inputText = ""

        messages.add(
            AssistantChatMessage(
                id = System.currentTimeMillis(),
                text = userQuery,
                isFromUser = true
            )
        )

        isThinking = true
        scope.launch {
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }

            val history = messages.map { it.text to it.isFromUser }
            val payload = assistantService.processQuery(
                userMessage = userQuery,
                history = history,
                properties = properties,
                customerRequests = customerRequests,
                favoriteProperties = favoriteProperties,
                userRole = currentUserRole
            )

            // في حال رغبة المستخدم في حفظ البحث
            if (payload.intent == com.example.data.ai.AiIntent.SAVE_SEARCH && payload.searchCriteria != null) {
                savedSearchManager.saveSearch(payload.searchCriteria)
            }

            messages.add(
                AssistantChatMessage(
                    id = System.currentTimeMillis() + 1,
                    text = payload.responseText,
                    isFromUser = false,
                    payload = payload
                )
            )

            isThinking = false
            if (messages.isNotEmpty()) {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }

    // إعداد لاقط الصوت والتعرف على الكلام (Speech to Text)
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                handleSend(spoken)
            }
        }
    }

    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ar-EG")
            putExtra(RecognizerIntent.EXTRA_PROMPT, "تحدث الآن بالطلب العقاري...")
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "التعرف الصوتي غير مدعوم على جهازك حاليًا", Toast.LENGTH_SHORT).show()
        }
    }

    // إرسال الطلب الأولي التلقائي إذا أتى من الشاشة الرئيسية
    var hasHandledInitialQuery by remember { mutableStateOf(false) }
    LaunchedEffect(initialQuery) {
        if (!initialQuery.isNullOrBlank() && !hasHandledInitialQuery) {
            hasHandledInitialQuery = true
            handleSend(initialQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // شريط العنوان العلوي
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = DesoukNavyDark,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("ai_assistant_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = Color.White
                    )
                }

                val pulseTransition = rememberInfiniteTransition(label = "pulse")
                val pulseScale by pulseTransition.animateFloat(
                    initialValue = 0.95f,
                    targetValue = 1.05f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1200),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_scale"
                )

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(DesoukGold, Color(0xFFFFA000)))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "روبوت الذكاء الاصطناعي",
                        tint = DesoukNavyDark,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "المساعد العقاري الذكي",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "AI",
                            tint = DesoukGold,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    Text(
                        text = "قولّي إنت عايز إيه في العقارات وأنا أساعدك",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }

                // زر مسح الجلسة والبدء من جديد
                IconButton(
                    onClick = {
                        assistantService.intentParser.resetContext()
                        messages.clear()
                        messages.add(
                            AssistantChatMessage(
                                id = System.currentTimeMillis(),
                                text = "تم بدء جلسة جديدة! كيف يمكنني مساعدتك في عقارات دسوق اليوم؟ 🏢✨",
                                isFromUser = false
                            )
                        )
                    },
                    modifier = Modifier.testTag("ai_clear_chat_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "مسح المحادثة",
                        tint = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // شريط الأزرار والتصنيفات العلوية السريعة (Quick Action Bar)
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(topActions) { actionLabel ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SurfaceBackground,
                    border = BorderStroke(1.dp, DesoukGold.copy(alpha = 0.6f)),
                    modifier = Modifier.clickable {
                        when (actionLabel) {
                            "🔎 ابحث عن عقار" -> handleSend("عايز ابحث عن عقار في دسوق")
                            "🏠 للبيع" -> handleSend("شقق للبيع في دسوق")
                            "🔑 للإيجار" -> handleSend("شقق للإيجار في دسوق")
                            "💰 حسب الميزانية" -> handleSend("شقق في حدود مليون ونص")
                            "📍 حسب المنطقة" -> handleSend("عقارات في شارع الجيش")
                            "⚖️ قارن عقارات" -> handleSend("قارن بين العقارات")
                            "📊 حلل السعر" -> handleSend("حلل سعر العقار وسعر المتر")
                            "✍️ اكتب إعلان" -> handleSend("ساعدني أكتب إعلان لعقاري")
                            "❤️ المفضلة" -> handleSend("اعرض عقاراتي المحفوظة")
                            "📋 قائمة الفحص" -> handleSend("قائمة فحص المعاينة")
                            "⚖️ نصائح قبل الشراء" -> handleSend("إيه اللي أسأل عنه قبل ما أشتري؟")
                            "🛡️ فحص البيانات الناقصة" -> handleSend("فحص البيانات الناقصة")
                            "🔍 اكتشاف العقارات المكررة" -> handleSend("اكتشاف الإعلانات المكررة")
                            "👔 مطابقة طلبات العملاء" -> handleSend("مطابقة طلبات العملاء")
                            else -> handleSend(actionLabel)
                        }
                    }
                ) {
                    Text(
                        text = actionLabel,
                        color = DesoukNavyDark,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        HorizontalDivider(color = Color(0xFFE0E0E0), thickness = 0.8.dp)

        // تيار الرسائل التفاعلية (Chat Stream)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageItem(
                    message = msg,
                    onPropertyClick = onPropertyClick,
                    onFavoriteToggle = onFavoriteToggle,
                    onQuickActionClick = { q -> handleSend(q) }
                )
            }

            if (isThinking) {
                item {
                    AiThinkingCard()
                }
            }
        }

        // حقل الإدخال السفلي مع زر الميكروفون والإرسال
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // زر التسجيل الصوتي
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DesoukNavyDark.copy(alpha = 0.08f))
                        .clickable { startVoiceInput() }
                        .testTag("ai_assistant_mic_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "بحث صوتي",
                        tint = DesoukNavyDark,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "اكتب طلبك العقاري (مثال: شقة للبيع بالجيش)...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("ai_assistant_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceBackground,
                        unfocusedContainerColor = SurfaceBackground,
                        focusedIndicatorColor = DesoukGold,
                        unfocusedIndicatorColor = Color(0xFFD6D6D6)
                    ),
                    maxLines = 3
                )

                Spacer(modifier = Modifier.width(8.dp))

                // زر الإرسال
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isThinking) {
                                Brush.linearGradient(listOf(DesoukNavyDark, Color(0xFF1565C0)))
                            } else {
                                Brush.linearGradient(listOf(Color(0xFFCFD8DC), Color(0xFFB0BEC5)))
                            }
                        )
                        .clickable(enabled = inputText.isNotBlank() && !isThinking) {
                            handleSend(inputText)
                        }
                        .testTag("ai_assistant_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isThinking) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "إرسال",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * مكون رسالة المحادثة التفاعلي مع دعم البطاقات والمقارنة وقوائم الفحص
 */
@Composable
fun ChatMessageItem(
    message: AssistantChatMessage,
    onPropertyClick: ((PropertyEntity) -> Unit)?,
    onFavoriteToggle: ((Long, Boolean) -> Unit)?,
    onQuickActionClick: (String) -> Unit
) {
    val isUser = message.isFromUser

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(DesoukNavyDark),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartToy,
                        contentDescription = "AI",
                        tint = DesoukGold,
                        modifier = Modifier.size(19.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                color = if (isUser) DesoukNavyDark else Color.White,
                shadowElevation = if (isUser) 1.dp else 2.dp,
                border = if (!isUser) BorderStroke(1.dp, Color(0xFFE8EAF6)) else null,
                modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = if (isUser) Color.White else Color(0xFF263238),
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = message.time,
                        color = if (isUser) Color.White.copy(alpha = 0.6f) else TextMuted,
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }

        // عرض بطاقات العقارات الحقيقية المضمنة في الرد (Embedded Property Cards)
        val payload = message.payload
        if (payload != null && payload.matchedProperties.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "🏠 العقارات المطابقة المسجلة بقاعدة البيانات:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark,
                modifier = Modifier.padding(start = 42.dp, bottom = 6.dp)
            )

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 38.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(payload.matchedProperties) { matchResult ->
                    EmbeddedPropertyCard(
                        matchResult = matchResult,
                        onPropertyClick = onPropertyClick,
                        onFavoriteToggle = onFavoriteToggle
                    )
                }
            }
        }

        // جدول المقارنة المضمن
        if (payload?.comparisonData != null) {
            Spacer(modifier = Modifier.height(10.dp))
            EmbeddedComparisonTable(
                comparisonData = payload.comparisonData,
                modifier = Modifier.padding(start = 38.dp)
            )
        }

        // بطاقة تحليل السعر وسعر المتر
        if (payload?.valuationData != null) {
            Spacer(modifier = Modifier.height(10.dp))
            EmbeddedValuationCard(
                valuationData = payload.valuationData,
                modifier = Modifier.padding(start = 38.dp)
            )
        }

        // بطاقة التحليل الاستثماري
        if (payload?.investmentData != null) {
            Spacer(modifier = Modifier.height(10.dp))
            EmbeddedInvestmentCard(
                investmentData = payload.investmentData,
                modifier = Modifier.padding(start = 38.dp)
            )
        }

        // بطاقة قائمة الفحص التفاعلية (Checklist)
        if (payload != null && payload.checklistItems.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            EmbeddedChecklistCard(
                items = payload.checklistItems,
                modifier = Modifier.padding(start = 38.dp)
            )
        }

        // الأزرار التفاعلية المقترحة بعد الرد
        if (payload != null && payload.quickActions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(payload.quickActions) { action ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFE8EAF6),
                        border = BorderStroke(1.dp, DesoukGold),
                        modifier = Modifier.clickable { onQuickActionClick(action.queryToTrigger) }
                    ) {
                        Text(
                            text = action.label,
                            color = DesoukNavyDark,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * بطاقة عقار حقيقية ومكتملة داخل المحادثة مع شارة التطابق وأزرار الاتصال
 */
@Composable
fun EmbeddedPropertyCard(
    matchResult: PropertyMatchResult,
    onPropertyClick: ((PropertyEntity) -> Unit)?,
    onFavoriteToggle: ((Long, Boolean) -> Unit)?
) {
    val context = LocalContext.current
    val prop = matchResult.property
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale("ar", "EG")) }

    Card(
        modifier = Modifier
            .width(225.dp)
            .shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // صورة العقار
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
            ) {
                PropertyImageLoader(
                    imageResName = prop.imageResName,
                    contentDescription = prop.title,
                    modifier = Modifier.fillMaxSize()
                )

                // شارة بيع / إيجار
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (prop.type == "للبيع") BadgeForSaleRed else BadgeForRentTeal)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(text = prop.type, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                // شارة Match Score
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (matchResult.isExact) Color(0xFF2E7D32) else Color(0xFFF57C00))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${matchResult.matchScore}% تطابق",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = prop.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = DesoukNavyDark,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = DesoukGold, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(text = "${prop.district} • ${prop.location}", fontSize = 11.sp, color = TextMuted, maxLines = 1)
                }

                Spacer(modifier = Modifier.height(4.dp))

                // السعر
                Text(
                    text = "${numberFormat.format(prop.price.toLong())} ${prop.priceUnit}",
                    color = BadgeForSaleRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                // المواصفات (غرف، حمام، مساحة)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (prop.rooms > 0) {
                        Text(text = "🛏️ ${prop.rooms} غرف", fontSize = 10.sp, color = TextSecondary)
                    }
                    if (prop.area > 0) {
                        Text(text = "📐 ${prop.area.toInt()} م²", fontSize = 10.sp, color = TextSecondary)
                    }
                    if (prop.floor > 0) {
                        Text(text = "🏢 دور ${prop.floor}", fontSize = 10.sp, color = TextSecondary)
                    }
                }

                // بيان الفرق في حال البحث المرن
                if (!matchResult.isExact && matchResult.differences.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⚠️ ${matchResult.differences.first()}",
                        fontSize = 9.5.sp,
                        color = Color(0xFFD84315),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // أزرار الإجراءات السريعة (تفاصيل، اتصال، واتساب، خريطة)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { onPropertyClick?.invoke(prop) },
                        modifier = Modifier.weight(1f).height(32.dp),
                        contentPadding = PaddingValues(0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(text = "التفاصيل", fontSize = 11.sp, color = Color.White)
                    }

                    // زر الاتصال الهاتفي المباشر
                    IconButton(
                        onClick = {
                            val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${prop.phone}"))
                            context.startActivity(dialIntent)
                        },
                        modifier = Modifier.size(32.dp).background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "اتصال", tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                    }

                    // زر الخريطة
                    IconButton(
                        onClick = {
                            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:${prop.latitude},${prop.longitude}?q=${prop.latitude},${prop.longitude}(${Uri.encode(prop.title)})"))
                            try { context.startActivity(mapIntent) } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(32.dp).background(Color(0xFFE3F2FD), RoundedCornerShape(8.dp))
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = "خريطة", tint = Color(0xFF1976D2), modifier = Modifier.size(16.dp))
                    }

                    // زر المفضلة
                    IconButton(
                        onClick = { onFavoriteToggle?.invoke(prop.id, !prop.isFavorite) },
                        modifier = Modifier.size(32.dp).background(Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = if (prop.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "مفضلة",
                            tint = BadgeForSaleRed,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * جدول المقارنة المضمن
 */
@Composable
fun EmbeddedComparisonTable(
    comparisonData: AiComparisonData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, DesoukGold.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = "⚖️ جدول المقارنة الفنية والمالية:",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = DesoukNavyDark
            )
            Spacer(modifier = Modifier.height(8.dp))

            comparisonData.comparisonPoints.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(if (index % 2 == 0) Color(0xFFF8F9FA) else Color.White)
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = row.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DesoukNavyDark, modifier = Modifier.width(90.dp))
                    row.values.forEach { v ->
                        Text(text = v, fontSize = 11.sp, color = TextSecondary, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * بطاقة تحليل سعر العقار وسعر المتر
 */
@Composable
fun EmbeddedValuationCard(
    valuationData: AiValuationData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
        border = BorderStroke(1.dp, Color(0xFF81C784))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.TrendingUp, contentDescription = null, tint = Color(0xFF2E7D32))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "📊 نتيجة التقييم الاسترشادي لسعر المتر:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF1B5E20))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = valuationData.analysisText, fontSize = 12.sp, color = Color(0xFF2E7D32), lineHeight = 18.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "⚠️ " + valuationData.disclaimer, fontSize = 10.sp, color = TextMuted)
        }
    }
}

/**
 * بطاقة التحليل الاستثماري
 */
@Composable
fun EmbeddedInvestmentCard(
    investmentData: AiInvestmentData,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = BorderStroke(1.dp, DesoukGold)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFF57F17))
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "📈 مؤشرات العائد الاستثماري:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFFE65100))
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "• العائد السنوي المتوقع: ${String.format("%.1f", investmentData.annualRentalYieldPercentage)}%\n" +
                        "• الإيجار الشهري المقدر: ${investmentData.estimatedMonthlyRent.toLong()} ج.م\n" +
                        "• فترة استرداد رأس المال التقديرية: ${String.format("%.1f", investmentData.estimatedPaybackYears)} سنة",
                fontSize = 12.sp,
                color = Color(0xFF3E2723),
                lineHeight = 19.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "ℹ️ " + investmentData.disclaimer, fontSize = 9.5.sp, color = TextMuted)
        }
    }
}

/**
 * قائمة الفحص التفاعلية (Checklist)
 */
@Composable
fun EmbeddedChecklistCard(
    items: List<ChecklistItem>,
    modifier: Modifier = Modifier
) {
    val checkStates = remember { mutableStateListOf(*items.map { false }.toTypedArray()) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFB0BEC5))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = "📋 بنود الفحص الميداني (اضغط للتعليم أثناء المعاينة):", fontWeight = FontWeight.Bold, fontSize = 12.5.sp, color = DesoukNavyDark)
            Spacer(modifier = Modifier.height(6.dp))

            items.forEachIndexed { idx, item ->
                val isChecked = if (idx < checkStates.size) checkStates[idx] else false
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { checkStates[idx] = !checkStates[idx] }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isChecked) Color(0xFF2E7D32) else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = item.title, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = if (isChecked) Color(0xFF2E7D32) else DesoukNavyDark)
                        Text(text = item.description, fontSize = 10.sp, color = TextMuted)
                    }
                }
            }
        }
    }
}

/**
 * مؤشر التفكير أثناء معالجة السؤال
 */
@Composable
fun AiThinkingCard() {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DesoukNavyDark),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "AI",
                tint = DesoukGold,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = DesoukGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "جاري فهم طلبك والبحث في قاعدة بيانات عقارات دسوق...",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
