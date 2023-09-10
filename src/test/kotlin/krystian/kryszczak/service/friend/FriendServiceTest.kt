package krystian.kryszczak.service.friend

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.uuid.Uuids
import com.datastax.oss.driver.api.querybuilder.QueryBuilder.*
import fixtures.user.*
import io.kotest.assertions.throwables.shouldThrowExactly
import io.kotest.core.spec.style.FreeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.shouldBe
import io.micronaut.security.authentication.Authentication
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import io.reactivex.rxjava3.core.*
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

    val validAuth = Authentication.build(testUser.email!!, mapOf("id" to testUser.id.toString()))
    val invitation = FriendInvitation(Uuids.timeBased(), inviter = thirdTestUser.id!!, receiver = testUser.id!!)

    beforeSpec {
        Flowable.zip(listOf(
            friendDao.save(testUser),
            friendDao.save(secondTestUser),
            friendDao.save(thirdTestUser),
            friendDao.save(fourthTestUser)
        )) { it }
        .blockingSubscribe()
    }

    fun saveInvitation(invitation: FriendInvitation) =
        Flowable.fromPublisher(friendInvitationDao.save(invitation))
            .blockingSubscribe()

    "propose function" - {
        "with authentication parameter" - {
            "should return list with test user" {
                friendService.propose(validAuth)
                    .blockingIterable() shouldContain testUser
            }

            "should return empty result" {
                friendService.propose(
                    Authentication.build(
                        testUser.email!!,
                        mapOf("id" to Uuids.timeBased().toString())
                    )
                ).blockingIterable().shouldBeEmpty()
            }

            "should throw exception with with excepted message" {
                shouldThrowExactly<NoSuchElementException> {
                    friendService.propose(
                        Authentication.build(testUser.email!!)
                    ).blockingFirst()
                }
            }
        }

        "with uuid parameter" - {
            "should return list with test user" {
                friendService.propose(testUser.id!!)
                    .blockingIterable() shouldContain testUser
            }

            "should return empty result" {
                friendService.propose(Uuids.timeBased())
                    .blockingIterable().shouldBeEmpty()
            }
        }
    }

    "search function" - {
        "with authentication" - {
            "should return not empty result" {
                friendService.search("${testUser.name} ${testUser.lastname}", validAuth)
                    .blockingIterable().shouldNotBeEmpty()
            }

            "should return empty result" {
                friendService.search("${Random.nextBytes(12)}", validAuth)
                    .blockingIterable().shouldBeEmpty()
            }

            "should return empty result" {
                friendService.search("", validAuth)
                    .blockingIterable().shouldBeEmpty()
            }
        }

        "without authentication" - {
            "should return not empty result" {
                friendService.search("${testUser.name} ${testUser.lastname}")
                    .blockingIterable().shouldNotBeEmpty()
            }

            "should return empty result" {
                friendService.search("${Random.nextBytes(12)}")
                    .blockingIterable().shouldBeEmpty()
            }

            "should return empty result" {
                friendService.search("")
                    .blockingIterable().shouldBeEmpty()
            }
        }
    }

    "friendship list function" - {
        "should return not empty result" {
            friendService.friendshipList(testUser.id!!)
                .blockingIterable().shouldNotBeEmpty()
        }

        "should return empty result" {
            friendService.friendshipList(Uuids.timeBased())
                .blockingIterable().shouldBeEmpty()
        }
    }

    "send invitation function" - {
        "should return true and save invitation in database" {
            val inviter = fourthTestUser.id!!
            val receiver = testUser.id!!

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                .collect(Collectors.toList())
                .doOnSuccess { it.shouldBeEmpty() }
                .flatMap { friendService.sendInvitation(FriendInvitationModel(inviter, receiver)) }
                .doOnSuccess { it shouldBe true }
                .flatMapPublisher { Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver)) }
                .blockingIterable()
                .filter { it.inviter == inviter }
                .shouldNotBeEmpty()
        }

        "should not sent invitation for user in friendship" {
            val inviter = testUser.id!!
            val receiver = secondTestUser.friends.first()

            val oldInvitations = Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver)).blockingIterable()

            friendService.sendInvitation(FriendInvitationModel(inviter, receiver))
                .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                .filter { !oldInvitations.contains(it) }
                .blockingIterable().shouldBeEmpty()
        }

        "should not sent invitation for already invited friend" {
            val inviter = testUser.id!!
            val receiver = testUser.friends.first()

            val model = FriendInvitationModel(inviter, receiver)

            friendService.sendInvitation(model)
                .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                .blockingIterable().shouldBeEmpty()
        }

        "should return false and not save invitation in database" {
            val inviter = Uuids.timeBased()
            val receiver = secondTestUser.id!!

            friendService.sendInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                    .blockingIterable().shouldBeEmpty()
        }

        "should return false and not save invitation in database" {
            val inviter = testUser.id!!
            val receiver = Uuids.timeBased()

            friendService.sendInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe false

            Flowable.fromPublisher(friendInvitationDao.findByReceiver(receiver))
                    .blockingIterable().shouldBeEmpty()
        }
    }

    "invitations function" - {
        "should return not empty result" {
            // prepare
            saveInvitation(invitation)

            friendService.invitations(testUser.id!!)
                .blockingIterable() shouldContain invitation
        }

        "should return empty result" {
            // prepare
            saveInvitation(invitation)

            friendService.invitations(Uuids.timeBased())
                .blockingIterable().shouldBeEmpty()
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
                    .blockingIterable().shouldBeEmpty()

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
                    .blockingIterable().shouldNotContain(invitation)

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
                    .blockingIterable().shouldBeEmpty()
            }

            "invitation should not be denied" {
                // prepare
                saveInvitation(invitation)

                val inviter = Uuids.timeBased()
                val receiver = invitation.receiver!!

                friendService.denyInvitation(FriendInvitationModel(inviter, receiver))
                    .blockingGet() shouldBe false

                Flowable.fromPublisher(friendInvitationDao.findById(invitation.id!!))
                    .blockingIterable().shouldNotBeEmpty()
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
                    .blockingIterable().shouldBeEmpty()
            }

            "invitation should not be denied" {
                // prepare
                saveInvitation(invitation)

                val invitationId = invitation.id!!

                friendService.denyInvitation(Uuids.timeBased())
                    .blockingGet() shouldBe false

                Flowable.fromPublisher(friendInvitationDao.findById(invitationId))
                    .blockingIterable() shouldContain invitation
            }
        }
    }

    "remove friend function" - {
        "should remove first friend from user friends list" {
            val userId = testUser.id!!
            val friendId = testUser.friends.first()

            friendService.removeFriend(userId, friendId)
                .doOnSuccess { it shouldBe true }
                .flatMapPublisher { Flowable.fromPublisher(friendDao.findById(userId)) }
                .blockingFirst().friends shouldNotContain friendId
        }

        "should not remove not exits user from test user friends list" {
            val userId = testUser.id!!
            val friendId = Uuids.timeBased()

            friendService.removeFriend(userId, friendId)
                .blockingGet() shouldBe false
        }

        "should not remove test user from friends list of not exits user" {
            val userId = Uuids.timeBased()
            val friendId = testUser.id!!

            friendService.removeFriend(userId, friendId)
                .blockingGet() shouldBe false
        }
    }

    afterSpec {
        Flowable.zip(listOf(
            friendDao.delete(testUser),
            friendDao.delete(secondTestUser),
            friendDao.delete(thirdTestUser),
            friendDao.delete(fourthTestUser)
        )) { it }
        .blockingSubscribe()
    }

    afterAny {
        Flowable.fromPublisher(cqlSession.executeReactive(truncate("friend_invitation").build()))
            .blockingSubscribe()
    }
})
