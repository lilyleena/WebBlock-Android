package com.kshrd.features.portal

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.session.SessionStore
import com.kshrd.core.session.WorkspaceWebsite
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.business.domain.usecase.OpeningHoursRules
import com.kshrd.features.monitor.domain.model.Deploy
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.domain.model.Plan
import com.kshrd.features.monitor.domain.model.SiteHealth
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog
import com.kshrd.features.products.domain.model.ProductDraft
import com.kshrd.features.products.domain.model.ProductFormError
import com.kshrd.features.products.domain.usecase.ProductValidator
import com.kshrd.features.products.presentation.model.ProductsUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.DayOfWeek

class PortalLogicTest {
    private val sites =
        listOf(
            WorkspaceWebsite("a", "Coffee House", "coffee.webblock.com"),
            WorkspaceWebsite("b", "Fashion Store", "fashion.webblock.com"),
        )

    @Test
    fun session_selectingAndSwitchingWebsite_updatesCurrent() {
        val session = SessionStore()
        session.setWebsites(sites)
        assertNull(session.workspace.value.current)
        session.selectWebsite("b")
        assertEquals("Fashion Store", session.workspace.value.current?.name)
        session.selectWebsite("a")
        assertEquals("CH", session.workspace.value.current?.initials)
    }

    @Test
    fun products_filterBySearchAndCategory() {
        val products =
            listOf(
                Product("1", "Flat white", "Coffee", 3.5, "", ""),
                Product("2", "Jasmine tea", "Tea", 3.2, "", ""),
                Product("3", "Cold brew", "Coffee", 4.0, "", ""),
            )
        val state = ProductsUiState(catalog = DataState.Success(ProductCatalog(products, listOf("Coffee", "Tea"))))
        assertEquals(3, state.visibleProducts.size)
        assertEquals(2, state.copy(selectedCategory = "Coffee").visibleProducts.size)
        assertEquals(listOf("Cold brew"), state.copy(query = "brew").visibleProducts.map { it.name })
    }

    @Test
    fun productValidator_flagsEmptyFieldsAndBadPrice() {
        val bad = ProductDraft(null, "", "", "abc", "", "")
        assertEquals(
            setOf(ProductFormError.NameEmpty, ProductFormError.CategoryEmpty, ProductFormError.PriceInvalid),
            ProductValidator.validate(bad),
        )
        assertEquals(38.0, ProductValidator.parsePrice("$38.00")!!, 0.0)
    }

    @Test
    fun openingHours_copyMondayToWeekdays_leavesWeekendAlone() {
        val hours =
            DayOfWeek.entries.map { day ->
                if (day == DayOfWeek.MONDAY) DayHours(day, true, 8 * 60, 17 * 60) else DayHours(day, false, 9 * 60, 18 * 60)
            }
        val copied = OpeningHoursRules.copyMondayToWeekdays(hours)
        assertEquals(8 * 60, copied.first { it.day == DayOfWeek.FRIDAY }.openMinutes)
        assertEquals(false, copied.first { it.day == DayOfWeek.SUNDAY }.isOpen)
    }

    @Test
    fun monitor_deploySuccessPercentages() {
        val deploys = (1..10).map { Deploy(it, 0L, 40, succeeded = it != 3 && it != 7) }
        val data = MonitorData(SiteHealth.Online, Plan("Pro", true), 99.9, deploys, emptyList(), emptyList())
        assertEquals(80, data.successPercent)
        assertEquals(20, data.failedPercent)
        assertEquals(7, data.recentTimeline.size)
    }
}
