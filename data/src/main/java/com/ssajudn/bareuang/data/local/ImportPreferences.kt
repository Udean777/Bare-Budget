package com.ssajudn.bareuang.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImportPreferences @Inject constructor(
    @ApplicationContext context: Context
) : com.ssajudn.bareuang.domain.port.ImportPreferencesPort {
    private val prefs: SharedPreferences = context.getSharedPreferences("bareuang_import", Context.MODE_PRIVATE)

    private val _importCount = MutableStateFlow(prefs.getInt(KEY_COUNT, 0))
    override val importCount: StateFlow<Int> = _importCount.asStateFlow()

    override fun increment(count: Int) {
        val newVal = _importCount.value + count
        prefs.edit { putInt(KEY_COUNT, newVal) }
        _importCount.value = newVal
    }

    companion object {
        private const val KEY_COUNT = "import_count"
    }
}
