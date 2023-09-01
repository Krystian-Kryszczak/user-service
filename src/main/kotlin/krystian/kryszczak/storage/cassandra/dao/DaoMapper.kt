package krystian.kryszczak.storage.cassandra.dao

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.mapper.MapperBuilder
import com.datastax.oss.driver.api.mapper.annotations.DaoFactory
import com.datastax.oss.driver.api.mapper.annotations.DaoKeyspace
import com.datastax.oss.driver.api.mapper.annotations.Mapper
import krystian.kryszczak.storage.cassandra.dao.friend.FriendDao
import krystian.kryszczak.storage.cassandra.dao.invitation.FriendInvitationDao

@Mapper
interface DaoMapper {
    @DaoFactory
    fun friendDao(@DaoKeyspace keyspace: CqlIdentifier): FriendDao

    @DaoFactory
    fun friendInvitationDao(@DaoKeyspace keyspace: CqlIdentifier): FriendInvitationDao

    @JvmStatic
    fun builder(session: CqlSession): MapperBuilder<DaoMapper> = DaoMapperBuilder(session)
}
