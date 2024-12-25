package com.waffletoy.team1server

import com.waffletoy.team1server.pretotype.service.PretotypeService
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ApplicationTests {
    @Autowired
    private val pretotypeService: PretotypeService? = null

    @Test
    fun contextLoads() {
    }

    @Test
    fun whenPretotypeAdded_thenOneItemInList() {
        pretotypeService!!.createPretotype("test@waffle.com", isSubscribed = false)
        assertThat(pretotypeService.listPretotypes(), hasSize(1))
    }
}
