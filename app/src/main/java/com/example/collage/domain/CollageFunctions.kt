package com.example.collage.domain

import androidx.appfunctions.AppFunctionContext
import androidx.appfunctions.AppFunctionSerializable
import androidx.appfunctions.service.AppFunction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Exposes Collage App's capabilities to system AI agents.
 */
class CollageFunctions {

    /**
     * Summarizes the people detected in a video processing result.
     * @param appFunctionContext The execution context.
     * @param videoName The name of the video processed.
     * @param peopleCount The total number of unique people identified.
     * @return A status message for the agent.
     */
    @AppFunction
    suspend fun reportProcessingComplete(
        appFunctionContext: AppFunctionContext,
        videoName: String,
        peopleCount: Int
    ): String = withContext(Dispatchers.IO) {
        "Successfully identified $peopleCount unique people in '$videoName' and generated a collage."
    }
}

/**
 * Represents a person detected in the app.
 */
@AppFunctionSerializable
data class PersonEntity(
    /** Internal ID of the person */
    val id: Int,
    /** How many times they appeared in the video */
    val appearances: Int
)
