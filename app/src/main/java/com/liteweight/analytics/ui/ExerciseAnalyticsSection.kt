package com.liteweight.analytics.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.liteweight.analytics.domain.ChartMetric
import com.liteweight.analytics.domain.ChartWindow
import com.liteweight.analytics.domain.ExerciseAnalyticsSnapshot
import com.liteweight.analytics.domain.PrType
import com.liteweight.analytics.domain.availableMetrics
import com.liteweight.analytics.domain.label
import com.liteweight.exercise.domain.UnitType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ExerciseAnalyticsSection(
    analytics: ExerciseAnalyticsSnapshot?,
    chartMetric: ChartMetric,
    chartWindow: ChartWindow,
    weightLabel: String,
    onMetricChange: (ChartMetric) -> Unit,
    onWindowChange: (ChartWindow) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (analytics == null) return

    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Progression", style = MaterialTheme.typography.titleMedium)
            if (analytics.sessionCount == 0) {
                Text(
                    "Complete a workout with this exercise to see charts.",
                    style = MaterialTheme.typography.bodySmall,
                )
                return@Column
            }

            PrCallouts(analytics = analytics, weightLabel = weightLabel)

            Text("Window", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ChartWindow.entries.forEach { window ->
                    FilterChip(
                        selected = chartWindow == window,
                        onClick = { onWindowChange(window) },
                        label = { Text(window.label) },
                    )
                }
            }

            Text("Metric", style = MaterialTheme.typography.labelMedium)
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                availableMetrics(analytics.unitType).forEach { metric ->
                    FilterChip(
                        selected = chartMetric == metric,
                        onClick = { onMetricChange(metric) },
                        label = { Text(metric.label(analytics.unitType)) },
                    )
                }
            }

            if (analytics.chartPoints.size >= 2) {
                ProgressionLineChart(points = analytics.chartPoints)
            } else {
                Text(
                    "Need at least two logged sessions for a trend line.",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            if (analytics.prTimeline.isNotEmpty()) {
                Text("PR history", style = MaterialTheme.typography.labelLarge)
                analytics.prTimeline.take(5).forEach { event ->
                    Text(
                        text = formatPrLine(event.prType, event.value, event.achievedAtEpochMs, weightLabel, analytics.unitType),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }
    }
}

@Composable
private fun PrCallouts(
    analytics: ExerciseAnalyticsSnapshot,
    weightLabel: String,
) {
    val callouts = buildList {
        analytics.bestLoad?.let { add("Best load: $it $weightLabel") }
        analytics.bestE1rm?.let { add("Best est. 1RM: ${"%.1f".format(it)} $weightLabel") }
        analytics.bestVolume?.let { add("Best volume: ${"%.0f".format(it)}") }
        analytics.bestReps?.let { add("Best reps: $it") }
    }
    if (callouts.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        callouts.forEach { line ->
            Text(line, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
        }
        Text(
            "${analytics.sessionCount} logged sessions",
            style = MaterialTheme.typography.bodySmall,
        )
    }
}

private val ChartWindow.label: String
    get() =
        when (this) {
            ChartWindow.WEEKS_4 -> "4w"
            ChartWindow.WEEKS_12 -> "12w"
            ChartWindow.MONTHS_6 -> "6m"
            ChartWindow.ALL -> "All"
        }

private fun formatPrLine(
    prType: PrType,
    value: Double,
    achievedAtEpochMs: Long,
    weightLabel: String,
    unitType: UnitType,
): String {
    val date = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(achievedAtEpochMs))
    val valueText =
        when (prType) {
            PrType.MAX_LOAD -> "${"%.1f".format(value)} $weightLabel"
            PrType.MAX_VOLUME -> "${"%.0f".format(value)}"
            PrType.MAX_E1RM -> "${"%.1f".format(value)} $weightLabel est. 1RM"
            PrType.MAX_REPS -> "${value.toInt()} reps"
        }
    return "${prType.label(unitType)}: $valueText · $date"
}

private fun PrType.label(unitType: UnitType): String =
    when (this) {
        PrType.MAX_LOAD -> if (unitType == UnitType.BODYWEIGHT) "Max reps" else "Max load"
        PrType.MAX_VOLUME -> "Max volume"
        PrType.MAX_E1RM -> "Max est. 1RM"
        PrType.MAX_REPS -> "Max reps"
    }
