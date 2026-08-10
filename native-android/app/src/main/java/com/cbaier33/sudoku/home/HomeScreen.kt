package com.cbaier33.sudoku.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.cbaier33.sudoku.R
import com.mudita.mmd.components.buttons.ButtonMMD
import com.mudita.mmd.components.buttons.OutlinedButtonMMD
import com.mudita.mmd.components.text.TextMMD

@Composable
fun HomeScreen(
    onPlay: () -> Unit,
    onStatistics: () -> Unit,
) {
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Image(
                    painter = painterResource(R.drawable.sudoku_stars),
                    contentDescription = null,
                    modifier = Modifier.size(300.dp),
                )
            }

            ButtonMMD(
                onClick = onPlay,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextMMD("Play", style = MaterialTheme.typography.titleMedium)
            }

            OutlinedButtonMMD(
                onClick = onStatistics,
                modifier = Modifier.fillMaxWidth(),
            ) {
                TextMMD("Statistics", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
