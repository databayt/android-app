package org.hogwarts.android.feature.stream.data.local

import kotlinx.coroutines.flow.first
import org.hogwarts.android.core.database.dao.CourseDao
import org.hogwarts.android.core.database.entity.ChapterEntity
import org.hogwarts.android.core.database.entity.CourseEntity
import org.hogwarts.android.core.database.entity.LessonEntity
import javax.inject.Inject

class StreamDemoSeeder @Inject constructor(
    private val courseDao: CourseDao
) {
    suspend fun seedIfEmpty(schoolId: String) {
        val existing = courseDao.getCourses(schoolId).first()
        if (existing.isNotEmpty()) return

        val now = System.currentTimeMillis()
        val courses = mutableListOf<CourseEntity>()
        val chapters = mutableListOf<ChapterEntity>()
        val lessons = mutableListOf<LessonEntity>()

        demoCourses.forEach { def ->
            courses.add(
                CourseEntity(
                    id = def.id,
                    schoolId = schoolId,
                    title = def.title,
                    description = def.description,
                    instructorName = def.instructor,
                    thumbnailUrl = null,
                    category = def.category,
                    enrollmentCount = def.enrollmentCount,
                    lessonCount = def.chapters.sumOf { it.lessons.size },
                    totalDuration = def.chapters.sumOf { ch -> ch.lessons.sumOf { it.durationMinutes } } * 60L,
                    status = "PUBLISHED",
                    grades = def.grades.joinToString(","),
                    lastSyncedAt = now
                )
            )

            def.chapters.forEachIndexed { ci, chDef ->
                val chapterId = "${def.id}_ch${ci + 1}"
                chapters.add(
                    ChapterEntity(
                        id = chapterId,
                        courseId = def.id,
                        title = chDef.title,
                        orderIndex = ci + 1,
                        lessonCount = chDef.lessons.size,
                        completedLessons = 0
                    )
                )

                chDef.lessons.forEachIndexed { li, lDef ->
                    lessons.add(
                        LessonEntity(
                            id = "${chapterId}_l${li + 1}",
                            chapterId = chapterId,
                            title = lDef.title,
                            type = lDef.type,
                            duration = lDef.durationMinutes * 60L,
                            // Demo videos play the CDN sample so the full
                            // catalog → detail → enroll → video flow works
                            // end-to-end without a backend.
                            contentUrl = if (lDef.type == "VIDEO") DEMO_VIDEO_URL else null,
                            orderIndex = li + 1,
                            isCompleted = false,
                            isLocked = false
                        )
                    )
                }
            }
        }

        courseDao.upsertCourses(courses)
        courseDao.upsertChapters(chapters)
        courseDao.upsertLessons(lessons)
    }
}

/** CloudFront-hosted sample video used for all demo lessons. */
private const val DEMO_VIDEO_URL = "https://d1dlwtcfl0db67.cloudfront.net/media/story.mp4"

private data class CourseDef(
    val id: String,
    val title: String,
    val description: String,
    val instructor: String,
    val category: String,
    val enrollmentCount: Int,
    val grades: List<Int>,
    val chapters: List<ChapterDef>
)

private data class ChapterDef(
    val title: String,
    val lessons: List<LessonDef>
)

private data class LessonDef(
    val title: String,
    val type: String,
    val durationMinutes: Long
)

private val demoCourses = listOf(
    CourseDef(
        id = "demo_math",
        title = "Mathematics",
        description = "Comprehensive mathematics covering algebra, geometry, and calculus fundamentals.",
        instructor = "Dr. Ahmed Hassan",
        category = "Science",
        enrollmentCount = 128,
        grades = listOf(1),
        chapters = listOf(
            ChapterDef("Algebra Fundamentals", listOf(
                LessonDef("Variables and Expressions", "VIDEO", 15),
                LessonDef("Linear Equations", "TEXT", 10),
                LessonDef("Algebra Quiz", "QUIZ", 8)
            )),
            ChapterDef("Geometry Basics", listOf(
                LessonDef("Angles and Lines", "VIDEO", 20),
                LessonDef("Triangles and Polygons", "TEXT", 12),
                LessonDef("Geometry Quiz", "QUIZ", 10)
            )),
            ChapterDef("Introduction to Calculus", listOf(
                LessonDef("Limits and Continuity", "VIDEO", 25),
                LessonDef("Derivatives", "TEXT", 15),
                LessonDef("Calculus Quiz", "QUIZ", 12)
            ))
        )
    ),
    CourseDef(
        id = "demo_arabic",
        title = "Arabic Language",
        description = "Master Arabic grammar, reading comprehension, and essay writing skills.",
        instructor = "Dr. Fatima Al-Rashidi",
        category = "Language",
        enrollmentCount = 95,
        grades = listOf(1),
        chapters = listOf(
            ChapterDef("Grammar Rules", listOf(
                LessonDef("Sentence Structure", "VIDEO", 18),
                LessonDef("Verb Conjugation", "TEXT", 12),
                LessonDef("Grammar Quiz", "QUIZ", 8)
            )),
            ChapterDef("Reading & Comprehension", listOf(
                LessonDef("Short Story Analysis", "VIDEO", 20),
                LessonDef("Poetry Appreciation", "TEXT", 15),
                LessonDef("Reading Quiz", "QUIZ", 10)
            ))
        )
    ),
    CourseDef(
        id = "demo_science",
        title = "General Science",
        description = "Explore physics, chemistry, and biology through interactive lessons and experiments.",
        instructor = "Prof. Omar Khalil",
        category = "Science",
        enrollmentCount = 112,
        grades = listOf(2),
        chapters = listOf(
            ChapterDef("Physics: Motion & Forces", listOf(
                LessonDef("Newton's Laws", "VIDEO", 22),
                LessonDef("Motion Equations", "TEXT", 14),
                LessonDef("Physics Quiz", "QUIZ", 10)
            )),
            ChapterDef("Chemistry: Elements", listOf(
                LessonDef("Periodic Table", "VIDEO", 18),
                LessonDef("Chemical Bonds", "TEXT", 12),
                LessonDef("Chemistry Quiz", "QUIZ", 8)
            )),
            ChapterDef("Biology: Cells", listOf(
                LessonDef("Cell Structure", "VIDEO", 20),
                LessonDef("Cell Division", "TEXT", 15),
                LessonDef("Biology Quiz", "QUIZ", 10)
            ))
        )
    ),
    CourseDef(
        id = "demo_islamic",
        title = "Islamic Studies",
        description = "Study Quran, Hadith, Fiqh, and Islamic history.",
        instructor = "Sheikh Abdullah Noor",
        category = "Religion",
        enrollmentCount = 87,
        grades = listOf(2),
        chapters = listOf(
            ChapterDef("Quran Studies", listOf(
                LessonDef("Tajweed Rules", "VIDEO", 20),
                LessonDef("Surah Interpretation", "TEXT", 15),
                LessonDef("Quran Quiz", "QUIZ", 10)
            )),
            ChapterDef("Islamic History", listOf(
                LessonDef("The Prophet's Life", "VIDEO", 25),
                LessonDef("The Rightly Guided Caliphs", "TEXT", 18),
                LessonDef("History Quiz", "QUIZ", 10)
            ))
        )
    ),
    CourseDef(
        id = "demo_english",
        title = "English Language",
        description = "Develop English reading, writing, and communication skills.",
        instructor = "Ms. Sarah Mitchell",
        category = "Language",
        enrollmentCount = 104,
        grades = listOf(1),
        chapters = listOf(
            ChapterDef("Grammar Essentials", listOf(
                LessonDef("Parts of Speech", "VIDEO", 15),
                LessonDef("Tenses Overview", "TEXT", 12),
                LessonDef("Grammar Quiz", "QUIZ", 8)
            )),
            ChapterDef("Writing Skills", listOf(
                LessonDef("Essay Structure", "VIDEO", 20),
                LessonDef("Paragraph Writing", "TEXT", 15),
                LessonDef("Writing Quiz", "QUIZ", 10)
            ))
        )
    )
)
