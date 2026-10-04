package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PropertyEntity::class,
        CustomerRequestEntity::class,
        PropertyReviewEntity::class,
        AdminActivityLogEntity::class,
        AppUserEntity::class,
        CustomerInquiryEntity::class,
        AppSettingEntity::class,
        PropertySourceEntity::class,
        RawPostEntity::class,
        ImportedPropertyEntity::class,
        ImportLogEntity::class,
        PropertyDraftEntity::class,
        CommunityPostEntity::class,
        CommunityCommentEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun propertyDao(): PropertyDao
    abstract fun customerRequestDao(): CustomerRequestDao
    abstract fun propertyReviewDao(): PropertyReviewDao
    abstract fun adminActivityLogDao(): AdminActivityLogDao
    abstract fun appUserDao(): AppUserDao
    abstract fun customerInquiryDao(): CustomerInquiryDao
    abstract fun appSettingDao(): AppSettingDao
    abstract fun propertySourceDao(): PropertySourceDao
    abstract fun rawPostDao(): RawPostDao
    abstract fun importedPropertyDao(): ImportedPropertyDao
    abstract fun importLogDao(): ImportLogDao
    abstract fun propertyDraftDao(): PropertyDraftDao
    abstract fun communityDao(): CommunityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "desouk_properties_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(
                            dao = database.propertyDao(),
                            reviewDao = database.propertyReviewDao(),
                            userDao = database.appUserDao(),
                            requestDao = database.customerRequestDao(),
                            inquiryDao = database.customerInquiryDao(),
                            settingDao = database.appSettingDao(),
                            logDao = database.adminActivityLogDao(),
                            sourceDao = database.propertySourceDao(),
                            rawPostDao = database.rawPostDao(),
                            importedDao = database.importedPropertyDao(),
                            importLogDao = database.importLogDao()
                        )
                    }
                }
            }
        }

        suspend fun populateInitialData(
            dao: PropertyDao,
            reviewDao: PropertyReviewDao? = null,
            userDao: AppUserDao? = null,
            requestDao: CustomerRequestDao? = null,
            inquiryDao: CustomerInquiryDao? = null,
            settingDao: AppSettingDao? = null,
            logDao: AdminActivityLogDao? = null,
            sourceDao: PropertySourceDao? = null,
            rawPostDao: RawPostDao? = null,
            importedDao: ImportedPropertyDao? = null,
            importLogDao: ImportLogDao? = null
        ) {
            val defaultProperties = listOf(
                // Card 1: أرض للبيع
                PropertyEntity(
                    id = 1,
                    title = "أرض للبيع",
                    category = "أراضي",
                    type = "للبيع",
                    location = "دسوق والمناطق المحيطة",
                    district = "المناطق المحيطة",
                    price = 750000.0,
                    priceUnit = "ج.م",
                    rooms = 0,
                    bathrooms = 0,
                    area = 500.0,
                    floor = 0,
                    imageResName = "img_property_land",
                    phone = "01099234511",
                    whatsapp = "201099234511",
                    description = "أرض مميزة للبيع بموقع استراتيجي على أطراف مدينة دسوق، صالحة للبناء أو الاستثمار الزراعي والتجاري، واجهة بحرية، كافة المرافق قريبة وقابلة للتوصيل، أوراق سليمة ومسجلة بالشهر العقاري.",
                    latitude = 31.1380,
                    longitude = 30.6550,
                    timestamp = System.currentTimeMillis() - 1000
                ),
                // Card 2: محل تجاري للإيجار
                PropertyEntity(
                    id = 2,
                    title = "محل تجاري للإيجار",
                    category = "محلات",
                    type = "للإيجار",
                    location = "دسوق",
                    district = "وسط البلد",
                    price = 12000.0,
                    priceUnit = "ج.م / شهريا",
                    rooms = 1,
                    bathrooms = 1,
                    area = 50.0,
                    floor = 1,
                    imageResName = "img_property_shop",
                    phone = "01018844221",
                    whatsapp = "201018844221",
                    description = "محل تجاري مميز بموقع حيوي جداً في قلب مدينة دسوق، واجهة زجاجية كاملة سيكوريت، تشطيب سوبر لوكس، عداد كهرباء تجاري وعداد مياه، مناسب لجميع الأنشطة التجارية والماركات والعيادات والمكاتب.",
                    latitude = 31.1310,
                    longitude = 30.6480,
                    timestamp = System.currentTimeMillis() - 2000
                ),
                // Card 3: شقة للبيع
                PropertyEntity(
                    id = 3,
                    title = "شقة للبيع",
                    category = "شقق",
                    type = "للبيع",
                    location = "دسوق - منطقة مميزة",
                    district = "منطقة مميزة",
                    price = 1500000.0,
                    priceUnit = "ج.م",
                    rooms = 3,
                    bathrooms = 2,
                    area = 160.0,
                    floor = 4,
                    imageResName = "img_property_building",
                    phone = "01145678901",
                    whatsapp = "201145678901",
                    description = "شقة فاخرة للبيع ببرج سكني حديث في موقع راقٍ بمدينة دسوق، تشطيب الترا سوبر لوكس، فيو مفتوح، مصعد كهربائي حديث، انتركم، عمارة مرخصة، حصة بالأرض، تطل على شوارع واسعة.",
                    latitude = 31.1340,
                    longitude = 30.6430,
                    timestamp = System.currentTimeMillis() - 3000
                ),
                // Card 4: شقة سكنية للإيجار
                PropertyEntity(
                    id = 4,
                    title = "شقة سكنية للإيجار",
                    category = "شقق",
                    type = "للإيجار",
                    location = "دسوق - شارع الشركات",
                    district = "شارع الشركات",
                    price = 6000.0,
                    priceUnit = "ج.م / شهريا",
                    rooms = 3,
                    bathrooms = 2,
                    area = 150.0,
                    floor = 3,
                    imageResName = "img_property_apartment",
                    phone = "01287654321",
                    whatsapp = "201287654321",
                    description = "شقة سكنية للإيجار في شارع الشركات الحيوي، غرف واسعة ورسبشن كبير 3 قطع، تشطيب هاي لوكس، قريبة من المدارس والمستشفيات والخدمات، غاز طبيعي وتكييفات جاهزة للسكن الفوري.",
                    latitude = 31.1290,
                    longitude = 30.6495,
                    timestamp = System.currentTimeMillis() - 4000
                ),
                // Card 5: منزل عائلي للبيع
                PropertyEntity(
                    id = 5,
                    title = "منزل عائلي كامل للبيع",
                    category = "منازل",
                    type = "للبيع",
                    location = "دسوق - شارع الجيش",
                    district = "شارع الجيش",
                    price = 3200000.0,
                    priceUnit = "ج.م",
                    rooms = 6,
                    bathrooms = 4,
                    area = 240.0,
                    floor = 3,
                    imageResName = "img_hero_building",
                    phone = "01005544332",
                    whatsapp = "201005544332",
                    description = "منزل مكون من 3 طوابق كاملة على شارع الجيش الرئيسي، واجهة حديثة، أساسات قوية تتحمل أدوار إضافية، رخصة بناء كاملة، مناسب لعائلة أو مشروع تجاري سكني.",
                    latitude = 31.1325,
                    longitude = 30.6470,
                    timestamp = System.currentTimeMillis() - 5000
                ),
                // Card 6: مكتب إداري راقي
                PropertyEntity(
                    id = 6,
                    title = "مكتب إداري وتجاري مجهز",
                    category = "مكاتب",
                    type = "للإيجار",
                    location = "دسوق - كورنيش النيل",
                    district = "كورنيش النيل",
                    price = 8500.0,
                    priceUnit = "ج.م / شهريا",
                    rooms = 2,
                    bathrooms = 1,
                    area = 85.0,
                    floor = 2,
                    imageResName = "img_property_shop",
                    phone = "01555512345",
                    whatsapp = "201555512345",
                    description = "مقر إداري مفروش ومكيف بالكامل يطل مباشرة على كورنيش النيل بدسوق، مناسب لعيادة طبية أو مكتب محاماة أو شركة برمجيات وتسويق، مجهز بكافة شبكات الإنترنت والسنترال.",
                    latitude = 31.1280,
                    longitude = 30.6410,
                    timestamp = System.currentTimeMillis() - 6000
                ),
                // Card 7: عمارة سكنية للبيع
                PropertyEntity(
                    id = 7,
                    title = "عمارة سكنية واستثمارية كاملة",
                    category = "عمارات",
                    type = "للبيع",
                    location = "دسوق - شارع الجيش",
                    district = "شارع الجيش",
                    price = 4800000.0,
                    priceUnit = "ج.م",
                    rooms = 12,
                    bathrooms = 6,
                    area = 380.0,
                    floor = 5,
                    imageResName = "img_hero_building",
                    phone = "01005544332",
                    whatsapp = "201005544332",
                    description = "عمارة سكنية استثمارية بالكامل للبيع في موقع حيوي على شارع الجيش بدسوق، مكونة من 5 طوابق، تشطيب كامل ومؤجرة بالكامل بعائد شهري استثماري مجزٍ، أوراق مرخصة ومسجلة بالشهر العقاري.",
                    latitude = 31.1330,
                    longitude = 30.6460,
                    timestamp = System.currentTimeMillis() - 7000
                )
            )
            dao.insertAll(defaultProperties)

            reviewDao?.let { rDao ->
                val sampleReviews = listOf(
                    PropertyReviewEntity(
                        propertyId = 1,
                        userName = "أحمد عبد الرحمن",
                        userRole = "زائر معاينة",
                        rating = 5,
                        comment = "الأرض موقعها ممتاز جداً والشارع واسع وتصلح لبناء مشروع أو عمارة، البائع صادق في الوصف والأوراق سليمة."
                    ),
                    PropertyReviewEntity(
                        propertyId = 1,
                        userName = "م. طارق الصاوي",
                        userRole = "مشتري موثق",
                        rating = 4,
                        comment = "سعر مناسب بالنسبة لأسعار الأراضي في دسوق، تفاوض راقي مع الوسيط."
                    ),
                    PropertyReviewEntity(
                        propertyId = 2,
                        userName = "سامي عبد الله",
                        userRole = "مستأجر سابق",
                        rating = 5,
                        comment = "الموقع بالميدان الإبراهيمي حيوي جداً ورجله قوية للمحلات التجارية، والمؤجر شخص محترم جداً."
                    ),
                    PropertyReviewEntity(
                        propertyId = 3,
                        userName = "د. كريم حامد",
                        userRole = "زائر معاينة",
                        rating = 5,
                        comment = "شقة ممتازة على الكورنيش بإطلالة خيالية على النيل، تشطيب راقي جداً ولا تحتاج أي مصاريف."
                    ),
                    PropertyReviewEntity(
                        propertyId = 3,
                        userName = "هشام النجار",
                        userRole = "مشتري موثق",
                        rating = 5,
                        comment = "العمارة هادئة والمدخل فندقي رخام والمصعد يعمل بكفاءة، تجربة ممتازة."
                    ),
                    PropertyReviewEntity(
                        propertyId = 4,
                        userName = "مصطفى قنديل",
                        userRole = "مستأجر سابق",
                        rating = 4,
                        comment = "المكتب في شارع الجيش موقع ممتاز وسهل الوصول للعملاء مع مواصلات متوفرة على مدار اليوم."
                    ),
                    PropertyReviewEntity(
                        propertyId = 5,
                        userName = "ياسر إبراهيم",
                        userRole = "زائر معاينة",
                        rating = 5,
                        comment = "شقة روعة بشارع الشركات قريبة من جميع الخدمات والمدارس والمستشفى العام."
                    )
                )
                rDao.insertAll(sampleReviews)
            }

            userDao?.let { uDao ->
                val sampleUsers = listOf(
                    AppUserEntity(
                        id = 1,
                        name = "Super Admin (الإدارة العامة)",
                        phone = "01000000000",
                        email = "admin@desouk-aqar.com",
                        role = UserRole.SUPER_ADMIN,
                        isBlocked = false,
                        propertiesCount = 6,
                        requestsCount = 0,
                        city = "دسوق",
                        district = "شارع الجيش"
                    ),
                    AppUserEntity(
                        id = 2,
                        name = "محمود البدري",
                        phone = "01012345678",
                        email = "m.badry@gmail.com",
                        role = UserRole.USER,
                        isBlocked = false,
                        propertiesCount = 1,
                        requestsCount = 2,
                        city = "دسوق",
                        district = "كورنيش النيل"
                    ),
                    AppUserEntity(
                        id = 3,
                        name = "م. خالد الفقي",
                        phone = "01123456789",
                        email = "k.feky@yahoo.com",
                        role = UserRole.USER,
                        isBlocked = false,
                        propertiesCount = 0,
                        requestsCount = 3,
                        city = "دسوق",
                        district = "شارع الشركات"
                    ),
                    AppUserEntity(
                        id = 4,
                        name = "سامح عبد الحميد",
                        phone = "01234567890",
                        email = "sameh.a@outlook.com",
                        role = UserRole.USER,
                        isBlocked = true,
                        propertiesCount = 0,
                        requestsCount = 1,
                        city = "دسوق",
                        district = "طريق فوه"
                    )
                )
                uDao.insertAll(sampleUsers)
            }

            requestDao?.let { reqDao ->
                val sampleRequests = listOf(
                    CustomerRequestEntity(
                        id = 1,
                        customerName = "د. وليد الشرقاوي",
                        customerPhone = "01099887766",
                        requestType = RequestType.PURCHASE,
                        propertyCategory = "شقق",
                        targetDistrict = "كورنيش النيل",
                        budget = "1,500,000 ج.م",
                        targetArea = "160 م²",
                        targetRooms = "3 غرف + 2 حمام",
                        details = "مطلوب شقة تطل مباشرة على النيل بكورنيش دسوق، تشطيب الترا لوكس أو سوبر لوكس، مع وجود مصعد شغال وعدادات كاملة والدفع كاش.",
                        status = RequestStatus.NEW
                    ),
                    CustomerRequestEntity(
                        id = 2,
                        customerName = "الحاج رأفت المنشاوي",
                        customerPhone = "01223344556",
                        requestType = RequestType.RENT,
                        propertyCategory = "محلات",
                        targetDistrict = "الميدان الإبراهيمي",
                        budget = "12,000 ج.م / شهر",
                        targetArea = "50 م²",
                        targetRooms = "محل + حمام",
                        details = "مطلوب محل تجاري بواجهة واضحة في محيط الميدان الإبراهيمي أو شارع الجيش يصلح لنشاط أحذية أو ملابس جاهزة، عقد إيجار لا يقل عن 3 سنوات.",
                        status = RequestStatus.IN_PROGRESS
                    ),
                    CustomerRequestEntity(
                        id = 3,
                        customerName = "م. عصام السعدني",
                        customerPhone = "01555667788",
                        requestType = RequestType.CUSTOM_SEARCH,
                        propertyCategory = "أراضي",
                        targetDistrict = "طريق فوه",
                        budget = "900,000 ج.م",
                        targetArea = "150 م²",
                        targetRooms = "أرض مباني",
                        details = "أبحث عن قطعة أرض داخل الحيز العمراني مسجلة شهر عقاري أو لها أصل ملكية واضح، شارع لا يقل عن 8 متر.",
                        status = RequestStatus.CONTACTED
                    )
                )
                reqDao.insertAll(sampleRequests)
            }

            inquiryDao?.let { inqDao ->
                val sampleInquiries = listOf(
                    CustomerInquiryEntity(
                        id = 1,
                        userName = "أحمد السبيعي",
                        userPhone = "01011223344",
                        propertyId = 3,
                        propertyTitle = "شقة ممتازة على الكورنيش بإطلالة خيالية",
                        inquiryText = "السلام عليكم، هل الشقة مرخصة ولها حصة في الأرض؟ وهل متاح التمويل العقاري؟",
                        status = InquiryStatus.NEW
                    ),
                    CustomerInquiryEntity(
                        id = 2,
                        userName = "م. تامر الجيار",
                        userPhone = "01144556677",
                        propertyId = 1,
                        propertyTitle = "أرض للبيع - دسوق والمناطق المحيطة",
                        inquiryText = "هل السعر 750,000 نهائي أم فيه تفاوض في حالة الدفع الفوري كاش؟ وكيفية المعاينة غداً؟",
                        status = InquiryStatus.REPLIED,
                        replyNotes = "تم الاتصال بالعميل وإبلاغه بإمكانية تفاوض بسيط وتحديد موعد معاينة غداً الساعة 4 عصراً."
                    )
                )
                inqDao.insertAll(sampleInquiries)
            }

            settingDao?.let { sDao ->
                val defaultSettings = listOf(
                    AppSettingEntity("hotline", "01001234567"),
                    AppSettingEntity("whatsapp", "201001234567"),
                    AppSettingEntity("office_address", "دسوق - شارع الجيش أمام الميدان الإبراهيمي، برج الصفا الإداري الدور الثاني"),
                    AppSettingEntity("announcement_banner", "أهلاً بكم في منصة عقارات دسوق Ai - المنصة العقارية الرسمية المعتمدة لمدينة دسوق وقراها"),
                    AppSettingEntity("featured_filter", "ALL")
                )
                sDao.setAll(defaultSettings)
            }

            logDao?.let { lDao ->
                lDao.insertLog(
                    AdminActivityLogEntity(
                        actionType = AdminActionType.UPDATE_SETTINGS,
                        details = "تهيئة النظام وقواعد بيانات الإدارة وحساب Super Admin المعتمد بنجاح."
                    )
                )
            }

            sourceDao?.let { sDao ->
                val sampleSources = listOf(
                    PropertySourceEntity(
                        id = 1,
                        name = "الصفحة الرسمية لعقارات دسوق وكفر الشيخ",
                        type = SourceType.FB_OFFICIAL_PAGE,
                        url = "https://facebook.com/desouk.official.realestate",
                        isActive = true,
                        lastSyncTime = System.currentTimeMillis() - 7200000L,
                        totalImportedPosts = 38,
                        newAdsCount = 6,
                        duplicateAdsCount = 3,
                        needsReviewCount = 4,
                        syncStatus = "مستقر"
                    ),
                    PropertySourceEntity(
                        id = 2,
                        name = "مجموعة مستثمري وأهالي دسوق العقارية (معتمدة)",
                        type = SourceType.FB_AUTHORIZED_GROUP,
                        url = "https://facebook.com/groups/desouk.properties.official",
                        isActive = true,
                        lastSyncTime = System.currentTimeMillis() - 14400000L,
                        totalImportedPosts = 24,
                        newAdsCount = 3,
                        duplicateAdsCount = 2,
                        needsReviewCount = 3,
                        syncStatus = "مستقر"
                    ),
                    PropertySourceEntity(
                        id = 3,
                        name = "بوابة الإعلانات المباشرة - دسوق",
                        type = SourceType.REAL_ESTATE_PORTAL,
                        url = "https://desouk-portal.local/api/feed",
                        isActive = true,
                        lastSyncTime = System.currentTimeMillis() - 86400000L,
                        totalImportedPosts = 16,
                        newAdsCount = 2,
                        duplicateAdsCount = 1,
                        needsReviewCount = 1,
                        syncStatus = "مستقر"
                    ),
                    PropertySourceEntity(
                        id = 4,
                        name = "ملف الاستيراد المكتبي الدوري المعتمد",
                        type = SourceType.OFFICIAL_IMPORT_FILE,
                        url = "file://assets/imports/desouk_batch_2026.json",
                        isActive = false,
                        lastSyncTime = System.currentTimeMillis() - 259200000L,
                        totalImportedPosts = 12,
                        newAdsCount = 0,
                        duplicateAdsCount = 0,
                        needsReviewCount = 0,
                        syncStatus = "متوقف مؤقتاً"
                    )
                )
                sDao.insertSources(sampleSources)
            }

            rawPostDao?.let { rDao ->
                val sampleRawPosts = listOf(
                    RawPostEntity(
                        id = 1,
                        sourceId = 1,
                        sourceName = "الصفحة الرسمية لعقارات دسوق وكفر الشيخ",
                        postExternalId = "fb_post_883491",
                        postUrl = "https://facebook.com/desouk.official.realestate/posts/883491",
                        postTimestamp = System.currentTimeMillis() - 3600000L,
                        originalRawText = "فرصة ذهبية شقة للبيع في دسوق حي دحروج مساحة 160 متر دور رابع 3 غرف وحمامين تشطيب سوبر لوكس يوجد أسانسير شغال وجميع العدادات واجهة بحرية تطل على شارع رئيسي مطلوب 1250000 جنيه للتواصل 01099887766 أستاذ إبراهيم الفقي",
                        imageUrl = "img_property_apartment",
                        authorName = "أستاذ إبراهيم الفقي",
                        authorPhone = "01099887766",
                        status = "معالج"
                    ),
                    RawPostEntity(
                        id = 2,
                        sourceId = 1,
                        sourceName = "الصفحة الرسمية لعقارات دسوق وكفر الشيخ",
                        postExternalId = "fb_post_883492",
                        postUrl = "https://facebook.com/desouk.official.realestate/posts/883492",
                        postTimestamp = System.currentTimeMillis() - 7200000L,
                        originalRawText = "محل تجاري متميز للإيجار في شارع الجيش الرئيسي بدسوق مساحة 45 متر متشطب جاهز للنشاط التجاري والصيدليات عداد كهرباء تجاري مطلوب 8000 ج.م شهريا للتواصل 01122334455 مكتب النيل للاستثمار",
                        imageUrl = "img_property_shop",
                        authorName = "مكتب النيل للاستثمار",
                        authorPhone = "01122334455",
                        status = "معالج"
                    ),
                    RawPostEntity(
                        id = 3,
                        sourceId = 2,
                        sourceName = "مجموعة مستثمري وأهالي دسوق العقارية (معتمدة)",
                        postExternalId = "fb_post_992104",
                        postUrl = "https://facebook.com/groups/desouk.properties.official/posts/992104",
                        postTimestamp = System.currentTimeMillis() - 10800000L,
                        originalRawText = "عمارة سكنية استثمارية للبيع قريبة من الميدان الإبراهيمي بدسوق 5 أدوار على مساحة 200 متر كل دور شقة كبيرة مرخصة بالكامل ولها حصة كاملة في الأرض للتواصل مع المالك مباشرة 01012398744 المعاينة يوم الجمعة",
                        imageUrl = "img_property_building",
                        authorName = "المالك مباشرة",
                        authorPhone = "01012398744",
                        status = "معالج"
                    )
                )
                rDao.insertRawPosts(sampleRawPosts)
            }

            importedDao?.let { iDao ->
                val sampleImported = listOf(
                    ImportedPropertyEntity(
                        id = 1,
                        sourceId = 1,
                        sourceName = "الصفحة الرسمية لعقارات دسوق وكفر الشيخ",
                        rawPostId = 1,
                        postExternalId = "fb_post_883491",
                        postUrl = "https://facebook.com/desouk.official.realestate/posts/883491",
                        title = "شقة للبيع في حي دحروج (160 م²)",
                        propertyType = "شقة",
                        operationType = "للبيع",
                        city = "دسوق",
                        district = "حي دحروج",
                        street = "شارع رئيسي",
                        price = 1250000.0,
                        priceUnit = "ج.م",
                        area = 160.0,
                        rooms = 3,
                        bathrooms = 2,
                        floor = 4,
                        hasElevator = true,
                        finishing = "سوبر لوكس",
                        facade = "واجهة بحرية",
                        cleanedText = "شقة للبيع في دسوق حي دحروج مساحة 160 متر دور رابع 3 غرف وحمامين تشطيب سوبر لوكس يوجد أسانسير شغال وجميع العدادات واجهة بحرية تطل على شارع رئيسي مطلوب 1250000 جنيه للتواصل 01099887766",
                        originalRawText = "فرصة ذهبية شقة للبيع في دسوق حي دحروج مساحة 160 متر دور رابع 3 غرف وحمامين تشطيب سوبر لوكس يوجد أسانسير شغال وجميع العدادات واجهة بحرية تطل على شارع رئيسي مطلوب 1250000 جنيه للتواصل 01099887766 أستاذ إبراهيم الفقي",
                        imageUrl = "img_property_apartment",
                        contactPhone = "01099887766",
                        contactWhatsapp = "201099887766",
                        contactName = "أستاذ إبراهيم الفقي",
                        completenessScore = 95,
                        uncertainData = "جميع البيانات الأساسية مؤكدة ومكتملة",
                        status = ImportedAdStatus.IN_REVIEW,
                        isDuplicate = false
                    ),
                    ImportedPropertyEntity(
                        id = 2,
                        sourceId = 1,
                        sourceName = "الصفحة الرسمية لعقارات دسوق وكفر الشيخ",
                        rawPostId = 2,
                        postExternalId = "fb_post_883492",
                        postUrl = "https://facebook.com/desouk.official.realestate/posts/883492",
                        title = "محل للإيجار في شارع الجيش (45 م²)",
                        propertyType = "محل",
                        operationType = "للإيجار",
                        city = "دسوق",
                        district = "شارع الجيش",
                        street = "شارع الجيش الرئيسي",
                        price = 8000.0,
                        priceUnit = "ج.م / شهرياً",
                        area = 45.0,
                        rooms = 0,
                        bathrooms = 1,
                        floor = 0,
                        hasElevator = false,
                        finishing = "سوبر لوكس",
                        cleanedText = "محل تجاري متميز للإيجار في شارع الجيش الرئيسي بدسوق مساحة 45 متر متشطب جاهز للنشاط التجاري والصيدليات عداد كهرباء تجاري مطلوب 8000 ج.م شهريا للتواصل 01122334455",
                        originalRawText = "محل تجاري متميز للإيجار في شارع الجيش الرئيسي بدسوق مساحة 45 متر متشطب جاهز للنشاط التجاري والصيدليات عداد كهرباء تجاري مطلوب 8000 ج.م شهريا للتواصل 01122334455 مكتب النيل للاستثمار",
                        imageUrl = "img_property_shop",
                        contactPhone = "01122334455",
                        contactWhatsapp = "201122334455",
                        contactName = "مكتب النيل للاستثمار",
                        completenessScore = 90,
                        uncertainData = "جميع البيانات مطابقة",
                        status = ImportedAdStatus.IN_REVIEW,
                        isDuplicate = false
                    ),
                    ImportedPropertyEntity(
                        id = 3,
                        sourceId = 2,
                        sourceName = "مجموعة مستثمري وأهالي دسوق العقارية (معتمدة)",
                        rawPostId = 3,
                        postExternalId = "fb_post_992104",
                        postUrl = "https://facebook.com/groups/desouk.properties.official/posts/992104",
                        title = "عمارة للبيع في الميدان الإبراهيمي (200 م²)",
                        propertyType = "عمارة",
                        operationType = "للبيع",
                        city = "دسوق",
                        district = "الميدان الإبراهيمي",
                        landmark = "الميدان الإبراهيمي",
                        price = null, // السعر غير مذكور في المنشور
                        priceUnit = "ج.م",
                        area = 200.0,
                        rooms = 15,
                        totalFloors = 5,
                        cleanedText = "عمارة سكنية استثمارية للبيع قريبة من الميدان الإبراهيمي بدسوق 5 أدوار على مساحة 200 متر كل دور شقة كبيرة مرخصة بالكامل ولها حصة كاملة في الأرض للتواصل 01012398744",
                        originalRawText = "عمارة سكنية استثمارية للبيع قريبة من الميدان الإبراهيمي بدسوق 5 أدوار على مساحة 200 متر كل دور شقة كبيرة مرخصة بالكامل ولها حصة كاملة في الأرض للتواصل مع المالك مباشرة 01012398744 المعاينة يوم الجمعة",
                        imageUrl = "img_property_building",
                        contactPhone = "01012398744",
                        contactWhatsapp = "201012398744",
                        contactName = "المالك مباشرة",
                        completenessScore = 70,
                        uncertainData = "السعر غير مذكور في المنشور الأصلي • الدور غير محدد",
                        status = ImportedAdStatus.IN_REVIEW,
                        isDuplicate = false
                    )
                )
                iDao.insertAll(sampleImported)
            }

            importLogDao?.let { lgDao ->
                val sampleLogs = listOf(
                    ImportLogEntity(
                        id = 1,
                        sourceName = "الصفحة الرسمية لعقارات دسوق وكفر الشيخ",
                        action = "مزامنة آلية ناجحة",
                        details = "تم استيراد ومعالجة 2 منشورات جديدة وتصنيفها بالذكاء الاصطناعي بنجاح",
                        aiResult = "تم استخراج 2 إعلان جديد، 0 مكرر، نسبة الدقة 95%",
                        timestamp = System.currentTimeMillis() - 3600000L
                    ),
                    ImportLogEntity(
                        id = 2,
                        sourceName = "مجموعة مستثمري وأهالي دسوق العقارية",
                        action = "فحص التكرار والمزامنة",
                        details = "اكتشاف منشور لعمارة سكنية بالميدان الإبراهيمي وإضافته لقائمة المراجعة",
                        aiResult = "تنبيه: السعر غير مذكور في المنشور الخام، تم تركه فارغاً وفق معايير الأمان",
                        timestamp = System.currentTimeMillis() - 7200000L
                    )
                )
                sampleLogs.forEach { lgDao.insertLog(it) }
            }
        }
    }
}
