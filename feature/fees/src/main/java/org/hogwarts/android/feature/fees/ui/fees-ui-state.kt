package org.hogwarts.android.feature.fees.ui

import org.hogwarts.android.feature.fees.domain.model.FeeRecord

data class FeesUiState(
    val isLoading: Boolean = true,
    val fees: List<FeeRecord> = emptyList(),
    val selectedStatusFilter: String? = null,
    val error: String? = null
)
