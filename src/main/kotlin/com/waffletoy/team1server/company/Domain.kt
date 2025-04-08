package com.waffletoy.team1server.company

enum class Domain {
    FINTECH,
    HEALTHTECH,
    EDUCATION,
    ECOMMERCE,
    OTHERS,
    MOBILITY,
    CONTENTS,
    B2B,
    FOODTECH,
    ;

    fun displayName(): String {
        return when (this) {
            FINTECH -> "금융기술"
            HEALTHTECH -> "헬스케어 기술"
            EDUCATION -> "교육"
            ECOMMERCE -> "전자상거래"
            OTHERS -> "기타"
            MOBILITY -> "모빌리티"
            CONTENTS -> "콘텐츠"
            B2B -> "기업간 거래 (B2B)"
            FOODTECH -> "식품기술"
        }
    }
}
