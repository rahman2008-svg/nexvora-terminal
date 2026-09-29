package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.developer.DevTools
import com.example.ui.components.GlassCard
import com.example.ui.theme.DarkBackground
import kotlinx.coroutines.launch

@Composable
fun DevToolsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val toolTabs = listOf(
        "JSON", "Base64", "URL", "Regex", "UUID", "Hash", "Timestamp", "Case", "HTTP", "Env"
    )
    var selectedTool by remember { mutableStateOf("JSON") }

    fun copyToClipboard(text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("NexVora", text))
        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Developer Utilities",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Utility Category Selector
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(toolTabs) { tool ->
                FilterChip(
                    selected = selectedTool == tool,
                    onClick = { selectedTool = tool },
                    label = { Text(tool) }
                )
            }
        }

        // Selected Tool View
        when (selectedTool) {
            "JSON" -> {
                var jsonInput by remember { mutableStateOf("{\"app\":\"NexVora\",\"status\":\"ready\",\"version\":1}") }
                var jsonOutput by remember { mutableStateOf("") }

                GlassCard {
                    Text("JSON Formatter & Minifier", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        label = { Text("Raw JSON Input") },
                        modifier = Modifier.fillMaxWidth().height(120.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val (ok, res) = DevTools.formatJson(jsonInput)
                                jsonOutput = res
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Beautify") }
                        OutlinedButton(
                            onClick = {
                                val (ok, res) = DevTools.minifyJson(jsonInput)
                                jsonOutput = res
                            },
                            modifier = Modifier.weight(1f)
                        ) { Text("Minify") }
                    }
                    if (jsonOutput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Output:", fontWeight = FontWeight.SemiBold)
                            Text("Copy", color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { copyToClipboard(jsonOutput) })
                        }
                        Text(
                            text = jsonOutput,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(DarkBackground, RoundedCornerShape(8.dp))
                                .padding(8.dp)
                        )
                    }
                }
            }

            "Base64" -> {
                var b64Input by remember { mutableStateOf("NexVora Developer Terminal") }
                var b64Output by remember { mutableStateOf("") }
                GlassCard {
                    Text("Base64 Encoder / Decoder", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = b64Input,
                        onValueChange = { b64Input = it },
                        label = { Text("Text or Base64 Input") },
                        modifier = Modifier.fillMaxWidth().height(100.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { b64Output = DevTools.encodeBase64(b64Input) }, modifier = Modifier.weight(1f)) { Text("Encode") }
                        OutlinedButton(onClick = { b64Output = DevTools.decodeBase64(b64Input).second }, modifier = Modifier.weight(1f)) { Text("Decode") }
                    }
                    if (b64Output.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text("Output: $b64Output", fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.clickable { copyToClipboard(b64Output) })
                    }
                }
            }

            "URL" -> {
                var urlInput by remember { mutableStateOf("https://example.com/search?q=nexvora terminal&lang=kotlin") }
                var urlOutput by remember { mutableStateOf("") }
                GlassCard {
                    Text("URL Encoder / Decoder", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        modifier = Modifier.fillMaxWidth().height(90.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { urlOutput = DevTools.encodeUrl(urlInput) }, modifier = Modifier.weight(1f)) { Text("Encode") }
                        OutlinedButton(onClick = { urlOutput = DevTools.decodeUrl(urlInput).second }, modifier = Modifier.weight(1f)) { Text("Decode") }
                    }
                    if (urlOutput.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(urlOutput, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.clickable { copyToClipboard(urlOutput) })
                    }
                }
            }

            "Regex" -> {
                var pattern by remember { mutableStateOf("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}") }
                var testString by remember { mutableStateOf("Contact support@nexvora.lab or dev@example.com for info.") }
                val (ok, matches) = remember(pattern, testString) { DevTools.testRegex(pattern, testString) }

                GlassCard {
                    Text("Regular Expression Tester", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = pattern, onValueChange = { pattern = it }, label = { Text("Regex Pattern") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(value = testString, onValueChange = { testString = it }, label = { Text("Test String") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Matches found: ${matches.size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    matches.forEach { m ->
                        Text("• \"${m.text}\" (index: ${m.range.first}..${m.range.last})", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }

            "UUID" -> {
                var uuids by remember { mutableStateOf(DevTools.generateUuids(5)) }
                GlassCard {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("UUID v4 Generator", fontWeight = FontWeight.Bold)
                        Button(onClick = { uuids = DevTools.generateUuids(5) }) { Text("Generate") }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    uuids.forEach { u ->
                        Text(u, fontFamily = FontFamily.Monospace, fontSize = 12.sp, modifier = Modifier.clickable { copyToClipboard(u) }.padding(vertical = 4.dp))
                    }
                }
            }

            "Hash" -> {
                var hashInput by remember { mutableStateOf("NexVora2026") }
                GlassCard {
                    Text("Cryptographic Hash Generator", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = hashInput, onValueChange = { hashInput = it }, label = { Text("Input String") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("MD5:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(DevTools.calculateHash(hashInput, "MD5"), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.clickable { copyToClipboard(DevTools.calculateHash(hashInput, "MD5")) })
                        Text("SHA-1:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(DevTools.calculateHash(hashInput, "SHA-1"), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        Text("SHA-256:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(DevTools.calculateHash(hashInput, "SHA-256"), fontFamily = FontFamily.Monospace, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            "Timestamp" -> {
                var epochSec by remember { mutableStateOf(System.currentTimeMillis() / 1000) }
                GlassCard {
                    Text("Unix Epoch Timestamp Converter", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = epochSec.toString(), onValueChange = { epochSec = it.toLongOrNull() ?: 0L }, label = { Text("Epoch (Seconds)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Converted Date: ${DevTools.epochToFormatted(epochSec)}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    Button(onClick = { epochSec = System.currentTimeMillis() / 1000 }, modifier = Modifier.padding(top = 8.dp)) { Text("Current Epoch") }
                }
            }

            "Case" -> {
                var caseInput by remember { mutableStateOf("nexvora terminal developer workstation") }
                GlassCard {
                    Text("Text Case Formatter", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = caseInput, onValueChange = { caseInput = it }, label = { Text("Input String") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("camelCase: ${DevTools.toCamelCase(caseInput)}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text("snake_case: ${DevTools.toSnakeCase(caseInput)}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text("kebab-case: ${DevTools.toKebabCase(caseInput)}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                        Text("UPPERCASE: ${caseInput.uppercase()}", fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                    }
                }
            }

            "HTTP" -> {
                var httpUrl by remember { mutableStateOf("https://httpbin.org/get") }
                var httpMethod by remember { mutableStateOf("GET") }
                var httpResult by remember { mutableStateOf("") }
                var isLoadingHttp by remember { mutableStateOf(false) }

                GlassCard {
                    Text("HTTP Request Tester", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedTextField(value = httpMethod, onValueChange = { httpMethod = it }, modifier = Modifier.width(90.dp))
                        OutlinedTextField(value = httpUrl, onValueChange = { httpUrl = it }, modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoadingHttp = true
                                val res = DevTools.executeHttpRequest(httpUrl, httpMethod)
                                isLoadingHttp = false
                                httpResult = if (res.isSuccess) {
                                    val r = res.getOrNull()!!
                                    "HTTP ${r.statusCode} (${r.latencyMs}ms)\n\n${r.body.take(500)}"
                                } else {
                                    "Error: ${res.exceptionOrNull()?.message}"
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isLoadingHttp) "Sending..." else "Send Request")
                    }
                    if (httpResult.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(httpResult, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    }
                }
            }

            "Env" -> {
                val envVars = remember { DevTools.getEnvironmentVariables() }
                GlassCard(modifier = Modifier.weight(1f)) {
                    Text("Environment Variables (${envVars.size})", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(envVars.entries.toList()) { (k, v) ->
                            Column {
                                Text(k, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontFamily = FontFamily.Monospace)
                                Text(v, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                HorizontalDivider(modifier = Modifier.padding(top = 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
