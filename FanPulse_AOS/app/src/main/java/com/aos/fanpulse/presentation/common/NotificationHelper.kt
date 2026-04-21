package com.aos.fanpulse.presentation.common

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

//  로컬 알림
object NotificationHelper {

    private const val CHANNEL_ID = "default_channel_id"
    private const val CHANNEL_NAME = "기본 알림"
    private const val NOTIFICATION_ID = 1

    fun showNotification(context: Context, title: String, message: String) {
        // 1. 알림 채널 생성 (Android 8.0 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT // 중요도 설정
            ).apply {
                description = "앱의 기본 알림을 수신하는 채널입니다."
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // 2. 알림 권한 체크 (Android 13 이상 필수)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // 권한이 없다면 알림을 띄우지 않고 종료 (실제 앱에서는 여기서 권한 요청 창을 띄워야 합니다)
                return
            }
        }

        // 3. 알림 디자인(UI) 빌드
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // 앱의 아이콘으로 교체하세요 (예: R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true) // 사용자가 알림을 터치하면 자동으로 사라짐

        // 4. 알림 띄우기
        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}