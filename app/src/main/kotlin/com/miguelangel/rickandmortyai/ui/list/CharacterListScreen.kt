package com.miguelangel.rickandmortyai.ui.list

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.imageLoader
import coil.request.ImageRequest
import coil.size.Size
import com.miguelangel.rickandmortyai.R
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.ui.components.CharacterCard
import com.miguelangel.rickandmortyai.ui.components.EmptyState
import com.miguelangel.rickandmortyai.ui.components.ErrorState
import com.miguelangel.rickandmortyai.ui.components.LoadingState
import com.miguelangel.rickandmortyai.ui.components.SearchField

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CharacterListScreen(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    onCharacterClick: (Int) -> Unit,
    viewModel: CharacterListViewModel = hiltViewModel(),
) {
    val items = viewModel.characters.collectAsLazyPagingItems()
    val query by viewModel.query.collectAsState()
    val listState = rememberLazyListState()
    val seenIds = remember { HashSet<Int>() }

    val context = LocalContext.current
    val prefetchedUrls = remember { HashSet<String>() }
    LaunchedEffect(items.itemCount) {
        val loader = context.imageLoader
        items.itemSnapshotList.items.forEach { character ->
            if (character.imageUrl.isNotEmpty() && prefetchedUrls.add(character.imageUrl)) {
                loader.enqueue(
                    ImageRequest.Builder(context)
                        .data(character.imageUrl)
                        .memoryCacheKey(character.imageUrl)
                        .diskCacheKey(character.imageUrl)
                        .size(Size.ORIGINAL)
                        .build(),
                )
            }
        }
    }

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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SearchField(
                value = query,
                onValueChange = viewModel::onQueryChange,
                onClear = viewModel::onClearQuery,
            )
            Spacer(modifier = Modifier.height(8.dp))

            when (val refresh = items.loadState.refresh) {
                is LoadState.Loading -> if (items.itemCount == 0) {
                    LoadingState()
                } else {
                    CharacterList(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope,
                        items = items,
                        listState = listState,
                        seenIds = seenIds,
                        onCharacterClick = onCharacterClick,
                    )
                }
                is LoadState.Error -> ErrorState(
                    onRetry = items::retry,
                    message = refresh.error.message,
                )
                else -> if (items.itemCount == 0) {
                    EmptyState(
                        text = if (query.isNotEmpty()) {
                            stringResource(R.string.empty_no_results, query)
                        } else null,
                    )
                } else {
                    CharacterList(
                        sharedTransitionScope = sharedTransitionScope,
                        animatedContentScope = animatedContentScope,
                        items = items,
                        listState = listState,
                        seenIds = seenIds,
                        onCharacterClick = onCharacterClick,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CharacterList(
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    items: LazyPagingItems<Character>,
    listState: LazyListState,
    seenIds: HashSet<Int>,
    onCharacterClick: (Int) -> Unit,
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            count = items.itemCount,
            key = items.itemKey { it.id },
        ) { index ->
            val character = items[index] ?: return@items
            FirstAppearanceItem(
                id = character.id,
                seenIds = seenIds,
            ) { entryModifier ->
                CharacterCard(
                    character = character,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    onClick = { onCharacterClick(character.id) },
                    modifier = entryModifier,
                )
            }
        }
        if (items.loadState.append is LoadState.Loading) {
            item {
                Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) { LoadingState() }
            }
        }
    }
}

@Composable
private fun LazyItemScope.FirstAppearanceItem(
    id: Int,
    seenIds: HashSet<Int>,
    content: @Composable (Modifier) -> Unit,
) {
    val alreadySeen = remember(id) { id in seenIds }
    val progress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 280),
        label = "entry-$id",
    )
    LaunchedEffect(id) { seenIds.add(id) }

    val animationProgress = if (alreadySeen) 1f else progress

    val modifier = Modifier
        .animateItem()
        .graphicsLayer {
            alpha = animationProgress
            translationY = (1f - animationProgress) * 40f
        }

    content(modifier)
}
