package pl.npesystem.aidevs.client

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Serializable
data class TaskAnswerAsListString(val task: String, val apikey: String, val answer: @Contextual List<String>)
