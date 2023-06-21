package com.spyneai.shootapp.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.spyneai.R
import com.spyneai.carinspectionocr.ui.theme.BlackFifty
import com.spyneai.carinspectionocr.ui.theme.PoppinsMedium
import com.spyneai.carinspectionocr.ui.theme.PoppinsRegular
import com.spyneai.carinspectionocr.ui.theme.WhiteEight
import com.spyneai.carinspectionocr.ui.theme.WhiteEighty
import com.spyneai.carinspectionocr.ui.theme.WhiteForty
import com.spyneai.carinspectionocr.ui.theme.WhiteTextColor
import com.spyneai.carinspectionocr.ui.theme.WhiteTwenty
import com.spyneai.shootapp.data.ShootViewModelApp
import com.spyneai.shootapp.data.model.ImproveShootResponse

@Composable
fun ImproveShootScreen(viewModel: ShootViewModelApp) {

    val improveShootList by viewModel.improveShootList.observeAsState()

    Box(modifier = Modifier.background(BlackFifty)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Card(
                modifier = Modifier
                    .background(BlackFifty)
                    .fillMaxWidth(),
                elevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BlackFifty),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 10.dp),
                        text = "How to improve your shots",
                        color = WhiteTextColor,
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp,
                        fontFamily = com.spyneai.carinspectionocr.ui.theme.PoppinsMedium,
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    Icon(
                        modifier = Modifier
                            .clickable {
                                viewModel.removeImproveShootFragment.value = true
                            }
                            .padding(top = 15.dp, bottom = 15.dp)
                            .size(16.dp),
                        painter = painterResource(id = R.drawable.ic_close),
                        contentDescription = "",
                        tint = WhiteForty
                    )

                    Spacer(modifier = Modifier.width(15.dp))

                }
            }

            if (improveShootList.isNullOrEmpty()) {

                RowItemShimmer()
            } else {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(BlackFifty)
                        .verticalScroll(rememberScrollState())
                ) {

                    improveShootList?.forEach { it ->

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(BlackFifty)
                        ) {

                            Spacer(modifier = Modifier.height(22.dp))

                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = it.heading ?: "",
                                color = WhiteTextColor,
                                style = MaterialTheme.typography.body2,
                                textAlign = TextAlign.Center,
                                fontSize = 18.sp,
                                fontFamily = PoppinsMedium,
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                modifier = Modifier.padding(start = 12.dp),
                                text = it.description ?: "",
                                color = WhiteForty,
                                style = MaterialTheme.typography.body2,
                                fontSize = 12.sp,
                                fontFamily = PoppinsRegular,
                            )

                            Spacer(modifier = Modifier.height(16.dp))


                            //lazy row

                            LazyRow(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(12.dp),
                            ) {
                                itemsIndexed(it.regulations ?: listOf()) { index, rowItem ->
                                    RowItem(index, rowItem)
                                }
                            }

                            Spacer(modifier = Modifier.height(22.dp))

                            Divider(
                                modifier = Modifier
                                    .padding(start = 12.dp, end = 12.dp)
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                color = WhiteTwenty,
                                thickness = 1.dp
                            )

                        }
                    }

                }
            }

        }
    }

}

@Composable
fun RowItem(index: Int, rowItem: ImproveShootResponse.Regulations) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.size(width = 125.dp, height = 126.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = 2.dp
        ) {

            Image(
                modifier = Modifier
                    .fillMaxSize(),
                painter = rememberAsyncImagePainter(rowItem.image_url ?: ""),
                contentDescription = ""
            )

            Column(modifier = Modifier.background(Color.Transparent)) {
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.background(Color.Transparent)) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        modifier = Modifier
                            .padding(6.dp)
                            .size(20.dp),
                        painter = if (rowItem.method == "correct") painterResource(id = R.drawable.ic_good_image_icon) else painterResource(
                            id = R.drawable.ic_bad_image_icon
                        ),
                        contentDescription = "",
                        tint = if (rowItem.method == "correct") Color.Green else Color.Red
                    )
                }
            }


        }
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .width(124.dp)
                .background(WhiteEight, shape = RoundedCornerShape(4.dp)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                modifier = Modifier.padding(
                    top = 10.dp,
                    bottom = 10.dp
                ),
                text = rowItem.description ?: "",
                color = WhiteEighty,
                style = MaterialTheme.typography.body2,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                fontFamily = PoppinsMedium,
            )
        }

    }
}

@Composable
fun RowItemShimmer() {

    val gradient = listOf(
        Color.LightGray.copy(alpha = 0.9f), //darker grey (90% opacity)
        Color.LightGray.copy(alpha = 0.3f), //lighter grey (30% opacity)
        Color.LightGray.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition() // animate infinite times

    val translateAnimation = transition.animateFloat( //animate the transition
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000, // duration for the animation
                easing = FastOutLinearInEasing
            )
        )
    )
    val brush = Brush.linearGradient(
        colors = gradient,
        start = Offset(200f, 200f),
        end = Offset(
            x = translateAnimation.value,
            y = translateAnimation.value
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(BlackFifty)
    ) {

        repeat(3) {
            Spacer(
                modifier = Modifier
                    .padding(start = 12.dp, top = 22.dp)
                    .background(brush)
                    .size(height = 22.dp, width = 60.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Spacer(
                modifier = Modifier
                    .padding(start = 12.dp, top = 4.dp)
                    .background(brush)
                    .size(height = 32.dp, width = 120.dp)
            )

            Spacer(modifier = Modifier.height(22.dp))

            LazyRow(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(count = 3) {
                    Box() {
                        Card(
                            modifier = Modifier
                                .size(height = 125.dp, width = 125.dp),
                            shape = RoundedCornerShape(8.dp),
                            elevation = 2.dp,
                        ) {
                            Spacer(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(brush)
                            )
                        }
                    }
                }
            }
        }
    }
}

