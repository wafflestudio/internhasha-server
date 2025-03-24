package com.waffletoy.team1server.applicant.dto

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [JobCategoryValidator::class])
annotation class ValidJobCategory(
    val message: String = "Invalid job category",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class JobCategoryValidator : ConstraintValidator<ValidJobCategory, JobCategory> {
    override fun isValid(
        value: JobCategory?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        return value != null && value in JobCategory.values()
    }
}
