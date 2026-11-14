package com.kshrd.core.auth

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import androidx.core.content.edit
import com.kshrd.core.api.ApiJson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.GeneralSecurityException
import java.security.KeyStore
import java.security.ProviderException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "KeystoreTokenStore"
private const val ANDROID_KEYSTORE = "AndroidKeyStore"
private const val KEY_ALIAS = "webblock.session"
private const val TRANSFORMATION = "AES/GCM/NoPadding"
private const val KEY_SIZE_BITS = 256
private const val GCM_TAG_BITS = 128
private const val GCM_IV_BYTES = 12

/** The prefs file name; `res/xml/backup_rules.xml` and `data_extraction_rules.xml` in `:app` exclude it. */
const val SESSION_PREFS_NAME = "webblock_session"
private const val PREF_TOKENS = "tokens"

/**
 * Keeps the tokens encrypted with an AES-GCM key that lives in the Android Keystore and never leaves it.
 * The ciphertext (IV + data, Base64) sits in a private prefs file that is excluded from backup and device transfer.
 * A value that can't be decrypted (a restored phone, a reset key) is dropped, and the owner signs in again.
 */
@Singleton
class KeystoreTokenStore
    @Inject
    constructor(
        @ApplicationContext context: Context,
    ) : TokenStore {
        private val prefs = context.getSharedPreferences(SESSION_PREFS_NAME, Context.MODE_PRIVATE)

        override fun load(): StoredTokens? {
            val encoded = prefs.getString(PREF_TOKENS, null) ?: return null
            return try {
                ApiJson.decodeFromString(StoredTokens.serializer(), decrypt(encoded))
            } catch (e: GeneralSecurityException) {
                dropUnreadable(e)
            } catch (e: ProviderException) {
                dropUnreadable(e)
            } catch (e: IllegalArgumentException) {
                // Bad Base64 or JSON (SerializationException is an IllegalArgumentException).
                dropUnreadable(e)
            }
        }

        override fun save(tokens: StoredTokens) {
            try {
                val encrypted = encrypt(ApiJson.encodeToString(StoredTokens.serializer(), tokens))
                prefs.edit { putString(PREF_TOKENS, encrypted) }
            } catch (e: GeneralSecurityException) {
                // The session still works until the app closes; the owner signs in again next time.
                Log.w(TAG, "Could not store the session", e)
            } catch (e: ProviderException) {
                Log.w(TAG, "Could not store the session", e)
            }
        }

        override fun clear() {
            prefs.edit { remove(PREF_TOKENS) }
        }

        private fun dropUnreadable(e: Exception): StoredTokens? {
            Log.w(TAG, "Stored session unreadable; signing out", e)
            clear()
            return null
        }

        private fun encrypt(plain: String): String {
            val cipher = Cipher.getInstance(TRANSFORMATION).apply { init(Cipher.ENCRYPT_MODE, key()) }
            val sealed = cipher.iv + cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
            return Base64.encodeToString(sealed, Base64.NO_WRAP)
        }

        private fun decrypt(encoded: String): String {
            val sealed = Base64.decode(encoded, Base64.NO_WRAP)
            require(sealed.size > GCM_IV_BYTES) { "Stored session too short" }
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.DECRYPT_MODE, key(), GCMParameterSpec(GCM_TAG_BITS, sealed, 0, GCM_IV_BYTES))
            return String(cipher.doFinal(sealed, GCM_IV_BYTES, sealed.size - GCM_IV_BYTES), Charsets.UTF_8)
        }

        private fun key(): SecretKey {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let { return it.secretKey }
            val spec =
                KeyGenParameterSpec
                    .Builder(KEY_ALIAS, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(KEY_SIZE_BITS)
                    .build()
            return KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
                .apply { init(spec) }
                .generateKey()
        }
    }
