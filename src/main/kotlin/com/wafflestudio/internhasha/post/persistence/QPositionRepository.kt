package com.wafflestudio.internhasha.post.persistence

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QPositionRepository {
    fun searchPosition(
        cond: PositionSearchCond,
        pageable: Pageable,
    ): Page<PositionEntity>
}
