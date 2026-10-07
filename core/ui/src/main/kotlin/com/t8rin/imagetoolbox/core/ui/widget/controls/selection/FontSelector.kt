/*
 * Pineapple Art font picker (extended from Image Toolbox by T8RIN, Apache-2.0).
 * Adds search, Arabic/Latin/imported/favorites categories, recently used,
 * bilingual preview and persistent favorites — upstream behavior preserved.
 */

package com.t8rin.imagetoolbox.core.ui.widget.controls.selection

import android.content.Context
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.rememberScrollState
import com.t8rin.imagetoolbox.core.resources.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.t8rin.imagetoolbox.core.resources.R
import com.t8rin.imagetoolbox.core.resources.icons.KeyboardArrowDown
import com.t8rin.imagetoolbox.core.resources.icons.Search
import com.t8rin.imagetoolbox.core.resources.icons.Star
import com.t8rin.imagetoolbox.core.resources.icons.TextFields
import com.t8rin.imagetoolbox.core.settings.presentation.model.UiFontFamily
import com.t8rin.imagetoolbox.core.ui.theme.ProvideTypography
import com.t8rin.imagetoolbox.core.ui.utils.helper.AppToastHost
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedBadge
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedChip
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.EnhancedIconButton
import com.t8rin.imagetoolbox.core.ui.widget.enhanced.enhancedFlingBehavior
import com.t8rin.imagetoolbox.core.ui.widget.modifier.ShapeDefaults
import com.t8rin.imagetoolbox.core.ui.widget.modifier.container
import com.t8rin.imagetoolbox.core.ui.widget.modifier.fadingEdges
import com.t8rin.imagetoolbox.core.ui.widget.modifier.scaleOnTap
import com.t8rin.imagetoolbox.core.ui.widget.text.AutoSizeText
import com.t8rin.imagetoolbox.core.ui.widget.text.TitleItem
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.pineappleFontPrefs by preferencesDataStore("pineapple_font_prefs")
private val FavoriteFontsKey = stringSetPreferencesKey("favorite_fonts")
private val RecentFontsKey = stringPreferencesKey("recent_fonts")

private const val PineapplePreviewText = "باينابل آرت • Pineapple Art"

@Composable
fun FontSelector(
    value: UiFontFamily,
    onValueChange: (UiFontFamily) -> Unit,
    modifier: Modifier = Modifier,
    title: String = stringResource(R.string.font),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    shape: Shape = ShapeDefaults.large,
    behaveAsContainer: Boolean = true
) {
    Column(
        modifier = modifier.then(
            if (behaveAsContainer) {
                Modifier.container(
                    shape = shape,
                    color = containerColor
                )
            } else Modifier
        )
    ) {
        val fonts = UiFontFamily.entries
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val prefs = remember(context) { context.pineappleFontPrefs }

        val favorites by remember(prefs) {
            prefs.data.map { it[FavoriteFontsKey] ?: emptySet() }
        }.collectAsState(initial = emptySet())
        val recentNames by remember(prefs) {
            prefs.data.map {
                it[RecentFontsKey].orEmpty().split("").filter { name ->
                    name.isNotEmpty()
                }
            }
        }.collectAsState(initial = emptyList())

        fun fontKey(font: UiFontFamily): String = font.name ?: "system"

        fun selectFont(font: UiFontFamily) {
            onValueChange(font)
            scope.launch {
                prefs.edit { settings ->
                    val current = settings[RecentFontsKey].orEmpty()
                        .split("").filter { it.isNotEmpty() }.toMutableList()
                    current.remove(fontKey(font))
                    current.add(0, fontKey(font))
                    settings[RecentFontsKey] = current.take(5).joinToString("")
                }
            }
        }

        fun toggleFavorite(font: UiFontFamily) {
            scope.launch {
                prefs.edit { settings ->
                    val current = settings[FavoriteFontsKey] ?: emptySet()
                    val key = fontKey(font)
                    settings[FavoriteFontsKey] = if (key in current) current - key else current + key
                }
            }
        }

        var expanded by rememberSaveable { mutableStateOf(false) }
        var query by rememberSaveable { mutableStateOf("") }
        var category by rememberSaveable { mutableStateOf(0) }

        val arabicFonts = remember {
            setOf(
                UiFontFamily.NotoSansArabic,
                UiFontFamily.NotoKufiArabic,
                UiFontFamily.NotoNaskhArabic
            )
        }
        val latinFonts = remember {
            setOf(UiFontFamily.NotoSans, UiFontFamily.NotoSerif)
        }

        val categoryTitles = listOf(
            stringResource(R.string.font_category_all),
            stringResource(R.string.font_category_arabic),
            stringResource(R.string.font_category_latin),
            stringResource(R.string.font_category_imported),
            stringResource(R.string.font_favorites)
        )

        val visibleFonts = remember(fonts, query, category, favorites) {
            val q = query.trim().lowercase()
            fonts.filter { font ->
                val matchesCategory = when (category) {
                    1 -> font in arabicFonts
                    2 -> font in latinFonts
                    3 -> font is UiFontFamily.Custom
                    4 -> fontKey(font) in favorites
                    else -> true
                }
                val matchesQuery = q.isEmpty() ||
                    (font.name ?: "system").lowercase().contains(q)
                matchesCategory && matchesQuery
            }
        }
        val recentFonts = remember(fonts, recentNames) {
            recentNames.mapNotNull { name ->
                fonts.find { fontKey(it) == name }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            val rotation by animateFloatAsState(if (expanded) 180f else 0f)
            TitleItem(
                text = title,
                icon = if (behaveAsContainer) Icons.Rounded.TextFields else null,
                modifier = Modifier.padding(top = 12.dp, start = 12.dp, bottom = 8.dp)
            )
            EnhancedBadge(
                content = {
                    Text(fonts.size.toString())
                },
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier
                    .padding(horizontal = 2.dp)
                    .padding(bottom = 12.dp)
                    .scaleOnTap {
                        AppToastHost.showConfetti()
                    }
            )
            Spacer(modifier = Modifier.weight(1f))
            EnhancedIconButton(
                containerColor = Color.Transparent,
                onClick = { expanded = !expanded }
            ) {
                Icon(
                    imageVector = Icons.Rounded.KeyboardArrowDown,
                    contentDescription = "Expand",
                    modifier = Modifier.rotate(rotation)
                )
            }
        }
        if (expanded) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                placeholder = { Text(stringResource(R.string.font_search_hint)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null
                    )
                },
                singleLine = true,
                shape = ShapeDefaults.default
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categoryTitles.forEachIndexed { index, label ->
                    EnhancedChip(
                        selected = category == index,
                        onClick = { category = index },
                        selectedColor = MaterialTheme.colorScheme.secondary,
                        contentPadding = PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        )
                    ) {
                        Text(label)
                    }
                }
            }
            if (recentFonts.isNotEmpty() && query.isBlank() && category == 0) {
                Text(
                    text = stringResource(R.string.font_recent),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentFonts.forEach { font ->
                        ProvideTypography(font) {
                            EnhancedChip(
                                selected = font == value,
                                onClick = { selectFont(font) },
                                selectedColor = MaterialTheme.colorScheme.secondary,
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                )
                            ) {
                                AutoSizeText(
                                    text = font.name ?: stringResource(id = R.string.system),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
            ProvideTypography(value) {
                AutoSizeText(
                    text = PineapplePreviewText,
                    maxLines = 2,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
        val state = rememberLazyStaggeredGridState()
        LazyHorizontalStaggeredGrid(
            verticalArrangement = Arrangement.spacedBy(
                space = 8.dp,
                alignment = Alignment.CenterVertically
            ),
            state = state,
            horizontalItemSpacing = 8.dp,
            rows = StaggeredGridCells.Adaptive(30.dp),
            modifier = Modifier
                .heightIn(max = animateDpAsState(if (expanded) 150.dp else 52.dp).value)
                .fadingEdges(
                    scrollableState = state,
                    isVertical = false,
                    spanCount = 3
                ),
            contentPadding = PaddingValues(8.dp),
            flingBehavior = enhancedFlingBehavior()
        ) {
            items(visibleFonts) { font ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    ProvideTypography(font) {
                        EnhancedChip(
                            selected = font == value,
                            onClick = {
                                selectFont(font)
                            },
                            selectedColor = MaterialTheme.colorScheme.secondary,
                            contentPadding = PaddingValues(
                                horizontal = 12.dp,
                                vertical = 8.dp
                            ),
                            modifier = Modifier.height(36.dp)
                        ) {
                            AutoSizeText(
                                text = font.name
                                    ?: stringResource(id = R.string.system),
                                maxLines = 1
                            )
                        }
                    }
                    if (expanded) {
                        val isFavorite = fontKey(font) in favorites
                        EnhancedIconButton(
                            containerColor = Color.Transparent,
                            onClick = { toggleFavorite(font) },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Rounded.Star else Icons.Outlined.Star,
                                contentDescription = stringResource(R.string.font_favorites),
                                tint = if (isFavorite) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
