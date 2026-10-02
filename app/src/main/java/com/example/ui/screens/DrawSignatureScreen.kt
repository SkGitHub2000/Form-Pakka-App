package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.ImageProcessor
import com.example.engine.StorageHelper
import com.example.model.DocKind
import com.example.model.ExamPresets
import com.example.ui.theme.*
import java.util.Locale
import kotlin.math.max

data class DrawnStroke(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrawSignatureScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val strokes = remember { mutableStateListOf<DrawnStroke>() }
    var currentPoints = remember { mutableStateListOf<Offset>() }

    var penColor by remember { mutableStateOf(Color.Black) }
    var strokeWidth by remember { mutableFloatStateOf(4f) }

    // Exam signature specs to export to
    val sigPresets = remember {
        ExamPresets.presets.flatMap { exam ->
            exam.documents.filter { it.kind == DocKind.INK }.map { doc -> exam.name to doc }
        }
    }
    var selectedPresetIndex by remember { mutableIntStateOf(0) }
    val (examName, currentDoc) = sigPresets.getOrElse(selectedPresetIndex) { sigPresets.first() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Draw your Signature", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("draw_signature_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "TARGET EXAM SPEC",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$examName — ${currentDoc.label}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimaryDark
                        )
                        Text(
                            text = "${currentDoc.displaySizeLabel} · ${currentDoc.minKB}–${currentDoc.maxKB} KB",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimaryDark
                        )
                    }
                }
            }

            // Signature Canvas Box
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        // Drawing surface
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(penColor, strokeWidth) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            currentPoints.clear()
                                            currentPoints.add(offset)
                                        },
                                        onDrag = { change, _ ->
                                            currentPoints.add(change.position)
                                        },
                                        onDragEnd = {
                                            if (currentPoints.isNotEmpty()) {
                                                strokes.add(
                                                    DrawnStroke(
                                                        points = currentPoints.toList(),
                                                        color = penColor,
                                                        strokeWidth = strokeWidth
                                                    )
                                                )
                                                currentPoints.clear()
                                            }
                                        }
                                    )
                                }
                        ) {
                            // Draw existing strokes
                            for (stroke in strokes) {
                                if (stroke.points.size < 2) continue
                                val path = androidx.compose.ui.graphics.Path()
                                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                                for (i in 1 until stroke.points.size) {
                                    val prev = stroke.points[i - 1]
                                    val curr = stroke.points[i]
                                    path.quadraticTo(
                                        prev.x, prev.y,
                                        (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f
                                    )
                                }
                                drawPath(
                                    path = path,
                                    color = stroke.color,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = stroke.strokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            }

                            // Draw current active stroke
                            if (currentPoints.size >= 2) {
                                val path = androidx.compose.ui.graphics.Path()
                                path.moveTo(currentPoints[0].x, currentPoints[0].y)
                                for (i in 1 until currentPoints.size) {
                                    val prev = currentPoints[i - 1]
                                    val curr = currentPoints[i]
                                    path.quadraticTo(
                                        prev.x, prev.y,
                                        (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f
                                    )
                                }
                                drawPath(
                                    path = path,
                                    color = penColor,
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                                        width = strokeWidth,
                                        cap = androidx.compose.ui.graphics.StrokeCap.Round,
                                        join = androidx.compose.ui.graphics.StrokeJoin.Round
                                    )
                                )
                            }
                        }

                        // Watermark hint if empty
                        if (strokes.isEmpty() && currentPoints.isEmpty()) {
                            Text(
                                text = "Sign here with your finger or stylus",
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        // Top right Undo / Clear actions
                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    if (strokes.isNotEmpty()) strokes.removeAt(strokes.lastIndex)
                                },
                                enabled = strokes.isNotEmpty()
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Undo")
                            }
                            IconButton(
                                onClick = { strokes.clear() },
                                enabled = strokes.isNotEmpty()
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Clear")
                            }
                        }
                    }
                }
            }

            // Pen Tools Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ink Color", fontWeight = FontWeight.Bold)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black)
                                        .border(
                                            width = if (penColor == Color.Black) 3.dp else 1.dp,
                                            color = if (penColor == Color.Black) GreenPrimary else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable { penColor = Color.Black }
                                )
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF1D4ED8))
                                        .border(
                                            width = if (penColor == Color(0xFF1D4ED8)) 3.dp else 1.dp,
                                            color = if (penColor == Color(0xFF1D4ED8)) GreenPrimary else Color.Gray,
                                            shape = CircleShape
                                        )
                                        .clickable { penColor = Color(0xFF1D4ED8) }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Pen Thickness: ${strokeWidth.toInt()}px",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Slider(
                            value = strokeWidth,
                            onValueChange = { strokeWidth = it },
                            valueRange = 2f..10f,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Export & Save
            item {
                Button(
                    onClick = {
                        if (strokes.isEmpty()) return@Button

                        // Render strokes onto a white bitmap
                        val bmpW = 800
                        val bmpH = 400
                        val bitmap = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap)
                        canvas.drawColor(android.graphics.Color.WHITE)

                        val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.STROKE
                            strokeCap = Paint.Cap.ROUND
                            strokeJoin = Paint.Join.ROUND
                        }

                        // Determine bounding box of drawn points
                        var minX = Float.MAX_VALUE
                        var minY = Float.MAX_VALUE
                        var maxX = Float.MIN_VALUE
                        var maxY = Float.MIN_VALUE

                        for (stroke in strokes) {
                            for (pt in stroke.points) {
                                if (pt.x < minX) minX = pt.x
                                if (pt.y < minY) minY = pt.y
                                if (pt.x > maxX) maxX = pt.x
                                if (pt.y > maxY) maxY = pt.y
                            }
                        }

                        val strokePad = 20f
                        val bWidth = max(50f, maxX - minX + strokePad * 2)
                        val bHeight = max(30f, maxY - minY + strokePad * 2)

                        // Draw centered onto intermediate bitmap
                        for (stroke in strokes) {
                            p.color = stroke.color.toArgb()
                            p.strokeWidth = stroke.strokeWidth * (bmpW / 400f)
                            if (stroke.points.size >= 2) {
                                val path = Path()
                                path.moveTo(stroke.points[0].x, stroke.points[0].y)
                                for (i in 1 until stroke.points.size) {
                                    val prev = stroke.points[i - 1]
                                    val curr = stroke.points[i]
                                    path.quadTo(
                                        prev.x, prev.y,
                                        (prev.x + curr.x) / 2f, (prev.y + curr.y) / 2f
                                    )
                                }
                                canvas.drawPath(path, p)
                            }
                        }

                        // Scale and compress directly to exam requirements
                        val scaled = Bitmap.createScaledBitmap(
                            bitmap,
                            currentDoc.targetWidthPx,
                            currentDoc.targetHeightPx,
                            true
                        )
                        val cleaned = ImageProcessor.cleanInk(scaled)
                        val compressed = ImageProcessor.compressToTargetKB(
                            cleaned,
                            currentDoc.minKB,
                            currentDoc.maxKB
                        )

                        StorageHelper.saveBytesToDownloads(
                            context,
                            compressed.bytes,
                            currentDoc.fileName
                        )
                    },
                    enabled = strokes.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_signature_button")
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Signature (${currentDoc.displaySizeLabel})",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}
