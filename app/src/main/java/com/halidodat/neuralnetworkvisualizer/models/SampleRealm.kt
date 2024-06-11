package com.halidodat.neuralnetworkvisualizer.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import kotlinx.serialization.Serializable
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

@Serializable
class SampleRealm: RealmObject {
    @PrimaryKey
    var id: ObjectId = BsonObjectId()

    var values: ByteArray = ByteArray(0)
    var width: Int = 0
    var height: Int = 0
    var label: Int = 0
}