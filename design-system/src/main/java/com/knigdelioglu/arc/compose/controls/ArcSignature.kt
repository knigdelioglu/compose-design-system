package com.knigdelioglu.arc.compose.controls

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.io.ByteArrayOutputStream

/** Normalized point data keeps a signature independent of the size used to draw it. */
data class ArcSignaturePoint(
    val x: Float,
    val y: Float,
    val pressure: Float = 1f,
    val timeMillis: Long = 0L
)

data class ArcSignatureStroke(
    val points: List<ArcSignaturePoint>,
    val color: Color,
    /** Width in pixels at the size where the stroke was captured. */
    val widthPx: Float
)

data class ArcSignature(
    val strokes: List<ArcSignatureStroke>,
    val canvasWidthPx: Int,
    val canvasHeightPx: Int
) {
    val isEmpty: Boolean get() = strokes.none { it.points.isNotEmpty() }

    /** Exports the captured pressure profile as a standalone SVG image. */
    fun toSvg(width: Int = canvasWidthPx, height: Int = canvasHeightPx): String {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val scale = minOf(safeWidth.toFloat() / canvasWidthPx.coerceAtLeast(1), safeHeight.toFloat() / canvasHeightPx.coerceAtLeast(1))
        val body = strokes.joinToString("") { stroke ->
            if (stroke.points.isEmpty()) return@joinToString ""
            val pathData = buildString {
                stroke.points.forEachIndexed { index, point ->
                    append(if (index == 0) "M" else "L")
                    append("${point.x.coerceIn(0f, 1f) * safeWidth},${point.y.coerceIn(0f, 1f) * safeHeight} ")
                }
            }
            val pressure = stroke.points.map { it.pressure.coerceIn(0.1f, 1f) }.average().toFloat()
            "<path d=\"$pathData\" fill=\"none\" stroke=\"#${stroke.color.toArgb().toUInt().toString(16).takeLast(6)}\" stroke-width=\"${stroke.widthPx * scale * pressure}\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>"
        }
        return "<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"$safeWidth\" height=\"$safeHeight\" viewBox=\"0 0 $safeWidth $safeHeight\">$body</svg>"
    }

    /** Renders a transparent PNG and returns the encoded bytes for app-owned storage/sharing. */
    fun toPngBytes(
        width: Int = canvasWidthPx,
        height: Int = canvasHeightPx,
        backgroundColor: Color = Color.Transparent
    ): ByteArray {
        val safeWidth = width.coerceAtLeast(1)
        val safeHeight = height.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(safeWidth, safeHeight, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.drawColor(backgroundColor.toArgb())
        val scale = minOf(safeWidth.toFloat() / canvasWidthPx.coerceAtLeast(1), safeHeight.toFloat() / canvasHeightPx.coerceAtLeast(1))
        strokes.forEach { stroke ->
            val points = stroke.points
            if (points.isEmpty()) return@forEach
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stroke.color.toArgb()
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
            }
            if (points.size == 1) {
                paint.style = Paint.Style.FILL
                paint.color = stroke.color.toArgb()
                canvas.drawCircle(
                    points[0].x.coerceIn(0f, 1f) * safeWidth,
                    points[0].y.coerceIn(0f, 1f) * safeHeight,
                    stroke.widthPx * scale * points[0].pressure.coerceIn(0.1f, 1f) / 2f,
                    paint
                )
            } else {
                points.zipWithNext().forEach { (start, end) ->
                    paint.strokeWidth = stroke.widthPx * scale * ((start.pressure + end.pressure) / 2f).coerceIn(0.1f, 1f)
                    canvas.drawLine(
                        start.x.coerceIn(0f, 1f) * safeWidth,
                        start.y.coerceIn(0f, 1f) * safeHeight,
                        end.x.coerceIn(0f, 1f) * safeWidth,
                        end.y.coerceIn(0f, 1f) * safeHeight,
                        paint
                    )
                }
            }
        }
        return ByteArrayOutputStream().use { output ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, output)
            bitmap.recycle()
            output.toByteArray()
        }
    }
}
