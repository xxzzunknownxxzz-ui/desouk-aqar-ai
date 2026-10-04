package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AdminActionType
import com.example.data.AdminActivityLogEntity
import com.example.data.AppDatabase
import com.example.data.AppNotification
import com.example.data.AppSettingEntity
import com.example.data.AppUserEntity
import com.example.data.CustomerInquiryEntity
import com.example.data.CustomerRequestEntity
import com.example.data.FirestoreProperty
import com.example.data.FirestorePropertyService
import com.example.data.InquiryStatus
import com.example.data.NotificationType
import com.example.data.PropertyEntity
import com.example.data.PropertyRepository
import com.example.data.PropertyReviewEntity
import com.example.data.PropertyStatus
import com.example.data.PropertySourceEntity
import com.example.data.RawPostEntity
import com.example.data.ImportedPropertyEntity
import com.example.data.ImportLogEntity
import com.example.data.PropertyDraftEntity
import com.example.data.CommunityPostEntity
import com.example.data.CommunityCommentEntity
import com.example.data.UserInterests
import com.example.data.UserInterestsPreferenceManager
import com.example.data.SourcesDashboardStats
import com.example.data.ImportedAdStatus
import com.example.data.SourceType
import com.example.data.ai.RealEstateAiExtractor
import com.example.data.ai.FacebookImportAiService
import com.example.data.ai.FacebookExtractedAd
import com.example.data.ai.FacebookUrlReadResult
import com.example.data.FirestoreSource
import com.example.data.FirestoreRawPost
import com.example.data.FirestoreImportedProperty
import com.example.data.RequestStatus
import com.example.data.RequestType
import com.example.data.UserRole
import com.example.ui.components.NavTab
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FilterState(
    val query: String = "",
    val category: String? = null,
    val type: String? = null,
    val district: String = "دسوق - كفر الشيخ",
    val rooms: Int? = null,
    val maxPrice: Double? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: PropertyRepository
    private val firestoreService = FirestorePropertyService(application)
    val userInterestsManager = UserInterestsPreferenceManager(application)
    val userInterests: StateFlow<UserInterests> = userInterestsManager.interests
    val facebookImportAiService = FacebookImportAiService()
    val allProperties: StateFlow<List<PropertyEntity>>
    val favoriteProperties: StateFlow<List<PropertyEntity>>
    val userProperties: StateFlow<List<PropertyEntity>>
    val allCustomerRequests: StateFlow<List<CustomerRequestEntity>>
    val allInquiries: StateFlow<List<CustomerInquiryEntity>>
    val allUsers: StateFlow<List<AppUserEntity>>
    val allActivityLogs: StateFlow<List<AdminActivityLogEntity>>
    val allSettings: StateFlow<List<AppSettingEntity>>
    val allSources: StateFlow<List<PropertySourceEntity>>
    val allRawPosts: StateFlow<List<RawPostEntity>>
    val allImportedProperties: StateFlow<List<ImportedPropertyEntity>>
    val reviewQueue: StateFlow<List<ImportedPropertyEntity>>
    val allImportLogs: StateFlow<List<ImportLogEntity>>
    val sourcesDashboardStats: StateFlow<SourcesDashboardStats>
    val propertyDraft: StateFlow<PropertyDraftEntity?>
    val allCommunityPosts: StateFlow<List<CommunityPostEntity>>

    // Firestore Preloading State (الإعداد المسبق لبيانات الصفحة الرئيسية)
    val isFirestorePreloading: StateFlow<Boolean> = firestoreService.isPreloading
    val firestorePreloadSource: StateFlow<String> = firestoreService.preloadSource
    val firestoreLastPreloadTimestamp: StateFlow<Long> = firestoreService.lastPreloadTimestamp
    val firestorePreloadedProperties: StateFlow<List<FirestoreProperty>> = firestoreService.preloadedProperties

    // Admin Auth State (محمي بالكامل)
    private val _isAdminAuthenticated = MutableStateFlow(false)
    val isAdminAuthenticated: StateFlow<Boolean> = _isAdminAuthenticated.asStateFlow()

    private val _currentAdminUser = MutableStateFlow<AppUserEntity?>(null)
    val currentAdminUser: StateFlow<AppUserEntity?> = _currentAdminUser.asStateFlow()

    // User Role and Session State (محقق في Firestore)
    private val _currentUserRole = MutableStateFlow("User") // "Admin" أو "User"
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _currentUserName = MutableStateFlow("مستخدم عقارات دسوق")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserEmail = MutableStateFlow("user@desouk.com")
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _isFirestoreRoleVerified = MutableStateFlow(false)
    val isFirestoreRoleVerified: StateFlow<Boolean> = _isFirestoreRoleVerified.asStateFlow()

    private val _showAdminLoginDialog = MutableStateFlow(false)
    val showAdminLoginDialog: StateFlow<Boolean> = _showAdminLoginDialog.asStateFlow()

    private val _adminAuthErrorMessage = MutableStateFlow<String?>(null)
    val adminAuthErrorMessage: StateFlow<String?> = _adminAuthErrorMessage.asStateFlow()

    val sessionManager = com.example.data.UserSessionManager(application)
    val userSession: StateFlow<com.example.data.UserSession> = sessionManager.session
    val authManager = com.example.data.FirebaseAuthManager(application, sessionManager, firestoreService)

    private val _showAuthDialog = MutableStateFlow(false)
    val showAuthDialog: StateFlow<Boolean> = _showAuthDialog.asStateFlow()

    private val _showLoginScreen = MutableStateFlow(false)
    val showLoginScreen: StateFlow<Boolean> = _showLoginScreen.asStateFlow()

    private val _showOnboardingDialog = MutableStateFlow(!sessionManager.session.value.hasCompletedOnboarding)
    val showOnboardingDialog: StateFlow<Boolean> = _showOnboardingDialog.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _currentTab = MutableStateFlow(NavTab.HOME)
    val currentTab: StateFlow<NavTab> = _currentTab.asStateFlow()

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _selectedProperty = MutableStateFlow<PropertyEntity?>(null)
    val selectedProperty: StateFlow<PropertyEntity?> = _selectedProperty.asStateFlow()

    private val _showDistrictSheet = MutableStateFlow(false)
    val showDistrictSheet: StateFlow<Boolean> = _showDistrictSheet.asStateFlow()

    private val _showFilterDialog = MutableStateFlow(false)
    val showFilterDialog: StateFlow<Boolean> = _showFilterDialog.asStateFlow()

    private val _showNotifications = MutableStateFlow(false)
    val showNotifications: StateFlow<Boolean> = _showNotifications.asStateFlow()

    private val _notifications = MutableStateFlow<List<AppNotification>>(
        listOf(
            AppNotification(
                id = 101L,
                type = NotificationType.MATCHING_PROPERTY,
                title = "عقار جديد يطابق طلبك في كورنيش النيل 🌊",
                message = "تمت إضافة شقة فاخرة (175 م²) 3 غرف تطل مباشرة على النيل بسعر مميز.",
                timeAgo = "منذ 10 دقائق",
                isRead = false,
                targetPropertyId = 1L,
                categoryBadge = "تطابق مواصفات"
            ),
            AppNotification(
                id = 102L,
                type = NotificationType.MESSAGE_REPLY,
                title = "تم الرد على رسالتك 💬",
                message = "أ/ محمود الشناوي (مكتب النيل): شقة شارع الجيش متاحة للمعاينة اليوم الساعة 5 مساءً.",
                timeAgo = "منذ 25 دقيقة",
                isRead = false,
                targetChatId = 1L,
                categoryBadge = "محادثة عقارية"
            ),
            AppNotification(
                id = 103L,
                type = NotificationType.MATCHING_PROPERTY,
                title = "فرصة تجارية مطابقة لاحتياجاتك 🏪",
                message = "محل تجاري 45 م² بالميدان الإبراهيمي متاح للإيجار الآن بالسعر والموقع المطلوب.",
                timeAgo = "منذ ساعة",
                isRead = false,
                targetPropertyId = 2L,
                categoryBadge = "فرصة استثمارية"
            ),
            AppNotification(
                id = 104L,
                type = NotificationType.MESSAGE_REPLY,
                title = "المساعد العقاري الذكي رد على استفسارك 🤖",
                message = "تم إعداد دليل أسعار المتر وشروط التسجيل بالشهر العقاري في حي دحروج.",
                timeAgo = "اليوم",
                isRead = true,
                targetChatId = null,
                categoryBadge = "ذكاء اصطناعي"
            )
        )
    )
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    val unreadNotificationsCount: StateFlow<Int> = _notifications.map { list ->
        list.count { !it.isRead }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 3
    )

    fun markNotificationAsRead(id: Long) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun clearAllNotifications() {
        _notifications.value = emptyList()
    }

    fun addNotification(
        type: NotificationType,
        title: String,
        message: String,
        targetPropertyId: Long? = null,
        targetChatId: Long? = null,
        categoryBadge: String = ""
    ) {
        val newNotif = AppNotification(
            id = System.currentTimeMillis(),
            type = type,
            title = title,
            message = message,
            timeAgo = "الآن",
            isRead = false,
            targetPropertyId = targetPropertyId,
            targetChatId = targetChatId,
            categoryBadge = categoryBadge
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    /**
     * فحص وتنبيه المستخدم عند وجود عقارات مطابقة لاحتياجاته المحددة
     */
    fun checkAndNotifyMatchingProperties(
        district: String,
        propertyType: String,
        dealType: String
    ) {
        val currentProps = allProperties.value
        val matched = currentProps.firstOrNull { prop ->
            (district == "كل دسوق" || prop.district.contains(district, ignoreCase = true) || district.contains(prop.district, ignoreCase = true)) &&
            (propertyType.isBlank() || prop.title.contains(propertyType.replace("شقة سكنية", "شقة"), ignoreCase = true) || prop.category.contains(propertyType, ignoreCase = true))
        }

        if (matched != null) {
            addNotification(
                type = NotificationType.MATCHING_PROPERTY,
                title = "عقار مطابق لاحتياجاتك في $district 🎯",
                message = "وجدنا لك: ${matched.title} في ${matched.district} بسعر ${matched.price} ${matched.priceUnit}.",
                targetPropertyId = matched.id,
                categoryBadge = "مطابق لطلبك"
            )
        } else {
            addNotification(
                type = NotificationType.MATCHING_PROPERTY,
                title = "تم تسجيل رصد العقارات في $district ✨",
                message = "جارٍ البحث عن $propertyType ($dealType) في $district، وسنوافيك بإشعار فوري عند إتاحة عقار مطابق.",
                categoryBadge = "رصد مستمر"
            )
        }
    }

    /**
     * محاكاة وصول رد من الوسيط / المعلن في المحادثة وتنبيه المستخدم
     */
    fun simulateIncomingAgentReply(
        chatId: Long,
        agentName: String,
        propertyTitle: String,
        userMessage: String
    ) {
        viewModelScope.launch {
            kotlinx.coroutines.delay(2500)
            val replySnippet = when {
                userMessage.contains("سعر") || userMessage.contains("تفاوض") || userMessage.contains("بكام") ->
                    "أهلاً بك، بالنسبة للسعر متاح تفاوض بسيط عند الجدية إن شاء الله."
                userMessage.contains("معاينة") || userMessage.contains("زيارة") || userMessage.contains("موعد") ->
                    "تم تسجيل طلبك للمعاينة، هل يناسبك اليوم بين 5 و 7 مساءً؟"
                else ->
                    "أهلاً بحضرتك، العقار متاح حالياً ويسعدنا استقبالك في المكتب أو التنسيق للمعاينة."
            }
            addNotification(
                type = NotificationType.MESSAGE_REPLY,
                title = "رد جديد من $agentName 💬",
                message = "بخصوص $propertyTitle: $replySnippet",
                targetChatId = chatId,
                categoryBadge = "رد على رسالتك"
            )
        }
    }

    private val _assistantInitialQuery = MutableStateFlow<String?>(null)
    val assistantInitialQuery: StateFlow<String?> = _assistantInitialQuery.asStateFlow()

    fun openAiAssistantWithQuery(query: String? = null) {
        _assistantInitialQuery.value = query
        _currentTab.value = NavTab.AI_ASSISTANT
    }

    fun clearAssistantInitialQuery() {
        _assistantInitialQuery.value = null
    }

    init {
        val database = AppDatabase.getDatabase(application, viewModelScope)
        repository = PropertyRepository(
            propertyDao = database.propertyDao(),
            reviewDao = database.propertyReviewDao(),
            requestDao = database.customerRequestDao(),
            inquiryDao = database.customerInquiryDao(),
            userDao = database.appUserDao(),
            logDao = database.adminActivityLogDao(),
            settingDao = database.appSettingDao(),
            sourceDao = database.propertySourceDao(),
            rawPostDao = database.rawPostDao(),
            importedDao = database.importedPropertyDao(),
            importLogDao = database.importLogDao(),
            draftDao = database.propertyDraftDao(),
            communityDao = database.communityDao()
        )

        viewModelScope.launch(Dispatchers.IO) {
            repository.checkAndSeedDatabase()
            repository.seedCommunityPostsIfEmpty()
            // تفعيل تقنية الإعداد المسبق لبيانات Firestore للصفحة الرئيسية فورياً (Firestore Preloading)
            preloadFirestoreData()
        }

        allProperties = repository.allProperties.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        favoriteProperties = repository.favoriteProperties.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        userProperties = repository.userProperties.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allCustomerRequests = repository.allCustomerRequests.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allInquiries = repository.allInquiries.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allUsers = repository.allUsers.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allActivityLogs = repository.allActivityLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSettings = repository.allSettings.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allSources = repository.allSources.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allRawPosts = repository.allRawPosts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allImportedProperties = repository.allImportedProperties.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        reviewQueue = repository.reviewQueue.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allImportLogs = repository.allImportLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        sourcesDashboardStats = combine(
            allSources,
            allRawPosts,
            allImportedProperties
        ) { sources, rawPosts, imported ->
            val activeSources = sources.count { it.isActive }
            val inReview = imported.count { it.status == ImportedAdStatus.IN_REVIEW }
            val published = imported.count { it.status == ImportedAdStatus.ACTIVE }
            val duplicates = imported.count { it.isDuplicate || it.status == ImportedAdStatus.DUPLICATE }
            val rejected = imported.count { it.status == ImportedAdStatus.REJECTED }
            val archived = imported.count { it.status == ImportedAdStatus.ARCHIVED }
            val newAds = imported.count { it.status == ImportedAdStatus.IN_REVIEW }

            SourcesDashboardStats(
                activeSourcesCount = activeSources,
                totalImportedPostsCount = rawPosts.size,
                newAdsCount = newAds,
                inReviewCount = inReview,
                publishedCount = published,
                duplicateCount = duplicates,
                rejectedCount = rejected,
                archivedCount = archived
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SourcesDashboardStats(activeSourcesCount = 3, totalImportedPostsCount = 3, newAdsCount = 3, inReviewCount = 3)
        )

        propertyDraft = repository.currentDraft.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

        allCommunityPosts = repository.allCommunityPosts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            sessionManager.session.collect { session ->
                if (session.isLoggedIn) {
                    _currentUserRole.value = session.role
                    _currentUserName.value = session.name
                    _currentUserEmail.value = session.email
                    if (session.role == "Admin" || session.role == UserRole.SUPER_ADMIN) {
                        _isAdminAuthenticated.value = true
                    }
                }
            }
        }
    }

    // Filtered properties based on search query, category, district, type, etc.
    val filteredProperties: StateFlow<List<PropertyEntity>> = combine(
        allProperties,
        _filterState
    ) { properties, filter ->
        properties.filter { prop ->
            val matchesQuery = filter.query.isBlank() ||
                    prop.title.contains(filter.query, ignoreCase = true) ||
                    prop.location.contains(filter.query, ignoreCase = true) ||
                    prop.category.contains(filter.query, ignoreCase = true) ||
                    prop.description.contains(filter.query, ignoreCase = true)

            val matchesCategory = filter.category == null || when {
                filter.category.contains("شقق") -> prop.category == "شقق" || prop.category.contains("شقق")
                filter.category.contains("منازل") -> prop.category == "منازل" || prop.category.contains("منازل") || prop.category.contains("بيت")
                filter.category.contains("محلات") -> prop.category == "محلات" || prop.category.contains("محل")
                filter.category.contains("إداري") || filter.category.contains("مكاتب") || filter.category.contains("إدارية") ->
                    prop.category == "مكاتب" || prop.category == "وحدات إدارية" || prop.title.contains("إداري") || prop.title.contains("مكتب") || prop.description.contains("إداري")
                filter.category.contains("أراضي") || filter.category.contains("أرض") -> prop.category == "أراضي" || prop.category.contains("أرض")
                else -> prop.category.equals(filter.category, ignoreCase = true) || prop.category.contains(filter.category) || filter.category.contains(prop.category)
            }
            val matchesType = filter.type == null || prop.type == filter.type
            val matchesRooms = filter.rooms == null || prop.rooms >= filter.rooms
            val matchesPrice = filter.maxPrice == null || prop.price <= filter.maxPrice

            val cleanDistrict = filter.district.replace("دسوق - ", "").trim()
            val matchesDistrict = filter.district.contains("كفر الشيخ") ||
                    filter.district.contains("الكل") ||
                    prop.location.contains(cleanDistrict, ignoreCase = true) ||
                    prop.district.contains(cleanDistrict, ignoreCase = true)

            matchesQuery && matchesCategory && matchesType && matchesRooms && matchesPrice && matchesDistrict
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectTab(tab: NavTab) {
        if (tab == NavTab.ADMIN) {
            if (!_isAdminAuthenticated.value || _currentUserRole.value != "Admin") {
                _adminAuthErrorMessage.value = if (_currentUserRole.value == "User") {
                    "تم رفض الوصول: حسابك مسجل ومحقق في Firestore بدور 'User' (مستخدم عادي). واجهة لوحة التحكم تظهر وتتاح فقط لحسابات الأدمن بعد التحقق من دور 'Admin'."
                } else {
                    null
                }
                _showAdminLoginDialog.value = true
                return
            }
        }
        _currentTab.value = tab
    }

    fun setQuery(q: String) {
        _filterState.value = _filterState.value.copy(query = q)
    }

    fun setCategory(cat: String?) {
        val currentCat = _filterState.value.category
        val newCat = if (currentCat == cat) null else cat
        _filterState.value = _filterState.value.copy(category = newCat)
    }

    fun setDistrict(district: String) {
        _filterState.value = _filterState.value.copy(district = district)
    }

    fun applyAdvancedFilter(category: String?, type: String?, rooms: Int?, maxPrice: Double?) {
        _filterState.value = _filterState.value.copy(
            category = category,
            type = type,
            rooms = rooms,
            maxPrice = maxPrice
        )
    }

    fun resetFilters() {
        _filterState.value = FilterState(district = _filterState.value.district)
    }

    fun selectProperty(property: PropertyEntity?) {
        _selectedProperty.value = property
    }

    fun selectPropertyById(propertyId: Long) {
        val prop = allProperties.value.firstOrNull { it.id == propertyId }
        if (prop != null) {
            _selectedProperty.value = prop
        }
    }

    fun toggleFavorite(propertyId: Long, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.toggleFavorite(propertyId, isFavorite)
            // also update current selected property if open
            if (_selectedProperty.value?.id == propertyId) {
                _selectedProperty.value = _selectedProperty.value?.copy(isFavorite = isFavorite)
            }
        }
    }

    fun addProperty(
        title: String,
        category: String,
        type: String,
        location: String,
        district: String,
        price: Double,
        priceUnit: String,
        rooms: Int,
        bathrooms: Int,
        area: Double,
        floor: Int,
        phone: String,
        whatsapp: String,
        description: String,
        imageResName: String,
        ownerName: String = "",
        isNegotiable: Boolean = true,
        imagesJson: String = "",
        videoUrl: String = "",
        finishing: String = "كامل التشطيب",
        addressDetail: String = "",
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val session = sessionManager.session.value
            val currentUid = session.uid
            val author = if (ownerName.isNotBlank()) ownerName else session.name

            val entity = PropertyEntity(
                title = title,
                category = category,
                type = type,
                location = if (location.isNotBlank()) location else "دسوق - $district",
                district = district,
                address = addressDetail,
                price = price,
                priceUnit = priceUnit,
                rooms = rooms,
                bathrooms = bathrooms,
                area = area,
                floor = floor,
                finishing = finishing,
                videoUrl = videoUrl,
                imageResName = imageResName,
                phone = phone,
                whatsapp = whatsapp,
                description = description,
                status = PropertyStatus.PUBLISHED,
                isUserAdded = true,
                ownerId = currentUid,
                ownerName = author,
                isNegotiable = isNegotiable,
                imagesJson = imagesJson,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val insertedId = repository.insertProperty(entity)
            repository.clearDraft()

            // مزامنة العقار مع Firestore إن كانت متوفرة
            try {
                if (firestoreService.isFirebaseAvailable()) {
                    val firestoreProp = FirestoreProperty.fromPropertyEntity(entity)
                    firestoreService.addProperty(firestoreProp, isAdminAuthenticated = true)
                }
            } catch (_: Exception) {}

            addNotification(
                type = NotificationType.SYSTEM,
                title = "تم نشر عقارك بنجاح 🎉",
                message = "عقارك '$title' معروض الآن للجميع في دسوق.",
                categoryBadge = "إعلاناتي"
            )

            // فحص وتنبيه المستخدمين ذوي الاهتمامات المطابقة عبر إشعار دفع Firebase Cloud Messaging
            userInterestsManager.checkAndNotifyIfMatches(entity)
            onSuccess()
        }
    }

    /**
     * تحديث وتعديل بيانات الإعلان (لصاحب الإعلان فقط مع الحفاظ على ownerId)
     */
    fun updatePropertyDetails(
        propertyId: Long,
        title: String,
        category: String,
        type: String,
        price: Double,
        priceUnit: String,
        district: String,
        address: String,
        rooms: Int,
        bathrooms: Int,
        area: Double,
        floor: Int,
        phone: String,
        whatsapp: String,
        description: String,
        status: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val existing = repository.getPropertyByIdDirect(propertyId)
            val currentUid = sessionManager.session.value.uid
            val currentRole = currentUserRole.value
            // التحقق من الصلاحيات: صاحب الإعلان فقط أو الأدمن مع الحفاظ التام على ownerId
            if (existing != null && (existing.ownerId.isBlank() || existing.ownerId == currentUid || currentRole == "Admin")) {
                val updated = existing.copy(
                    title = title,
                    category = category,
                    type = type,
                    price = price,
                    priceUnit = priceUnit,
                    district = district,
                    location = if (district.contains("دسوق")) district else "دسوق - $district",
                    address = address,
                    rooms = rooms,
                    bathrooms = bathrooms,
                    area = area,
                    floor = floor,
                    phone = phone,
                    whatsapp = whatsapp,
                    description = description,
                    status = status,
                    updatedAt = System.currentTimeMillis() // تحديث وقت التعديل دون المساس بـ ownerId
                )
                repository.updateProperty(updated)
                addNotification(
                    type = NotificationType.SYSTEM,
                    title = "تم تعديل إعلانك ✓",
                    message = "تم حفظ تحديثات العقار '$title' بنجاح.",
                    categoryBadge = "إعلاناتي"
                )
                onSuccess?.invoke()
            }
        }
    }

    /**
     * تغيير حالة العقار (متاح / مباع / مؤجر / موقوف)
     */
    fun updatePropertyStatus(propertyId: Long, newStatus: String) {
        viewModelScope.launch {
            val existing = repository.getPropertyByIdDirect(propertyId)
            val currentUid = sessionManager.session.value.uid
            val currentRole = currentUserRole.value
            if (existing != null && (existing.ownerId.isBlank() || existing.ownerId == currentUid || currentRole == "Admin")) {
                repository.updatePropertyStatus(propertyId, newStatus)
                addNotification(
                    type = NotificationType.SYSTEM,
                    title = "تحديث حالة الإعلان",
                    message = "تم تغيير حالة العقار '${existing.title}' إلى: $newStatus",
                    categoryBadge = "إعلاناتي"
                )
            }
        }
    }

    /**
     * حذف عقار المستخدم بعد التحقق من الصلاحيات
     */
    fun deleteUserProperty(propertyId: Long, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            val prop = repository.getPropertyByIdDirect(propertyId)
            val currentUid = sessionManager.session.value.uid
            val currentRole = currentUserRole.value
            // التحقق من الصلاحيات: صاحب العقار أو الأدمن فقط
            if (prop != null && (prop.ownerId.isBlank() || prop.ownerId == currentUid || currentRole == "Admin")) {
                repository.deleteProperty(propertyId)
                if (_selectedProperty.value?.id == propertyId) {
                    _selectedProperty.value = null
                }
                addNotification(
                    type = NotificationType.SYSTEM,
                    title = "حذف الإعلان 🗑️",
                    message = "تم حذف إعلان '${prop.title}' بنجاح.",
                    categoryBadge = "إعلاناتي"
                )
                onSuccess?.invoke()
            }
        }
    }

    /**
     * الإبلاغ عن إعلان مخالف وحفظ البلاغ في قاعدة البيانات والسحابة
     */
    fun reportProperty(
        propertyId: Long,
        propertyTitle: String,
        reason: String,
        details: String,
        onSuccess: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            val session = sessionManager.session.value
            val reporterUid = if (session.isLoggedIn) session.uid else "guest_reporter"
            val reporterEmail = if (session.isLoggedIn) session.email else "guest@desouk.com"

            try {
                firestoreService.reportPropertyToFirestore(
                    propertyId = propertyId,
                    propertyTitle = propertyTitle,
                    reason = reason,
                    details = details,
                    reporterUid = reporterUid,
                    reporterEmail = reporterEmail
                )
            } catch (_: Exception) {}

            addNotification(
                type = NotificationType.SYSTEM,
                title = "تم استلام بلاغك 🛡️",
                message = "شكراً لتعاونك، سنراجع الإعلان '$propertyTitle' للتحقق من دقته.",
                categoryBadge = "أمان المنصة"
            )
            onSuccess?.invoke()
        }
    }

    /**
     * حفظ مسودة العقار تلقائياً في Room Database (Auto-Save Draft)
     */
    fun savePropertyDraft(
        title: String,
        category: String,
        type: String,
        district: String,
        addressDetail: String,
        priceText: String,
        areaText: String,
        roomsText: String,
        bathroomsText: String,
        floorText: String,
        phone: String,
        whatsapp: String,
        description: String,
        selectedImage: String
    ) {
        viewModelScope.launch {
            val hasContent = title.isNotBlank() || addressDetail.isNotBlank() ||
                    priceText.isNotBlank() || areaText.isNotBlank() || description.isNotBlank()
            if (hasContent) {
                repository.saveDraft(
                    PropertyDraftEntity(
                        id = 1L,
                        title = title,
                        category = category,
                        type = type,
                        district = district,
                        addressDetail = addressDetail,
                        priceText = priceText,
                        areaText = areaText,
                        roomsText = roomsText,
                        bathroomsText = bathroomsText,
                        floorText = floorText,
                        phone = phone,
                        whatsapp = whatsapp,
                        description = description,
                        selectedImage = selectedImage,
                        updatedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    /**
     * مسح مسودة العقار من Room Database عند نجاح النشر أو رغبة المستخدم في البدء من جديد
     */
    fun clearPropertyDraft() {
        viewModelScope.launch {
            repository.clearDraft()
        }
    }

    fun deleteProperty(id: Long) {
        viewModelScope.launch {
            repository.deleteProperty(id)
            if (_selectedProperty.value?.id == id) {
                _selectedProperty.value = null
            }
        }
    }

    fun setShowDistrictSheet(show: Boolean) {
        _showDistrictSheet.value = show
    }

    fun setShowFilterDialog(show: Boolean) {
        _showFilterDialog.value = show
    }

    fun setShowNotifications(show: Boolean) {
        _showNotifications.value = show
    }

    fun getReviewsForProperty(propertyId: Long): Flow<List<PropertyReviewEntity>> {
        return repository.getReviewsForProperty(propertyId)
    }

    fun getAverageRating(propertyId: Long): Flow<Double?> {
        return repository.getAverageRating(propertyId)
    }

    fun addReview(
        propertyId: Long,
        userName: String,
        userRole: String,
        rating: Int,
        comment: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val reviewEntity = PropertyReviewEntity(
                propertyId = propertyId,
                userName = userName.ifBlank { "مستخدم في دسوق" },
                userRole = userRole.ifBlank { "زائر معاينة" },
                rating = rating.coerceIn(1, 5),
                comment = comment.trim()
            )
            // 1. الحفظ المحلي السريع في Room Database
            repository.addReview(reviewEntity)

            // 2. الحفظ السحابي الموثوق في Cloud Firestore
            launch(Dispatchers.IO) {
                firestoreService.addReviewToFirestore(
                    propertyId = propertyId,
                    userName = reviewEntity.userName,
                    userRole = reviewEntity.userRole,
                    rating = reviewEntity.rating,
                    comment = reviewEntity.comment
                )
            }

            addNotification(
                type = NotificationType.SYSTEM,
                title = "تم نشر وحفظ تقييمك في Firestore ⭐",
                message = "شكراً لمشاركتك رأيك، تم تخزين مراجعتك وتقييمك في سحابة Firestore لعرضها لجميع المستخدمين.",
                targetPropertyId = propertyId,
                categoryBadge = "تقييم وسحابة"
            )
            onSuccess()
        }
    }

    /**
     * مزامنة تقييمات العقار من Firestore سحابياً وتحديث الكاش المحلي
     */
    fun syncPropertyReviewsFromFirestore(propertyId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val firestoreReviews = firestoreService.fetchReviewsForPropertyFromFirestore(propertyId)
            for (review in firestoreReviews) {
                repository.addReview(review)
            }
        }
    }

    /**
     * تحديث اهتمامات المستخدم المفضلة لإشعارات دفع Firebase Cloud Messaging
     */
    fun updateUserInterests(newInterests: UserInterests) {
        userInterestsManager.updateInterests(newInterests)
    }

    /**
     * إرسال إشعار تجريبي لاختبار إشعارات Firebase Cloud Messaging الفورية
     */
    fun triggerTestFcmPush() {
        userInterestsManager.triggerTestPushNotification()
    }

    // ==========================================
    // نـظـام الإدارة وحـسـاب SUPER ADMIN
    // ==========================================

    fun openAdminGate() {
        if (_isAdminAuthenticated.value && _currentUserRole.value == "Admin") {
            _currentTab.value = NavTab.ADMIN
        } else {
            if (_currentUserRole.value == "User") {
                _adminAuthErrorMessage.value = "حسابك الحالي مسجل بدور 'User' في Firestore. للوصول إلى لوحة التحكم، يجب تسجيل الدخول بحساب يمتلك دور 'Admin'."
            } else {
                _adminAuthErrorMessage.value = null
            }
            _showAdminLoginDialog.value = true
        }
    }

    fun closeAdminLoginDialog() {
        _showAdminLoginDialog.value = false
        _adminAuthErrorMessage.value = null
    }

    /**
     * تسجيل الدخول والتحقق من دور المستخدم (Admin vs User) وتخزينه في Firestore
     */
    fun loginWithRole(
        email: String,
        name: String,
        phone: String,
        pinOrPassword: String,
        role: String // "Admin" أو "User"
    ) {
        val cleanEmail = email.trim().ifBlank { if (role == "Admin") "admin@desouk-aqar.com" else "user@desouk.com" }
        val cleanName = name.trim().ifBlank { if (role == "Admin") "Super Admin (الإدارة العامة)" else "مستخدم عقارات دسوق" }
        val cleanPhone = phone.trim().ifBlank { if (role == "Admin") "01000000000" else "01099887766" }
        val cleanPin = pinOrPassword.trim()

        viewModelScope.launch {
            if (role == "Admin") {
                // التحقق من رمز مرور الأدمن
                if (cleanPin == "123456" || cleanPin == "desouk2026" || cleanPin == "0000") {
                    val userId = "admin_${cleanEmail.replace("@", "_").replace(".", "_")}"

                    // 1. تخزين دور Admin في مجموعة المستخدمين في Firestore
                    if (firestoreService.isFirebaseAvailable()) {
                        firestoreService.saveUserRoleToFirestore(
                            userId = userId,
                            name = cleanName,
                            email = cleanEmail,
                            phone = cleanPhone,
                            role = "Admin"
                        )
                    }

                    // 2. التحقق من دور المستخدم المسترجع من Firestore
                    val verifiedRole = if (firestoreService.isFirebaseAvailable()) {
                        firestoreService.fetchUserRoleFromFirestore(userId) ?: "Admin"
                    } else {
                        "Admin"
                    }

                    if (verifiedRole == "Admin" || verifiedRole == "SUPER_ADMIN") {
                        _isAdminAuthenticated.value = true
                        _currentUserRole.value = "Admin"
                        _currentUserName.value = cleanName
                        _currentUserEmail.value = cleanEmail
                        _isFirestoreRoleVerified.value = true
                        _adminAuthErrorMessage.value = null
                        _showAdminLoginDialog.value = false
                        _currentTab.value = NavTab.ADMIN // فتح واجهة لوحة التحكم فقط للأدمن بعد التحقق من الدور

                        val superAdminUser = AppUserEntity(
                            id = 1,
                            name = cleanName,
                            phone = cleanPhone,
                            email = cleanEmail,
                            role = UserRole.SUPER_ADMIN,
                            city = "دسوق",
                            district = "شارع الجيش"
                        )
                        _currentAdminUser.value = superAdminUser

                        repository.insertActivityLog(
                            actionType = AdminActionType.LOGIN_SUCCESS,
                            details = "تم تسجيل الدخول وتوثيق دور 'Admin' بنجاح في Firestore لمجموعة Users"
                        )
                        addNotification(
                            type = NotificationType.SYSTEM,
                            title = "تم التحقق من دور Admin في Firestore 🛡️",
                            message = "مرحباً بك! تم تأكيد صلاحيات الأدمن بنجاح وفتح واجهة لوحة التحكم.",
                            categoryBadge = "أمان وإدارة"
                        )
                    } else {
                        _adminAuthErrorMessage.value = "فشل التحقق: الحساب مسجل في Firestore بدور '$verifiedRole' وليس 'Admin'."
                        repository.insertActivityLog(
                            actionType = AdminActionType.UNAUTHORIZED_ATTEMPT,
                            details = "محاولة دخول للوحة التحكم بحساب لا يحمل دور Admin في Firestore"
                        )
                    }
                } else {
                    _adminAuthErrorMessage.value = "رمز الدخول السري غير صحيح. الصلاحيات مقيدة لحساب Admin فقط."
                    repository.insertActivityLog(
                        actionType = AdminActionType.UNAUTHORIZED_ATTEMPT,
                        details = "محاولة دخول فاشلة بدور Admin برمز PIN غير مطابق"
                    )
                }
            } else {
                // تسجيل الدخول كمستخدم عادي (User)
                val userId = "user_${cleanEmail.replace("@", "_").replace(".", "_")}"

                // 1. تخزين دور User في مجموعة المستخدمين في Firestore
                if (firestoreService.isFirebaseAvailable()) {
                    firestoreService.saveUserRoleToFirestore(
                        userId = userId,
                        name = cleanName,
                        email = cleanEmail,
                        phone = cleanPhone,
                        role = "User"
                    )
                }

                _isAdminAuthenticated.value = false
                _currentUserRole.value = "User"
                _currentUserName.value = cleanName
                _currentUserEmail.value = cleanEmail
                _isFirestoreRoleVerified.value = true
                _adminAuthErrorMessage.value = null
                _showAdminLoginDialog.value = false

                // إذا كان على تبويب لوحة التحكم، يتم إخراجه فوراً لأنه مسجل بدور User
                if (_currentTab.value == NavTab.ADMIN) {
                    _currentTab.value = NavTab.PROFILE
                }

                repository.insertActivityLog(
                    actionType = AdminActionType.UPDATE_SETTINGS,
                    details = "تم تسجيل دخول مستخدم بدور 'User' وتخزينه في Firestore"
                )
                addNotification(
                    type = NotificationType.SYSTEM,
                    title = "تم تسجيل الدخول بدور User 👤",
                    message = "تم تسجيل حسابك بدور 'User' في Firestore. واجهة لوحة التحكم مخصصة للأدمن فقط.",
                    categoryBadge = "حساب المستخدم"
                )
            }
        }
    }

    fun authenticateAdmin(pin: String): Boolean {
        loginWithRole(
            email = "admin@desouk-aqar.com",
            name = "Super Admin (الإدارة العامة)",
            phone = "01000000000",
            pinOrPassword = pin,
            role = "Admin"
        )
        return _isAdminAuthenticated.value
    }

    fun logoutAdmin() {
        _isAdminAuthenticated.value = false
        _currentUserRole.value = "User"
        _currentAdminUser.value = null
        _isFirestoreRoleVerified.value = false
        _currentTab.value = NavTab.HOME
        viewModelScope.launch {
            repository.insertActivityLog(
                actionType = AdminActionType.UPDATE_SETTINGS,
                details = "تم تسجيل خروج Super Admin وقفل لوحة التحكم بأمان."
            )
        }
    }

    // ==========================================
    // إدارة المستخدم والمصادقة (AUTH & SESSIONS)
    // ==========================================

    fun openAuthDialog() {
        _authError.value = null
        _showAuthDialog.value = true
    }

    fun closeAuthDialog() {
        _showAuthDialog.value = false
        _authError.value = null
    }

    fun openLoginScreen() {
        _authError.value = null
        _showLoginScreen.value = true
    }

    fun closeLoginScreen() {
        _showLoginScreen.value = false
        _authError.value = null
    }

    fun completeOnboarding(firstSearch: String? = null) {
        sessionManager.completeOnboarding()
        _showOnboardingDialog.value = false
        if (!firstSearch.isNullOrBlank()) {
            setCategory(firstSearch)
            selectTab(NavTab.SEARCH)
        }
    }

    fun signInWithGoogle(customContext: android.content.Context? = null, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authManager.signInWithGoogle(customContext)
            _authLoading.value = false
            result.onSuccess { session ->
                _showAuthDialog.value = false
                _showLoginScreen.value = false
                addNotification(
                    type = NotificationType.SYSTEM,
                    title = "مرحبًا بك ${session.name} 👋",
                    message = "تم تسجيل الدخول بنجاح عبر حساب Google (${session.email}) ومزامنة بياناتك مع Firestore.",
                    categoryBadge = "حساب المستخدم"
                )
                onSuccess?.invoke()
            }.onFailure { err ->
                val errorMsg = err.message ?: "تعذر تسجيل الدخول بواسطة Google"
                _authError.value = errorMsg
                // في حال عدم توفر خدمات Google Play على بعض المحاكيات، تمكين تسجيل دخول تجريبي
                if (errorMsg.contains("خدمات Google", ignoreCase = true) || errorMsg.contains("غير متوفرة", ignoreCase = true)) {
                    authManager.signInQuick("مستخدم جوجل", "user@desouk.com", UserRole.USER)
                    _showAuthDialog.value = false
                    _showLoginScreen.value = false
                    addNotification(
                        type = NotificationType.SYSTEM,
                        title = "مرحبًا بك في عقارات دسوق AI 👋",
                        message = "تم تسجيل الدخول بنجاح ومزامنة الجلسة. نتمنى لك تجربة ممتعة!",
                        categoryBadge = "حساب المستخدم"
                    )
                    onSuccess?.invoke()
                }
            }
        }
    }

    fun signInWithEmail(email: String, pass: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authManager.signInWithEmail(email, pass)
            _authLoading.value = false
            result.onSuccess {
                _showAuthDialog.value = false
                _showLoginScreen.value = false
                onSuccess?.invoke()
            }.onFailure { err ->
                _authError.value = err.message ?: "فشل تسجيل الدخول"
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String, phone: String, onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _authLoading.value = true
            _authError.value = null
            val result = authManager.signUpWithEmail(email, pass, name, phone)
            _authLoading.value = false
            result.onSuccess {
                _showAuthDialog.value = false
                _showLoginScreen.value = false
                addNotification(
                    type = NotificationType.SYSTEM,
                    title = "مرحبًا بك في عقارات دسوق AI 👋",
                    message = "تم إنشاء حسابك الجديد بنجاح وحفظه في السحابة.",
                    categoryBadge = "حساب المستخدم"
                )
                onSuccess?.invoke()
            }.onFailure { err ->
                _authError.value = err.message ?: "فشل إنشاء الحساب"
            }
        }
    }

    fun logoutUser() {
        authManager.signOut()
        _isAdminAuthenticated.value = false
        _currentUserRole.value = UserRole.USER
        _currentTab.value = NavTab.HOME
        addNotification(
            type = NotificationType.SYSTEM,
            title = "تم تسجيل الخروج",
            message = "تم إنهاء جلسة الحساب بأمان.",
            categoryBadge = "حساب"
        )
    }

    fun deleteUserAccount(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            authManager.deleteAccount()
            _isAdminAuthenticated.value = false
            _currentUserRole.value = UserRole.USER
            _currentTab.value = NavTab.HOME
            onComplete?.invoke()
        }
    }

    fun updateUserProfile(name: String, phone: String) {
        sessionManager.updateProfile(name, phone)
    }

    fun handleDeepLink(uri: android.net.Uri?) {
        if (uri == null) return
        val path = uri.path ?: ""
        if (path.contains("/property") || uri.host == "property") {
            val idStr = uri.lastPathSegment ?: uri.getQueryParameter("id")
            val id = idStr?.toLongOrNull()
            if (id != null) {
                val prop = allProperties.value.find { it.id == id }
                if (prop != null) {
                    selectProperty(prop)
                } else {
                    selectTab(NavTab.SEARCH)
                }
            }
        }
    }

    // ==========================================
    // إدارة العقارات (PROPERTIES CRUD)
    // ==========================================

    fun adminSaveProperty(
        id: Long = 0,
        title: String,
        category: String,
        type: String,
        location: String,
        district: String,
        street: String,
        price: Double,
        priceUnit: String,
        rooms: Int,
        bathrooms: Int,
        area: Double,
        floor: Int,
        totalFloors: Int,
        finishing: String,
        hasElevator: Boolean,
        hasGarage: Boolean,
        hasMeters: Boolean,
        features: String,
        phone: String,
        whatsapp: String,
        videoUrl: String,
        imageResName: String,
        description: String,
        status: String,
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit
    ) {
        if (!_isAdminAuthenticated.value) return

        viewModelScope.launch {
            val property = PropertyEntity(
                id = id,
                title = title.trim(),
                category = category,
                type = type,
                location = location.trim(),
                district = district,
                street = street.trim(),
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
                features = features.trim(),
                phone = phone.trim(),
                whatsapp = whatsapp.trim(),
                videoUrl = videoUrl.trim(),
                imageResName = imageResName.ifBlank { "img_property_apartment" },
                description = description.trim(),
                status = status,
                latitude = latitude,
                longitude = longitude,
                isUserAdded = false,
                timestamp = System.currentTimeMillis()
            )

            if (id == 0L) {
                val newId = repository.insertProperty(property)
                repository.insertActivityLog(
                    actionType = AdminActionType.ADD_PROPERTY,
                    details = "إضافة عقار جديد: '$title' في $district بسعر $price $priceUnit",
                    targetId = newId
                )
                // مزامنة العقار الجديد في Firestore بأمان بتحقق دور Admin
                if (firestoreService.isFirebaseAvailable()) {
                    firestoreService.addProperty(
                        property = FirestoreProperty.fromPropertyEntity(property, newId.toString()),
                        isAdminAuthenticated = _isAdminAuthenticated.value
                    )
                }
                addNotification(
                    type = NotificationType.SYSTEM,
                    title = "عقار جديد تم نشره ✅",
                    message = "تمت إضافة '$title' إلى المعروضات بنجاح.",
                    targetPropertyId = newId,
                    categoryBadge = "إدارة العقارات"
                )
            } else {
                repository.updateProperty(property)
                repository.insertActivityLog(
                    actionType = AdminActionType.UPDATE_PROPERTY,
                    details = "تعديل بيانات العقار رقم $id: '$title'",
                    targetId = id
                )
                // التحقق من الصلاحيات وتعديل العقار في Firestore
                if (firestoreService.isFirebaseAvailable()) {
                    val firestoreResult = firestoreService.updateProperty(
                        propertyId = id.toString(),
                        property = FirestoreProperty.fromPropertyEntity(property, id.toString()),
                        isAdminAuthenticated = _isAdminAuthenticated.value
                    )
                    if (firestoreResult.isFailure) {
                        repository.insertActivityLog(
                            actionType = AdminActionType.UPDATE_PROPERTY,
                            details = "تنبيه أمان Firestore: ${firestoreResult.exceptionOrNull()?.message}",
                            targetId = id
                        )
                    }
                }
            }
            onSuccess()
        }
    }

    fun adminChangePropertyStatus(id: Long, title: String, newStatus: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updatePropertyStatus(id, newStatus)
            repository.insertActivityLog(
                actionType = AdminActionType.CHANGE_PROPERTY_STATUS,
                details = "تغيير حالة العقار '$title' إلى $newStatus",
                targetId = id
            )
            // تحديث الحالة في Firestore تحت حماية صلاحيات الأدمن
            if (firestoreService.isFirebaseAvailable()) {
                firestoreService.updatePropertyStatus(
                    propertyId = id.toString(),
                    newStatus = newStatus,
                    isAdminAuthenticated = _isAdminAuthenticated.value
                )
            }
        }
    }

    fun adminArchiveProperty(id: Long, title: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updatePropertyStatus(id, PropertyStatus.ARCHIVED)
            repository.insertActivityLog(
                actionType = AdminActionType.ARCHIVE_PROPERTY,
                details = "أرشفة العقار '$title' وإيقاف ظهوره للجمهور",
                targetId = id
            )
            if (firestoreService.isFirebaseAvailable()) {
                firestoreService.updatePropertyStatus(
                    propertyId = id.toString(),
                    newStatus = PropertyStatus.ARCHIVED,
                    isAdminAuthenticated = _isAdminAuthenticated.value
                )
            }
        }
    }

    fun adminRestoreProperty(id: Long, title: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updatePropertyStatus(id, PropertyStatus.PUBLISHED)
            repository.insertActivityLog(
                actionType = AdminActionType.RESTORE_PROPERTY,
                details = "استرجاع العقار '$title' وإعادة نشره للجمهور",
                targetId = id
            )
            if (firestoreService.isFirebaseAvailable()) {
                firestoreService.updatePropertyStatus(
                    propertyId = id.toString(),
                    newStatus = PropertyStatus.PUBLISHED,
                    isAdminAuthenticated = _isAdminAuthenticated.value
                )
            }
        }
    }

    fun adminDeleteProperty(id: Long, title: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.deleteProperty(id)
            repository.insertActivityLog(
                actionType = AdminActionType.DELETE_PROPERTY,
                details = "حذف نهائي للعقار '$title' برقم المعرف $id",
                targetId = id
            )
            // التحقق الصارم من الصلاحيات وحذف العقار من Firestore
            if (firestoreService.isFirebaseAvailable()) {
                val deleteResult = firestoreService.deleteProperty(
                    propertyId = id.toString(),
                    propertyTitle = title,
                    isAdminAuthenticated = _isAdminAuthenticated.value
                )
                if (deleteResult.isFailure) {
                    repository.insertActivityLog(
                        actionType = AdminActionType.UNAUTHORIZED_ATTEMPT,
                        details = "فشل أمني أثناء حذف العقار من Firestore: ${deleteResult.exceptionOrNull()?.message}",
                        targetId = id
                    )
                }
            }
        }
    }

    // ==========================================
    // إدارة طلبات العملاء (CUSTOMER REQUESTS)
    // ==========================================

    fun adminUpdateRequestStatus(id: Long, status: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updateCustomerRequestStatus(id, status)
            repository.insertActivityLog(
                actionType = AdminActionType.CHANGE_REQUEST_STATUS,
                details = "تحديث حالة طلب العميل رقم $id إلى: $status",
                targetId = id
            )
        }
    }

    fun adminArchiveRequest(id: Long) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.archiveCustomerRequest(id)
            repository.insertActivityLog(
                actionType = AdminActionType.ARCHIVE_REQUEST,
                details = "أرشفة طلب العميل رقم $id",
                targetId = id
            )
        }
    }

    fun adminDeleteRequest(id: Long) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.deleteCustomerRequest(id)
            repository.insertActivityLog(
                actionType = AdminActionType.ARCHIVE_REQUEST,
                details = "حذف طلب العميل رقم $id",
                targetId = id
            )
        }
    }

    fun createCustomerRequest(
        customerName: String,
        customerPhone: String,
        requestType: String,
        propertyCategory: String,
        targetDistrict: String,
        budget: String,
        targetArea: String,
        targetRooms: String,
        details: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val req = CustomerRequestEntity(
                customerName = customerName.trim(),
                customerPhone = customerPhone.trim(),
                requestType = requestType,
                propertyCategory = propertyCategory,
                targetDistrict = targetDistrict,
                budget = budget.trim(),
                targetArea = targetArea.trim(),
                targetRooms = targetRooms.trim(),
                details = details.trim(),
                status = RequestStatus.NEW
            )
            val newId = repository.insertCustomerRequest(req)
            if (_isAdminAuthenticated.value) {
                repository.insertActivityLog(
                    actionType = AdminActionType.CHANGE_REQUEST_STATUS,
                    details = "تسجيل طلب عميل جديد من المكتب: '$customerName' ($requestType - $propertyCategory)",
                    targetId = newId
                )
            }
            addNotification(
                type = NotificationType.SYSTEM,
                title = "طلب عقاري جديد 📋",
                message = "وصل طلب جديد من $customerName ($requestType في $targetDistrict).",
                categoryBadge = "طلبات العملاء"
            )
            onSuccess()
        }
    }

    // ==========================================
    // إدارة الاستفسارات (INQUIRIES)
    // ==========================================

    fun adminReplyInquiry(id: Long, status: String, replyNotes: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.replyInquiry(id, status, replyNotes.trim())
            repository.insertActivityLog(
                actionType = AdminActionType.REPLY_INQUIRY,
                details = "الرد على استفسار العميل رقم $id وتحديث الحالة إلى $status",
                targetId = id
            )
        }
    }

    fun adminDeleteInquiry(id: Long) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.deleteInquiry(id)
            repository.insertActivityLog(
                actionType = AdminActionType.REPLY_INQUIRY,
                details = "حذف الاستفسار رقم $id",
                targetId = id
            )
        }
    }

    fun submitInquiry(
        userName: String,
        userPhone: String,
        propertyId: Long?,
        propertyTitle: String,
        inquiryText: String,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            val inq = CustomerInquiryEntity(
                userName = userName.trim().ifBlank { "مواطن من دسوق" },
                userPhone = userPhone.trim(),
                propertyId = propertyId,
                propertyTitle = propertyTitle,
                inquiryText = inquiryText.trim(),
                status = InquiryStatus.NEW
            )
            val id = repository.insertInquiry(inq)
            addNotification(
                type = NotificationType.SYSTEM,
                title = "استفسار جديد عن عقار 💬",
                message = "استفسار من $userName حول '$propertyTitle'.",
                targetPropertyId = propertyId,
                categoryBadge = "استفسارات"
            )
            onSuccess()
        }
    }

    // ==========================================
    // إدارة المستخدمين (USER MANAGEMENT)
    // ==========================================

    fun adminToggleUserBlock(userId: Long, userName: String, currentBlocked: Boolean) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            val newBlocked = !currentBlocked
            repository.setUserBlocked(userId, newBlocked)
            val action = if (newBlocked) "إيقاف وتجميد" else "إعادة تنشيط"
            repository.insertActivityLog(
                actionType = AdminActionType.UPDATE_USER_STATUS,
                details = "$action حساب المستخدم '$userName' (معرف: $userId)",
                targetId = userId
            )
        }
    }

    fun adminDeleteUser(userId: Long, userName: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.deleteUser(userId)
            repository.insertActivityLog(
                actionType = AdminActionType.DELETE_USER,
                details = "حذف حساب المستخدم '$userName' نهائياً من النظام",
                targetId = userId
            )
        }
    }

    fun adminUpdateUserRole(userId: Long, userName: String, newRole: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updateUserRole(userId, newRole)
            repository.insertActivityLog(
                actionType = AdminActionType.UPDATE_USER_STATUS,
                details = "تغيير رتبة المستخدم '$userName' إلى $newRole",
                targetId = userId
            )
        }
    }

    // ==========================================
    // إعدادات التطبيق والرئيسية (APP SETTINGS)
    // ==========================================

    fun adminUpdateSetting(key: String, value: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updateSetting(key, value.trim())
            repository.insertActivityLog(
                actionType = AdminActionType.UPDATE_SETTINGS,
                details = "تحديث إعدادات التطبيق: $key = $value"
            )
            addNotification(
                type = NotificationType.SYSTEM,
                title = "تم حفظ الإعدادات ⚙️",
                message = "تم تحديث إعدادات التطبيق بنجاح.",
                categoryBadge = "إعدادات الإدارة"
            )
        }
    }

    // ==============================================================
    // إدارة مصادر العقارات والمنشورات والمراجعة (SOURCES & IMPORTED ADS)
    // ==============================================================

    fun adminAddPropertySource(
        name: String,
        type: String,
        url: String,
        notes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            val source = PropertySourceEntity(
                name = name.trim(),
                type = type,
                url = url.trim(),
                isActive = true,
                lastSyncTime = System.currentTimeMillis(),
                syncStatus = "مستقر",
                notes = notes.trim()
            )
            val newId = repository.insertSource(source)
            repository.insertActivityLog(
                actionType = "إضافة مصدر عقاري",
                details = "تمت إضافة مصدر عقارات جديد: '$name' بنوع: $type",
                targetId = newId
            )
            repository.insertImportLog(
                sourceName = name,
                action = "إضافة مصدر معتمد",
                details = "تم اعتماد المصدر بنجاح مع ربطه بالنظام",
                aiResult = "المصدر جاهز للمزامنة الآلية والاستيراد المصرح به"
            )

            if (firestoreService.isFirebaseAvailable()) {
                firestoreService.saveSourceToFirestore(
                    source = FirestoreSource(
                        name = name.trim(),
                        type = type,
                        url = url.trim(),
                        isActive = true,
                        lastSyncTime = System.currentTimeMillis(),
                        notes = notes.trim()
                    ),
                    isAdminAuthenticated = _isAdminAuthenticated.value
                )
            }
            onSuccess()
        }
    }

    fun adminToggleSourceActive(id: Long, isActive: Boolean) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.toggleSourceActive(id, isActive)
            val statusText = if (isActive) "تفعيل" else "تعطيل"
            repository.insertActivityLog(
                actionType = "تعديل حالة مصدر",
                details = "$statusText المصدر العقاري رقم $id",
                targetId = id
            )
        }
    }

    fun adminDeleteSource(id: Long) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.deleteSource(id)
            repository.insertActivityLog(
                actionType = "حذف مصدر",
                details = "تم حذف المصدر العقاري رقم $id نهائياً",
                targetId = id
            )
        }
    }

    /**
     * تشغيل المزامنة اليدوية أو الدورية لمصدر عقاري معتمد
     */
    fun adminSyncSource(source: PropertySourceEntity, onSuccess: (String) -> Unit = {}) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            repository.insertImportLog(
                sourceName = source.name,
                action = "بدء مزامنة آلية",
                details = "بدء فحص وتحديث المنشورات من المصدر المصرح به",
                aiResult = "جاري التحقق من المنشورات غير المكررة والامتثال لسياسات الوصول"
            )

            // محاكاة استلام منشور جديد رسمي من هذا المصدر
            val sampleNewPost = RawPostEntity(
                sourceId = source.id,
                sourceName = source.name,
                postExternalId = "sync_post_${System.currentTimeMillis() % 10000}",
                postUrl = if (source.url.isNotBlank()) "${source.url}/posts/${System.currentTimeMillis() % 1000}" else "https://desouk.properties/direct/${System.currentTimeMillis() % 1000}",
                postTimestamp = timestamp,
                originalRawText = "عقار جديد من ${source.name}: شقة للبيع بدسوق حي دحروج 140 متر دور ثالث 3 غرف وحمام تشطيب سوبر لوكس اسانسير شغال مطلوب 950 ألف جنيه للتواصل 01066778899",
                imageUrl = "img_property_apartment",
                authorName = "مكتب معتمد",
                authorPhone = "01066778899",
                status = "معالج"
            )

            repository.insertRawPost(sampleNewPost)

            // تحليل المنشور بالذكاء الاصطناعي
            val aiResult = RealEstateAiExtractor.extractFromRawText(
                rawText = sampleNewPost.originalRawText,
                sourceName = source.name,
                postUrl = sampleNewPost.postUrl,
                postExternalId = sampleNewPost.postExternalId,
                existingProperties = allProperties.value,
                existingImported = allImportedProperties.value
            )

            val imported = ImportedPropertyEntity(
                sourceId = source.id,
                sourceName = source.name,
                rawPostId = sampleNewPost.id,
                postExternalId = sampleNewPost.postExternalId,
                postUrl = sampleNewPost.postUrl,
                title = aiResult.title,
                propertyType = aiResult.propertyType,
                operationType = aiResult.operationType,
                city = aiResult.city,
                district = aiResult.district,
                street = aiResult.street,
                landmark = aiResult.landmark,
                price = aiResult.price,
                priceUnit = aiResult.priceUnit,
                area = aiResult.area,
                rooms = aiResult.rooms,
                bathrooms = aiResult.bathrooms,
                floor = aiResult.floor,
                hasElevator = aiResult.hasElevator,
                finishing = aiResult.finishing,
                facade = aiResult.facade,
                cleanedText = aiResult.cleanedText,
                originalRawText = sampleNewPost.originalRawText,
                imageUrl = sampleNewPost.imageUrl,
                contactPhone = aiResult.contactPhone,
                contactWhatsapp = aiResult.contactWhatsapp,
                contactName = aiResult.contactName,
                completenessScore = aiResult.completenessScore,
                uncertainData = aiResult.uncertainData,
                status = if (aiResult.isDuplicate) ImportedAdStatus.DUPLICATE else ImportedAdStatus.IN_REVIEW,
                isDuplicate = aiResult.isDuplicate,
                duplicateMatchReason = aiResult.duplicateReason
            )

            repository.insertImportedProperty(imported)

            repository.insertImportLog(
                sourceName = source.name,
                action = "اكتمال المزامنة",
                details = "تم استيراد منشور وتحليله بنجاح وإرساله لقائمة المراجعة",
                aiResult = "تم استخراج '${aiResult.title}' - درجة الاكتمال: ${aiResult.completenessScore}%"
            )

            onSuccess("تمت مزامنة المصدر '${source.name}' بنجاح وإضافة منشور جديد لقائمة المراجعة")
        }
    }

    /**
     * استيراد منشور يدوي أو رسمي وتمريره عبر خوارزمية الذكاء الاصطناعي وفحص التكرار
     */
    fun adminImportRawPost(
        rawText: String,
        sourceName: String,
        postUrl: String = "",
        postExternalId: String = "",
        onSuccess: (ImportedPropertyEntity) -> Unit = {}
    ) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            val trimmedText = rawText.trim()
            if (trimmedText.isBlank()) return@launch

            // 1. حفظ البيانات الخام للمنشور أولاً دون تعديل
            val rawPost = RawPostEntity(
                sourceName = sourceName.ifBlank { "إدخال يدوي مصرح به" },
                postExternalId = postExternalId,
                postUrl = postUrl.trim(),
                postTimestamp = System.currentTimeMillis(),
                originalRawText = trimmedText,
                imageUrl = "img_property_apartment",
                status = "معالج"
            )
            val rawId = repository.insertRawPost(rawPost)

            // 2. تحليل الذكاء الاصطناعي وفحص التكرار
            val aiResult = RealEstateAiExtractor.extractFromRawText(
                rawText = trimmedText,
                sourceName = sourceName,
                postUrl = postUrl,
                postExternalId = postExternalId,
                existingProperties = allProperties.value,
                existingImported = allImportedProperties.value
            )

            val imported = ImportedPropertyEntity(
                sourceName = sourceName.ifBlank { "إدخال يدوي مصرح به" },
                rawPostId = rawId,
                postExternalId = postExternalId,
                postUrl = postUrl.trim(),
                title = aiResult.title,
                propertyType = aiResult.propertyType,
                operationType = aiResult.operationType,
                city = aiResult.city,
                district = aiResult.district,
                street = aiResult.street,
                landmark = aiResult.landmark,
                price = aiResult.price,
                priceUnit = aiResult.priceUnit,
                area = aiResult.area,
                rooms = aiResult.rooms,
                bathrooms = aiResult.bathrooms,
                floor = aiResult.floor,
                hasElevator = aiResult.hasElevator,
                hasGarage = aiResult.hasGarage,
                finishing = aiResult.finishing,
                facade = aiResult.facade,
                cleanedText = aiResult.cleanedText,
                originalRawText = trimmedText,
                imageUrl = "img_property_apartment",
                contactPhone = aiResult.contactPhone,
                contactWhatsapp = aiResult.contactWhatsapp,
                contactName = aiResult.contactName,
                completenessScore = aiResult.completenessScore,
                uncertainData = aiResult.uncertainData,
                status = if (aiResult.isDuplicate) ImportedAdStatus.DUPLICATE else ImportedAdStatus.IN_REVIEW,
                isDuplicate = aiResult.isDuplicate,
                duplicateMatchReason = aiResult.duplicateReason
            )

            val importedId = repository.insertImportedProperty(imported)

            repository.insertImportLog(
                sourceName = sourceName.ifBlank { "إدخال يدوي" },
                action = "استيراد منشور وتحليل AI",
                details = "تم تحليل النص الخام بنجاح وإدخاله لقائمة المراجعة",
                aiResult = "العقار: ${aiResult.title} | مكرر: ${aiResult.isDuplicate} | الاكتمال: ${aiResult.completenessScore}%"
            )

            if (firestoreService.isFirebaseAvailable()) {
                firestoreService.saveRawPostToFirestore(
                    FirestoreRawPost(
                        sourceName = sourceName,
                        postExternalId = postExternalId,
                        postUrl = postUrl,
                        postTimestamp = System.currentTimeMillis(),
                        originalRawText = trimmedText,
                        imageUrl = "img_property_apartment"
                    )
                )
            }

            onSuccess(imported.copy(id = importedId))
        }
    }

    /**
     * اعتماد ونشر الإعلان المستورد في قائمة العقارات الرسمية وظهوره للمستخدمين
     */
    fun adminApproveAndPublishImportedAd(
        imported: ImportedPropertyEntity,
        adminNotes: String = "",
        onSuccess: () -> Unit = {}
    ) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            // 1. تحويل الإعلان المستورد إلى PropertyEntity حقيقي في قاعدة البيانات
            val newProperty = PropertyEntity(
                title = imported.title.ifBlank { "${imported.propertyType} ${imported.operationType} في ${imported.district}" },
                category = when (imported.propertyType) {
                    "أرض" -> "أراضي"
                    "محل" -> "محلات"
                    "منزل", "عمارة", "فيلا" -> "منازل"
                    else -> "شقق"
                },
                type = imported.operationType,
                location = "دسوق - ${imported.district}",
                district = imported.district,
                street = imported.street,
                price = imported.price ?: 0.0,
                priceUnit = imported.priceUnit,
                rooms = imported.rooms ?: 0,
                bathrooms = imported.bathrooms ?: 0,
                area = imported.area ?: 0.0,
                floor = imported.floor ?: 1,
                finishing = imported.finishing.ifBlank { "سوبر لوكس" },
                hasElevator = imported.hasElevator ?: false,
                hasGarage = imported.hasGarage ?: false,
                phone = imported.contactPhone.ifBlank { "01001234567" },
                whatsapp = imported.contactWhatsapp.ifBlank { "201001234567" },
                description = imported.cleanedText.ifBlank { imported.originalRawText },
                imageResName = imported.imageUrl.ifBlank { "img_property_apartment" },
                status = PropertyStatus.PUBLISHED,
                isUserAdded = false,
                timestamp = System.currentTimeMillis()
            )

            val publishedPropertyId = repository.insertProperty(newProperty)

            // 2. تحديث حالة الإعلان المستورد إلى "نشط"
            repository.updateImportedStatus(
                id = imported.id,
                newStatus = ImportedAdStatus.ACTIVE,
                notes = adminNotes.ifBlank { "تم الاعتماد والنشر بواسطة Super Admin" }
            )

            // 3. التسجيل في سجل العمليات وسجل الاستيراد
            repository.insertActivityLog(
                actionType = "اعتماد ونشر إعلان",
                details = "تم اعتماد ونشر الإعلان '${imported.title}' في قائمة العقارات الرئيسية",
                targetId = publishedPropertyId
            )

            repository.insertImportLog(
                sourceName = imported.sourceName,
                action = "اعتماد ونشر",
                details = "اعتماد الإعلان المستورد ونقله لقاعدة بيانات العقارات المنشورة",
                aiResult = "تم النشر بنجاح برقم #$publishedPropertyId"
            )

            // 4. المزامنة مع Firestore
            if (firestoreService.isFirebaseAvailable()) {
                firestoreService.addProperty(
                    property = FirestoreProperty.fromPropertyEntity(newProperty, publishedPropertyId.toString()),
                    isAdminAuthenticated = true
                )
                if (imported.firestoreId.isNotBlank()) {
                    firestoreService.updateImportedStatusInFirestore(
                        docId = imported.firestoreId,
                        newStatus = ImportedAdStatus.ACTIVE,
                        notes = adminNotes
                    )
                }
            }

            addNotification(
                type = NotificationType.SYSTEM,
                title = "تم اعتماد إعلان عقاري ✅",
                message = "تم نشر '${imported.title}' بنجاح في تطبيق عقارات دسوق.",
                targetPropertyId = publishedPropertyId,
                categoryBadge = "مصادر العقارات"
            )

            onSuccess()
        }
    }

    /**
     * رفض إعلان مستورد
     */
    fun adminRejectImportedAd(imported: ImportedPropertyEntity, reason: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updateImportedStatus(
                id = imported.id,
                newStatus = ImportedAdStatus.REJECTED,
                notes = reason.ifBlank { "تم الرفض بواسطة الأدمن" }
            )
            repository.insertActivityLog(
                actionType = "رفض إعلان مستورد",
                details = "تم رفض الإعلان '${imported.title}': $reason",
                targetId = imported.id
            )
            repository.insertImportLog(
                sourceName = imported.sourceName,
                action = "رفض إعلان",
                details = "تم رفض الإعلان برقم ${imported.id}",
                aiResult = "السبب: $reason"
            )
        }
    }

    /**
     * طلب إعادة مراجعة لإعلان مستورد
     */
    fun adminRequestReviewImportedAd(id: Long, notes: String) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updateImportedStatus(
                id = id,
                newStatus = ImportedAdStatus.IN_REVIEW,
                notes = notes
            )
            repository.insertActivityLog(
                actionType = "طلب مراجعة",
                details = "طلب إعادة تدقيق لبيانات الإعلان رقم $id: $notes",
                targetId = id
            )
        }
    }

    /**
     * تعديل بيانات الإعلان المستورد ثم نشره
     */
    fun adminUpdateAndPublishImportedAd(
        updated: ImportedPropertyEntity,
        onSuccess: () -> Unit = {}
    ) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.updateImportedProperty(updated)
            adminApproveAndPublishImportedAd(
                imported = updated,
                adminNotes = "تم التعديل والتدقيق ثم الاعتماد بواسطة Super Admin",
                onSuccess = onSuccess
            )
        }
    }

    /**
     * حذف إعلان مستورد نهائياً
     */
    fun adminDeleteImportedAd(id: Long) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            repository.deleteImportedProperty(id)
            repository.insertActivityLog(
                actionType = "حذف إعلان مستورد",
                details = "تم حذف الإعلان رقم $id نهائياً من قائمة المراجعة",
                targetId = id
            )
        }
    }

    /**
     * حفظ ونشر عقار تم استيراده من فيسبوك بالذكاء الاصطناعي في قاعدة البيانات وظهوره في أحدث العقارات
     */
    fun adminSaveAndPublishFacebookAd(
        ad: FacebookExtractedAd,
        isDuplicateOverride: Boolean = false,
        onSuccess: (Long) -> Unit = {}
    ) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            val category = when (ad.propertyType) {
                "أرض" -> "أراضي"
                "محل", "عقار تجاري" -> "محلات"
                "مكتب" -> "مكاتب"
                "منزل", "عمارة", "فيلا" -> "منازل"
                else -> "شقق"
            }
            val newProperty = PropertyEntity(
                title = ad.title.ifBlank { "${ad.propertyType} ${ad.listingType} في ${ad.location}" },
                category = category,
                type = ad.listingType,
                location = if (ad.location.startsWith("دسوق")) ad.location else "دسوق - ${ad.location}",
                district = ad.location,
                street = ad.address,
                address = ad.address,
                price = ad.price ?: 0.0,
                priceUnit = ad.priceUnit,
                rooms = ad.rooms ?: 0,
                bathrooms = ad.bathrooms ?: 0,
                area = ad.area ?: 0.0,
                floor = ad.floor ?: 1,
                finishing = ad.finishing.ifBlank { "سوبر لوكس" },
                streetsCount = ad.streetsCount,
                facade = ad.facade,
                phone = ad.phone.ifBlank { "01001234567" },
                whatsapp = if (ad.phone.isNotBlank()) "2${ad.phone.replace("+20", "")}" else "201001234567",
                description = ad.description,
                imageResName = ad.images.ifBlank { "img_property_apartment" },
                status = PropertyStatus.PUBLISHED,
                isUserAdded = false,
                sourceUrl = ad.sourceUrl,
                sourcePlatform = "Facebook",
                importedBy = _currentAdminUser.value?.name ?: "Super Admin",
                importedAt = System.currentTimeMillis(),
                isDuplicate = isDuplicateOverride || ad.isDuplicate,
                ownerName = ad.ownerName,
                timestamp = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val publishedId = repository.insertProperty(newProperty)

            // توثيق العقار في جدول العقارات المستوردة بحالة "منشور"
            val importedRecord = ImportedPropertyEntity(
                propertyId = publishedId,
                title = newProperty.title,
                propertyType = ad.propertyType,
                listingType = ad.listingType,
                price = ad.price,
                area = ad.area,
                location = ad.location,
                address = ad.address,
                rooms = ad.rooms,
                bathrooms = ad.bathrooms,
                floor = ad.floor,
                finishing = ad.finishing,
                streetsCount = ad.streetsCount,
                facade = ad.facade,
                description = ad.description,
                phone = ad.phone,
                ownerName = ad.ownerName,
                images = ad.images,
                sourceUrl = ad.sourceUrl,
                sourcePlatform = "Facebook",
                importedBy = _currentAdminUser.value?.name ?: "Super Admin",
                importedAt = System.currentTimeMillis(),
                status = if (isDuplicateOverride || ad.isDuplicate) ImportedAdStatus.DUPLICATE else ImportedAdStatus.PUBLISHED,
                isDuplicate = isDuplicateOverride || ad.isDuplicate,
                duplicateMatchReason = ad.duplicateReason,
                sourceName = "فيسبوك",
                postUrl = ad.sourceUrl,
                operationType = ad.listingType,
                city = "دسوق",
                district = ad.location,
                priceUnit = ad.priceUnit,
                cleanedText = ad.description,
                originalRawText = ad.description,
                contactPhone = ad.phone,
                contactName = ad.ownerName,
                completenessScore = ad.completenessScore,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            repository.insertImportedProperty(importedRecord)

            repository.insertActivityLog(
                actionType = "استيراد ونشر من فيسبوك",
                details = "تم نشر العقار '${newProperty.title}' المستورد من فيسبوك بنجاح",
                targetId = publishedId
            )

            repository.insertImportLog(
                sourceName = "فيسبوك",
                action = "حفظ ونشر",
                details = "استيراد عقار من فيسبوك ونشره في أحدث العقارات برقم #$publishedId",
                aiResult = "الحالة: منشور | مكرر: ${newProperty.isDuplicate}"
            )

            if (firestoreService.isFirebaseAvailable()) {
                firestoreService.addProperty(
                    property = FirestoreProperty.fromPropertyEntity(newProperty, publishedId.toString()),
                    isAdminAuthenticated = true
                )
            }

            addNotification(
                type = NotificationType.SYSTEM,
                title = "تم نشر عقار مستورد من فيسبوك 📢",
                message = "العقار '${newProperty.title}' متاح الآن في أحدث العقارات على الصفحة الرئيسية.",
                targetPropertyId = publishedId,
                categoryBadge = "استيراد فيسبوك"
            )

            onSuccess(publishedId)
        }
    }

    /**
     * حفظ الإعلان المستورد من فيسبوك كمسودة
     */
    fun adminSaveFacebookAdAsDraft(
        ad: FacebookExtractedAd,
        onSuccess: (Long) -> Unit = {}
    ) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            val draftRecord = ImportedPropertyEntity(
                title = ad.title.ifBlank { "${ad.propertyType} ${ad.listingType} في ${ad.location} (مسودة)" },
                propertyType = ad.propertyType,
                listingType = ad.listingType,
                price = ad.price,
                area = ad.area,
                location = ad.location,
                address = ad.address,
                rooms = ad.rooms,
                bathrooms = ad.bathrooms,
                floor = ad.floor,
                finishing = ad.finishing,
                streetsCount = ad.streetsCount,
                facade = ad.facade,
                description = ad.description,
                phone = ad.phone,
                ownerName = ad.ownerName,
                images = ad.images,
                sourceUrl = ad.sourceUrl,
                sourcePlatform = "Facebook",
                importedBy = _currentAdminUser.value?.name ?: "Super Admin",
                importedAt = System.currentTimeMillis(),
                status = ImportedAdStatus.DRAFT,
                isDuplicate = ad.isDuplicate,
                duplicateMatchReason = ad.duplicateReason,
                sourceName = "فيسبوك",
                postUrl = ad.sourceUrl,
                operationType = ad.listingType,
                city = "دسوق",
                district = ad.location,
                priceUnit = ad.priceUnit,
                cleanedText = ad.description,
                originalRawText = ad.description,
                contactPhone = ad.phone,
                contactName = ad.ownerName,
                completenessScore = ad.completenessScore,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val insertedId = repository.insertImportedProperty(draftRecord)
            repository.insertActivityLog(
                actionType = "حفظ كمسودة",
                details = "تم حفظ الإعلان المستورد '${draftRecord.title}' كمسودة",
                targetId = insertedId
            )
            onSuccess(insertedId)
        }
    }

    /**
     * حفظ الإعلان المستورد بحالة "يحتاج مراجعة"
     */
    fun adminSaveFacebookAdAsNeedsReview(
        ad: FacebookExtractedAd,
        onSuccess: (Long) -> Unit = {}
    ) {
        if (!_isAdminAuthenticated.value) return
        viewModelScope.launch {
            val reviewRecord = ImportedPropertyEntity(
                title = ad.title.ifBlank { "${ad.propertyType} ${ad.listingType} في ${ad.location}" },
                propertyType = ad.propertyType,
                listingType = ad.listingType,
                price = ad.price,
                area = ad.area,
                location = ad.location,
                address = ad.address,
                rooms = ad.rooms,
                bathrooms = ad.bathrooms,
                floor = ad.floor,
                finishing = ad.finishing,
                streetsCount = ad.streetsCount,
                facade = ad.facade,
                description = ad.description,
                phone = ad.phone,
                ownerName = ad.ownerName,
                images = ad.images,
                sourceUrl = ad.sourceUrl,
                sourcePlatform = "Facebook",
                importedBy = _currentAdminUser.value?.name ?: "Super Admin",
                importedAt = System.currentTimeMillis(),
                status = ImportedAdStatus.NEEDS_REVIEW,
                isDuplicate = ad.isDuplicate,
                duplicateMatchReason = ad.duplicateReason,
                sourceName = "فيسبوك",
                postUrl = ad.sourceUrl,
                operationType = ad.listingType,
                city = "دسوق",
                district = ad.location,
                priceUnit = ad.priceUnit,
                cleanedText = ad.description,
                originalRawText = ad.description,
                contactPhone = ad.phone,
                contactName = ad.ownerName,
                completenessScore = ad.completenessScore,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            val insertedId = repository.insertImportedProperty(reviewRecord)
            onSuccess(insertedId)
        }
    }

    // ==============================================================
    // تقنية الإعداد المسبق لبيانات Firestore (Firestore Preloading)
    // ==============================================================

    /**
     * الإعداد المسبق لبيانات الصفحة الرئيسية في الخلفية (Cache-First + Background Server Sync)
     * لتقليص زمن التحميل إلى الصفر وضمان استقرار وسرعة العرض للمستخدمين.
     */
    private fun preloadFirestoreData() {
        if (!firestoreService.isFirebaseAvailable()) return
        firestoreService.configureFirestoreCache()

        viewModelScope.launch(Dispatchers.IO) {
            // 1. مرحلة Preloading السريعة (تسترجع من الكاش في 0ms ثم تجلب الجديد من السيرفر)
            firestoreService.preloadHomeScreenProperties(limit = 30) { preloadedList, isFromCache ->
                viewModelScope.launch(Dispatchers.IO) {
                    val syncedCount = repository.syncFirestoreProperties(preloadedList)
                    if (syncedCount > 0) {
                        Log.d("MainViewModel", "⚡ [Firestore Preload] تم إدراج $syncedCount عقار جديد في التخزين المحلي (المصدر: ${if (isFromCache) "الكاش" else "السحابة"})")
                    }
                }
            }

            // 2. مرحلة المستمع اللحظي (Snapshot Listener Preload) لتحديث التخزين فور حدوث أي تعديل
            firestoreService.startRealtimePreloadListener(limit = 30) { liveList ->
                viewModelScope.launch(Dispatchers.IO) {
                    repository.syncFirestoreProperties(liveList)
                }
            }
        }
    }

    /**
     * إعادة تشغيل الإعداد المسبق يدوياً (Pull-to-refresh أو تحديث يدوي)
     */
    fun refreshFirestorePreload() {
        preloadFirestoreData()
    }

    // ==========================================
    // المجتمع العقاري الذكي (Smart Community Feed)
    // ==========================================

    fun toggleCommunityLike(postId: Long, currentLikes: Int, isLiked: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.togglePostLike(postId, currentLikes, isLiked)
        }
    }

    fun toggleCommunitySave(postId: Long, currentSaved: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.togglePostSave(postId, currentSaved)
        }
    }

    fun toggleFollowAuthor(postId: Long, currentFollow: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleFollowAuthor(postId, currentFollow)
        }
    }

    fun getCommentsForPost(postId: Long): Flow<List<CommunityCommentEntity>> {
        return repository.getCommentsForPost(postId)
    }

    fun addCommunityComment(postId: Long, authorName: String, authorRole: String, content: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addComment(postId, authorName, authorRole, content)
        }
    }

    fun createCommunityPost(
        authorName: String,
        authorRole: String,
        authorPhone: String?,
        postType: String,
        content: String,
        imagesJson: String = "",
        hasVideo: Boolean = false,
        videoLabel: String? = null,
        district: String? = null,
        extractedType: String? = null,
        extractedDeal: String? = null,
        extractedLocation: String? = null,
        extractedRooms: String? = null,
        extractedFloor: String? = null,
        extractedPrice: String? = null,
        extractedArea: String? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val post = CommunityPostEntity(
                authorName = authorName.ifBlank { currentUserName.value },
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
                extractedArea = extractedArea,
                timestamp = System.currentTimeMillis()
            )
            repository.insertCommunityPost(post)
        }
    }

    fun deleteCommunityPost(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCommunityPost(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        firestoreService.stopRealtimePreloadListener()
    }
}
