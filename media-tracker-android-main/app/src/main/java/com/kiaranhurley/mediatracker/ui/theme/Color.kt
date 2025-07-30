package com.kiaranhurley.mediatracker.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand Colors - Softer, more harmonious tones
val BrandOrange = Color(0xFFE67E22)          // Warmer, softer orange
val BrandOrangeVariant = Color(0xFFD35400)   // Deeper orange variant
val BrandBlue = Color(0xFF3498DB)            // Softer, more muted blue
val BrandBlueVariant = Color(0xFF2980B9)     // Deeper blue variant

// Main Background Colors - Better harmony and transitions
val PrimaryGray = Color(0xFF2C3E50)          // Deeper, warmer gray
val SecondaryGray = Color(0xFF34495E)        // Slightly lighter variant
val TertiaryGray = Color(0xFF4A6741)         // Greenish-gray for warmth
val SurfaceGray = Color(0xFF445566)          // Card backgrounds
val DividerGray = Color(0xFF5D6D7E)          // Softer borders

// Text Colors - Better contrast and readability
val TextPrimary = Color(0xFFF8F9FA)          // Softer white
val TextSecondary = Color(0xFFE9ECEF)        // Light gray with warmth
val TextTertiary = Color(0xFFADB5BD)         // Medium gray
val TextDisabled = Color(0xFF6C757D)         // Muted gray
val TextOnSurface = Color(0xFFDEE2E6)        // Text on elevated surfaces

// Heading Colors - Optimized for readability
val HeadingPrimary = Color(0xFFFFFFFF)       // Pure white for main headings
val HeadingSecondary = Color(0xFFF1F3F5)     // Very light gray for secondary headings
val HeadingAccent = BrandOrange              // Orange for accent headings

// TopAppBar specific colors for better contrast
val AppBarBackground = Color(0xFF1C2833)     // Darker background for better contrast
val AppBarText = Color(0xFFFFFFFF)           // Pure white text on app bars
val AppBarSubtext = Color(0xFFE8F0F3)        // Light text for subtitles

// Card and Surface text colors - FIXED FOR LIGHT BACKGROUNDS
val CardBackground = Color(0xFF2C3E50)        // Dark background for cards
val CardTitleText = Color(0xFFE8F0F3)        // Light text for dark cards  
val CardBodyText = Color(0xFFCCD6E0)         // Light gray for card content
val CardSubtitleText = Color(0xFFADB5BD)     // Medium gray for subtitles on cards
val SurfaceText = Color(0xFFE8F0F3)          // Light text for dark surfaces

// Input field colors
val InputTextColor = Color(0xFF9CA3AF)       // Much lighter grey text for search inputs  
val InputPlaceholderColor = Color(0xFF85929E) // Gray placeholder text

// Interactive Colors - Softer, more pleasant interactions
val ButtonPrimary = BrandOrange
val ButtonPrimaryHover = Color(0xFFE8926F)   // Lighter orange hover
val ButtonSecondary = Color(0xFF5A6B7C)      // Muted secondary
val ButtonSecondaryHover = Color(0xFF6B7C8D) // Lighter secondary hover
val LinkColor = BrandBlue
val LinkHover = Color(0xFF5DADE2)            // Lighter blue hover

// Status Colors - Gentler, less aggressive
val StatusSuccess = Color(0xFF27AE60)        // Forest green
val StatusSuccessLight = Color(0xFF58D68D)   // Light green
val StatusWarning = Color(0xFFF39C12)        // Warm yellow-orange
val StatusWarningLight = Color(0xFFF7DC6F)   // Light yellow
val StatusError = Color(0xFFE74C3C)          // Softer red
val StatusErrorLight = Color(0xFFF1948A)     // Light red
val StatusInfo = BrandBlue
val StatusInfoLight = Color(0xFF85C1E9)      // Light blue

// Surface Colors - Smoother transitions
val SurfaceElevated = Color(0xFF4A5A6B)      // Elevated cards
val SurfaceHighlight = Color(0xFF5D6D7E)     // Highlighted elements
val SurfaceSelected = Color(0xFF708090)      // Selected states
val SurfaceHover = Color(0xFF566B78)         // Hover states

// Accent Colors - More muted and pleasant
val AccentWarm = Color(0xFFD68910)           // Warm accent
val AccentCool = Color(0xFF5499C7)           // Cool accent
val AccentNeutral = Color(0xFF85929E)        // Neutral accent

// Theme Colors for Material Design 3
val DarkBackground = PrimaryGray
val DarkSurface = SurfaceGray
val DarkSurfaceVariant = SecondaryGray
val DarkOnBackground = TextSecondary
val DarkOnSurface = TextPrimary
val DarkOnSurfaceVariant = TextTertiary

// Light theme colors (for contrast)
val LightBackground = Color(0xFFF8F9FA)
val LightSurface = Color(0xFFFFFFFF)
val LightOnBackground = Color(0xFF2C3E50)
val LightOnSurface = Color(0xFF1B2631)

// Legacy color mappings for compatibility
val AccentOrange = BrandOrange
val AccentBlue = BrandBlue
val ErrorRed = StatusError
val SuccessGreen = StatusSuccess
val WarningYellow = StatusWarning