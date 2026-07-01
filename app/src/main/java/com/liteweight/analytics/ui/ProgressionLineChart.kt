package com.liteweight.analytics.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.liteweight.analytics.domain.ChartPoint

@Composable
fun ProgressionLineChart(
    points: List<ChartPoint>,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val prColor = MaterialTheme.colorScheme.tertiary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)

    Canvas(
        modifier =
            modifier
                .fillMaxWidth()
                .height(180.dp),
    ) {
        if (points.size < 2) return@Canvas

        val minX = points.minOf { it.completedAtEpochMs }.toFloat()
        val maxX = points.maxOf { it.completedAtEpochMs }.toFloat()
        val minY = points.minOf { it.value }.toFloat()
        val maxY = points.maxOf { it.value }.toFloat()
        val yRange = (maxY - minY).takeIf { it > 0f } ?: 1f
        val xRange = (maxX - minX).takeIf { it > 0f } ?: 1f

        val padding = 16f
        val chartWidth = size.width - padding * 2
        val chartHeight = size.height - padding * 2

        fun xFor(epochMs: Long): Float =
            padding + ((epochMs - minX) / xRange) * chartWidth

        fun yFor(value: Double): Float =
            padding + chartHeight - (((value - minY) / yRange) * chartHeight).toFloat()

        drawLine(
            color = gridColor,
            start = Offset(padding, padding + chartHeight),
            end = Offset(padding + chartWidth, padding + chartHeight),
            strokeWidth = 1f,
        )

        val path = Path()
        points.forEachIndexed { index, point ->
            val x = xFor(point.completedAtEpochMs)
            val y = yFor(point.value)
            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 3f))

        points.forEach { point ->
            val center = Offset(xFor(point.completedAtEpochMs), yFor(point.value))
            drawCircle(
                color = if (point.isPr) prColor else lineColor,
                radius = if (point.isPr) 7f else 5f,
                center = center,
            )
        }
    }
}
