package org.hogwarts.android.feature.stream.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

/**
 * Parity with web `/Users/abdout/hogwarts/src/lib/asset-url.ts`.
 * Hogwarts CloudFront holds illustrations, icons, photos, animations.
 */
private const val HOGWARTS_CDN = "https://d1dlwtcfl0db67.cloudfront.net"

/**
 * Resolves a relative asset path (e.g. `/illustrations/teach.jpg`) to its CloudFront URL.
 */
fun asset(path: String): String {
    val clean = if (path.startsWith("/")) path else "/$path"
    return "$HOGWARTS_CDN$clean"
}

object StreamAssets {
    // Hero — Lottie animation loaded from Hogwarts CDN
    val educationAnimation = asset("/animations/education.json")

    // Feature grid — SVG icons shared with the web app.
    // Source: hogwarts/public/icons/stream-*.svg, synced via scripts/migrate-assets-to-s3.ts.
    val featureCuratedCourses = asset("/icons/stream-curated-courses.svg")
    val featureInteractiveLearning = asset("/icons/stream-interactive-learning.svg")
    val featureProgressTracking = asset("/icons/stream-progress-tracking.svg")
    val featureCommunity = asset("/icons/stream-community.svg")

    // Reasons section — Hogwarts CDN illustrations
    val reasonTeach = asset("/illustrations/teach.jpg")
    val reasonInspire = asset("/illustrations/inspire.jpg")
    val reasonReward = asset("/illustrations/reward.jpg")

    // AI Fluency hero
    val aiFluencyHero = asset("/illustrations/ai-fluency-hero.webp")

    // Skills cards
    val skillGenerativeAi = asset("/illustrations/generative-ai.png")
    val skillItCertifications = asset("/illustrations/it-certifications.png")
    val skillDataScience = asset("/illustrations/data-science.png")

    // Teaching hero (sourced from Webflow CDN on web, kept verbatim for parity)
    const val teachingHeroIllustration =
        "https://cdn.prod.website-files.com/68a44d4040f98a4adf2207b6/6903d229061abf091318fc81_6905c83d0735e1bc430025fdd1748d1406079036-1000x1000.svg"

    // Explore Courses hero — Anthropic abstract illustration (same as web)
    val coursesHeroIllustration = asset("/illustrations/anthropic-abstract.svg")

    // How-to-begin step illustrations (Udemy teaching CDN — same as web)
    const val beginPlanIllustration =
        "https://s.udemycdn.com/teaching/plan-your-curriculum-2x-v3.jpg"
    const val beginRecordIllustration =
        "https://s.udemycdn.com/teaching/record-your-video-2x-v3.jpg"
    const val beginLaunchIllustration =
        "https://s.udemycdn.com/teaching/launch-your-course-2x-v3.jpg"

    // Curriculum icons (Contentful — same as web)
    const val curriculumWorldClassIcon =
        "https://images.ctfassets.net/2pudprfttvy6/4XMrz5se3QIJusI0TKe8Vp/2e71bff5bf8f587e24bb7b2d4fb515f5/icon_website.svg"
    const val curriculumGuidedIcon =
        "https://images.ctfassets.net/2pudprfttvy6/14Ncu21DhOvd6FxJYVX8zk/3fc2089321031b72a380553f7ca7f0a2/icon-instructor.svg"
    const val curriculumCertificatesIcon =
        "https://images.ctfassets.net/2pudprfttvy6/4weCx1DOOp7qrFXKOQhOVr/c3ed49c7d07bbcacf569012056f7a900/CourseraIcon_Diplomas_Black.svg"
    const val curriculumLmsIcon =
        "https://images.ctfassets.net/2pudprfttvy6/55nWm7sKBNc29Ey0gfWsgo/230dfdcbbb963ea06109df7c30094e6c/icon-integration.svg"
}

/**
 * Horizontally flips content when the current layout direction is RTL.
 *
 * Parity with web's `rtl:[transform:scaleX(-1)]` used on directional illustrations
 * (teaching hero, how-to-begin steps, skill cards).
 */
@Composable
@ReadOnlyComposable
fun Modifier.flipForRtl(): Modifier {
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    return if (isRtl) this.scale(scaleX = -1f, scaleY = 1f) else this
}
