package krystian.kryszczak.model.invitation

import com.datastax.oss.driver.api.core.uuid.Uuids
import io.micronaut.serde.annotation.Serdeable
import java.util.UUID

@Serdeable
data class FriendInvitationModel(val inviter: UUID, val receiver: UUID) {
    fun mapToInvitation() = FriendInvitation(Uuids.timeBased(), inviter, receiver)
}
