package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.RectF
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormScanScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var scanBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var selectedTarget by remember { mutableStateOf("photo") } // "photo", "signature", "thumb"

    val defaultExam = ExamPresets.presets.first { it.id == "ibps" }
    val targetDoc = remember(selectedTarget) {
        when (selectedTarget) {
            "signature" -> defaultExam.documents.first { it.id == "signature" }
            "thumb" -> defaultExam.documents.first { it.id == "thumb" }
            else -> defaultExam.documents.first { it.id == "photo" }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bmp = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                scanBitmap = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Processed crop preview
    val croppedResult by remember(scanBitmap, selectedTarget) {
        derivedStateOf {
            val bmp = scanBitmap
            if (bmp != null) {
                // Approximate region based on standard forms (e.g. top right for photo, bottom for sign)
                val rect = when (selectedTarget) {
                    "signature" -> RectF(
                        bmp.width * 0.15f,
                        bmp.height * 0.70f,
                        bmp.width * 0.85f,
                        bmp.height * 0.95f
                    )
                    "thumb" -> RectF(
                        bmp.width * 0.10f,
                        bmp.height * 0.60f,
                        bmp.width * 0.50f,
                        bmp.height * 0.85f
                    )
                    else -> RectF(
                        bmp.width * 0.55f,
                        bmp.height * 0.05f,
                        bmp.width * 0.95f,
                        bmp.height * 0.45f
                    )
                }

                var extracted = ImageProcessor.cropAndScale(
                    bmp,
                    rect,
                    targetDoc.targetWidthPx,
                    targetDoc.targetHeightPx,
                    fillFrame = true
                )

                if (targetDoc.kind == DocKind.INK) {
                    extracted = ImageProcessor.cleanInk(extracted)
                }

                val compressed = ImageProcessor.compressToTargetKB(
                    extracted,
                    targetDoc.minKB,
                    targetDoc.maxKB
                )
                extracted to compressed
            } else null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto-crop from Form Scan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .testTag("form_scan_screen"),
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
                            text = "ONE SCAN TO ALL DOCUMENTS",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Extract Photo & Signature from a single form page",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimaryDark
                        )
                        Text(
                            text = "If you have a scanned page or a photo of your printed application form, select what you want to extract below.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimaryDark
                        )
                    }
                }
            }

            // Target selector chips
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    FilterChip(
                        selected = selectedTarget == "photo",
                        onClick = { selectedTarget = "photo" },
                        label = { Text("Photo (Passport)") },
                        leadingIcon = { Icon(Icons.Default.Face, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    FilterChip(
                        selected = selectedTarget == "signature",
                        onClick = { selectedTarget = "signature" },
                        label = { Text("Signature") },
                        leadingIcon = { Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                    FilterChip(
                        selected = selectedTarget == "thumb",
                        onClick = { selectedTarget = "thumb" },
                        label = { Text("Thumb") },
                        leadingIcon = { Icon(Icons.Default.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    )
                }
            }

            // Upload / Preview Container
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (croppedResult != null) {
                            val (bmp, comp) = croppedResult!!
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Cropped Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                            )
                            Row(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${comp.width} × ${comp.height} px · ${String.format(Locale.US, "%.1f", comp.sizeKB)} KB",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.CropFree,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                    tint = GreenPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Upload scanned page",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(
                                                ActivityResultContracts.PickVisualMedia.ImageOnly
                                            )
                                        )
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Choose Scan Image")
                                }
                            }
                        }
                    }
                }
            }

            // Save Extracted Button
            if (croppedResult != null) {
                item {
                    val comp = croppedResult!!.second
                    Button(
                        onClick = {
                            StorageHelper.saveBytesToDownloads(
                                context,
                                comp.bytes,
                                targetDoc.fileName
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_cropped_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Cropped ${targetDoc.label} (${String.format(Locale.US, "%.1f", comp.sizeKB)} KB)",
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
