package com.waffletoy.team1server.config

import org.springframework.boot.actuate.health.HealthEndpointGroups
import org.springframework.boot.actuate.health.HealthEndpointGroupsPostProcessor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class HealthCheckConfig {
    @Bean
    fun healthEndpointGroupsPostProcessor(): HealthEndpointGroupsPostProcessor {
        return HealthEndpointGroupsPostProcessor { groups ->
            groups
                .addAdditionalHealthEndpointGroup("liveness") { builder ->
                    builder.include("livenessState")
                }
                .addAdditionalHealthEndpointGroup("readiness") { builder ->
                    builder.include("readinessState")
                }
                .addAdditionalHealthEndpointGroup("startup") { builder ->
                    builder.include("startupState")
                }
        }
    }
}