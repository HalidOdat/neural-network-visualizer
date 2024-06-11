package com.halidodat.neuralnetworkvisualizer

import android.app.Application
import com.halidodat.neuralnetworkvisualizer.models.NeuralNetworkRealm
import com.halidodat.neuralnetworkvisualizer.models.SampleRealm
import dagger.hilt.android.HiltAndroidApp
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

class CoreApplication: Application() {
    companion object {
        lateinit var realm: Realm
    }

    override fun onCreate() {
        super.onCreate()
        val config = RealmConfiguration.create(
            setOf(
                NeuralNetworkRealm::class,
                SampleRealm::class,
            )
        )
        realm = Realm.open(config)
    }
}