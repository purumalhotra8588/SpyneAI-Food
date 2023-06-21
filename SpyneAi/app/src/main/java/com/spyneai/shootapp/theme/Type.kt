package com.spyneai.carinspectionocr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.spyneai.R

// Set of Material typography styles to start with
val PoppinsRegular = FontFamily(
    Font(R.font.poppins_regular),
)
val PoppinsBold = FontFamily(
    Font(R.font.poppins_bold),
)
val PoppinsSemiBold = FontFamily(
    Font(R.font.poppins_semibold),
)
val PoppinsLight = FontFamily(
    Font(R.font.poppins_light),
)
val PoppinsMedium = FontFamily(
    Font(R.font.poppins_medium),
)
val Typography = androidx.compose.material.Typography(
    h2 = TextStyle(
        fontFamily = PoppinsRegular,
    ),
    body1 = TextStyle(
        fontFamily = PoppinsRegular,
    ),
    body2 = TextStyle(
        fontFamily = PoppinsMedium,
    )
)