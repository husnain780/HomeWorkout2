package com.homeworkout.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

@Composable
fun FlameIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.08f)
            cubicTo(w * 0.65f, h * 0.25f, w * 0.85f, h * 0.45f, w * 0.85f, h * 0.68f)
            cubicTo(w * 0.85f, h * 0.88f, w * 0.70f, h * 0.95f, w * 0.50f, h * 0.95f)
            cubicTo(w * 0.30f, h * 0.95f, w * 0.15f, h * 0.88f, w * 0.15f, h * 0.68f)
            cubicTo(w * 0.15f, h * 0.48f, w * 0.35f, h * 0.32f, w * 0.42f, h * 0.20f)
            cubicTo(w * 0.45f, h * 0.35f, w * 0.52f, h * 0.45f, w * 0.60f, h * 0.50f)
            cubicTo(w * 0.60f, h * 0.35f, w * 0.55f, h * 0.20f, w * 0.50f, h * 0.08f)
            close()
        }
        drawPath(path = path, color = color, style = Fill)
    }
}

@Composable
fun TrophyIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Cup body
        val cupPath = Path().apply {
            moveTo(w * 0.25f, h * 0.20f)
            lineTo(w * 0.75f, h * 0.20f)
            lineTo(w * 0.70f, h * 0.55f)
            cubicTo(w * 0.68f, h * 0.70f, w * 0.32f, h * 0.70f, w * 0.30f, h * 0.55f)
            close()
        }
        drawPath(cupPath, color, style = stroke)
        // Stem and base
        drawLine(color, Offset(w * 0.5f, h * 0.68f), Offset(w * 0.5f, h * 0.84f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.3f, h * 0.84f), Offset(w * 0.7f, h * 0.84f), stroke.width, StrokeCap.Round)
        // Handles
        drawArc(color, -90f, 180f, false, Offset(w * 0.12f, h * 0.24f), Size(w * 0.22f, h * 0.24f), style = stroke)
        drawArc(color, 90f, -180f, false, Offset(w * 0.66f, h * 0.24f), Size(w * 0.22f, h * 0.24f), style = stroke)
    }
}

@Composable
fun ClockIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val stroke = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
        drawCircle(color = color, radius = w * 0.42f, center = Offset(w * 0.5f, w * 0.5f), style = stroke)
        drawLine(color, Offset(w * 0.5f, w * 0.5f), Offset(w * 0.5f, w * 0.26f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.5f, w * 0.5f), Offset(w * 0.70f, w * 0.5f), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun ZapIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.58f, h * 0.08f)
            lineTo(w * 0.22f, h * 0.52f)
            lineTo(w * 0.48f, h * 0.52f)
            lineTo(w * 0.42f, h * 0.92f)
            lineTo(w * 0.78f, h * 0.44f)
            lineTo(w * 0.52f, h * 0.44f)
            close()
        }
        drawPath(path, color, style = Fill)
    }
}

@Composable
fun DumbbellIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = h * 0.12f, cap = StrokeCap.Round)
        // Center bar
        drawLine(color, Offset(w * 0.28f, h * 0.5f), Offset(w * 0.72f, h * 0.5f), stroke.width, StrokeCap.Round)
        // Left plates
        drawRoundRect(color, Offset(w * 0.20f, h * 0.28f), Size(w * 0.08f, h * 0.44f), CornerRadius(4f, 4f))
        drawRoundRect(color, Offset(w * 0.10f, h * 0.35f), Size(w * 0.07f, h * 0.30f), CornerRadius(3f, 3f))
        // Right plates
        drawRoundRect(color, Offset(w * 0.72f, h * 0.28f), Size(w * 0.08f, h * 0.44f), CornerRadius(4f, 4f))
        drawRoundRect(color, Offset(w * 0.83f, h * 0.35f), Size(w * 0.07f, h * 0.30f), CornerRadius(3f, 3f))
    }
}

@Composable
fun SparkleIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.5f, h * 0.10f)
            cubicTo(w * 0.5f, h * 0.38f, w * 0.62f, h * 0.5f, w * 0.90f, h * 0.5f)
            cubicTo(w * 0.62f, h * 0.5f, w * 0.5f, h * 0.62f, w * 0.5f, h * 0.90f)
            cubicTo(w * 0.5f, h * 0.62f, w * 0.38f, h * 0.5f, w * 0.10f, h * 0.5f)
            cubicTo(w * 0.38f, h * 0.5f, w * 0.5f, h * 0.38f, w * 0.5f, h * 0.10f)
            close()
        }
        drawPath(path, color, style = Fill)
    }
}

@Composable
fun BookIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(w * 0.18f, h * 0.22f)
            lineTo(w * 0.48f, h * 0.30f)
            lineTo(w * 0.48f, h * 0.85f)
            lineTo(w * 0.18f, h * 0.77f)
            close()
            moveTo(w * 0.82f, h * 0.22f)
            lineTo(w * 0.52f, h * 0.30f)
            lineTo(w * 0.52f, h * 0.85f)
            lineTo(w * 0.82f, h * 0.77f)
            close()
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
fun CameraIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        // Body
        drawRoundRect(color, Offset(w * 0.14f, h * 0.30f), Size(w * 0.72f, h * 0.52f), CornerRadius(8f, 8f), style = stroke)
        // Top flash notch
        val topPath = Path().apply {
            moveTo(w * 0.34f, h * 0.30f)
            lineTo(w * 0.40f, h * 0.20f)
            lineTo(w * 0.60f, h * 0.20f)
            lineTo(w * 0.66f, h * 0.30f)
        }
        drawPath(topPath, color, style = stroke)
        // Lens
        drawCircle(color, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.56f), style = stroke)
    }
}

@Composable
fun CrownIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.74f)
            lineTo(w * 0.85f, h * 0.74f)
            lineTo(w * 0.85f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.52f)
            lineTo(w * 0.50f, h * 0.26f)
            lineTo(w * 0.35f, h * 0.52f)
            lineTo(w * 0.15f, h * 0.35f)
            close()
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
fun SettingsGearIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round)
        drawCircle(color, radius = w * 0.26f, center = Offset(w * 0.5f, w * 0.5f), style = stroke)
        repeat(6) { index ->
            val angle = index * (Math.PI / 3).toFloat()
            val x1 = (w * 0.5f + Math.cos(angle.toDouble()) * (w * 0.30f)).toFloat()
            val y1 = (w * 0.5f + Math.sin(angle.toDouble()) * (w * 0.30f)).toFloat()
            val x2 = (w * 0.5f + Math.cos(angle.toDouble()) * (w * 0.44f)).toFloat()
            val y2 = (w * 0.5f + Math.sin(angle.toDouble()) * (w * 0.44f)).toFloat()
            drawLine(color, Offset(x1, y1), Offset(x2, y2), stroke.width, StrokeCap.Round)
        }
    }
}

@Composable
fun SearchGlyph(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val stroke = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
        drawCircle(color, radius = w * 0.28f, center = Offset(w * 0.42f, w * 0.42f), style = stroke)
        drawLine(color, Offset(w * 0.62f, w * 0.62f), Offset(w * 0.85f, w * 0.85f), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun FilterGlyph(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.1f, cap = StrokeCap.Round)
        drawLine(color, Offset(w * 0.15f, h * 0.28f), Offset(w * 0.85f, h * 0.28f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.28f, h * 0.52f), Offset(w * 0.72f, h * 0.52f), stroke.width, StrokeCap.Round)
        drawLine(color, Offset(w * 0.40f, h * 0.76f), Offset(w * 0.60f, h * 0.76f), stroke.width, StrokeCap.Round)
    }
}

@Composable
fun CheckmarkIcon(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val stroke = Stroke(width = w * 0.12f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path().apply {
            moveTo(w * 0.22f, w * 0.52f)
            lineTo(w * 0.44f, w * 0.74f)
            lineTo(w * 0.82f, w * 0.28f)
        }
        drawPath(path, color, style = stroke)
    }
}

@Composable
fun PhoneGlyph(
    color: Color,
    modifier: Modifier = Modifier.size(16.dp)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(width = w * 0.10f, cap = StrokeCap.Round)
        drawRoundRect(color, Offset(w * 0.22f, h * 0.12f), Size(w * 0.56f, h * 0.76f), CornerRadius(8f, 8f), style = stroke)
        drawCircle(color, radius = w * 0.05f, center = Offset(w * 0.5f, h * 0.76f))
    }
}
