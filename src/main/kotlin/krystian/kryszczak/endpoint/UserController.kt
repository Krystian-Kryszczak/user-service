package krystian.kryszczak.endpoint

import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.reactivex.rxjava3.core.Maybe
import krystian.kryszczak.commons.model.being.user.User
import krystian.kryszczak.commons.service.being.user.UserService
import java.util.UUID

@Secured(SecurityRule.IS_ANONYMOUS)
@Controller("/users")
class UserController(private val userService: UserService) {
    @Get("/{id}")
    fun get(id: UUID): Maybe<User> = userService.findById(id)
}
