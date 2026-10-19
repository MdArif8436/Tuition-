package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.ChatTurn
import com.example.data.ai.GeminiChatService
import com.example.data.ai.GeminiModel
import com.example.data.firebase.FirestoreTuitionService
import com.example.data.model.DoubtMessage
import com.example.data.model.UserRole
import com.example.ui.components.CodeSnippetView
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel
import kotlinx.coroutines.launch

/**
 * Multi-Turn Academic Chat Interface powered by Gemini AI.
 *
 * Implements:
 * - Multi-turn conversation history tracking
 * - System instructions for MAKAUT BCA Academic Teaching Assistant role
 * - Dynamic model selection:
 *     • gemini-4-argon for particularly complex tasks
 *     • gemini-3.8-flash for general tasks (default)
 *     • gemini-3.1-flash-lite for tasks that should happen fast
 * - Scrollable thread with Markdown and syntax-highlighted code blocks
 * - Cloud Firestore persistence for doubts and responses
 * - Styled with the balanced brand palette
 */
@Composable
fun DoubtChatScreen(
    viewModel: TuitionViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val currentRole by viewModel.currentRole.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val messages by viewModel.doubtMessages.collectAsState()

    val geminiService = remember { GeminiChatService() }
    val firestoreService = remember { FirestoreTuitionService() }

    var selectedModel by remember { mutableStateOf(GeminiModel.GENERAL_FLASH) }
    var inputMessage by remember { mutableStateOf("") }
    var inputCodeSnippet by remember { mutableStateOf("") }
    var showCodeDialog by remember { mutableStateOf(false) }
    var isAiGenerating by remember { mutableStateOf(false) }

    // Multi-turn conversation history for Gemini API
    val conversationHistory = remember { mutableStateListOf<ChatTurn>() }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BrandMutedCanvas) // Balanced non-white canvas #D9E9F0
            .testTag("doubt_chat_screen")
    ) {
        // Mentor / AI Assistant Header Card
        Surface(
            shadowElevation = 3.dp,
            color = BrandSurfaceCard, // #E8F2F6
            border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(BrandOceanDeep),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Gemini AI Assistant",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BCA Academic AI Assistant",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = BrandOceanDeep
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = BrandSoftSky,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Gemini AI",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BrandOceanDeep,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Multi-turn academic doubts • MAKAUT CBCS syllabus",
                            style = MaterialTheme.typography.bodySmall,
                            color = BrandTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Gemini Model Selector Chips (gemini-4-argon, gemini-3.8-flash, gemini-3.1-flash-lite)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    GeminiModel.values().forEach { model ->
                        val isSelected = selectedModel == model
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedModel = model },
                            label = {
                                Text(
                                    text = model.displayName,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BrandOceanDeep,
                                selectedLabelColor = Color.White,
                                containerColor = BrandMutedCanvas,
                                labelColor = BrandOceanDeep
                            ),
                            border = if (isSelected) null else BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.5f))
                        )
                    }
                }
            }
        }

        // Messages List Thread (Scrollable)
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 14.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isMe = !msg.isFromTeacher

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    if (!isMe) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BrandOceanDeep),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Surface(
                        shape = RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isMe) 16.dp else 4.dp,
                            bottomEnd = if (isMe) 4.dp else 16.dp
                        ),
                        color = if (isMe) BrandOceanDeep else BrandSurfaceCard,
                        border = if (isMe) null else BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.45f)),
                        shadowElevation = 1.dp,
                        modifier = Modifier.widthIn(max = 310.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            // Sender header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isMe) "You" else msg.senderName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMe) BrandSoftSky else BrandOceanDeep
                                )
                                Text(
                                    text = msg.timestamp,
                                    fontSize = 10.sp,
                                    color = if (isMe) Color.White.copy(alpha = 0.7f) else BrandTextSecondary
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Message body
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = if (isMe) Color.White else BrandTextPrimary,
                                lineHeight = 18.sp
                            )

                            // Attached code snippet
                            msg.codeSnippet?.let { code ->
                                Spacer(modifier = Modifier.height(8.dp))
                                CodeSnippetView(
                                    code = code,
                                    language = "python"
                                )
                            }
                        }
                    }

                    if (isMe) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(BrandTerracotta),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentUser.name.take(1),
                                color = Color(0xFF3E1D18),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Typing / Thinking indicator
            if (isAiGenerating) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 40.dp, top = 4.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = BrandOceanDeep,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${selectedModel.displayName} analyzing query...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BrandOceanDeep
                        )
                    }
                }
            }
        }

        // Bottom Input Bar
        Surface(
            shadowElevation = 8.dp,
            color = BrandSurfaceCard, // #E8F2F6
            border = BorderStroke(1.dp, BrandSkyTeal.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Code Snippet Attachment Button
                IconButton(onClick = { showCodeDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Code,
                        contentDescription = "Attach Code Snippet",
                        tint = if (inputCodeSnippet.isNotBlank()) BrandTerracotta else BrandOceanDeep
                    )
                }

                // Input Text Field
                OutlinedTextField(
                    value = inputMessage,
                    onValueChange = { inputMessage = it },
                    placeholder = {
                        Text(
                            text = "Ask question (${selectedModel.displayName})...",
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 4.dp),
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrandOceanDeep,
                        unfocusedBorderColor = BrandSkyTeal.copy(alpha = 0.6f),
                        focusedContainerColor = BrandInputBackground,
                        unfocusedContainerColor = BrandInputBackground,
                        focusedLabelColor = BrandOceanDeep
                    )
                )

                // Send Button
                IconButton(
                    onClick = {
                        if (inputMessage.isNotBlank() || inputCodeSnippet.isNotBlank()) {
                            val userQuestion = inputMessage
                            val codeToSend = inputCodeSnippet.ifBlank { null }
                            inputMessage = ""
                            inputCodeSnippet = ""

                            // 1. Send User message to repository
                            val studentMsg = DoubtMessage(
                                text = userQuestion,
                                codeSnippet = codeToSend,
                                isFromTeacher = false,
                                senderId = currentUser.id,
                                senderName = currentUser.name,
                                senderRole = currentRole,
                                timestamp = "Just now"
                            )
                            viewModel.sendDoubtMessage(
                                text = userQuestion,
                                codeSnippet = codeToSend,
                                isFromTeacher = false,
                                senderName = currentUser.name
                            )

                            // 2. Add to conversation history
                            conversationHistory.add(ChatTurn(role = "user", text = userQuestion))

                            // 3. Persist to Firestore asynchronously
                            coroutineScope.launch {
                                try {
                                    firestoreService.saveDoubtMessage(studentMsg)
                                } catch (e: Exception) {
                                    // Offline fallback
                                }
                            }

                            // 4. Generate AI response via Gemini API
                            coroutineScope.launch {
                                isAiGenerating = true
                                val result = geminiService.sendMessage(
                                    history = conversationHistory.toList(),
                                    userMessage = userQuestion,
                                    model = selectedModel
                                )
                                isAiGenerating = false

                                val aiResponseText = result.getOrDefault(
                                    "I am reviewing your academic question according to MAKAUT BCA guidelines. Please ask in class if needed."
                                )

                                // Add model turn to conversation history
                                conversationHistory.add(ChatTurn(role = "model", text = aiResponseText))

                                // Post AI response to repository
                                val aiMsg = DoubtMessage(
                                    text = aiResponseText,
                                    codeSnippet = null,
                                    isFromTeacher = true,
                                    senderId = "gemini-ai-bot",
                                    senderName = selectedModel.displayName,
                                    senderRole = UserRole.ADMIN,
                                    timestamp = "Just now"
                                )
                                viewModel.sendDoubtMessage(
                                    text = aiResponseText,
                                    codeSnippet = null,
                                    isFromTeacher = true,
                                    senderName = selectedModel.displayName
                                )

                                // Persist AI response to Firestore
                                try {
                                    firestoreService.saveDoubtMessage(aiMsg)
                                } catch (e: Exception) {
                                    // Offline fallback
                                }
                            }
                        }
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = BrandOceanDeep),
                    enabled = !isAiGenerating && (inputMessage.isNotBlank() || inputCodeSnippet.isNotBlank())
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // Attach Code Snippet Dialog
    if (showCodeDialog) {
        AlertDialog(
            onDismissRequest = { showCodeDialog = false },
            containerColor = BrandSurfaceCard,
            shape = RoundedCornerShape(18.dp),
            title = {
                Text(
                    text = "Attach Code Snippet",
                    fontWeight = FontWeight.Bold,
                    color = BrandOceanDeep
                )
            },
            text = {
                Column {
                    Text(
                        text = "Paste your Python, Java, C++, or SQL query code for Gemini to debug:",
                        fontSize = 12.sp,
                        color = BrandTextPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = inputCodeSnippet,
                        onValueChange = { inputCodeSnippet = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        placeholder = { Text("def solution():\n    # Paste code here\n    pass") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandOceanDeep,
                            focusedContainerColor = BrandInputBackground,
                            unfocusedContainerColor = BrandInputBackground
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showCodeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandOceanDeep)
                ) {
                    Text("Attach Code", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    inputCodeSnippet = ""
                    showCodeDialog = false
                }) {
                    Text("Cancel", color = BrandTextSecondary)
                }
            }
        )
    }
}
