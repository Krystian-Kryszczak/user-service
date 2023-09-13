package krystian.kryszczak.storage.cassandra.dao.friend

import com.datastax.dse.driver.api.core.cql.reactive.ReactiveResultSet
import com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet
import com.datastax.oss.driver.api.mapper.annotations.*
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.storage.cassandra.dao.being.user.UserDao
import java.util.UUID

@Dao
interface FriendDao: UserDao {
    @Select(customWhereClause = "id = :id")
    fun findFriendsById(@CqlName("id") id: UUID): MappedReactiveResultSet<User>
    @Select(customWhereClause = "id IN :ids")
    fun findFriendsByIdInIds(@CqlName("ids") ids: List<UUID>): MappedReactiveResultSet<User>
    @Select(customWhereClause = "lastname LIKE :lastname", limit = ":l")
    fun searchByLastname(@CqlName("lastname") lastname: String, @CqlName("l") limit: Int): MappedReactiveResultSet<User>
    @Query("UPDATE user SET friends = friends + :friendsIds WHERE id = :id;")
    fun addFriends(@CqlName("id") id: UUID, @CqlName("friendsIds") friendsIds: Set<UUID>): ReactiveResultSet
    @Query("UPDATE user SET friends = friends - :friendsIds WHERE id = :id;")
    fun removeFriends(@CqlName("id") id: UUID, @CqlName("friendsIds") friendsIds: Set<UUID>): ReactiveResultSet
}
