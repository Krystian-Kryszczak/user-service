package io.kotest.provided

import com.datastax.driver.core.Cluster
import com.datastax.driver.core.Session
import com.datastax.oss.driver.api.querybuilder.SchemaBuilder
import io.kotest.core.config.AbstractProjectConfig
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.containers.CassandraContainer.CQL_PORT
import org.testcontainers.utility.DockerImageName

object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)

    private val cassandra = CassandraContainer(DockerImageName.parse("cassandra:3.11.2"))

    private fun getSession(): Session = Cluster.builder()
        .addContactPoint(cassandra.host)
        .withPort(cassandra.getMappedPort(CQL_PORT))
        .withoutJMXReporting()
        .build()
        .connect()

    override suspend fun beforeProject() {
        cassandra.start()

        getSession().execute(
            SchemaBuilder.createKeyspace(System.getProperty("CASSANDRA_KEYSPACE", "app")).ifNotExists()
                .withSimpleStrategy(2)
                .build()
                .query
        )

        System.setProperty("CASSANDRA_HOST", cassandra.host)
        System.setProperty("CASSANDRA_PORT", cassandra.getMappedPort(9042).toString())
    }

    override suspend fun afterProject() {
        cassandra.stop()
    }
}
