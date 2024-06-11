package com.halidodat.neuralnetworkvisualizer.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.halidodat.neuralnetworkvisualizer.ExpandableCard
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import uniffi.convnet_rust.Activation
import uniffi.convnet_rust.Layer
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.halidodat.neuralnetworkvisualizer.MainViewModel
import com.halidodat.neuralnetworkvisualizer.Networks
import com.halidodat.neuralnetworkvisualizer.models.SampleRealm
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import kotlinx.coroutines.launch
import uniffi.convnet_rust.EndLayer
import uniffi.convnet_rust.Specification
import uniffi.mobile.NeuralNetwork
import uniffi.mobile.specificationToJson

//data class LayerDescriptor(
//    val name: Strig
//)

@Composable
fun ActivationDropdown(
    current: Activation,
    select: (value: Activation) -> Unit
) {
    val options = Activation.entries.toList()
    var expanded by remember { mutableStateOf(false) }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = { expanded = true })
                .padding(horizontal = 16.dp)
                .height(IntrinsicSize.Min)
        ) {
            Text(
                text = current.name,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .selectableGroup()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    onClick = {
                        select(option)
                        expanded = false
                    },
                    text = {Text(option.name)}
                )
            }
        }
    }
}

@Composable
fun UnsignedTextNumberInput(
    label: String,
    current: UInt,
    setValue: (value: UInt) -> Unit
) {
    var isValid by remember { mutableStateOf(true) }
    var textInput by remember { mutableStateOf(current.toString()) }

    TextField(
        value = textInput,
        onValueChange = { input ->
            textInput = input
            try {
                val value = input.toUInt()
                isValid = true
                setValue(value)
            } catch (e: NumberFormatException) {
                isValid = false
            }
        },
        label = { Text(label) },
        isError = !isValid,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    )

    if (!isValid) {
        Text(text = "Please enter valid unsigned number", color = Color.Red)
    }
}

data class LayerOption(
    val name: String,
    val layer: Layer,
)

private val layerOptions = listOf(
    LayerOption(
        name = "Conv Layer",
        layer = Layer.Conv(
            filters = 16U,
            padding = 2U,
            stride = 1U,
            sx = 5U,
            activation = Activation.RELU
        )
    ),
    LayerOption(
        name = "Pool Layer",
        layer = Layer.Pool(
            sx = 2U,
            stride = 2U
        )
    ),
    LayerOption(
        name = "Dense Layer",
        layer = Layer.Dense(
            neurons = 10U,
            activation = Activation.RELU
        )
    )
)

@Composable
fun AddScreen(
    repository: MainViewModel? = null,
    navController: NavController?=null
) {

    var name by remember { mutableStateOf("MyNeuralNetwork") }
    var selectedOption by remember { mutableStateOf(layerOptions[0]) }
    var expanded by remember { mutableStateOf(false) }

    val layers = remember { mutableStateListOf<Layer>() }
//
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            text = "Add Neural Network",
            fontSize = MaterialTheme.typography.titleLarge.fontSize,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        HorizontalDivider()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            // Name input field
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (layers.isEmpty()) {
                layers.add(Layer.Input(
                    width = 24U, height = 24U, depth = 1U
                ));
            }

            for (i in layers.indices) {
                val layer = layers[i]
                ExpandableCard(
                    title = "${layer.javaClass.kotlin.simpleName ?: layer.javaClass.name} Layer",
                    description = "",
                ) {
                    if (layer is Layer.Input) {
                        UnsignedTextNumberInput(
                            label = "Width",
                            current = layer.width
                        ) {
                            layers[i] = layer.copy(width = it.toUInt())
                        }
                        UnsignedTextNumberInput(
                            label = "Height",
                            current = layer.height
                        ) {
                            layers[i] = layer.copy(height = it.toUInt())
                        }
                    } else if (layer is Layer.Conv) {
                        UnsignedTextNumberInput(
                            label = "Width",
                            current = layer.sx
                        ) {
                            layers[i] = layer.copy(sx = it)
                        }
                        UnsignedTextNumberInput(
                            label = "Filters",
                            current = layer.filters
                        ) {
                            layers[i] = layer.copy(filters = it)
                        }
                        UnsignedTextNumberInput(
                            label = "Stride",
                            current = layer.stride
                        ) {
                            layers[i] = layer.copy(stride = it)
                        }
                        UnsignedTextNumberInput(
                            label = "Padding",
                            current = layer.padding
                        ) {
                            layers[i] = layer.copy(padding = it)
                        }

                        ActivationDropdown(layer.activation) {
                            layers[i] = layer.copy(activation = it)
                        }
                    } else if (layer is Layer.Pool) {
                        UnsignedTextNumberInput(
                            label = "Width",
                            current = layer.sx
                        ) {
                            layers[i] = layer.copy(sx = it)
                        }
                        UnsignedTextNumberInput(
                            label = "Stride",
                            current = layer.stride
                        ) {
                            layers[i] = layer.copy(stride = it)
                        }
                    } else if (layer is Layer.Dense) {
                        UnsignedTextNumberInput(
                            label = "Neurons",
                            current = layer.neurons
                        ) {
                            layers[i] = layer.copy(neurons = it)
                        }

                        ActivationDropdown(layer.activation) {
                            layers[i] = layer.copy(activation = it)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
            ) {
                // Dropdown
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(6F)) {
                    OutlinedButton(
                        onClick = { expanded = !expanded },
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Text(text = selectedOption.name)
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        layerOptions.forEach { option ->
                            DropdownMenuItem(
                                onClick = {
                                    selectedOption = option
                                    expanded = true
                                },
                                text = {  Text(text = option.name) }
                            )
                        }
                    }
                }

               IconButton(
                   modifier = Modifier.weight(1F),
                   onClick = {
                       val layer = selectedOption.layer
                       if (layer is Layer.Conv) {
                           layers.add(layer.copy())
                       } else if (layer is Layer.Pool) {
                           layers.add(layer.copy())
                       } else if (layer is Layer.Dense) {
                           layers.add(layer.copy())
                       }
                   }
               ) {
                   Icon(Icons.Default.AddCircle, contentDescription = "Add Layer")
               }
            }

            Spacer(modifier = Modifier.height(8.dp))

            var numClasses by remember { mutableStateOf(2U) }

            ExpandableCard(
                title = "Softmax Layer",
                description = "Defined the number of labels",
            ) {
                UnsignedTextNumberInput(
                    label = "Classes",
                    current = numClasses
                ) {
                    if (it > 0U) {
                        numClasses = it
                    }
                }
            }

            val coroutineScope = rememberCoroutineScope()

            // Button to submit form
            Button(
                onClick = {
                    val spec = Specification(
                        layers = layers,
                        finalLayer = EndLayer.Softmax(classes = numClasses)
                    );
                    var net = NeuralNetwork(spec)
                    val bytes = net.toBytes()

                    coroutineScope.launch {
                        repository!!.insert(
                            NeuralNetworkRealm().apply {
                                this.name = name
                                this.specification = specificationToJson(spec)
                                this.neuralNetworkBytes = bytes
                                this.labels = realmListOf()
                                this.samples = realmListOf()
                            }
                        )
                    }

                    navController!!.navigate(Networks)
                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = "Submit")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddScreenPreview() {
    AddScreen()
}