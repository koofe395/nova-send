package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.NovaViewModel
import com.example.ui.components.TransferDetailsModal
import com.example.ui.theme.NovaBorder
import com.example.ui.theme.NovaError
import com.example.ui.theme.NovaPrimary
import com.example.ui.theme.NovaTextMuted
import com.example.ui.theme.NovaTextPrimary
import com.example.ui.theme.NovaTextSecondary

@Composable
fun HistoryScreen(
  viewModel: NovaViewModel,
  modifier: Modifier = Modifier
) {
  val filteredTransfers by viewModel.filteredTransfers.collectAsStateWithLifecycle()
  val searchQuery by viewModel.historySearch.collectAsStateWithLifecycle()
  val currentFilter by viewModel.historyFilter.collectAsStateWithLifecycle()
  val inspectedTransfer by viewModel.inspectedTransfer.collectAsStateWithLifecycle()

  Column(
    modifier = modifier
      .fillMaxSize()
      .background(com.example.ui.theme.NovaBackground)
      .testTag("history_screen")
  ) {
    // Top Bar & Search
    Surface(
      color = Color.White,
      border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Transfer History & Ledger",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = NovaTextPrimary
          )

          IconButton(
            onClick = { viewModel.clearAllHistory() },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = NovaTextMuted)
          }
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.setHistorySearch(it) },
          modifier = Modifier.fillMaxWidth().testTag("history_search_input"),
          placeholder = { Text("Search by file name or contact...", fontSize = 13.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NovaTextMuted) },
          shape = RoundedCornerShape(24.dp),
          colors = TextFieldDefaults.colors(
            focusedContainerColor = com.example.ui.theme.NovaSurfaceVariant,
            unfocusedContainerColor = com.example.ui.theme.NovaSurfaceVariant,
            focusedIndicatorColor = NovaPrimary,
            unfocusedIndicatorColor = Color.Transparent
          ),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Pills
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf(
            Pair("ALL", "All"),
            Pair("SUCCESSFUL", "Successful"),
            Pair("FAILED", "Failed"),
            Pair("SENT", "Sent"),
            Pair("RECEIVED", "Received")
          ).forEach { (filterKey, label) ->
            val isSelected = currentFilter == filterKey
            val pillColor = if (isSelected) {
              when (filterKey) {
                "FAILED" -> com.example.ui.theme.NovaError
                "SUCCESSFUL" -> com.example.ui.theme.NovaSuccess
                else -> NovaPrimary
              }
            } else {
              com.example.ui.theme.NovaSurfaceVariant
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(100.dp))
                .background(pillColor)
                .border(1.dp, if (isSelected) pillColor else com.example.ui.theme.NovaBorderSubtle, RoundedCornerShape(100.dp))
                .clickable { viewModel.setHistoryFilter(filterKey) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
              Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) Color.White else NovaTextSecondary
              )
            }
          }
        }
      }
    }

    // List of History Transactions
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      if (filteredTransfers.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, com.example.ui.theme.NovaBorderSubtle)
          ) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(36.dp),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.Sync,
                  contentDescription = null,
                  tint = NovaTextMuted,
                  modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("No transfers found", fontWeight = FontWeight.Bold, color = NovaTextPrimary)
                Text("Try adjusting your search query or filters.", fontSize = 12.sp, color = NovaTextSecondary)
              }
            }
          }
        }
      } else {
        items(filteredTransfers, key = { it.id }) { transfer ->
          TransferHistoryItemRow(
            transfer = transfer,
            onClick = { viewModel.inspectTransfer(transfer) },
            onRetry = { viewModel.retryTransfer(transfer) }
          )
        }
      }
    }
  }

  // Details Modal
  inspectedTransfer?.let { transfer ->
    TransferDetailsModal(
      transfer = transfer,
      onDismiss = { viewModel.inspectTransfer(null) },
      onDelete = { viewModel.deleteTransfer(transfer.id) },
      onRetry = { viewModel.retryTransfer(transfer) }
    )
  }
}
