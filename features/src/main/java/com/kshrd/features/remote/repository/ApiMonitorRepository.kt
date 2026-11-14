package com.kshrd.features.remote.repository

import com.kshrd.core.data.entity.DataState
import com.kshrd.core.session.SessionStore
import com.kshrd.coreui.format.formatRelativeTime
import com.kshrd.features.monitor.domain.model.MonitorData
import com.kshrd.features.monitor.domain.repository.MonitorRepository
import com.kshrd.features.remote.api.PaymentApi
import com.kshrd.features.remote.api.ProjectApi
import com.kshrd.features.remote.api.ProvisioningApi
import com.kshrd.features.remote.mapper.MonitorFormat
import com.kshrd.features.remote.mapper.MonitorParts
import com.kshrd.features.remote.mapper.PlanTier
import com.kshrd.features.remote.mapper.toMonitorData
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * The Monitor screen from Provisioning, Payment and Project (docs/API-MAPPING.md 2.6), all asked in parallel. A part
 * that fails shows "—" or nothing; only a failed Project read of the website itself is an error (it says whether the
 * website is the owner's at all).
 */
class ApiMonitorRepository
    @Inject
    constructor(
        private val projectApi: ProjectApi,
        private val provisioningApi: ProvisioningApi,
        private val paymentApi: PaymentApi,
        private val session: SessionStore,
        private val format: MonitorFormat,
    ) : MonitorRepository {
        override fun observeMonitor(websiteId: String): Flow<DataState<MonitorData>> =
            flow {
                emit(loadState { load(websiteId) })
            }

        private suspend fun load(websiteId: String): MonitorData =
            coroutineScope {
                val plan = PlanTier.fromCode(session.owner.value?.plan)
                val site = async { projectApi.website(websiteId) }
                val status = async { optional { provisioningApi.status(websiteId) } }
                val stats = async { optional { provisioningApi.releaseStats(websiteId) } }
                val releases = async { optional { provisioningApi.releases(websiteId).items } }
                val usage = async { optional { paymentApi.usage(websiteId) } }
                // Pro and Ultra only; Project answers 403 on the other plans, which is an empty log, not an error.
                val activity = async { if (plan.showsActivityLog) optional { projectApi.activity(websiteId).items } else null }
                site.await()
                MonitorParts(
                    plan = plan,
                    status = status.await(),
                    stats = stats.await(),
                    releases = releases.await().orEmpty().filter { it.status != null },
                    usage = usage.await(),
                    activity = activity.await().orEmpty(),
                ).toMonitorData(System.currentTimeMillis(), format)
            }
    }

/** The app's dates in the device's language, as the sample data writes them. */
object DeviceMonitorFormat : MonitorFormat {
    override fun relative(epochMillis: Long): String = formatRelativeTime(epochMillis)

    override fun deployDetail(
        number: Int,
        epochMillis: Long,
    ): String = "Deploy $number · ${SimpleDateFormat("d MMM, h:mm a", Locale.getDefault()).format(Date(epochMillis))}"

    override fun dayMonth(epochMillis: Long): String = SimpleDateFormat("d MMM", Locale.getDefault()).format(Date(epochMillis))
}
