package com.ssajudn.bareuang.ui.analytics
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.ssajudn.bareuang.ui.common.labelRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.ssajudn.bareuang.ui.theme.IncomeAccent
import com.ssajudn.bareuang.ui.theme.ExpenseAccent
import com.ssajudn.bareuang.presentation.R
import com.ssajudn.bareuang.ui.common.asString
import com.ssajudn.bareuang.ui.components.ErrorState
import com.ssajudn.bareuang.ui.components.getCategoryIcon
import com.ssajudn.bareuang.ui.theme.AppShapes
import com.ssajudn.bareuang.ui.theme.Spacing
import com.ssajudn.bareuang.ui.theme.categoryColors
import com.ssajudn.bareuang.ui.theme.crispBorder
import com.ssajudn.bareuang.utils.CurrencyFormatter
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.ssajudn.bareuang.ui.components.AppIconButton
import com.ssajudn.bareuang.ui.components.FeatureTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: (() -> Unit)? = null,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.loadAnalyticsData()
        onPauseOrDispose { }
    }

    Scaffold(
        topBar = {
            FeatureTopAppBar(
                titleRes = R.string.analytics_title,
                onNavigateBack = onNavigateBack
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.loadAnalyticsData(isPullToRefresh = true) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AnalyticsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is AnalyticsUiState.Error -> {
                    ErrorState(
                        title = stringResource(R.string.analytics_error_title),
                        message = state.message.asString(),
                        retryLabel = stringResource(R.string.common_retry),
                        modifier = Modifier.align(Alignment.Center),
                        onRetry = { viewModel.loadAnalyticsData() }
                    )
                }

                is AnalyticsUiState.Success -> {
                    AnalyticsContent(
                        state = state,
                        onTabSelected = { viewModel.selectTab(it) }
                    )
                }
            }
        }
    }
}
