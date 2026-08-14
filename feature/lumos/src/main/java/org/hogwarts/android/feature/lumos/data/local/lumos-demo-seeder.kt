package org.hogwarts.android.feature.lumos.data.local

import kotlinx.coroutines.flow.first
import org.hogwarts.android.core.database.dao.CourseDao
import org.hogwarts.android.core.database.entity.ChapterEntity
import org.hogwarts.android.core.database.entity.CourseEntity
import org.hogwarts.android.core.database.entity.LessonEntity
import javax.inject.Inject

class LumosDemoSeeder @Inject constructor(
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
                    thumbnailUrl = def.thumbnailUrl,
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
                            // Demo videos play the story.mp4 sample clip
                            contentUrl = if (lDef.type == "VIDEO") LUMOS_FALLBACK_VIDEO_URL else null,
                            thumbnailUrl = def.thumbnailUrl,
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

/** CloudFront-hosted sample story video used for all demo lessons and error fallbacks. */
const val LUMOS_FALLBACK_VIDEO_URL = "https://d1dlwtcfl0db67.cloudfront.net/media/story.mp4"

private data class CourseDef(
    val id: String,
    val title: String,
    val description: String,
    val instructor: String,
    val category: String,
    val enrollmentCount: Int,
    val grades: List<Int>,
    val thumbnailUrl: String? = null,
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
        id = "lumos-g1-math",
        title = "الرياضيات — الصف الأول",
        description = "مفاهيم الأعداد والعد والعمليات الحسابية الأساسية والأشكال الهندسية للصف الأول الابتدائي.",
        instructor = "أ. سارة المنصور",
        category = "الرياضيات",
        enrollmentCount = 142,
        grades = listOf(1),
        thumbnailUrl = "https://cdn.databayt.org/hogwarts/curriculum-math.png",
        chapters = listOf(
            ChapterDef(
                title = "الوحدة الأولى: الأعداد حتى 10",
                lessons = listOf(
                    LessonDef("العد من 1 إلى 5", "VIDEO", 8),
                    LessonDef("العد من 6 إلى 10 ومقارنة الأعداد", "VIDEO", 12),
                    LessonDef("اختبار قصير: قراءة وكتابة الأعداد", "QUIZ", 5)
                )
            ),
            ChapterDef(
                title = "الوحدة الثانية: الجمع البسيط",
                lessons = listOf(
                    LessonDef("مفهوم الجمع ورموزه", "VIDEO", 10),
                    LessonDef("تمارين الجمع بالصور", "TEXT", 6),
                    LessonDef("اختبار الوحدة الثانية", "QUIZ", 5)
                )
            )
        )
    ),
    CourseDef(
        id = "lumos-g1-arabic",
        title = "لغتي الجميلة — الصف الأول",
        description = "تعلم الحروف الهجائية، أصوات الحروف، والكلمات والجمل الأولى بطريقة تفاعلية وممتعة.",
        instructor = "أ. فاطمة الزهراء",
        category = "اللغة العربية",
        enrollmentCount = 189,
        grades = listOf(1),
        thumbnailUrl = "https://cdn.databayt.org/hogwarts/curriculum-arabic.png",
        chapters = listOf(
            ChapterDef(
                title = "حروفي الأولى",
                lessons = listOf(
                    LessonDef("حرف الألف وحرف الباء", "VIDEO", 10),
                    LessonDef("حرف التاء وحرف الثاء", "VIDEO", 9),
                    LessonDef("قراءة كلمات قصيرة", "TEXT", 7)
                )
            ),
            ChapterDef(
                title = "أصوات الحروف والمدود",
                lessons = listOf(
                    LessonDef("المد بالألف والواو والياء", "VIDEO", 14),
                    LessonDef("اختبار المدود والحركات", "QUIZ", 5)
                )
            )
        )
    ),
    CourseDef(
        id = "lumos-g2-science",
        title = "العلوم واستكشاف الطبيعة",
        description = "استكشف عالم الكائنات الحية، النباتات، وحالات المادة والتجارب العلمية المبسطة.",
        instructor = "د. طارق الحكيم",
        category = "العلوم",
        enrollmentCount = 95,
        grades = listOf(2),
        thumbnailUrl = "https://cdn.databayt.org/hogwarts/curriculum-science.png",
        chapters = listOf(
            ChapterDef(
                title = "النباتات والحيوانات",
                lessons = listOf(
                    LessonDef("أجزاء النبات ووظائفها", "VIDEO", 11),
                    LessonDef("كيف تنمو النباتات وتتكاثر", "VIDEO", 13),
                    LessonDef("اختبار دورة حياة النبات", "QUIZ", 5)
                )
            ),
            ChapterDef(
                title = "المادة وخصائصها",
                lessons = listOf(
                    LessonDef("المواد الصلبة والسائلة والغازية", "VIDEO", 12),
                    LessonDef("ملخص حالات المادة", "TEXT", 8)
                )
            )
        )
    ),
    CourseDef(
        id = "lumos-g3-english",
        title = "English for Beginners — Grade 3",
        description = "Build solid vocabulary, phonics, everyday expressions, and simple reading comprehension.",
        instructor = "Mr. James Wright",
        category = "English Language",
        enrollmentCount = 120,
        grades = listOf(3),
        thumbnailUrl = "https://cdn.databayt.org/hogwarts/curriculum-english.png",
        chapters = listOf(
            ChapterDef(
                title = "Phonics & Everyday Words",
                lessons = listOf(
                    LessonDef("Sight Words & Phonics Basics", "VIDEO", 15),
                    LessonDef("Greetings & Introductions", "VIDEO", 10),
                    LessonDef("Vocabulary Quiz", "QUIZ", 5)
                )
            )
        )
    )
)
