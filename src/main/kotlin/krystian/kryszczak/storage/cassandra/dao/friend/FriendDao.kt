package krystian.kryszczak.storage.cassandra.dao.friend

import com.datastax.dse.driver.api.mapper.reactive.MappedReactiveResultSet
import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Query
import com.datastax.oss.driver.api.mapper.annotations.Select
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.storage.cassandra.dao.being.user.UserDao
import java.util.UUID

@Dao
interface FriendDao: UserDao {
    @Query("UPDATE user SET friends = friends + {:friendsId} WHERE id = :userId")
    fun addFriend(@CqlName("userId") userId: UUID, @CqlName("friendId") friendId: UUID)
    @Query("UPDATE user SET friends = friends + :friendsIds WHERE id = :userId")
    fun addFriends(@CqlName("userId") userId: UUID, @CqlName("friendIds") friendIds: Set<UUID>)

    @Query("UPDATE user SET friends = friends - {:friendsId} WHERE id = :userId")
    fun removeFriend(@CqlName("userId") userId: UUID, @CqlName("friendId") friendId: UUID)
    @Query("UPDATE user SET friends = friends - :friendsIds WHERE id = :userId")
    fun removeFriends(@CqlName("userId") userId: UUID, @CqlName("friendIds") friendIds: Set<UUID>)

    @Select(customWhereClause = "friends NOT CONTAINS :userId", allowFiltering = true)
    fun findWithoutUserIdInFriends(@CqlName("userId") userId: UUID): MappedReactiveResultSet<User>
}
