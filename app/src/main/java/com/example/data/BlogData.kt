package com.example.data

import android.content.Context
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

// ==========================================
// 1. Entities
// ==========================================

@Entity(tableName = "articles")
data class Article(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val content: String,
    val summary: String,
    val category: String,
    val tags: String, // Comma-separated
    val authorName: String,
    val authorAvatar: String, // Identifiers for styling
    val authorBio: String,
    val publishedDate: String,
    val viewsCount: Int = 0,
    val likesCount: Int = 0,
    val isFeatured: Boolean = false,
    val isTrending: Boolean = false,
    val imageUrl: String = "" // Optional url
)

@Entity(
    tableName = "comments",
    foreignKeys = [
        ForeignKey(
            entity = Article::class,
            parentColumns = ["id"],
            childColumns = ["articleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["articleId"])]
)
data class Comment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val articleId: Int,
    val authorName: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "newsletter_subscribers")
data class NewsletterSubscriber(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val email: String,
    val subscribedAt: Long = System.currentTimeMillis()
)

// ==========================================
// 2. Data Access Object (DAO)
// ==========================================

@Dao
interface BlogDao {
    @Query("SELECT * FROM articles ORDER BY id DESC")
    fun getAllArticles(): Flow<List<Article>>

    @Query("SELECT * FROM articles WHERE id = :id")
    fun getArticleById(id: Int): Flow<Article?>

    @Query("SELECT * FROM articles WHERE isFeatured = 1 LIMIT 1")
    fun getFeaturedArticle(): Flow<Article?>

    @Query("SELECT * FROM articles WHERE isTrending = 1 ORDER BY viewsCount DESC")
    fun getTrendingArticles(): Flow<List<Article>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: Article): Long

    @Update
    suspend fun updateArticle(article: Article)

    @Delete
    suspend fun deleteArticle(article: Article)

    // Comments query
    @Query("SELECT * FROM comments WHERE articleId = :articleId ORDER BY timestamp DESC")
    fun getCommentsForArticle(articleId: Int): Flow<List<Comment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertComment(comment: Comment): Long

    @Delete
    suspend fun deleteComment(comment: Comment)

    // Newsletter Subscribers
    @Query("SELECT * FROM newsletter_subscribers ORDER BY subscribedAt DESC")
    fun getAllSubscribers(): Flow<List<NewsletterSubscriber>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertSubscriber(subscriber: NewsletterSubscriber): Long
}

// ==========================================
// 3. Database & Seeding
// ==========================================

@Database(
    entities = [Article::class, Comment::class, NewsletterSubscriber::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun blogDao(): BlogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "blogsphere_database"
                )
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.blogDao())
                }
            }
        }

        private suspend fun populateDatabase(blogDao: BlogDao) {
            // Seed articles
            val art1Id = blogDao.insertArticle(
                Article(
                    title = "The Future of Jetpack Compose: Shaders and Canvas Magic",
                    category = "Tech",
                    tags = "Compose, Kotlin, Graphics, Shaders",
                    summary = "Leverage custom graphics pipelines and canvas manipulation to elevate your Android application design to the absolute highest level.",
                    content = """Jetpack Compose has completely revolutionized native Android UI development. But beyond simple buttons and lists lies a powerful, underutilized graphics suite. In Android 13 (API 33) and above, Compose added support for AGSL Runtime Shaders, enabling engineers to render highly complex fragment shaders directly inside any Composable view using Modifier.drawBehind.

In this deep dive, we will unpack how to:
1. Load a Custom Shader: Read AGSL code and bind variables like resolution, time, and touch vectors.
2. Optimize Recompositions: Utilize DrawScope drawing blocks (which skip layout and measurement phases entirely) to maintain a locking 60 FPS / 120 FPS frame rate on fluid screens.
3. Combine Canvas and Filters: Create dynamic backdrop blurs, organic liquid fluid morphs, and glowing neon visual grids dynamically.

Understanding structural performance triggers in Jetpack Compose is what separates standard widgets from high-fidelity premium digital layout craft. By offloading complex rendering computations directly into GPU pixel shading routines, you ensure your workspace behaves dynamically without dragging the main CPU thread down to garbage collection pauses.""",
                    authorName = "Sophia Vance",
                    authorAvatar = "sophia",
                    authorBio = "Sophia is a Principal UI Architect specializing in real-time GPU graphics pipelines and declarative systems engineering.",
                    publishedDate = "June 18, 2026",
                    viewsCount = 1520,
                    likesCount = 342,
                    isFeatured = true,
                    isTrending = true
                )
            )

            val art2Id = blogDao.insertArticle(
                Article(
                    title = "Designing for Deep Focus: Minimalist Interfaces That Convert",
                    category = "Design",
                    tags = "Design, UX, Minimalism, Mobile",
                    summary = "In an era of infinite scrolls and constant noise, designing interfaces that respect user attention spans is a rare and powerful superpower.",
                    content = """Look around your current screen setup. Modern digital environments are constantly competing for attention with aggressive notification banners, infinite-scroll feeds, auto-playing video drawers, and high-frequency tactile buzzes. As UI and UX professionals, the ultimate act of respect we can grant our end users is to design digital interfaces centered around "Deep Focus."

Minimalist typography and generous boundaries are not just stylistic markers; they are cognitive lifelines. By leaning heavily into visual symmetry, high contrast typography, and careful macro-spacing, you drastically reduce cognitive noise. 

Key architectural tactics:
- Generous Margin Grids: Adopt double the standard spacing margins (e.g., utilize 24dp or 32dp outer padding gutters) to separate interactive nodes.
- Typographic Hierarchy: Elevate display headings to massive sizes while keeping subtexts readable. Use high-contrast color choices instead of adding colored borders and boxes.
- Intentional Interaction Cycles: Remove all unsolicited background notifications or distracting motion. Only trigger animated visual cues when a user has completed a critical focus chain.

This professional design-led approach results in higher organic retention, pristine user emotional brand association, and exceptionally clear goal conversion metrics.""",
                    authorName = "Aris Thorne",
                    authorBio = "Aris Thorne is a Senior Product Designer focused on cognitive ergonomics, premium branding, and interaction typography.",
                    authorAvatar = "aris",
                    publishedDate = "June 15, 2026",
                    viewsCount = 840,
                    likesCount = 189,
                    isFeatured = false,
                    isTrending = true
                )
            )

            val art3Id = blogDao.insertArticle(
                Article(
                    title = "Scaling to First 100k Readers: Growth Playbook for Tech Publications",
                    category = "Growth",
                    tags = "Marketing, Growth, Audience, SEO",
                    summary = "An extremely granular, action-oriented playbook detailing the exact community and search strategies used to scale our platform.",
                    content = """Growing a tech publication to over 100,000 monthly active readers sounds like a daunting mountain, but the path is remarkably mechanical. It comes down to consistency, search engine visibility (SEO), product-market-content fit, and high-quality email retention loops.

Forget paid advertising campaigns. In 2026, the most resilient audience growth strategy is organic community-led compounding. Here is how we did it:

Phase 1: Deep Search Tuning (SEO)
Never write thin "news summary" columns. Write definitive evergreen reference blueprints. Google looks for "E-E-A-T" (Experience, Expertise, Authoritativeness, and Trustworthiness). An single 2500-word tutorial with structural blueprints, complete repository walk-throughs, and real edge-case handling is worth more than fifty generic summaries.

Phase 2: Newsletter-First Retention
Getting traffic is easy; keeping it is where the battle is won. Place sleek newsletter subscription cards on every readable surface. Promote premium exclusive insight updates to turn standard web visitors into daily email primary list subscribers.

Phase 3: Syndication Amplification
Package your long-form articles into micro-lessons. Syndicate them on HackerNews, specialized subreddits, Dev.to, and Medium, always linking back to your self-hosted canonical publication domain. This builds a robust backlink architecture that cements your authority.

With this structured playbook, you can turn a low-traffic playground into an authoritative industry-changing publishing powerhouse within six to twelve months.""",
                    authorName = "Olivia Chen",
                    authorBio = "Olivia leads audience growth for tech networks and develops automated content amplification pipelines.",
                    authorAvatar = "olivia",
                    publishedDate = "June 12, 2026",
                    viewsCount = 2450,
                    likesCount = 521,
                    isFeatured = false,
                    isTrending = true
                )
            )

            val art4Id = blogDao.insertArticle(
                Article(
                    title = "The Zen of Typing: Mechanical Keyboards and Developer Wellness",
                    category = "Wellness",
                    tags = "Wellness, Work, Keyboards, Health",
                    summary = "Configure your physical workspace setup, input accessories, and seating ergonomics to ensure optimal cognitive focus and physical longevity.",
                    content = """As developers, writers, and digital engineers, we spend the majority of our conscious life with our hands resting on a keyboard and our eyes tracking letters across pixel fields. Yet, physical ergonomics are often treated as an afterthought—until the sudden, sharp buzz of wrist strain or neck tension sets in.

True wellness begins with the primary physical instruments of our work: our keyboards, screens, chairs, and posture. 

Let's unpack the core ingredients of a long-term wellness environment:
1. Tactile Mechanical Inputs: Tactile switches (like tactile tactile browns or split layouts) provide a clear physical actuation point, preventing you from continually bottoming out keys with excessive mechanical force. This spares your fingers and forearms thousands of pounds of cumulative friction daily.
2. Split Coordinate Geometries: Standard row-staggered keyboards force your wrists to pronate inward. Placing a split ergonomic keyboard slightly wider than shoulder-width allows your chest to open up, neutralizing strain on your upper neck and spine.
3. Postural Transitions: A high-density standing desk coupled with timed mobility stretching is non-negotiable. Commit to a 5-minute stretch cycle for every 90 minutes of active focused keyboard writing.

Investing in your physical workspace isn't a mechanical hobbyist indulgence; it is a vital buffer that guarantees professional longevity and preserves your physical health.""",
                    authorName = "Dr. Keanu Reyes",
                    authorBio = "Dr. Keanu Reyes is an occupational therapist and workstation consultant specializing in office ergonomic interventions.",
                    authorAvatar = "keanu",
                    publishedDate = "June 10, 2026",
                    viewsCount = 412,
                    likesCount = 98,
                    isFeatured = false,
                    isTrending = false
                )
            )

            // Seed initial comments
            blogDao.insertComment(
                Comment(
                    articleId = art1Id.toInt(),
                    authorName = "Jordan Kyle",
                    content = "This was incredibly helpful! I've been struggling to optimize shaders on Compose, and drawBehind solved all my framing issues."
                )
            )
            blogDao.insertComment(
                Comment(
                    articleId = art1Id.toInt(),
                    authorName = "Elena Rostova",
                    content = "Absolutely stunning design! Looking forward to the next article!"
                )
            )
            blogDao.insertComment(
                Comment(
                    articleId = art2Id.toInt(),
                    authorName = "Marcus Aurelius",
                    content = "Pruning the noise is so essential. As designers, we must learn to say 'no' to feature creep."
                )
            )
        }
    }
}

// ==========================================
// 4. Repository (Concrete Implementation)
// ==========================================

class BlogRepository(private val blogDao: BlogDao) {
    val allArticles: Flow<List<Article>> = blogDao.getAllArticles()
    val featuredArticle: Flow<Article?> = blogDao.getFeaturedArticle()
    val trendingArticles: Flow<List<Article>> = blogDao.getTrendingArticles()
    val newsletterSubscribers: Flow<List<NewsletterSubscriber>> = blogDao.getAllSubscribers()

    fun getArticleById(id: Int): Flow<Article?> {
        return blogDao.getArticleById(id)
    }

    fun getCommentsForArticle(articleId: Int): Flow<List<Comment>> {
        return blogDao.getCommentsForArticle(articleId)
    }

    suspend fun insertArticle(article: Article): Int {
        return blogDao.insertArticle(article).toInt()
    }

    suspend fun updateArticle(article: Article) {
        blogDao.updateArticle(article)
    }

    suspend fun deleteArticle(article: Article) {
        blogDao.deleteArticle(article)
    }

    suspend fun insertComment(comment: Comment) {
        blogDao.insertComment(comment)
    }

    suspend fun deleteComment(comment: Comment) {
        blogDao.deleteComment(comment)
    }

    suspend fun subscribeNewsletter(email: String): Boolean {
        if (email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return false
        }
        val id = blogDao.insertSubscriber(NewsletterSubscriber(email = email.trim()))
        return id != -1L
    }
}
