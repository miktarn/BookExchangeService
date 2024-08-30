package pet.project.app.validation

import jakarta.validation.Constraint
import jakarta.validation.Payload
import pet.project.app.validation.validator.OpenIdValidator
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [OpenIdValidator::class])
annotation class ValidOpenId(
    val message: String = "Invalid OpenID format",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)