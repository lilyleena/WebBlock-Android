package com.kshrd.features.remote.mapper

/**
 * The plans by Payment's code: the title and whether Project keeps an activity log. There are deliberately no limits
 * here: every limit comes from Payment's `metrics[].limit` (decision D1); sample numbers live only in the mock data.
 */
enum class PlanTier(
    val code: String,
    val title: String,
) {
    FreeTrial("FREE_TRIAL", "Free trial"),
    Plus("PLUS", "Plus"),
    Pro("PRO", "Pro"),
    Ultra("ULTRA", "Ultra"),
    ;

    /** Project's activity log answers 403 on the other plans. */
    val showsActivityLog: Boolean get() = this == Pro || this == Ultra

    companion object {
        /** Payment's code; unknown or `null` (Payment down) counts as the free trial until the plan reloads. */
        fun fromCode(code: String?): PlanTier = entries.firstOrNull { it.code == code } ?: FreeTrial
    }
}
