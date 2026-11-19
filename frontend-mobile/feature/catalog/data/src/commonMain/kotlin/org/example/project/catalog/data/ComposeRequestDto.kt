package org.example.project.catalog.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
internal data class ComposeRequestDto(@SerialName("template_version_id") val versionId: String, val parameters: Map<String, String>)
