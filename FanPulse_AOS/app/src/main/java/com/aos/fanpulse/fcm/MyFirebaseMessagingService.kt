package com.aos.fanpulse.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.aos.fanpulse.MainActivity
import com.aos.fanpulse.domain.model.NotificationModel
import com.aos.fanpulse.domain.repository.NotificationRepository
import com.aos.fanpulse.domain.repository.SettingRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    @Inject
    lateinit var notificationRepository: NotificationRepository
    @Inject
    lateinit var settingRepository: SettingRepository

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "파이어베이스에서 새 토큰이 발급되었습니다: $token")
        runBlocking {
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val isNotificationEnabled = runBlocking {
            settingRepository.isNotificationEnabled.first()
        }

        if (!isNotificationEnabled) {
            Log.d("FCM_TEST", "DataStore 설정이 OFF 상태이므로 배너를 띄우지 않습니다.")
            return
        }

        val title = message.notification?.title ?: "FanPulse"
        val body = message.notification?.body ?: "새로운 알림이 도착했습니다."
        runBlocking {
            notificationRepository.insertNotification(
                NotificationModel(
                    title = title,
                    body = body,
                    receivedAt = System.currentTimeMillis(),
                    isRead = false
                )
            )
        }
        sendNotification(title, body)
    }

    private fun sendNotification(title: String, body: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val channelId = "fanpulse_notification_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "앱 공지사항 및 안내",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }

    fun onNotificationSwitchChanged(isChecked: Boolean) {
        if (isChecked) {
            FirebaseMessaging.getInstance().subscribeToTopic("charts")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Log.d("FCM_TEST", "charts 구독 성공")
                }
        } else {
            FirebaseMessaging.getInstance().unsubscribeFromTopic("charts")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) Log.d("FCM_TEST", "charts 구독 취소 성공")
                }
        }
    }
}