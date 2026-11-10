package com.creativelydifferent.studio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.runtime.DisposableEffect
import java.util.Locale
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

private val Ink = Color(0xFF10131B)
private val Panel = Color(0xFF1B2030)
private val Accent = Color(0xFFB9F36A)
private val Muted = Color(0xFFAAB1C2)
private val Bright = Color(0xFFF4F6FC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudioScreen() }
    }
}

@Composable
fun StudioScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var topic by remember { mutableStateOf("") }
    var angle by remember { mutableStateOf("Motivational") }
    var duration by remember { mutableStateOf("30 seconds") }
    var apiKey by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf("") }
    var savedProjects by remember { mutableStateOf(loadProjects(context)) }
    var busy by remember { mutableStateOf(false) }
    var tts by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsReady by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        val engine = TextToSpeech(context) { result ->
            ttsReady = result == TextToSpeech.SUCCESS
            if (result == TextToSpeech.SUCCESS) tts?.language = Locale.US
        }
        tts = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            tts = null
            ttsReady = false
        }
    }
    var status by remember { mutableStateOf("Free mode is ready. No account, subscription, or API key required.") }
    val angles = listOf("Motivational", "Storytelling", "Poetic", "Educational", "Funny")
    val durations = listOf("15 seconds", "30 seconds", "60 seconds")

    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent,
        onPrimary = Ink, onBackground = Bright, onSurface = Bright
    )) {
        Column(
            Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("CREATIVELY DIFFERENT", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Make them\\nfeel something.", color = Bright, fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.ExtraBold)
            Text("Your short-form video creative studio.", color = Muted, fontSize = 15.sp)

            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("01  CREATIVE BRIEF", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(
                        value = topic, onValueChange = { topic = it },
                        label = { Text("What's the video about?") },
                        placeholder = { Text("e.g. choosing yourself after heartbreak") },
                        modifier = Modifier.fillMaxWidth(), minLines = 2,
                        colors = fieldColors()
                    )
                    Text("Creative direction", color = Muted, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        angles.take(3).forEach { item ->
                            Box(Modifier.weight(1f)) {
                                FilterChip(selected = angle == item, onClick = { angle = item },
                                    label = { Text(item, maxLines = 1, fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        angles.drop(3).forEach { item ->
                            Box(Modifier.weight(1f)) {
                                FilterChip(selected = angle == item, onClick = { angle = item },
                                    label = { Text(item, maxLines = 1, fontSize = 11.sp) }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                        Spacer(Modifier.weight(1f))
                    }
                    Text("Target length", color = Muted, fontSize = 13.sp)
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        durations.forEach { item ->
                            Box(Modifier.weight(1f)) {
                                FilterChip(selected = duration == item, onClick = { duration = item },
                                    label = { Text(item.replace(" seconds", "s"), maxLines = 1, fontSize = 12.sp) },
                                    modifier = Modifier.fillMaxWidth())
                            }
                        }
                    }
                    Button(
                        onClick = {
                            if (topic.isBlank()) {
                                status = "Add a topic first so the creative direction has something to work with."
                            } else if (apiKey.isBlank()) {
                                draft = makeDraft(topic, angle, duration)
                                status = "Free offline concept created. No API key or subscription needed."
                            } else {
                                busy = true
                                status = "Gemini is developing your concept…"
                                scope.launch {
                                    try {
                                        draft = generateWithGemini(apiKey.trim(), topic.trim(), angle, duration)
                                        status = "Your Gemini concept is ready."
                                    } catch (e: Exception) {
                                        status = "Gemini request failed: ${e.message ?: "Check your key and connection."}"
                                    } finally {
                                        busy = false
                                    }
                                }
                            }
                        },
                        enabled = !busy,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)
                    ) {
                        if (busy) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Ink, strokeWidth = 2.dp)
                        else Text(if (apiKey.isBlank()) "CREATE VIDEO CONCEPT" else "CREATE WITH GEMINI", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("02  OPTIONAL CLOUD AI", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("The studio works without this. You can optionally add a free-tier Gemini key for more varied AI scripts. Never required; usage limits and provider terms apply.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    OutlinedTextField(
                        value = apiKey, onValueChange = { apiKey = it },
                        label = { Text("Optional Gemini API key") },
                        placeholder = { Text("Paste your key here") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors()
                    )
                    Text("Leave blank for completely offline creative planning. If supplied, your key stays in memory only. Free-tier availability is controlled by the provider and may change.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                    Text(status, color = if (status.startsWith("Gemini request failed")) Color(0xFFFF9B9B) else Muted, fontSize = 12.sp)
                }
            }

            if (draft.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("03  YOUR VIDEO BLUEPRINT", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(draft, color = Bright, fontSize = 14.sp, lineHeight = 22.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                savedProjects = saveProject(context, topic, angle, duration, draft)
                                Toast.makeText(context, "Project saved on this device", Toast.LENGTH_SHORT).show()
                            }) { Text("SAVE PROJECT", color = Accent) }
                            OutlinedButton(onClick = {
                                val share = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "CreativelyDifferent video blueprint")
                                    putExtra(Intent.EXTRA_TEXT, draft)
                                }
                                context.startActivity(Intent.createChooser(share, "Share video blueprint"))
                            }) { Text("SHARE", color = Accent) }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                val voiceover = extractVoiceover(draft)
                                if (voiceover.isBlank()) {
                                    Toast.makeText(context, "No voiceover section found", Toast.LENGTH_SHORT).show()
                                } else if (ttsReady) {
                                    tts?.speak(voiceover, TextToSpeech.QUEUE_FLUSH, null, "creatively_voiceover")
                                } else {
                                    Toast.makeText(context, "Voice engine is still starting", Toast.LENGTH_SHORT).show()
                                }
                            }) { Text("PLAY VOICEOVER", color = Accent) }
                            OutlinedButton(onClick = {
                                tts?.stop()
                                Toast.makeText(context, "Voiceover stopped", Toast.LENGTH_SHORT).show()
                            }) { Text("STOP", color = Accent) }
                        }
                        OutlinedButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Video concept", draft))
                            Toast.makeText(context, "Concept copied", Toast.LENGTH_SHORT).show()
                        }) { Text("COPY SCRIPT", color = Accent) }
                    }
                }
            }

            if (savedProjects.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("04  SAVED PROJECTS", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        savedProjects.forEachIndexed { index, project ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(project.optString("topic"), color = Bright, fontWeight = FontWeight.SemiBold)
                                    Text(project.optString("style") + " • " + project.optString("duration"), color = Muted, fontSize = 11.sp)
                                }
                                TextButton(onClick = {
                                    topic = project.optString("topic")
                                    angle = project.optString("style")
                                    duration = project.optString("duration")
                                    draft = project.optString("draft")
                                    status = "Project loaded."
                                }) { Text("OPEN", color = Accent) }
                            }
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("THE CREATIVE STANDARD", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("• Vertical-first 9:16 storytelling\\n• Strong opening hook\\n• Original narration and scene direction\\n• Clean visuals: no hashtag overlays\\n• Hashtags stay separate from the video", color = Muted, lineHeight = 23.sp)
                }
            }
            Text("Version 1.3 • Free offline-first studio", color = Muted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Bright, unfocusedTextColor = Bright,
    focusedBorderColor = Accent, unfocusedBorderColor = Color(0xFF596176),
    focusedLabelColor = Accent, unfocusedLabelColor = Muted,
    cursorColor = Accent, focusedPlaceholderColor = Muted, unfocusedPlaceholderColor = Muted
)

private suspend fun generateWithGemini(apiKey: String, topic: String, style: String, duration: String): String =
    withContext(Dispatchers.IO) {
        val prompt = """
You are the creative director and senior short-form filmmaker for CreativelyDifferent.
Create a distinctive, emotionally engaging $duration vertical 9:16 video concept.
Topic: $topic
Creative direction: $style
Requirements:
- Write an original, specific opening hook for the first 1-3 seconds.
- Give a coherent story arc with emotional progression, not generic motivational filler.
- Write natural voiceover sized for the requested duration.
- Provide a timed, numbered shot list with concrete camera framing, movement, lighting, and visual details.
- Include minimal on-screen text only when it adds meaning.
- Write a separate social caption and a separate optional hashtag list.
- NEVER put hashtags on screen or in the video.
- Add practical sound design and editing notes.
Use clear section headings. Be original, cinematic, human, and specific. Do not claim to have generated footage.
""".trimIndent()
        val body = JSONObject()
            .put("contents", org.json.JSONArray().put(JSONObject().put("parts", org.json.JSONArray().put(JSONObject().put("text", prompt)))))
            .put("generationConfig", JSONObject().put("temperature", 0.9).put("maxOutputTokens", 3000))
        val connection = (URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 20000
            connection.readTimeout = 60000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.setRequestProperty("x-goog-api-key", apiKey)
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream else connection.errorStream
            val response = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) {
                val message = try { JSONObject(response).optJSONObject("error")?.optString("message") } catch (_: Exception) { null }
                throw IllegalStateException(message?.takeIf { it.isNotBlank() } ?: "HTTP $code")
            }
            val json = JSONObject(response)
            json.optJSONArray("candidates")?.optJSONObject(0)?.optJSONObject("content")
                ?.optJSONArray("parts")?.optJSONObject(0)?.optString("text")
                ?.takeIf { it.isNotBlank() } ?: throw IllegalStateException("Gemini returned no text.")
        } finally {
            connection.disconnect()
        }
    }

private fun extractVoiceover(script: String): String {
    val marker = "VOICEOVER"
    val start = script.indexOf(marker, ignoreCase = true)
    if (start < 0) return ""
    val contentStart = script.indexOf('\n', start).let { if (it < 0) return "" else it + 1 }
    val nextSection = Regex("\\n[A-Z][A-Z0-9 ()–-]{2,}\\n").find(script, contentStart)
    return script.substring(contentStart, nextSection?.range?.first ?: script.length)
        .trim().trim('“', '”', '"')
}

private fun makeDraft(topic: String, style: String, duration: String): String {
    val subject = topic.trim().trimEnd('.', '!', '?')
    val hook = when (style) {
        "Storytelling" -> "There was a moment when $subject stopped being just an idea — it became real."
        "Poetic" -> "Somewhere between who you were and who you're becoming, $subject found its meaning."
        "Educational" -> "Most people misunderstand $subject. Here's the part worth remembering."
        "Funny" -> "I thought I had $subject figured out. That was my first mistake."
        else -> "This is your reminder: $subject doesn't get to decide how your story ends."
    }
    val voiceover = when (style) {
        "Storytelling" -> "I remember when it first started. I thought I knew how the story would go. Then life changed the plan. What mattered wasn't having a perfect answer. It was choosing what to do next. And that choice changed everything."
        "Poetic" -> "Not every change arrives like thunder. Sometimes it is a quiet decision, a breath you finally take, a small light left on in a dark room. Let $subject be part of the becoming, not the thing that keeps you standing still."
        "Educational" -> "Start with the facts. Notice what is working, what is not, and what you can actually change. Small, consistent steps beat dramatic promises. Understand the process, make one useful decision, and build from there."
        "Funny" -> "Step one: make a plan. Step two: feel confident about the plan. Step three: discover that absolutely nobody told reality about the plan. So take a breath, adjust, and pretend this was the strategy all along."
        else -> "You don't need to have the whole road figured out. You need one honest reason to take the next step. Let the hard days teach you, not define you. Your story is still moving, and you still get a say in what happens next."
    }
    val shots = when (style) {
        "Educational" -> listOf("Overhead detail shot introducing the subject", "Close-up of the key idea or action", "Demonstration in a clean, uncluttered frame", "Final direct-to-camera takeaway")
        "Funny" -> listOf("Deadpan close-up before the reveal", "Quick cut to the unexpected problem", "Reaction shot with a deliberate pause", "Punchline delivered straight to camera")
        "Poetic" -> listOf("Soft natural light across an empty space", "Slow detail shot with shallow depth of field", "Wide silhouette or landscape with negative space", "Hold the final image after the last words")
        "Storytelling" -> listOf("Intimate close-up establishing the moment", "Moving shot that reveals the setting", "Three quick detail cuts that build tension", "Quiet wide shot that resolves the story")
        else -> listOf("Strong close-up with direct eye contact", "Wide shot showing the world around the subject", "Progress detail: hands, footsteps, or meaningful object", "Confident final frame with room for the closing line")
    }
    val seconds = when (duration) { "15 seconds" -> 15; "60 seconds" -> 60; else -> 30 }
    val sceneLines = shots.mapIndexed { index, shot ->
        val from = index * seconds / 4
        val to = (index + 1) * seconds / 4
        "${index + 1}. ${from}s–${to}s: $shot. Use deliberate framing, controlled movement, and natural or motivated light."
    }.joinToString("\\n")
    return """FORMAT: $duration • vertical 9:16 • planned for 1080 × 1920

HOOK (first 1–3 seconds)
“$hook”

CREATIVE IDEA
Build a specific, emotionally clear short video about $subject. Keep one central message and make every shot support it.

VOICEOVER
“$voiceover”

STORYBOARD / SHOT LIST
$sceneLines

ON-SCREEN TEXT
Use the hook as the opening text and one short closing phrase: “Keep becoming.”
No hashtags, usernames, or caption hashtags on the video.

CAPTION (separate from video)
$subject — one step, one choice, one moment at a time.

OPTIONAL HASHTAGS (caption only)
#CreativelyDifferent #YourStory #KeepBecoming

SOUND AND EDIT
Use a restrained instrumental bed beneath narration. Cut on meaningful words, keep transitions simple, add a short fade at the end, and leave breathing room after the final line.

PRODUCTION NOTE
This is an offline creative blueprint. Add your own footage or images to turn the shot list into a finished video."""
}
}
