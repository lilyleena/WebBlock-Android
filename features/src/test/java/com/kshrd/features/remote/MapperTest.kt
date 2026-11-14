package com.kshrd.features.remote

import com.kshrd.features.business.domain.model.BusinessDraft
import com.kshrd.features.business.domain.model.DayHours
import com.kshrd.features.notifications.domain.model.PortalTarget
import com.kshrd.features.remote.dto.BusinessInfoDto
import com.kshrd.features.remote.dto.NotificationDto
import com.kshrd.features.remote.dto.NotificationTargetDto
import com.kshrd.features.remote.dto.OpeningHourDto
import com.kshrd.features.remote.dto.ProductDto
import com.kshrd.features.remote.dto.WebsiteDto
import com.kshrd.features.remote.mapper.PhotoChange
import com.kshrd.features.remote.mapper.businessInfoBody
import com.kshrd.features.remote.mapper.parseApiTime
import com.kshrd.features.remote.mapper.priceText
import com.kshrd.features.remote.mapper.productBody
import com.kshrd.features.remote.mapper.toAppNotification
import com.kshrd.features.remote.mapper.toBusinessInfo
import com.kshrd.features.remote.mapper.toProduct
import com.kshrd.features.remote.mapper.toWebsite
import com.kshrd.features.website.domain.model.WebsiteStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class MapperTest {
    private val website =
        WebsiteDto(
            id = "w1",
            name = "Petal & Stem",
            businessType = null,
            status = "LIVE",
            domain = "petal.webblock.site",
            logoUrl = "https://cdn/logo.png",
        )

    @Test
    fun `a website keeps its logo and the hosting state`() {
        val mapped = website.toWebsite(hostingOffline = true)

        assertEquals(WebsiteStatus.Live, mapped.status)
        assertEquals("—", mapped.businessType)
        assertEquals("https://cdn/logo.png", mapped.logoUrl)
        assertTrue(mapped.isHostingOffline)
        assertTrue(mapped.hasPortal)
    }

    @Test
    fun `an unknown website status has no portal`() {
        assertEquals(WebsiteStatus.Draft, website.copy(status = "ARCHIVED").toWebsite().status)
    }

    @Test
    fun `a null price is a hidden price`() {
        val hidden = ProductDto(id = "p1", name = "Tulips", price = null, imageUrl = null, version = 2).toProduct()
        val shown = ProductDto(id = "p2", name = "Lily", price = "6.50").toProduct()

        assertTrue(hidden.isPriceHidden)
        assertEquals("", hidden.imageUrl)
        assertEquals(2L, hidden.version)
        assertFalse(shown.isPriceHidden)
        assertEquals(6.5, shown.price, 0.0)
    }

    @Test
    fun `prices are sent with a dot and two decimals`() {
        assertEquals("6.00", priceText(6.0))
        assertEquals("1234.50", priceText(1234.5))
        assertEquals("0.10", priceText(0.1))
    }

    @Test
    fun `the product body says what happens to the photo`() {
        val keep = productBody("Tulips", "Spring", "Flowers", null, PhotoChange.Keep, version = 3)
        val remove = productBody("Tulips", "Spring", "Flowers", 6.0, PhotoChange.Remove, version = null)
        val set = productBody("Tulips", "Spring", "Flowers", 6.0, PhotoChange.Set("https://cdn/p.jpg"), version = null)

        assertEquals("""{"name":"Tulips","description":"Spring","category":"Flowers","price":null,"version":3}""", keep.toString())
        assertEquals("""{"name":"Tulips","description":"Spring","category":"Flowers","price":"6.00","imageUrl":null}""", remove.toString())
        assertEquals("https://cdn/p.jpg", set["imageUrl"].toString().trim('"'))
    }

    @Test
    fun `a blank description is left out of the product body`() {
        val body = productBody("Tulips", "  ", "Flowers", 6.0, PhotoChange.Keep, version = null)

        assertEquals(false, "description" in body)
    }

    @Test
    fun `business info fills every weekday`() {
        val dto =
            BusinessInfoDto(
                description = "Flowers",
                phone = "012",
                email = "hi@petal.com",
                address = "Street 1",
                mapsUrl = null,
                hours = listOf(OpeningHourDto("MONDAY", true, "08:30", "17:00")),
            )

        val info = dto.toBusinessInfo(website)

        assertEquals("Petal & Stem", info.profile.name)
        assertEquals("petal.webblock.site", info.profile.url)
        assertEquals("", info.profile.mapsLink)
        assertEquals(7, info.hours.size)
        assertEquals(DayHours(DayOfWeek.MONDAY, true, 510, 1020), info.hours.first())
        assertEquals(DayHours(DayOfWeek.SUNDAY, false, 540, 1080), info.hours.last())
    }

    @Test
    fun `the business body round-trips the hours and drops an empty maps link`() {
        val hours = listOf(DayHours(DayOfWeek.TUESDAY, true, 570, 1290))
        val body = businessInfoBody(BusinessDraft("012", "hi@petal.com", "Street 1", "  ", "Flowers"), hours)

        assertNull(body.mapsUrl)
        assertEquals(7, body.hours.size)
        assertEquals(OpeningHourDto("TUESDAY", true, "09:30", "21:30"), body.hours[1])
        assertEquals(OpeningHourDto("MONDAY", false, "09:00", "18:00"), body.hours[0])
    }

    @Test
    fun `a notification opens the tab its target names`() {
        val dto =
            NotificationDto(
                id = "k1",
                title = "Petal & Stem",
                message = "A product changed",
                websiteId = "w1",
                createdAt = "2026-11-14T03:00:00Z",
                read = true,
                detail = "Tulips now cost 6.00",
                target = NotificationTargetDto(websiteId = "w1", tab = "PRODUCTS", workflow = "product-updated"),
            )

        val mapped = dto.toAppNotification("2 minutes ago", logoUrl = null)

        assertEquals(PortalTarget.Products, mapped.target)
        assertEquals("Open Products", mapped.actionTitle)
        assertEquals("Tulips now cost 6.00", mapped.detail)
        assertFalse(mapped.unread)
    }

    @Test
    fun `without target a website's notification opens Monitor, a platform one nothing`() {
        val site =
            NotificationDto(id = "k2", title = "Petal & Stem", message = "Live", websiteId = "w1", createdAt = "2026-11-14T03:00:00Z")
        val platform = site.copy(id = "k3", title = "WebBlock Billing", websiteId = null)
        val noTab = site.copy(target = NotificationTargetDto(websiteId = "w1", tab = null))

        assertEquals(PortalTarget.Monitor, site.toAppNotification("now", null).target)
        assertNull(platform.toAppNotification("now", null).target)
        assertNull(platform.toAppNotification("now", null).actionTitle)
        assertNull(noTab.toAppNotification("now", null).target)
    }

    @Test
    fun `API times with fractions and offsets are read`() {
        assertEquals(1_763_089_200_000L, parseApiTime("2025-11-14T03:00:00Z"))
        assertEquals(1_763_089_200_500L, parseApiTime("2025-11-14T10:00:00.5+07:00"))
        assertNull(parseApiTime("yesterday"))
        assertNull(parseApiTime(null))
    }
}
