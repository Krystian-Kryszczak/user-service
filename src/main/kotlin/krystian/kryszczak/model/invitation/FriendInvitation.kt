package krystian.kryszczak.model.invitation

import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import com.datastax.oss.driver.api.mapper.annotations.SchemaHint
import io.micronaut.serde.annotation.Serdeable
import krystian.kryszczak.commons.model.Item
import java.util.UUID

@Entity
@Serdeable
@SchemaHint(targetElement = SchemaHint.TargetElement.TABLE)
data class FriendInvitation(
    @PartitionKey
    override val id: UUID? = null,
    val inviter: UUID? = null,
    val receiver: UUID? = null
): Item(id)
