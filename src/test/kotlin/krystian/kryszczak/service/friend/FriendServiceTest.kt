package krystian.kryszczak.service.friend

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.uuid.Uuids
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.*
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.*
import io.kotest.matchers.shouldBe
import io.micronaut.security.authentication.Authentication
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.reactivex.rxjava3.core.*
import krystian.kryszczak.commons.testing.fixtures.*
import krystian.kryszczak.model.invitation.FriendInvitation
import krystian.kryszczak.model.invitation.FriendInvitationModel
import krystian.kryszczak.storage.cassandra.dao.friend.FriendDao
import krystian.kryszczak.storage.cassandra.dao.invitation.FriendInvitationDao
import java.util.stream.Collectors
import kotlin.random.Random

@MicronautTest
class FriendServiceTest(
    friendService: FriendService,
    friendDao: FriendDao,
    friendInvitationDao: FriendInvitationDao,
    cqlSession: CqlSession
): FreeSpec({

    val validAuth = Authentication.build(johnSmithUser.email!!, mapOf("id" to johnSmithUser.id.toString()))
    val invitation = FriendInvitation(Uuids.timeBased(), inviter = jimSmithUser.id!!, receiver = johnSmithUser.id!!)

    beforeSpec {
        Flowable.zip(listOf(
            friendDao.save(johnSmithUser),
            friendDao.save(jackSmithUser),
            friendDao.save(jimSmithUser),
            friendDao.save(jasonSmithUser)
        )) { it }
        .blockingSubscribe()
    }

    fun saveInvitation(invitation: FriendInvitation) =
        Flowable.fromPublisher(friendInvitationDao.save(invitation))
            .blockingSubscribe()

    "propose function" - {
        "with authentication parameter" - {
            "should return not empty list without test user and his friends" {
                friendService.propose(validAuth).test().await().values()
                    .shouldNotBeEmpty()
                    .shouldNotContain(johnSmithUser)
                    .shouldNotContainAnyOf(johnSmithUser.friends)
            }

            "should return empty result" {
                friendService.propose(
                    Authentication.build(
                        johnSmithUser.email!!,
                        mapOf("id" to Uuids.timeBased().toString())
                    )
                ).test().await().values().shouldBeEmpty()
            }

            "should throw exception with with excepted message" {
                shouldThrowExactly<NoSuchElementException> {
                    friendService.propose(
                        Authentication.build(johnSmithUser.email!!)
                    ).blockingFirst()
                }
            }
        }

        "with uuid parameter" - {
            "should return not empty list without test user and his friends" {
                friendService.propose(johnSmithUser.id!!).test().await().values()
                    .shouldNotBeEmpty()
                    .shouldNotContain(johnSmithUser)
                    .shouldNotContainAnyOf(johnSmithUser.friends)
            }

            "should return empty result" {
                friendService.propose(Uuids.timeBased()).test().await().values()
                    .shouldBeEmpty()
            }
        }
    }

    "search function" - {
        "with authentication" - {
            "should return not empty result" {
                friendService.search("${johnSmithUser.name} ${johnSmithUser.lastname}", validAuth)
                    .test().await().values().shouldNotBeEmpty()
            }

            "should return empty result" {
                friendService.search("${Random.nextBytes(12)}", validAuth)
                    .test().await().values().shouldBeEmpty()
            }

            "should return empty result" {
                friendService.search("", validAuth)
                    .test().await().values().shouldBeEmpty()
            }
        }

        "without authentication" - {
            "should return not empty result" {
                friendService.search("${johnSmithUser.name} ${johnSmithUser.lastname}")
                    .test().await().values().shouldNotBeEmpty()
            }

            "should return empty result" {
                friendService.search("${Random.nextBytes(12)}")
                    .test().await().values().shouldBeEmpty()
            }

            "should return empty result" {
                friendService.search("")
                    .test().await().values().shouldBeEmpty()
            }
        }
    }

    "friendship list function" - {
        "should return not empty result" {
            friendService.friendshipList(johnSmithUser.id!!)
                .test().await().values().shouldNotBeEmpty()
        }

        "should return empty result" {
            friendService.friendshipList(Uuids.timeBased())
                .test().await().values().shouldBeEmpty()
        }
    }

    "send invitation function" - {
        "should return true and save invitation in database" {
            val inviter = jasonSmithUser.id!!
            val receiver = johnSmithUser.id!!

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                .collect(Collectors.toList())
                .doOnSuccess { it.shouldBeEmpty() }
                .flatMap { friendService.sendInvitation(FriendInvitationModel(inviter, receiver)) }
                .doOnSuccess { it shouldBe true }
                .flatMapPublisher { Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver)) }
                .test().await().values()
                .filter { it.inviter == inviter }
                .shouldNotBeEmpty()
        }

        "should not sent invitation for user in friendship" {
            val inviter = johnSmithUser.id!!
            val receiver = jackSmithUser.friends.first()

            val oldInvitations = Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver)).test().await().values()

            friendService.sendInvitation(FriendInvitationModel(inviter, receiver))
                .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                .filter { !oldInvitations.contains(it) }
                .test().await().values().shouldBeEmpty()
        }

        "should not sent invitation for already invited friend" {
            val inviter = johnSmithUser.id!!
            val receiver = johnSmithUser.friends.first()

            val model = FriendInvitationModel(inviter, receiver)

            friendService.sendInvitation(model)
                .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                .test().await().values().shouldBeEmpty()
        }

        "should return false and not save invitation in database" {
            val inviter = Uuids.timeBased()
            val receiver = jackSmithUser.id!!

            friendService.sendInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                    .test().await().values().shouldBeEmpty()
        }

        "should return false and not save invitation in database" {
            val inviter = johnSmithUser.id!!
            val receiver = Uuids.timeBased()

            friendService.sendInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                    .test().await().values().shouldBeEmpty()
        }
    }

    "invitations function" - {
        "should return not empty result" {
            // prepare
            saveInvitation(invitation)

            friendService.invitations(johnSmithUser.id!!)
                .test().await().values() shouldContain invitation
        }

        "should return empty result" {
            // prepare
            saveInvitation(invitation)

            friendService.invitations(Uuids.timeBased())
                .test().await().values().shouldBeEmpty()
        }

    }

    "accept invitation function" - {
        "with friend invitation model parameter" - {
            "invitation should be accepted successful" {
                // prepare
                saveInvitation(invitation)

                val inviter = invitation.inviter!!
                val receiver = invitation.receiver!!

                friendService.acceptInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe true

                Flowable.fromPublisher(friendInvitationDao.findById(invitation.id!!))
                    .test().await().values().shouldBeEmpty()

                Flowable.fromPublisher(friendDao.findById(receiver))
                    .blockingFirst().friends shouldContain inviter
            }

            "invitation should not be accepted" {
                // prepare
                saveInvitation(invitation)

                val inviter = Uuids.timeBased()
                val receiver = invitation.receiver!!

                friendService.acceptInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe false

                Flowable.fromPublisher(friendDao.findById(receiver))
                    .blockingFirst().friends.shouldNotContain(inviter)
            }
        }

        "with uuid parameter" - {
            "invitation should be accepted successful" {
                // prepare
                saveInvitation(invitation)

                val inviter = invitation.inviter!!
                val receiver = invitation.receiver!!

                friendService.acceptInvitation(invitation.id!!)
                    .blockingGet() shouldBe true

                Flowable.fromPublisher(friendInvitationDao.findById(invitation.id!!))
                    .test().await().values().shouldNotContain(invitation)

                Flowable.fromPublisher(friendDao.findById(receiver))
                    .blockingFirst().friends.shouldContain(inviter)
            }

            "invitation should not be accepted" {
                // prepare
                saveInvitation(invitation)

                val inviter = Uuids.timeBased()
                val receiver = invitation.receiver!!

                friendService.acceptInvitation(Uuids.timeBased())
                    .blockingGet() shouldBe false

                Flowable.fromPublisher(friendDao.findById(receiver))
                    .blockingFirst().friends.shouldNotContain(inviter)
            }
        }
    }

    "deny invitation function" - {
        "with friend invitation model parameter" - {
            "invitation should be denied successful" {
                // prepare
                saveInvitation(invitation)

                val inviter = invitation.inviter!!
                val receiver = invitation.receiver!!

                friendService.denyInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe true

                Flowable.fromPublisher(friendInvitationDao.findById(invitation.id!!))
                    .test().await().values().shouldBeEmpty()
            }

            "invitation should not be denied" {
                // prepare
                saveInvitation(invitation)

                val inviter = Uuids.timeBased()
                val receiver = invitation.receiver!!

                friendService.denyInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe false

                Flowable.fromPublisher(friendInvitationDao.findById(invitation.id!!))
                    .test().await().values().shouldNotBeEmpty()
            }
        }

        "with uuid parameter" - {
            "invitation should be denied successful" {
                // prepare
                saveInvitation(invitation)

                val invitationId = invitation.id!!

                friendService.denyInvitation(invitationId)
                    .blockingGet() shouldBe true

                Flowable.fromPublisher(friendInvitationDao.findById(invitationId))
                    .test().await().values().shouldBeEmpty()
            }

            "invitation should not be denied" {
                // prepare
                saveInvitation(invitation)

                val invitationId = invitation.id!!

                friendService.denyInvitation(Uuids.timeBased())
                    .blockingGet() shouldBe false

                Flowable.fromPublisher(friendInvitationDao.findById(invitationId))
                    .test().await().values() shouldContain invitation
            }
        }
    }

    "remove friend function" - {
        "should remove first friend from user friends list" {
            val userId = johnSmithUser.id!!
            val friendId = johnSmithUser.friends.first()

            friendService.removeFriend(userId, friendId)
                .doOnSuccess { it shouldBe true }
                .flatMapPublisher { Flowable.fromPublisher(friendDao.findById(userId)) }
                .blockingFirst().friends shouldNotContain friendId
        }

        "should not remove not exits user from test user friends list" {
            val userId = johnSmithUser.id!!
            val friendId = Uuids.timeBased()

            friendService.removeFriend(userId, friendId)
                .blockingGet() shouldBe false
        }

        "should not remove test user from friends list of not exits user" {
            val userId = Uuids.timeBased()
            val friendId = johnSmithUser.id!!

            friendService.removeFriend(userId, friendId)
                .blockingGet() shouldBe false
        }
    }

    afterSpec {
        Flowable.zip(listOf(
            friendDao.delete(johnSmithUser),
            friendDao.delete(jackSmithUser),
            friendDao.delete(jimSmithUser),
            friendDao.delete(jasonSmithUser)
        )) { it }
        .blockingSubscribe()
    }

    afterAny {
        Flowable.fromPublisher(cqlSession.executeReactive(truncate("friend_invitation").build()))
            .blockingSubscribe()
    }
})
