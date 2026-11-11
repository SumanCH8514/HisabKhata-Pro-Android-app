package com.sumanonline.hisabkhata

import com.sumanonline.hisabkhata.core.common.Constants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class OutboxSyncLogicTest {

    @Test
    fun testClientOperationUuidUniqueness() {
        val id1 = UUID.randomUUID().toString()
        val id2 = UUID.randomUUID().toString()
        assertNotEquals(id1, id2)
    }

    @Test
    fun testSyncStateTransitions() {
        val initialStatus = Constants.SYNC_STATUS_PENDING
        assertEquals("PENDING_SYNC", initialStatus)

        val syncingStatus = Constants.SYNC_STATUS_SYNCING
        assertEquals("SYNCING", syncingStatus)

        val syncedStatus = Constants.SYNC_STATUS_SYNCED
        assertEquals("SYNCED", syncedStatus)
    }

    @Test
    fun testTransactionDeduplicationByClientTxId() {
        val existingTxIds = setOf("tx_101", "tx_102", "tx_103")
        val incomingTxId = "tx_102"

        val isDuplicate = existingTxIds.contains(incomingTxId)
        assertTrue(isDuplicate)
    }
}
