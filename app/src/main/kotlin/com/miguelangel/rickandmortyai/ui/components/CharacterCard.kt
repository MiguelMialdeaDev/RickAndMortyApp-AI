package com.miguelangel.rickandmortyai.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.miguelangel.rickandmortyai.domain.model.Character

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun CharacterCard(
    character: Character,
    sharedTransitionScope: SharedTransitionScope,
    animatedContentScope: AnimatedVisibilityScope,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    with(sharedTransitionScope) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AsyncImage(
                    model = character.imageUrl,
                    contentDescription = character.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .sharedElement(
                            state = rememberSharedContentState(key = "image-${character.id}"),
                            animatedVisibilityScope = animatedContentScope,
                        )
                        .size(84.dp)
                        .clip(RoundedCornerShape(16.dp)),
                )
                Spacer(modifier = Modifier.size(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = character.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "name-${character.id}"),
                            animatedVisibilityScope = animatedContentScope,
                        ),
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${character.species} • ${character.location}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatusChip(status = character.status)
                }
            }
        }
    }
}
