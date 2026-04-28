package com.miguelangel.rickandmortyai.ui.detail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.miguelangel.rickandmortyai.R
import com.miguelangel.rickandmortyai.domain.model.Character
import com.miguelangel.rickandmortyai.domain.model.Episode
import com.miguelangel.rickandmortyai.ui.components.ErrorState
import com.miguelangel.rickandmortyai.ui.components.LoadingState
import com.miguelangel.rickandmortyai.ui.components.StatusChip

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun CharacterDetailScreen(
    characterId: Int,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    onBack: () -> Unit,
    viewModel: CharacterDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = (state as? CharacterDetailUiState.Success)?.character?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.detail_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        AnimatedContent(
            targetState = state,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "detailContent",
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { current ->
            when (current) {
                is CharacterDetailUiState.Loading -> LoadingState()
                is CharacterDetailUiState.Error -> ErrorState(
                    onRetry = viewModel::retry,
                    message = current.message,
                )
                is CharacterDetailUiState.Success -> DetailContent(
                    character = current.character,
                    episodes = current.episodes,
                    sharedTransitionScope = sharedTransitionScope,
                    animatedContentScope = animatedContentScope,
                    characterId = characterId,
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun DetailContent(
    character: Character,
    episodes: List<Episode>,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    characterId: Int,
) {
    with(sharedTransitionScope) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Box {
                    AsyncImage(
                        model = character.imageUrl,
                        contentDescription = character.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .sharedElement(
                                state = rememberSharedContentState(key = "image-$characterId"),
                                animatedVisibilityScope = animatedContentScope,
                            )
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(24.dp)),
                    )
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "name-$characterId"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    )
                    StatusChip(status = character.status)
                }
            }
            item {
                InfoRow(label = stringResource(R.string.label_species), value = character.species.ifBlank { "—" })
                InfoRow(label = stringResource(R.string.label_gender), value = character.gender.name.lowercase().replaceFirstChar { it.uppercase() })
                InfoRow(label = stringResource(R.string.label_origin), value = character.origin.ifBlank { "—" })
                InfoRow(label = stringResource(R.string.label_location), value = character.location.ifBlank { "—" })
            }
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.section_episodes),
                    style = MaterialTheme.typography.titleLarge,
                )
            }
            items(items = episodes, key = { it.id }) { episode ->
                EpisodeRow(episode = episode)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            modifier = Modifier.weight(1f),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1.6f),
        )
    }
}

@Composable
private fun EpisodeRow(episode: Episode) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "${episode.code}  ·  ${episode.name}",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = episode.airDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}
