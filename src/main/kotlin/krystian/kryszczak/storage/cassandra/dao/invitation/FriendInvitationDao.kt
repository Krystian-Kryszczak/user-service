package krystian.kryszczak.storage.cassandra.dao.invitation

import com.datastax.oss.driver.api.mapper.annotations.Dao
import krystian.kryszczak.commons.storage.cassandra.dao.ItemDao
import krystian.kryszczak.model.invitation.FriendInvitation

@Dao
interface FriendInvitationDao: ItemDao<FriendInvitation>
