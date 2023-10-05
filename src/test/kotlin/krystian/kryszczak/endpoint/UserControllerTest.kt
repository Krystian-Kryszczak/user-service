package krystian.kryszczak.endpoint

import com.datastax.oss.driver.api.core.uuid.Uuids
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.shouldBe
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.service.being.user.UserService
import krystian.kryszczak.commons.testing.fixtures.*

@MicronautTest
class UserControllerTest(@Client("/users") httpClient: HttpClient, jwtTokenGenerator: JwtTokenGenerator): FreeSpec({

    val client = httpClient.toBlocking()
    val accessToken = jwtTokenGenerator.generateToken(
        Authentication.build(
            johnSmithUser.email,
            mapOf("id" to johnSmithUser.id!!)
        ),
        3600
    ).orElseThrow()

    "/users endpoints tests" - {
        "should return test user" {
            val response = client.exchange(
                HttpRequest.GET<String>("/${Uuids.timeBased()}")
                    .accept(MediaType.APPLICATION_JSON),
                User::class.java
            )
            response.status shouldBe HttpStatus.OK
            response.body() shouldBe johnSmithUser
        }

        "should return list of test users" {
            val response = client.exchange(
                HttpRequest.GET<String>("/${List(4) {Uuids.timeBased()}.joinToString(",")}")
                    .accept(MediaType.APPLICATION_JSON_STREAM),
                Argument.listOf(User::class.java)
            )
            response.status shouldBe HttpStatus.OK
            response.body() shouldBe List(4) {johnSmithUser}
        }

        "should throw http client response exception with `Forbidden` message" {
            shouldThrowWithMessage<HttpClientResponseException> ("Forbidden") {
                client.exchange(
                    HttpRequest.GET<String>("")
                        .bearerAuth(accessToken),
                    User::class.java
                )
            }
        }

        "should throw http client response exception with `Unauthorized` message" {
            shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                client.exchange(
                    HttpRequest.GET<String>(""),
                    User::class.java
                )
            }
        }
    }
}) {
    @MockBean(UserService::class)
    fun userService(): UserService {
        val userService = mockk<UserService>()

        every { userService.findById(any()) } returns Maybe.just(johnSmithUser)
        every { userService.findByIdInIds(any()) } returns Flowable.fromIterable(List(4) {johnSmithUser})

        return userService
    }
}
