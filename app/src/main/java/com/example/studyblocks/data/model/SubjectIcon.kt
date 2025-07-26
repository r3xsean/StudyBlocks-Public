package com.example.studyblocks.data.model

enum class SubjectIcon(
    val emoji: String, 
    val keywords: List<String>,
    val category: IconCategory = IconCategory.GENERAL,
    val displayName: String = ""
) {
    // STEM - Mathematics
    MATH("🧮", listOf("mathematics", "math", "maths", "calculus", "algebra", "geometry", "trigonometry", "arithmetic", "statistics", "probability"), IconCategory.STEM, "Mathematics"),
    ALGEBRA("📐", listOf("algebra", "algebraic", "linear algebra", "abstract algebra", "equations", "variables"), IconCategory.STEM, "Algebra"),
    GEOMETRY("📏", listOf("geometry", "geometric", "shapes", "angles", "triangles", "circles", "euclidean"), IconCategory.STEM, "Geometry"),
    STATISTICS("📊", listOf("statistics", "stats", "data analysis", "probability", "regression", "hypothesis", "statistical"), IconCategory.STEM, "Statistics"),
    CALCULUS("∫", listOf("calculus", "derivatives", "integrals", "differential", "integral", "limits", "analysis"), IconCategory.STEM, "Calculus"),
    
    // STEM - Sciences
    PHYSICS("⚡", listOf("physics", "mechanics", "thermodynamics", "electricity", "magnetism", "quantum", "relativity", "optics"), IconCategory.STEM, "Physics"),
    CHEMISTRY("🧪", listOf("chemistry", "chemical", "reactions", "organic", "inorganic", "biochemistry", "molecules", "compounds"), IconCategory.STEM, "Chemistry"),
    BIOLOGY("🧬", listOf("biology", "biological", "genetics", "ecology", "evolution", "anatomy", "physiology", "botany", "zoology"), IconCategory.STEM, "Biology"),
    ANATOMY("🫀", listOf("anatomy", "human body", "organs", "muscles", "skeleton", "physiological", "medical anatomy"), IconCategory.STEM, "Anatomy"),
    ASTRONOMY("🔭", listOf("astronomy", "astrophysics", "space", "planets", "stars", "universe", "cosmology", "telescope"), IconCategory.STEM, "Astronomy"),
    EARTH_SCIENCE("🌍", listOf("earth science", "geology", "meteorology", "oceanography", "climate", "weather", "rocks", "minerals"), IconCategory.STEM, "Earth Science"),
    
    // STEM - Technology & Engineering
    COMPUTER("💻", listOf("computer science", "programming", "coding", "software", "development", "algorithms", "data structures", "cs"), IconCategory.STEM, "Computer Science"),
    ENGINEERING("🏗️", listOf("engineering", "mechanical", "civil", "electrical", "chemical", "aerospace", "industrial"), IconCategory.STEM, "Engineering"),
    ROBOTICS("🤖", listOf("robotics", "automation", "artificial intelligence", "ai", "machine learning", "mechatronics"), IconCategory.STEM, "Robotics"),
    DATA_SCIENCE("📈", listOf("data science", "machine learning", "artificial intelligence", "big data", "analytics", "data mining"), IconCategory.STEM, "Data Science"),
    CYBERSECURITY("🔒", listOf("cybersecurity", "information security", "network security", "cryptography", "hacking", "cyber"), IconCategory.STEM, "Cybersecurity"),
    WEB_DEV("🌐", listOf("web development", "web design", "html", "css", "javascript", "frontend", "backend", "fullstack"), IconCategory.STEM, "Web Development"),
    
    // Languages
    ENGLISH("🇺🇸", listOf("english", "literature", "grammar", "writing", "composition", "rhetoric", "shakespeare"), IconCategory.LANGUAGES, "English"),
    SPANISH("🇪🇸", listOf("spanish", "español", "castellano", "hispanic", "latin american"), IconCategory.LANGUAGES, "Spanish"),
    FRENCH("🇫🇷", listOf("french", "français", "francophone", "francais"), IconCategory.LANGUAGES, "French"),
    GERMAN("🇩🇪", listOf("german", "deutsch", "deutsche", "germanic"), IconCategory.LANGUAGES, "German"),
    CHINESE("🇨🇳", listOf("chinese", "mandarin", "cantonese", "中文", "汉语", "putonghua"), IconCategory.LANGUAGES, "Chinese"),
    JAPANESE("🇯🇵", listOf("japanese", "nihongo", "日本語", "kanji", "hiragana", "katakana"), IconCategory.LANGUAGES, "Japanese"),
    KOREAN("🇰🇷", listOf("korean", "hangul", "한국어", "한글"), IconCategory.LANGUAGES, "Korean"),
    ITALIAN("🇮🇹", listOf("italian", "italiano", "italiana"), IconCategory.LANGUAGES, "Italian"),
    PORTUGUESE("🇵🇹", listOf("portuguese", "português", "lusophone", "brasileiro"), IconCategory.LANGUAGES, "Portuguese"),
    RUSSIAN("🇷🇺", listOf("russian", "русский", "cyrillic", "slavic"), IconCategory.LANGUAGES, "Russian"),
    ARABIC("🇸🇦", listOf("arabic", "العربية", "arab", "middle eastern"), IconCategory.LANGUAGES, "Arabic"),
    HINDI("🇮🇳", listOf("hindi", "हिन्दी", "devanagari", "indian"), IconCategory.LANGUAGES, "Hindi"),
    
    // Arts & Humanities
    ART("🎨", listOf("art", "visual arts", "fine arts", "drawing", "painting", "sculpture", "artistic"), IconCategory.ARTS, "Art"),
    MUSIC("🎵", listOf("music", "musical", "composition", "theory", "harmony", "melody", "rhythm"), IconCategory.ARTS, "Music"),
    PIANO("🎹", listOf("piano", "keyboard", "keys", "classical music"), IconCategory.ARTS, "Piano"),
    GUITAR("🎸", listOf("guitar", "strings", "acoustic", "electric", "rock", "folk"), IconCategory.ARTS, "Guitar"),
    VIOLIN("🎻", listOf("violin", "viola", "strings", "orchestra", "classical", "fiddle"), IconCategory.ARTS, "Violin"),
    DRAMA("🎭", listOf("drama", "theater", "theatre", "acting", "performance", "plays", "stagecraft"), IconCategory.ARTS, "Drama"),
    FILM("🎬", listOf("film", "cinema", "movies", "filmmaking", "cinematography", "directing", "video production"), IconCategory.ARTS, "Film"),
    PHOTOGRAPHY("📷", listOf("photography", "photo", "camera", "digital photography", "portrait", "landscape"), IconCategory.ARTS, "Photography"),
    DANCE("💃", listOf("dance", "ballet", "choreography", "movement", "contemporary", "hip hop"), IconCategory.ARTS, "Dance"),
    
    // Humanities & Social Sciences
    HISTORY("📜", listOf("history", "historical", "ancient", "medieval", "modern", "world history", "civilization"), IconCategory.HUMANITIES, "History"),
    PHILOSOPHY("🤔", listOf("philosophy", "philosophical", "ethics", "logic", "metaphysics", "epistemology"), IconCategory.HUMANITIES, "Philosophy"),
    PSYCHOLOGY("🧠", listOf("psychology", "psychological", "mental health", "behavior", "cognitive", "therapy", "counseling"), IconCategory.HUMANITIES, "Psychology"),
    SOCIOLOGY("👥", listOf("sociology", "social science", "society", "culture", "social behavior", "anthropology"), IconCategory.HUMANITIES, "Sociology"),
    POLITICAL_SCIENCE("🏛️", listOf("political science", "politics", "government", "public policy", "international relations"), IconCategory.HUMANITIES, "Political Science"),
    ECONOMICS("💰", listOf("economics", "economic", "macroeconomics", "microeconomics", "finance", "market"), IconCategory.HUMANITIES, "Economics"),
    GEOGRAPHY("🗺️", listOf("geography", "geographical", "maps", "cartography", "physical geography", "human geography"), IconCategory.HUMANITIES, "Geography"),
    ANTHROPOLOGY("🏺", listOf("anthropology", "anthropological", "cultural", "archaeological", "human evolution"), IconCategory.HUMANITIES, "Anthropology"),
    RELIGION("☪️", listOf("religion", "religious studies", "theology", "spirituality", "comparative religion"), IconCategory.HUMANITIES, "Religious Studies"),
    
    // Professional & Applied Fields
    BUSINESS("💼", listOf("business", "management", "entrepreneurship", "marketing", "administration", "corporate"), IconCategory.PROFESSIONAL, "Business"),
    LAW("⚖️", listOf("law", "legal", "justice", "court", "attorney", "jurisprudence", "constitutional"), IconCategory.PROFESSIONAL, "Law"),
    MEDICINE("🏥", listOf("medicine", "medical", "health", "healthcare", "clinical", "nursing", "pharmacy"), IconCategory.PROFESSIONAL, "Medicine"),
    EDUCATION("🎓", listOf("education", "teaching", "pedagogy", "curriculum", "learning", "educational psychology"), IconCategory.PROFESSIONAL, "Education"),
    JOURNALISM("📰", listOf("journalism", "news", "media", "reporting", "communication", "broadcasting"), IconCategory.PROFESSIONAL, "Journalism"),
    ARCHITECTURE("🏢", listOf("architecture", "architectural", "design", "building", "urban planning", "construction"), IconCategory.PROFESSIONAL, "Architecture"),
    AGRICULTURE("🌾", listOf("agriculture", "farming", "crops", "livestock", "agronomy", "agricultural science"), IconCategory.PROFESSIONAL, "Agriculture"),
    
    // Skills & Hobbies
    COOKING("🍳", listOf("cooking", "culinary", "food", "nutrition", "recipe", "chef", "gastronomy", "baking"), IconCategory.SKILLS, "Cooking"),
    SPORTS("⚽", listOf("sports", "fitness", "exercise", "physical education", "athletics", "training", "health"), IconCategory.SKILLS, "Sports"),
    WRITING("✍️", listOf("writing", "creative writing", "composition", "journalism", "copywriting", "technical writing"), IconCategory.SKILLS, "Writing"),
    PUBLIC_SPEAKING("🎤", listOf("public speaking", "presentation", "communication", "rhetoric", "debate", "oratory"), IconCategory.SKILLS, "Public Speaking"),
    CRAFTS("🧵", listOf("crafts", "handicrafts", "sewing", "knitting", "woodworking", "pottery", "jewelry making"), IconCategory.SKILLS, "Crafts"),
    GARDENING("🌱", listOf("gardening", "horticulture", "plants", "botany", "landscaping", "agriculture"), IconCategory.SKILLS, "Gardening"),
    
    // Literature & Reading
    LITERATURE("📚", listOf("literature", "reading", "novels", "poetry", "prose", "literary analysis", "classics"), IconCategory.ARTS, "Literature"),
    POETRY("🖋️", listOf("poetry", "poems", "verse", "haiku", "sonnet", "creative writing"), IconCategory.ARTS, "Poetry"),
    
    // Default
    DEFAULT("📖", listOf(), IconCategory.GENERAL, "General")
}

enum class IconCategory(val displayName: String) {
    STEM("STEM"),
    LANGUAGES("Languages"), 
    ARTS("Arts"),
    HUMANITIES("Humanities"),
    PROFESSIONAL("Professional"),
    SKILLS("Skills & Hobbies"),
    GENERAL("General")
}

object SubjectIconMatcher {
    
    /**
     * Enhanced icon matching with priority-based algorithm
     * 1. Exact matches (highest priority)
     * 2. Word boundary matches 
     * 3. Contains matches (partial)
     * 4. Fuzzy matches for common typos
     */
    fun getIconForSubject(subjectName: String): SubjectIcon {
        val normalizedName = subjectName.lowercase().trim()
        
        if (normalizedName.isBlank()) return SubjectIcon.DEFAULT
        
        val icons = SubjectIcon.values().filter { it != SubjectIcon.DEFAULT }
        
        // 1. Exact keyword matches (highest priority)
        icons.firstOrNull { icon ->
            icon.keywords.any { keyword -> 
                normalizedName == keyword.lowercase() 
            }
        }?.let { return it }
        
        // 2. Word boundary matches - keyword is a complete word in the subject name
        icons.firstOrNull { icon ->
            icon.keywords.any { keyword ->
                val keywordLower = keyword.lowercase()
                normalizedName.split(" ", "-", "_", "/").any { word ->
                    word == keywordLower
                }
            }
        }?.let { return it }
        
        // 3. Multi-word keyword matches - for compound subjects like "applied mathematics"
        icons.firstOrNull { icon ->
            icon.keywords.any { keyword ->
                val keywordWords = keyword.lowercase().split(" ")
                if (keywordWords.size > 1) {
                    keywordWords.all { keywordWord ->
                        normalizedName.contains(keywordWord)
                    }
                } else false
            }
        }?.let { return it }
        
        // 4. Partial matches - keyword contained in subject name
        icons.firstOrNull { icon ->
            icon.keywords.any { keyword ->
                normalizedName.contains(keyword.lowercase())
            }
        }?.let { return it }
        
        // 5. Fuzzy matching for common typos and variations
        icons.firstOrNull { icon ->
            icon.keywords.any { keyword ->
                isFuzzyMatch(normalizedName, keyword.lowercase())
            }
        }?.let { return it }
        
        return SubjectIcon.DEFAULT
    }
    
    /**
     * Simple fuzzy matching for common typos and variations
     */
    private fun isFuzzyMatch(subject: String, keyword: String): Boolean {
        // Skip fuzzy matching for very short words to avoid false positives
        if (keyword.length < 4) return false
        
        // Calculate Levenshtein distance for single words
        val subjectWords = subject.split(" ", "-", "_", "/")
        return subjectWords.any { word ->
            if (word.length >= 3) {
                val distance = levenshteinDistance(word, keyword)
                // Allow 1-2 character differences based on word length
                val threshold = when {
                    keyword.length <= 5 -> 1
                    keyword.length <= 8 -> 2
                    else -> 3
                }
                distance <= threshold && distance < keyword.length / 2
            } else false
        }
    }
    
    /**
     * Calculate Levenshtein distance between two strings
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                dp[i][j] = if (s1[i-1] == s2[j-1]) {
                    dp[i-1][j-1]
                } else {
                    1 + minOf(dp[i-1][j], dp[i][j-1], dp[i-1][j-1])
                }
            }
        }
        
        return dp[s1.length][s2.length]
    }
    
    /**
     * Get all icons grouped by category for UI display
     */
    fun getIconsByCategory(): Map<IconCategory, List<SubjectIcon>> {
        return SubjectIcon.values()
            .filter { it != SubjectIcon.DEFAULT }
            .groupBy { it.category }
            .toSortedMap(compareBy { it.displayName })
    }
    
    /**
     * Get all icons as a flat list (for backward compatibility)
     */
    fun getAllIcons(): List<SubjectIcon> = SubjectIcon.values().toList()
    
    /**
     * Get icons for a specific category
     */
    fun getIconsForCategory(category: IconCategory): List<SubjectIcon> {
        return SubjectIcon.values().filter { it.category == category && it != SubjectIcon.DEFAULT }
    }
    
    /**
     * Search icons by keyword or display name
     */
    fun searchIcons(query: String): List<SubjectIcon> {
        if (query.isBlank()) return getAllIcons()
        
        val queryLower = query.lowercase().trim()
        return SubjectIcon.values()
            .filter { it != SubjectIcon.DEFAULT }
            .filter { icon ->
                icon.displayName.lowercase().contains(queryLower) ||
                icon.keywords.any { keyword -> keyword.lowercase().contains(queryLower) }
            }
            .sortedBy { icon ->
                // Prioritize exact display name matches
                when {
                    icon.displayName.lowercase() == queryLower -> 0
                    icon.displayName.lowercase().startsWith(queryLower) -> 1
                    icon.keywords.any { it.lowercase() == queryLower } -> 2
                    else -> 3
                }
            }
    }
}