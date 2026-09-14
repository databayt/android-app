package org.hogwarts.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import org.hogwarts.android.core.designsystem.atom.AtomStudio
import org.hogwarts.android.core.designsystem.theme.HogwartsTheme

/**
 * Standalone activity to launch Atom Studio directly.
 *
 * Launch via ADB:
 * adb shell am start -n org.hogwarts.android.debug/org.hogwarts.android.AtomStudioActivity
 */
class AtomStudioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HogwartsTheme {
                AtomStudio()
            }
        }
    }
}
