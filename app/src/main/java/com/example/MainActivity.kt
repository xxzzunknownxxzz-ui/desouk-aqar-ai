package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AuthDialog
import com.example.ui.components.OnboardingDialog
import com.example.ui.components.AdminLoginDialog
import com.example.ui.components.DesoukBottomNav
import com.example.ui.components.DesoukDrawerContent
import com.example.ui.components.DistrictPickerSheet
import com.example.ui.components.FilterDialog
import com.example.ui.components.NavTab
import com.example.ui.components.NotificationsDialog
import com.example.ui.screens.AddPropertyScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AiAssistantScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.CommunityScreen
import com.example.ui.screens.FavoritesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.PropertyDetailScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SurfaceBackground
import com.example.viewmodel.MainViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.handleDeepLink(intent?.data)
        setContent {
            MyApplicationTheme {
                // Ensure RTL layout for Arabic
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    DesoukRealEstateApp(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleDeepLink(intent.data)
    }
}

@Composable
fun DesoukRealEstateApp(
    viewModel: MainViewModel
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val allProperties by viewModel.allProperties.collectAsStateWithLifecycle()
    val filteredProperties by viewModel.filteredProperties.collectAsStateWithLifecycle()
    val favoriteProperties by viewModel.favoriteProperties.collectAsStateWithLifecycle()
    val userProperties by viewModel.userProperties.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()
    val selectedProperty by viewModel.selectedProperty.collectAsStateWithLifecycle()

    val showDistrictSheet by viewModel.showDistrictSheet.collectAsStateWithLifecycle()
    val showFilterDialog by viewModel.showFilterDialog.collectAsStateWithLifecycle()
    val showNotifications by viewModel.showNotifications.collectAsStateWithLifecycle()
    val unreadNotificationsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()

    // Admin & User Role State Collections
    val isAdminAuthenticated by viewModel.isAdminAuthenticated.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    val isFirestoreRoleVerified by viewModel.isFirestoreRoleVerified.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val showAdminLoginDialog by viewModel.showAdminLoginDialog.collectAsStateWithLifecycle()
    val adminAuthErrorMessage by viewModel.adminAuthErrorMessage.collectAsStateWithLifecycle()
    val customerRequests by viewModel.allCustomerRequests.collectAsStateWithLifecycle()
    val inquiries by viewModel.allInquiries.collectAsStateWithLifecycle()
    val users by viewModel.allUsers.collectAsStateWithLifecycle()
    val activityLogs by viewModel.allActivityLogs.collectAsStateWithLifecycle()
    val settings by viewModel.allSettings.collectAsStateWithLifecycle()
    val sources by viewModel.allSources.collectAsStateWithLifecycle()
    val importedProperties by viewModel.allImportedProperties.collectAsStateWithLifecycle()
    val reviewQueue by viewModel.reviewQueue.collectAsStateWithLifecycle()
    val importLogs by viewModel.allImportLogs.collectAsStateWithLifecycle()
    val sourcesStats by viewModel.sourcesDashboardStats.collectAsStateWithLifecycle()
    val isFirestorePreloading by viewModel.isFirestorePreloading.collectAsStateWithLifecycle()
    val firestorePreloadSource by viewModel.firestorePreloadSource.collectAsStateWithLifecycle()
    val userInterests by viewModel.userInterests.collectAsStateWithLifecycle()
    val allCommunityPosts by viewModel.allCommunityPosts.collectAsStateWithLifecycle()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Back button handling
    BackHandler(enabled = drawerState.isOpen || selectedProperty != null || currentTab != NavTab.HOME) {
        when {
            drawerState.isOpen -> scope.launch { drawerState.close() }
            selectedProperty != null -> viewModel.selectProperty(null)
            currentTab != NavTab.HOME -> viewModel.selectTab(NavTab.HOME)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DesoukDrawerContent(
                onNavigate = { tab -> viewModel.selectTab(tab) },
                onOpenDistricts = { viewModel.setShowDistrictSheet(true) },
                onOpenAdmin = { viewModel.openAdminGate() },
                onCloseDrawer = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = SurfaceBackground,
            bottomBar = {
                // Hide bottom nav only when full property details or full admin dashboard is active
                if (selectedProperty == null && currentTab != NavTab.ADMIN) {
                    DesoukBottomNav(
                        currentTab = currentTab,
                        onTabSelected = { tab -> viewModel.selectTab(tab) },
                        favoritesCount = favoriteProperties.size,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(SurfaceBackground)
            ) {
                if (selectedProperty != null) {
                    val propertyId = selectedProperty!!.id
                    val propertyReviewsFlow = remember(propertyId) { viewModel.getReviewsForProperty(propertyId) }
                    val propertyReviews by propertyReviewsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
                    val propertyAvgRatingFlow = remember(propertyId) { viewModel.getAverageRating(propertyId) }
                    val propertyAvgRating by propertyAvgRatingFlow.collectAsStateWithLifecycle(initialValue = null)

                    LaunchedEffect(propertyId) {
                        viewModel.syncPropertyReviewsFromFirestore(propertyId)
                    }

                    PropertyDetailScreen(
                        property = selectedProperty!!,
                        reviews = propertyReviews,
                        averageRating = propertyAvgRating,
                        onAddReview = { name, role, rating, comment ->
                            viewModel.addReview(selectedProperty!!.id, name, role, rating, comment)
                        },
                        onReportProperty = { id, title, reason, details ->
                            viewModel.reportProperty(id, title, reason, details)
                        },
                        onBack = { viewModel.selectProperty(null) },
                        onFavoriteToggle = { id, fav -> viewModel.toggleFavorite(id, fav) },
                        onDelete = { id -> viewModel.deleteProperty(id) },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = innerPadding.calculateBottomPadding())
                    )
                } else {
                    when (currentTab) {
                        NavTab.HOME -> {
                            HomeScreen(
                                properties = if (filterState.category != null || filterState.query.isNotBlank()) filteredProperties else allProperties,
                                filterState = filterState,
                                allProperties = allProperties,
                                onMenuClick = { scope.launch { drawerState.open() } },
                                onNotificationsClick = { viewModel.setShowNotifications(true) },
                                onDistrictClick = { viewModel.setShowDistrictSheet(true) },
                                onQueryChange = { q -> viewModel.setQuery(q) },
                                onSearchClick = { viewModel.selectTab(NavTab.SEARCH) },
                                onFilterClick = { viewModel.setShowFilterDialog(true) },
                                onCategoryClick = { cat ->
                                    viewModel.setCategory(cat)
                                    viewModel.selectTab(NavTab.SEARCH)
                                },
                                onAiAssistantClick = { query -> viewModel.openAiAssistantWithQuery(query) },
                                onExploreBannerClick = { viewModel.selectTab(NavTab.COMMUNITY) },
                                onViewAllClick = { viewModel.selectTab(NavTab.SEARCH) },
                                onPropertyClick = { prop -> viewModel.selectProperty(prop) },
                                onFavoriteToggle = { id, fav -> viewModel.toggleFavorite(id, fav) },
                                unreadNotificationsCount = unreadNotificationsCount,
                                onNeedsSpecified = { district, propType, dealType ->
                                    viewModel.checkAndNotifyMatchingProperties(district, propType, dealType)
                                },
                                isFirestorePreloading = isFirestorePreloading,
                                firestorePreloadSource = firestorePreloadSource,
                                onRefreshPreload = { viewModel.refreshFirestorePreload() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        NavTab.SEARCH -> {
                            SearchScreen(
                                properties = filteredProperties,
                                filterState = filterState,
                                onQueryChange = { q -> viewModel.setQuery(q) },
                                onSearchClick = {},
                                onFilterClick = { viewModel.setShowFilterDialog(true) },
                                onCategoryClick = { cat -> viewModel.setCategory(if (cat.isBlank()) null else cat) },
                                onPropertyClick = { prop -> viewModel.selectProperty(prop) },
                                onFavoriteToggle = { id, fav -> viewModel.toggleFavorite(id, fav) },
                                onResetFilters = { viewModel.resetFilters() },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        NavTab.ADD -> {
                            if (!userSession.isLoggedIn) {
                                LaunchedEffect(Unit) {
                                    viewModel.openLoginScreen()
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = innerPadding.calculateBottomPadding())
                                        .padding(24.dp),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    androidx.compose.material3.Card(
                                        shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                                        colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White),
                                        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 4.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        androidx.compose.foundation.layout.Column(
                                            modifier = Modifier.padding(24.dp),
                                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                        ) {
                                            androidx.compose.material3.Text("🔐", fontSize = 42.sp)
                                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(12.dp))
                                            androidx.compose.material3.Text(
                                                text = "تسجيل الدخول مطلوب لنشر العقار",
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                fontSize = 17.sp,
                                                color = com.example.ui.theme.DesoukNavyDark,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                                            androidx.compose.material3.Text(
                                                text = "قم بتسجيل الدخول باستخدام حساب Google لحفظ إعلاناتك ومزامنتها في سحابة Firestore وإدارتها بسهولة.",
                                                fontSize = 13.sp,
                                                color = com.example.ui.theme.TextSecondary,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(20.dp))
                                            androidx.compose.material3.Button(
                                                onClick = { viewModel.openLoginScreen() },
                                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.DesoukGold),
                                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                                                modifier = Modifier.fillMaxWidth().height(48.dp)
                                            ) {
                                                androidx.compose.material3.Text(
                                                    text = "تسجيل الدخول باستخدام Google",
                                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                                                    color = com.example.ui.theme.DesoukNavyDark
                                                )
                                            }
                                        }
                                    }
                                }
                            } else {
                                val propertyDraft by viewModel.propertyDraft.collectAsStateWithLifecycle()
                                AddPropertyScreen(
                                    draft = propertyDraft,
                                    onSaveDraft = { title, category, type, district, addressDetail,
                                                    priceText, areaText, roomsText, bathroomsText,
                                                    floorText, phone, whatsapp, description, selectedImage ->
                                        viewModel.savePropertyDraft(
                                            title, category, type, district, addressDetail,
                                            priceText, areaText, roomsText, bathroomsText,
                                            floorText, phone, whatsapp, description, selectedImage
                                        )
                                    },
                                    onClearDraft = { viewModel.clearPropertyDraft() },
                                    onAddSuccess = {
                                        viewModel.clearPropertyDraft()
                                        viewModel.selectTab(NavTab.HOME)
                                    },
                                    onSaveProperty = { title, category, type, location, district, price, priceUnit,
                                                       rooms, bathrooms, area, floor, phone, whatsapp, description,
                                                       imageResName, ownerName, isNegotiable, imagesJson, videoUrl,
                                                       finishing, addressDetail, onSuccess ->
                                        viewModel.addProperty(
                                            title = title,
                                            category = category,
                                            type = type,
                                            location = location,
                                            district = district,
                                            price = price,
                                            priceUnit = priceUnit,
                                            rooms = rooms,
                                            bathrooms = bathrooms,
                                            area = area,
                                            floor = floor,
                                            phone = phone,
                                            whatsapp = whatsapp,
                                            description = description,
                                            imageResName = imageResName,
                                            ownerName = ownerName,
                                            isNegotiable = isNegotiable,
                                            imagesJson = imagesJson,
                                            videoUrl = videoUrl,
                                            finishing = finishing,
                                            addressDetail = addressDetail,
                                            onSuccess = onSuccess
                                        )
                                    },
                                    currentUserName = userSession.name,
                                    currentUserPhone = userSession.phone,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = innerPadding.calculateBottomPadding())
                                )
                            }
                        }

                        NavTab.CHATS -> {
                            ChatsScreen(
                                onAiAssistantClick = { viewModel.selectTab(NavTab.AI_ASSISTANT) },
                                onUserSentMessage = { agentName, propTitle, text, chatId ->
                                    viewModel.simulateIncomingAgentReply(chatId, agentName, propTitle, text)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        NavTab.COMMUNITY -> {
                            CommunityScreen(
                                posts = allCommunityPosts,
                                allProperties = allProperties,
                                currentUserName = currentUserName,
                                currentUserRole = currentUserRole,
                                onBack = { viewModel.selectTab(NavTab.HOME) },
                                onPropertyClick = { prop -> viewModel.selectProperty(prop) },
                                onLikeToggle = { id, currentLikes, isLiked ->
                                    viewModel.toggleCommunityLike(id, currentLikes, isLiked)
                                },
                                onSaveToggle = { id, isSaved ->
                                    viewModel.toggleCommunitySave(id, isSaved)
                                },
                                onFollowToggle = { id, isFollowed ->
                                    viewModel.toggleFollowAuthor(id, isFollowed)
                                },
                                onGetComments = { id -> viewModel.getCommentsForPost(id) },
                                onAddComment = { id, name, role, text ->
                                    viewModel.addCommunityComment(id, name, role, text)
                                },
                                onCreatePost = { authorName, authorRole, authorPhone, postType, content,
                                                 imagesJson, hasVideo, videoLabel, district,
                                                 extractedType, extractedDeal, extractedLocation,
                                                 extractedRooms, extractedFloor, extractedPrice, extractedArea ->
                                    viewModel.createCommunityPost(
                                        authorName = authorName,
                                        authorRole = authorRole,
                                        authorPhone = authorPhone,
                                        postType = postType,
                                        content = content,
                                        imagesJson = imagesJson,
                                        hasVideo = hasVideo,
                                        videoLabel = videoLabel,
                                        district = district,
                                        extractedType = extractedType,
                                        extractedDeal = extractedDeal,
                                        extractedLocation = extractedLocation,
                                        extractedRooms = extractedRooms,
                                        extractedFloor = extractedFloor,
                                        extractedPrice = extractedPrice,
                                        extractedArea = extractedArea
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        NavTab.AI_ASSISTANT -> {
                            val initialQuery by viewModel.assistantInitialQuery.collectAsStateWithLifecycle()
                            AiAssistantScreen(
                                properties = allProperties,
                                onBack = {
                                    viewModel.clearAssistantInitialQuery()
                                    viewModel.selectTab(NavTab.HOME)
                                },
                                customerRequests = customerRequests,
                                favoriteProperties = favoriteProperties,
                                currentUserRole = currentUserRole,
                                onPropertyClick = { prop -> viewModel.selectProperty(prop) },
                                onFavoriteToggle = { id, fav -> viewModel.toggleFavorite(id, fav) },
                                onNavigateToSearch = { query, category ->
                                    viewModel.setCategory(category ?: "")
                                    viewModel.selectTab(NavTab.SEARCH)
                                },
                                initialQuery = initialQuery,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        NavTab.FAVORITES -> {
                            FavoritesScreen(
                                favorites = favoriteProperties,
                                onPropertyClick = { prop -> viewModel.selectProperty(prop) },
                                onFavoriteToggle = { id, fav -> viewModel.toggleFavorite(id, fav) },
                                onExploreClick = { viewModel.selectTab(NavTab.HOME) },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        NavTab.PROFILE -> {
                            ProfileScreen(
                                userSession = userSession,
                                userProperties = userProperties,
                                favoritesCount = favoriteProperties.size,
                                onOpenRoleLogin = { viewModel.openAdminGate() },
                                onOpenAdminGate = { viewModel.openAdminGate() },
                                onOpenNotifications = { viewModel.setShowNotifications(true) },
                                onOpenAuthDialog = { viewModel.openLoginScreen() },
                                onUpdateProfile = { name, phone -> viewModel.updateUserProfile(name, phone) },
                                onLogout = { viewModel.logoutUser() },
                                onDeleteAccount = { viewModel.deleteUserAccount() },
                                onNavigateToAdd = { viewModel.selectTab(NavTab.ADD) },
                                onNavigateToSearchWithCriteria = { criteria ->
                                    viewModel.setCategory(criteria.category ?: "")
                                    viewModel.selectTab(NavTab.SEARCH)
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = innerPadding.calculateBottomPadding())
                            )
                        }

                        NavTab.ADMIN -> {
                            // التحقق الصارم من أن واجهة لوحة التحكم تظهر فقط للأدمن بعد التحقق من دور 'Admin'
                            if (!isAdminAuthenticated || currentUserRole != "Admin") {
                                viewModel.selectTab(NavTab.HOME)
                            } else {
                                AdminDashboardScreen(
                                    properties = allProperties,
                                    customerRequests = customerRequests,
                                    inquiries = inquiries,
                                    users = users,
                                    activityLogs = activityLogs,
                                    settings = settings,
                                    sources = sources,
                                    importedProperties = importedProperties,
                                    reviewQueue = reviewQueue,
                                    importLogs = importLogs,
                                    sourcesStats = sourcesStats,
                                    onBackToApp = { viewModel.selectTab(NavTab.HOME) },
                                    onLogout = { viewModel.logoutAdmin() },
                                    aiService = viewModel.facebookImportAiService,
                                    onSaveAndPublishFacebookAd = { ad, isDup, onSuccess ->
                                        viewModel.adminSaveAndPublishFacebookAd(ad, isDup, onSuccess)
                                    },
                                    onSaveFacebookAdAsDraft = { ad, onSuccess ->
                                        viewModel.adminSaveFacebookAdAsDraft(ad, onSuccess)
                                    },
                                    onSaveFacebookAdAsNeedsReview = { ad, onSuccess ->
                                        viewModel.adminSaveFacebookAdAsNeedsReview(ad, onSuccess)
                                    },
                                    onViewPropertyDetail = { propId ->
                                        viewModel.selectPropertyById(propId)
                                    },
                                    onAddSource = { name, type, url, notes ->
                                        viewModel.adminAddPropertySource(name, type, url, notes)
                                    },
                                    onToggleSourceActive = { id, active ->
                                        viewModel.adminToggleSourceActive(id, active)
                                    },
                                    onDeleteSource = { id ->
                                        viewModel.adminDeleteSource(id)
                                    },
                                    onSyncSource = { source ->
                                        viewModel.adminSyncSource(source)
                                    },
                                    onImportRawPost = { rawText, sourceName, postUrl, postExternalId, onSuccess ->
                                        viewModel.adminImportRawPost(rawText, sourceName, postUrl, postExternalId) {
                                            onSuccess()
                                        }
                                    },
                                    onApproveAndPublishImported = { item, notes ->
                                        viewModel.adminApproveAndPublishImportedAd(item, notes)
                                    },
                                    onRejectImported = { item, reason ->
                                        viewModel.adminRejectImportedAd(item, reason)
                                    },
                                    onRequestReviewImported = { id, notes ->
                                        viewModel.adminRequestReviewImportedAd(id, notes)
                                    },
                                    onUpdateAndPublishImported = { item ->
                                        viewModel.adminUpdateAndPublishImportedAd(item)
                                    },
                                    onDeleteImported = { id ->
                                        viewModel.adminDeleteImportedAd(id)
                                    },
                                    onSaveProperty = { id, title, category, type, location, district, street, price, priceUnit,
                                                       rooms, bathrooms, area, floor, totalFloors, finishing, hasElevator, hasGarage,
                                                       hasMeters, features, phone, whatsapp, videoUrl, imageResName, description, status, lat, lng, onSuccess ->
                                        viewModel.adminSaveProperty(
                                            id = id,
                                            title = title,
                                            category = category,
                                            type = type,
                                            location = location,
                                            district = district,
                                            street = street,
                                            price = price,
                                            priceUnit = priceUnit,
                                            rooms = rooms,
                                            bathrooms = bathrooms,
                                            area = area,
                                            floor = floor,
                                            totalFloors = totalFloors,
                                            finishing = finishing,
                                            hasElevator = hasElevator,
                                            hasGarage = hasGarage,
                                            hasMeters = hasMeters,
                                            features = features,
                                            phone = phone,
                                            whatsapp = whatsapp,
                                            videoUrl = videoUrl,
                                            imageResName = imageResName,
                                            description = description,
                                            status = status,
                                            latitude = lat,
                                            longitude = lng,
                                            onSuccess = onSuccess
                                        )
                                    },
                                    onChangePropertyStatus = { id, title, newStatus ->
                                        viewModel.adminChangePropertyStatus(id, title, newStatus)
                                    },
                                    onArchiveProperty = { id, title ->
                                        viewModel.adminArchiveProperty(id, title)
                                    },
                                    onRestoreProperty = { id, title ->
                                        viewModel.adminRestoreProperty(id, title)
                                    },
                                    onDeleteProperty = { id, title ->
                                        viewModel.adminDeleteProperty(id, title)
                                    },
                                    onUpdateRequestStatus = { id, newStatus ->
                                        viewModel.adminUpdateRequestStatus(id, newStatus)
                                    },
                                    onArchiveRequest = { id ->
                                        viewModel.adminArchiveRequest(id)
                                    },
                                    onDeleteRequest = { id ->
                                        viewModel.adminDeleteRequest(id)
                                    },
                                    onCreateRequest = { name, phone, type, category, district, budget, area, rooms, details ->
                                        viewModel.createCustomerRequest(name, phone, type, category, district, budget, area, rooms, details)
                                    },
                                    onReplyInquiry = { id, status, notes ->
                                        viewModel.adminReplyInquiry(id, status, notes)
                                    },
                                    onDeleteInquiry = { id ->
                                        viewModel.adminDeleteInquiry(id)
                                    },
                                    onToggleUserBlock = { id, name, blocked ->
                                        viewModel.adminToggleUserBlock(id, name, blocked)
                                    },
                                    onDeleteUser = { id, name ->
                                        viewModel.adminDeleteUser(id, name)
                                    },
                                    onUpdateSetting = { key, value ->
                                        viewModel.adminUpdateSetting(key, value)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            // District Picker Sheet
            if (showDistrictSheet) {
                DistrictPickerSheet(
                    currentDistrict = filterState.district,
                    onDistrictSelected = { district ->
                        viewModel.setDistrict(district)
                    },
                    onDismiss = { viewModel.setShowDistrictSheet(false) }
                )
            }

            // Filter Dialog Sheet
            if (showFilterDialog) {
                FilterDialog(
                    selectedCategory = filterState.category,
                    selectedType = filterState.type,
                    selectedRooms = filterState.rooms,
                    onApplyFilter = { category, type, rooms, maxPrice ->
                        viewModel.applyAdvancedFilter(category, type, rooms, maxPrice)
                    },
                    onDismiss = { viewModel.setShowFilterDialog(false) }
                )
            }

            // Notifications Sheet
            if (showNotifications) {
                NotificationsDialog(
                    notifications = notifications,
                    userInterests = userInterests,
                    onUpdateInterests = { updated -> viewModel.updateUserInterests(updated) },
                    onTestPush = { viewModel.triggerTestFcmPush() },
                    onDismiss = { viewModel.setShowNotifications(false) },
                    onNotificationClick = { notif ->
                        viewModel.markNotificationAsRead(notif.id)
                        if (notif.targetPropertyId != null) {
                            val prop = allProperties.firstOrNull { it.id == notif.targetPropertyId }
                            if (prop != null) {
                                viewModel.selectProperty(prop)
                                viewModel.setShowNotifications(false)
                            }
                        } else if (notif.targetChatId != null) {
                            viewModel.selectTab(NavTab.CHATS)
                            viewModel.setShowNotifications(false)
                        }
                    },
                    onMarkAllAsRead = { viewModel.markAllNotificationsAsRead() },
                    onClearAll = { viewModel.clearAllNotifications() }
                )
            }

            // Super Admin & User Role Login Dialog
            if (showAdminLoginDialog) {
                AdminLoginDialog(
                    errorMessage = adminAuthErrorMessage,
                    currentRole = currentUserRole,
                    onDismiss = { viewModel.closeAdminLoginDialog() },
                    onLoginWithRole = { email, name, phone, pin, role ->
                        viewModel.loginWithRole(email, name, phone, pin, role)
                    },
                    onAuthenticate = { pin ->
                        viewModel.authenticateAdmin(pin)
                    }
                )
            }

            // User Google/Email Authentication Dialog & Login Screen
            val showAuthDialog by viewModel.showAuthDialog.collectAsStateWithLifecycle()
            val showLoginScreen by viewModel.showLoginScreen.collectAsStateWithLifecycle()
            val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
            val authError by viewModel.authError.collectAsStateWithLifecycle()
            val appContext = androidx.compose.ui.platform.LocalContext.current

            if (showLoginScreen) {
                LoginScreen(
                    onGoogleSignIn = { viewModel.signInWithGoogle(appContext) },
                    onEmailSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
                    onEmailSignUp = { email, pass, name, phone -> viewModel.signUpWithEmail(email, pass, name, phone) },
                    onContinueAsGuest = { viewModel.closeLoginScreen() },
                    onBack = { viewModel.closeLoginScreen() },
                    isLoading = authLoading,
                    errorMessage = authError
                )
            }

            AuthDialog(
                isOpen = showAuthDialog,
                onDismiss = { viewModel.closeAuthDialog() },
                onGoogleSignIn = { viewModel.signInWithGoogle(appContext) },
                onEmailSignIn = { email, pass -> viewModel.signInWithEmail(email, pass) },
                onEmailSignUp = { email, pass, name, phone -> viewModel.signUpWithEmail(email, pass, name, phone) },
                isLoading = authLoading,
                errorMessage = authError
            )

            // New User Onboarding Flow
            val showOnboardingDialog by viewModel.showOnboardingDialog.collectAsStateWithLifecycle()
            OnboardingDialog(
                isOpen = showOnboardingDialog,
                onComplete = { firstSearch -> viewModel.completeOnboarding(firstSearch) }
            )
        }
    }
}
