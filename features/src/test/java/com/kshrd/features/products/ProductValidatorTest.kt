package com.kshrd.features.products

import com.kshrd.features.products.domain.model.ProductDraft
import com.kshrd.features.products.domain.model.ProductFormError
import com.kshrd.features.products.domain.usecase.ProductValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductValidatorTest {
    @Test
    fun `prices follow Project's rule`() {
        assertEquals(4.5, ProductValidator.parsePrice("4.50")!!, 0.0)
        assertEquals(4.5, ProductValidator.parsePrice("$4.5")!!, 0.0)
        assertEquals(1234.0, ProductValidator.parsePrice("1,234")!!, 0.0)
        assertEquals(999999.99, ProductValidator.parsePrice("999999.99")!!, 0.0)
    }

    @Test
    fun `more than two decimals is refused, not rounded`() {
        assertNull(ProductValidator.parsePrice("4.505"))
        assertNull(ProductValidator.parsePrice("1234567"))
        assertNull(ProductValidator.parsePrice("-1"))
        assertNull(ProductValidator.parsePrice("4."))
        assertNull(ProductValidator.parsePrice("abc"))
    }

    @Test
    fun `a description is empty or 10 to 300 characters once trimmed`() {
        assertTrue(ProductValidator.isDescriptionValid(""))
        assertTrue(ProductValidator.isDescriptionValid("   "))
        assertTrue(ProductValidator.isDescriptionValid("  Ten chars!  "))
        assertTrue(ProductValidator.isDescriptionValid("x".repeat(300)))
        assertFalse(ProductValidator.isDescriptionValid("Too short"))
        assertFalse(ProductValidator.isDescriptionValid("x".repeat(301)))
    }

    @Test
    fun `a short description is a form error`() {
        val draft = ProductDraft(null, "Tulips", "Flowers", "6.00", "Nice", "")

        assertEquals(setOf(ProductFormError.DescriptionLength), ProductValidator.validate(draft))
    }

    @Test
    fun `an empty price keeps a hidden price hidden, and is an error otherwise`() {
        val hidden = ProductDraft(null, "Tulips", "Flowers", "", "", "", version = 3, wasPriceHidden = true)

        assertTrue(ProductValidator.validate(hidden).isEmpty())
        assertEquals(setOf(ProductFormError.PriceInvalid), ProductValidator.validate(hidden.copy(wasPriceHidden = false)))
    }
}
