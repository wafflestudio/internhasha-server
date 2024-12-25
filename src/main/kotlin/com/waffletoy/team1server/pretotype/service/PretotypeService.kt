package com.waffletoy.team1server.pretotype.service

import com.waffletoy.team1server.pretotype.PretotypeEmailConflictException
import com.waffletoy.team1server.pretotype.controller.Pretotype
import com.waffletoy.team1server.pretotype.persistence.PretotypeEntity
import com.waffletoy.team1server.pretotype.persistence.PretotypeRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class PretotypeService(
    private val pretotypeRepository: PretotypeRepository,
) {
    @Transactional
    fun createPretotype(
        email: String,
        isSubscribed: Boolean,
    ): Pretotype {
        pretotypeRepository.findByEmail(email) ?.let {
            throw PretotypeEmailConflictException()
        } ?: run {
            val pretotypeEntity =
                PretotypeEntity(
                    email = email,
                    isSubscribed = isSubscribed,
                    createdAt = Instant.now(),
                )
            pretotypeRepository.save(pretotypeEntity)
            return Pretotype.fromEntity(pretotypeEntity)
        }
    }

    fun listPretotypes(): List<Pretotype> {
        return pretotypeRepository.findAll().map {
            Pretotype.fromEntity(it)
        }
    }
}
