package com.waffletoy.internhasha.exceptions

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

data class ErrorResponse(
    val timestamp: String = Instant.now().toString(),
    val message: String,
    val code: String,
    @JsonInclude(JsonInclude.Include.NON_NULL)
    val details: Map<String, Any>? = null,
)
