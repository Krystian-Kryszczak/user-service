package krystian.kryszczak.storage.cassandra.dao.friend

import com.datastax.oss.driver.api.mapper.annotations.Dao
import krystian.kryszczak.commons.storage.cassandra.dao.being.user.UserDao

@Dao
interface FriendDao: UserDao
