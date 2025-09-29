package com.example.myapplication.ui.screen.signup

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import com.example.myapplication.model.TopLevelDestination
import com.example.myapplication.util.ScrollbarConfig
import com.example.myapplication.util.scrollWithScrollbar

// 기본 패딩 값 설정
private val paddingValues = PaddingValues(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 0.dp)

// 애니메이션 지속 시간 설정
private const val animationDurationMillis = 300

// 활성화된 원의 배경색
private val activeBackgroundColor = Color(0xFF3AB47D)

// 비활성화된 원의 배경색
private val inactiveBackgroundColor = activeBackgroundColor.copy(alpha = 0.4f)

// 활성화된 글자 색상
private val activeTextColor = Color(0xEBFFFFFF)

// 비활성화된 글자 색상
private val inactiveTextColor = activeTextColor.copy(alpha = 0.4f)

// 원의 반지름
private val circleRadius = 36.dp

// 원과 원 사이의 간격
private val circleSpacing = 48.dp

// 통로의 두께
private val strokeWidth = 16.dp

// 원과 단계명 사이의 간격
private val stepLabelSpacing = 28.dp

// 단계 번호의 글자 크기
private val stepNumberTextSize = 32.sp

// 단계명의 글자 크기
private val stepLabelTextSize = 28.sp

@Composable
fun SignUpProcessView(
    localizedContext: Context,
    scrollState: ScrollState,
    destinations: List<TopLevelDestination>,
    prevStep: Int,
    currentStep: Int,
    onCurrentStepChange: (Int) -> Unit
) {
    val density = LocalDensity.current
    val circleRadiusPx = with(density) { circleRadius.toPx() }
    val circleSpacingPx = with(density) { circleSpacing.toPx() }
    val stepLabelSpacingPx = with(density) { stepLabelSpacing.toPx() }

    // 텍스트 측정기
    val textMeasurer = rememberTextMeasurer()

    // 전체 높이
    val totalHeight =
        (circleRadius * 2) * destinations.size + (circleSpacing * (destinations.lastIndex))

    // 사용자에게 보여지는 화면 높이
    var viewportHeight by remember { mutableIntStateOf(0) }

    // 원의 중심점 및 클릭 영역 초기화
    var circleCenters by remember { mutableStateOf<List<Offset>>(emptyList()) }
    var stepRects by remember { mutableStateOf<List<Rect>>(emptyList()) }

    // 애니메이션 Y 위치 계산
    val animatedY by animateFloatAsState(
        targetValue = (circleCenters.getOrNull(currentStep)?.y ?: 0f)
                + circleRadiusPx + circleSpacingPx / 2,
        animationSpec = tween(durationMillis = animationDurationMillis),
        label = "Current Step Y Position"
    )

    // 현재 활성화된 단계
    var activeStep by remember { mutableIntStateOf(0) }

    // 선택된 스텝으로 스크롤
    LaunchedEffect(currentStep, viewportHeight) {
        stepRects.getOrNull(currentStep)?.let { rect ->
            scrollToCurrentStep(rect, scrollState, viewportHeight)
        }
    }

    // Y 좌표에 따라 활성화된 단계 업데이트
    LaunchedEffect(animatedY) {
        updateActiveStep(
            currentY = animatedY,
            prevStep = prevStep,
            currentStep = currentStep,
            circleCenters = circleCenters,
            onActiveStepChange = { newStep ->
                if (newStep != activeStep) {
                    activeStep = newStep
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .padding(paddingValues)
            .onSizeChanged { size -> viewportHeight = size.height }
            .scrollWithScrollbar(
                state = scrollState,
                direction = Orientation.Vertical,
                scrollbarConfig = ScrollbarConfig()
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(totalHeight)
                .drawWithCache {
                    // 초기화 시 클릭 영역과 원의 중심점 설정
                    if (circleCenters.isEmpty()) {
                        initStepBounds(
                            destinations = destinations,
                            onCircleCentersUpdate = { newCenters ->
                                circleCenters = newCenters // 원의 중심점 업데이트
                            },
                            onStepRectsUpdate = { newRects ->
                                stepRects = newRects // 클릭 영역 업데이트
                            },
                            size = this.size,
                            circleRadiusPx = circleRadiusPx,
                            circleSpacingPx = circleSpacingPx
                        )
                    }

                    // 단계 레이아웃 그리기
                    val stepPath = generateStepPath(
                        centers = circleCenters,
                        circleRadiusPx = circleRadiusPx,
                        strokeWidthPx = strokeWidth.toPx()
                    )

                    onDrawBehind {
                        // 비활성화된 배경 그리기
                        drawPath(
                            path = stepPath,
                            color = inactiveBackgroundColor
                        )

                        // 활성화된 배경 그리기
                        clipPath(path = stepPath) {
                            drawRect(
                                color = activeBackgroundColor,
                                size = Size(size.width, animatedY)
                            )
                        }

                        // 단계 번호 및 명칭 그리기
                        circleCenters.fastForEachIndexed { i, offset ->
                            drawStepNumber(
                                index = i,
                                activeStep = activeStep,
                                textMeasurer = textMeasurer,
                                offset = offset
                            )

                            drawStepLabel(
                                localizedContext = localizedContext,
                                destination = destinations[i],
                                index = i,
                                activeStep = activeStep,
                                currentStep = currentStep,
                                textMeasurer = textMeasurer,
                                offset = offset,
                                circleRadiusPx = circleRadiusPx,
                                stepLabelSpacingPx = stepLabelSpacingPx
                            )
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { offset -> // 단계 클릭 감지
                            onStepClick(
                                offset = offset,
                                stepRects = stepRects,
                                onCurrentStepChange = onCurrentStepChange
                            )
                        }
                    )
                }
        )
    }
}

private suspend fun scrollToCurrentStep(
    rect: Rect,
    scrollState: ScrollState,
    viewportHeight: Int
) {
    val visibleTop = scrollState.value
    val visibleBottom = visibleTop + viewportHeight

    // 사각형이 보이는 영역 안에 있는지 확인
    val isAboveVisible = rect.bottom < visibleBottom  // 하단 영역
    val isBelowVisible = rect.top > visibleTop  // 상단 영역

    // 스크롤 조정
    when {
        !isAboveVisible -> {
            val scrollAmount = rect.bottom - visibleBottom
            if (scrollAmount > 0) {
                scrollState.animateScrollBy(scrollAmount)  // 아래로 스크롤
            }
        }

        !isBelowVisible -> {
            val scrollAmount = rect.top - visibleTop
            if (scrollAmount < 0) {
                scrollState.animateScrollBy(scrollAmount)  // 위로 스크롤
            }
        }
    }
}

private fun updateActiveStep(
    currentY: Float,
    prevStep: Int,
    currentStep: Int,
    circleCenters: List<Offset>,
    onActiveStepChange: (Int) -> Unit
) {
    if (currentStep > prevStep) {
        for (step in prevStep + 1..currentStep) {
            val circleY = circleCenters.getOrNull(step)?.y ?: 0f
            if (currentY >= circleY) {
                onActiveStepChange(step)
            }
        }
    } else if (currentStep < prevStep) {
        for (step in prevStep - 1 downTo currentStep) {
            val circleY = circleCenters.getOrNull(step + 1)?.y ?: 0f
            if (currentY <= circleY) {
                onActiveStepChange(step)
            }
        }
    }
}

private fun onStepClick(
    offset: Offset,
    stepRects: List<Rect>,
    onCurrentStepChange: (Int) -> Unit
) {
    for ((index, rect) in stepRects.withIndex()) {
        if (rect.contains(offset)) {
            onCurrentStepChange(index)
            break
        }
    }
}

private fun initStepBounds(
    destinations: List<TopLevelDestination>,
    onCircleCentersUpdate: (List<Offset>) -> Unit,
    onStepRectsUpdate: (List<Rect>) -> Unit,
    size: Size,
    circleRadiusPx: Float,
    circleSpacingPx: Float
) {
    val newCircleCenters = mutableListOf<Offset>()
    val newStepRects = mutableListOf<Rect>()

    destinations.forEachIndexed { step, _ ->
        val y = step * (circleRadiusPx * 2 + circleSpacingPx) + circleRadiusPx
        val center = Offset(circleRadiusPx, y)
        newCircleCenters.add(center)

        val stepRect = Rect(
            left = 0f,
            top = center.y - circleRadiusPx -
                    if (step > 0) circleSpacingPx / 2 else 0f,
            right = size.width,
            bottom = center.y + circleRadiusPx +
                    if (step < destinations.lastIndex) circleSpacingPx / 2 else 0f
        )
        newStepRects.add(stepRect)
    }

    onCircleCentersUpdate(newCircleCenters)
    onStepRectsUpdate(newStepRects)
}

private fun generateStepPath(
    centers: List<Offset>,
    circleRadiusPx: Float,
    strokeWidthPx: Float
): Path {
    val path = Path()

    centers.forEach { center ->
        path.addOval(Rect(center, circleRadiusPx))
    }

    if (centers.size > 1) {
        val firstCenter = centers.first()
        val lastCenter = centers.last()

        path.addRect(
            Rect(
                left = firstCenter.x - strokeWidthPx / 2,
                top = firstCenter.y,
                right = firstCenter.x + strokeWidthPx / 2,
                bottom = lastCenter.y
            )
        )
    }

    return path
}

private fun DrawScope.drawStepNumber(
    index: Int,
    activeStep: Int,
    textMeasurer: TextMeasurer,
    offset: Offset
) {
    val text = (index + 1).toString()
    val color = if (activeStep >= index) {
        activeTextColor
    } else {
        inactiveTextColor
    }

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = stepNumberTextSize,
            textAlign = TextAlign.Center
        )
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            offset.x - textLayoutResult.size.width / 2,
            offset.y - textLayoutResult.size.height / 2
        )
    )
}

private fun DrawScope.drawStepLabel(
    localizedContext: Context,
    destination: TopLevelDestination,
    index: Int,
    activeStep: Int,
    currentStep: Int,
    textMeasurer: TextMeasurer,
    offset: Offset,
    circleRadiusPx: Float,
    stepLabelSpacingPx: Float
) {
    val text = localizedContext.resources.getString(destination.titleResId)
    val color = if (activeStep == index && activeStep == currentStep) {
        activeTextColor
    } else {
        inactiveTextColor
    }

    val textLayoutResult = textMeasurer.measure(
        text = text,
        style = TextStyle(
            color = color,
            fontSize = stepLabelTextSize,
            textAlign = TextAlign.Left
        )
    )

    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = Offset(
            offset.x + circleRadiusPx + stepLabelSpacingPx,
            offset.y - textLayoutResult.size.height / 2
        )
    )
}
