package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Verified
import androidx.compose.ui.graphics.Brush
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BadgeForSaleRed
import com.example.ui.theme.DesoukGold
import com.example.ui.theme.DesoukNavyDark
import com.example.ui.theme.SurfaceBackground
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

data class ChatMessage(
    val id: Long,
    val senderName: String,
    val text: String,
    val time: String,
    val isFromMe: Boolean
)

data class ChatItem(
    val id: Long,
    val agentName: String,
    val officeName: String,
    val avatarColor: Color,
    val initialLetter: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int,
    val propertyTitle: String,
    val phone: String,
    val isVerified: Boolean = true,
    val category: String = "مكتب"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatsScreen(
    onAiAssistantClick: () -> Unit = {},
    onUserSentMessage: (agentName: String, propertyTitle: String, text: String, chatId: Long) -> Unit = { _, _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("الكل") }
    var activeChat by remember { mutableStateOf<ChatItem?>(null) }

    val chatList = remember {
        listOf(
            ChatItem(
                id = 1L,
                agentName = "أ/ محمود الشناوي",
                officeName = "مكتب النيل للعقارات",
                avatarColor = Color(0xFF1E88E5),
                initialLetter = "ن",
                lastMessage = "السلام عليكم، شقة شارع الجيش 140م متاحة للمعاينة اليوم الساعة 5 مساءً",
                time = "منذ 5 د",
                unreadCount = 2,
                propertyTitle = "شقة فاخرة بشارع الجيش",
                phone = "01001234567",
                category = "مكتب"
            ),
            ChatItem(
                id = 2L,
                agentName = "م/ خالد الدسوقي",
                officeName = "عقارات دسوق الحديثة",
                avatarColor = Color(0xFF00897B),
                initialLetter = "د",
                lastMessage = "تم إرسال تفاصيل المحل التجاري وعقد الإيجار النموذجي",
                time = "12:30 م",
                unreadCount = 1,
                propertyTitle = "محل تجاري بالميدان الإبراهيمي",
                phone = "01123456789",
                category = "مكتب"
            ),
            ChatItem(
                id = 3L,
                agentName = "الحاج مصطفى الصياد",
                officeName = "مالك عقار مباشر",
                avatarColor = Color(0xFFD84315),
                initialLetter = "م",
                lastMessage = "السعر 1,450,000 كاش وقابل للتفاوض البسيط عند الجدية إن شاء الله",
                time = "أمس",
                unreadCount = 0,
                propertyTitle = "منزل طابقين بحي الزهور",
                phone = "01234567890",
                category = "مالك"
            ),
            ChatItem(
                id = 4L,
                agentName = "أ/ سمير الغرياني",
                officeName = "مكتب الصفا للتسويق العقاري",
                avatarColor = Color(0xFF5E35B1),
                initialLetter = "ص",
                lastMessage = "متاح لدينا قطعة أرض 200م مسجلة شهر عقاري بكامل المرافق",
                time = "منذ يومين",
                unreadCount = 0,
                propertyTitle = "أرض سكنية بحي الكشلة",
                phone = "01098765432",
                category = "مكتب"
            ),
            ChatItem(
                id = 5L,
                agentName = "م/ أحمد كمال",
                officeName = "مجموعة الدلتا للاستثمار العقاري",
                avatarColor = Color(0xFFF57C00),
                initialLetter = "ك",
                lastMessage = "نرحب باستفسارك عن المكاتب الإدارية الجديدة في برج النيل",
                time = "منذ 3 أيام",
                unreadCount = 0,
                propertyTitle = "مكتب إداري ببرج النيل",
                phone = "01555555555",
                category = "مكتب"
            )
        )
    }

    val filteredChats = chatList.filter { chat ->
        val matchesQuery = searchQuery.isBlank() ||
                chat.agentName.contains(searchQuery, ignoreCase = true) ||
                chat.officeName.contains(searchQuery, ignoreCase = true) ||
                chat.propertyTitle.contains(searchQuery, ignoreCase = true) ||
                chat.lastMessage.contains(searchQuery, ignoreCase = true)

        val matchesFilter = when (selectedFilter) {
            "غير مقروءة" -> chat.unreadCount > 0
            "المكاتب العقارية" -> chat.category == "مكتب"
            "ملاك العقارات" -> chat.category == "مالك"
            else -> true
        }

        matchesQuery && matchesFilter
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBackground)
    ) {
        // 1. ترويسة شاشة المحادثات
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DesoukNavyDark)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(DesoukGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = DesoukNavyDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "المحادثات والرسائل",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "تواصل مباشر مع أصحاب العقارات والمكاتب",
                            color = DesoukGold,
                            fontSize = 11.sp
                        )
                    }
                }

                // شارة إجمالي غير المقروءة
                BadgedBox(
                    badge = {
                        Badge(containerColor = BadgeForSaleRed, contentColor = Color.White) {
                            Text("3")
                        }
                    }
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. شريط البحث في المحادثات
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = DesoukNavyDark,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("ابحث باسم المكتب، العقار، أو الرسالة...", fontSize = 12.sp, color = TextMuted) },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_search_input")
                )
            }
        }

        // 3. فلاتر المحادثات
        val filterOptions = listOf("الكل", "غير مقروءة", "المكاتب العقارية", "ملاك العقارات")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filterOptions) { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(text = filter, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DesoukNavyDark,
                        selectedLabelColor = Color.White,
                        containerColor = Color.White,
                        labelColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (selectedFilter == filter) DesoukGold else Color(0xFFE2E8F0),
                        enabled = true,
                        selected = selectedFilter == filter
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. قائمة المحادثات
        if (filteredChats.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "لا توجد محادثات تطابق بحثك",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // المحادثة المثبتة: المساعد العقاري الذكي
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAiAssistantClick() }
                            .testTag("chat_ai_assistant_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, DesoukGold.copy(alpha = 0.7f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(DesoukNavyDark, Color(0xFF0D47A1))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SmartToy,
                                    contentDescription = "المساعد الذكي",
                                    tint = DesoukGold,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "المساعد العقاري الذكي (AI)",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DesoukNavyDark
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE8F5E9)
                                    ) {
                                        Text(
                                            text = "متصل 24/7",
                                            color = Color(0xFF2E7D32),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = "اسأل عن أسعار المتر، أفضل الشقق، والأوراق القانونية بدسوق",
                                    fontSize = 11.5.sp,
                                    color = TextMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                items(filteredChats) { chat ->
                    ConversationItemCard(
                        chat = chat,
                        onClick = { activeChat = chat },
                        onCallClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${chat.phone}"))
                            context.startActivity(intent)
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    // Interactive Chat Conversation Bottom Sheet
    if (activeChat != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { activeChat = null },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            ChatConversationView(
                chat = activeChat!!,
                onClose = { activeChat = null },
                onUserSentMessage = onUserSentMessage
            )
        }
    }
}

@Composable
fun ConversationItemCard(
    chat: ChatItem,
    onClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("chat_item_${chat.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // صورة / أيقونة الطرف الآخر
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(chat.avatarColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.initialLetter,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // المحتوى الرئيسي
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = chat.agentName,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = DesoukNavyDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (chat.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "موثق",
                                tint = Color(0xFF1E88E5),
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    Text(
                        text = chat.time,
                        fontSize = 11.sp,
                        color = if (chat.unreadCount > 0) DesoukGold else TextMuted,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                // اسم العقار المرتبط
                Text(
                    text = "بخصوص: ${chat.propertyTitle}",
                    fontSize = 11.sp,
                    color = DesoukGold,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // نص آخر رسالة
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 12.sp,
                        color = if (chat.unreadCount > 0) TextPrimary else TextMuted,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (chat.unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(BadgeForSaleRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // زر الاتصال السريع
            IconButton(
                onClick = onCallClick,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8F5E9))
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "اتصال",
                    tint = Color(0xFF2E7D32),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun ChatConversationView(
    chat: ChatItem,
    onClose: () -> Unit,
    onUserSentMessage: (agentName: String, propertyTitle: String, text: String, chatId: Long) -> Unit = { _, _, _, _ -> }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val messages = remember {
        mutableStateListOf(
            ChatMessage(1L, chat.agentName, "مرحباً بك! يسعدنا تواصلك معنا بخصوص ${chat.propertyTitle}.", "11:00 ص", false),
            ChatMessage(2L, "أنا", "أهلاً وسهلاً، أود الاستفسار عن موعد متاح لمعاينة العقار على الطبيعة.", "11:15 ص", true),
            ChatMessage(3L, chat.agentName, chat.lastMessage, chat.time, false)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(550.dp)
            .background(Color.White)
            .padding(16.dp)
    ) {
        // الترويسة العلوية للمحادثة
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(chat.avatarColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = chat.initialLetter,
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = chat.agentName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = DesoukNavyDark
                    )
                    Text(
                        text = chat.officeName,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Row {
                IconButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${chat.phone}"))
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "اتصال",
                        tint = Color(0xFF2E7D32)
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

        // قائمة الرسائل
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(messages) { msg ->
                val isMe = msg.isFromMe
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .clip(
                                RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                )
                            )
                            .background(if (isMe) DesoukNavyDark else Color(0xFFF1F5F9))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Column {
                            Text(
                                text = msg.text,
                                color = if (isMe) Color.White else TextPrimary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    text = msg.time,
                                    color = if (isMe) Color.White.copy(alpha = 0.7f) else TextMuted,
                                    fontSize = 10.sp
                                )
                                if (isMe) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = null,
                                        tint = DesoukGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // شريط كتابة الرسالة
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("اكتب رسالتك هنا...", fontSize = 12.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
                shape = RoundedCornerShape(24.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF8FAFC),
                    unfocusedContainerColor = Color(0xFFF8FAFC),
                    focusedIndicatorColor = DesoukGold,
                    unfocusedIndicatorColor = Color(0xFFE2E8F0)
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(DesoukGold)
                    .clickable {
                        if (inputText.isNotBlank()) {
                            val userText = inputText.trim()
                            messages.add(
                                ChatMessage(
                                    id = System.currentTimeMillis(),
                                    senderName = "أنا",
                                    text = userText,
                                    time = "الآن",
                                    isFromMe = true
                                )
                            )
                            inputText = ""
                            onUserSentMessage(chat.agentName, chat.propertyTitle, userText, chat.id)

                            // محاكاة وصول رد الوسيط في نافذة المحادثة بعد مهلة قصيرة
                            scope.launch {
                                delay(2500)
                                val replySnippet = when {
                                    userText.contains("سعر") || userText.contains("تفاوض") || userText.contains("بكام") ->
                                        "أهلاً بك، بالنسبة للسعر متاح تفاوض بسيط عند الجدية إن شاء الله."
                                    userText.contains("معاينة") || userText.contains("زيارة") || userText.contains("موعد") ->
                                        "تم تسجيل طلبك للمعاينة، هل يناسبك اليوم بين 5 و 7 مساءً؟"
                                    else ->
                                        "أهلاً بحضرتك، العقار متاح حالياً ويسعدنا استقبالك في المكتب أو التنسيق للمعاينة."
                                }
                                messages.add(
                                    ChatMessage(
                                        id = System.currentTimeMillis() + 1,
                                        senderName = chat.agentName,
                                        text = replySnippet,
                                        time = "الآن",
                                        isFromMe = false
                                    )
                                )
                            }
                        }
                    }
                    .testTag("chat_send_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "إرسال",
                    tint = DesoukNavyDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
