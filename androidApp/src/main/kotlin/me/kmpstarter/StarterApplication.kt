package me.kmpstarter

import android.app.Application
import me.kmpstarter.data.local.createStarterDatabase
import me.kmpstarter.data.network.HttpClientFactory
import me.kmpstarter.data.network.createHttpEngine
import me.kmpstarter.di.StarterGraph
import me.kmpstarter.di.createStarterGraph

class StarterApplication : Application() {
    lateinit var graph: StarterGraph
        private set

    override fun onCreate() {
        super.onCreate()
        val database = createStarterDatabase(this)
        val client = HttpClientFactory(createHttpEngine()).create()
        graph = createStarterGraph(database, client)
    }
}
