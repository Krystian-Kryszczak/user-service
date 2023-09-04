package krystian.kryszczak.endpoint

import fixtures.user.UserFixtures
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Maybe
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.service.being.user.UserService
import java.util.UUID

@MicronautTest
class UserControllerTest(@Client("/users") client: HttpClient): StringSpec({
    "response body should be test user json" {
        client.toBlocking().retrieve("/${UUID.randomUUID()}", User::class.java) shouldBe UserFixtures.testUser
    }
}) {
    @MockBean(UserService::class)
    fun userService(): UserService {
        val userService = mockk<UserService>()

        every { userService.findById(any()) } returns Maybe.just(UserFixtures.testUser)

        return userService
    }
}
