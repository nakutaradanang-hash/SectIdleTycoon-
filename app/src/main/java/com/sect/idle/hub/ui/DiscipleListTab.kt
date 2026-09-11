package com.sect.idle.hub.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sect.idle.hub.SectHubState
import com.sect.idle.hub.SectHubViewModel

@Composable
fun DiscipleListTab(
    state: SectHubState,
    viewModel: SectHubViewModel,
    modifier: Modifier = Modifier
) {
    DiscipleList(
        state = state,
        viewModel = viewModel,
        modifier = modifier
    )
}
