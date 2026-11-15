package co.id.bankbsi.superapp.testcodeasp.presentation.login

object LoginValidator {
    data class Result(val usernameMissing: Boolean, val passwordMissing: Boolean) {
        val isValid get() = !usernameMissing && !passwordMissing
    }

    fun validate(username: String, password: String): Result = Result(
        usernameMissing = username.isBlank(),
        passwordMissing = password.isBlank()
    )
}
