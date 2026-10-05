package me.kmpstarter

import androidx.compose.ui.window.ComposeUIViewController
import me.kmpstarter.data.local.createStarterDatabase
import me.kmpstarter.data.network.HttpClientFactory
import me.kmpstarter.data.network.createHttpEngine
import me.kmpstarter.di.createStarterGraph

private object StarterHost {
    private val database = createStarterDatabase()
    private val client = HttpClientFactory(createHttpEngine()).create()
    val graph = createStarterGraph(database, client)
}

fun MainViewController(): platform.UIKit.UIViewController {
    val graph = StarterHost.graph
    return ComposeUIViewController { App(graph) }
}
