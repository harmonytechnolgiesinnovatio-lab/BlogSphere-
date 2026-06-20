package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Article
import com.example.ui.components.*
import com.example.ui.viewmodel.BlogViewModel

@Composable
fun HomeScreen(
    viewModel: BlogViewModel,
    onNavigateToArticle: (Int) -> Unit,
    onNavigateToExplore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val featuredArticle by viewModel.featuredArticle.collectAsState()
    val trendingArticles by viewModel.trendingArticles.collectAsState()
    val filteredArticles by viewModel.filteredArticles.collectAsState()
    val currentCategory by viewModel.selectedCategory.collectAsState()
    val subscriberMsg by viewModel.newsletterMessage.collectAsState()

    val categoriesList = listOf("All", "Tech", "Design", "Growth", "Wellness")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // 1. Hero / Featured section
        featuredArticle?.let { article ->
            item {
                FeaturedHeroSection(
                    article = article,
                    onClick = { onNavigateToArticle(article.id) }
                )
            }
        }

        // 2. Categories scrollable selector
        item {
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text(
                    text = "Browse Categories",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(categoriesList) { category ->
                        val isSelected = currentCategory == category
                        FilterChip(
                            selected = isSelected,
                            onClick = { 
                                viewModel.setSelectedCategory(category)
                                onNavigateToExplore() 
                            },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }
        }

        // 3. Trending Articles Horizontal Band
        if (trendingArticles.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    SectionHeader(
                        title = "Trending on BlogSphere",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        trailingContent = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onNavigateToExplore() }
                            ) {
                                Text(
                                    "See all",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(trendingArticles) { article ->
                            TrendingItemCard(
                                article = article,
                                onClick = { onNavigateToArticle(article.id) }
                            )
                        }
                    }
                }
            }
        }

        // 4. Latest Articles section
        item {
            SectionHeader(
                title = "Latest Deep Insights",
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp)
            )
        }

        // Filter the feature to avoid duplication if preferred, but listing all is great
        val remainingArticles = filteredArticles.filter { it.id != (featuredArticle?.id ?: -1) }

        if (remainingArticles.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No other articles in this feed.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            // Adaptive Responsive grid-like representation using BoxWithConstraints inside screens
            item {
                BoxWithConstraints(modifier = Modifier.padding(horizontal = 16.dp)) {
                    val width = maxWidth
                    if (width > 600.dp) {
                        // Multi-column responsive grid layout manually implemented for compatibility
                        Column(modifier = Modifier.fillMaxWidth()) {
                            val chunks = remainingArticles.chunked(2)
                            chunks.forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    rowItems.forEach { article ->
                                        ArticleCard(
                                            article = article,
                                            onClick = { onNavigateToArticle(article.id) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size < 2) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    } else {
                        Column {
                            remainingArticles.forEach { article ->
                                ArticleCard(
                                    article = article,
                                    onClick = { onNavigateToArticle(article.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. Subscription Newsletter Box
        item {
            NewsletterWidget(
                subscribedMessage = subscriberMsg,
                onSubscribe = { viewModel.subscribeNewsletter(it) },
                onDismissMessage = { viewModel.clearNewsletterMessage() },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 6. Corporate Professional Footer
        item {
            BlogSphereFooter()
        }
    }
}

@Composable
fun FeaturedHeroSection(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(getAccentGradient())
                .clickable { onClick() }
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FEATURED COLUMN",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = article.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = 40.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = article.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "by ${article.authorName}",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp
                        )
                        Text(
                            text = article.publishedDate,
                            style = MaterialTheme.styleOfSubtitle(),
                            fontSize = 12.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primary)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Read Now",
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrendingItemCard(
    article: Article,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(280.dp)
            .height(160.dp),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = article.category.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )

                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Trending icon",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.authorName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${article.viewsCount} reads",
                    fontSize = 11.sp,
                    style = MaterialTheme.styleOfSubtitle()
                )
            }
        }
    }
}
