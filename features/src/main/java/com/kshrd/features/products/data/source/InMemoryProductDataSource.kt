package com.kshrd.features.products.data.source

import com.kshrd.features.mock.MockIds
import com.kshrd.features.products.domain.model.Product
import com.kshrd.features.products.domain.model.ProductCatalog
import com.kshrd.features.products.domain.model.ProductImages
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Per-website mock catalogs held in memory, so edits survive switching websites during a session.
 * Replace with a service + Retrofit API when the backend exists.
 */
@Singleton
class InMemoryProductDataSource
    @Inject
    constructor() {
        private val catalogs = mutableMapOf<String, MutableStateFlow<ProductCatalog>>()

        fun catalog(websiteId: String): StateFlow<ProductCatalog> = flowFor(websiteId).asStateFlow()

        suspend fun save(
            websiteId: String,
            product: Product,
        ): Product {
            delay(LATENCY_MS)
            flowFor(websiteId).update { catalog ->
                val exists = catalog.products.any { it.id == product.id }
                val products = if (exists) catalog.products.map { if (it.id == product.id) product else it } else listOf(product) + catalog.products
                val categories = if (product.category in catalog.categories) catalog.categories else catalog.categories + product.category
                ProductCatalog(products, categories)
            }
            return product
        }

        suspend fun delete(
            websiteId: String,
            productId: String,
        ) {
            delay(LATENCY_MS)
            flowFor(websiteId).update { it.copy(products = it.products.filterNot { p -> p.id == productId }) }
        }

        suspend fun addCategory(
            websiteId: String,
            name: String,
        ): Result<String> {
            delay(LATENCY_MS)
            val flow = flowFor(websiteId)
            if (flow.value.categories.any { it.equals(name, ignoreCase = true) }) {
                return Result.failure(IllegalArgumentException("That category already exists."))
            }
            flow.update { it.copy(categories = it.categories + name) }
            return Result.success(name)
        }

        private fun flowFor(websiteId: String): MutableStateFlow<ProductCatalog> =
            catalogs.getOrPut(websiteId) { MutableStateFlow(seed(websiteId)) }

        private fun seed(websiteId: String): ProductCatalog {
            val products =
                when (websiteId) {
                    MockIds.PETAL_STEM -> petalAndStem()
                    MockIds.GOLDEN_CRUMB -> goldenCrumb()
                    MockIds.BEAN_BREW -> beanAndBrew()
                    MockIds.STUDIO_LOTUS -> studioLotus()
                    else -> emptyList()
                }
            return ProductCatalog(products, categoriesOf(products))
        }

        /** The categories a website uses, in the order its products introduce them; florist defaults when empty. */
        private fun categoriesOf(products: List<Product>): List<String> =
            products.map { it.category }.distinct().ifEmpty { DEFAULT_CATEGORIES }

        private fun product(
            name: String,
            category: String,
            description: String,
            price: Int,
            image: String,
        ) = Product(UUID.randomUUID().toString(), name, category, price.toDouble(), description, ProductImages.drawable(image))

        // Sample data copied from the iOS MockWebBlockService. The last six Petal & Stem items have no bundled
        // photo on iOS either, so they show the leaf placeholder.
        private fun petalAndStem() =
            listOf(
                product("Blue lily stem", "Flowers", "A single long-stemmed blue lily, cut fresh each morning. Looks lovely on its own in a slim vase.", 6, "blue_lily_stem"),
                product("Gift bouquet", "Bouquets", "A hand-tied bouquet of seasonal flowers, wrapped in kraft paper with a ribbon and a small card.", 38, "gift_bouquet"),
                product("Lily bunch", "Flowers", "Three lily stems tied by hand. The buds open over a few days and fill the room with a soft scent.", 16, "lily_bunch"),
                product("Stone vase", "Gifts", "A handmade ceramic vase with a matte stone finish. Holds a small bouquet or stands well on its own.", 24, "stone_vase"),
                product("Table arrangement", "Events", "A low arrangement of seasonal flowers and greenery for dining tables and events. Order two days ahead.", 45, "table_arrangement"),
                product("Spring tulip bunch", "Flowers", "This week’s special: ten fresh tulips in soft spring colours, tied with natural twine.", 14, "spring_tulip_bunch"),
                product("Peony bouquet", "Bouquets", "Soft pink peonies in season, tied with eucalyptus. The full blooms open slowly over several days.", 32, "peony_bouquet"),
                product("Petal candle", "Gifts", "A hand-poured soy wax candle topped with dried petals. Burns for about 40 hours.", 26, "petal_candle"),
                product("Dried grass bunch", "Flowers", "Natural dried grasses that keep their shape and colour for months. No water needed.", 18, "dried_grass_bunch"),
                product("Bud vase", "Gifts", "A small hand-glazed bud vase. Pairs well with a single stem or a few dried flowers.", 18, "bud_vase"),
                product("Ceramic bud vase", "Gifts", "A small hand-thrown vase with a matte glaze, sized for one or two stems.", 22, "ceramic_bud_vase"),
                product("Pampas stem", "Flowers", "One tall, fluffy pampas stem, dried to last for years. Adds a calm, natural touch to any room.", 12, "pampas_stem"),
            )

        private fun goldenCrumb() =
            listOf(
                product("Butter croissant", "Pastries", "Flaky, golden and baked fresh every morning with French butter.", 3, "sample_croissant"),
                product("Sourdough loaf", "Bread", "A 24-hour fermented loaf with a crisp crust and a soft, tangy crumb.", 7, "sample_sourdough"),
                product("Pain au chocolat", "Pastries", "Laminated dough wrapped around two bars of dark chocolate.", 4, "sample_pain_au_chocolat"),
                product("Cinnamon roll", "Pastries", "Soft rolls swirled with cinnamon sugar and topped with cream cheese icing.", 5, "sample_cinnamon_roll"),
            )

        private fun beanAndBrew() =
            listOf(
                product("Flat white", "Coffee", "A double shot of house espresso with silky steamed milk.", 4, "sample_flat_white"),
                product("Iced latte", "Coffee", "Espresso over ice with cold milk. Add vanilla or caramel on request.", 5, "sample_iced_latte"),
                product("Matcha latte", "Tea", "Ceremonial-grade matcha whisked with steamed milk.", 5, "sample_matcha_latte"),
                product("Banana bread", "Bakes", "Baked daily. Moist, lightly spiced and served warm.", 3, "sample_banana_bread"),
            )

        private fun studioLotus() =
            listOf(
                product("Drop-in class", "Classes", "One 60-minute class. Mats and props are provided.", 8, "sample_yoga_dropin"),
                product("10-class pass", "Passes", "Ten classes, valid for three months. Share with a friend.", 70, "sample_yoga_pass"),
                product("Monthly unlimited", "Memberships", "Unlimited classes for 30 days, including weekend workshops.", 90, "sample_yoga_monthly"),
                product("Private session", "Classes", "A one-to-one session tailored to your goals and experience.", 35, "sample_yoga_private"),
            )

        private companion object {
            const val LATENCY_MS = 450L
            val DEFAULT_CATEGORIES = listOf("Flowers", "Bouquets", "Gifts", "Events")
        }
    }
