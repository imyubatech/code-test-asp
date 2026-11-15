package co.id.bankbsi.superapp.testcodeasp

import co.id.bankbsi.superapp.testcodeasp.presentation.login.LoginValidator
import org.junit.Assert.*
import org.junit.Test

class LoginValidatorTest {
    @Test fun emptyCredentialsRequireBothFields() {
        val result = LoginValidator.validate("", "")
        assertFalse(result.isValid)
        assertTrue(result.usernameMissing)
        assertTrue(result.passwordMissing)
    }

    @Test fun whitespaceIsNotAValidCredential() {
        assertFalse(LoginValidator.validate(" \t", "secret").isValid)
        assertFalse(LoginValidator.validate("reader", " \n").isValid)
    }

    @Test fun missingPasswordDoesNotFlagUsername() {
        val result = LoginValidator.validate("reader", "")
        assertFalse(result.usernameMissing)
        assertTrue(result.passwordMissing)
    }

    @Test fun nonEmptyCredentialsAreAccepted() {
        assertTrue(LoginValidator.validate("reader", "secret").isValid)
    }
}
