package dev.olvex.demo

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.olvex.core.Olvex
import dev.olvex.getPlatform

private val Green = Color(0xFF4ade80)
private val Blue = Color(0xFF60a5fa)
private val Red = Color(0xFFf87171)
private val DarkBg = Color(0xFF060a06)
private val CardBg = Color(0xFF0d160d)
private val Muted = Color(0xFF3a5c3a)
private val SubMuted = Color(0xFF6b7c6b)

private enum class VerificationStatus(val label: String, val color: Color) {
    NOT_STARTED("Not started", SubMuted),
    IN_PROGRESS("In progress", Blue),
    PASSED("Passed", Green),
    FAILED("Failed", Red)
}

private data class VerificationScenario(
    val id: String,
    val title: String,
    val expected: String
)

private val verificationScenarios = listOf(
    VerificationScenario(
        id = "offline_queue",
        title = "Offline queue persistence",
        expected = "Events generated offline must appear after network recovery."
    ),
    VerificationScenario(
        id = "retry_recovery",
        title = "Retry after network failure",
        expected = "Transient backend/network failures should recover without event loss."
    ),
    VerificationScenario(
        id = "restart_recovery",
        title = "Restart delivery recovery",
        expected = "Queued events should survive app restart and be delivered on next launch."
    ),
    VerificationScenario(
        id = "crash_restore",
        title = "Crash restore on next launch",
        expected = "Forced crash must be visible in dashboard after relaunch."
    )
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun App() {
    var customEventName by remember { mutableStateOf("") }
    var log by remember { mutableStateOf(listOf<String>()) }
    var scenarioStatuses by remember {
        mutableStateOf(
            verificationScenarios.associate { it.id to VerificationStatus.NOT_STARTED }
        )
    }
    val platformName = remember { getPlatform().name }

    fun addLog(msg: String) {
        log = listOf(msg) + log.take(199)
    }

    fun updateScenarioStatus(id: String, status: VerificationStatus) {
        scenarioStatuses = scenarioStatuses.toMutableMap().apply {
            this[id] = status
        }
        addLog("Matrix '${id}' → ${status.label}")
    }

    MaterialTheme {
        Surface(color = DarkBg, modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                // Header
                Text(
                    "olvex",
                    color = Green,
                    fontSize = 24.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    "SDK Demo",
                    color = SubMuted,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    platformName,
                    color = SubMuted,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                // ── Sessions ──────────────────────────────────────────
                SectionLabel("Session")
                Spacer(Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OlvexButton("Start Session", Green) {
                        Olvex.startSession()
                        addLog("✓ Session started via Olvex.startSession()")
                    }
                    OlvexButton("End Session", Green) {
                        Olvex.endSession()
                        addLog("✓ Session ended via Olvex.endSession()")
                    }
                }

                Spacer(Modifier.height(24.dp))

                // ── Custom event ───────────────────────────────────────
                SectionLabel("Custom Event")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customEventName,
                    onValueChange = { customEventName = it },
                    placeholder = { Text("event_name", color = Color(0xFF4a5c4a)) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Green,
                        unfocusedBorderColor = Color(0xFF2a3c2a),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Green
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OlvexButton(
                    text = "Track Event",
                    color = Blue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val name = customEventName.trim()
                    if (name.isNotBlank()) {
                        Olvex.track(name)
                        addLog("✓ CustomEvent '$name' sent")
                        customEventName = ""
                    }
                }
                Spacer(Modifier.height(8.dp))
                OlvexButton(
                    text = "Track Event With Properties",
                    color = Blue,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val name = customEventName.trim().ifBlank { "demo_event_with_props" }
                    Olvex.track(
                        name = name,
                        properties = mapOf(
                            "screen" to "demo_test_bench",
                            "platform" to platformName,
                            "case" to "custom_event_with_properties"
                        )
                    )
                    addLog("✓ CustomEvent '$name' with properties sent")
                }

                Spacer(Modifier.height(24.dp))

                // ── Crash ──────────────────────────────────────────────
                SectionLabel("Crash")
                Spacer(Modifier.height(8.dp))
                OlvexButton(
                    text = "Force Kotlin Crash",
                    color = Red,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    addLog("💥 Forcing crash — relaunch to see it in dashboard")
                    throw RuntimeException("Olvex test crash — intentional")
                }
                if (supportsNativeSignalCrashTest) {
                    Spacer(Modifier.height(8.dp))
                    OlvexButton(
                        text = "Force Native SIGABRT (iOS)",
                        color = Red,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        addLog("💥 Forcing SIGABRT — relaunch to see native crash in dashboard")
                        triggerNativeSignalCrash()
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Crash reports are sent on next app launch.",
                    color = SubMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(Modifier.height(24.dp))

                // ── Verification matrix ─────────────────────────────
                SectionLabel("Verification Matrix")
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = CardBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        verificationScenarios.forEach { scenario ->
                            val status = scenarioStatuses[scenario.id] ?: VerificationStatus.NOT_STARTED
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        scenario.title,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        status.label,
                                        color = status.color,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                                Text(
                                    scenario.expected,
                                    color = SubMuted,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                FlowRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    MatrixButton("Start", Blue) { updateScenarioStatus(scenario.id, VerificationStatus.IN_PROGRESS) }
                                    MatrixButton("Pass", Green) { updateScenarioStatus(scenario.id, VerificationStatus.PASSED) }
                                    MatrixButton("Fail", Red) { updateScenarioStatus(scenario.id, VerificationStatus.FAILED) }
                                    MatrixButton("Reset", SubMuted) { updateScenarioStatus(scenario.id, VerificationStatus.NOT_STARTED) }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // ── Event log ──────────────────────────────────────────
                SectionLabel("Log")
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = CardBg,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 180.dp, max = 280.dp)
                ) {
                    LazyColumn(modifier = Modifier.padding(12.dp)) {
                        if (log.isEmpty()) {
                            item {
                                Text(
                                    "No events yet. Try the buttons above.",
                                    color = Muted,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                        items(log) { entry ->
                            Text(
                                entry,
                                color = SubMuted,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MatrixButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.16f)),
        shape = RoundedCornerShape(6.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text,
            color = color,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text.uppercase(),
        color = Muted,
        fontSize = 11.sp,
        fontFamily = FontFamily.Monospace,
        letterSpacing = 1.sp
    )
}

@Composable
private fun OlvexButton(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = color.copy(alpha = 0.12f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text,
            color = color,
            fontSize = 13.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}
