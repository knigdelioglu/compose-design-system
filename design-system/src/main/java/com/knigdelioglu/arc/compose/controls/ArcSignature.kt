package com.knigdelioglu.arc.compose.controls

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import java.io.ByteArrayOutputStream

// Exports also accept caller-created signatures, not only normalized stylus input.
private fun Float.safeSignatureCoordinate(): Float =
    takeIf(Float::isFinite)?.coerceIn(0f, 1f) ?: 0f

private fun Float.safeSignaturePressure(): Float =
    takeIf(Float::isFinite)?.coerceIn(0.1f, 1f) ?: 0.5f

private fun Float.safeSignatureWidth(): Float =
    takeIf(Float::isFinite)?.coerceAtLeast(0f) ?: 0f

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
            val points = stroke.points
            if (points.isEmpty()) return@joinToString ""
            val color = "#${stroke.color.toArgb().toUInt().toString(16).padStart(8, '0').takeLast(6)}"
            val opacity = stroke.color.alpha.coerceIn(0f, 1f)
            val brushWidth = stroke.widthPx.safeSignatureWidth()
            if (points.size == 1) {
                val point = points.first()
                val radius = brushWidth * scale * point.pressure.safeSignaturePressure() / 2f
                "<circle cx=\"${point.x.safeSignatureCoordinate() * safeWidth}\" cy=\"${point.y.safeSignatureCoordinate() * safeHeight}\" r=\"$radius\" fill=\"$color\" fill-opacity=\"$opacity\"/>"
            } else {
                // One segment per pair reproduces the pressure-dependent widths used by PNG.
                points.zipWithNext().joinToString("") { (startPoint, endPoint) ->
                    val widthPx = brushWidth * scale *
                        ((startPoint.pressure.safeSignaturePressure() + endPoint.pressure.safeSignaturePressure()) / 2f)
                    val x1 = startPoint.x.safeSignatureCoordinate() * safeWidth
                    val y1 = startPoint.y.safeSignatureCoordinate() * safeHeight
                    val x2 = endPoint.x.safeSignatureCoordinate() * safeWidth
                    val y2 = endPoint.y.safeSignatureCoordinate() * safeHeight
                    "<path d=\"M$x1,$y1 L$x2,$y2\" fill=\"none\" stroke=\"$color\" stroke-opacity=\"$opacity\" stroke-width=\"$widthPx\" stroke-linecap=\"round\" stroke-linejoin=\"round\"/>"
                }
            }
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
                    points[0].x.safeSignatureCoordinate() * safeWidth,
                    points[0].y.safeSignatureCoordinate() * safeHeight,
                    stroke.widthPx.safeSignatureWidth() * scale * points[0].pressure.safeSignaturePressure() / 2f,
                    paint
                )
            } else {
                points.zipWithNext().forEach { (start, end) ->
                    paint.strokeWidth = stroke.widthPx * scale * ((start.pressure.safeSignaturePressure() + end.pressure.safeSignaturePressure()) / 2f)
                    canvas.drawLine(
                        start.x.safeSignatureCoordinate() * safeWidth,
                        start.y.safeSignatureCoordinate() * safeHeight,
                        end.x.safeSignatureCoordinate() * safeWidth,
                        end.y.safeSignatureCoordinate() * safeHeight,
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
