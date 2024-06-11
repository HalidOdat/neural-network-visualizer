package com.halidodat.neuralnetworkvisualizer.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.halidodat.neuralnetworkvisualizer.AddSample
import com.halidodat.neuralnetworkvisualizer.ExpandableCard
import com.halidodat.neuralnetworkvisualizer.Grid
import com.halidodat.neuralnetworkvisualizer.Training
import com.halidodat.neuralnetworkvisualizer.composables.CanvasGrid
import com.halidodat.neuralnetworkvisualizer.models.Dim
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import uniffi.mobile.Layer
import uniffi.mobile.NeuralNetwork

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun InfoScreen(
    network: NeuralNetworkRealm,
    dim: Dim,
    navController: NavController,
) {
    var net by remember { mutableStateOf(NeuralNetwork.fromBytes(network.neuralNetworkBytes)) }
    var layers by remember { mutableStateOf<List<Layer>>(net.layers()) }

    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = "Input ${dim.width}x${dim.height}x1",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Column {
            for (i in layers.indices) {
                val layer = layers[i]
                ExpandableCard(
                    title = "${layer.name} Layer ${layer.inSx}x${layer.inSy}x${layer.inDepth}",
                    description = "The ${i + 1}th layer of the convolutional Network"
                ) {
                    FlowRow(modifier = Modifier.padding(8.dp)) {
                        key(layers) {
                            var images by remember { mutableStateOf(net.layerImages(i.toUInt())!!) }
                            for (image in images) {
                                CanvasGrid(
                                    grid = Grid(image),
                                    pixelSize = 5,
                                    gridLineWeight = 0F
                                )
                            }
                        }
                    }
                }
            }
        }

        Row {
            Button(
                onClick = {
                    navController.navigate(AddSample(
                        name = network.name
                    ))
                }
            ) {
                Text(text = "Add Sample")
            }

            Button(onClick = {
                navController.navigate(Training(name = network.name))
            }) {
                Text(text = "Train")
            }
        }
    }
}