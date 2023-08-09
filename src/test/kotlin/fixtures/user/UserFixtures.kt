package fixtures.user

import com.datastax.oss.driver.api.core.uuid.Uuids
import krystian.kryszczak.commons.model.being.user.User
import java.time.LocalDate
import java.time.temporal.ChronoUnit

object UserFixtures {
    val testUser: User = User(
        Uuids.timeBased(),
        "John",
        "Smith",
        "john.smith@example.com",
        "555 555 555",
        LocalDate.EPOCH.until(LocalDate.of(2003, 7, 25), ChronoUnit.DAYS).toInt(),
        setOf(),
        0,
        null
    )
}
