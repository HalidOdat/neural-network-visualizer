package com.halidodat.neuralnetworkvisualizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.halidodat.neuralnetworkvisualizer.composables.CanvasGrid
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import com.halidodat.neuralnetworkvisualizer.screens.AddSampleScreen
import com.halidodat.neuralnetworkvisualizer.screens.AddScreen
import com.halidodat.neuralnetworkvisualizer.screens.InfoScreen
import com.halidodat.neuralnetworkvisualizer.screens.TestScreen
import com.halidodat.neuralnetworkvisualizer.screens.ListNeuralNetworkScreen
import com.halidodat.neuralnetworkvisualizer.screens.TrainingScreen
import com.halidodat.neuralnetworkvisualizer.ui.theme.NeuralNetworkVisualizerTheme
import io.realm.kotlin.ext.realmListOf
import kotlinx.serialization.Serializable
import uniffi.convnet_rust.Activation
import uniffi.convnet_rust.EndLayer
import uniffi.convnet_rust.Layer
import uniffi.convnet_rust.Specification
import uniffi.mobile.NeuralNetwork
import uniffi.mobile.specificationToJson

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalLayoutApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val items = listOf(
            BottomNavigationItem(
                title = "Networks",
                selectedIcon = Icons.Filled.Star,
                unselectedIcon = Icons.Default.Star,
                action = { _, navController ->
                    navController.navigate(Networks)
                }
            ),
            BottomNavigationItem(
                title = "New",
                selectedIcon = Icons.Filled.Add,
                unselectedIcon = Icons.Default.Add,
                action = { _, navController ->
                    navController.navigate(Add)
                }
            )
        );

        //                    var net = NeuralNetwork(Specification(layers = listOf(Layer.Input(10U, 10U, 10U)), finalLayer = EndLayer.Softmax (2U)))

        setContent {
            var selectedNavigationItemIndex by rememberSaveable { mutableIntStateOf(0) }

            NeuralNetworkVisualizerTheme {

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Scaffold(
                        bottomBar = {
                            NavigationBar {
                                items.forEachIndexed { index, item ->
                                    NavigationBarItem(
                                        selected = selectedNavigationItemIndex == index,
                                        onClick = {
//                                            if (selectedNavigationItemIndex != index) {
                                                selectedNavigationItemIndex = index
                                                item.action(index, navController)
//                                            }
                                        },
                                        label = { Text(text = item.title) },
                                        icon = {
                                            BadgedBox(
                                                badge = {
                                                    if (item.hasNews) {
                                                        Badge()
                                                    }
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (selectedNavigationItemIndex == index) {
                                                        item.selectedIcon
                                                    } else {
                                                        item.unselectedIcon
                                                    },
                                                    contentDescription = "${item.title} Navigation"
                                                )
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        Column(modifier = Modifier
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = Networks
                            ) {
                                composable<Networks> {
                                    val networks = viewModel.getAll();
                                    if (networks.isEmpty()) {
                                        viewModel.insert(NeuralNetworkRealm().apply {
                                            this.name = "MNIST Network"
                                            this.specification = specificationToJson(Specification(
                                                layers = listOf(
                                                    Layer.Input(28U, 28U, 1U),
                                                    Layer.Conv(5U, 8U, 1U, 2U, activation = Activation.RELU),
                                                    Layer.Pool(2U, 2U),
                                                    Layer.Conv(5U, 16U, 1U, 2U, activation = Activation.RELU),
                                                    Layer.Pool(3U, 3U),
                                                ),
                                                finalLayer = EndLayer.Softmax( classes = 10U ),
                                                )
                                            )
                                            this.neuralNetworkBytes = NeuralNetwork.loadMnist().toBytes()
                                            this.samples = realmListOf()
                                            this.labels = realmListOf()
                                        })
                                        ListNeuralNetworkScreen(networks = viewModel.getAll(), navController)
                                    } else {
                                        ListNeuralNetworkScreen(networks = networks, navController)
                                    }
                                }
                                composable<Test> {
                                    val args = it.toRoute<Test>()
                                    var net by remember { mutableStateOf(viewModel.get(args.name)!!) };
                                    var dim = net.dim()
                                    var grid by remember { mutableStateOf(Grid(dim.width.toInt(), dim.height.toInt())) };
                                    TestScreen(net, grid)
                                }
                                composable<Info> {
                                    val args = it.toRoute<Info>()
                                    var net by remember { mutableStateOf(viewModel.get(args.name)!!) };
                                    var dim = net.dim()
                                    InfoScreen(net, dim, navController)
                                }
                                composable<Training> {
                                    val args = it.toRoute<Training>()
                                    var net by remember { mutableStateOf(viewModel.get(args.name)!!) };
                                    var dim = net.dim()
                                    TrainingScreen(net, dim, navController)
                                }
                                composable<AddSample> {
                                    val args = it.toRoute<AddSample>()
                                    var net by remember { mutableStateOf(viewModel.get(args.name)!!) };
                                    var dim = net.dim()
                                    AddSampleScreen(net, dim, viewModel, navController)
                                }
                                composable<Add> {
                                    AddScreen(
                                        viewModel,
                                        navController
                                    )
                                }
                            }
                        }
                    }
                }

            }
        }
    }
}

@Serializable
data class Training(
    val name: String,
)

@Serializable
data class AddSample(
    val name: String,
)

@Serializable
data class Test(
    val name: String,
)

@Serializable
data class Info(
    val name: String,
)

@Serializable
object Networks

@Serializable
object Add

//@Composable
//fun BitmapImage(image: WeightImage, modifier: Modifier) {
//    val bitmap = Bitmap.createBitmap(image.width.toInt(), image.height.toInt(), Bitmap.Config.ALPHA_8)
//    bitmap.setPixels(image.image.asIterable().map { x -> x.toInt() }.toIntArray(), 0, image.width.toInt(), 0, 0, image.width.toInt(), image.height.toInt())
//    Image(
//        modifier = modifier,
//        bitmap = bitmap.asImageBitmap(),
//        contentDescription = "some useful description",
//        filterQuality = FilterQuality.High
//    )
//}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NeuralNetworkVisualizerTheme {
        CanvasGrid(Grid(4, 8), 16) { grid, x, y ->
            println("x: $x, y: $y")
            grid.set(x, y, 255U)

        }
    }
}
