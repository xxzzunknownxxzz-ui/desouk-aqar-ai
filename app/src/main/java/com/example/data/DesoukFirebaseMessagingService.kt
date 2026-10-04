package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * خدمة Firebase Cloud Messaging (FCM) لمعالجة الإشعارات الدفعية الفورية
 * وربط اهتمامات المستخدم وتنبيهه عند توفر عقارات مطابقة في دسوق.
 */
class DesoukFirebaseMessagingService : FirebaseMessagingService() {

    private val tag = "DesoukFCMService"

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(tag, "🔑 تم تجديد رمز FCM Device Token: $token")
        // حفظ الرمز محلياً ومزامنته مع Firestore
        val prefs = applicationContext.getSharedPreferences("desouk_fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(tag, "📩 استلام إشعار دفع جديد من FCM: ${remoteMessage.data}")

        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "عقار جديد يطابق اهتماماتك في دسوق 🎯"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "يتوفر الآن عقار جديد يطابق المنطقة والسعر المفضلين لديك."

        val propertyIdStr = remoteMessage.data["propertyId"]
        val propertyId = propertyIdStr?.toLongOrNull()

        sendPushNotification(
            context = applicationContext,
            title = title,
            messageBody = body,
            propertyId = propertyId
        )
    }

    companion object {
        const val CHANNEL_ID = "desouk_fcm_property_alerts"
        const val CHANNEL_NAME = "تنبيهات عقارات دسوق الفورية"

        /**
         * إرسال وعرض إشعار دفع في شريط الإشعارات بالنظام
         */
        fun sendPushNotification(
            context: Context,
            title: String,
            messageBody: String,
            propertyId: Long? = null
        ) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // إنشاء قناة الإشعارات لنظام أندرويد 8.0 فما فوق
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "إشعارات فورية للعقارات المطابقة لاهتمامات المستخدم في دسوق"
                    enableLights(true)
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
                if (propertyId != null) {
                    putExtra("target_property_id", propertyId)
                }
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                (System.currentTimeMillis() % 10000).toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon_fg_1790238581142)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)

            notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notificationBuilder.build())
        }
    }
}
