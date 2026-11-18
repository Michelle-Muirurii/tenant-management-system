package com.example.tenantmanagementsystem.data.local

import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.tenantmanagementsystem.data.model.Tenant
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class TenantDaoTest {

    private lateinit var database: TenantDatabase
    private lateinit var tenantDao: TenantDao

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, TenantDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        tenantDao = database.tenantDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndRetrieveTenant() = runBlocking {
        val tenant = Tenant(
            name = "Grace Njeri",
            phone = "0722000111",
            rent = "20000.0",
            id = 1,
        )

        tenantDao.insertTenant(tenant)

        val latch = CountDownLatch(1)
        var retrievedList: List<Tenant> = emptyList()

        val observer = Observer<List<Tenant>> { tenants ->
            retrievedList = tenants
            latch.countDown()
        }

        tenantDao.getAllTenants().observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)
        tenantDao.getAllTenants().removeObserver(observer)

        assertEquals(1, retrievedList.size)
        assertEquals("Grace Njeri", retrievedList[0].name)
        assertEquals("0722000111", retrievedList[0].phone)
        assertEquals("20000.0", retrievedList[0].rent)
    }

    @Test
    fun insertMultipleTenants_returnsSortedByName() = runBlocking {
        val tenant1 = Tenant(
            name = "Bob Smith",
            phone = "0711111111",
            rent = "15000.0",
            id = 1,
        )
        val tenant2 = Tenant(
            name = "Alice Jones",
            phone = "0722222222",
            rent = "18000.0",
            id = 2,
        )

        tenantDao.insertTenant(tenant1)
        tenantDao.insertTenant(tenant2)

        val latch = CountDownLatch(1)
        var retrievedList: List<Tenant> = emptyList()

        val observer = Observer<List<Tenant>> { tenants ->
            retrievedList = tenants
            latch.countDown()
        }

        tenantDao.getAllTenants().observeForever(observer)
        latch.await(2, TimeUnit.SECONDS)
        tenantDao.getAllTenants().removeObserver(observer)

        assertEquals(2, retrievedList.size)
        assertEquals("Alice Jones", retrievedList[0].name)
        assertEquals("Bob Smith", retrievedList[1].name)
    }
}
