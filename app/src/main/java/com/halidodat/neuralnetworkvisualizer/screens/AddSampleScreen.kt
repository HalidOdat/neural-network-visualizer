package com.halidodat.neuralnetworkvisualizer.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.halidodat.neuralnetworkvisualizer.ExpandableCard
import com.halidodat.neuralnetworkvisualizer.Grid
import com.halidodat.neuralnetworkvisualizer.MainViewModel
import com.halidodat.neuralnetworkvisualizer.composables.CanvasGrid
import com.halidodat.neuralnetworkvisualizer.models.Dim
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import com.halidodat.neuralnetworkvisualizer.models.SampleRealm
import uniffi.mobile.Layer
import uniffi.mobile.NeuralNetwork

@SuppressLint("UnrememberedMutableState")
@Composable
fun AddSampleScreen(
    network: NeuralNetworkRealm,
    dim: Dim,
    modelView: MainViewModel,
    navController: NavController
) {
    var network = network
    var grid by remember { mutableStateOf(Grid(dim.width.toInt(), dim.height.toInt())) }
    var label by remember { mutableIntStateOf(0) }
    var expanded by remember { mutableStateOf(false) }
    var classesOptions by remember { mutableStateOf((0..<network.numLabels().toInt()).toList()) }

    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        CanvasGrid(grid, 16) { grid, x, y ->
            grid.add(x + 1, y, 10U)
            grid.add(x - 1, y, 10U)
            grid.add(x, y + 1, 10U)
            grid.add(x, y - 1, 10U)
            grid.add(x, y, 150U)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
        ) {
            // Dropdown
            Box {
                OutlinedButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.CenterStart)
                ) {
                    Text(text = "Class ${label.toString()}")
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.align(Alignment.CenterEnd)
                ) {
                    classesOptions.forEach { option ->
                        DropdownMenuItem(
                            onClick = {
                                label = option
                                expanded = true
                            },
                            text = {  Text(text = "Class ${option.toString()}") }
                        )
                    }
                }
            }

            Row {
                Button(onClick = {
                    grid.clear()
                }) {
                    Text("Clear")
                }
                Button(onClick = {
                    val sample = SampleRealm().apply {
                        this.values = grid.bytes()
                        this.width = grid.width
                        this.height = grid.height
                        this.label = label
                    };
                    label = label
                    modelView.addSampleToNetwork(network, sample)
                    grid.clear()
                    network = modelView.get(name = network.name)!!
                }) {
                    Text("Add Sample")
                }
            }
    }
}
}