package org.hogwarts.android.feature.subjects.ui.components

import coil.request.ImageRequest
import coil.decode.SvgDecoder
import coil.compose.AsyncImage
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.hogwarts.android.feature.subjects.R
import org.hogwarts.android.feature.subjects.domain.model.QuestionStats

/**
 * Fixed order + colors from QUESTION_TYPE_CONFIG in
 * catalog-content-sections.tsx lines 141–185.
 */
private val QUESTION_TYPE_PIPELINE = listOf(
    "MULTIPLE_CHOICE" to Color(0xFF4B976A),
    "TRUE_FALSE" to Color(0xFFCF6E30),
    "SHORT_ANSWER" to Color(0xFFD25E8C),
    "ESSAY" to Color(0xFF2C70B2),
    "FILL_BLANK" to Color(0xFF825BA3),
    "MATCHING" to Color(0xFF57908C),
    "ORDERING" to Color(0xFFD85E4C),
    "MULTI_SELECT" to Color(0xFFA14B46),
)

/** Each type's artwork, laid over its colour at 30% — the web's `config.svg`. */
private val QUESTION_TYPE_ART = mapOf(
    "MULTIPLE_CHOICE" to "https://cdn.databayt.org/hogwarts/multiple-choice.svg",
    "TRUE_FALSE" to "https://cdn.databayt.org/hogwarts/true-false.svg",
    "SHORT_ANSWER" to "https://cdn.databayt.org/hogwarts/short-answer.svg",
    "ESSAY" to "https://cdn.databayt.org/hogwarts/essay.svg",
    "FILL_BLANK" to "https://cdn.databayt.org/hogwarts/fill-in-blank.svg",
    "MATCHING" to "https://cdn.databayt.org/hogwarts/matching.svg",
    "ORDERING" to "https://cdn.databayt.org/hogwarts/ordering.svg",
    "MULTI_SELECT" to "https://cdn.databayt.org/hogwarts/multi-select.svg",
)

private val HEADER_CREAM = Color(0xFFF4F1D0)
private val HEADER_INK = Color(0xFF212222)

@Composable
fun QBankSection(
    stats: QuestionStats,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = QUESTION_TYPE_PIPELINE, key = { it.first }) { (type, color) ->
            val card = stats.cards.firstOrNull { it.type == type }
            QuestionTypeTile(
                type = type,
                color = color,
                count = card?.count ?: 0,
            )
        }
    }
}

@Composable
private fun QuestionTypeTile(
    type: String,
    color: Color,
    count: Int,
) {
    // `rounded-xl`, 245 wide, a 250dp frame under the head — measured off
    // catalog-content-sections.tsx.
    val shape = RoundedCornerShape(14.dp)
    Surface(
        modifier = Modifier
            .width(245.dp)
            .clip(shape),
        shape = shape,
        color = Color.Transparent,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Cream header strip with type name
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HEADER_CREAM)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
            ) {
                Text(
                    text = stringResource(questionTypeLabelRes(type)),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.35).sp,
                    color = HEADER_INK,
                )
            }
            // Colored body with count pill + description
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .background(color),
            ) {
                QUESTION_TYPE_ART[type]?.let { art ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(art)
                            .decoderFactory(SvgDecoder.Factory())
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().alpha(0.3f),
                    )
                }
                // Count pill at top-end
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(percent = 50))
                        .background(HEADER_CREAM.copy(alpha = 0.8f))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = count.toString(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = HEADER_INK,
                    )
                }
                // Divider + description at bottom
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 14.dp, end = 14.dp, bottom = 24.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .width(32.dp)
                            .height(1.5.dp)
                            .background(HEADER_CREAM),
                    )
                    Spacer(Modifier.size(10.dp))
                    Text(
                        text = stringResource(questionTypeDescRes(type)),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        lineHeight = 19.5.sp,
                        color = HEADER_CREAM,
                    )
                }
            }
        }
    }
}

private fun questionTypeLabelRes(type: String): Int = when (type) {
    "MULTIPLE_CHOICE" -> R.string.subjects_q_type_multiple_choice
    "TRUE_FALSE" -> R.string.subjects_q_type_true_false
    "SHORT_ANSWER" -> R.string.subjects_q_type_short_answer
    "ESSAY" -> R.string.subjects_q_type_essay
    "FILL_BLANK" -> R.string.subjects_q_type_fill_blank
    "MATCHING" -> R.string.subjects_q_type_matching
    "ORDERING" -> R.string.subjects_q_type_ordering
    "MULTI_SELECT" -> R.string.subjects_q_type_multi_select
    else -> R.string.subjects_q_type_multiple_choice
}

private fun questionTypeDescRes(type: String): Int = when (type) {
    "MULTIPLE_CHOICE" -> R.string.subjects_q_desc_multiple_choice
    "TRUE_FALSE" -> R.string.subjects_q_desc_true_false
    "SHORT_ANSWER" -> R.string.subjects_q_desc_short_answer
    "ESSAY" -> R.string.subjects_q_desc_essay
    "FILL_BLANK" -> R.string.subjects_q_desc_fill_blank
    "MATCHING" -> R.string.subjects_q_desc_matching
    "ORDERING" -> R.string.subjects_q_desc_ordering
    "MULTI_SELECT" -> R.string.subjects_q_desc_multi_select
    else -> R.string.subjects_q_desc_multiple_choice
}
