package org.hogwarts.android.core.security

import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CredentialManagerTest {

    private class MemoryPrefs : SharedPreferences {
        val map = mutableMapOf<String, Any?>()
        override fun getAll(): MutableMap<String, *> = map
        override fun getString(key: String, defValue: String?) = map[key] as String? ?: defValue
        override fun getStringSet(key: String, defValues: MutableSet<String>?) = defValues
        override fun getInt(key: String, defValue: Int) = map[key] as Int? ?: defValue
        override fun getLong(key: String, defValue: Long) = map[key] as Long? ?: defValue
        override fun getFloat(key: String, defValue: Float) = map[key] as Float? ?: defValue
        override fun getBoolean(key: String, defValue: Boolean) = map[key] as Boolean? ?: defValue
        override fun contains(key: String) = map.containsKey(key)
        override fun registerOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
        override fun unregisterOnSharedPreferenceChangeListener(l: SharedPreferences.OnSharedPreferenceChangeListener?) = Unit
        override fun edit(): SharedPreferences.Editor = object : SharedPreferences.Editor {
            private val pending = mutableMapOf<String, Any?>()
            private val removed = mutableSetOf<String>()
            override fun putString(key: String, value: String?) = apply { pending[key] = value }
            override fun putStringSet(key: String, values: MutableSet<String>?) = apply { pending[key] = values }
            override fun putInt(key: String, value: Int) = apply { pending[key] = value }
            override fun putLong(key: String, value: Long) = apply { pending[key] = value }
            override fun putFloat(key: String, value: Float) = apply { pending[key] = value }
            override fun putBoolean(key: String, value: Boolean) = apply { pending[key] = value }
            override fun remove(key: String) = apply { removed += key }
            override fun clear() = apply { map.clear() }
            override fun commit(): Boolean { apply(); return true }
            override fun apply() {
                removed.forEach { map.remove(it) }
                pending.forEach { (k, v) -> if (v == null) map.remove(k) else map[k] = v }
            }
        }
    }

    private val manager = CredentialManager(MemoryPrefs())

    @Test
    fun `password sign-in is remembered without biometric`() {
        manager.rememberSignIn("s123456", "1234", "alabidae", enableBiometric = false)
        val login = manager.rememberedLogin!!
        assertEquals("s123456", login.identifier)
        assertEquals("1234", login.password)
        assertEquals("alabidae", login.school)
        assertFalse(manager.hasSavedCredentials)
        assertFalse(manager.isBiometricLoginEnabled)
    }

    @Test
    fun `an email login carries no school and biometric turns on where available`() {
        manager.rememberSignIn("a@b.com", "pw", "x", enableBiometric = true)
        assertNull(manager.rememberedLogin!!.school)
        assertTrue(manager.hasSavedCredentials)
        assertEquals("a@b.com" to "pw", manager.getCredentials())
    }

    @Test
    fun `forgetting the password keeps the username for the prefill`() {
        manager.rememberSignIn("s123456", "1234", "alabidae", enableBiometric = true)
        manager.forgetPassword()
        assertNull(manager.rememberedLogin)
        assertEquals("s123456", manager.rememberedIdentifier)
        assertFalse(manager.isBiometricLoginEnabled)
    }

    @Test
    fun `logout clears username, password and biometric`() {
        manager.rememberSignIn("s123456", "1234", "alabidae", enableBiometric = true)
        manager.clearCredentials()
        assertNull(manager.rememberedLogin)
        assertNull(manager.rememberedIdentifier)
        assertNull(manager.getCredentials())
        assertFalse(manager.isBiometricLoginEnabled)
    }
}
