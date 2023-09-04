package krystian.kryszczak.service.friend

import io.micronaut.security.authentication.Authentication
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import jakarta.inject.Singleton
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.service.being.user.UserService
import krystian.kryszczak.commons.utils.SecurityUtils
import krystian.kryszczak.model.invitation.FriendInvitation
import krystian.kryszczak.model.invitation.FriendInvitationModel
import krystian.kryszczak.storage.cassandra.dao.friend.FriendDao
import krystian.kryszczak.storage.cassandra.dao.invitation.FriendInvitationDao
import java.util.UUID

@Singleton
class FriendServiceImpl(
    private val userService: UserService,
    private val friendDao: FriendDao,
    private val friendInvitationDao: FriendInvitationDao
): FriendService {
    override fun propose(authentication: Authentication): Flowable<User> {
        return propose(SecurityUtils.getClientId(authentication) ?: return Flowable.empty())
    }

    override fun propose(clientId: UUID): Flowable<User> =
        userService.findById(clientId)
        .map(User::friends)
        .flatMapPublisher { friends ->
            Flowable.fromIterable(friends)
                .flatMapMaybe(userService::findById)
                .flatMapIterable(User::friends)
                .skipWhile(friends::contains)
                .flatMapMaybe(userService::findById)
        }

    override fun friendshipList(page: Int, clientId: UUID) =
        Maybe.fromPublisher(friendDao.findById(clientId))
            .map(User::friends)
            .flatMapPublisher { Flowable.fromIterable(it) }

    override fun invitations(id: UUID): Flowable<FriendInvitation> = Flowable.fromPublisher(friendInvitationDao.findByReceiverId(id))

    override fun sendInvitation(invitation: FriendInvitationModel) {
        Single.fromPublisher(friendInvitationDao.save(invitation.mapToInvitation()))
            .subscribe()
    }

    override fun acceptInvitation(invitation: FriendInvitationModel) {
        Single.just(invitation)
            .filter { it.inviter != null && it.receiver != null }
            .flatMap {
                Maybe.fromPublisher(friendInvitationDao.deleteIfExist(invitation.mapToInvitation()))
            }.filter { it }
            .doOnSuccess {
                friendDao.addFriend(invitation.inviter!!, invitation.receiver!!)
                friendDao.addFriend(invitation.receiver, invitation.inviter)
            }.subscribe()
    }

    override fun acceptInvitation(id: UUID) {
        friendInvitationDao.findById(id)
    }

    override fun denyInvitation(invitation: FriendInvitationModel) {
        Single.fromPublisher(friendInvitationDao.deleteIfExist(invitation.mapToInvitation()))
            .subscribe()
    }

    override fun denyInvitation(id: UUID) {
        Single.fromPublisher(friendInvitationDao.deleteByIdIfExists(id))
            .subscribe()
    }

    override fun removeFriend(id: UUID, friendId: UUID) =
        Single.fromCallable {
            friendDao.removeFriend(id, friendId)
            friendDao.removeFriend(friendId, id)
        }.map { true }
        .onErrorReturnItem(false)
}
