package io.kotest.provided

import io.kotest.core.config.AbstractProjectConfig
import io.micronaut.test.extensions.kotest5.MicronautKotest5Extension
import org.testcontainers.containers.CassandraContainer
import org.testcontainers.utility.DockerImageName

object ProjectConfig : AbstractProjectConfig() {
    override fun extensions() = listOf(MicronautKotest5Extension)

    private val cassandra = CassandraContainer(DockerImageName.parse("cassandra:3.11.2"))

    override suspend fun beforeProject() {
        cassandra.start()
    }

    override suspend fun afterProject() {
        cassandra.stop()
    }
}
