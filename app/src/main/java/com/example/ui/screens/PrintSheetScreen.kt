package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.engine.ImageProcessor
import com.example.engine.StorageHelper
import com.example.ui.theme.*
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrintSheetScreen(
    initialPhoto: Bitmap? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var sourcePhoto by remember { mutableStateOf(initialPhoto) }

    var sheetType by remember { mutableStateOf("4x6") } // "4x6" or "a4"
    var copyCount by remember { mutableIntStateOf(8) }

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
                sourcePhoto = bmp
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Generated print sheet bitmap
    val sheetBitmap by remember(sourcePhoto, sheetType, copyCount) {
        derivedStateOf {
            val photo = sourcePhoto
            if (photo != null) {
                if (sheetType == "4x6") {
                    ImageProcessor.generatePassportPrintSheet(
                        photo = photo,
                        pageSizeInchW = 4f,
                        pageSizeInchH = 6f,
                        dpi = 300,
                        copies = copyCount
                    )
                } else {
                    ImageProcessor.generatePassportPrintSheet(
                        photo = photo,
                        pageSizeInchW = 8.27f,
                        pageSizeInchH = 11.69f,
                        dpi = 300,
                        copies = copyCount
                    )
                }
            } else null
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Passport Photo Print Sheet", fontWeight = FontWeight.Bold) },
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
                .testTag("print_sheet_screen"),
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
                            text = "PRINT READY SHEET",
                            style = MaterialTheme.typography.labelSmall,
                            color = GreenPrimaryDark,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Print 8 copies on standard 4×6\" photo paper",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimaryDark
                        )
                        Text(
                            text = "Take this file to any local photo studio or print on your home inkjet printer. Includes cut guidelines around each passport photo.",
                            style = MaterialTheme.typography.bodySmall,
                            color = GreenPrimaryDark
                        )
                    }
                }
            }

            // Sheet Options
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Paper Size", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = sheetType == "4x6",
                                onClick = {
                                    sheetType = "4x6"
                                    copyCount = 8
                                },
                                label = { Text("4 × 6 inch (8 photos)") }
                            )
                            FilterChip(
                                selected = sheetType == "a4",
                                onClick = {
                                    sheetType = "a4"
                                    copyCount = 16
                                },
                                label = { Text("A4 sheet (16 photos)") }
                            )
                        }
                    }
                }
            }

            // Preview Container
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (sheetBitmap != null) {
                            Image(
                                bitmap = sheetBitmap!!.asImageBitmap(),
                                contentDescription = "Print Sheet Preview",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                            )
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Icon(
                                    Icons.Default.Print,
                                    contentDescription = null,
                                    modifier = Modifier.size(52.dp),
                                    tint = GreenPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "No photo selected",
                                    style = MaterialTheme.typography.bodyMedium,
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
                                    Text("Choose Passport Photo")
                                }
                            }
                        }
                    }
                }
            }

            // Save / Print Buttons
            if (sheetBitmap != null) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val stream = ByteArrayOutputStream()
                                sheetBitmap!!.compress(Bitmap.CompressFormat.JPEG, 95, stream)
                                StorageHelper.saveBytesToDownloads(
                                    context,
                                    stream.toByteArray(),
                                    "passport_print_sheet_${sheetType}.jpg"
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_print_sheet_button")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Save High-Res Sheet (300 DPI)",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Change Photo")
                        }
                    }
                }
            }
        }
    }
}
