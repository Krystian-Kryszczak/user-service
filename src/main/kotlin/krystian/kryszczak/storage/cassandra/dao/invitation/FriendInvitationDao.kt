package krystian.kryszczak.storage.cassandra.dao.invitation

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet
import com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet
import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Delete
import com.datastax.oss.driver.api.mapper.annotations.Select
import krystian.kryszczak.commons.storage.cassandra.dao.ItemDao
import krystian.kryszczak.model.invitation.FriendInvitation
import java.util.*

@Dao
interface FriendInvitationDao: ItemDao<FriendInvitation> {
    @Select(customWhereClause = "receiver = :receiver", limit = "100", allowFiltering = true)
    fun findByReceiverId(@CqlName("receiver") receiver: UUID): MappedReactiveResultSet<FriendInvitation>
    @Delete(entityClass = [FriendInvitation::class])
    fun deleteById(id: UUID): ReactiveResultSet
    @Delete(entityClass = [FriendInvitation::class], ifExists = true)
    fun deleteByIdIfExists(id: UUID): MappedReactiveResultSet<Boolean>
}
