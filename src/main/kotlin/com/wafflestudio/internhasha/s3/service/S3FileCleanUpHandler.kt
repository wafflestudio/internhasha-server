package com.wafflestudio.internhasha.s3.service

import com.wafflestudio.internhasha.common.event.S3FileCleanupEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class S3FileCleanUpHandler(
    private val s3Service: S3Service,
    private val applicationScope: CoroutineScope,
) {
    private val logger = LoggerFactory.getLogger(S3FileCleanUpHandler::class.java)

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun handleS3Cleanup(event: S3FileCleanupEvent) {
        applicationScope.launch {
            try {
                s3Service.deleteS3Files(event.s3Files)
            } catch (e: Exception) {
                logger.error(e.message)
            }
        }
    }
}
