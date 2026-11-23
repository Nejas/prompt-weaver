package org.example.project

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.example.project.auth.domain.SessionTokens
import org.example.project.auth.domain.TokenStore
import platform.CoreFoundation.*
import platform.Foundation.*
import platform.Security.*

@OptIn(ExperimentalForeignApi::class)
class IosTokenStore : TokenStore {
    private companion object { val mutex = Mutex() }

    private inline fun <T> withQuery(block: (CFMutableDictionaryRef?) -> T): T {
        val service = CFBridgingRetain("org.example.project.session")
        val account = CFBridgingRetain("tokens")
        val query = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null)
        try {
            CFDictionarySetValue(query, kSecClass, kSecClassGenericPassword)
            CFDictionarySetValue(query, kSecAttrService, service)
            CFDictionarySetValue(query, kSecAttrAccount, account)
            return block(query)
        } finally {
            CFRelease(query)
            CFBridgingRelease(service)
            CFBridgingRelease(account)
        }
    }

    override suspend fun read(): SessionTokens? = withContext(Dispatchers.Default) { mutex.withLock {
        withQuery { query -> memScoped {
            CFDictionarySetValue(query, kSecReturnData, kCFBooleanTrue)
            CFDictionarySetValue(query, kSecMatchLimit, kSecMatchLimitOne)
            val result = alloc<CFTypeRefVar>()
            val status = SecItemCopyMatching(query, result.ptr)
            if (status == errSecItemNotFound) return@withQuery null
            check(status == errSecSuccess) { "Keychain read failed: $status" }
            val data = CFBridgingRelease(result.value) as? NSData ?: error("Invalid Keychain data")
            val text = NSString.create(data, NSUTF8StringEncoding) as? String ?: error("Invalid Keychain encoding")
            val parts = text.split('\n')
            require(parts.size == 3 && parts[0].isNotEmpty() && parts[1].isNotEmpty())
            SessionTokens(parts[0], parts[1], parts[2].toInt().also { require(it >= 0) })
        } }
    } }

    override suspend fun write(tokens: SessionTokens): Unit = withContext(Dispatchers.Default) { mutex.withLock {
        require('\n' !in tokens.accessToken && '\n' !in tokens.refreshToken)
        @Suppress("CAST_NEVER_SUCCEEDS")
        val string = "${tokens.accessToken}\n${tokens.refreshToken}\n${tokens.expiresIn}" as NSString
        val data = string.dataUsingEncoding(NSUTF8StringEncoding) ?: error("Invalid token encoding")
        val value = CFBridgingRetain(data)
        try { withQuery { query ->
            val update = CFDictionaryCreateMutable(kCFAllocatorDefault, 0, null, null)
            try {
                CFDictionarySetValue(update, kSecValueData, value)
                CFDictionarySetValue(update, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
                var status = SecItemUpdate(query, update)
                if (status == errSecItemNotFound) {
                    CFDictionarySetValue(query, kSecValueData, value)
                    CFDictionarySetValue(query, kSecAttrAccessible, kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly)
                    status = SecItemAdd(query, null)
                }
                check(status == errSecSuccess) { "Keychain write failed: $status" }
            } finally { CFRelease(update) }
        } } finally { CFBridgingRelease(value) }
    } }

    override suspend fun clear(): Unit = withContext(Dispatchers.Default) { mutex.withLock {
        withQuery { query ->
            val status = SecItemDelete(query)
            check(status == errSecSuccess || status == errSecItemNotFound) { "Keychain delete failed: $status" }
        }
    } }
}
