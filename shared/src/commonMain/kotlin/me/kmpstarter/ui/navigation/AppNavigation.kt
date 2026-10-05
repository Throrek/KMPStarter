package me.kmpstarter.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import me.kmpstarter.di.StarterGraph
import me.kmpstarter.ui.articledetails.ArticleDetailsRoute
import me.kmpstarter.ui.articles.ArticlesRoute

@Composable
fun AppNavigation(graph: StarterGraph) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Articles) {
        composable<Articles> { entry ->
            ArticlesRoute(graph) { articleId ->
                // A second tap can arrive before recomposition or the transition completes.
                if (
                    navController.currentBackStackEntry == entry &&
                        entry.lifecycle.currentState == Lifecycle.State.RESUMED
                ) {
                    navController.navigate(ArticleDetails(articleId)) { launchSingleTop = true }
                }
            }
        }
        composable<ArticleDetails> { entry ->
            ArticleDetailsRoute(graph, entry.toRoute<ArticleDetails>().articleId) {
                if (
                    navController.currentBackStackEntry == entry &&
                        entry.lifecycle.currentState == Lifecycle.State.RESUMED
                ) {
                    navController.popBackStack()
                }
            }
        }
    }
}
