package krystian.kryszczak.factory.cassandra

import com.datastax.oss.driver.api.core.CqlIdentifier
import com.datastax.oss.driver.api.core.CqlSession
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import krystian.kryszczak.storage.cassandra.dao.DaoMapper
import krystian.kryszczak.storage.cassandra.dao.friend.FriendDao
import krystian.kryszczak.storage.cassandra.dao.invitation.FriendInvitationDao

@Factory
class CassandraFactory(private val cqlSession: CqlSession) {
    private val keyspace: CqlIdentifier = cqlSession.keyspace.get()

    @Bean
    fun daoMapper(): DaoMapper = DaoMapper.builder(cqlSession).build()

    @Bean
    fun friendDao(daoMapper: DaoMapper): FriendDao = daoMapper.friendDao(keyspace)

    @Bean
    fun friendInvitationDao(daoMapper: DaoMapper): FriendInvitationDao = daoMapper.friendInvitationDao(keyspace)
}
