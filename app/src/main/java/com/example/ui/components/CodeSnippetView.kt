package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CodeSnippetView(
    code: String,
    language: String = "c",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isCopied by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    val langLabel = when (language.lowercase()) {
        "c" -> "C Language"
        "sql" -> "PostgreSQL / SQL"
        "java" -> "Java OOP"
        "python" -> "Python 3"
        else -> language.uppercase()
    }

    val badgeColor = when (language.lowercase()) {
        "c" -> Color(0xFF3B82F6)
        "sql" -> Color(0xFFF59E0B)
        "java" -> Color(0xFFEF4444)
        "python" -> Color(0xFF10B981)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("code_snippet_card"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CodeBackground)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = langLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Source Code", code)
                        clipboard.setPrimaryClip(clip)
                        isCopied = true
                        coroutineScope.launch {
                            delay(2000)
                            isCopied = false
                        }
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isCopied) Icons.Default.Done else Icons.Default.ContentCopy,
                        contentDescription = "Copy code",
                        tint = if (isCopied) StatusPaid else Color(0xFF94A3B8),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Code content with syntax styling
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(14.dp)
            ) {
                Text(
                    text = highlightCode(code, language),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.5.sp,
                    lineHeight = 19.sp,
                    color = Color(0xFFE2E8F0)
                )
            }
        }
    }
}

private fun highlightCode(code: String, language: String) = buildAnnotatedString {
    val keywords = when (language.lowercase()) {
        "sql" -> listOf("SELECT", "FROM", "WHERE", "JOIN", "ON", "GROUP BY", "HAVING", "ORDER BY", "DESC", "ASC", "AVG", "COUNT", "SUM", "AND", "OR", "IN", "NOT")
        "java" -> listOf("public", "private", "protected", "class", "void", "synchronized", "static", "final", "double", "int", "boolean", "return", "new", "this", "throw", "try", "catch")
        "python" -> listOf("def", "class", "return", "import", "from", "as", "if", "elif", "else", "for", "while", "in", "self", "True", "False", "None")
        else -> listOf("include", "struct", "int", "void", "return", "if", "else", "while", "for", "sizeof", "malloc", "free", "NULL")
    }

    val lines = code.split("\n")
    lines.forEachIndexed { index, line ->
        // Line number
        withStyle(SpanStyle(color = Color(0xFF475569), fontWeight = FontWeight.Normal)) {
            val numStr = (index + 1).toString().padStart(2, ' ') + "  "
            append(numStr)
        }

        if (line.trimStart().startsWith("//") || line.trimStart().startsWith("--") || line.trimStart().startsWith("#")) {
            withStyle(SpanStyle(color = CodeComment)) {
                append(line)
            }
        } else {
            // Tokenize words
            val parts = line.split(Regex("(?<=\\b)|(?=\\b)"))
            for (token in parts) {
                when {
                    keywords.contains(token) -> {
                        withStyle(SpanStyle(color = CodeKeyword, fontWeight = FontWeight.Bold)) {
                            append(token)
                        }
                    }
                    token.startsWith("\"") && token.endsWith("\"") -> {
                        withStyle(SpanStyle(color = CodeString)) {
                            append(token)
                        }
                    }
                    token.toIntOrNull() != null -> {
                        withStyle(SpanStyle(color = CodeNumber)) {
                            append(token)
                        }
                    }
                    else -> {
                        append(token)
                    }
                }
            }
        }
        if (index < lines.size - 1) append("\n")
    }
}
