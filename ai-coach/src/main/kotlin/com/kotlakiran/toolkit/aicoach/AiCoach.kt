package com.kotlakiran.toolkit.aicoach

import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.HarmBlockThreshold
import com.google.firebase.ai.type.HarmCategory
import com.google.firebase.ai.type.SafetySetting
import com.google.firebase.ai.type.content
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import kotlin.coroutines.cancellation.CancellationException

/**
 * Installs Firebase App Check. Call from Application.onCreate before any AiCoach use.
 *
 * Debug builds need `debugImplementation("com.google.firebase:firebase-appcheck-debug")`
 * in the app module; the debug factory is loaded reflectively so release AARs never
 * ship the debug provider.
 */
object AppCheckInstaller {

    fun install(context: Context, debug: Boolean) {
        FirebaseApp.initializeApp(context)
        val check = FirebaseAppCheck.getInstance()
        if (debug) {
            try {
                val cls = Class.forName("com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory")
                val getInstance = cls.getMethod("getInstance")
                val factory = getInstance.invoke(null) as com.google.firebase.appcheck.AppCheckProviderFactory
                check.installAppCheckProviderFactory(factory)
            } catch (e: ReflectiveOperationException) {
                throw IllegalStateException(
                    "Debug App Check requested but firebase-appcheck-debug is not on the classpath. " +
                        "Add debugImplementation(\"com.google.firebase:firebase-appcheck-debug\") to the app module.",
                    e,
                )
            }
        } else {
            check.installAppCheckProviderFactory(PlayIntegrityAppCheckProviderFactory.getInstance())
        }
    }
}

/** Local rule-engine fallback used when the model is unreachable or returns nothing. */
fun interface AiFallback {
    /** Return a local answer, or null to surface the original failure. */
    fun answer(question: String, context: String): String?
}

/**
 * Gemini via Firebase AI Logic so the API key stays server-side (gated by App Check).
 * Defaults to the Vertex AI backend, verified on-device in production use.
 * Consuming app must apply the google-services plugin and ship google-services.json.
 */
class AiCoach(
    modelName: String = "gemini-flash-lite-latest",
    systemPrompt: String? = null,
    vertexLocation: String = "us-central1",
    private val fallback: AiFallback? = null,
) {
    private val model = Firebase.ai(backend = GenerativeBackend.vertexAI(location = vertexLocation)).generativeModel(
        modelName = modelName,
        systemInstruction = systemPrompt?.let { content { text(it) } },
        safetySettings = listOf(
            HarmCategory.HARASSMENT,
            HarmCategory.HATE_SPEECH,
            HarmCategory.SEXUALLY_EXPLICIT,
            HarmCategory.DANGEROUS_CONTENT,
        ).map { SafetySetting(it, HarmBlockThreshold.MEDIUM_AND_ABOVE) },
    )

    suspend fun ask(question: String, context: String = ""): String {
        val prompt = if (context.isBlank()) question else "Context:\n$context\n\nQuestion: $question"
        return try {
            val text = model.generateContent(prompt).text?.trim()
            if (!text.isNullOrBlank()) {
                text
            } else {
                fallback?.answer(question, context) ?: "No answer available right now."
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            fallback?.answer(question, context) ?: throw e
        }
    }
}
