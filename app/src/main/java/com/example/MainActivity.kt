package com.example

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.screens.*
import com.example.ui.theme.FormPakkaTheme
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FormPakkaTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    var currentScreen by remember { mutableStateOf("home") }
    var selectedExamId by remember { mutableStateOf<String?>(null) }
    var printSheetPhoto by remember { mutableStateOf<Bitmap?>(null) }

    // Android back button handling
    if (currentScreen != "home") {
        BackHandler {
            currentScreen = "home"
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentScreen == "home",
                    onClick = { currentScreen = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    modifier = Modifier.testTag("nav_home")
                )
                NavigationBarItem(
                    selected = currentScreen == "resize",
                    onClick = {
                        selectedExamId = null
                        currentScreen = "resize"
                    },
                    icon = { Icon(Icons.Default.Image, contentDescription = "Resizer") },
                    label = { Text("Resizer") },
                    modifier = Modifier.testTag("nav_resize")
                )
                NavigationBarItem(
                    selected = currentScreen == "docs",
                    onClick = { currentScreen = "docs" },
                    icon = { Icon(Icons.Default.Description, contentDescription = "PDF Docs") },
                    label = { Text("PDF Docs") },
                    modifier = Modifier.testTag("nav_docs")
                )
                NavigationBarItem(
                    selected = currentScreen == "draw",
                    onClick = { currentScreen = "draw" },
                    icon = { Icon(Icons.Default.Draw, contentDescription = "Draw") },
                    label = { Text("Draw Sign") },
                    modifier = Modifier.testTag("nav_draw")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                "home" -> HomeScreen(
                    onNavigateToTool = { toolId, examId ->
                        selectedExamId = examId
                        currentScreen = toolId
                    }
                )
                "resize" -> ResizeScreen(
                    initialExamId = selectedExamId,
                    onBack = { currentScreen = "home" },
                    onOpenPrintSheet = { bmp ->
                        printSheetPhoto = bmp
                        currentScreen = "print"
                    }
                )
                "form" -> FormScanScreen(
                    onBack = { currentScreen = "home" }
                )
                "docs" -> PdfDocsScreen(
                    onBack = { currentScreen = "home" }
                )
                "draw" -> DrawSignatureScreen(
                    onBack = { currentScreen = "home" }
                )
                "print" -> PrintSheetScreen(
                    initialPhoto = printSheetPhoto,
                    onBack = { currentScreen = "home" }
                )
                "batch" -> BatchResizeScreen(
                    onBack = { currentScreen = "home" }
                )
            }
        }
    }
}
