package com.kshrd.features.remote.mapper

/**
 * The plans and their published limits, used only when Payment's usage has no limit for a meter (decision D1:
 * Payment's `metrics[].limit` wins). There is deliberately no photo number here: the photos meter shows only with
 * Payment's `photos` metric, and its limit comes from that metric.
 */
@Suppress("MagicNumber") // The published plan table, the same as the iOS app's PlanTier.
enum class PlanTier(
    val code: String,
    val title: String,
    val websites: Int,
    val redeploys: Int,
    val products: Int,
) {
    FreeTrial("FREE_TRIAL", "Free trial", websites = 1, redeploys = 0, products = 30),
    Plus("PLUS", "Plus", websites = 1, redeploys = 5, products = 30),
    Pro("PRO", "Pro", websites = 3, redeploys = 30, products = 200),
    Ultra("ULTRA", "Ultra", websites = 10, redeploys = 100, products = 1000),
    ;

    /** Project's activity log answers 403 on the other plans. */
    val showsActivityLog: Boolean get() = this == Pro || this == Ultra

    companion object {
        /** Payment's code; unknown or `null` (Payment down) counts as the free trial until the plan reloads. */
        fun fromCode(code: String?): PlanTier = entries.firstOrNull { it.code == code } ?: FreeTrial
    }
}
