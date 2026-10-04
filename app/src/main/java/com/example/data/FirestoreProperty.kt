package com.example.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName

/**
 * نموذج بيانات العقار المتوافق مع قاعدة بيانات Firebase Firestore:
 * يدعم الاسترجاع والحفظ التلقائي مع تعيين المعرف التلقائي DocumentId
 * وتوفير دوال للتحويل من وإلى نموذج Room المحلي PropertyEntity.
 */
@IgnoreExtraProperties
data class FirestoreProperty(
    @DocumentId
    val id: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String = "شقق", // شقق, منازل, محلات, مكاتب, أراضي

    @get:PropertyName("type")
    @set:PropertyName("type")
    var type: String = "للبيع", // للبيع, للإيجار

    @get:PropertyName("location")
    @set:PropertyName("location")
    var location: String = "",

    @get:PropertyName("district")
    @set:PropertyName("district")
    var district: String = "دسوق",

    @get:PropertyName("price")
    @set:PropertyName("price")
    var price: Double = 0.0,

    @get:PropertyName("priceUnit")
    @set:PropertyName("priceUnit")
    var priceUnit: String = "ج.م",

    @get:PropertyName("rooms")
    @set:PropertyName("rooms")
    var rooms: Int = 0,

    @get:PropertyName("bathrooms")
    @set:PropertyName("bathrooms")
    var bathrooms: Int = 0,

    @get:PropertyName("area")
    @set:PropertyName("area")
    var area: Double = 0.0,

    @get:PropertyName("floor")
    @set:PropertyName("floor")
    var floor: Int = 1,

    @get:PropertyName("phone")
    @set:PropertyName("phone")
    var phone: String = "01001234567",

    @get:PropertyName("whatsapp")
    @set:PropertyName("whatsapp")
    var whatsapp: String = "201001234567",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("imageResName")
    @set:PropertyName("imageResName")
    var imageResName: String = "img_property_apartment",

    @get:PropertyName("imageUrl")
    @set:PropertyName("imageUrl")
    var imageUrl: String = "",

    @get:PropertyName("isFeatured")
    @set:PropertyName("isFeatured")
    var isFeatured: Boolean = false,

    @get:PropertyName("latitude")
    @set:PropertyName("latitude")
    var latitude: Double = 31.1306,

    @get:PropertyName("longitude")
    @set:PropertyName("longitude")
    var longitude: Double = 30.6482,

    @get:PropertyName("timestamp")
    @set:PropertyName("timestamp")
    var timestamp: Long = System.currentTimeMillis()
) {
    /**
     * تحويل عقار Firestore إلى PropertyEntity المستخدم في التخزين المحلي والواجهات
     */
    fun toPropertyEntity(localId: Long = 0L): PropertyEntity {
        return PropertyEntity(
            id = localId,
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
            imageResName = imageResName,
            phone = phone,
            whatsapp = whatsapp,
            description = description,
            latitude = latitude,
            longitude = longitude,
            timestamp = timestamp
        )
    }

    /**
     * تحويل الكائن إلى Map لتخزينه في Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "title" to title,
            "category" to category,
            "type" to type,
            "location" to location,
            "district" to district,
            "price" to price,
            "priceUnit" to priceUnit,
            "rooms" to rooms,
            "bathrooms" to bathrooms,
            "area" to area,
            "floor" to floor,
            "phone" to phone,
            "whatsapp" to whatsapp,
            "description" to description,
            "imageResName" to imageResName,
            "imageUrl" to imageUrl,
            "isFeatured" to isFeatured,
            "latitude" to latitude,
            "longitude" to longitude,
            "timestamp" to timestamp
        )
    }

    companion object {
        /**
         * إنشاء نموذج Firestore من PropertyEntity محلي
         */
        fun fromPropertyEntity(entity: PropertyEntity, firestoreId: String = ""): FirestoreProperty {
            return FirestoreProperty(
                id = firestoreId,
                title = entity.title,
                category = entity.category,
                type = entity.type,
                location = entity.location,
                district = entity.district,
                price = entity.price,
                priceUnit = entity.priceUnit,
                rooms = entity.rooms,
                bathrooms = entity.bathrooms,
                area = entity.area,
                floor = entity.floor,
                phone = entity.phone,
                whatsapp = entity.whatsapp,
                description = entity.description,
                imageResName = entity.imageResName,
                latitude = entity.latitude,
                longitude = entity.longitude,
                timestamp = entity.timestamp
            )
        }
    }
}
