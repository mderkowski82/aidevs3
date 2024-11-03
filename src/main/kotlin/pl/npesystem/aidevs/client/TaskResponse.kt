package pl.npesystem.aidevs.client

import kotlinx.serialization.Serializable

@Serializable
data class TaskResponse(val code: Int, val message: String)
