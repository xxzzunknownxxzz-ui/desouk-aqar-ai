package com.example.ui.screens

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CommunityCommentEntity
import com.example.data.CommunityPostEntity
import com.example.data.PropertyEntity
import com.example.data.ai.AiQueryState
import com.example.data.ai.CommunityAiMatcher
import com.example.data.ai.ExtractedPostData
import com.example.ui.components.bounceClick
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// التصنيفات الـ 9 الرسمية للمنشورات
val COMMUNITY_POST_TYPES = listOf(
    "🏠 عقار للبيع",
    "🔑 عقار للإيجار",
    "🔎 مطلوب عقار",
    "🌾 أرض",
    "🏢 محل أو وحدة تجارية",
    "🏬 مكتب أو وحدة إدارية",
    "💬 سؤال عقاري",
    "📢 إعلان عقاري",
    "📰 معلومة أو نصيحة عقارية"
)

// تصنيفات المجتمع الرئيسية المطلوبة (شراء، بيع، إيجار، استثمار، أراضي، نصائح عقارية)
val COMMUNITY_MAIN_CATEGORIES = listOf(
    "🌟 الكل",
    "🛒 شراء",
    "🏷️ بيع",
    "🔑 إيجار",
    "📈 استثمار",
    "🌾 أراضي",
    "💡 نصائح عقارية"
)

// الأقسام الـ 5 الرئيسية لصفحة المجتمع العقاري
enum class CommunitySection(val title: String, val badge: String? = null) {
    POSTS("منشورات المجتمع"),
    QUESTION("سؤال المجتمع", "استطلاع"),
    DISCUSSIONS("أحدث المناقشات", "ساخن"),
    OFFERS("عروض الأعضاء", "فرص"),
    EXPERT("اسأل خبير عقاري", "معتمد")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityScreen(
    posts: List<CommunityPostEntity>,
    allProperties: List<PropertyEntity>,
    currentUserName: String,
    currentUserRole: String,
    onBack: () -> Unit,
    onPropertyClick: (PropertyEntity) -> Unit,
    onLikeToggle: (postId: Long, currentLikes: Int, isLiked: Boolean) -> Unit,
    onSaveToggle: (postId: Long, currentSaved: Boolean) -> Unit,
    onFollowToggle: (postId: Long, currentFollow: Boolean) -> Unit,
    onGetComments: (postId: Long) -> Flow<List<CommunityCommentEntity>>,
    onAddComment: (postId: Long, authorName: String, authorRole: String, text: String) -> Unit,
    onCreatePost: (
        authorName: String,
        authorRole: String,
        authorPhone: String?,
        postType: String,
        content: String,
        imagesJson: String,
        hasVideo: Boolean,
        videoLabel: String?,
        district: String?,
        extractedType: String?,
        extractedDeal: String?,
        extractedLocation: String?,
        extractedRooms: String?,
        extractedFloor: String?,
        extractedPrice: String?,
        extractedArea: String?
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // القسم النشط والتصنيف والبحث
    var selectedSection by remember { mutableStateOf(CommunitySection.POSTS) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("🌟 الكل") }
    var selectedTypeFilter by remember { mutableStateOf<String?>(null) }

    // Sheet / Dialog states
    var showCreatePostSheet by remember { mutableStateOf(false) }
    var initialCreatePostType by remember { mutableStateOf<String?>(null) }
    var initialCreatePostContent by remember { mutableStateOf("") }

    var showAiAssistantSheet by remember { mutableStateOf(false) }
    var activeCommentsPost by remember { mutableStateOf<CommunityPostEntity?>(null) }
    var activeReportPost by remember { mutableStateOf<CommunityPostEntity?>(null) }

    // تصفية المنشورات بمرونة تشمل البحث، التصنيف، والقسم
    val filteredPosts = remember(posts, searchQuery, selectedCategoryFilter, selectedTypeFilter, selectedSection) {
        posts.filter { post ->
            val matchesQuery = searchQuery.isBlank() ||
                    post.content.contains(searchQuery, ignoreCase = true) ||
                    post.authorName.contains(searchQuery, ignoreCase = true) ||
                    (post.extractedLocation?.contains(searchQuery, ignoreCase = true) == true)

            val matchesCategory = when (selectedCategoryFilter) {
                "🛒 شراء" -> post.postType == "🔎 مطلوب عقار" || post.content.contains("شراء") || post.content.contains("مطلوب")
                "🏷️ بيع" -> post.postType.contains("بيع") || post.content.contains("بيع") || post.content.contains("للبيع")
                "🔑 إيجار" -> post.postType.contains("إيجار") || post.content.contains("إيجار") || post.content.contains("للإيجار")
                "📈 استثمار" -> post.content.contains("استثمار") || post.content.contains("محل") || post.content.contains("تجاري") || post.content.contains("مكتب")
                "🌾 أراضي" -> post.postType.contains("أرض") || post.content.contains("أرض") || post.content.contains("فدان")
                "💡 نصائح عقارية" -> post.postType.contains("نصيحة") || post.postType.contains("سؤال") || post.content.contains("نصيح") || post.content.contains("معلومة")
                else -> true
            }

            val matchesType = selectedTypeFilter == null || post.postType == selectedTypeFilter

            val matchesSection = when (selectedSection) {
                CommunitySection.POSTS -> true
                CommunitySection.QUESTION -> post.postType.contains("سؤال") || post.content.contains("؟") || post.content.contains("سؤال") || post.content.contains("استفسار")
                CommunitySection.DISCUSSIONS -> post.postType.contains("نصيحة") || post.postType.contains("سؤال") || post.commentsCount > 0 || post.content.length > 40
                CommunitySection.OFFERS -> post.postType.contains("بيع") || post.postType.contains("إيجار") || post.postType.contains("أرض") || post.postType.contains("محل")
                CommunitySection.EXPERT -> post.authorRole in listOf("مكتب عقاري", "وسيط معتمد", "خبير عقاري") || post.content.contains("استشارة") || post.postType.contains("سؤال")
            }

            matchesQuery && matchesCategory && matchesType && matchesSection
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = SurfaceBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CommunityTopBar(
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                onBack = onBack,
                onOpenAiAssistant = { showAiAssistantSheet = true }
            )
        },
        floatingActionButton = {
            // 6. الزر العائم "+" لإضافة منشور أو سؤال
            FloatingActionButton(
                onClick = {
                    initialCreatePostType = when (selectedSection) {
                        CommunitySection.QUESTION -> "💬 سؤال عقاري"
                        CommunitySection.DISCUSSIONS -> "📰 معلومة أو نصيحة عقارية"
                        CommunitySection.OFFERS -> "🏠 عقار للبيع"
                        CommunitySection.EXPERT -> "💬 سؤال عقاري"
                        else -> null
                    }
                    initialCreatePostContent = ""
                    showCreatePostSheet = true
                },
                containerColor = DesoukGold,
                contentColor = DesoukNavyDark,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("community_fab_add")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة منشور أو سؤال",
                        tint = DesoukNavyDark,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = when (selectedSection) {
                            CommunitySection.QUESTION -> "اطرح سؤالاً"
                            CommunitySection.EXPERT -> "استشر خبيراً"
                            CommunitySection.DISCUSSIONS -> "ابدأ مناقشة"
                            CommunitySection.OFFERS -> "أضف عرضك"
                            else -> "منشور جديد"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // شريط الأقسام الـ 5 الرئيسية للمجتمع
            item {
                CommunitySectionTabsRow(
                    selectedSection = selectedSection,
                    onSelectSection = { section ->
                        selectedSection = section
                    }
                )
            }

            when (selectedSection) {
                // 1. منشورات المجتمع (News Feed الرئيسي)
                CommunitySection.POSTS -> {
                    // المساعد العقاري الذكي AI
                    item {
                        CommunityAiHeroCard(
                            onClick = { showAiAssistantSheet = true }
                        )
                    }

                    // خانة إنشاء منشور سريع
                    item {
                        CreatePostPromptCard(
                            currentUserName = currentUserName,
                            onOpenCreate = { type ->
                                initialCreatePostType = type
                                initialCreatePostContent = ""
                                showCreatePostSheet = true
                            }
                        )
                    }

                    // 9. شريط التصنيفات الرئيسية: شراء، بيع، إيجار، استثمار، أراضي، نصائح عقارية
                    item {
                        CommunityCategoryChipsRow(
                            selectedCategory = selectedCategoryFilter,
                            onSelectCategory = { selectedCategoryFilter = it }
                        )
                    }
                }

                // 2. سؤال المجتمع
                CommunitySection.QUESTION -> {
                    item {
                        CommunityWeeklyPollCard(
                            onVoteSubmitted = { option ->
                                scope.launch {
                                    snackbarHostState.showSnackbar("تم تسجيل تصويتك: $option بنجاح شكراً لمشاركتك!")
                                }
                            }
                        )
                    }

                    item {
                        AskQuestionPromptCard(
                            onAskClick = {
                                initialCreatePostType = "💬 سؤال عقاري"
                                initialCreatePostContent = ""
                                showCreatePostSheet = true
                            }
                        )
                    }
                }

                // 3. أحدث المناقشات العقارية
                CommunitySection.DISCUSSIONS -> {
                    item {
                        CommunityTrendingDiscussionsHeader(
                            onStartDiscussion = {
                                initialCreatePostType = "📰 معلومة أو نصيحة عقارية"
                                initialCreatePostContent = ""
                                showCreatePostSheet = true
                            }
                        )
                    }
                }

                // 4. عروض عقارية من الأعضاء
                CommunitySection.OFFERS -> {
                    item {
                        MemberOffersBanner(
                            onAddOffer = {
                                initialCreatePostType = "🏠 عقار للبيع"
                                initialCreatePostContent = ""
                                showCreatePostSheet = true
                            }
                        )
                    }
                }

                // 5. قسم "اسأل خبير عقاري"
                CommunitySection.EXPERT -> {
                    item {
                        AskRealEstateExpertSection(
                            onAskConsultation = { expertName ->
                                initialCreatePostType = "💬 سؤال عقاري"
                                initialCreatePostContent = "استشارة موجهة لـ $expertName: "
                                showCreatePostSheet = true
                            }
                        )
                    }
                }
            }

            // عرض المنشورات وفق القسم المختار والتصفية
            if (filteredPosts.isEmpty()) {
                item {
                    EmptyCommunityView(
                        isFiltered = searchQuery.isNotBlank() || selectedCategoryFilter != "🌟 الكل" || selectedTypeFilter != null,
                        onClearFilters = {
                            searchQuery = ""
                            selectedCategoryFilter = "🌟 الكل"
                            selectedTypeFilter = null
                        },
                        onCreatePost = {
                            initialCreatePostType = null
                            initialCreatePostContent = ""
                            showCreatePostSheet = true
                        }
                    )
                }
            } else {
                items(filteredPosts, key = { it.id }) { post ->
                    CommunityPostCard(
                        post = post,
                        allProperties = allProperties,
                        onPropertyClick = onPropertyClick,
                        onLikeToggle = { onLikeToggle(post.id, post.likesCount, post.isLikedByMe) },
                        onSaveToggle = {
                            onSaveToggle(post.id, post.isSavedByMe)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (!post.isSavedByMe) "تم حفظ المنشور في المفضلة" else "تم إلغاء حفظ المنشور"
                                )
                            }
                        },
                        onFollowToggle = {
                            onFollowToggle(post.id, post.isFollowed)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (!post.isFollowed) "أصبحت تتابع ${post.authorName}" else "تم إلغاء متابعة ${post.authorName}"
                                )
                            }
                        },
                        onCommentClick = { activeCommentsPost = post },
                        onShareClick = {
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "من تطبيق عقارات دسوق:\n${post.authorName} (${post.postType}):\n\n${post.content}\n\nتابع المزيد في تطبيق عقارات دسوق Ai"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "مشاركة المنشور"))
                        },
                        onReportClick = { activeReportPost = post }
                    )
                }
            }
        }

        // Sheet: إنشاء منشور جديد مع تنظيم البيانات بالذكاء الاصطناعي
        if (showCreatePostSheet) {
            CreatePostBottomSheet(
                initialType = initialCreatePostType,
                initialContent = initialCreatePostContent,
                currentUserName = currentUserName,
                currentUserRole = currentUserRole,
                onDismiss = { showCreatePostSheet = false },
                onPublish = { type, content, imagesJson, hasVideo, videoLabel, extracted ->
                    onCreatePost(
                        currentUserName,
                        currentUserRole,
                        null,
                        type,
                        content,
                        imagesJson,
                        hasVideo,
                        videoLabel,
                        extracted.location.ifBlank { null },
                        extracted.propertyType.ifBlank { null },
                        extracted.dealType.ifBlank { null },
                        extracted.location.ifBlank { null },
                        extracted.rooms.ifBlank { null },
                        extracted.floor.ifBlank { null },
                        extracted.price.ifBlank { null },
                        extracted.area.ifBlank { null }
                    )
                    showCreatePostSheet = false
                    scope.launch {
                        snackbarHostState.showSnackbar("تم نشر منشورك في المجتمع العقاري بنجاح 🎉")
                    }
                }
            )
        }

        // Sheet: التعليقات
        if (activeCommentsPost != null) {
            val commentsFlow = onGetComments(activeCommentsPost!!.id)
            val commentsList by commentsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

            CommentsBottomSheet(
                post = activeCommentsPost!!,
                comments = commentsList,
                currentUserName = currentUserName,
                currentUserRole = currentUserRole,
                onDismiss = { activeCommentsPost = null },
                onAddComment = { text ->
                    onAddComment(activeCommentsPost!!.id, currentUserName, currentUserRole, text)
                }
            )
        }

        // Sheet: المساعد العقاري الذكي AI داخل المجتمع
        if (showAiAssistantSheet) {
            CommunityAiAssistantBottomSheet(
                allProperties = allProperties,
                onDismiss = { showAiAssistantSheet = false },
                onPropertyClick = { prop ->
                    showAiAssistantSheet = false
                    onPropertyClick(prop)
                },
                onPostWantedRequest = { prefilledText ->
                    showAiAssistantSheet = false
                    initialCreatePostType = "🔎 مطلوب عقار"
                    initialCreatePostContent = prefilledText
                    showCreatePostSheet = true
                }
            )
        }

        // Dialog: الإبلاغ عن منشور
        if (activeReportPost != null) {
            ReportPostDialog(
                post = activeReportPost!!,
                onDismiss = { activeReportPost = null },
                onSubmitReport = { reason ->
                    activeReportPost = null
                    scope.launch {
                        snackbarHostState.showSnackbar("شكراً لك، تم استلام البلاغ وسيتم مراجعته من الإدارة فوراً.")
                    }
                }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Top Bar
// -------------------------------------------------------------------------------------------------
@Composable
private fun CommunityTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onBack: () -> Unit,
    onOpenAiAssistant: () -> Unit
) {
    Surface(
        color = Color(0xFF0F1E36),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "المجتمع العقاري الذكي",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DesoukGold)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "دسوق",
                                    color = DesoukNavyDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = "شارك • اسأل • استفيد مع أهالي ومكاتب دسوق",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }

                // زر المساعد الذكي الصغير بالأعلى
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DesoukGold.copy(alpha = 0.2f))
                        .clickable { onOpenAiAssistant() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI",
                            tint = DesoukGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مساعد AI",
                            color = DesoukGold,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // شريط البحث داخل منشورات المجتمع
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("community_search_input"),
                placeholder = {
                    Text(
                        text = "ابحث في منشورات المجتمع (شقة، أرض، شارع الجيش...)",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = DesoukGold,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "مسح",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF162A4A),
                    unfocusedContainerColor = Color(0xFF162A4A),
                    focusedBorderColor = DesoukGold,
                    unfocusedBorderColor = Color(0xFF233E68),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// بطاقة المساعد العقاري الذكي AI البارزة (المطلب 4)
// -------------------------------------------------------------------------------------------------
@Composable
private fun CommunityAiHeroCard(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clickable { onClick() }
            .testTag("community_ai_assistant_btn"),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            Color(0xFF0D254C),
                            Color(0xFF1E3A8A),
                            Color(0xFF172554)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DesoukGold.copy(alpha = 0.25f))
                            .border(1.5.dp, DesoukGold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Assistant",
                            tint = DesoukGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🤖 المساعد العقاري الذكي",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "فعّال ومباشر",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "اسأل بالعامية: «عاوز شقة 3 غرف إيجار لحد 7000 ج» للبحث الفوري",
                            color = Color(0xFFCBD5E1),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DesoukGold)
                        .padding(horizontal = 10.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = "ابـدأ الآن ←",
                        color = DesoukNavyDark,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// مربع دعوة كتابة منشور جديد (Create Post Prompt)
// -------------------------------------------------------------------------------------------------
@Composable
private fun CreatePostPromptCard(
    currentUserName: String,
    onOpenCreate: (String?) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenCreate(null) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(DesoukNavyDark),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUserName.take(1).ifBlank { "م" },
                        color = DesoukGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "شارك عقاراً، اسأل سؤالاً، أو اطلب عقار في دسوق...",
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // أزرار وصول سريع لأنواع المنشورات الرئيسية
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                QuickPostTypeButton(
                    label = "🏠 عقار للبيع",
                    onClick = { onOpenCreate("🏠 عقار للبيع") }
                )
                QuickPostTypeButton(
                    label = "🔑 عقار للإيجار",
                    onClick = { onOpenCreate("🔑 عقار للإيجار") }
                )
                QuickPostTypeButton(
                    label = "🔎 مطلوب عقار",
                    onClick = { onOpenCreate("🔎 مطلوب عقار") }
                )
                QuickPostTypeButton(
                    label = "💬 سؤال",
                    onClick = { onOpenCreate("💬 سؤال عقاري") }
                )
            }
        }
    }
}

@Composable
private fun QuickPostTypeButton(
    label: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .bounceClick(
                scaleDown = 0.92f,
                rippleColor = DesoukGold.copy(alpha = 0.25f),
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = DesoukNavyDark
        )
    }
}

// -------------------------------------------------------------------------------------------------
// شريط الأقسام الـ 5 الرئيسية للمجتمع
// -------------------------------------------------------------------------------------------------
@Composable
private fun CommunitySectionTabsRow(
    selectedSection: CommunitySection,
    onSelectSection: (CommunitySection) -> Unit
) {
    Surface(
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(CommunitySection.values()) { section ->
                val isSelected = selectedSection == section
                val icon = when (section) {
                    CommunitySection.POSTS -> Icons.Default.Feed
                    CommunitySection.QUESTION -> Icons.Default.HelpOutline
                    CommunitySection.DISCUSSIONS -> Icons.Default.Forum
                    CommunitySection.OFFERS -> Icons.Default.LocalOffer
                    CommunitySection.EXPERT -> Icons.Default.SupportAgent
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) DesoukNavyDark else Color(0xFFF1F5F9))
                        .border(
                            1.dp,
                            if (isSelected) DesoukGold else Color(0xFFE2E8F0),
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { onSelectSection(section) }
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = icon,
                            contentDescription = section.title,
                            tint = if (isSelected) DesoukGold else Color(0xFF64748B),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = section.title,
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else Color(0xFF334155)
                        )
                        if (section.badge != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) DesoukGold else Color(0xFFE2E8F0))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = section.badge,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) DesoukNavyDark else Color(0xFF475569)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// شريط التصنيفات الرئيسية الـ 6 المطلوبة
// -------------------------------------------------------------------------------------------------
@Composable
private fun CommunityCategoryChipsRow(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(COMMUNITY_MAIN_CATEGORIES) { category ->
            val isSelected = selectedCategory == category
            FilterChipItem(
                label = category,
                isSelected = isSelected,
                onClick = { onSelectCategory(category) }
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// بطاقة استطلاع الأسبوع التفاعلية (سؤال المجتمع)
// -------------------------------------------------------------------------------------------------
@Composable
private fun CommunityWeeklyPollCard(
    onVoteSubmitted: (String) -> Unit
) {
    var selectedOptionIndex by remember { mutableStateOf<Int?>(null) }
    val options = listOf(
        "شارع الجيش (قلب المدينة والنشاط التجاري)" to 44,
        "دحروج ومنطقة المستشفى العام" to 28,
        "طريق الكورنيش والنيل (إطلالة وهدوء)" to 19,
        "حي الصفا / الميدان الإبراهيمي" to 9
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "📊 استطلاع الأسبوع",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "1,240 صوت",
                        fontSize = 10.5.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "ما هي المنطقة الأفضل للاستثمار السكني وشراء شقة في دسوق لعام 2026؟",
                fontSize = 13.5.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark,
                lineHeight = 19.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            options.forEachIndexed { index, (optionText, percent) ->
                val isChosen = selectedOptionIndex == index
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isChosen) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                        .border(
                            1.dp,
                            if (isChosen) Color(0xFF3B82F6) else Color(0xFFE2E8F0),
                            RoundedCornerShape(10.dp)
                        )
                        .clickable {
                            selectedOptionIndex = index
                            onVoteSubmitted(optionText)
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = optionText,
                            fontSize = 11.5.sp,
                            fontWeight = if (isChosen) FontWeight.Bold else FontWeight.Normal,
                            color = if (isChosen) Color(0xFF1E3A8A) else Color(0xFF1E293B)
                        )
                        Text(
                            text = "$percent%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChosen) Color(0xFF2563EB) else Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { percent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = if (isChosen) DesoukGold else Color(0xFF3B82F6),
                        trackColor = Color(0xFFE2E8F0),
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// خانة طرح سؤال عقاري سريع
// -------------------------------------------------------------------------------------------------
@Composable
private fun AskQuestionPromptCard(
    onAskClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "عندك استفسار عقاري محيرك؟",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "اطرح سؤالك وسيجيبك أهالي دسوق والخبراء المعتمدون فوراً",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.5.sp
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = onAskClick,
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "اطرح سؤالك",
                    color = DesoukNavyDark,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// ترويسة المناقشات الساخنة
// -------------------------------------------------------------------------------------------------
@Composable
private fun CommunityTrendingDiscussionsHeader(
    onStartDiscussion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF081B38))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = "مناقشات",
                            tint = DesoukGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "أحدث المناقشات الساخنة في دسوق",
                            color = Color.White,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFDC2626))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "مباشر",
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "شارك برأيك في نقاشات أسعار السوق، عقود البيع، والمقارنات بين أحياء دسوق.",
                    color = Color(0xFFCBD5E1),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onStartDiscussion,
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "+ ابدأ مناقشة جديدة في المجتمع",
                        color = DesoukNavyDark,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// بانر عروض الأعضاء المباشرة
// -------------------------------------------------------------------------------------------------
@Composable
private fun MemberOffersBanner(
    onAddOffer: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A2246))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer,
                        contentDescription = "عروض",
                        tint = DesoukGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "عروض عقارية حصرية من الملاك",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "عقارات معروضة مباشرة من أهالي دسوق بدون عمولات وسيط",
                    color = Color(0xFF94A3B8),
                    fontSize = 10.5.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onAddOffer,
                colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = "+ أضف عقارك",
                    color = DesoukNavyDark,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// قسم اسأل خبير عقاري
// -------------------------------------------------------------------------------------------------
@Composable
private fun AskRealEstateExpertSection(
    onAskConsultation: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1E36))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(DesoukGold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "خبير",
                                tint = DesoukNavyDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "اسأل خبير عقاري معتمد بدسوق",
                                color = Color.White,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "استشارات هندسية، قانونية، وتثمين أسعار مجاناً",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val experts = listOf(
                    Triple("م. طارق المهدي", "مهندس استشاري ومعاين رخص عقارات", "خبرة 18 سنة"),
                    Triple("أ. مصطفى الشناوي", "مستشار عقود وشهر عقاري", "خبرة 14 سنة"),
                    Triple("الحاج إبراهيم الدسوقي", "خبير تثمين أراضي ومحلات تجارية", "خبرة 25 سنة")
                )

                experts.forEach { (name, specialty, exp) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFF1E293B))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF334155)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = name.take(1),
                                    color = DesoukGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = name,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "معتمد",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = "$specialty • $exp",
                                    color = DesoukGold,
                                    fontSize = 9.5.sp
                                )
                            }
                        }

                        Button(
                            onClick = { onAskConsultation(name) },
                            colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "استشارة",
                                color = DesoukNavyDark,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// شريط تصنيف المنشورات الـ 9
// -------------------------------------------------------------------------------------------------
@Composable
private fun CommunityFilterChipsRow(
    selectedType: String?,
    onSelectType: (String?) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChipItem(
                label = "🌟 الكل",
                isSelected = selectedType == null,
                onClick = { onSelectType(null) }
            )
        }
        items(COMMUNITY_POST_TYPES) { type ->
            FilterChipItem(
                label = type,
                isSelected = selectedType == type,
                onClick = {
                    onSelectType(if (selectedType == type) null else type)
                }
            )
        }
    }
}

@Composable
private fun FilterChipItem(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) DesoukNavyDark else Color.White
    val textCol = if (isSelected) DesoukGold else Color(0xFF475569)
    val borderCol = if (isSelected) DesoukGold else Color(0xFFE2E8F0)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .border(1.dp, borderCol, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textCol
        )
    }
}

// -------------------------------------------------------------------------------------------------
// بطاقة المنشور الواحدة (Community Post Card)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CommunityPostCard(
    post: CommunityPostEntity,
    allProperties: List<PropertyEntity>,
    onPropertyClick: (PropertyEntity) -> Unit,
    onLikeToggle: () -> Unit,
    onSaveToggle: () -> Unit,
    onFollowToggle: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onReportClick: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    // المطابقة الذكية للطلبات (المطلب 8)
    val smartMatches = remember(post.postType, post.content, allProperties) {
        if (post.postType == "🔎 مطلوب عقار") {
            CommunityAiMatcher.findMatchingPropertiesForRequest(post.content, allProperties)
        } else {
            emptyList()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Author Avatar, Name, Role, Time, Follow, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(DesoukNavyDark, Color(0xFF1E3A8A))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.authorName.take(1),
                            color = DesoukGold,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = post.authorName,
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = DesoukNavyDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // شارة الدور
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        when (post.authorRole) {
                                            "مكتب عقاري" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                                            "وسيط معتمد" -> DesoukGold.copy(alpha = 0.2f)
                                            "خبير عقاري" -> Color(0xFF10B981).copy(alpha = 0.15f)
                                            else -> Color(0xFF64748B).copy(alpha = 0.12f)
                                        }
                                    )
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = post.authorRole,
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (post.authorRole) {
                                        "مكتب عقاري" -> Color(0xFF0369A1)
                                        "وسيط معتمد" -> Color(0xFF9A7B0C)
                                        "خبير عقاري" -> Color(0xFF047857)
                                        else -> Color(0xFF475569)
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        val timeAgo = remember(post.timestamp) {
                            formatTimeAgo(post.timestamp)
                        }
                        Text(
                            text = timeAgo,
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                // زر متابعة الناشر وزر القائمة
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (post.isFollowed) Color(0xFFE2E8F0) else DesoukGold.copy(alpha = 0.18f))
                            .clickable { onFollowToggle() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (post.isFollowed) "✓ متابَع" else "+ متابعة",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (post.isFollowed) Color(0xFF475569) else Color(0xFF9A7B0C)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "خيارات",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "إبلاغ",
                                            tint = Color(0xFFDC2626),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "الإبلاغ عن المنشور", fontSize = 12.sp, color = Color(0xFFDC2626))
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onReportClick()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // شارة نوع المنشور الـ 9
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = post.postType,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // نص المنشور
            Text(
                text = post.content,
                fontSize = 13.sp,
                color = Color(0xFF1E293B),
                lineHeight = 19.sp,
                textAlign = TextAlign.Start
            )

            // بطاقة البيانات المنظمة تلقائياً بالذكاء الاصطناعي (المطلب 7)
            if (post.extractedType != null || post.extractedPrice != null || post.extractedLocation != null) {
                Spacer(modifier = Modifier.height(8.dp))
                StructuredPostAiCard(post)
            }

            // مؤشر وجود فيديو توضيحي (المطلب 2)
            if (post.hasVideo) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF2F2))
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = "فيديو",
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = post.videoLabel ?: "يتضمن فيديو ومعاينة مصورة للعقار 📹",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF991B1B)
                    )
                }
            }

            // الصور المرفقة
            val imageList = remember(post.imagesJson) {
                post.imagesJson.split(",").filter { it.isNotBlank() }
            }
            if (imageList.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                PostImagesGrid(imageList)
            }

            // المطابقة الذكية بين الطلبات والعقارات (المطلب 8)
            if (smartMatches.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                SmartPropertyMatchesSection(
                    matches = smartMatches,
                    onPropertyClick = onPropertyClick
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(6.dp))

            // Action Bar: إعجاب، تعليق، مشاركة، حفظ
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // إعجاب
                Row(
                    modifier = Modifier
                        .bounceClick(
                            scaleDown = 0.86f,
                            rippleColor = Color(0xFFDC2626).copy(alpha = 0.25f),
                            onClick = onLikeToggle
                        )
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (post.isLikedByMe) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "إعجاب",
                        tint = if (post.isLikedByMe) Color(0xFFDC2626) else Color(0xFF64748B),
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (post.likesCount > 0) "${post.likesCount}" else "إعجاب",
                        fontSize = 12.sp,
                        color = if (post.isLikedByMe) Color(0xFFDC2626) else Color(0xFF64748B),
                        fontWeight = if (post.isLikedByMe) FontWeight.Bold else FontWeight.Normal
                    )
                }

                // تعليق
                Row(
                    modifier = Modifier
                        .bounceClick(
                            scaleDown = 0.88f,
                            rippleColor = DesoukNavyDark.copy(alpha = 0.15f),
                            onClick = onCommentClick
                        )
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Comment,
                        contentDescription = "تعليق",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (post.commentsCount > 0) "${post.commentsCount}" else "تعليق",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // مشاركة
                Row(
                    modifier = Modifier
                        .bounceClick(
                            scaleDown = 0.88f,
                            rippleColor = DesoukNavyDark.copy(alpha = 0.15f),
                            onClick = onShareClick
                        )
                        .padding(vertical = 4.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "مشاركة",
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مشاركة",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }

                // حفظ المنشور
                IconButton(
                    onClick = onSaveToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (post.isSavedByMe) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "حفظ المنشور",
                        tint = if (post.isSavedByMe) DesoukGold else Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// بطاقة استخراج البيانات الذكية AI (المطلب 7)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StructuredPostAiCard(post: CommunityPostEntity) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = DesoukGold,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "بيانات مستخرجة بالذكاء الاصطناعي:",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (!post.extractedType.isNullOrBlank()) {
                    AiChip(label = "النوع", value = post.extractedType)
                }
                if (!post.extractedDeal.isNullOrBlank()) {
                    AiChip(label = "الحالة", value = post.extractedDeal)
                }
                if (!post.extractedLocation.isNullOrBlank()) {
                    AiChip(label = "الموقع", value = post.extractedLocation)
                }
                if (!post.extractedRooms.isNullOrBlank()) {
                    AiChip(label = "الغرف", value = post.extractedRooms)
                }
                if (!post.extractedFloor.isNullOrBlank()) {
                    AiChip(label = "الدور", value = post.extractedFloor)
                }
                if (!post.extractedPrice.isNullOrBlank()) {
                    AiChip(label = "السعر", value = post.extractedPrice, isHighlight = true)
                }
                if (!post.extractedArea.isNullOrBlank()) {
                    AiChip(label = "المساحة", value = post.extractedArea)
                }
            }
        }
    }
}

@Composable
private fun AiChip(label: String, value: String, isHighlight: Boolean = false) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isHighlight) DesoukGold.copy(alpha = 0.2f) else Color.White)
            .border(0.8.dp, if (isHighlight) DesoukGold else Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label: $value",
            fontSize = 10.sp,
            fontWeight = if (isHighlight) FontWeight.Bold else FontWeight.Medium,
            color = if (isHighlight) Color(0xFF92400E) else Color(0xFF334155)
        )
    }
}

// -------------------------------------------------------------------------------------------------
// المطابقة الذكية بين الطلبات والعقارات (المطلب 8)
// -------------------------------------------------------------------------------------------------
@Composable
private fun SmartPropertyMatchesSection(
    matches: List<PropertyEntity>,
    onPropertyClick: (PropertyEntity) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEFF6FF))
            .border(1.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
            .padding(10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "مطابقة",
                    tint = Color(0xFF1D4ED8),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = "💡 عقارات مقترحة مطابقة لطلبك في التطبيق:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E40AF)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            matches.forEach { prop ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .bounceClick(
                            scaleDown = 0.96f,
                            rippleColor = DesoukGold.copy(alpha = 0.25f),
                            onClick = { onPropertyClick(prop) }
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prop.title,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${prop.district} • ${prop.price} ${prop.priceUnit}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(DesoukGold)
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "معاينة",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// عرض شبكة الصور للمنشور
// -------------------------------------------------------------------------------------------------
@Composable
private fun PostImagesGrid(images: List<String>) {
    val context = LocalContext.current
    if (images.size == 1) {
        val imgName = images[0]
        val resId = remember(imgName) { resolveDrawableId(context, imgName) }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            if (resId != 0) {
                AsyncImage(
                    model = resId,
                    contentDescription = "صورة المنشور",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            images.take(3).forEach { imgName ->
                val resId = remember(imgName) { resolveDrawableId(context, imgName) }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                ) {
                    if (resId != 0) {
                        AsyncImage(
                            model = resId,
                            contentDescription = "صورة",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Sheet: إنشاء منشور جديد + تنظيم بالذكاء الاصطناعي (المطلب 2 و 7)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatePostBottomSheet(
    initialType: String?,
    initialContent: String,
    currentUserName: String,
    currentUserRole: String,
    onDismiss: () -> Unit,
    onPublish: (
        type: String,
        content: String,
        imagesJson: String,
        hasVideo: Boolean,
        videoLabel: String?,
        extracted: ExtractedPostData
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedType by remember { mutableStateOf(initialType ?: COMMUNITY_POST_TYPES[0]) }
    var contentText by remember { mutableStateOf(initialContent) }
    var hasVideo by remember { mutableStateOf(false) }
    var attachedImages by remember { mutableStateOf<List<String>>(emptyList()) }

    // بيانات مستخرجة بالذكاء الاصطناعي
    var extractedData by remember { mutableStateOf(ExtractedPostData()) }
    var showAiReviewBanner by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .imePadding()
        ) {
            // العنوان وزر الإغلاق
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "إنشاء منشور في المجتمع العقاري",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // اختيار نوع المنشور من الـ 9
            Text(
                text = "اختر نوع المنشور:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF475569)
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(COMMUNITY_POST_TYPES) { type ->
                    val isSel = selectedType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSel) DesoukNavyDark else Color(0xFFF1F5F9))
                            .clickable { selectedType = type }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = type,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) DesoukGold else Color(0xFF334155)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // مربع نص المنشور
            OutlinedTextField(
                value = contentText,
                onValueChange = { contentText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .testTag("create_post_content_input"),
                placeholder = {
                    Text(
                        text = "اكتب تفاصيل عقارك أو سؤالك هنا بالتفصيل (مثل: شقة للبيع في شارع الجيش 3 غرف الدور الرابع والسعر مليون ونصف...)",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DesoukGold,
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // زر التنظيم الذكي بالذكاء الاصطناعي (المطلب 7)
            Button(
                onClick = {
                    extractedData = CommunityAiMatcher.extractStructuredData(contentText)
                    showAiReviewBanner = true
                },
                enabled = contentText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E3A8A)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = DesoukGold,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "تنظيم بيانات المنشور بالذكاء الاصطناعي ✨",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // عرض البيانات المستخرجة للمراجعة قبل النشر (المطلب 7)
            if (showAiReviewBanner && extractedData.propertyType.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFFEF9C3))
                        .border(1.dp, Color(0xFFFACC15), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✓ استخراج ذكي جاهز للمراجعة قبل النشر:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF854D0E)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "• نوع العقار: ${extractedData.propertyType}\n" +
                                    "• الحالة: ${extractedData.dealType}\n" +
                                    "• الموقع: ${extractedData.location}\n" +
                                    (if (extractedData.rooms.isNotBlank()) "• الغرف: ${extractedData.rooms}\n" else "") +
                                    (if (extractedData.floor.isNotBlank()) "• الدور: ${extractedData.floor}\n" else "") +
                                    (if (extractedData.price.isNotBlank()) "• السعر: ${extractedData.price}" else ""),
                            fontSize = 11.sp,
                            color = Color(0xFF713F12),
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // إضافة وسائط (صور / فيديو)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // زر إضافة صور نموذجية
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF1F5F9))
                            .clickable {
                                attachedImages = if (attachedImages.isEmpty()) {
                                    listOf("img_cat_3d_apartments_1790767759152")
                                } else {
                                    emptyList()
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = "صور",
                                tint = DesoukNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (attachedImages.isEmpty()) "إضافة صورة" else "✓ صورة مرفقة",
                                fontSize = 11.sp,
                                color = DesoukNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // زر تفعيل علامة الفيديو
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (hasVideo) Color(0xFFFEE2E2) else Color(0xFFF1F5F9))
                            .clickable { hasVideo = !hasVideo }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "فيديو",
                                tint = if (hasVideo) Color(0xFFDC2626) else DesoukNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (hasVideo) "✓ فيديو مفعّل" else "إضافة فيديو",
                                fontSize = 11.sp,
                                color = if (hasVideo) Color(0xFFDC2626) else DesoukNavyDark,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // زر النشر
                Button(
                    onClick = {
                        val finalExtracted = if (extractedData.propertyType.isBlank()) {
                            CommunityAiMatcher.extractStructuredData(contentText)
                        } else {
                            extractedData
                        }
                        onPublish(
                            selectedType,
                            contentText,
                            attachedImages.joinToString(","),
                            hasVideo,
                            if (hasVideo) "فيديو توضيحي للعقار" else null,
                            finalExtracted
                        )
                    },
                    enabled = contentText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = "نشر الآن",
                        color = DesoukNavyDark,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Sheet: التعليقات (Comments Bottom Sheet)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommentsBottomSheet(
    post: CommunityPostEntity,
    comments: List<CommunityCommentEntity>,
    currentUserName: String,
    currentUserRole: String,
    onDismiss: () -> Unit,
    onAddComment: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentInput by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 20.dp)
                .imePadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "التعليقات (${comments.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = DesoukNavyDark
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (comments.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "لا توجد تعليقات حتى الآن. كن أول من يعلق!",
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(comments, key = { it.id }) { comment ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(DesoukNavyDark),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = comment.authorName.take(1),
                                    color = DesoukGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = comment.authorName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DesoukNavyDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = formatTimeAgo(comment.timestamp),
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = comment.content,
                                    fontSize = 12.sp,
                                    color = Color(0xFF334155),
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // حقل إدخال التعليق
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentInput,
                    onValueChange = { commentInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    placeholder = {
                        Text(text = "اكتب تعليقك هنا...", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DesoukGold,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (commentInput.isNotBlank()) {
                            onAddComment(commentInput.trim())
                            commentInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DesoukNavyDark)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "إرسال",
                        tint = DesoukGold,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Sheet: المساعد العقاري الذكي AI داخل المجتمع (المطالب 4، 5، 6)
// -------------------------------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CommunityAiAssistantBottomSheet(
    allProperties: List<PropertyEntity>,
    onDismiss: () -> Unit,
    onPropertyClick: (PropertyEntity) -> Unit,
    onPostWantedRequest: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var userInput by remember { mutableStateOf("") }
    var currentAiState by remember { mutableStateOf<AiQueryState?>(null) }
    var conversationHistory by remember { mutableStateOf<List<Pair<String, Boolean>>>(emptyList()) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color(0xFF0F1E36)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .imePadding()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(DesoukGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI",
                            tint = DesoukNavyDark,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "المساعد العقاري الذكي لمدينة دسوق",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "يفهم اللهجة المصرية ويبحث في عقارات التطبيق الحقيقية",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إغلاق",
                        tint = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // أمثلة سريعة يمكن النقر عليها
            Text(
                text = "جرب استفسارات سريعة:",
                fontSize = 11.sp,
                color = DesoukGold
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                val samples = listOf(
                    "عاوز شقة 3 غرف في دسوق إيجار لحد 7000 جنيه",
                    "عاوز محل للبيع في دسوق بأقل من مليون",
                    "محتاج أرض في دسوق",
                    "شقة تمليك في شارع الجيش"
                )
                items(samples) { sample ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1E293B))
                            .clickable {
                                userInput = sample
                                currentAiState = CommunityAiMatcher.processAiAssistantQuery(sample, allProperties)
                            }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = sample,
                            fontSize = 10.5.sp,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // نتيجة المعالجة
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF091426))
                    .padding(12.dp)
            ) {
                if (currentAiState == null) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "مرحباً بك! اكتب ما تبحث عنه بالعامية المصرية وسأقوم بفحص قاعدة البيانات وعرض العقارات الحقيقية المطابقة فوراً.",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                } else {
                    when (val state = currentAiState!!) {
                        // 1. حالة النجاح مع العقارات المطابقة
                        is AiQueryState.Success -> {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                item {
                                    Text(
                                        text = state.reply,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }
                                items(state.matchingProperties) { prop ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFF1E293B))
                                            .clickable { onPropertyClick(prop) }
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = prop.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "${prop.district} • ${prop.category} (${prop.type}) • ${prop.price} ${prop.priceUnit}",
                                                fontSize = 10.5.sp,
                                                color = DesoukGold
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(DesoukGold)
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "معاينة العقار",
                                                color = DesoukNavyDark,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 2. حالة الأسئلة التوضيحية (المطلب 5)
                        is AiQueryState.ClarificationNeeded -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = state.question,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.suggestionChips.forEach { chip ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(DesoukGold)
                                                .clickable {
                                                    val newQuery = "$userInput $chip"
                                                    userInput = newQuery
                                                    currentAiState = CommunityAiMatcher.processAiAssistantQuery(newQuery, allProperties)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = chip,
                                                color = DesoukNavyDark,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // 3. حالة عدم وجود نتائج مع زر "انشر طلب عقاري" (المطلب 6 الصريح)
                        is AiQueryState.NoMatch -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.message,
                                    color = Color(0xFFFCA5A5),
                                    fontSize = 12.5.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 18.sp
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = { onPostWantedRequest(state.prefilledContent) },
                                    colors = ButtonDefaults.buttonColors(containerColor = DesoukGold),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = "📢 انشر طلب عقاري في المجتمع",
                                        color = DesoukNavyDark,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // حقل كتابة الطلب
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { userInput = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("ai_assistant_query_input"),
                    placeholder = {
                        Text(
                            text = "اكتب طلبك: «عاوز شقة إيجار في دسوق»...",
                            fontSize = 11.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    shape = RoundedCornerShape(20.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF162A4A),
                        unfocusedContainerColor = Color(0xFF162A4A),
                        focusedBorderColor = DesoukGold,
                        unfocusedBorderColor = Color(0xFF233E68),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (userInput.isNotBlank()) {
                            currentAiState = CommunityAiMatcher.processAiAssistantQuery(userInput, allProperties)
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(DesoukGold)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "بحث",
                        tint = DesoukNavyDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Dialog: الإبلاغ عن منشور
// -------------------------------------------------------------------------------------------------
@Composable
private fun ReportPostDialog(
    post: CommunityPostEntity,
    onDismiss: () -> Unit,
    onSubmitReport: (String) -> Unit
) {
    var selectedReason by remember { mutableStateOf("محتوى غير لائق أو مخالف") }
    val reasons = listOf(
        "محتوى غير لائق أو مخالف",
        "معلومات مضللة أو سعر غير حقيقي",
        "إعلان مكرر (سبام)",
        "انتحال صفة مكتب أو وسيط آخر",
        "احتيال أو طلب دفع مسبق مشبوه"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "الإبلاغ عن المنشور",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "يرجى تحديد سبب الإبلاغ لمراجعته من إدارة التطبيق:",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )
                Spacer(modifier = Modifier.height(8.dp))
                reasons.forEach { reason ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedReason = reason }
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            colors = RadioButtonDefaults.colors(selectedColor = DesoukGold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = reason, fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmitReport(selectedReason) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
            ) {
                Text(text = "إرسال البلاغ", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "إلغاء", color = Color(0xFF64748B))
            }
        }
    )
}

// -------------------------------------------------------------------------------------------------
// عرض فارغ (Empty View)
// -------------------------------------------------------------------------------------------------
@Composable
private fun EmptyCommunityView(
    isFiltered: Boolean,
    onClearFilters: () -> Unit,
    onCreatePost: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Groups,
                contentDescription = null,
                tint = DesoukGold,
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (isFiltered) "لا توجد منشورات تطابق البحث أو التصنيف المختار" else "لا توجد منشورات حتى الآن في المجتمع",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = DesoukNavyDark,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isFiltered) "يمكنك إلغاء التصفية للرجوع لجميع المنشورات" else "كن أول من يشارك عقاراً أو يطرح سؤالاً في مدينة دسوق",
                fontSize = 12.sp,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
            if (isFiltered) {
                Button(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukNavyDark)
                ) {
                    Text(text = "إلغاء التصفية وعرض الكل", color = Color.White)
                }
            } else {
                Button(
                    onClick = onCreatePost,
                    colors = ButtonDefaults.buttonColors(containerColor = DesoukGold)
                ) {
                    Text(text = "انشر أول مشاركة الآن", color = DesoukNavyDark, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// Helper Utilities
// -------------------------------------------------------------------------------------------------
private fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / (60 * 1000)
    val hours = diff / (60 * 60 * 1000)
    val days = diff / (24 * 60 * 60 * 1000)

    return when {
        mins < 1 -> "الآن"
        mins < 60 -> "منذ $mins دقيقة"
        hours < 24 -> "منذ $hours ساعة"
        days < 7 -> "منذ $days يوم"
        else -> SimpleDateFormat("dd MMMM", Locale.forLanguageTag("ar")).format(Date(timestamp))
    }
}

private fun resolveDrawableId(context: android.content.Context, name: String): Int {
    if (name.isBlank()) return 0
    return try {
        context.resources.getIdentifier(name, "drawable", context.packageName)
    } catch (_: Exception) {
        0
    }
}
