package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Article
import com.example.data.Comment
import com.example.data.BlogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BlogViewModel(private val repository: BlogRepository) : ViewModel() {

    // 1. Search and Filtering States
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory = _selectedCategory.asStateFlow()

    // 2. Base Data Streams from Repository
    val allArticles: StateFlow<List<Article>> = repository.allArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredArticle: StateFlow<Article?> = repository.featuredArticle
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val trendingArticles: StateFlow<List<Article>> = repository.trendingArticles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscribers = repository.newsletterSubscribers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 3. Dynamic Filtered Articles Pipeline
    val filteredArticles: StateFlow<List<Article>> = combine(
        allArticles,
        _searchQuery,
        _selectedCategory
    ) { articles, query, category ->
        articles.filter { article ->
            val matchesCategory = category == "All" || article.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isBlank() || 
                article.title.contains(query, ignoreCase = true) ||
                article.summary.contains(query, ignoreCase = true) ||
                article.content.contains(query, ignoreCase = true) ||
                article.tags.contains(query, ignoreCase = true) ||
                article.authorName.contains(query, ignoreCase = true)
            
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 4. Active Reading & Comment Pipeline
    private val _activeArticleId = MutableStateFlow<Int?>(null)
    val activeArticleId = _activeArticleId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeArticle: StateFlow<Article?> = _activeArticleId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else repository.getArticleById(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val activeComments: StateFlow<List<Comment>> = _activeArticleId
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else repository.getCommentsForArticle(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // 5. User Interaction Operations
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setActiveArticle(id: Int?) {
        _activeArticleId.value = id
        if (id != null) {
            // Track view increment
            viewModelScope.launch {
                allArticles.value.firstOrNull { it.id == id }?.let { article ->
                    repository.updateArticle(article.copy(viewsCount = article.viewsCount + 1))
                }
            }
        }
    }

    fun toggleLikeArticle(id: Int) {
        viewModelScope.launch {
            allArticles.value.firstOrNull { it.id == id }?.let { article ->
                val alreadyLiked = false // Simple mock check, could be persisted
                val difference = if (alreadyLiked) -1 else 1
                repository.updateArticle(article.copy(likesCount = article.likesCount + difference))
            }
        }
    }

    // 6. Comments Engine
    fun addComment(articleId: Int, author: String, text: String, onFinished: () -> Unit = {}) {
        if (author.isBlank() || text.isBlank()) return
        viewModelScope.launch {
            repository.insertComment(
                Comment(
                    articleId = articleId,
                    authorName = author.trim(),
                    content = text.trim()
                )
            )
            onFinished()
        }
    }

    // 7. Newsletter Subscription Flow
    private val _newsletterMessage = MutableStateFlow<String?>(null)
    val newsletterMessage = _newsletterMessage.asStateFlow()

    fun subscribeNewsletter(email: String) {
        viewModelScope.launch {
            val success = repository.subscribeNewsletter(email)
            if (success) {
                _newsletterMessage.value = "Subscription Successful! Welcome aboard our circle."
            } else {
                _newsletterMessage.value = "Please check your email address format."
            }
        }
    }

    fun clearNewsletterMessage() {
        _newsletterMessage.value = null
    }

    // 8. Admin CRUD Actions
    fun createArticle(
        title: String,
        content: String,
        summary: String,
        category: String,
        tags: String,
        authorName: String,
        authorBio: String,
        isFeatured: Boolean,
        isTrending: Boolean,
        imageUrl: String = "",
        onCompleted: (Int) -> Unit
    ) {
        viewModelScope.launch {
            val newId = repository.insertArticle(
                Article(
                    title = title.trim(),
                    content = content.trim(),
                    summary = summary.trim(),
                    category = category.trim(),
                    tags = tags.trim(),
                    authorName = authorName.trim(),
                    authorBio = authorBio.trim(),
                    authorAvatar = "generic",
                    publishedDate = "Just Now",
                    isFeatured = isFeatured,
                    isTrending = isTrending,
                    viewsCount = 1,
                    likesCount = 0,
                    imageUrl = imageUrl
                )
            )
            onCompleted(newId)
        }
    }

    fun updateArticle(
        id: Int,
        title: String,
        content: String,
        summary: String,
        category: String,
        tags: String,
        authorName: String,
        authorBio: String,
        isFeatured: Boolean,
        isTrending: Boolean,
        imageUrl: String = "",
        onCompleted: () -> Unit
    ) {
        viewModelScope.launch {
            allArticles.value.firstOrNull { it.id == id }?.let { existing ->
                repository.updateArticle(
                    existing.copy(
                        title = title.trim(),
                        content = content.trim(),
                        summary = summary.trim(),
                        category = category.trim(),
                        tags = tags.trim(),
                        authorName = authorName.trim(),
                        authorBio = authorBio.trim(),
                        isFeatured = isFeatured,
                        isTrending = isTrending,
                        imageUrl = imageUrl
                    )
                )
                onCompleted()
            }
        }
    }

    fun deleteArticle(articleId: Int, onCompleted: () -> Unit = {}) {
        viewModelScope.launch {
            allArticles.value.firstOrNull { it.id == articleId }?.let { existing ->
                repository.deleteArticle(existing)
                if (_activeArticleId.value == articleId) {
                    _activeArticleId.value = null
                }
                onCompleted()
            }
        }
    }
}

// Factory to inject repository
class BlogViewModelFactory(private val repository: BlogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BlogViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BlogViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
