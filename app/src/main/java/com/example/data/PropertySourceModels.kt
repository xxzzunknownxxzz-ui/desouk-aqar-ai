package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties

/**
 * حالات الإعلان المستورد
 */
object ImportedAdStatus {
    const val PUBLISHED = "منشور"
    const val DRAFT = "مسودة"
    const val NEEDS_REVIEW = "يحتاج مراجعة"
    const val DUPLICATE = "مكرر"
    const val IN_REVIEW = "يحتاج مراجعة"
    const val ACTIVE = "منشور"
    const val ARCHIVED = "مؤرشف"
    const val SOLD = "تم البيع"
    const val RENTED = "تم التأجير"
    const val UNAVAILABLE = "غير متاح"
    const val REJECTED = "مرفوض"
}

/**
 * أنواع مصادر العقارات
 */
object SourceType {
    const val FB_OFFICIAL_PAGE = "صفحة فيسبوك رسمية"
    const val FB_AUTHORIZED_GROUP = "مجموعة معتمدة"
    const val REAL_ESTATE_PORTAL = "بوابة عقارية معتمدة"
    const val OFFICIAL_IMPORT_FILE = "ملف استيراد رسمي"
    const val AUTHORIZED_MANUAL = "إدخال يدوي مصرح به"
}

/**
 * جدول مصادر العقارات (Sources)
 */
@Entity(tableName = "property_sources")
data class PropertySourceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firestoreId: String = "",
    val name: String,
    val type: String = SourceType.FB_OFFICIAL_PAGE,
    val url: String = "",
    val isActive: Boolean = true,
    val lastSyncTime: Long = System.currentTimeMillis(),
    val totalImportedPosts: Int = 0,
    val newAdsCount: Int = 0,
    val duplicateAdsCount: Int = 0,
    val needsReviewCount: Int = 0,
    val syncStatus: String = "مستقر",
    val notes: String = ""
)

/**
 * جدول البيانات الخام للمنشورات (RawPosts)
 * يحفظ النص الأصلي وكافة البيانات الأولية دون تعديل
 */
@Entity(tableName = "raw_posts")
data class RawPostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firestoreId: String = "",
    val sourceId: Long = 0,
    val sourceName: String = "",
    val postExternalId: String = "",
    val postUrl: String = "",
    val postTimestamp: Long = System.currentTimeMillis(),
    val originalRawText: String = "",
    val imageUrl: String = "",
    val authorName: String = "",
    val authorPhone: String = "",
    val status: String = "معالج"
)

/**
 * جدول الإعلانات المستوردة المعالجة بواسطة الذكاء الاصطناعي (ImportedProperties / ReviewQueue)
 */
@Entity(tableName = "imported_properties")
data class ImportedPropertyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val propertyId: Long = 0, // property_id
    val title: String = "",
    val propertyType: String = "شقة", // property_type: شقة، فيلا، منزل، أرض، محل، مكتب، عمارة، مخزن، شاليه، عقار تجاري، أخرى
    val listingType: String = "للبيع", // listing_type: للبيع، للإيجار
    val price: Double? = null, // price
    val area: Double? = null, // area
    val location: String = "دسوق", // location: دسوق، أحياء ومناطق دسوق، القرى والمناطق المحيطة
    val address: String = "", // address
    val rooms: Int? = null, // rooms
    val bathrooms: Int? = null, // bathrooms
    val floor: Int? = null, // floor
    val finishing: String = "", // finishing
    val description: String = "", // description
    val phone: String = "", // phone
    val ownerName: String = "", // owner_name
    val images: String = "img_property_apartment", // images
    val sourceUrl: String = "", // source_url
    val sourcePlatform: String = "Facebook", // source_platform
    val importedBy: String = "Super Admin", // imported_by
    val importedAt: Long = System.currentTimeMillis(), // imported_at
    val status: String = ImportedAdStatus.NEEDS_REVIEW, // status: منشور، مسودة، يحتاج مراجعة، مكرر
    val isDuplicate: Boolean = false, // is_duplicate
    val createdAt: Long = System.currentTimeMillis(), // created_at
    val updatedAt: Long = System.currentTimeMillis(), // updated_at
    val streetsCount: Int = 1,
    val facade: String = "",
    val firestoreId: String = "",
    val sourceId: Long = 0,
    val sourceName: String = "",
    val rawPostId: Long = 0,
    val postExternalId: String = "",
    val postUrl: String = "",
    val operationType: String = "للبيع",
    val city: String = "دسوق",
    val district: String = "دسوق",
    val street: String = "",
    val landmark: String = "",
    val priceUnit: String = "ج.م",
    val totalFloors: Int? = null,
    val hasElevator: Boolean? = null,
    val hasGarage: Boolean? = null,
    val condition: String = "",
    val features: String = "",
    val cleanedText: String = "",
    val originalRawText: String = "",
    val imageUrl: String = "img_property_apartment",
    val contactPhone: String = "",
    val contactWhatsapp: String = "",
    val contactName: String = "",
    val completenessScore: Int = 80,
    val uncertainData: String = "",
    val duplicateMatchReason: String = "",
    val duplicateOfId: Long? = null,
    val reviewedAt: Long? = null,
    val reviewedBy: String = "",
    val reviewNotes: String = ""
)

/**
 * سجل عمليات الاستيراد والمزامنة (ImportLogs)
 */
@Entity(tableName = "import_logs")
data class ImportLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val firestoreId: String = "",
    val sourceName: String,
    val action: String,
    val details: String,
    val aiResult: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * ملخص الإحصائيات الإدارية المطلوبة المستندة إلى مجموعات Firestore
 */
data class SourcesDashboardStats(
    val activeSourcesCount: Int = 0,
    val totalImportedPostsCount: Int = 0,
    val newAdsCount: Int = 0,
    val inReviewCount: Int = 0,
    val publishedCount: Int = 0,
    val duplicateCount: Int = 0,
    val rejectedCount: Int = 0,
    val archivedCount: Int = 0
)

/**
 * موديلات Firestore المتوافقة مع بنية البيانات السحابية
 */
@IgnoreExtraProperties
data class FirestoreSource(
    @get:Exclude var id: String = "",
    var name: String = "",
    var type: String = "",
    var url: String = "",
    var isActive: Boolean = true,
    var lastSyncTime: Long = 0L,
    var totalImportedPosts: Int = 0,
    var newAdsCount: Int = 0,
    var duplicateAdsCount: Int = 0,
    var needsReviewCount: Int = 0,
    var syncStatus: String = "مستقر",
    var notes: String = ""
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "type" to type,
        "url" to url,
        "isActive" to isActive,
        "lastSyncTime" to lastSyncTime,
        "totalImportedPosts" to totalImportedPosts,
        "newAdsCount" to newAdsCount,
        "duplicateAdsCount" to duplicateAdsCount,
        "needsReviewCount" to needsReviewCount,
        "syncStatus" to syncStatus,
        "notes" to notes
    )
}

@IgnoreExtraProperties
data class FirestoreRawPost(
    @get:Exclude var id: String = "",
    var sourceId: String = "",
    var sourceName: String = "",
    var postExternalId: String = "",
    var postUrl: String = "",
    var postTimestamp: Long = 0L,
    var originalRawText: String = "",
    var imageUrl: String = "",
    var authorName: String = "",
    var authorPhone: String = "",
    var status: String = "معالج"
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "sourceId" to sourceId,
        "sourceName" to sourceName,
        "postExternalId" to postExternalId,
        "postUrl" to postUrl,
        "postTimestamp" to postTimestamp,
        "originalRawText" to originalRawText,
        "imageUrl" to imageUrl,
        "authorName" to authorName,
        "authorPhone" to authorPhone,
        "status" to status
    )
}

@IgnoreExtraProperties
data class FirestoreImportedProperty(
    @get:Exclude var id: String = "",
    var propertyId: Long = 0L,
    var sourceName: String = "",
    var postExternalId: String = "",
    var postUrl: String = "",
    var sourceUrl: String = "",
    var sourcePlatform: String = "Facebook",
    var importedBy: String = "Super Admin",
    var importedAt: Long = 0L,
    var title: String = "",
    var propertyType: String = "",
    var listingType: String = "للبيع",
    var operationType: String = "",
    var city: String = "دسوق",
    var district: String = "",
    var location: String = "دسوق",
    var address: String = "",
    var street: String = "",
    var streetsCount: Int = 1,
    var landmark: String = "",
    var price: Double? = null,
    var priceUnit: String = "ج.م",
    var area: Double? = null,
    var rooms: Int? = null,
    var bathrooms: Int? = null,
    var floor: Int? = null,
    var totalFloors: Int? = null,
    var hasElevator: Boolean? = null,
    var hasGarage: Boolean? = null,
    var finishing: String = "",
    var facade: String = "",
    var description: String = "",
    var cleanedText: String = "",
    var originalRawText: String = "",
    var images: String = "img_property_apartment",
    var imageUrl: String = "",
    var phone: String = "",
    var contactPhone: String = "",
    var contactWhatsapp: String = "",
    var ownerName: String = "",
    var contactName: String = "",
    var completenessScore: Int = 0,
    var uncertainData: String = "",
    var status: String = ImportedAdStatus.NEEDS_REVIEW,
    var isDuplicate: Boolean = false,
    var duplicateMatchReason: String = "",
    var createdAt: Long = 0L,
    var updatedAt: Long = 0L
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "property_id" to propertyId,
        "title" to title,
        "property_type" to propertyType,
        "listing_type" to listingType,
        "price" to price,
        "area" to area,
        "location" to location,
        "address" to address,
        "rooms" to rooms,
        "bathrooms" to bathrooms,
        "floor" to floor,
        "finishing" to finishing,
        "description" to description,
        "phone" to phone,
        "owner_name" to ownerName,
        "images" to images,
        "source_url" to (sourceUrl.ifBlank { postUrl }),
        "source_platform" to sourcePlatform,
        "imported_by" to importedBy,
        "imported_at" to importedAt,
        "status" to status,
        "is_duplicate" to isDuplicate,
        "created_at" to createdAt,
        "updated_at" to updatedAt,
        "sourceName" to sourceName,
        "postExternalId" to postExternalId,
        "postUrl" to postUrl,
        "operationType" to operationType,
        "city" to city,
        "district" to district,
        "street" to street,
        "streetsCount" to streetsCount,
        "facade" to facade,
        "landmark" to landmark,
        "priceUnit" to priceUnit,
        "totalFloors" to totalFloors,
        "hasElevator" to hasElevator,
        "hasGarage" to hasGarage,
        "cleanedText" to cleanedText,
        "originalRawText" to originalRawText,
        "imageUrl" to imageUrl,
        "contactPhone" to contactPhone,
        "contactWhatsapp" to contactWhatsapp,
        "contactName" to contactName,
        "completenessScore" to completenessScore,
        "uncertainData" to uncertainData,
        "duplicateMatchReason" to duplicateMatchReason
    )
}
