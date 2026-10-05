package me.kmpstarter

import androidx.compose.runtime.Composable
import me.kmpstarter.di.StarterGraph
import me.kmpstarter.ui.navigation.AppNavigation
import me.kmpstarter.ui.theme.StarterTheme

@Composable
fun App(graph: StarterGraph) {
    StarterTheme { AppNavigation(graph) }
}
