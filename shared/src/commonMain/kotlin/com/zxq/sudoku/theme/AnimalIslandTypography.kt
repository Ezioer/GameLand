package com.zxq.sudoku.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import sudoku.shared.generated.resources.Res
import sudoku.shared.generated.resources.google_sans_rounded_regular
import sudoku.shared.generated.resources.nunito_black
import sudoku.shared.generated.resources.nunito_bold
import sudoku.shared.generated.resources.nunito_extra_bold
import sudoku.shared.generated.resources.nunito_medium
import sudoku.shared.generated.resources.nunito_regular
import sudoku.shared.generated.resources.nunito_semi_bold

@Composable
internal fun AnimalIslandTypography(): Typography {
    val nunito = FontFamily(
        Font(Res.font.google_sans_rounded_regular, FontWeight.Normal),
        Font(Res.font.google_sans_rounded_regular, FontWeight.Medium),
        Font(Res.font.google_sans_rounded_regular, FontWeight.SemiBold),
        Font(Res.font.google_sans_rounded_regular, FontWeight.Bold),
        Font(Res.font.google_sans_rounded_regular, FontWeight.ExtraBold),
        Font(Res.font.google_sans_rounded_regular, FontWeight.Black),
    )
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(
            fontFamily = nunito, fontWeight = FontWeight.Black,
            fontSize = 48.sp, lineHeight = 56.sp, letterSpacing = 2.sp,
        ),
        displayMedium = base.displayMedium.copy(fontFamily = nunito),
        displaySmall = base.displaySmall.copy(fontFamily = nunito),
        headlineLarge = base.headlineLarge.copy(fontFamily = nunito),
        headlineMedium = base.headlineMedium.copy(fontFamily = nunito),
        headlineSmall = base.headlineSmall.copy(fontFamily = nunito),
        titleLarge = base.titleLarge.copy(
            fontFamily = nunito, fontWeight = FontWeight.Bold,
            fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = 0.sp,
        ),
        titleMedium = base.titleMedium.copy(fontFamily = nunito),
        titleSmall = base.titleSmall.copy(fontFamily = nunito),
        bodyLarge = base.bodyLarge.copy(
            fontFamily = nunito, fontWeight = FontWeight.Medium,
            fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.01.sp,
        ),
        bodyMedium = base.bodyMedium.copy(
            fontFamily = nunito, fontWeight = FontWeight.Medium,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.01.sp,
        ),
        bodySmall = base.bodySmall.copy(fontFamily = nunito),
        labelLarge = base.labelLarge.copy(
            fontFamily = nunito, fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.02.sp,
        ),
        labelMedium = base.labelMedium.copy(fontFamily = nunito),
        labelSmall = base.labelSmall.copy(fontFamily = nunito),
    )
}
