package com.wafflestudio.internhasha.post.repository

import com.wafflestudio.internhasha.post.domain.Position
import com.wafflestudio.internhasha.post.service.dto.PositionSearchCond
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface QPositionRepository {
    fun searchPosition(
        cond: PositionSearchCond,
        pageable: Pageable,
    ): Page<Position>
}
