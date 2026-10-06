package com.example.mda_mobile_app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable


@Composable
fun MilestoneFeedback(
    title: String,
    message: String,
    buttonText: String = "Yay me!",
    icon: String? = null,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title)
        },
        text = {
            Column {
                if (icon != null) {
                    Text(
                        text = icon,
                        style = MaterialTheme.typography.headlineMedium
                    )
                }

                Text(text = message)
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(buttonText)
            }
        }
    )
}

//example call
//@Composable
//fun MilestoneDemoScreen(
//    modifier: Modifier = Modifier
//) {
//    var showMilestone by remember {
//        mutableStateOf(false)
//    }
//
//    Column(
//        modifier = modifier.fillMaxSize(),
//        verticalArrangement = Arrangement.Center,
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Button(
//            onClick = {
//                showMilestone = true
//            }
//        ) {
//            Text("Show Milestone")
//        }
//    }
//
//    if (showMilestone) {
//        MilestoneFeedback(
//            title = "Great job!",
//            message = "You completed a 3-day medication streak.",
//            buttonText = "Continue",
//            icon = "🏆",
//            onDismiss = {
//                showMilestone = false
//            }
//        )
//    }
//}
