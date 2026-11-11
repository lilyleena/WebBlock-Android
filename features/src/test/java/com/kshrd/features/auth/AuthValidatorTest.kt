package com.kshrd.features.auth

import com.kshrd.features.auth.domain.validation.AuthValidator
import com.kshrd.features.auth.domain.validation.FieldError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AuthValidatorTest {
    @Test
    fun email_rejectsBlankAndMalformed_acceptsValid() {
        assertEquals(FieldError.Empty, AuthValidator.email("  "))
        assertEquals(FieldError.InvalidEmail, AuthValidator.email("owner@"))
        assertNull(AuthValidator.email("owner@example.com"))
    }

    @Test
    fun newPassword_requiresEightCharacters() {
        assertEquals(FieldError.Empty, AuthValidator.newPassword(""))
        assertEquals(FieldError.TooShort, AuthValidator.newPassword("short"))
        assertNull(AuthValidator.newPassword("longenough"))
    }

    @Test
    fun confirmPassword_mustMatch() {
        assertEquals(FieldError.Mismatch, AuthValidator.confirmPassword("password1", "password2"))
        assertNull(AuthValidator.confirmPassword("password1", "password1"))
    }

    @Test
    fun code_needsSixDigits() {
        assertEquals(FieldError.CodeIncomplete, AuthValidator.code("123"))
        assertNull(AuthValidator.code("123456"))
    }

    @Test
    fun isEmailValid_followsIosRule() {
        assert(AuthValidator.isEmailValid("a@b.co"))
        assert(!AuthValidator.isEmailValid("a@b"))
        assert(!AuthValidator.isEmailValid("@b.co"))
        assert(!AuthValidator.isEmailValid("a b@c.co"))
        assert(!AuthValidator.isEmailValid("a@@b.co"))
    }

    @Test
    fun passwordChecks_scoreStrength() {
        assertEquals(0, AuthValidator.passwordChecks("").strength)
        val weak = AuthValidator.passwordChecks("abc")
        assertEquals(0, weak.strength)
        val good = AuthValidator.passwordChecks("Abcdef12")
        assert(good.meetsRequirements)
        assertEquals(2, good.strength)
        val strong = AuthValidator.passwordChecks("Abcdef12345!")
        assertEquals(4, strong.strength)
    }
}
