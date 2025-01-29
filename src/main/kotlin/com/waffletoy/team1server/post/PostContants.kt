package com.waffletoy.team1server.post

// ENUM 타입 정의
enum class Category {
    FRONT,
    APP,
    BACKEND,
    DATA,
    OTHERS,
    DESIGN,
    PLANNER,
    MARKETING,
    ;

    fun displayName(): String {
        return when (this) {
            FRONT -> "프론트엔드 개발"
            APP -> "앱 개발"
            BACKEND -> "백엔드 개발"
            DATA -> "데이터 분석"
            OTHERS -> "기타"
            DESIGN -> "디자인"
            PLANNER -> "기획"
            MARKETING -> "마케팅"
        }
    }
}

enum class Series {
    SEED,
    PRE_A,
    A,
    B,
    C,
    D,
}
