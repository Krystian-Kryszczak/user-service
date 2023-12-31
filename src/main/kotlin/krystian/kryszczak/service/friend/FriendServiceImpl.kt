package krystian.kryszczak.service.friend

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveRow
import io.micronaut.security.authentication.Authentication
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import jakarta.inject.Singleton
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.service.being.user.UserService
import krystian.kryszczak.commons.utils.SecurityUtils
import krystian.kryszczak.commons.extenstion.collection.takeRandom
import krystian.kryszczak.model.invitation.FriendInvitationModel
import krystian.kryszczak.storage.cassandra.dao.friend.FriendDao
import krystian.kryszczak.storage.cassandra.dao.invitation.FriendInvitationDao
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.stream.Collectors

private const val TIMEOUT_SECONDS = 8L

@Singleton
class FriendServiceImpl(
    private val userService: UserService,
    private val friendDao: FriendDao,
    private val friendInvitationDao: FriendInvitationDao
): FriendService {
    private fun <T : Any> Flowable<T>.timeout() = timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS, Flowable.empty())
    private fun Single<Boolean>.timeout() = timeout(TIMEOUT_SECONDS, TimeUnit.SECONDS, Single.just(false))

    override fun propose(authentication: Authentication): Flowable<User> {
        return propose(SecurityUtils.getClientId(authentication) ?: return Flowable.empty())
    }

    override fun propose(clientId: UUID) =
        userService.findFriendsById(clientId)
            .map(Set<UUID>::toMutableList)
            .map { it.takeRandom(4) }
            .flatMapPublisher { friends ->
                friends.findFriendsOfFriendsWithoutClientFriendsAndClient(clientId)
            }.collect(Collectors.toList())
            .map { it.takeRandom(8) }
            .flatMapPublisher(userService::findByIdInIds)
            .switchIfEmpty(
                userService.findById(clientId)
                    .filter { it.lastname != null }
                    .flatMapPublisher { clientUser ->
                        Flowable.fromPublisher(friendDao.searchByLastname(clientUser.lastname!!, 8))
                            .skipWhile { it.id == clientId }
                            .switchIfEmpty(
                                Flowable.fromCompletionStage(friendDao.findAll(8))
                                    .flatMapIterable { paging ->
                                        paging.currentPage()
                                            .filterNot { clientUser.friends.contains(it.id) || it.id == clientId }
                                    }
                            )
                    }
            ).timeout()

    private fun List<UUID>.findFriendsOfFriendsWithoutClientFriendsAndClient(clientId: UUID) =
        userService.findFriendsByIdInIds(this)
            .flatMapIterable { it }
            .skipWhile(this::contains)
            .skipWhile { it == clientId }

    override fun search(query: String, authentication: Authentication?) =
        Single.just(query.split(" ").filter(String::isNotBlank))
            .filter { it.size > 1 }
            .flatMapPublisher {
                userService.search(it.first(), it.last())
            }.timeout()

    override fun friendshipList(clientId: UUID, page: Int) = userService.findFriendsById(clientId)
        .flatMapPublisher { Flowable.fromIterable(it) }

    override fun invitations(id: UUID) = Flowable.fromPublisher(friendInvitationDao.findByReceiver(id)).timeout()

    override fun sendInvitation(invitation: FriendInvitationModel) =
        Flowable.fromPublisher(friendDao.findByIdInIds(listOf(invitation.inviter, invitation.receiver)))
            .filter {
                (it.id == invitation.inviter && !it.friends.contains(invitation.receiver)) ||
                (it.id == invitation.receiver && !it.friends.contains(invitation.inviter))
            }
            .collect(Collectors.toList())
            .filter { it.size == 2 }
            .flatMapPublisher {
                Flowable.fromPublisher(friendInvitationDao.save(invitation.mapToInvitation()))
                    .map(ReactiveRow::wasApplied)
                    .defaultIfEmpty(true)
            }
            .reduce(Boolean::and)
            .defaultIfEmpty(false)
            .timeout()

    override fun acceptInvitation(invitation: FriendInvitationModel) =
        Flowable.fromPublisher(friendInvitationDao.findByReceiver(invitation.receiver))
            .filter { it.inviter == invitation.inviter }
            .flatMapMaybe {
                Flowable.zip(
                    Flowable.fromPublisher(friendDao.addFriends(invitation.inviter, setOf(invitation.receiver)))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true),
                    Flowable.fromPublisher(friendDao.addFriends(invitation.receiver, setOf(invitation.inviter)))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true),
                    Flowable.fromPublisher(friendInvitationDao.deleteById(it.id!!))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true)
                ) { t1, t2, t3 -> t1 && t2 && t3 }
                .reduce(Boolean::and)
            }.reduce(Boolean::and)
            .defaultIfEmpty(false)
            .timeout()

    override fun acceptInvitation(id: UUID) =
        Flowable.fromPublisher(friendInvitationDao.findById(id))
            .flatMapMaybe {
                Flowable.zip(
                    Flowable.fromPublisher(friendDao.addFriends(it.inviter!!, setOf(it.receiver!!)))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true),
                    Flowable.fromPublisher(friendDao.addFriends(it.receiver, setOf(it.inviter)))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true),
                    Flowable.fromPublisher(friendInvitationDao.deleteById(id))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true)
                ) { t1, t2, t3 -> t1 && t2 && t3 }
                .reduce(Boolean::and)
            }.reduce(Boolean::and)
            .defaultIfEmpty(false)
            .timeout()

    override fun denyInvitation(invitation: FriendInvitationModel) =
        Flowable.fromPublisher(friendInvitationDao.findByReceiver(invitation.receiver))
            .filter { it.inviter == invitation.inviter }
            .flatMapMaybe {
                Flowable.fromPublisher(friendInvitationDao.deleteById(it.id!!))
                    .map(ReactiveRow::wasApplied)
                    .defaultIfEmpty(true)
                    .reduce(Boolean::and)
            }.reduce(Boolean::and)
            .defaultIfEmpty(false)
            .timeout()

    override fun denyInvitation(id: UUID) =
        Flowable.fromPublisher(friendInvitationDao.findById(id))
            .flatMapMaybe {
                Flowable.fromPublisher(friendInvitationDao.deleteById(id))
                    .map(ReactiveRow::wasApplied)
                    .defaultIfEmpty(true)
                    .reduce(Boolean::and)
            }.reduce(Boolean::and)
            .defaultIfEmpty(false)
            .timeout()

    override fun removeFriend(id: UUID, friendId: UUID) =
        Flowable.fromPublisher(friendDao.findByIdInIds(listOf(id, friendId)))
            .filter { (it.id == id && it.friends.contains(friendId)) || (it.id == friendId && it.friends.contains(id)) }
            .collect(Collectors.toList())
            .filter { it.size == 2 }
            .flatMapPublisher {
                Flowable.zip(
                    Flowable.fromPublisher(friendDao.removeFriends(id, setOf(friendId)))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true),
                    Flowable.fromPublisher(friendDao.removeFriends(friendId, setOf(id)))
                        .map(ReactiveRow::wasApplied)
                        .defaultIfEmpty(true)
                ) { t1, t2 -> t1 && t2 }
            }
            .reduce(Boolean::and)
            .defaultIfEmpty(false)
            .timeout()
}
