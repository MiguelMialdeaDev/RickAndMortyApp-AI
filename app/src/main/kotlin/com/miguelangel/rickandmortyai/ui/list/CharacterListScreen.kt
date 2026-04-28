package com.miguelangel.rickandmortyai.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.miguelangel.rickandmortyai.R
import com.miguelangel.rickandmortyai.ui.components.CharacterCard
import com.miguelangel.rickandmortyai.ui.components.EmptyState
import com.miguelangel.rickandmortyai.ui.components.ErrorState
import com.miguelangel.rickandmortyai.ui.components.LoadingState

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CharacterListScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    onCharacterClick: (Int) -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel(),
) {
    val items = viewModel.characters.collectAsLazyPagingItems()
    val listState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.characters_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                },
            )
        },
    ) { padding ->
        when (val refresh = items.loadState.refresh) {
            is LoadState.Loading -> if (items.itemCount == 0) {
                LoadingState(modifier = Modifier.padding(padding))
            } else {
                CharacterList(
                    padding = padding,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    items = items,
                    listState = listState,
                    onCharacterClick = onCharacterClick,
                )
            }
            is LoadState.Error -> ErrorState(
                onRetry = items::retry,
                modifier = Modifier.padding(padding),
                message = refresh.error.message,
            )
            else -> if (items.itemCount == 0) {
                EmptyState(modifier = Modifier.padding(padding))
            } else {
                CharacterList(
                    padding = padding,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    items = items,
                    listState = listState,
                    onCharacterClick = onCharacterClick,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CharacterList(
    padding: PaddingValues,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    items: androidx.paging.compose.LazyPagingItems<com.miguelangel.rickandmortyai.domain.model.Character>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onCharacterClick: (Int) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.id },
        ) { index ->
            val character = items[index] ?: return@items
            val animated = remember(character.id) { mutableStateOf(false) }
            StaggeredEntry(visibleState = animated, index = index) {
                CharacterCard(
                    character = character,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    onClick = { onCharacterClick(character.id) },
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
        if (items.loadState.append is LoadState.Loading) {
            item { Box(modifier = Modifier.fillMaxSize()) { LoadingState() } }
        }
    }
}

@Composable
private fun StaggeredEntry(
    visibleState: MutableState<Boolean>,
    index: Int,
    content: @Composable () -> Unit,
) {
    androidx.compose.runtime.LaunchedEffect(Unit) { visibleState.value = true }
    AnimatedVisibility(
        visible = visibleState.value,
        enter = fadeIn(animationSpec = tween(300, delayMillis = (index % 10) * 40)) +
            slideInVertically(
                animationSpec = tween(durationMillis = 350, delayMillis = (index % 10) * 40, easing = LinearEasing),
                initialOffsetY = { it / 4 },
            ),
    ) { content() }
}
