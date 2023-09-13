package krystian.kryszczak.service.friend

import io.micronaut.security.authentication.Authentication
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Single
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.model.invitation.FriendInvitation
import krystian.kryszczak.model.invitation.FriendInvitationModel
import java.util.UUID

interface FriendService {
    fun propose(authentication: Authentication): Flowable<User>
    fun propose(clientId: UUID): Flowable<User>
    fun search(query: String, authentication: Authentication? = null): Flowable<User>
    fun friendshipList(clientId: UUID, page: Int = 0): Flowable<UUID>
    fun findFriendsById(id: UUID): Maybe<Set<UUID>>
    fun findFriendsByIdInIds(ids: List<UUID>): Flowable<Set<UUID>>
    fun invitations(id: UUID): Flowable<FriendInvitation>
    fun sendInvitation(invitation: FriendInvitationModel): Single<Boolean>
    fun acceptInvitation(invitation: FriendInvitationModel): Single<Boolean>
    fun acceptInvitation(id: UUID): Single<Boolean>
    fun denyInvitation(invitation: FriendInvitationModel): Single<Boolean>
    fun denyInvitation(id: UUID): Single<Boolean>
    fun removeFriend(id: UUID, friendId: UUID): Single<Boolean>
}
