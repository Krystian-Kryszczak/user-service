package krystian.kryszczak.storage.cassandra.dao.invitation

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet
import com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet
import com.datastax.oss.driver.api.mapper.annotations.*
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.storage.cassandra.dao.ItemDao
import krystian.kryszczak.model.invitation.FriendInvitation
import java.util.UUID

@Dao
interface FriendInvitationDao: ItemDao<FriendInvitation> {
    @Select(customWhereClause = "receiver = :receiver", limit = "100", allowFiltering = true)
    fun findByReceiver(@CqlName("receiver") receiver: UUID): MappedReactiveResultSet<FriendInvitation>
    @Delete(entityClass = [FriendInvitation::class])
    fun deleteById(id: UUID): ReactiveResultSet
    @Delete(entityClass = [User::class], ifExists = true)
    fun deleteByIdIfExists(id: UUID): ReactiveResultSet
}
