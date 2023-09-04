package krystian.kryszczak.endpoint

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.UNAUTHORIZED
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import krystian.kryszczak.commons.utils.SecurityUtils
import krystian.kryszczak.model.invitation.FriendInvitationModel
import krystian.kryszczak.service.friend.FriendService
import java.util.UUID

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/friends")
class FriendController(private val friendService: FriendService) {
    @Get("/{?page:3}")
    fun friendshipList(page: Int?, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.friendshipList(page ?: 0, clientId)
    }

    @Post
    fun invite(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.sendInvitation(FriendInvitationModel(clientId, id))
    }

    @Delete
    fun remove(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.removeFriend(id, clientId)
    }

    @Get("/invitations")
    fun invitations(authentication: Authentication) = useWithExtractedId(authentication, friendService::invitations)

    @Post("/invitations")
    fun accept(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.acceptInvitation(FriendInvitationModel(id, clientId))
    }

    @Delete("/invitations")
    fun deny(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.denyInvitation(FriendInvitationModel(id, clientId))
    }

    @Get("/propose")
    fun propose(authentication: Authentication) = friendService.propose(authentication)

    private inline fun <T> useWithExtractedId(authentication: Authentication, body: (id: UUID) -> T): HttpResponse<T> {
        return HttpResponse.ok(
            body(
                SecurityUtils.getClientId(authentication)
                    ?: return HttpResponse.status(UNAUTHORIZED, "JWT not contain client id.")
            )
        )
    }
}
