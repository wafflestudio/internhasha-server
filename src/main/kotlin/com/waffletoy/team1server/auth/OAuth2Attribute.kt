package com.waffletoy.team1server.auth

// Oauth의 정보를 담기 위한 data class
data class OAuth2Attribute(
    val provider: String,
    val attributes: Map<String, Any>,
    val userId: String,
    val username: String,
    val email: String,
) {
    companion object {
        fun of(
            provider: String,
            userNameAttributeName: String,
            attributes: Map<String, Any>,
        ): OAuth2Attribute {
            return when (provider) {
                "google" -> ofGoogle(provider, userNameAttributeName, attributes)
                else -> throw IllegalArgumentException("Unsupported provider: $provider")
            }
        }

        private fun ofGoogle(
            provider: String,
            userNameAttributeName: String,
            attributes: Map<String, Any>,
        ): OAuth2Attribute {
            return OAuth2Attribute(
                provider = provider,
                attributes = attributes,
                userId = attributes[userNameAttributeName]?.toString() ?: "",
                username = attributes["name"]?.toString() ?: "",
                email = attributes["email"]?.toString() ?: "",
            )
        }
    }

    fun mapAttribute(): Map<String, Any> {
        return mapOf(
            "userId" to userId,
            "username" to username,
            "email" to email,
            "provider" to provider,
        )
    }
}
