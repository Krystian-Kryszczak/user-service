package krystian.kryszczak.endpoint

import com.datastax.oss.driver.api.core.uuid.Uuids
import fixtures.user.testUser
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.assertions.throwables.shouldThrowWithMessage
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.micronaut.core.type.Argument
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus.OK
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.token.jwt.generator.JwtTokenGenerator
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.mockk.every
import io.mockk.mockk
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.model.invitation.FriendInvitation
import krystian.kryszczak.model.invitation.FriendInvitationModel
import krystian.kryszczak.service.friend.FriendService
import java.util.UUID

@MicronautTest
class FriendControllerTest(@Client("/friends") httpClient: Rx3HttpClient, jwtTokenGenerator: JwtTokenGenerator) : FreeSpec({

    val client = httpClient.toBlocking()
    val accessToken = jwtTokenGenerator.generateToken(
        Authentication.build(
            testUser.email,
            mapOf("id" to testUser.id!!)
        ),
        3600
    ).orElseThrow()

    "/friends endpoints tests" - {
        val endpoint = "/"

        "response of GET (friendship list) request" - {
            "should return default page of user friendship list" {
                val response = client.exchange(
                    HttpRequest.GET<String>(endpoint)
                        .bearerAuth(accessToken),
                    Argument.listOf(UUID::class.java)
                )
                response.status shouldBe OK
                response.body().shouldNotBeEmpty()
            }

            "should return 1st page of user friendship list" {
                val response = client.exchange(
                    HttpRequest.GET<String>("$endpoint/1")
                        .bearerAuth(accessToken),
                    Argument.listOf(UUID::class.java)
                )
                response.status shouldBe OK
                response.body().shouldNotBeEmpty()
            }

            "should throw http client response exception with `Forbidden` message" {
                shouldThrowWithMessage<HttpClientResponseException> ("Forbidden") {
                    client.exchange(
                        HttpRequest.GET<String>("$endpoint/1000")
                            .bearerAuth(accessToken),
                        Argument.listOf(UUID::class.java)
                    )
                }
            }

            "should throw http client response exception with `Unauthorized` message" {
                shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                    client.exchange(
                        HttpRequest.GET<String>("$endpoint/100"),
                        Argument.listOf(UUID::class.java)
                    )
                }
            }
        }

        "response of POST (invite) request" - {
            "should return OK status" {
                val response = client.exchange(
                    HttpRequest.POST(endpoint, Uuids.timeBased())
                        .bearerAuth(accessToken),
                    String::class.java
                )
                response.status shouldBe OK
            }

            "should throw http client response exception" {
                shouldThrowExactly<HttpClientResponseException> {
                    client.exchange(
                        HttpRequest.POST(endpoint, "")
                            .bearerAuth(accessToken),
                        String::class.java
                    )
                }
            }

            "should throw http client response exception with `Unauthorized` message" {
                shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                    client.exchange(
                        HttpRequest.POST(endpoint, Uuids.timeBased()),
                        String::class.java
                    )
                }
            }
        }

        "response of DELETE (remove) request" - {
            "should return OK status" {
                val response = client.exchange(
                    HttpRequest.DELETE(endpoint, Uuids.timeBased())
                        .bearerAuth(accessToken),
                    String::class.java
                )
                response.status shouldBe OK
            }

            "should throw http client response exception" {
                shouldThrowExactly<HttpClientResponseException> {
                    client.exchange(
                        HttpRequest.DELETE<UUID>(endpoint)
                            .bearerAuth(accessToken),
                        String::class.java
                    )
                }
            }

            "should throw http client response exception with `Unauthorized` message" {
                shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                    client.exchange(
                        HttpRequest.DELETE(endpoint, Uuids.timeBased()),
                        String::class.java
                    )
                }
            }
        }
    }

    "/friends/invitations endpoints tests" - {
        val endpoint = "/invitations"

        "response of GET (invitations) request" - {
            "should return list of friend invitations" {
                val response = client.exchange(
                    HttpRequest.GET<String>(endpoint)
                        .bearerAuth(accessToken),
                    Argument.listOf(FriendInvitation::class.java)
                )
                response.status shouldBe OK
                response.body().shouldNotBeEmpty()
            }

            "should throw http client response exception with `Unauthorized` message" {
                shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                    client.exchange(
                        HttpRequest.GET<String>(endpoint),
                        String::class.java
                    )
                }
            }
        }

        "response of POST (invitations) request" - {
            "should return OK status" {
                val response = client.exchange(
                    HttpRequest.POST(endpoint, Uuids.timeBased())
                        .bearerAuth(accessToken),
                    String::class.java
                )
                response.status shouldBe OK
            }

            "should throw http client response exception with `Forbidden` message" {
                shouldThrowWithMessage<HttpClientResponseException> ("Bad Request") {
                    client.exchange(
                        HttpRequest.POST(endpoint, "")
                            .bearerAuth(accessToken),
                        String::class.java
                    )
                }
            }

            "should throw http client response exception with `Unauthorized` message" {
                shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                    client.exchange(
                        HttpRequest.POST(endpoint, Uuids.timeBased()),
                        String::class.java
                    )
                }
            }
        }

        "response of DELETE (invitations) request" - {
            "should return OK status" {
                val response = client.exchange(
                    HttpRequest.DELETE(endpoint, Uuids.timeBased())
                        .bearerAuth(accessToken),
                    String::class.java
                )
                response.status shouldBe OK
            }

            "should throw http client response exception" {
                shouldThrowExactly<HttpClientResponseException> {
                    client.exchange(
                        HttpRequest.DELETE<String>(endpoint)
                            .bearerAuth(accessToken),
                        String::class.java
                    )
                }
            }

            "should throw http client response exception with `Unauthorized` message " {
                shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                    client.exchange(
                        HttpRequest.DELETE(endpoint, Uuids.timeBased()),
                        String::class.java
                    )
                }
            }
        }
    }

    "response of GET (propose) request" - {
        val endpoint = "/propose"

        "should return list of proposed friends" {
            val response = client.exchange(
                HttpRequest.GET<String>(endpoint)
                    .bearerAuth(accessToken),
                Argument.listOf(User::class.java)
            )
            response.status shouldBe OK
            response.body().shouldNotBeEmpty()
        }

        "should throw http client response exception with `Unauthorized` message" {
            shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                client.exchange(
                    HttpRequest.GET<String>(endpoint),
                    Argument.listOf(User::class.java)
                )
            }
        }
    }

    "response of GET (search) request" - {
        val endpoint = "/search"

        "should return list of found friends" {
            val response = client.exchange(
                HttpRequest.GET<String>("$endpoint/${testUser.name}")
                    .bearerAuth(accessToken),
                Argument.listOf(User::class.java)
            )
            response.status shouldBe OK
            response.body().shouldNotBeEmpty()
        }

        "should throw http client response exception with `Forbidden` message" {
            shouldThrowWithMessage<HttpClientResponseException> ("Forbidden") {
                client.exchange(
                    HttpRequest.GET<String>(endpoint)
                        .bearerAuth(accessToken),
                    Argument.listOf(User::class.java)
                )
            }
        }

        "should throw http client response exception with `Unauthorized` message" {
            shouldThrowWithMessage<HttpClientResponseException> ("Unauthorized") {
                client.exchange(
                    HttpRequest.GET<String>("$endpoint/${testUser.name}"),
                    Argument.listOf(User::class.java)
                )
            }
        }
    }
}) {
    @MockBean(FriendService::class)
    fun friendService(): FriendService {
        val service = mockk<FriendService>()

        every { service.propose(any<Authentication>()) } returns Flowable.just(testUser)
        every { service.propose(any<UUID>()) } returns Flowable.just(testUser)
        every { service.search(any(), any()) } returns Flowable.just(testUser)
        every { service.invitations(any()) } returns Flowable.just(FriendInvitation(Uuids.timeBased(), Uuids.timeBased(), Uuids.timeBased()))
        every { service.friendshipList(any(), any()) } returns Flowable.just(Uuids.timeBased())
        every { service.sendInvitation(any()) } returns Single.just(true)
        every { service.acceptInvitation(any<FriendInvitationModel>()) } returns Single.just(true)
        every { service.acceptInvitation(any<UUID>()) } returns Single.just(true)
        every { service.denyInvitation(any<FriendInvitationModel>()) } returns Single.just(true)
        every { service.denyInvitation(any<UUID>()) } returns Single.just(true)
        every { service.removeFriend(any(), any()) } returns Single.just(true)

        return service
    }
}
