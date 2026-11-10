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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF10131B)
private val Panel = Color(0xFF1B2030)
private val Accent = Color(0xFFB9F36A)
private val Muted = Color(0xFFAAB1C2)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { StudioScreen() }
    }
}

@Composable
fun StudioScreen() {
    val context = LocalContext.current
    var topic by remember { mutableStateOf("") }
    var angle by remember { mutableStateOf("Motivational") }
    var duration by remember { mutableStateOf("30 seconds") }
    var draft by remember { mutableStateOf("") }
    val angles = listOf("Motivational", "Storytelling", "Poetic", "Educational", "Funny")
    val durations = listOf("15 seconds", "30 seconds", "60 seconds")
    MaterialTheme(colorScheme = darkColorScheme(
        background = Ink, surface = Panel, primary = Accent,
        onPrimary = Ink, onBackground = Color.White, onSurface = Color.White
    )) {
        Column(Modifier.fillMaxSize().background(Ink).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("CREATIVELY DIFFERENT", color = Accent, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            Text("Make them\nfeel something.", fontSize = 34.sp, lineHeight = 39.sp, fontWeight = FontWeight.ExtraBold)
            Text("Your short-form video creative studio.", color = Muted, fontSize = 15.sp)
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("01  CREATIVE BRIEF", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    OutlinedTextField(value = topic, onValueChange = { topic = it },
                        label = { Text("What's the video about?") },
                        placeholder = { Text("e.g. choosing yourself after heartbreak") },
                        modifier = Modifier.fillMaxWidth(), minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Accent,
                            unfocusedBorderColor = Color(0xFF485064), focusedLabelColor = Accent, cursorColor = Accent))
                    Text("Creative direction", color = Muted, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        angles.forEach { item -> FilterChip(selected = angle == item, onClick = { angle = item }, label = { Text(item) }) }
                    }
                    Text("Target length", color = Muted, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        durations.forEach { item -> FilterChip(selected = duration == item, onClick = { duration = item }, label = { Text(item) }) }
                    }
                    Button(onClick = { draft = makeDraft(topic.ifBlank { "the courage to begin again" }, angle, duration) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Accent, contentColor = Ink)) {
                        Text("CREATE VIDEO CONCEPT", fontWeight = FontWeight.Bold)
                    }
                }
            }
            if (draft.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("02  YOUR FIRST DRAFT", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(draft, fontSize = 14.sp, lineHeight = 22.sp)
                        OutlinedButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Video concept", draft))
                            Toast.makeText(context, "Draft copied", Toast.LENGTH_SHORT).show()
                        }) { Text("COPY SCRIPT") }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Panel), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("THE CREATIVE STANDARD", color = Accent, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("• Vertical-first 9:16 storytelling\n• Strong opening hook\n• Original narration and scene direction\n• Clean visuals: no hashtag overlays\n• Hashtags stay separate from the video", color = Muted, lineHeight = 23.sp)
                }
            }
            Text("Version 1.0 • Creative studio foundation", color = Muted, fontSize = 11.sp)
        }
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
Use only the hook and one short emphasis line. Keep typography minimal and readable. No hashtags on the video.

CAPTION
Write a short, human caption that invites genuine conversation. Add hashtags separately in the caption field, never as video overlays.

EDIT NOTES
9:16 • 1080 × 1920 • restrained transitions • clear voiceover • captions timed to speech • music below narration."""
}
