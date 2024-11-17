package pl.npesystem.aidevs.models

import kotlinx.serialization.Serializable

@Serializable
data class ParsedUrlMarkdown(val info:String, val url:String, val complete:String, val extension: String, var content: String)
