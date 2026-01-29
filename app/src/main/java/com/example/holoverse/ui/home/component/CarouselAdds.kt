package com.example.holoverse.ui.home.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.holoverse.ui.theme.HoloverseTheme
import com.example.holoverse.utils.GlassCard
import kotlinx.coroutines.delay

@Composable
@Preview
fun CarouselAdds() {
    val state: List<String> = listOf(" First Index  " , " Second Index  ", " Third Index ")
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { state.size }
    )
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var isAutoScrolling by remember {
        mutableStateOf(true)
    }



    LaunchedEffect(key1 = pagerState.currentPage) {
        if (isDragged) {
            isAutoScrolling = false
        } else {
            isAutoScrolling = true
            delay(5000)
            with(pagerState) {
                val target = if (currentPage < state.size - 1) currentPage + 1 else 0
                scrollToPage(target)
            }


        }
    }
    val itemSpacing = 8.dp
    val defaultPadding = 0.dp
    Row (modifier = Modifier.fillMaxWidth() ,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(start = defaultPadding),
            pageSpacing = itemSpacing
        ) { page ->
            if (isAutoScrolling) {
                AnimatedContent(targetState = page) { index ->
                    Row(modifier = Modifier.fillMaxWidth() ,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center) {
                        CarouselAddsCards(text = state[index])
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth() ,
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center) {
                    CarouselAddsCards(text = state[page])
                }
            }

        }

    }
}


@Composable
@Preview
fun CarouselAddsCards(text: String = " Enter\nAR Lab") {
    HoloverseTheme() {
        GlassCard(
            modifier = Modifier
                .height(160.dp)
                .width(320.dp),
            color = MaterialTheme.colorScheme.primary,
            onClick = {},
            enable = true
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = text, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }

}
