package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Requested Custom Palette: #579FB6, #77B7D0, #B3E0F4, #FCFDFE, #EED3CE, #E2BAB1
val BrandOceanDeep = Color(0xFF579FB6)    // #579FB6 - Deep Ocean / Primary Brand
val BrandSkyTeal = Color(0xFF77B7D0)      // #77B7D0 - Sky Cyan / Secondary Accent
val BrandSoftSky = Color(0xFFB3E0F4)      // #B3E0F4 - Light Sky Container / Pill Background
val BrandCanvasWhite = Color(0xFFFCFDFE)  // #FCFDFE - Palette White Reference
val BrandWarmRose = Color(0xFFEED3CE)     // #EED3CE - Warm Peach / Soft Rose Container
val BrandTerracotta = Color(0xFFE2BAB1)   // #E2BAB1 - Warm Dusty Terracotta Accent / Button

// Balanced "Decreased White" Mid-Tone Surfaces (Neither harsh light nor dark)
val BrandMutedCanvas = Color(0xFFD9E9F0)  // Soothing, balanced mid-light slate-sky background
val BrandSurfaceCard = Color(0xFFE8F2F6)  // Gentle tinted card surface (non-white)
val BrandSurfaceVariant = Color(0xFFCFE3EC) // Richer mid-tone container
val BrandInputBackground = Color(0xFFD2E6EF) // Soft, non-glare input surface
val BrandTextPrimary = Color(0xFF14303B)  // Deep readable slate ocean
val BrandTextSecondary = Color(0xFF385E6E) // Medium muted teal-slate

// System Color Mapping utilizing the exact requested palette & balanced mid-tone
val AcademicPrimary = BrandOceanDeep
val AcademicPrimaryDark = BrandSkyTeal
val AcademicPrimaryContainer = BrandSoftSky
val AcademicPrimaryContainerDark = Color(0xFF264C59)

val AcademicSecondary = BrandSkyTeal
val AcademicSecondaryDark = BrandSoftSky
val AcademicSecondaryContainer = BrandSoftSky
val AcademicSecondaryContainerDark = Color(0xFF2E5363)

val AcademicTertiary = BrandTerracotta
val AcademicTertiaryDark = BrandWarmRose
val AcademicTertiaryContainer = BrandWarmRose
val AcademicTertiaryContainerDark = Color(0xFF5A413D)

val AcademicBackgroundLight = BrandMutedCanvas
val AcademicSurfaceLight = BrandSurfaceCard
val AcademicSurfaceVariantLight = BrandSurfaceVariant

val AcademicBackgroundDark = Color(0xFF131D24)
val AcademicSurfaceDark = Color(0xFF1B2830)
val AcademicSurfaceVariantDark = Color(0xFF253742)

// Button and Badge Action colors - every button strictly uses the 6-color palette
val ButtonPrimary = BrandOceanDeep
val ButtonSecondary = BrandTerracotta
val ButtonTertiary = BrandSkyTeal
val ButtonContainerSoft = BrandSoftSky
val ButtonWarmContainer = BrandWarmRose

// Status colors
val StatusPresent = Color(0xFF10B981)
val StatusAbsent = Color(0xFFEF4444)
val StatusLate = Color(0xFFF59E0B)
val StatusOverdue = Color(0xFFE11D48)
val StatusPaid = Color(0xFF059669)
val StatusPending = Color(0xFFD97706)

// Code highlighting colors
val CodeBackground = Color(0xFF111E26)
val CodeKeyword = Color(0xFF93C5FD)
val CodeString = Color(0xFF6EE7B7)
val CodeComment = Color(0xFF94A3B8)
val CodeFunction = Color(0xFF77B7D0)
val CodeNumber = Color(0xFFFDE68A)
