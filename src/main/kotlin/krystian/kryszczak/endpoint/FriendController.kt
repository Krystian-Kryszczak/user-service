package krystian.kryszczak.endpoint

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.*
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.CustomHttpMethod
import io.micronaut.http.annotation.Delete
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.security.authentication.Authentication
import krystian.kryszczak.commons.utils.SecurityUtils
import krystian.kryszczak.model.invitation.FriendInvitationModel
import krystian.kryszczak.service.friend.FriendService
import java.util.UUID

@Controller("/friends/")
class FriendController(private val friendService: FriendService) {
    @Get("/propose")
    fun propose(authentication: Authentication) = friendService.propose(authentication)

    @Get("/invitations")
    fun invitations(authentication: Authentication) = useWithExtractedId(authentication, friendService::invitations)

    @Get
    fun friendshipList(page: Int, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.friendshipList(page, clientId)
    }

    @Post
    fun invite(id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.sendInvitation(FriendInvitationModel(clientId, id))
    }

    @CustomHttpMethod(method = "ACCEPT")
    fun accept(id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.acceptInvitation(FriendInvitationModel(id, clientId))
    }

    @CustomHttpMethod(method = "DENY")
    fun deny(id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.denyInvitation(FriendInvitationModel(id, clientId))
    }

    @Delete
    fun remove(id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.removeFriend(id, clientId)
    }

    private inline fun <T> useWithExtractedId(authentication: Authentication, body: (id: UUID) -> T): HttpResponse<T> {
        return HttpResponse.ok(
            body(
                SecurityUtils.getClientId(authentication)
                    ?: return HttpResponse.status(UNAUTHORIZED, "JWT not contain client id.")
            )
        )
    }
}
