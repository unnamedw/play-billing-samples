package com.example.subscriptions.data

import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import com.example.subscriptions.billing.BillingClientLifecycle
import com.example.subscriptions.data.disk.SubLocalDataSource
import com.example.subscriptions.data.disk.db.AppDatabase
import com.example.subscriptions.data.network.SubRemoteDataSource
import com.example.subscriptions.data.network.firebase.FakeServerFunctions
import com.example.subscriptions.data.network.firebase.ServerFunctions
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import java.io.IOException

class SubRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var serverFunctions: ServerFunctions
    private lateinit var subLocalDataSource: SubLocalDataSource
    private lateinit var subRemoteDataSource: SubRemoteDataSource
    private lateinit var billingClientLifecycle: BillingClientLifecycle
    private lateinit var repository: SubRepository

    @Before
    fun setUp() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        // TODO delegate the following instantiations to Hilt
        database = Room.inMemoryDatabaseBuilder(appContext, AppDatabase::class.java).build()
        serverFunctions = FakeServerFunctions.getInstance()
        subLocalDataSource = SubLocalDataSource.getInstance(database.subscriptionStatusDao())
        subRemoteDataSource = SubRemoteDataSource.getInstance(serverFunctions)
        // TODO create mock object
        billingClientLifecycle = BillingClientLifecycle.getInstance(appContext)

        repository = SubRepository.getInstance(
            subLocalDataSource, subRemoteDataSource, billingClientLifecycle
        )
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.close()
    }

    // TODO remove @Ignore and make this passed
    @Ignore
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun registerSubscription_emitRegisteredSubscriptionToStateFlow() {
        runTest {
            repository.registerSubscription("basic_subscription", "TEST_TOKEN")

            val storedSubscriptionList = repository.subscriptions.value
            assertThat(storedSubscriptionList.size, `is`(1))
            val storedSubscription = storedSubscriptionList.first()
            assertThat(storedSubscription.sku, `is`("basic_subscription"))
            assertThat(storedSubscription.purchaseToken, `is`("TEST_TOKEN"))
        }
    }
}