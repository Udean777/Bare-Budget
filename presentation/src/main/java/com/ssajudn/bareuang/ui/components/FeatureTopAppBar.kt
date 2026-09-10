package com.ssajudn.bareuang.ui.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.presentation.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeatureTopAppBar(
    @StringRes titleRes: Int,
    onNavigateBack: (() -> Unit)? = null,
    backEnabled: Boolean = true
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(titleRes),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                AppIconButton(enabled = backEnabled, onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.common_back)
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        )
    )
}
