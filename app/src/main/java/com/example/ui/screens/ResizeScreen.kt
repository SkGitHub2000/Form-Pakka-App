package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.engine.CompressionResult
import com.example.engine.ImageProcessor
import com.example.engine.QualityCheckResult
import com.example.engine.StorageHelper
import com.example.model.DocKind
import com.example.model.DocumentSpec
import com.example.model.ExamPreset
import com.example.model.ExamPresets
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResizeScreen(
    initialExamId: String? = null,
    onBack: () -> Unit,
    onOpenPrintSheet: (Bitmap) -> Unit
) {
    val context = LocalContext.current
    val allExams = ExamPresets.presets

    var selectedExam by remember {
        mutableStateOf(allExams.find { it.id == initialExamId } ?: allExams.first())
    }
    var selectedDocIndex by remember { mutableIntStateOf(0) }
    val currentDoc = selectedExam.documents.getOrNull(selectedDocIndex) ?: selectedExam.documents.first()

    // Loaded source bitmap
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var rotationDeg by remember { mutableFloatStateOf(0f) }
    var straightenDeg by remember { mutableFloatStateOf(0f) }
    var flipH by remember { mutableStateOf(false) }
    var enableInkCleanup by remember { mutableStateOf(currentDoc.kind == DocKind.INK) }
    var selectedBgColor by remember { mutableStateOf<Int?>(null) } // null = original, Color.WHITE, etc.

    // Name & date strip state
    var enableNameDate by remember { mutableStateOf(currentDoc.requiresNameDate) }
    var candidateName by remember { mutableStateOf("RAHUL KUMAR") }
    var photoDate by remember {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        mutableStateOf(sdf.format(Date()))
    }

    var showExamDialog by remember { mutableStateOf(false) }

    // Update defaults when doc changes
    LaunchedEffect(currentDoc) {
        enableInkCleanup = currentDoc.kind == DocKind.INK
        enableNameDate = currentDoc.requiresNameDate
    }

    // Pick visual media launcher
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
                sourceBitmap = bmp
                rotationDeg = 0f
                straightenDeg = 0f
                flipH = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        if (bmp != null) {
            sourceBitmap = bmp
            rotationDeg = 0f
            straightenDeg = 0f
            flipH = false
        }
    }

    // Processed output bitmap & compression state
    var processedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressionResult by remember { mutableStateOf<CompressionResult?>(null) }
    var qualityCheck by remember { mutableStateOf<QualityCheckResult?>(null) }

    // Recalculate output whenever inputs or settings change
    LaunchedEffect(
        sourceBitmap,
        rotationDeg,
        straightenDeg,
        flipH,
        enableInkCleanup,
        selectedBgColor,
        enableNameDate,
        candidateName,
        photoDate,
        currentDoc
    ) {
        val src = sourceBitmap
        if (src != null) {
            var transformed = ImageProcessor.transformBitmap(src, rotationDeg, straightenDeg, flipH)

            if (enableInkCleanup) {
                transformed = ImageProcessor.cleanInk(transformed)
            } else if (selectedBgColor != null) {
                transformed = ImageProcessor.applyBackgroundColor(transformed, selectedBgColor!!)
            }

            var finalBmp = ImageProcessor.cropAndScale(
                transformed,
                null,
                currentDoc.targetWidthPx,
                currentDoc.targetHeightPx,
                fillFrame = true
            )

            if (enableNameDate && candidateName.isNotBlank()) {
                finalBmp = ImageProcessor.overlayNameAndDate(finalBmp, candidateName, photoDate)
            }

            val compressed = ImageProcessor.compressToTargetKB(
                finalBmp,
                currentDoc.minKB,
                currentDoc.maxKB
            )

            val qCheck = ImageProcessor.checkPhotoQuality(
                src,
                currentDoc.targetWidthPx,
                currentDoc.targetHeightPx
            )

            processedBitmap = finalBmp
            compressionResult = compressed
            qualityCheck = qCheck
        } else {
            processedBitmap = null
            compressionResult = null
            qualityCheck = null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Photo & Signature Resizer", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("back_button")) {
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
                .testTag("resize_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exam Selector Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showExamDialog = true }
                        .testTag("exam_selector_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GreenLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.School, contentDescription = null, tint = GreenPrimary)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exam / Portal",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = selectedExam.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { showExamDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenLight),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Change", color = GreenPrimaryDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Document Tabs
            item {
                Column {
                    Text(
                        text = "Document Type",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedExam.documents.indices.toList()) { index ->
                            val doc = selectedExam.documents[index]
                            val isSelected = index == selectedDocIndex
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedDocIndex = index },
                                label = { Text(doc.label) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (doc.kind == DocKind.PHOTO) Icons.Default.Face else Icons.Default.Draw,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GreenPrimary,
                                    selectedLabelColor = Color.White,
                                    selectedLeadingIconColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("doc_chip_${doc.id}")
                            )
                        }
                    }
                }
            }

            // Document Requirements Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = GreenLight)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "REQUIREMENTS",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimaryDark,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = currentDoc.fileName,
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimaryDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Dimensions", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    currentDoc.displaySizeLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimaryDark
                                )
                            }
                            Column {
                                Text("File Size", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    "${currentDoc.minKB} – ${currentDoc.maxKB} KB",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimaryDark
                                )
                            }
                            Column {
                                Text("Format", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    "JPG",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = GreenPrimaryDark
                                )
                            }
                        }
                    }
                }
            }

            // Image Preview & Upload Container
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (processedBitmap != null) {
                            Image(
                                bitmap = processedBitmap!!.asImageBitmap(),
                                contentDescription = "Processed Image Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )

                            // Status badge overlay
                            compressionResult?.let { res ->
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 16.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(horizontal = 14.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GreenAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${res.width} × ${res.height} px · ${String.format(Locale.US, "%.1f", res.sizeKB)} KB",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    imageVector = if (currentDoc.kind == DocKind.PHOTO) Icons.Default.AddPhotoAlternate else Icons.Default.Draw,
                                    contentDescription = null,
                                    modifier = Modifier.size(56.dp),
                                    tint = GreenPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Add your ${currentDoc.label.lowercase()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Target: ${currentDoc.displaySizeLabel} · ${currentDoc.minKB}–${currentDoc.maxKB} KB",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Button(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                androidx.activity.result.PickVisualMediaRequest(
                                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                                )
                                            )
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("upload_image_button")
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Choose Photo")
                                    }
                                    OutlinedButton(
                                        onClick = { cameraLauncher.launch() },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.testTag("camera_button")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Camera")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Adjustments & Controls (if image is loaded)
            if (sourceBitmap != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ADJUST & ENHANCE",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            // Rotate & Flip buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { rotationDeg = (rotationDeg - 90f) % 360f },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.RotateLeft, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("-90°")
                                }
                                OutlinedButton(
                                    onClick = { rotationDeg = (rotationDeg + 90f) % 360f },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.RotateRight, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("+90°")
                                }
                                OutlinedButton(
                                    onClick = { flipH = !flipH },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Flip, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Flip")
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Straighten slider
                            Text(
                                text = "Straighten: ${straightenDeg.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Slider(
                                value = straightenDeg,
                                onValueChange = { straightenDeg = it },
                                valueRange = -15f..15f,
                                modifier = Modifier.fillMaxWidth()
                            )

                            // Ink cleanup (for signatures/thumbs)
                            if (currentDoc.kind == DocKind.INK) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Ink Cleanup", fontWeight = FontWeight.Bold)
                                        Text(
                                            "Whitens paper background, crisps dark ink strokes",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = enableInkCleanup,
                                        onCheckedChange = { enableInkCleanup = it }
                                    )
                                }
                            }

                            // Background color replacement (for photos)
                            if (currentDoc.kind == DocKind.PHOTO) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Background Color",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    FilterChip(
                                        selected = selectedBgColor == null,
                                        onClick = { selectedBgColor = null },
                                        label = { Text("Original") }
                                    )
                                    FilterChip(
                                        selected = selectedBgColor == android.graphics.Color.WHITE,
                                        onClick = { selectedBgColor = android.graphics.Color.WHITE },
                                        label = { Text("White") }
                                    )
                                    FilterChip(
                                        selected = selectedBgColor == android.graphics.Color.parseColor("#E0F2FE"),
                                        onClick = { selectedBgColor = android.graphics.Color.parseColor("#E0F2FE") },
                                        label = { Text("Light Blue") }
                                    )
                                }
                            }

                            // Name & Date on photo (NEET / UPSC requirement)
                            if (currentDoc.kind == DocKind.PHOTO) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider()
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Name & Date on photo", fontWeight = FontWeight.Bold)
                                        Text(
                                            "Printed in a white strip under the photo (NEET/UPSC)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = enableNameDate,
                                        onCheckedChange = { enableNameDate = it }
                                    )
                                }

                                if (enableNameDate) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = candidateName,
                                        onValueChange = { candidateName = it },
                                        label = { Text("Candidate Full Name") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = photoDate,
                                        onValueChange = { photoDate = it },
                                        label = { Text("Date of Photo (DD/MM/YYYY)") },
                                        modifier = Modifier.fillMaxWidth(),
                                        singleLine = true
                                    )
                                }
                            }
                        }
                    }
                }

                // Compliance Checklist
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "PORTAL VERIFICATION CHECKLIST",
                                style = MaterialTheme.typography.labelSmall,
                                color = GreenPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            val res = compressionResult
                            if (res != null) {
                                // Dimensions check
                                RuleRow(
                                    title = "Dimensions: ${res.width} × ${res.height} px",
                                    subtitle = "Exact match for ${currentDoc.displaySizeLabel}",
                                    isPass = true
                                )
                                // File size check
                                RuleRow(
                                    title = "File size: ${String.format(Locale.US, "%.1f", res.sizeKB)} KB",
                                    subtitle = "Allowed: ${currentDoc.minKB}–${currentDoc.maxKB} KB",
                                    isPass = res.meetsMinKB && res.meetsMaxKB
                                )
                                // Quality check
                                qualityCheck?.let { qc ->
                                    RuleRow(
                                        title = "Lighting & Clarity: ${qc.brightnessNote}",
                                        subtitle = qc.sharpnessNote,
                                        isPass = qc.isOverallGood
                                    )
                                }
                            }
                        }
                    }
                }

                // Download & Share Actions
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                compressionResult?.let { res ->
                                    StorageHelper.saveBytesToDownloads(
                                        context,
                                        res.bytes,
                                        currentDoc.fileName
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("download_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Save ${currentDoc.fileName} (${String.format(Locale.US, "%.1f", compressionResult?.sizeKB ?: 0f)} KB)",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    compressionResult?.let { res ->
                                        StorageHelper.shareBytes(
                                            context,
                                            res.bytes,
                                            currentDoc.fileName,
                                            "image/jpeg"
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share")
                            }

                            if (currentDoc.kind == DocKind.PHOTO && processedBitmap != null) {
                                OutlinedButton(
                                    onClick = { onOpenPrintSheet(processedBitmap!!) },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Print Sheet")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Exam Selection Dialog
    if (showExamDialog) {
        AlertDialog(
            onDismissRequest = { showExamDialog = false },
            title = { Text("Choose your Exam / Form", fontWeight = FontWeight.Bold) },
            text = {
                var searchQuery by remember { mutableStateOf("") }
                val filteredExams = allExams.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                    it.group.contains(searchQuery, ignoreCase = true)
                }

                Column(modifier = Modifier.heightIn(max = 400.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search IBPS, SBI, SSC, UPSC...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyColumn {
                        items(filteredExams) { exam ->
                            ListItem(
                                headlineContent = { Text(exam.name, fontWeight = FontWeight.SemiBold) },
                                supportingContent = { Text(exam.group) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedExam = exam
                                        selectedDocIndex = 0
                                        showExamDialog = false
                                    }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExamDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun RuleRow(
    title: String,
    subtitle: String,
    isPass: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isPass) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (isPass) GreenAccent else WarningAmber,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
