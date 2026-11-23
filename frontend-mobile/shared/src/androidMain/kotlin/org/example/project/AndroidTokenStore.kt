package org.example.project

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.example.project.auth.domain.SessionTokens
import org.example.project.auth.domain.TokenStore
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.security.KeyStore
import java.io.FileNotFoundException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** The encrypted token pair is stored outside Android automatic backup. */
class AndroidTokenStore(context: Context) : TokenStore {
    private val directory = context.noBackupFilesDir
    private val file = java.io.File(directory, "auth-session")
    private val atomicFile = android.util.AtomicFile(file)
    private val alias = "prompt-weaver-session-v1"
    private val mutex = Mutex()

    private fun key(create: Boolean): SecretKey? {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        if (!create) return null
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
            generateKey()
        }
    }

    override suspend fun write(tokens: SessionTokens): Unit = withContext(Dispatchers.IO) { mutex.withLock {
        val bytes = ByteArrayOutputStream().also { buffer ->
            DataOutputStream(buffer).use { out ->
                out.writeUTF(tokens.accessToken)
                out.writeUTF(tokens.refreshToken)
                out.writeInt(tokens.expiresIn)
            }
        }.toByteArray()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, requireNotNull(key(true)))
        val payload = Base64.encodeToString(cipher.iv + cipher.doFinal(bytes), Base64.NO_WRAP).toByteArray(Charsets.UTF_8)
        val output = atomicFile.startWrite()
        try { output.write(payload); atomicFile.finishWrite(output) }
        catch (error: Exception) { atomicFile.failWrite(output); throw error }
    } }

    override suspend fun read(): SessionTokens? = withContext(Dispatchers.IO) { mutex.withLock {
        val stored = try { atomicFile.readFully() } catch (_: FileNotFoundException) { return@withLock null }
        run {
            val encrypted = Base64.decode(stored, Base64.NO_WRAP)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, requireNotNull(key(false)), GCMParameterSpec(128, encrypted.copyOfRange(0, 12)))
            DataInputStream(ByteArrayInputStream(cipher.doFinal(encrypted.copyOfRange(12, encrypted.size)))).use {
                SessionTokens(it.readUTF(), it.readUTF(), it.readInt())
            }
        }
    } }

    override suspend fun clear(): Unit = withContext(Dispatchers.IO) { mutex.withLock { atomicFile.delete() } }
}
