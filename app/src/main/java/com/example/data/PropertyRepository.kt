package com.example.data

import kotlinx.coroutines.flow.Flow

class PropertyRepository(
    private val propertyDao: PropertyDao,
    private val reviewDao: PropertyReviewDao? = null,
    private val requestDao: CustomerRequestDao? = null,
    private val inquiryDao: CustomerInquiryDao? = null,
    private val userDao: AppUserDao? = null,
    private val logDao: AdminActivityLogDao? = null,
    private val settingDao: AppSettingDao? = null,
    private val sourceDao: PropertySourceDao? = null,
    private val rawPostDao: RawPostDao? = null,
    private val importedDao: ImportedPropertyDao? = null,
    private val importLogDao: ImportLogDao? = null,
    private val draftDao: PropertyDraftDao? = null,
    private val communityDao: CommunityDao? = null
) {

    val allProperties: Flow<List<PropertyEntity>> = propertyDao.getAllProperties()
    val favoriteProperties: Flow<List<PropertyEntity>> = propertyDao.getFavoriteProperties()
    val userProperties: Flow<List<PropertyEntity>> = propertyDao.getUserProperties()

    fun getPropertyById(id: Long): Flow<PropertyEntity?> {
        return propertyDao.getPropertyById(id)
    }

    suspend fun getPropertyByIdDirect(id: Long): PropertyEntity? {
        return propertyDao.getPropertyByIdDirect(id)
    }

    suspend fun getAllPropertiesDirect(): List<PropertyEntity> {
        return propertyDao.getAllPropertiesDirect()
    }

    suspend fun syncFirestoreProperties(firestoreList: List<FirestoreProperty>): Int {
        if (firestoreList.isEmpty()) return 0
        return try {
            val existing = propertyDao.getAllPropertiesDirect()
            val existingKeys = existing.map { "${it.title.trim()}_${it.district.trim()}" }.toSet()
            val newEntities = mutableListOf<PropertyEntity>()

            for (fp in firestoreList) {
                val key = "${fp.title.trim()}_${fp.district.trim()}"
                if (!existingKeys.contains(key)) {
                    newEntities.add(fp.toPropertyEntity())
                }
            }

            if (newEntities.isNotEmpty()) {
                propertyDao.insertAll(newEntities)
            }
            newEntities.size
        } catch (e: Exception) {
            0
        }
    }

    suspend fun insertProperty(property: PropertyEntity): Long {
        return propertyDao.insertProperty(property)
    }

    suspend fun updateProperty(property: PropertyEntity) {
        propertyDao.updateProperty(property)
    }

    suspend fun updatePropertyStatus(id: Long, status: String) {
        propertyDao.updatePropertyStatus(id, status)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        propertyDao.toggleFavorite(id, isFavorite)
    }

    suspend fun deleteProperty(id: Long) {
        propertyDao.deleteProperty(id)
    }

    // Reviews
    fun getReviewsForProperty(propertyId: Long): Flow<List<PropertyReviewEntity>> {
        return reviewDao?.getReviewsForProperty(propertyId) ?: kotlinx.coroutines.flow.flowOf(emptyList())
    }

    fun getAverageRating(propertyId: Long): Flow<Double?> {
        return reviewDao?.getAverageRating(propertyId) ?: kotlinx.coroutines.flow.flowOf(null)
    }

    suspend fun addReview(review: PropertyReviewEntity): Long {
        return reviewDao?.insertReview(review) ?: 0L
    }

    // Customer Requests
    val allCustomerRequests: Flow<List<CustomerRequestEntity>> =
        requestDao?.getAllRequests() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    val activeCustomerRequests: Flow<List<CustomerRequestEntity>> =
        requestDao?.getAllActiveRequests() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertCustomerRequest(request: CustomerRequestEntity): Long {
        return requestDao?.insertRequest(request) ?: 0L
    }

    suspend fun updateCustomerRequestStatus(id: Long, status: String) {
        requestDao?.updateRequestStatus(id, status)
    }

    suspend fun archiveCustomerRequest(id: Long) {
        requestDao?.archiveRequest(id)
    }

    suspend fun deleteCustomerRequest(id: Long) {
        requestDao?.deleteRequest(id)
    }

    // Inquiries
    val allInquiries: Flow<List<CustomerInquiryEntity>> =
        inquiryDao?.getAllInquiries() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertInquiry(inquiry: CustomerInquiryEntity): Long {
        return inquiryDao?.insertInquiry(inquiry) ?: 0L
    }

    suspend fun replyInquiry(id: Long, status: String, replyNotes: String) {
        inquiryDao?.replyInquiry(id, status, replyNotes)
    }

    suspend fun deleteInquiry(id: Long) {
        inquiryDao?.deleteInquiry(id)
    }

    // Users
    val allUsers: Flow<List<AppUserEntity>> =
        userDao?.getAllUsers() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun setUserBlocked(id: Long, isBlocked: Boolean) {
        userDao?.setUserBlocked(id, isBlocked)
    }

    suspend fun updateUserRole(id: Long, role: String) {
        userDao?.updateUserRole(id, role)
    }

    suspend fun deleteUser(id: Long) {
        userDao?.deleteUser(id)
    }

    // Activity Logs
    val allActivityLogs: Flow<List<AdminActivityLogEntity>> =
        logDao?.getAllLogs() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertActivityLog(actionType: String, details: String, targetId: Long? = null) {
        logDao?.insertLog(
            AdminActivityLogEntity(
                actionType = actionType,
                details = details,
                targetId = targetId
            )
        )
    }

    // Settings
    val allSettings: Flow<List<AppSettingEntity>> =
        settingDao?.getAllSettings() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun updateSetting(key: String, value: String) {
        settingDao?.setSetting(AppSettingEntity(key, value))
    }

    suspend fun getSettingDirect(key: String): String? {
        return settingDao?.getSettingDirect(key)
    }

    // Property Sources
    val allSources: Flow<List<PropertySourceEntity>> =
        sourceDao?.getAllSources() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    val activeSources: Flow<List<PropertySourceEntity>> =
        sourceDao?.getActiveSources() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertSource(source: PropertySourceEntity): Long {
        return sourceDao?.insertSource(source) ?: 0L
    }

    suspend fun updateSource(source: PropertySourceEntity) {
        sourceDao?.updateSource(source)
    }

    suspend fun toggleSourceActive(id: Long, isActive: Boolean) {
        sourceDao?.toggleActive(id, isActive)
    }

    suspend fun deleteSource(id: Long) {
        sourceDao?.deleteSource(id)
    }

    // Raw Posts
    val allRawPosts: Flow<List<RawPostEntity>> =
        rawPostDao?.getAllRawPosts() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertRawPost(post: RawPostEntity): Long {
        return rawPostDao?.insertRawPost(post) ?: 0L
    }

    suspend fun findExistingRawPost(url: String, externalId: String): RawPostEntity? {
        return rawPostDao?.findExistingPost(url, externalId)
    }

    // Imported Properties & Review Queue
    val allImportedProperties: Flow<List<ImportedPropertyEntity>> =
        importedDao?.getAllImportedProperties() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    val reviewQueue: Flow<List<ImportedPropertyEntity>> =
        importedDao?.getReviewQueue() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertImportedProperty(property: ImportedPropertyEntity): Long {
        return importedDao?.insert(property) ?: 0L
    }

    suspend fun updateImportedProperty(property: ImportedPropertyEntity) {
        importedDao?.update(property)
    }

    suspend fun updateImportedStatus(id: Long, newStatus: String, notes: String, adminName: String = "Super Admin") {
        importedDao?.updateStatus(id, newStatus, System.currentTimeMillis(), adminName, notes)
    }

    suspend fun deleteImportedProperty(id: Long) {
        importedDao?.delete(id)
    }

    // Import Logs
    val allImportLogs: Flow<List<ImportLogEntity>> =
        importLogDao?.getAllLogs() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertImportLog(sourceName: String, action: String, details: String, aiResult: String) {
        importLogDao?.insertLog(
            ImportLogEntity(
                sourceName = sourceName,
                action = action,
                details = details,
                aiResult = aiResult
            )
        )
    }

    // Property Draft (Auto-Save in Room DB)
    val currentDraft: Flow<PropertyDraftEntity?> =
        draftDao?.getDraft() ?: kotlinx.coroutines.flow.flowOf(null)

    suspend fun getDraftDirect(): PropertyDraftEntity? {
        return draftDao?.getDraftDirect()
    }

    suspend fun saveDraft(draft: PropertyDraftEntity) {
        draftDao?.saveDraft(draft)
    }

    suspend fun clearDraft() {
        draftDao?.clearDraft()
    }

    // ==========================================
    // المجتمع العقاري الذكي (Smart Community Feed)
    // ==========================================
    val allCommunityPosts: Flow<List<CommunityPostEntity>> =
        communityDao?.getAllPosts() ?: kotlinx.coroutines.flow.flowOf(emptyList())

    fun getCommentsForPost(postId: Long): Flow<List<CommunityCommentEntity>> =
        communityDao?.getCommentsForPost(postId) ?: kotlinx.coroutines.flow.flowOf(emptyList())

    suspend fun insertCommunityPost(post: CommunityPostEntity): Long {
        return communityDao?.insertPost(post) ?: 0L
    }

    suspend fun togglePostLike(postId: Long, currentLikes: Int, isLiked: Boolean) {
        val newLikes = if (isLiked) (currentLikes - 1).coerceAtLeast(0) else currentLikes + 1
        communityDao?.updateLikeStatus(postId, newLikes, !isLiked)
    }

    suspend fun togglePostSave(postId: Long, currentSaved: Boolean) {
        communityDao?.updateSaveStatus(postId, !currentSaved)
    }

    suspend fun toggleFollowAuthor(postId: Long, currentFollow: Boolean) {
        communityDao?.updateFollowStatus(postId, !currentFollow)
    }

    suspend fun addComment(postId: Long, authorName: String, authorRole: String, content: String): Long {
        val comment = CommunityCommentEntity(
            postId = postId,
            authorName = authorName,
            authorRole = authorRole,
            content = content,
            timestamp = System.currentTimeMillis()
        )
        val commentId = communityDao?.insertComment(comment) ?: 0L
        if (commentId > 0) {
            communityDao?.incrementCommentsCount(postId)
        }
        return commentId
    }

    suspend fun deleteCommunityPost(id: Long) {
        communityDao?.deletePost(id)
    }

    suspend fun seedCommunityPostsIfEmpty() {
        if (communityDao != null && communityDao.getPostsCount() == 0) {
            val samplePosts = listOf(
                CommunityPostEntity(
                    id = 1,
                    authorName = "مكتب النيل للاستثمار العقاري",
                    authorRole = "مكتب عقاري",
                    authorAvatarRes = null,
                    authorPhone = "01098765432",
                    postType = "🏠 عقار للبيع",
                    content = "فرصة نادرة للتمليك: شقة سوبر لوكس للبيع في شارع الجيش الرئيسي بدسوق 3 غرف وصالة كبيرة وحمامين الدور الرابع بعمارة فخمة بها أسانسير وعداد كهرباء كودي والسعر مليون ونصف جاهزة للسكن الفوري والتسليم فوري من المالك بدون وسيط.",
                    imagesJson = "img_cat_3d_apartments_1790767759152,img_property_apartment",
                    hasVideo = true,
                    videoLabel = "جولة فيديو مصورة للشقة",
                    likesCount = 28,
                    commentsCount = 6,
                    isLikedByMe = false,
                    isSavedByMe = false,
                    isFollowed = false,
                    timestamp = System.currentTimeMillis() - 1800000L, // 30 mins ago
                    district = "شارع الجيش",
                    extractedType = "شقة",
                    extractedDeal = "للبيع",
                    extractedLocation = "شارع الجيش - دسوق",
                    extractedRooms = "3 غرف",
                    extractedFloor = "الدور الرابع",
                    extractedPrice = "1,500,000 جنيه",
                    extractedArea = "140 م²"
                ),
                CommunityPostEntity(
                    id = 2,
                    authorName = "أحمد رضوان (مستثمر)",
                    authorRole = "مستخدم",
                    authorAvatarRes = null,
                    authorPhone = "01123456789",
                    postType = "🔎 مطلوب عقار",
                    content = "مطلوب شقة للإيجار في دسوق، غرفتين، بحد أقصى 6000 جنيه في حي دحروج أو بالقرب من المستشفى العام لعائلة صغيرة. نرجو التواصل من الملاك أو المكاتب المعتمدة.",
                    imagesJson = "",
                    hasVideo = false,
                    likesCount = 14,
                    commentsCount = 4,
                    isLikedByMe = true,
                    isSavedByMe = false,
                    isFollowed = false,
                    timestamp = System.currentTimeMillis() - 7200000L, // 2 hours ago
                    district = "دحروج",
                    extractedType = "شقة",
                    extractedDeal = "مطلوب عقار للإيجار",
                    extractedLocation = "دحروج / المستشفى العام",
                    extractedRooms = "2 غرف",
                    extractedFloor = "",
                    extractedPrice = "6,000 ج/شهرياً",
                    extractedArea = "100 م²"
                ),
                CommunityPostEntity(
                    id = 3,
                    authorName = "المهندس محمود الشناوي",
                    authorRole = "خبير عقاري",
                    authorAvatarRes = null,
                    authorPhone = "01234567890",
                    postType = "📰 معلومة أو نصيحة عقارية",
                    content = "نصيحة هامة لكل مشتري عقار في دسوق: قبل توقيع العقد، تأكد من مطابقة الرفع المساحي للشهر العقاري طبقاً للقانون 9 لسنة 2022، واطلب شهادة براءة ذمة من شركة مياه الشرب والكهرباء بدسوق لضمان عدم وجود فواتير متأخرة.",
                    imagesJson = "",
                    hasVideo = false,
                    likesCount = 45,
                    commentsCount = 11,
                    isLikedByMe = false,
                    isSavedByMe = true,
                    isFollowed = true,
                    timestamp = System.currentTimeMillis() - 14400000L, // 4 hours ago
                    district = "مدينة دسوق",
                    extractedType = "نصيحة قانونية",
                    extractedDeal = "إرشادات",
                    extractedLocation = "دسوق",
                    extractedRooms = "",
                    extractedFloor = "",
                    extractedPrice = "",
                    extractedArea = ""
                ),
                CommunityPostEntity(
                    id = 4,
                    authorName = "مكتب الصفا للتسويق العقاري",
                    authorRole = "وسيط معتمد",
                    authorAvatarRes = null,
                    authorPhone = "01011223344",
                    postType = "🏢 محل أو وحدة تجارية",
                    content = "محل تجاري حيوي للإيجار بشارع سعد زغلول موقع تجاري من الدرجة الأولى مساحة 40 متر واجهة زجاجية تشطيب ممتاز يصلح لمحل ملابس أو صيدلية أو مطعم تيك أواي الإيجار 12,000 جنيه شهرياً.",
                    imagesJson = "img_property_shop_1790238644238",
                    hasVideo = true,
                    videoLabel = "فيديو للمحل والشارع المحيط",
                    likesCount = 19,
                    commentsCount = 3,
                    isLikedByMe = false,
                    isSavedByMe = false,
                    isFollowed = false,
                    timestamp = System.currentTimeMillis() - 28800000L, // 8 hours ago
                    district = "شارع سعد زغلول",
                    extractedType = "محل تجاري",
                    extractedDeal = "للإيجار",
                    extractedLocation = "شارع سعد زغلول - دسوق",
                    extractedRooms = "",
                    extractedFloor = "الدور الأرضي",
                    extractedPrice = "12,000 ج/شهرياً",
                    extractedArea = "40 م²"
                ),
                CommunityPostEntity(
                    id = 5,
                    authorName = "الحاج إبراهيم الدسوقي",
                    authorRole = "مالك",
                    authorAvatarRes = null,
                    authorPhone = "01555667788",
                    postType = "🌾 أرض",
                    content = "قطعة أرض كردون مباني للبيع في حي الصفا الجديد مساحة 175 متر واجهة بحرية 12 متر على شارع 10 متر كاملة المرافق (مياه وكهرباء وغاز) أوراق مسجلة بالكامل وبها ترخيص بناء.",
                    imagesJson = "img_cat_3d_land_parcels_1790767799928",
                    hasVideo = false,
                    likesCount = 33,
                    commentsCount = 8,
                    isLikedByMe = false,
                    isSavedByMe = false,
                    isFollowed = false,
                    timestamp = System.currentTimeMillis() - 86400000L, // 1 day ago
                    district = "حي الصفا",
                    extractedType = "أرض",
                    extractedDeal = "للبيع",
                    extractedLocation = "حي الصفا - دسوق",
                    extractedRooms = "",
                    extractedFloor = "",
                    extractedPrice = "سعر المتر 8,500 جنيه",
                    extractedArea = "175 م²"
                ),
                CommunityPostEntity(
                    id = 6,
                    authorName = "محمد عبد العاطي",
                    authorRole = "مستخدم",
                    authorAvatarRes = null,
                    authorPhone = "01009988776",
                    postType = "💬 سؤال عقاري",
                    content = "يا جماعة لو سمحتم، كم متوسط سعر متر الشقق التمليك حالياً في منطقة كورنيش النيل بدسوق؟ وهل الخدمات متوفرة ومستقرة هناك في الشتاء؟ شكراً مقدماً.",
                    imagesJson = "",
                    hasVideo = false,
                    likesCount = 22,
                    commentsCount = 9,
                    isLikedByMe = false,
                    isSavedByMe = false,
                    isFollowed = false,
                    timestamp = System.currentTimeMillis() - 172800000L, // 2 days ago
                    district = "كورنيش النيل",
                    extractedType = "استفسار",
                    extractedDeal = "سؤال",
                    extractedLocation = "كورنيش النيل",
                    extractedRooms = "",
                    extractedFloor = "",
                    extractedPrice = "",
                    extractedArea = ""
                )
            )
            communityDao.insertPosts(samplePosts)

            // إضافة تعليقات نموذجية
            communityDao.insertComment(
                CommunityCommentEntity(
                    postId = 1,
                    authorName = "كريم البدري",
                    authorRole = "مستخدم",
                    content = "هل العمارة مسجلة شهر عقاري وحصتها في الأرض متوثقة في العقد؟",
                    timestamp = System.currentTimeMillis() - 1200000L
                )
            )
            communityDao.insertComment(
                CommunityCommentEntity(
                    postId = 1,
                    authorName = "مكتب النيل للاستثمار العقاري",
                    authorRole = "مكتب عقاري",
                    content = "أهلاً أستاذ كريم، نعم العمارة مسجلة بالكامل ولكل شقة حصة ثابتة بالأرض ومتاح معاينة الأوراق بمقر المكتب بشارع الجيش.",
                    timestamp = System.currentTimeMillis() - 600000L
                )
            )
            communityDao.insertComment(
                CommunityCommentEntity(
                    postId = 6,
                    authorName = "مكتب النيل للاستثمار العقاري",
                    authorRole = "مكتب عقاري",
                    content = "سعر المتر في الكورنيش يتراوح حالياً بين 16,000 و 24,000 جنيه حسب الإطلالة المباشرة على النيل والدور، والخدمات ممتازة طوال العام.",
                    timestamp = System.currentTimeMillis() - 86400000L
                )
            )
        }
    }

    suspend fun checkAndSeedDatabase() {
        if (propertyDao.getCount() == 0) {
            AppDatabase.populateInitialData(
                dao = propertyDao,
                reviewDao = reviewDao,
                userDao = userDao,
                requestDao = requestDao,
                inquiryDao = inquiryDao,
                settingDao = settingDao,
                logDao = logDao,
                sourceDao = sourceDao,
                rawPostDao = rawPostDao,
                importedDao = importedDao,
                importLogDao = importLogDao
            )
        }
    }
}
