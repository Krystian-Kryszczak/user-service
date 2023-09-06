package krystian.kryszczak.endpoint

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus.*
import io.micronaut.http.annotation.*
import io.micronaut.security.annotation.Secured
import io.micronaut.security.authentication.Authentication
import io.micronaut.security.rules.SecurityRule
import io.reactivex.rxjava3.core.Single
import krystian.kryszczak.commons.utils.SecurityUtils
import krystian.kryszczak.model.invitation.FriendInvitationModel
import krystian.kryszczak.service.friend.FriendService
import java.util.UUID

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/friends")
class FriendController(private val friendService: FriendService) {
    @Get("/{/page:3}")
    fun friendshipList(@PathVariable(defaultValue = "0") page: Int, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.friendshipList(clientId, page)
    }

    @Post
    fun invite(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.sendInvitation(FriendInvitationModel(clientId, id)).mapToStatus()
    }

    @Delete
    fun remove(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.removeFriend(id, clientId).mapToStatus()
    }

    @Get("/invitations")
    fun invitations(authentication: Authentication) = useWithExtractedId(authentication, friendService::invitations)

    @Post("/invitations")
    fun accept(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.acceptInvitation(FriendInvitationModel(id, clientId)).mapToStatus()
    }

    @Delete("/invitations")
    fun deny(@Body id: UUID, authentication: Authentication) = useWithExtractedId(authentication) {
        clientId -> friendService.denyInvitation(FriendInvitationModel(id, clientId)).mapToStatus()
    }

    @Get("/propose")
    fun propose(authentication: Authentication) = friendService.propose(authentication)

    @Get("/search/{query}")
    fun search(query: String, authentication: Authentication) = friendService.search(query, authentication)

    private inline fun <T> useWithExtractedId(authentication: Authentication, body: (id: UUID) -> T): HttpResponse<T> {
        return HttpResponse.ok(
            body(
                SecurityUtils.getClientId(authentication)
                    ?: return HttpResponse.status(UNAUTHORIZED, "JWT not contain client id.")
            )
        )
    }

    private fun Single<Boolean>.mapToStatus() = map { if (it) OK else CONFLICT }
}
