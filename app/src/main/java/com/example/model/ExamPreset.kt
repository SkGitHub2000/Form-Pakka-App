package com.example.model

enum class DocKind {
    PHOTO,
    INK
}

data class DocumentSpec(
    val id: String,
    val kind: DocKind,
    val label: String,
    val targetWidthPx: Int,
    val targetHeightPx: Int,
    val minKB: Int,
    val maxKB: Int,
    val dpi: Int = 150,
    val fileName: String,
    val requiresNameDate: Boolean = false,
    val rules: List<String> = emptyList(),
    val displaySizeLabel: String = "${targetWidthPx} × ${targetHeightPx} px"
)

data class ExamPreset(
    val id: String,
    val group: String,
    val name: String,
    val officialSite: String? = null,
    val livePhoto: Boolean = false,
    val documents: List<DocumentSpec>
)

object ExamPresets {
    val presets: List<ExamPreset> = listOf(
        ExamPreset(
            id = "default",
            group = "General",
            name = "Standard form (132 × 170)",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo",
                    targetWidthPx = 132,
                    targetHeightPx = 170,
                    minKB = 5,
                    maxKB = 50,
                    dpi = 150,
                    fileName = "photo.jpg",
                    rules = listOf("Recent colour photo", "White or light background", "Face clearly visible")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 170,
                    targetHeightPx = 132,
                    minKB = 5,
                    maxKB = 20,
                    dpi = 150,
                    fileName = "signature.jpg",
                    rules = listOf("Sign on white paper", "Clear, not blurred")
                ),
                DocumentSpec(
                    id = "thumb",
                    kind = DocKind.INK,
                    label = "Thumb impression",
                    targetWidthPx = 170,
                    targetHeightPx = 132,
                    minKB = 5,
                    maxKB = 20,
                    dpi = 150,
                    fileName = "thumb.jpg",
                    rules = listOf("Left thumb, blue or black ink", "Clear, not blurred")
                )
            )
        ),
        ExamPreset(
            id = "passport",
            group = "General",
            name = "Passport-size photo (3.5 × 4.5 cm)",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Passport photo",
                    targetWidthPx = 413, // 3.5cm @ 300 DPI
                    targetHeightPx = 531, // 4.5cm @ 300 DPI
                    minKB = 20,
                    maxKB = 100,
                    dpi = 300,
                    fileName = "passport-photo.jpg",
                    displaySizeLabel = "3.5 × 4.5 cm (300 DPI)",
                    rules = listOf("White background", "Face clearly visible, 70-80% coverage")
                )
            )
        ),
        ExamPreset(
            id = "ibps",
            group = "Banking",
            name = "IBPS (PO / Clerk / RRB / SO)",
            officialSite = "https://www.ibps.in",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo",
                    targetWidthPx = 200,
                    targetHeightPx = 230,
                    minKB = 20,
                    maxKB = 50,
                    dpi = 200,
                    fileName = "photo.jpg",
                    rules = listOf("Recent colour photo", "White background", "200 × 230 px, 20–50 KB")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 140,
                    targetHeightPx = 60,
                    minKB = 10,
                    maxKB = 20,
                    dpi = 200,
                    fileName = "signature.jpg",
                    rules = listOf("Black ink on white paper", "Not in CAPITAL letters", "140 × 60 px, 10–20 KB")
                ),
                DocumentSpec(
                    id = "thumb",
                    kind = DocKind.INK,
                    label = "Thumb impression",
                    targetWidthPx = 240,
                    targetHeightPx = 240,
                    minKB = 20,
                    maxKB = 50,
                    dpi = 200,
                    fileName = "thumb.jpg",
                    rules = listOf("Left thumb impression", "Blue or black ink", "240 × 240 px, 20–50 KB")
                ),
                DocumentSpec(
                    id = "declaration",
                    kind = DocKind.INK,
                    label = "Handwritten declaration",
                    targetWidthPx = 800,
                    targetHeightPx = 400,
                    minKB = 50,
                    maxKB = 100,
                    dpi = 200,
                    fileName = "declaration.jpg",
                    rules = listOf("Handwritten in English by candidate", "Black ink on white paper", "50–100 KB")
                )
            )
        ),
        ExamPreset(
            id = "sbi",
            group = "Banking",
            name = "SBI (PO / Clerk / Specialist)",
            officialSite = "https://sbi.co.in/web/careers",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo",
                    targetWidthPx = 200,
                    targetHeightPx = 230,
                    minKB = 20,
                    maxKB = 50,
                    dpi = 200,
                    fileName = "photo.jpg",
                    rules = listOf("Recent colour photo", "White background", "200 × 230 px, 20–50 KB")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 140,
                    targetHeightPx = 60,
                    minKB = 10,
                    maxKB = 20,
                    dpi = 200,
                    fileName = "signature.jpg",
                    rules = listOf("Black ink on white paper", "Running handwriting (no caps)", "10–20 KB")
                ),
                DocumentSpec(
                    id = "thumb",
                    kind = DocKind.INK,
                    label = "Thumb impression",
                    targetWidthPx = 240,
                    targetHeightPx = 240,
                    minKB = 20,
                    maxKB = 50,
                    dpi = 200,
                    fileName = "thumb.jpg",
                    rules = listOf("Left thumb, blue/black ink", "240 × 240 px, 20–50 KB")
                ),
                DocumentSpec(
                    id = "declaration",
                    kind = DocKind.INK,
                    label = "Declaration",
                    targetWidthPx = 800,
                    targetHeightPx = 400,
                    minKB = 50,
                    maxKB = 100,
                    dpi = 200,
                    fileName = "declaration.jpg",
                    rules = listOf("Handwritten in English", "Black ink", "50–100 KB")
                )
            )
        ),
        ExamPreset(
            id = "ssc",
            group = "SSC",
            name = "SSC (CGL / CHSL / MTS / GD / CPO)",
            officialSite = "https://ssc.gov.in",
            livePhoto = true,
            documents = listOf(
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 276, // 3.5cm @ 200 DPI
                    targetHeightPx = 118, // 1.5cm @ 200 DPI
                    minKB = 10,
                    maxKB = 20,
                    dpi = 200,
                    fileName = "signature.jpg",
                    displaySizeLabel = "3.5 × 1.5 cm (10–20 KB)",
                    rules = listOf("Black or blue ink", "Running handwriting (not in capitals)", "10–20 KB")
                )
            )
        ),
        ExamPreset(
            id = "rrb",
            group = "Railways",
            name = "Railway RRB (NTPC / Group D / ALP / JE)",
            officialSite = "https://www.rrbapply.gov.in",
            livePhoto = true,
            documents = listOf(
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 280,
                    targetHeightPx = 120,
                    minKB = 30,
                    maxKB = 49,
                    dpi = 200,
                    fileName = "signature.jpg",
                    displaySizeLabel = "280 × 120 px (30–49 KB)",
                    rules = listOf("Running handwriting, not capitals", "Black ink on white paper", "30–49 KB")
                )
            )
        ),
        ExamPreset(
            id = "upsc",
            group = "UPSC",
            name = "UPSC (CSE / NDA / CDS / OTR)",
            officialSite = "https://upsconline.nic.in",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo",
                    targetWidthPx = 500,
                    targetHeightPx = 625,
                    minKB = 20,
                    maxKB = 300,
                    dpi = 200,
                    fileName = "photo.jpg",
                    displaySizeLabel = "500 × 625 px (20–300 KB)",
                    rules = listOf("White background", "Face clearly visible", "20–300 KB")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature (3 times)",
                    targetWidthPx = 600,
                    targetHeightPx = 600,
                    minKB = 20,
                    maxKB = 300,
                    dpi = 200,
                    fileName = "signature.jpg",
                    displaySizeLabel = "600 × 600 px (20–300 KB)",
                    rules = listOf("Sign 3 times, one below another", "Black ink on white paper", "20–300 KB")
                )
            )
        ),
        ExamPreset(
            id = "neet",
            group = "NTA (NEET, JEE, CUET)",
            name = "NEET UG (NTA)",
            officialSite = "https://neet.nta.nic.in",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo (with Name & Date)",
                    targetWidthPx = 276, // 3.5cm @ 200 DPI
                    targetHeightPx = 354, // 4.5cm @ 200 DPI
                    minKB = 10,
                    maxKB = 200,
                    dpi = 200,
                    fileName = "photo.jpg",
                    requiresNameDate = true,
                    displaySizeLabel = "3.5 × 4.5 cm (10–200 KB)",
                    rules = listOf("Name & date printed on bottom strip", "White background", "Face 80% coverage")
                ),
                DocumentSpec(
                    id = "postcard",
                    kind = DocKind.PHOTO,
                    label = "Postcard photo (4 × 6\")",
                    targetWidthPx = 600, // 4 inch @ 150 DPI
                    targetHeightPx = 900, // 6 inch @ 150 DPI
                    minKB = 10,
                    maxKB = 200,
                    dpi = 150,
                    fileName = "postcard-photo.jpg",
                    displaySizeLabel = "4 × 6 inch (10–200 KB)",
                    rules = listOf("White background", "Face covers ~80% of photo", "10–200 KB")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 276,
                    targetHeightPx = 118,
                    minKB = 10,
                    maxKB = 50,
                    dpi = 200,
                    fileName = "signature.jpg",
                    displaySizeLabel = "3.5 × 1.5 cm (10–50 KB)",
                    rules = listOf("Black ink on white paper", "Running handwriting", "10–50 KB")
                ),
                DocumentSpec(
                    id = "lefthand",
                    kind = DocKind.INK,
                    label = "Left hand fingers & thumb",
                    targetWidthPx = 800,
                    targetHeightPx = 600,
                    minKB = 10,
                    maxKB = 200,
                    dpi = 150,
                    fileName = "left-hand.jpg",
                    rules = listOf("All four fingers and thumb impressions", "10–200 KB")
                ),
                DocumentSpec(
                    id = "righthand",
                    kind = DocKind.INK,
                    label = "Right hand fingers & thumb",
                    targetWidthPx = 800,
                    targetHeightPx = 600,
                    minKB = 10,
                    maxKB = 200,
                    dpi = 150,
                    fileName = "right-hand.jpg",
                    rules = listOf("All four fingers and thumb impressions", "10–200 KB")
                )
            )
        ),
        ExamPreset(
            id = "jee",
            group = "NTA (NEET, JEE, CUET)",
            name = "JEE Main (NTA)",
            officialSite = "https://jeemain.nta.nic.in",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo",
                    targetWidthPx = 276,
                    targetHeightPx = 354,
                    minKB = 10,
                    maxKB = 200,
                    dpi = 200,
                    fileName = "photograph.jpg",
                    displaySizeLabel = "3.5 × 4.5 cm (10–200 KB)",
                    rules = listOf("White background", "Face 80% coverage", "10–200 KB")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 276,
                    targetHeightPx = 118,
                    minKB = 10,
                    maxKB = 100,
                    dpi = 200,
                    fileName = "signature.jpg",
                    displaySizeLabel = "3.5 × 1.5 cm (10–100 KB)",
                    rules = listOf("Sign on white paper with dark pen", "10–100 KB")
                )
            )
        ),
        ExamPreset(
            id = "cuet",
            group = "NTA (NEET, JEE, CUET)",
            name = "CUET UG (NTA)",
            officialSite = "https://cuet.nta.nic.in",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo",
                    targetWidthPx = 276,
                    targetHeightPx = 354,
                    minKB = 10,
                    maxKB = 200,
                    dpi = 200,
                    fileName = "photo.jpg",
                    rules = listOf("White background", "Face 80% coverage", "10–200 KB")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 276,
                    targetHeightPx = 118,
                    minKB = 10,
                    maxKB = 50,
                    dpi = 200,
                    fileName = "signature.jpg",
                    rules = listOf("Sign on white paper with dark pen", "10–50 KB")
                )
            )
        ),
        ExamPreset(
            id = "ctet",
            group = "Teaching",
            name = "CTET (CBSE)",
            officialSite = "https://ctet.nic.in",
            documents = listOf(
                DocumentSpec(
                    id = "photo",
                    kind = DocKind.PHOTO,
                    label = "Photo",
                    targetWidthPx = 210,
                    targetHeightPx = 270,
                    minKB = 10,
                    maxKB = 100,
                    dpi = 150,
                    fileName = "photo.jpg",
                    rules = listOf("Recent colour photo", "Face clearly visible", "10–100 KB")
                ),
                DocumentSpec(
                    id = "signature",
                    kind = DocKind.INK,
                    label = "Signature",
                    targetWidthPx = 210,
                    targetHeightPx = 90,
                    minKB = 3,
                    maxKB = 30,
                    dpi = 150,
                    fileName = "signature.jpg",
                    rules = listOf("Sign on white paper", "3–30 KB")
                )
            )
        )
    )
}
