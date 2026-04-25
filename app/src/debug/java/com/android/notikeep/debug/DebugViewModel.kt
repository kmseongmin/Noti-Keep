package com.android.notikeep.debug

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DebugViewModel @Inject constructor(
    private val seeder: TestDataSeeder
) : ViewModel() {

    fun seedTestData() {
        viewModelScope.launch { seeder.seed() }
    }
}
