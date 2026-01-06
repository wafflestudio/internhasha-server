package com.wafflestudio.internhasha.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val redisHost: String,
    @Value("\${spring.data.redis.port}") private val redisPort: Int,
    @Value("\${spring.data.redis.database:0}") private val redisDatabase: Int,
) {
    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val config = RedisStandaloneConfiguration(redisHost, redisPort)
        config.database = redisDatabase
        return LettuceConnectionFactory(config)
    }

    @Bean
    fun redisTemplate(redisConnectionFactory: RedisConnectionFactory): RedisTemplate<String, String> {
        val redisTemplate = RedisTemplate<String, String>()
        redisTemplate.connectionFactory = redisConnectionFactory
        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        return redisTemplate
    }
}
