package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StreamHeroSection(
    title: String,
    tagline: String,
    exploreLabel: String,
    learningLabel: String,
    showLearningCta: Boolean,
    onExplore: () -> Unit,
    onMyLearning: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        EducationAnimation(
            modifier = Modifier
                .weight(1f)
                .height(220.dp)
        )

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 54.sp,
                    lineHeight = 54.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-2).sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = tagline,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Start
            )
            Spacer(Modifier.height(16.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroCtaButton(
                    text = exploreLabel,
                    onClick = onExplore,
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
                if (showLearningCta) {
                    HeroCtaButton(
                        text = learningLabel,
                        onClick = onMyLearning,
                        containerColor = HeroFavBackground,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

private val HeroFavBackground = Color(0xFFE5E5E7)

@Composable
private fun HeroCtaButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.height(44.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
