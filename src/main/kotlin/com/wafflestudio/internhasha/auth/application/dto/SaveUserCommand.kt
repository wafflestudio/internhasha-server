package com.wafflestudio.internhasha.auth.application.dto

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
