package com.example.util

data class SeoMetaTags(
    val title: String,
    val description: String,
    val keywords: String,
    val ogTitle: String,
    val ogDescription: String,
    val ogUrl: String,
    val ogType: String = "article",
    val twitterCard: String = "summary_large_image",
    val canonicalUrl: String,
    val rawHtml: String,
    val seoScore: Int,
    val suggestions: List<String>
)

object SeoMetadataHelper {
    
    /**
     * Generates SEO-optimized meta tag details and calculates an audit index score.
     */
    fun generateMetaTags(title: String, summary: String, category: String, tags: String): SeoMetaTags {
        val cleanTitle = title.trim()
        val cleanSummary = summary.trim()
        
        // 1. Core Description Limit (SEO best practice is ~150-160 chars)
        val optimalDescription = if (cleanSummary.length > 155) {
            cleanSummary.substring(0, 152) + "..."
        } else if (cleanSummary.isEmpty()) {
            "Explore amazing technical perspectives and premium design systems on BlogSphere."
        } else {
            cleanSummary
        }

        // 2. Keywords Generator
        val parsedTags = tags.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        val keywordsList = (listOf(category, "BlogSphere", "Harmony Technologies") + parsedTags)
            .distinct()
        val keywordString = keywordsList.joinToString(", ")

        // 3. SEO Friendly Url slug generator
        val slug = cleanTitle.lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "") // strip punctuation
            .replace(Regex("\\s+"), "-")         // spaces to hyphens
            .trim('-')
        val canonical = "https://blogsphere.harmonytechnologies.com/posts/$slug"

        // 4. Raw HTML block builder
        val rawHtmlBlock = """
            |<title>$cleanTitle | BlogSphere</title>
            |<meta name="description" content="$optimalDescription" />
            |<meta name="keywords" content="$keywordString" />
            |<link rel="canonical" href="$canonical" />
            |
            |<!-- Open Graph / Rich Social Cards -->
            |<meta property="og:type" content="article" />
            |<meta property="og:title" content="$cleanTitle" />
            |<meta property="og:description" content="$optimalDescription" />
            |<meta property="og:url" content="$canonical" />
            |<meta property="og:site_name" content="BlogSphere" />
            |
            |<!-- Twitter Summary Card -->
            |<meta name="twitter:card" content="summary_large_image" />
            |<meta name="twitter:title" content="$cleanTitle" />
            |<meta name="twitter:description" content="$optimalDescription" />
        """.trimMargin()

        // 5. Calculate SEO Audit parameters
        val auditSuggestions = mutableListOf<String>()
        var score = 100

        if (cleanTitle.length < 20) {
            score -= 15
            auditSuggestions.add("Title is too brief (less than 20 chars). Expand to target precise search queries.")
        } else if (cleanTitle.length > 70) {
            score -= 10
            auditSuggestions.add("Title exceeds 70 characters. It might get clipped on Google search results.")
        }

        if (cleanSummary.isEmpty()) {
            score -= 30
            auditSuggestions.add("Missing content summary. Search engines rely heavily on article summaries.")
        } else if (cleanSummary.length < 100) {
            score -= 10
            auditSuggestions.add("Summary is quite short. Ideal summaries are between 110 and 160 characters to capture user intents.")
        }

        if (parsedTags.size < 3) {
            score -= 10
            auditSuggestions.add("Add at least three tags to build dynamic SEO entity graphs for other indices.")
        }

        if (score < 40) score = 40

        return SeoMetaTags(
            title = "$cleanTitle | BlogSphere",
            description = optimalDescription,
            keywords = keywordString,
            ogTitle = cleanTitle,
            ogDescription = optimalDescription,
            ogUrl = canonical,
            canonicalUrl = canonical,
            rawHtml = rawHtmlBlock,
            seoScore = score,
            suggestions = auditSuggestions
        )
    }
}
