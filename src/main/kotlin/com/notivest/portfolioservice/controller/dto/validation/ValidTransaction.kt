package com.notivest.portfolioservice.controller.dto.validation

import com.notivest.portfolioservice.controller.dto.PostTransactionRequest
import com.notivest.portfolioservice.models.enums.TransactionType
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TransactionValidator::class])
annotation class ValidTransaction(
    val message: String = "Invalid transaction",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

class TransactionValidator : ConstraintValidator<ValidTransaction, PostTransactionRequest> {
    override fun isValid(
        request: PostTransactionRequest?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (request == null) return true

        context?.disableDefaultConstraintViolation()
        var valid = true

        // Para BUY/SELL necesitamos symbolId y qty
        if (request.type == TransactionType.BUY || request.type == TransactionType.SELL) {
            if (request.symbolId.isNullOrBlank()) {
                context?.buildConstraintViolationWithTemplate("Symbol ID is required for BUY/SELL transactions")
                    ?.addPropertyNode("symbolId")
                    ?.addConstraintViolation()
                valid = false
            }

            if (request.qty == null || request.qty <= java.math.BigDecimal.ZERO) {
                context?.buildConstraintViolationWithTemplate("Quantity must be greater than 0 for BUY/SELL transactions")
                    ?.addPropertyNode("qty")
                    ?.addConstraintViolation()
                valid = false
            }
        }

        return valid
    }
}
