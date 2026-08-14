package org.hogwarts.android.feature.lumos.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.unit.dp

/**
 * Player glyphs copied from the web video-overlay SVG paths
 * (`components/lumos/shared/video-player/video-overlay.tsx`).
 */

val LumosPlayIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "LumosPlay",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 93.5f,
        viewportHeight = 97.51f
    )
        .group(translationX = -96.69f, translationY = -30.35f) {
            addPath(
                pathData = addPathNodes(
                    "M113.428 127.863c2.588 0 5.03-.733 8.448-2.686l60.302-35.01c4.883-2.88 " +
                        "8.008-6.103 8.008-11.084 0-4.98-3.125-8.203-8.008-11.035l-60.302-35.01c" +
                        "-3.418-2.002-5.86-2.685-8.448-2.685-5.566 0-10.742 4.248-10.742 11.67v74." +
                        "17c0 7.422 5.176 11.67 10.742 11.67Z"
                ),
                fill = SolidColor(Color.White)
            )
        }
        .build()
}

val LumosPauseIcon: ImageVector by lazy {
    ImageVector.Builder(
        name = "LumosPause",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 74.91f,
        viewportHeight = 88.23f
    )
        .group(translationX = -104.23f, translationY = -34.94f) {
            addPath(
                pathData = addPathNodes(
                    "M113.411 123.175h12.94c6.103 0 9.13-3.027 9.13-9.13V44.073c0-5.86-3.027-8.936" +
                        "-9.13-9.131h-12.94c-6.103 0-9.18 3.027-9.18 9.13v69.971c-.146 6.104 2.881 9." +
                        "131 9.18 9.131Zm43.604 0h12.939c6.104 0 9.18-3.027 9.18-9.13V44.073c0-5.86-3" +
                        ".076-9.131-9.18-9.131h-12.94c-6.103 0-9.18 3.027-9.18 9.13v69.971c0 6.104 2." +
                        "93 9.131 9.18 9.131Z"
                ),
                fill = SolidColor(Color.White)
            )
        }
        .build()
}

val LumosRewind10Icon: ImageVector by lazy {
    ImageVector.Builder(
        name = "LumosRewind10",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 99.61f,
        viewportHeight = 109.08f
    )
        .group(translationX = -84.21f, translationY = -19.76f) {
            addPath(
                pathData = addPathNodes(
                    "M84.205 79.035c0 27.246 22.608 49.804 49.805 49.804 27.246 0 49.805-22.558 49." +
                        "805-49.804 0-24.024-17.53-44.385-40.381-48.877v-6.934c0-3.467-2.393-4.395-5." +
                        "03-2.49l-15.576 10.888c-2.246 1.563-2.295 3.907 0 5.518l15.528 10.938c2.685 " +
                        "1.953 5.078 1.025 5.078-2.49v-6.934c18.457 4.199 32.031 20.605 32.031 40.38 " +
                        "0 23.047-18.408 41.504-41.455 41.504-23.047 0-41.553-18.457-41.504-41.503.0" +
                        "49-13.868 6.787-26.124 17.188-33.545 2.002-1.514 2.636-3.809 1.416-5.86-1.2" +
                        "21-2.002-3.907-2.539-6.055-.879-12.549 9.131-20.85 23.877-20.85 40.284Zm61." +
                        "866 20.556c8.105 0 13.427-7.666 13.427-19.385 0-11.816-5.322-19.58-13.427-1" +
                        "9.58-8.106 0-13.428 7.764-13.428 19.58 0 11.72 5.322 19.385 13.428 19.385Zm" +
                        "-25.44-.586c1.904 0 3.125-1.318 3.125-3.369V64.923c0-2.392-1.27-3.906-3.467-" +
                        "3.906-1.318 0-2.246.44-4.052 1.611l-6.739 4.541c-1.074.782-1.611 1.66-1.611 " +
                        "2.832 0 1.612 1.27 2.979 2.832 2.979.928 0 1.367-.195 2.344-.879l4.54-3.32v2" +
                        "6.855c0 2.002 1.173 3.37 3.028 3.37Zm25.44-5.322c-4.297 0-7.08-5.127-7.08-13." +
                        "477 0-8.496 2.734-13.671 7.08-13.671 4.345 0 7.03 5.126 7.03 13.671 0 8.35-2." +
                        "734 13.477-7.03 13.477Z"
                ),
                fill = SolidColor(Color.White)
            )
        }
        .build()
}

val LumosForward10Icon: ImageVector by lazy {
    ImageVector.Builder(
        name = "LumosForward10",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 99.61f,
        viewportHeight = 109.06f
    )
        .group(translationX = -84.21f, translationY = -19.78f) {
            addPath(
                pathData = addPathNodes(
                    "M84.205 79.035c0 27.246 22.608 49.804 49.805 49.804 27.246 0 49.805-22.558 49." +
                        "805-49.804 0-16.407-8.301-31.153-20.85-40.284-2.148-1.66-4.834-1.123-6.055." +
                        "88-1.22 2.05-.586 4.345 1.416 5.859 10.4 7.421 17.139 19.677 17.188 33.545." +
                        "049 23.046-18.457 41.503-41.504 41.503-23.047 0-41.455-18.457-41.455-41.503 " +
                        "0-19.776 13.574-36.182 32.031-40.381v6.982c0 3.467 2.393 4.395 5.078 2.49L14" +
                        "5.24 37.19c2.198-1.514 2.247-3.858 0-5.469l-15.527-10.937c-2.734-1.954-5.127" +
                        "-1.026-5.127 2.49v6.885c-22.851 4.492-40.38 24.853-40.38 48.877Zm61.621 20." +
                        "556c8.106 0 13.428-7.666 13.428-19.385 0-11.816-5.322-19.58-13.428-19.58-8." +
                        "105 0-13.427 7.764-13.427 19.58 0 11.72 5.322 19.385 13.427 19.385Zm-25.44-." +
                        "586c1.905 0 3.126-1.318 3.126-3.369V64.923c0-2.392-1.27-3.906-3.467-3.906-1." +
                        "318 0-2.246.44-4.053 1.611l-6.738 4.541c-1.074.782-1.611 1.66-1.611 2.832 0 " +
                        "1.612 1.27 2.979 2.832 2.979.928 0 1.367-.195 2.344-.879l4.54-3.32v26.855c0 " +
                        "2.002 1.172 3.37 3.028 3.37Zm25.44-5.322c-4.296 0-7.08-5.127-7.08-13.477 0-8." +
                        "496 2.735-13.671 7.08-13.671 4.346 0 7.032 5.126 7.032 13.671 0 8.35-2.735 1" +
                        "3.477-7.032 13.477Z"
                ),
                fill = SolidColor(Color.White)
            )
        }
        .build()
}
