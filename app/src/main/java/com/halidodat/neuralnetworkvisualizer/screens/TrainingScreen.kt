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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.halidodat.neuralnetworkvisualizer.AddSample
import com.halidodat.neuralnetworkvisualizer.ExpandableCard
import com.halidodat.neuralnetworkvisualizer.Grid
import com.halidodat.neuralnetworkvisualizer.MainViewModel
import com.halidodat.neuralnetworkvisualizer.composables.CanvasGrid
import com.halidodat.neuralnetworkvisualizer.models.Dim
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import kotlinx.coroutines.launch
import uniffi.convnet_rust.Sample
import uniffi.convnet_rust.TrainStats
import uniffi.mobile.Layer
import uniffi.mobile.NeuralNetwork
import uniffi.mobile.OtherSample

@OptIn(ExperimentalLayoutApi::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun TrainingScreen(
    network: NeuralNetworkRealm,
    dim: Dim,
    modelView: MainViewModel,
    navController: NavController,
) {
    var net by remember { mutableStateOf(NeuralNetwork.fromBytes(network.neuralNetworkBytes)) }

    var stats by remember { mutableStateOf<MutableList<TrainStats>>(mutableListOf()) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        ExpandableCard(title = "Samples", description = "Samples to Train the Neural Network") {
            for (sample in network.samples) {
                Column {
                    CanvasGrid(grid = Grid(sample), pixelSize = 4)
                    Text(text = "Class ${sample.label}")
                }
            }
        }

        Button(onClick = {
            var samples = mutableListOf<OtherSample>()
            for (sample in network.samples) {
                samples.add(
                    OtherSample(
                        bytes = sample.values,
                        width = sample.width.toUInt(),
                        height = sample.height.toUInt(),
                        label = sample.label.toUInt(),
                    )
                )
            }
            net.setSamples(samples, 5U)
            stats = mutableListOf()

            coroutineScope.launch {
                var stat = net.train()
                while (!stat.finished) {
//                    println("Training!!")
                    stats.add(stat.stats)

                    stat = net.train()
                }

                 modelView.realm.write {
                    findLatest(network)?.let { network ->
                        network.neuralNetworkBytes = net.toBytes()
                    }
                }
            }

        }) {
            Text(text = "Train")
        }

        key(stats) {
            if (stats.isNotEmpty()) {
                Column(
                    modifier = Modifier.padding(4.dp)
                ) {
                    for (stat in stats) {
                        Text(text = "Loss: ${stat.costLoss}")
                    }
                }
            }
        }
    }
}
