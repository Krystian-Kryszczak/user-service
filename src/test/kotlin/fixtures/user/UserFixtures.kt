package fixtures.user

import com.datastax.oss.driver.api.core.uuid.Uuids
import krystian.kryszczak.commons.model.being.user.User
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val testUserId = Uuids.timeBased()
private val secondTestUserId = Uuids.timeBased()

val testUser = User(
    testUserId,
    "John",
    "Smith",
    "john.smith@example.com",
    "555 555 555",
    LocalDate.EPOCH.until(LocalDate.of(2003, 7, 25), ChronoUnit.DAYS).toInt(),
    setOf(Uuids.timeBased()),
    0,
    null
)

val secondTestUser = User(
    secondTestUserId,
    "Jack",
    "Smith",
    "jack.smith@example.com",
    "585 585 585",
    LocalDate.EPOCH.until(LocalDate.of(2003, 7, 25), ChronoUnit.DAYS).toInt(),
    setOf(Uuids.timeBased()),
    0,
    null
)
