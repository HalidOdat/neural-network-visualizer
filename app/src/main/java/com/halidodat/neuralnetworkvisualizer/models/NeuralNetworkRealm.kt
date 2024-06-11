package com.halidodat.neuralnetworkvisualizer.models

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import org.mongodb.kbson.ObjectId
import uniffi.convnet_rust.EndLayer
import uniffi.convnet_rust.Layer
import uniffi.mobile.specificationFromJson

data class Dim(
    val width: UInt,
    val height: UInt
)

@Serializable
class NeuralNetworkRealm: RealmObject {
    @PrimaryKey var id: ObjectId = ObjectId()

    var name: String = ""
    var specification: String = ""
    var neuralNetworkBytes: ByteArray = ByteArray(0)

    var labels: RealmList<String> = realmListOf()
    var samples: RealmList<SampleRealm> = realmListOf()

    fun dim(): Dim {
        val layer = specificationFromJson(specification).layers[0]
        val input = (layer as Layer.Input)
        return Dim(input.width, input.height)
    }

    fun numLabels(): UInt {
        val layer = specificationFromJson(specification).finalLayer
        val input = (layer as EndLayer.Softmax)
        return input.classes
    }
}
