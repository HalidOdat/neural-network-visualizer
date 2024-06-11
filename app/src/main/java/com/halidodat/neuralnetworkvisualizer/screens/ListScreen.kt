package com.halidodat.neuralnetworkvisualizer.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.halidodat.neuralnetworkvisualizer.Info
import com.halidodat.neuralnetworkvisualizer.Test
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm

@Composable
fun ListNeuralNetworkScreen(
    networks: List<NeuralNetworkRealm>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Neural Networks",
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        HorizontalDivider()

        for (network in networks) {
            Card(
                modifier = Modifier
                    .fillMaxSize()
                    .animateContentSize(
                        animationSpec = tween(
                            durationMillis = 300,
                            easing = LinearOutSlowInEasing,
                        )
                    )
                    .padding(4.dp),
                onClick = {
                    navController.navigate(Test(name = network.name))
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            modifier = Modifier.weight(6F),
                            text = network.name,
                            fontSize = MaterialTheme.typography.titleMedium.fontSize,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(
                            modifier = Modifier
                                .alpha(0.5F)
                                .weight(1F),
                            onClick = {
                                navController.navigate(Info(name = network.name))
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Info, contentDescription = "Info and Train")
                        }
                        IconButton(
                            modifier = Modifier
                                .alpha(0.5F)
                                .weight(1F),
                            onClick = {
                                navController.navigate(Test(name = network.name))
                            }
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Test Network")
                        }
                    }
                }
            }
        }
    }
}