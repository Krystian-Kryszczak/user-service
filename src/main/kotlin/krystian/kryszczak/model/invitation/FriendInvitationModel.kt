package krystian.kryszczak.model.invitation

import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class FriendInvitationModel(val inviter: UUID? = null, val receiver: UUID? = null) {
    fun mapToInvitation() = FriendInvitation(
        inviter = inviter,
        receiver = receiver
    )
}
