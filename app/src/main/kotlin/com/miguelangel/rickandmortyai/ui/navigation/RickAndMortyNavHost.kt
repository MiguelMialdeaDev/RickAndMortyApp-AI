package com.miguelangel.rickandmortyai.ui.navigation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.miguelangel.rickandmortyai.ui.detail.CharacterDetailScreen
import com.miguelangel.rickandmortyai.ui.list.CharacterListScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun RickAndMortyNavHost() {
    val navController = rememberNavController()

    SharedTransitionLayout {
        NavHost(
            navController = navController,
            startDestination = Route.CharacterList,
        ) {
            composable<Route.CharacterList> {
                CharacterListScreen(
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this,
                    onCharacterClick = { id ->
                        navController.navigate(Route.CharacterDetail(id))
                    },
                )
            }
            composable<Route.CharacterDetail> { backStackEntry ->
                val args = backStackEntry.toRoute<Route.CharacterDetail>()
                CharacterDetailScreen(
                    characterId = args.id,
                    sharedTransitionScope = this@SharedTransitionLayout,
                    animatedContentScope = this,
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
