package com.android.notikeep.domain.usecase

import com.android.notikeep.domain.model.AppNotification
import com.android.notikeep.domain.repository.NotificationRepository
import javax.inject.Inject

/**
 * 알림 1건을 DB에 저장하는 UseCase.
 * NotiKeepListenerService에서 알림 수신 시 호출.
 */
class SaveNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    /** @param notification 저장할 알림 도메인 모델 */
    suspend operator fun invoke(notification: AppNotification) =
        repository.saveNotification(notification)
}
