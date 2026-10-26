package com.example.fiscalflow

import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FiscalFlowCloudSyncTest {

    @Test
    fun cloudSyncDisabled_hasCloudData_returnsFalseImmediately() = runBlocking {
        assertFalse("IS_CLOUD_SYNC_ENABLED must be false", FiscalFlowApp.IS_CLOUD_SYNC_ENABLED)
        val result = FiscalFlowCloudSync.hasCloudData("test-uid")
        assertFalse("hasCloudData should return false when cloud sync is disabled", result)
    }

    @Test
    fun cloudSyncDisabled_deleteCloudExpense_returnsWithoutError() = runBlocking {
        assertFalse("IS_CLOUD_SYNC_ENABLED must be false", FiscalFlowApp.IS_CLOUD_SYNC_ENABLED)
        FiscalFlowCloudSync.deleteCloudExpense(100L, "test-uid")
        assertTrue(true)
    }

    @Test
    fun cloudSyncDisabled_deleteCloudCategory_returnsWithoutError() = runBlocking {
        assertFalse("IS_CLOUD_SYNC_ENABLED must be false", FiscalFlowApp.IS_CLOUD_SYNC_ENABLED)
        FiscalFlowCloudSync.deleteCloudCategory("Groceries", "test-uid")
        assertTrue(true)
    }
}
