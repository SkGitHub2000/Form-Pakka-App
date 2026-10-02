# FormPakka — Android Application

FormPakka is an Android application built with Kotlin and Jetpack Compose designed to prepare exam-ready photos, signatures, thumb impressions, and PDF documents. It satisfies official government examination portal requirements (IBPS, SBI, SSC, UPSC, NEET, JEE, CUET, CTET, and custom presets).

## Features

- **Photo & Signature Resizer**:
  - Exact pixel dimensions, DPI, and KB limits (e.g. 20–50 KB, 10–20 KB).
  - Crop, 90° rotation, horizontal flip, and fine angle straightening (-15° to +15°).
  - **Ink Cleanup**: Whitens off-white scanned paper and crisps dark ink strokes for signatures and thumb impressions.
  - **Background Replacement**: Clean white or light blue solid background replacement.
  - **Name & Date on Photo**: Built-in support for NEET & UPSC candidate name and date stamp on the bottom white strip.
  - **Strict KB Range Optimization**: Binary search JPEG compression with safe padding markers so images never fail minimum or maximum size validation checks.
  - **Quality Check**: Analyzes lighting, contrast, resolution, and sharpness.

- **Auto-Crop from Form Scan**:
  - Extract photo, signature, or thumb impression from a single scanned form page or camera shot.

- **Documents to PDF under KB**:
  - Combine multiple certificate or marksheet photos into a single PDF.
  - Choose target max KB (< 100 KB, < 200 KB, < 300 KB, < 500 KB, < 1 MB).
  - Page tone modes: Clean B&W, Grayscale, or Color.

- **Draw your Signature**:
  - Smooth touchscreen signature pad.
  - Adjustable stroke thickness, pen color (Black / Blue), undo, and clear.
  - Direct export to the selected exam's exact signature dimensions and KB limits.

- **Passport Photo Print Sheet**:
  - Arrange 8 copies on standard 4×6" photo paper (or 16 copies on A4) with cut guidelines ready for printing.

- **Bulk Resize**:
  - Process multiple photos or signatures simultaneously.

- **100% On-Device & Private**:
  - All processing is conducted locally on device with zero cloud uploads.

## Tech Stack & Architecture

- **Language**: Kotlin
- **UI Toolkit**: Jetpack Compose with Material Design 3 (M3)
- **Target SDK**: Android 36 (Android 15)
- **Minimum SDK**: Android 26 (Android 8.0)
- **Graphics & Processing**: Android Graphics (`Bitmap`, `Canvas`, `Paint`, `ColorMatrix`, `PdfDocument`)
