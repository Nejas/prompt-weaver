package org.example.project

import kotlinx.cinterop.ExperimentalForeignApi
import org.example.project.chat.data.DataStoreLocalChatTemplateStore
import org.example.project.chat.data.LocalChatTemplateStore
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSUserDefaults

class IosLocalChatTemplateStore : LocalChatTemplateStore by iosTemplateStore

@OptIn(ExperimentalForeignApi::class)
private val iosTemplateStore: LocalChatTemplateStore by lazy {
    val directory = requireNotNull(NSFileManager.defaultManager.URLForDirectory(
        directory = NSApplicationSupportDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = true,
        error = null,
    )?.path)
    val legacy = NSUserDefaults.standardUserDefaults
    DataStoreLocalChatTemplateStore.create(
        path = "$directory/chat_templates.preferences_pb",
        readLegacy = { legacy.stringForKey("chat_templates") },
        clearLegacy = { legacy.removeObjectForKey("chat_templates") },
    )
}
