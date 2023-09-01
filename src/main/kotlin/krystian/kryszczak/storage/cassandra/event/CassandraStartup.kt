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
        createWatchTable()
    }

    private fun createWatchTable() = execute(
        getItemTableBase("friend_invitation")
            .withColumn("from", DataTypes.TIMEUUID)
            .withColumn("to", DataTypes.TIMEUUID)
            .build()
    )

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(CassandraStartup::class.java)
    }
}
