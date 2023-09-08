package fixtures.user

import com.datastax.oss.driver.api.core.uuid.Uuids
import krystian.kryszczak.commons.model.being.user.User
import java.time.LocalDate
import java.time.temporal.ChronoUnit

private val testUserId = Uuids.startOf(System.currentTimeMillis() + 100)
private val secondTestUserId = Uuids.startOf(System.currentTimeMillis() + 250)
private val thirdTestUserId = Uuids.startOf(System.currentTimeMillis() + 500)

val testUser = User(
    testUserId,
    "John",
    "Smith",
    "john.smith@example.com",
    "555 555 555",
    LocalDate.EPOCH.until(LocalDate.of(2003, 7, 25), ChronoUnit.DAYS).toInt(),
    mutableSetOf(secondTestUserId),
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
    mutableSetOf(testUserId, thirdTestUserId),
    0,
    null
)

val thirdTestUser = User(
    thirdTestUserId,
    "Jim",
    "Smith",
    "jim.smith@example.com",
    "785 785 785",
    LocalDate.EPOCH.until(LocalDate.of(2003, 7, 25), ChronoUnit.DAYS).toInt(),
    mutableSetOf(secondTestUserId),
    0,
    null
)

val fourthTestUser = User(
    thirdTestUserId,
    "Jason",
    "Smith",
    "jason.smith@example.com",
    "885 885 885",
    LocalDate.EPOCH.until(LocalDate.of(2003, 7, 25), ChronoUnit.DAYS).toInt(),
    mutableSetOf(),
    0,
    null
)
