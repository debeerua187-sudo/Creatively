package com.creativelydifferent.studio

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
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
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("Gemini is optional. Add your API key to generate original concepts.") }
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
                                status = "Starter concept created. Add a Gemini API key below for an original AI-written version."
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
                    Text("02  GEMINI AI", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Connect your own Google AI Studio API key to generate tailored scripts and scene direction.", color = Muted, fontSize = 13.sp, lineHeight = 19.sp)
                    OutlinedTextField(
                        value = apiKey, onValueChange = { apiKey = it },
                        label = { Text("Gemini API key") },
                        placeholder = { Text("Paste your key here") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true, modifier = Modifier.fillMaxWidth(), colors = fieldColors()
                    )
                    Text("Your key is kept in memory only and is not saved by this app. For personal testing, use a restricted key and monitor its usage. A production release should use a secure backend.", color = Muted, fontSize = 11.sp, lineHeight = 16.sp)
                    Text(status, color = if (status.startsWith("Gemini request failed")) Color(0xFFFF9B9B) else Muted, fontSize = 12.sp)
                }
            }

            if (draft.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("03  YOUR VIDEO BLUEPRINT", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(draft, color = Bright, fontSize = 14.sp, lineHeight = 22.sp)
                        OutlinedButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Video concept", draft))
                            Toast.makeText(context, "Concept copied", Toast.LENGTH_SHORT).show()
                        }) { Text("COPY SCRIPT", color = Accent) }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("THE CREATIVE STANDARD", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("• Vertical-first 9:16 storytelling\\n• Strong opening hook\\n• Original narration and scene direction\\n• Clean visuals: no hashtag overlays\\n• Hashtags stay separate from the video", color = Muted, lineHeight = 23.sp)
                }
            }
            Text("Version 1.1 • Gemini creative foundation", color = Muted, fontSize = 11.sp)
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
        val connection = (URL("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey").openConnection() as HttpURLConnection)
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 20000
            connection.readTimeout = 60000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
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

private fun makeDraft(topic: String, style: String, duration: String): String {
    val hook = when (style) {
        "Storytelling" -> "Nobody tells you what happens after the moment everything changes."
        "Poetic" -> "Some chapters end quietly. The person you become next does not."
        "Educational" -> "Here's one thing about $topic that most people overlook."
        "Funny" -> "I had a plan for $topic. Naturally, life had other ideas."
        else -> "If you're facing $topic, stop scrolling for ten seconds."
    }
    return """FORMAT: $duration • 9:16 vertical

HOOK (0–3 sec)
“$hook”

STORY
Bring the viewer into one specific, relatable moment connected to $topic. Use a close-up, a deliberate pause, and a visual detail that makes the feeling real.

VOICEOVER
“Maybe the point isn't to have every answer today. Maybe it's to take the next honest step, even when nobody is watching. You are allowed to begin again — and this time, do it your way.”

SCENE DIRECTION
1. Open on a cinematic close-up; make the first frame intriguing.
2. Cut to a wider atmospheric shot that establishes the emotion.
3. Use two or three purposeful detail shots; avoid generic filler.
4. End on a memorable image and let the final line breathe.

ON-SCREEN TEXT
Use only the hook and one short emphasis line. No hashtags on the video.

CAPTION
Write a short, human caption. Add hashtags separately in the caption field.

EDIT NOTES
9:16 • 1080 × 1920 • restrained transitions • clear voiceover • captions timed to speech • music below narration."""
}
