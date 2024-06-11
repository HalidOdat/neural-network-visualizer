package com.halidodat.neuralnetworkvisualizer.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.halidodat.neuralnetworkvisualizer.ExpandableCard
import com.halidodat.neuralnetworkvisualizer.Grid
import com.halidodat.neuralnetworkvisualizer.composables.CanvasGrid
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import uniffi.mobile.Layer
import uniffi.mobile.NeuralNetwork

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TestScreen(
    network: NeuralNetworkRealm,
    grid: Grid,
    weights: Boolean = false,
) {
    var prediction by remember { mutableIntStateOf(-1) }
    var rest by remember { mutableStateOf("") }

    var layers by remember { mutableStateOf<List<Layer>>(listOf()) }
    var net by remember { mutableStateOf(NeuralNetwork.fromBytes(network.neuralNetworkBytes)) }


    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        CanvasGrid(grid, 16) { grid, x, y ->
            grid.add(x + 1, y, 10U)
            grid.add(x - 1, y, 10U)
            grid.add(x, y + 1, 10U)
            grid.add(x, y - 1, 10U)
            grid.add(x, y, 150U)

            prediction =
                net.predict(grid.bytes(), grid.width.toUInt(), grid.height.toUInt())
                    .toInt()
            val predictions = net.predicts(
                grid.bytes(),
                grid.width.toUInt(),
                grid.height.toUInt()
            );
            rest = predictions.joinToString(separator = "\n") { x ->
                "Index: ${x.index}: Confidence: ${
                    String.format(
                        "%.4f",
                        x.confidance
                    )
                }"
            }

            layers = net.layers()
        }
        Button(onClick = {
            grid.clear()
            rest = ""
        }) {
            Text("Clear")
        }
        Text("$rest")

        Column {
            for (i in layers.indices) {
                val layer = layers[i]
                ExpandableCard(
                    title = "${layer.name} Layer ${layer.inSx}x${layer.inSy}x${layer.inDepth}",
                    description = "The ${i}th layer of the convolutional Network"
                ) {
                    FlowRow(modifier = Modifier.padding(8.dp)) {
                        key(layers) {
                            var images by remember { mutableStateOf(net.weightImages(i.toUInt())!!) }
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
    }
}