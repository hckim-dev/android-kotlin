package com.example.myapplication.ui.screen.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.myapplication.R
import com.example.myapplication.model.TopLevelDestination

@Composable
fun SignUpProcessScreen() {
    val localizedContext = LocalContext.current
    val scrollState = rememberScrollState()

    var prevIndex by remember { mutableIntStateOf(0) }
    var focusIndex by remember { mutableIntStateOf(0) }

    val destinations = List(10) { TopLevelDestination(R.string.app_name) }
    val stepLabels = remember(destinations) {
        destinations.map { localizedContext.resources.getString(it.titleResId) }
    }

    val onFocusChanged: (Int) -> Unit = { newIndex ->
        if (newIndex in destinations.indices && newIndex != focusIndex) {
            prevIndex = focusIndex
            focusIndex = newIndex
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Column(modifier = Modifier.weight(1f)) {
            SignUpProcessView(
                localizedContext = localizedContext,
                scrollState = scrollState,
                destinations = destinations,
                prevStep = prevIndex,
                currentStep = focusIndex,
                onCurrentStepChange = onFocusChanged
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if (focusIndex > 0) {
                        onFocusChanged(focusIndex - 1)
                    }
                },
                enabled = focusIndex > 0
            ) {
                Text("이전")
            }

            Button(
                onClick = {
                    if (focusIndex < destinations.lastIndex) {
                        onFocusChanged(focusIndex + 1)
                    }
                },
                enabled = focusIndex < destinations.size - 1
            ) {
                Text("다음")
            }
        }
    }
}