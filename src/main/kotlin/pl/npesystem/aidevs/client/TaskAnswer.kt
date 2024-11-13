package pl.npesystem.aidevs.client

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable


@Serializable
sealed class TaskAnswer {
    abstract val task: String
    abstract val apikey: String
}

@Serializable
data class TaskAnswerStringList(
    override val task: String,
    override val apikey: String,
    @Contextual val answer: List<String>
) : TaskAnswer()

@Serializable
data class TaskAnswerString(
    override val task: String,
    override val apikey: String,
    val answer: String
) : TaskAnswer()