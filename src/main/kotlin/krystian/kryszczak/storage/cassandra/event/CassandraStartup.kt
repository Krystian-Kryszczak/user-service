package krystian.kryszczak.storage.cassandra.event

import com.datastax.oss.driver.api.core.CqlSession
import com.datastax.oss.driver.api.core.type.DataTypes
import io.micronaut.context.event.StartupEvent
import io.micronaut.runtime.event.annotation.EventListener
import jakarta.inject.Singleton
import krystian.kryszczak.commons.storage.cassandra.event.AbstractCassandraStartup
import org.slf4j.Logger
import org.slf4j.LoggerFactory

@Singleton
class CassandraStartup(cqlSession: CqlSession): AbstractCassandraStartup(cqlSession, logger) {
    @EventListener
    override fun onStartupEvent(event: StartupEvent) {
        createFriendInvitationTable()
    }

    private fun createFriendInvitationTable() = execute(
        getItemTableBase("friend_invitation")
            .withColumn("inviter", DataTypes.TIMEUUID)
            .withColumn("receiver", DataTypes.TIMEUUID)
            .build()
    )

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CassandraStartup::class.java)
    }
}
