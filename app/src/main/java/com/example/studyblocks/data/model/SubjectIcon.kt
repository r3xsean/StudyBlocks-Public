package com.example.studyblocks.data.model

enum class SubjectIcon(val emoji: String, val keywords: List<String>) {
    BOOKS("📚", listOf("language", "literature", "history", "english", "reading", "writing")),
    MATH("🧮", listOf("mathematics", "math", "statistics", "calculus", "algebra", "geometry")),
    COMPUTER("💻", listOf("computer", "programming", "coding", "software", "development", "cs")),
    SCIENCE("🧪", listOf("chemistry", "physics", "biology", "science", "lab", "experiment")),
    CHART("📈", listOf("economics", "business", "finance", "accounting", "marketing", "statistics")),
    ART("🎨", listOf("art", "design", "music", "creative", "drawing", "painting")),
    GLOBE("🌍", listOf("geography", "social", "politics", "world", "culture", "society")),
    SCALES("⚖️", listOf("law", "legal", "justice", "court", "attorney")),
    MEDICAL("🏥", listOf("medicine", "health", "medical", "nursing", "anatomy", "biology")),
    ENGINEERING("🏗️", listOf("engineering", "construction", "mechanical", "civil", "electrical")),
    PSYCHOLOGY("🧠", listOf("psychology", "mental", "behavior", "cognitive", "therapy")),
    LANGUAGE("🗣️", listOf("spanish", "french", "german", "chinese", "japanese", "foreign")),
    MUSIC("🎵", listOf("music", "piano", "guitar", "singing", "composition", "theory")),
    SPORTS("⚽", listOf("sports", "fitness", "exercise", "physical", "health", "training")),
    COOKING("🍳", listOf("cooking", "culinary", "food", "nutrition", "recipe", "chef")),
    DEFAULT("📖", listOf())
}

object SubjectIconMatcher {
    fun getIconForSubject(subjectName: String): SubjectIcon {
        val normalizedName = subjectName.lowercase().trim()
        
        return SubjectIcon.values()
            .filter { it != SubjectIcon.DEFAULT }
            .firstOrNull { icon ->
                icon.keywords.any { keyword ->
                    normalizedName.contains(keyword)
                }
            } ?: SubjectIcon.DEFAULT
    }
    
    fun getAllIcons(): List<SubjectIcon> = SubjectIcon.values().toList()
}