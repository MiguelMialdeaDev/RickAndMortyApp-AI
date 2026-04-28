package com.miguelangel.rickandmortyai.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.miguelangel.rickandmortyai.R
import com.miguelangel.rickandmortyai.domain.model.CharacterStatus
import com.miguelangel.rickandmortyai.ui.theme.StatusAlive
import com.miguelangel.rickandmortyai.ui.theme.StatusDead
import com.miguelangel.rickandmortyai.ui.theme.StatusUnknown

@Composable
internal fun StatusChip(
    status: CharacterStatus,
    modifier: Modifier = Modifier,
) {
    val targetColor = when (status) {
        CharacterStatus.ALIVE -> StatusAlive
        CharacterStatus.DEAD -> StatusDead
        CharacterStatus.UNKNOWN -> StatusUnknown
    }
    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 400),
        label = "statusColor",
    )
    val label = when (status) {
        CharacterStatus.ALIVE -> stringResource(R.string.status_alive)
        CharacterStatus.DEAD -> stringResource(R.string.status_dead)
        CharacterStatus.UNKNOWN -> stringResource(R.string.status_unknown)
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(animatedColor.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Dot(color = animatedColor)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = animatedColor,
        )
    }
}

@Composable
private fun Dot(color: Color) {
    androidx.compose.foundation.layout.Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color),
    )
}
