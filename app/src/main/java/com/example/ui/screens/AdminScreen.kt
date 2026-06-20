package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Article
import com.example.ui.components.styleOfSubtitle
import com.example.ui.viewmodel.BlogViewModel

enum class AdminState {
    LIST, CREATE_FORM, EDIT_FORM
}

@Composable
fun AdminScreen(
    viewModel: BlogViewModel,
    modifier: Modifier = Modifier
) {
    var currentState by remember { mutableStateOf(AdminState.LIST) }
    var selectedArticleForEdit by remember { mutableStateOf<Article?>(null) }

    // Form Field States
    var formTitle by remember { mutableStateOf("") }
    var formSummary by remember { mutableStateOf("") }
    var formContent by remember { mutableStateOf("") }
    var formCategory by remember { mutableStateOf("Tech") }
    var formTags by remember { mutableStateOf("Compose, Tech") }
    var formAuthorName by remember { mutableStateOf("Tanmay Kashyap") }
    var formAuthorBio by remember { mutableStateOf("Founder of Harmony Technologies Innovation Pvt Ltd") }
    var formIsFeatured by remember { mutableStateOf(false) }
    var formIsTrending by remember { mutableStateOf(false) }

    val articles by viewModel.allArticles.collectAsState()
    val subscribers by viewModel.subscribers.collectAsState()

    // Helper to load article details into form
    fun loadFormForEdit(article: Article) {
        selectedArticleForEdit = article
        formTitle = article.title
        formSummary = article.summary
        formContent = article.content
        formCategory = article.category
        formTags = article.tags
        formAuthorName = article.authorName
        formAuthorBio = article.authorBio
        formIsFeatured = article.isFeatured
        formIsTrending = article.isTrending
        currentState = AdminState.EDIT_FORM
    }

    // Reset Form Fields
    fun resetFormFieldStates() {
        selectedArticleForEdit = null
        formTitle = ""
        formSummary = ""
        formContent = ""
        formCategory = "Tech"
        formTags = "Compose, Tech"
        formAuthorName = "Tanmay Kashyap"
        formAuthorBio = "Founder of Harmony Technologies Innovation Pvt Ltd"
        formIsFeatured = false
        formIsTrending = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (currentState) {
            AdminState.LIST -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 80.dp)
                ) {
                    item {
                        Column(modifier = Modifier.padding(vertical = 16.dp)) {
                            Text(
                                text = "Admin Control Suite",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Publish columns, monitor reads, and handle local SQLite subscribers.",
                                style = MaterialTheme.styleOfSubtitle(),
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Quick Stats strip
                    item {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                        ) {
                            StatCard(
                                title = "Articles Count",
                                value = articles.size.toString(),
                                icon = Icons.Default.Article,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Subscribers",
                                value = subscribers.size.toString(),
                                icon = Icons.Default.Email,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    item {
                        Text(
                            text = "Published Articles (${articles.size})",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }

                    if (articles.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No articles in database yet. Click the + button to seed!")
                            }
                        }
                    } else {
                        items(articles) { article ->
                            AdminArticleRow(
                                article = article,
                                onEdit = { loadFormForEdit(article) },
                                onDelete = { viewModel.deleteArticle(article.id) }
                            )
                        }
                    }

                    // Newsletter subscriber index
                    if (subscribers.isNotEmpty()) {
                        item {
                            Text(
                                text = "Active Mailing List Contacts",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp)
                            )
                        }

                        items(subscribers) { sub ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MarkEmailRead,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = sub.email,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Add FAB button
                FloatingActionButton(
                    onClick = {
                        resetFormFieldStates()
                        currentState = AdminState.CREATE_FORM
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create new Column article"
                    )
                }
            }

            AdminState.CREATE_FORM, AdminState.EDIT_FORM -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentState = AdminState.LIST }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Cancel form")
                        }
                        Text(
                            text = if (currentState == AdminState.CREATE_FORM) "Publish New Insights" else "Refactor Column Code",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Form Fields
                    OutlinedTextField(
                        value = formTitle,
                        onValueChange = { formTitle = it },
                        label = { Text("Article Title") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = formSummary,
                        onValueChange = { formSummary = it },
                        label = { Text("Short Excerpt / Summary") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = formContent,
                        onValueChange = { formContent = it },
                        label = { Text("Entire Article Content Body") },
                        minLines = 6,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    // Category dropdown or choices chips selector
                    Text(
                        text = "CHOOSE CATEGORY:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                    )
                    
                    val possibleCats = listOf("Tech", "Design", "Growth", "Wellness")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        possibleCats.forEach { cat ->
                            val selected = formCategory == cat
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { formCategory = cat }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = formTags,
                        onValueChange = { formTags = it },
                        label = { Text("Tags (Comma Separated, e.g. UI, UX, Web)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = formAuthorName,
                        onValueChange = { formAuthorName = it },
                        label = { Text("Author Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    OutlinedTextField(
                        value = formAuthorBio,
                        onValueChange = { formAuthorBio = it },
                        label = { Text("Author Biography") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Live SEO health optimizer preview
                    AdminSeoPreviewPanel(
                        title = formTitle,
                        summary = formSummary,
                        category = formCategory,
                        tags = formTags
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Promotion Toggles
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column {
                            Text("Featured Article", fontWeight = FontWeight.Bold)
                            Text("Show prominently as primary top hero column banner.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = formIsFeatured,
                            onCheckedChange = { formIsFeatured = it }
                        )
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Column {
                            Text("Trending List Spot", fontWeight = FontWeight.Bold)
                            Text("Renders inside home swipeable trending carousel row.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = formIsTrending,
                            onCheckedChange = { formIsTrending = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Save Buttons
                    Button(
                        onClick = {
                            if (formTitle.isNotBlank() && formContent.isNotBlank()) {
                                if (currentState == AdminState.CREATE_FORM) {
                                    viewModel.createArticle(
                                        title = formTitle,
                                        content = formContent,
                                        summary = formSummary,
                                        category = formCategory,
                                        tags = formTags,
                                        authorName = formAuthorName,
                                        authorBio = formAuthorBio,
                                        isFeatured = formIsFeatured,
                                        isTrending = formIsTrending
                                    ) {
                                        resetFormFieldStates()
                                        currentState = AdminState.LIST
                                    }
                                } else {
                                    selectedArticleForEdit?.let { existing ->
                                        viewModel.updateArticle(
                                            id = existing.id,
                                            title = formTitle,
                                            content = formContent,
                                            summary = formSummary,
                                            category = formCategory,
                                            tags = formTags,
                                            authorName = formAuthorName,
                                            authorBio = formAuthorBio,
                                            isFeatured = formIsFeatured,
                                            isTrending = formIsTrending
                                        ) {
                                            resetFormFieldStates()
                                            currentState = AdminState.LIST
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentState == AdminState.CREATE_FORM) "Publish Article" else "Refactor Column Details",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            resetFormFieldStates()
                            currentState = AdminState.LIST
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Discard Draft Changes")
                    }

                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun AdminArticleRow(
    article: Article,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Tags
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = article.category.uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (article.isFeatured) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFEAB308))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("Featured", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (article.isTrending) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFF0D9488))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text("Trending", fontSize = 9.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = article.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Reads: ${article.viewsCount} • Likes: ${article.likesCount}",
                    style = MaterialTheme.styleOfSubtitle(),
                    fontSize = 11.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit article detail",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete article",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AdminSeoPreviewPanel(
    title: String,
    summary: String,
    category: String,
    tags: String
) {
    val metaTags = remember(title, summary, category, tags) {
        com.example.util.SeoMetadataHelper.generateMetaTags(title, summary, category, tags)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DoneAll,
                        contentDescription = "SEO Health Indicator",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Real-time SEO Optimizer",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when {
                                metaTags.seoScore >= 90 -> Color(0xFF10B981)
                                metaTags.seoScore >= 70 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Score: ${metaTags.seoScore}/100",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (metaTags.suggestions.isNotEmpty()) {
                Text(
                    text = "CRITICAL INSIGHTS FOR PUBLISHING:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
                metaTags.suggestions.forEach { sug ->
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚡",
                            fontSize = 11.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = sug,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(text = "✅", fontSize = 14.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Excellent! Metadata meets highest compliance standards.",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF10B981)
                    )
                }
            }
        }
    }
}
