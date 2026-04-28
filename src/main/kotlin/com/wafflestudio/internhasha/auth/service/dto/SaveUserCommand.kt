package com.wafflestudio.internhasha.auth.service.dto

sealed interface SaveUserCommand

data class SaveApplicantCommand(
    val name: String,
    val email: String,
    val passwordHash: String,
) : SaveUserCommand

data class SaveCompanyCommand(
    val name: String,
    val email: String,
    val passwordHash: String,
    val vcName: String?,
    val vcRec: String?,
) : SaveUserCommand
