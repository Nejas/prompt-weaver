package org.example.project.catalog.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import org.example.project.catalog.domain.*

@Serializable
internal data class ComposedPromptDto(@SerialName("template_version_id") val versionId: String, val prompt: String) {
    fun toDomain() = ComposedPrompt(versionId, prompt)
}
