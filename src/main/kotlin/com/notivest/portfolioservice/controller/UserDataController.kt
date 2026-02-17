package com.notivest.portfolioservice.controller

import com.notivest.portfolioservice.security.JwtUserIdResolver
import com.notivest.portfolioservice.service.interfaces.UserDataDeletionService
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping
class UserDataController(
    private val userDataDeletionService: UserDataDeletionService,
    private val userIdResolver: JwtUserIdResolver,
) {
    @DeleteMapping("/v1/user-data/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMyData(
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        val userId = userIdResolver.requireUserId(jwt)
        userDataDeletionService.deleteUserData(userId)
    }

    @DeleteMapping("/internal/v1/user-data")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteUserDataInternal(
        @RequestParam userId: UUID,
    ) {
        userDataDeletionService.deleteUserData(userId)
    }
}

