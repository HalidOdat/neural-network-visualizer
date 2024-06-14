package com.halidodat.neuralnetworkvisualizer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import com.halidodat.neuralnetworkvisualizer.models.SampleRealm
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

class MainViewModel: ViewModel() {
    val realm: Realm = CoreApplication.realm

    fun insert(net: NeuralNetworkRealm) {
        viewModelScope.launch {
            realm.write {
                copyToRealm(net, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    fun insert(sample: SampleRealm) {
        viewModelScope.launch {
            realm.write {
                copyToRealm(sample, updatePolicy = UpdatePolicy.ALL)
            }
        }
    }

    fun getAll(): List<NeuralNetworkRealm> = realm.query<NeuralNetworkRealm>().find().toList()

    fun get(name: String): NeuralNetworkRealm? {
        return realm.query<NeuralNetworkRealm>("name == $0", name).first().find()
    }

    fun get(id: ObjectId): NeuralNetworkRealm? {
        return realm.query<NeuralNetworkRealm>("id == $0", id).first().find()
    }

    fun delete(id: String) {
        viewModelScope.launch {
            realm.write {
                val entity = query<NeuralNetworkRealm>("id == $0", id).find()
                delete(entity)
            }
        }
    }

    fun update(entity: NeuralNetworkRealm) {
        viewModelScope.launch {
            realm.write {
                val findNote = query<NeuralNetworkRealm>("id == $0", entity.id).first().find()

                findNote?.apply {
                    name = entity.name
                    specification = entity.specification
                    neuralNetworkBytes = entity.neuralNetworkBytes
                    labels = entity.labels
                    samples = entity.samples
                }
            }
        }
    }
    fun addSampleToNetwork(entity: NeuralNetworkRealm, sample: SampleRealm) {
        viewModelScope.launch {
            realm.write {

                val instantiatedSample = copyToRealm(sample, updatePolicy = UpdatePolicy.ALL)
                val sampleLatest = findLatest(instantiatedSample) ?: return@write
//                val findNote = query<NeuralNetworkRealm>("id == $0", entity.id).first().find()
                val entityLatest = findLatest(entity) ?: return@write

                entityLatest.samples.add(sampleLatest)
                entityLatest?.apply {
                    this.name = entityLatest.name
                    this.specification = entityLatest.specification
                    this.neuralNetworkBytes = entityLatest.neuralNetworkBytes
                    this.labels = entityLatest.labels
                    this.samples = entityLatest.samples

                }

                update(entityLatest)
            }
        }
    }
}