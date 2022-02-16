package ru.ikarenkov.podlodca.animationtask

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintSet
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.ExperimentalMotionApi
import androidx.constraintlayout.compose.MotionLayout
import kotlinx.coroutines.launch
import ru.ikarenkov.podlodca.animationtask.ui.theme.AnimationTaskTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialApi::class, ExperimentalMotionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AnimationTaskTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    Screen()
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@ExperimentalMotionApi
@Composable
fun Screen() {
    // TODO 23 MotionLayout

    val screenHeight = LocalConfiguration.current.screenHeightDp.toFloat()

    val swipingState = rememberSwipeableState(initialValue = SwipingStates.COLLAPSED)

    val animateMotionLayoutProgress by animateFloatAsState(
        targetValue = if (swipingState.progress.to == SwipingStates.COLLAPSED) {
            swipingState.progress.fraction
        } else {
            1f - swipingState.progress.fraction
        },
        animationSpec = spring()
    )

    MotionLayout(
        start = expandedConstraintSet(),
        end = collapsedConstraintSet(),
        progress = animateMotionLayoutProgress,
        modifier = Modifier
            .fillMaxWidth()
            .height(screenHeight.dp)
            .swipeable(
                state = swipingState,
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
                orientation = Orientation.Vertical,
                anchors = mapOf(
                    0f to SwipingStates.COLLAPSED,
                    screenHeight to SwipingStates.EXPANDED,
                )
            )
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .layoutId(ScreenIds.TOP)
                .fillMaxWidth()
                .height(400.dp)
                .background(MaterialTheme.colors.primary)
                .alpha(alpha = 1f - animateMotionLayoutProgress)
        ) {

            val xOffsetAnimatable = remember { Animatable(0.dp, Dp.VectorConverter) }
            val rotationAnimatable = remember { Animatable(0f, Float.VectorConverter) }
            LaunchedEffect(swipingState.currentValue) {
                if (swipingState.currentValue == SwipingStates.EXPANDED) {
                    launch { xOffsetAnimatable.animateTo(-minWidth, tween(3000)) }
                    launch { rotationAnimatable.animateTo(360f, tween(2000)) }
                } else {
                    xOffsetAnimatable.snapTo(0.dp)
                    rotationAnimatable.snapTo(0f)
                }
            }
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .rotate(rotationAnimatable.value),
                text = "🕶",
                style = MaterialTheme.typography.h1,
            )
            Text(
                text = "🚕",
                style = MaterialTheme.typography.h2,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 16.dp, start = 16.dp)
                    .scale(animateMotionLayoutProgress.reversed)
                    .scale(scaleX = -1f, scaleY = 1f)
                    .offset(x = xOffsetAnimatable.value)
            )
        }
        Box(
            Modifier
                .wrapContentHeight()
                .layoutId(ScreenIds.CONTENT),
        ) {
            val transition = rememberInfiniteTransition()
            val offsetPregress by transition.animateFloat(
                initialValue = -1f,
                targetValue = 1f,
                animationSpec = InfiniteRepeatableSpec(
                    tween(durationMillis = 1500),
                    repeatMode = RepeatMode.Reverse
                ),
            )
            Text(
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(animateMotionLayoutProgress.reversed)
                    .offset(x = 16.dp * offsetPregress, y = 16.dp * offsetPregress),
                text = "💸",
                style = MaterialTheme.typography.h1,
            )
            Column(
                Modifier.alpha(animateMotionLayoutProgress),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Поехали?!",
                    style = MaterialTheme.typography.h2,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "☝️",
                    modifier = Modifier.rotate(
                        (animateMotionLayoutProgress - 1) * 100
                    ),
                    style = MaterialTheme.typography.h2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

val Float.reversed get() = (this * -1) + 1

private fun expandedConstraintSet() = ConstraintSet {
    val topBar = createRefFor(ScreenIds.TOP)
    val content = createRefFor(ScreenIds.CONTENT)

    constrain(topBar) {
        width = Dimension.fillToConstraints
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        top.linkTo(parent.top)
    }

    constrain(content) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        top.linkTo(topBar.bottom, 16.dp)
        bottom.linkTo(parent.bottom)
    }
}

private fun collapsedConstraintSet() = ConstraintSet {
    val topBar = createRefFor(ScreenIds.TOP)
    val content = createRefFor(ScreenIds.CONTENT)

    constrain(topBar) {
        width = Dimension.fillToConstraints
        height = Dimension.value(56.dp)
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        top.linkTo(parent.top)
    }

    constrain(content) {
        start.linkTo(parent.start)
        end.linkTo(parent.end)
        top.linkTo(topBar.bottom, 16.dp)
        bottom.linkTo(parent.bottom)
    }
}

enum class SwipingStates {
    EXPANDED,
    COLLAPSED
}

enum class ScreenIds {
    CONTENT,
    TOP,
    TAXI
}