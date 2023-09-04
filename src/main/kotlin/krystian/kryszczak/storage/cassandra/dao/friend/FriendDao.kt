package krystian.kryszczak.storage.cassandra.dao.friend

import com.datastax.oss.driver.api.mapper.annotations.CqlName
import com.datastax.oss.driver.api.mapper.annotations.Dao
import com.datastax.oss.driver.api.mapper.annotations.Query
import krystian.kryszczak.commons.storage.cassandra.dao.being.user.UserDao
import java.util.UUID

@Dao
interface FriendDao: UserDao {
    @Query("UPDATE user SET friends = friends + :friendsId WHERE id = :id")
    fun addFriend(@CqlName("id") id: UUID, @CqlName("friendId") friendId: UUID)
    @Query("UPDATE user SET friends = friends + :friendsIds WHERE id = :id")
    fun addFriends(@CqlName("id") id: UUID, @CqlName("friendIds") friendIds: Set<UUID>)

    @Query("UPDATE user SET friends = friends - :friendsId WHERE id = :id")
    fun removeFriend(@CqlName("id") id: UUID, @CqlName("friendId") friendId: UUID)
    @Query("UPDATE user SET friends = friends - :friendsIds WHERE id = :id")
    fun removeFriends(@CqlName("id") id: UUID, @CqlName("friendIds") friendIds: Set<UUID>)
}
