package org.hogwarts.android.feature.subjects.domain.model

/**
 * Catalog subject adopted by the school. Mirrors the web `SubjectItem` shape in
 * components/school-dashboard/listings/subjects/catalog-subjects-grid.tsx.
 */
data class Subject(
    val id: String,
    val slug: String,
    val name: String,
    val department: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val color: String? = null,
    val levels: List<SubjectLevel> = emptyList(),
    val grades: List<Int> = emptyList(),
    val totalChapters: Int = 0,
    val totalLessons: Int = 0,
    val averageRating: Float = 0f,
    val ratingCount: Int = 0,
) {
    val primaryLevel: SubjectLevel get() = levels.firstOrNull() ?: SubjectLevel.ELEMENTARY
    val primaryGrade: Int get() = grades.firstOrNull() ?: 0
}

enum class SubjectLevel {
    ELEMENTARY,
    MIDDLE,
    HIGH;

    companion object {
        fun fromString(value: String): SubjectLevel =
            entries.find { it.name.equals(value, ignoreCase = true) } ?: ELEMENTARY
    }
}

/**
 * Detailed view of a subject. Mirrors the aggregation in
 * src/app/[lang]/.../subjects/[slug]/page.tsx: catalog hierarchy (chapters →
 * lessons) plus the five content sections (videos, materials, exams, question
 * stats, assignments).
 */
data class SubjectDetail(
    val subject: Subject,
    val bannerUrl: String? = null,
    val gradeRange: String? = null,
    val curriculum: String? = null,
    val tags: List<String> = emptyList(),
    val totalContent: Int = 0,
    val chapters: List<SubjectChapter> = emptyList(),
    val videos: List<VideoItem> = emptyList(),
    val materials: List<MaterialItem> = emptyList(),
    val exams: List<ExamItem> = emptyList(),
    val assignments: List<AssignmentItem> = emptyList(),
    val questionStats: QuestionStats = QuestionStats(),
) {
    val totalTopics: Int get() = chapters.sumOf { it.lessons.size }
}

/**
 * Chapter: second level of the catalog hierarchy (Subject → Chapter → Lesson).
 */
data class SubjectChapter(
    val id: String,
    val name: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val color: String? = null,
    val totalLessons: Int = 0,
    val lessons: List<SubjectLesson> = emptyList(),
)

/**
 * Lesson: third level of the catalog hierarchy, rendered as a topic card.
 */
data class SubjectLesson(
    val id: String,
    val title: String,
    val description: String? = null,
    val thumbnailUrl: String? = null,
    val color: String? = null,
    val durationMinutes: Int? = null,
    val videoCount: Int = 0,
    val resourceCount: Int = 0,
)

/**
 * Video card derived from a lesson. Matches catalog-content-sections.tsx
 * VideoItem shape.
 */
data class VideoItem(
    val id: String,
    val title: String,
    val thumbnailUrl: String? = null,
    val durationSeconds: Int = 0,
    val viewCount: Int = 0,
    val isFeatured: Boolean = false,
    val provider: String = "catalog",
    val catalogLessonId: String = "",
    val color: String? = null,
)

/**
 * Learning material (PDF, worksheet, etc.). Matches the web MaterialItem shape.
 */
data class MaterialItem(
    val id: String,
    val title: String,
    val description: String? = null,
    val type: String,
    val pageCount: Int? = null,
    val downloadCount: Int = 0,
    val fileSize: Long? = null,
    val mimeType: String? = null,
)

/**
 * Exam linked to a subject. Matches the web ExamItem shape.
 */
data class ExamItem(
    val id: String,
    val title: String,
    val examType: String,
    val durationMinutes: Int? = null,
    val totalMarks: Int? = null,
    val totalQuestions: Int? = null,
    val usageCount: Int = 0,
)

/**
 * Assignment linked at subject/chapter/lesson level.
 */
data class AssignmentItem(
    val id: String,
    val title: String,
    val assignmentType: String? = null,
    val estimatedTime: Int? = null,
    val totalPoints: Float? = null,
    val usageCount: Int = 0,
)

/**
 * Aggregated question-bank stats by type and difficulty.
 */
data class QuestionStats(
    val total: Int = 0,
    val cards: List<QuestionTypeCard> = emptyList(),
)

data class QuestionTypeCard(
    val type: String,
    val count: Int = 0,
    val byDifficulty: Map<String, Int> = emptyMap(),
)

/**
 * Summary of a subject the current user is enrolled in or teaches.
 */
data class MySubjectSummary(
    val id: String,
    val name: String,
    val slug: String,
    val department: String? = null,
    val thumbnailUrl: String? = null,
    val teacherName: String? = null,
)
