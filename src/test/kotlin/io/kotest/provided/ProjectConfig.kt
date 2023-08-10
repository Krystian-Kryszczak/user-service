package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.time.Duration

object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)

    private val cassandra = CassandraContainer(DockerImageName.parse("cassandra:3.11.2"))
        .waitingFor(Wait.forListeningPort()
        .withStartupTimeout(Duration.ofSeconds(30)))
        .withExposedPorts(9042)

    override suspend fun beforeProject() {
        cassandra.start()
    }

    override suspend fun afterProject() {
        cassandra.stop()
    }
}
