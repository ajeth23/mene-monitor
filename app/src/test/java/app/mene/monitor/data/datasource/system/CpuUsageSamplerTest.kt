package app.mene.monitor.data.datasource.system

import org.junit.Assert.*
import org.junit.Test

class CpuUsageSamplerTest {
    @Test fun `uses interval counters including steal and excluding duplicate guest`() {
        val sampler = CpuUsageSampler()
        assertNull(sampler.sample("cpu 100 0 0 100 0 0 0 0 50 0"))
        assertEquals(50, sampler.sample("cpu 120 0 0 150 0 0 0 30 60 0"))
    }

    @Test fun `lost access resets baseline and never reports false zero`() {
        val sampler = CpuUsageSampler()
        sampler.sample("cpu 100 0 0 100")
        assertNull(sampler.sample(null))
        assertNull(sampler.sample("cpu 200 0 0 200"))
        assertEquals(0, sampler.sample("cpu 200 0 0 300"))
        assertNull(sampler.sample("cpu 1 0 0 1"))
    }
}
