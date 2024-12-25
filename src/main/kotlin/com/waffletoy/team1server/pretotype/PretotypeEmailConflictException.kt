package com.waffletoy.team1server.pretotype

import com.waffletoy.team1server.DomainException
import org.springframework.http.HttpStatus

class PretotypeEmailConflictException : DomainException(0, HttpStatus.CONFLICT, "Email already exists")
