package com.example.tenantmanagementsystem.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.example.tenantmanagementsystem.data.model.Tenant
import com.example.tenantmanagementsystem.data.repository.TenantRepository

class TenantViewModel : ViewModel() {

    // Backing MutableLiveData for the list of tenants
    private val _tenants = MutableLiveData<List<Tenant>>()
    val tenants: LiveData<List<Tenant>> get() = _tenants

    // Search query state for filtering tenants in the UI
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> get() = _searchQuery

    // Filtered list automatically recalculated whenever _tenants or _searchQuery changes
    val filteredTenants: LiveData<List<Tenant>> = _tenants.map { list ->
        val query = _searchQuery.value.orEmpty().trim().lowercase()
        if (query.isEmpty()) {
            list
        } else {
            list.filter { tenant ->
                tenant.name.lowercase().contains(query) ||
                        tenant.phone.lowercase().contains(query)
            }
        }
    }

    // Dashboard summary statistics derived via LiveData transformations
    val totalTenantsCount: LiveData<Int> = _tenants.map { it.size }

    val totalMonthlyRent: LiveData<Double> = _tenants.map { list ->
        list.sumOf { it.rent.toDoubleOrNull() ?: 0.0 }
    }

    init {
        loadTenants()
    }

    fun loadTenants() {
        _tenants.value = TenantRepository.tenants.toList()
    }

    fun addTenant(tenant: Tenant) {
        TenantRepository.addTenant(tenant)
        loadTenants()
    }

    fun deleteTenant(tenant: Tenant) {
        TenantRepository.tenants.remove(tenant)
        loadTenants()
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
        // Trigger recalculation of filteredTenants
        _tenants.value = _tenants.value
    }
}