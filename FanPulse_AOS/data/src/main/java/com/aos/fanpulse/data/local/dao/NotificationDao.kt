package com.aos.fanpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.aos.fanpulse.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /**
     * 1. 새로운 알림 추가 (Insert)
     * FCM을 통해 새로운 푸시 알림이 들어오면 이 함수를 통해 로컬 DB에 저장합니다.
     * 동일한 ID가 부딪힐 경우를 대비해 덮어쓰기(REPLACE) 전략을 취합니다.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    /**
     * 2. 전체 알림 목록 조회 (최신순 정렬)
     * '전체' 탭에서 보여줄 리스트입니다. 최근에 받은 알림이 맨 위로 오도록
     * receivedAt(받은 시간)을 기준으로 내림차순(DESC) 정렬하여 Flow 형태로 가져옵니다.
     */
    @Query("SELECT * FROM notification_table ORDER BY receivedAt DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    /**
     * 3. 읽지 않은 알림 목록 조회 (최신순 정렬)
     * '읽지 않음' 탭에서 사용할 리스트입니다.
     * isRead 값이 0(false)인 데이터만 필터링하여 최신순으로 가져옵니다.
     */
    @Query("SELECT * FROM notification_table WHERE isRead = 0 ORDER BY receivedAt DESC")
    fun getUnreadNotifications(): Flow<List<NotificationEntity>>

    /**
     * 4. 특정 알림 하나만 읽음 처리 (Update)
     * 사용자가 알림 리스트에서 특정 아이템을 터치했을 때 호출됩니다.
     * 해당 id를 가진 행을 찾아 클릭 시점에 isRead 값을 true(1)로 변경합니다.
     */
    @Query("UPDATE notification_table SET isRead = :isRead WHERE id = :id")
    suspend fun updateReadStatus(id: Int, isRead: Boolean)

    /**
     * 5. 모든 알림을 한 번에 읽음 처리 (Bulk Update)
     * 상단의 "모두 읽음 처리" 텍스트를 눌렀을 때 작동합니다.
     * 테이블에 존재하는 모든 알림 데이터의 isRead 컬럼을 무조건 1(true)로 갱신합니다.
     */
    @Query("UPDATE notification_table SET isRead = 1")
    suspend fun markAllAsRead()

    /**
     * 6. 특정 알림 데이터 삭제 (Delete)
     * 사용자가 알림을 옆으로 밀거나(Swipe) 삭제 버튼을 눌러 개별 삭제할 때 사용합니다.
     */
    @Delete
    suspend fun deleteNotification(notification: NotificationEntity)

    /**
     * 7. 모든 알림 데이터 초기화 (전체 삭제)
     * 알림 센터 내의 보관 기록을 한 번에 싹 비우고 싶을 때 사용하는 보너스 함수입니다.
     */
    @Query("DELETE FROM notification_table")
    suspend fun deleteAllNotifications()
}