package krystian.kryszczak.model.invitation

import com.datastax.oss.driver.api.mapper.annotations.Entity
import com.datastax.oss.driver.api.mapper.annotations.PartitionKey
import com.datastax.oss.driver.api.mapper.annotations.SchemaHint
import io.micronaut.core.annotation.Introspected
import io.micronaut.serde.annotation.Serdeable
import krystian.kryszczak.commons.model.Item
import java.util.*

@Entity
@Introspected
@Serdeable
@SchemaHint(targetElement = SchemaHint.TargetElement.TABLE)
data class FriendInvitation(
    @PartitionKey
    override val id: UUID? = null,
    val from: UUID? = null,
    val to: UUID? = null
): Item(id)
