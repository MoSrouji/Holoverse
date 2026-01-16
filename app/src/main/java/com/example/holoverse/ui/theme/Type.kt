package com.example.holoverse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.holoverse.R


// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

val IbarraNovaFont = FontFamily(

    Font(R.font.ibarra_nova_regular),
    Font(R.font.ibarra_nova_bold, weight = FontWeight.Bold),
    Font(R.font.ibarra_nova_medium, weight = FontWeight.Medium),
    Font(R.font.ibarra_real_nova_semi_bold, weight = FontWeight.SemiBold),

    )
val rubik_glitch_pop = FontFamily(
    Font(R.font.rubik_glitch_pop, weight = FontWeight.Bold)
)

val rubik_glitch_pop16 = TextStyle(
    fontFamily = rubik_glitch_pop,
    fontSize = 100.sp,
    fontWeight = FontWeight.Bold,
)
val IbarraNovaBoldPlatinum25 =
    TextStyle(
        fontFamily = IbarraNovaFont,
        fontWeight = FontWeight.Bold,
        fontSize = 25.sp,
        //   color = ColorPlatinum
    )


val IbarraNovaSemiBoldGraniteGray =
    TextStyle(
        fontFamily = IbarraNovaFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        color = ColorGunmetal
    )


val IbarraNovaSemiBoldPlatinum16 =
    TextStyle(
        fontFamily = IbarraNovaFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        //color = secondaryLight
    )


val IbarraNovaNormalGray14 =
    TextStyle(
        fontFamily = IbarraNovaFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        //color = secondaryDark
    )

val IbarraNovaSemiBoldPlatinum17 =
    TextStyle(
        fontFamily = IbarraNovaFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        color = ColorPlatinum
    )

val IbarraNovaBoldPlatinum18 =
    TextStyle(
        fontFamily = IbarraNovaFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        //   color = ColorPlatinum
    )

val IbarraNovaSemiBoldColorVerdigris17 = TextStyle(
    fontFamily = IbarraNovaFont,
    fontWeight = FontWeight.SemiBold,
    fontSize = 17.sp,
    color = ColorVerdigris
)
val IbarraNovaNormalError13 = TextStyle(
    fontFamily = IbarraNovaFont,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    color = ColorRed
)