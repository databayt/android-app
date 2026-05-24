package org.hogwarts.android.core.network.security

/**
 * Security constants and configuration for network layer.
 * AUTH-008: Security hardening with certificate pinning and TLS enforcement.
 *
 * Certificate pinning is enforced in release builds only.
 * Debug builds skip pinning for development flexibility.
 */
object SecurityConfig {
    // Certificate pins for production API
    // Generated using: openssl s_client -servername ed.databayt.org -connect ed.databayt.org:443 | openssl x509 -pubkey -noout | openssl pkey -pubin -outform der | openssl dgst -sha256 -binary | openssl enc -base64
    const val API_HOST_PATTERN = "*.databayt.org"

    // Primary pin (current Let's Encrypt / Cloudflare certificate for ed.databayt.org)
    // ISRG Root X1 - Let's Encrypt root CA pin
    const val PRIMARY_PIN = "sha256/C5+lpZ7tcVwmwQIMcRtPbsQtWLABXhQzejna0wHFr8M="

    // Backup pin (Cloudflare intermediate - for rotation)
    // Cloudflare Inc ECC CA-3
    const val BACKUP_PIN = "sha256/jQJTbIh0grw0/1TkHSumWb+Fs0Ggogr621gT3PvPKG0="

    // Minimum TLS version
    const val MIN_TLS_VERSION = "TLSv1.2"

    // Request timeout in seconds
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L
}
