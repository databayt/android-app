package org.hogwarts.android.shell.search

import androidx.annotation.StringRes
import org.hogwarts.android.R
import org.hogwarts.android.core.data.tenant.UserRole
import org.hogwarts.android.shell.visibleNav

/** A row the search offers: a page to open, or something to create. */
data class SearchItem(
    val id: String,
    val title: String,
    val href: String,
    val kind: SearchItemKind,
    /** Widens the match surface; never rendered. */
    val keywords: List<String> = emptyList(),
    val description: String? = null,
)

enum class SearchItemKind { Page, Action }

/**
 * Arabic synonyms that widen what a page answers to — a port of
 * `ARABIC_KEYWORDS` in `derive-from-platform-nav.ts`, keyed by the same nav
 * keys. The displayed title comes from the string resources; these are extra
 * tokens the filter sees and the reader does not, so "طلاب" finds the
 * students page whatever the sidebar happens to call it.
 */
private val ARABIC_KEYWORDS: Map<String, List<String>> = mapOf(
    "dashboard" to listOf("لوحة", "الرئيسية"),
    "school" to listOf("المدرسة", "الإدارة"),
    "sales" to listOf("المبيعات", "العملاء"),
    "announcements" to listOf("إعلانات", "أخبار"),
    "finance" to listOf("مالية", "محاسبة", "حسابات"),
    "grades" to listOf("درجات", "علامات", "تقديرات"),
    "subjects" to listOf("مواد", "مقررات", "منهج"),
    "parents" to listOf("أولياء", "آباء", "ذوي الطلاب"),
    "admission" to listOf("قبول", "تسجيل"),
    "students" to listOf("طلاب", "طالب", "تلاميذ"),
    "teachers" to listOf("معلمين", "معلم", "أساتذة"),
    "classrooms" to listOf("قاعات", "فصول", "صفوف"),
    "exams" to listOf("اختبارات", "امتحان"),
    "events" to listOf("فعاليات", "أنشطة"),
    "attendance" to listOf("حضور", "غياب"),
    "timetable" to listOf("جدول", "حصص"),
    "library" to listOf("مكتبة", "كتب"),
    "transportation" to listOf("نقل", "حافلات", "مواصلات"),
    "transportationFees" to listOf("رسوم النقل", "اشتراك الباص"),
    "myTransportation" to listOf("باصي", "نقلي", "مواصلاتي"),
    "transportationTrips" to listOf("رحلات", "نقل"),
    "myAssignments" to listOf("واجبات", "واجباتي", "تكليفات"),
    "lumos" to listOf("بث", "فيديو", "دروس مصورة"),
    "liveClasses" to listOf("اجتماعات", "حصص مباشرة", "بث مباشر", "مؤتمر"),
    "messages" to listOf("رسائل", "محادثات"),
    "whatsapp" to listOf("واتساب", "محادثات"),
    "profile" to listOf("ملفي", "حسابي"),
    "settings" to listOf("إعدادات", "تفضيلات"),
    "charts" to listOf("رسوم بيانية", "إحصاءات"),
    "stats" to listOf("إحصاءات"),
    "billing" to listOf("الفوترة", "الاشتراك"),
)

/**
 * The "create" rows, ported from `platform-config.ts` → `actions`. Same ids,
 * hrefs, roles and English keywords; the titles and descriptions are string
 * resources so they read in the app's language.
 */
internal data class ActionSpec(
    val id: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val href: String,
    val roles: Set<UserRole>,
    val keywords: List<String>,
)

private val ADMIN_STAFF = setOf(UserRole.ADMIN, UserRole.STAFF)
private val ADMIN_STAFF_TEACHER = setOf(UserRole.ADMIN, UserRole.STAFF, UserRole.TEACHER)

internal val searchActions: List<ActionSpec> = listOf(
    ActionSpec(
        "new-student", R.string.search_new_student, R.string.search_new_student_desc,
        "/students/new", ADMIN_STAFF, listOf("create", "add", "enroll", "register", "طالب", "تسجيل"),
    ),
    ActionSpec(
        "new-teacher", R.string.search_new_teacher, R.string.search_new_teacher_desc,
        "/teachers/new", ADMIN_STAFF, listOf("create", "add", "hire", "معلم", "توظيف"),
    ),
    ActionSpec(
        "new-classroom", R.string.search_new_class, R.string.search_new_class_desc,
        "/classrooms/create", ADMIN_STAFF, listOf("create", "add", "group", "classroom", "فصل", "قاعة"),
    ),
    ActionSpec(
        "new-subject", R.string.search_new_subject, R.string.search_new_subject_desc,
        "/subjects/new", ADMIN_STAFF, listOf("create", "add", "course", "مادة", "مقرر"),
    ),
    ActionSpec(
        "new-announcement", R.string.search_new_announcement, R.string.search_new_announcement_desc,
        "/announcements/new", ADMIN_STAFF_TEACHER, listOf("create", "add", "post", "notify", "إعلان"),
    ),
    ActionSpec(
        "new-exam", R.string.search_new_exam, R.string.search_new_exam_desc,
        "/exams/new", ADMIN_STAFF_TEACHER, listOf("create", "add", "test", "assessment", "امتحان", "اختبار"),
    ),
)

/**
 * Every page this role can reach, as search rows — `platformNav` filtered the
 * way the sidebar and the menu filter it, so a page that is not in the menu is
 * not findable here either. `deriveNavSearchItems` on the web does the same
 * from the same registry, which is why neither list can drift from the other.
 */
fun navSearchItems(
    role: UserRole?,
    enabledModules: List<String>?,
    title: (Int) -> String,
): List<SearchItem> = visibleNav(role, enabledModules).map { nav ->
    SearchItem(
        id = "nav-${nav.key}",
        title = title(nav.titleRes),
        href = nav.href,
        kind = SearchItemKind.Page,
        keywords = listOf(nav.key) + ARABIC_KEYWORDS[nav.key].orEmpty(),
    )
}

/** The create rows this role can use. */
fun actionSearchItems(
    role: UserRole?,
    title: (Int) -> String,
): List<SearchItem> = searchActions.filter { role != null && role in it.roles }.map { action ->
    SearchItem(
        id = action.id,
        title = title(action.titleRes),
        href = action.href,
        kind = SearchItemKind.Action,
        keywords = action.keywords,
        description = title(action.descriptionRes),
    )
}

/**
 * `filterByQuery`: one normalized haystack per row — title, description and
 * keywords — and a plain substring test, exactly as the web filters.
 */
fun filterByQuery(items: List<SearchItem>, query: String): List<SearchItem> {
    val needle = normalizeForMatch(query)
    if (needle.isEmpty()) return items
    return items.filter { item ->
        val haystack = (listOf(item.title, item.description.orEmpty()) + item.keywords)
            .joinToString(" ") { normalizeForMatch(it) }
        haystack.contains(needle)
    }
}
