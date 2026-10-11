package com.knigdelioglu.arc.compose.foundation

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Arc's supported semantic accent palettes. */
enum class ArcAccent { Neutral, Violet, Blue, Green, Amber, Orange, Coral, Rose }

/**
 * Arc Library color tokens adapted for Jetpack Compose.
 * Preserves the light and dark neutral ramps and brand gradient definitions.
 */
@Immutable
data class ArcColors(
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceRaised: Color,
    val surfaceMuted: Color,
    val foreground: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val borderSubtle: Color,
    val borderStrong: Color,
    val accent: Color,
    val accentStrong: Color,
    val accentSubtle: Color,
    val accentForeground: Color,
    val controlOn: Color,
    val controlGlyph: Color,
    val controlTrack: Color,
    val controlThumb: Color,
    val success: Color,
    val warning: Color,
    val danger: Color,
    val info: Color,
    val gradientFrom: Color,
    val gradientTo: Color,
    val gradientForeground: Color,
    val accentVariant: ArcAccent = ArcAccent.Neutral
) {
    val brandGradient: Brush
        get() = Brush.linearGradient(listOf(gradientFrom, gradientTo))

    val brandGradientSoft: Brush
        get() = Brush.linearGradient(
            listOf(
                gradientFrom.copy(alpha = if (isDark) 0.12f else 0.06f),
                gradientTo.copy(alpha = if (isDark) 0.12f else 0.06f)
            )
        )

    val brandGradientInk: Brush
        get() = Brush.linearGradient(
            listOf(
                if (isDark) Color(0xFFA78BFA) else Color(0xFF6D28D9),
                if (isDark) Color(0xFFFB923C) else Color(0xFFC2410C)
            )
        )

    /** Color-blind-aware series colors shared by all chart composables. */
    val dataVisualizationPalette: List<Color>
        get() = if (isDark) {
            listOf(Color(0xFF55ADFF), Color(0xFF9C37BE), Color(0xFFE66E00), Color(0xFF00A861))
        } else {
            listOf(Color(0xFF1F7DCF), Color(0xFF7128A5), Color(0xFFE58F00), Color(0xFF009E84))
        }
}

fun ArcColors.withAccent(accent: ArcAccent): ArcColors = when (accent) {
    ArcAccent.Neutral -> copy(accentVariant = accent)
    ArcAccent.Violet -> copy(
        accent = if (isDark) Color(0xFFA78BFF) else Color(0xFF7747FF),
        accentStrong = if (isDark) Color(0xFFC7B7FF) else Color(0xFF5528CE),
        accentSubtle = if (isDark) Color(0x387747FF) else Color(0x217747FF),
        accentForeground = if (isDark) Color(0xFF18181B) else Color.White,
        controlOn = if (isDark) Color(0xFF8A66FF) else Color(0xFF7747FF),
        controlGlyph = Color.White,
        accentVariant = accent
    )
    ArcAccent.Blue -> copy(
        accent = if (isDark) Color(0xFF6BA3FF) else Color(0xFF0562EF),
        accentStrong = if (isDark) Color(0xFFA4C7FF) else Color(0xFF074DB7),
        accentSubtle = if (isDark) Color(0x380562EF) else Color(0x1F0562EF),
        accentForeground = if (isDark) Color(0xFF18181B) else Color.White,
        controlOn = if (isDark) Color(0xFF3B82F6) else Color(0xFF0562EF),
        controlGlyph = Color.White,
        accentVariant = accent
    )
    ArcAccent.Green -> copy(
        accent = if (isDark) Color(0xFF53DCA6) else Color(0xFF0DB879),
        accentStrong = if (isDark) Color(0xFF93EFD0) else Color(0xFF087C54),
        accentSubtle = if (isDark) Color(0x330DB879) else Color(0x210DB879),
        accentForeground = if (isDark) Color(0xFF18181B) else Color(0xFF102016),
        controlOn = if (isDark) Color(0xFF1FAE78) else Color(0xFF10A870),
        controlGlyph = Color.White,
        accentVariant = accent
    )
    ArcAccent.Amber -> copy(
        accent = if (isDark) Color(0xFFFFCB62) else Color(0xFFF3AD20),
        accentStrong = if (isDark) Color(0xFFFFE0A1) else Color(0xFF9C6300),
        accentSubtle = if (isDark) Color(0x33F3AD20) else Color(0x29F3AD20),
        accentForeground = Color(0xFF20190D),
        controlOn = if (isDark) Color(0xFFE8A52A) else Color(0xFFEDA11A),
        controlGlyph = Color(0xFF2A1C03),
        accentVariant = accent
    )
    ArcAccent.Orange -> copy(
        accent = if (isDark) Color(0xFFFFAD5E) else Color(0xFFF48120),
        accentStrong = if (isDark) Color(0xFFFFD0A0) else Color(0xFFAA4700),
        accentSubtle = if (isDark) Color(0x33F48120) else Color(0x21F48120),
        accentForeground = Color(0xFF17130F),
        controlOn = if (isDark) Color(0xFFEF873A) else Color(0xFFF07818),
        controlGlyph = Color.White,
        accentVariant = accent
    )
    ArcAccent.Coral -> copy(
        accent = if (isDark) Color(0xFFFF9386) else Color(0xFFF15F55),
        accentStrong = if (isDark) Color(0xFFFFC1BA) else Color(0xFFB92C27),
        accentSubtle = if (isDark) Color(0x33F15F55) else Color(0x24F15F55),
        accentForeground = if (isDark) Color(0xFF18181B) else Color(0xFF21110F),
        controlOn = if (isDark) Color(0xFFEC6A60) else Color(0xFFEC564B),
        controlGlyph = Color.White,
        accentVariant = accent
    )
    ArcAccent.Rose -> copy(
        accent = if (isDark) Color(0xFFFF8BC5) else Color(0xFFED4E9D),
        accentStrong = if (isDark) Color(0xFFFFC0DF) else Color(0xFFAE2570),
        accentSubtle = if (isDark) Color(0x33ED4E9D) else Color(0x24ED4E9D),
        accentForeground = if (isDark) Color(0xFF18181B) else Color(0xFF250F1B),
        controlOn = if (isDark) Color(0xFFE457A0) else Color(0xFFE64494),
        controlGlyph = Color.White,
        accentVariant = accent
    )
}

val ArcLightColors = ArcColors(
    isDark = false,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    surfaceRaised = Color(0xFFFAFAFA),
    surfaceMuted = Color(0xFFF5F5F7),
    foreground = Color(0xFF141414),
    textSecondary = Color(0xFF555555),
    textMuted = Color(0xFF7D7D82),
    border = Color(0xFFE5E5EA),
    borderSubtle = Color(0xFFF0F0F3),
    borderStrong = Color(0xFFC7C7CC),
    accent = Color(0xFF2C2C2E),
    accentStrong = Color(0xFF1C1C1E),
    accentSubtle = Color(0x142C2C2E),
    accentForeground = Color(0xFFFFFFFF),
    controlOn = Color(0xFF1C1C1E),
    controlGlyph = Color(0xFFFFFFFF),
    controlTrack = Color(0xFFE5E5EA),
    controlThumb = Color(0xFFFFFFFF),
    success = Color(0xFF10B981),
    warning = Color(0xFFF59E0B),
    danger = Color(0xFFEF4444),
    info = Color(0xFF3B82F6),
    gradientFrom = Color(0xFFE9D5FF),
    gradientTo = Color(0xFFFFEDD5),
    gradientForeground = Color(0xFF2E1065)
)

val ArcDarkColors = ArcColors(
    isDark = true,
    background = Color(0xFF121214),
    surface = Color(0xFF1A1A1E),
    surfaceRaised = Color(0xFF24242A),
    surfaceMuted = Color(0xFF2C2C34),
    foreground = Color(0xFFF3F3F6),
    textSecondary = Color(0xFFA1A1AA),
    textMuted = Color(0xFF71717A),
    border = Color(0xFF2E2E36),
    borderSubtle = Color(0xFF23232A),
    borderStrong = Color(0xFF444450),
    accent = Color(0xFFE4E4E7),
    accentStrong = Color(0xFFFFFFFF),
    accentSubtle = Color(0x22E4E4E7),
    accentForeground = Color(0xFF18181B),
    controlOn = Color(0xFFF4F4F5),
    controlGlyph = Color(0xFF18181B),
    controlTrack = Color(0xFF3F3F46),
    controlThumb = Color(0xFFFFFFFF),
    success = Color(0xFF34D399),
    warning = Color(0xFFFBBF24),
    danger = Color(0xFFF87171),
    info = Color(0xFF60A5FA),
    gradientFrom = Color(0xFF581C87),
    gradientTo = Color(0xFF7C2D12),
    gradientForeground = Color(0xFFF3E8FF)
)
