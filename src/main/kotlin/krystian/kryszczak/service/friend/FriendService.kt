package krystian.kryszczak.service.friend

import io.micronaut.security.authentication.Authentication
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Single
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.model.invitation.FriendInvitation
import krystian.kryszczak.model.invitation.FriendInvitationModel
import java.util.UUID

interface FriendService {
    fun propose(authentication: Authentication): Flowable<User>
    fun propose(clientId: UUID): Flowable<User>
    fun friendshipList(page: Int, clientId: UUID): Flowable<UUID>
    fun invitations(id: UUID): Flowable<FriendInvitation>
    fun sendInvitation(invitation: FriendInvitationModel)
    fun acceptInvitation(invitation: FriendInvitationModel)
    fun acceptInvitation(id: UUID)
    fun denyInvitation(invitation: FriendInvitationModel)
    fun denyInvitation(id: UUID)
    fun removeFriend(id: UUID, friendId: UUID): Single<Boolean>
}
