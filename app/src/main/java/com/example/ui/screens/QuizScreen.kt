package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.data.model.Quiz
import com.example.data.model.QuizAttempt
import com.example.ui.components.AntiCheatDialog
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel
import java.util.Locale

import androidx.activity.compose.BackHandler

@Composable
fun QuizScreen(
    viewModel: TuitionViewModel
) {
    val quizzes by viewModel.quizzes.collectAsState()
    val attempts by viewModel.quizAttempts.collectAsState()
    val quizState by viewModel.quizState.collectAsState()
    var showExitQuizDialog by remember { mutableStateOf(false) }

    // Intercept Back button during active quiz
    if (quizState.quiz != null) {
        BackHandler {
            if (quizState.isFinished) {
                viewModel.exitQuizView()
            } else {
                showExitQuizDialog = true
            }
        }
    }

    if (showExitQuizDialog) {
        AlertDialog(
            onDismissRequest = { showExitQuizDialog = false },
            title = { Text("Exit Assessment?") },
            text = { Text("Are you sure you want to exit? Your answers entered so far will be submitted automatically.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitQuizDialog = false
                        viewModel.finishQuiz()
                    }
                ) {
                    Text("Submit & Exit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitQuizDialog = false }) {
                    Text("Continue Quiz")
                }
            }
        )
    }

    // Observe Android lifecycle to track blur / app minimization during active quiz
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, quizState.quiz, quizState.isFinished) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                if (quizState.quiz != null && !quizState.isFinished) {
                    viewModel.recordAntiCheatInfraction()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Active anti-cheat warning overlay
    if (quizState.showAntiCheatWarning) {
        AntiCheatDialog(
            infractionCount = quizState.infractionCount,
            maxAllowed = quizState.maxInfractionsAllowed,
            onResumeAssessment = { viewModel.dismissAntiCheatWarning() }
        )
    }

    if (quizState.quiz != null) {
        ActiveQuizPlayer(
            viewModel = viewModel,
            quiz = quizState.quiz!!,
            quizState = quizState
        )
    } else {
        // Quiz List & Attempt History
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("quiz_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "MAKAUT Online Assessment Engine",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Anti-cheat heuristics active • Automatic objective scoring",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(AcademicTertiary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = AcademicTertiary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Integrity Monitor: Window blurs and tab switches beyond 3 infractions will auto-lock your test.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Available Assessments",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(quizzes) { quiz ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = AcademicPrimary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = quiz.subject,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AcademicPrimary,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${quiz.timeLimitMinutes} mins",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = quiz.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "${quiz.questions.size} Multiple Choice Questions • ${quiz.maxMarks} Marks Max",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.startQuiz(quiz) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("start_quiz_${quiz.id}"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Start Proctored Assessment")
                        }
                    }
                }
            }

            if (attempts.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Recent Test Scores & Attempt History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(attempts) { att ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(att.quizTitle, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "Completed: ${att.completedAt} • Infractions: ${att.infractionCount}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (att.infractionCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (att.wasForcedSubmitted) {
                                    Text(
                                        text = "⚠️ Auto-submitted due to anti-cheat violation",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.error,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${att.score} / ${att.maxScore}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (att.percentage >= 60.0) StatusPaid else StatusPending
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", att.percentage)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ActiveQuizPlayer(
    viewModel: TuitionViewModel,
    quiz: Quiz,
    quizState: com.example.ui.viewmodel.ActiveQuizState
) {
    if (quizState.isFinished) {
        // Quiz Results & Review
        val attempt = quizState.latestAttemptResult
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("quiz_results_screen"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(3.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (quizState.latestAttemptResult?.wasForcedSubmitted == true)
                                "Assessment Locked (Infraction Limit Reached)"
                            else
                                "Assessment Completed!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (quizState.latestAttemptResult?.wasForcedSubmitted == true) MaterialTheme.colorScheme.error else StatusPaid
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "${attempt?.score ?: 0} / ${quiz.maxMarks}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Text(
                            text = "Score: ${String.format(Locale.US, "%.1f", attempt?.percentage ?: 0.0)}%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Infractions logged during test: ${attempt?.infractionCount ?: 0}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if ((attempt?.infractionCount ?: 0) > 0) MaterialTheme.colorScheme.error else StatusPaid
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.exitQuizView() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Return to Assessments")
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Question Breakdown & Solutions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(quiz.questions.indices.toList()) { index ->
                val q = quiz.questions[index]
                val selectedOpt = quizState.selectedAnswers[index]
                val isCorrect = selectedOpt == q.correctOptionIndex

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Q${index + 1}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (isCorrect) StatusPaid.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = if (isCorrect) "Correct ✓" else "Incorrect ✗",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isCorrect) StatusPaid else MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = q.text, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Correct Answer: ${q.options[q.correctOptionIndex]}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusPaid,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Explanation: ${q.explanation}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    } else {
        // Active Question View
        val qIndex = quizState.currentQuestionIndex
        val currentQuestion = quiz.questions[qIndex]
        val selectedOption = quizState.selectedAnswers[qIndex]

        val mins = quizState.secondsRemaining / 60
        val secs = quizState.secondsRemaining % 60
        val timeFormatted = String.format(Locale.US, "%02d:%02d", mins, secs)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("active_quiz_player"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Proctored Status Header
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = AcademicPrimary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = timeFormatted,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (quizState.secondsRemaining < 60) MaterialTheme.colorScheme.error else AcademicPrimary
                            )
                        }

                        // Anti-cheat Infractions count badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (quizState.infractionCount > 0) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else StatusPaid.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (quizState.infractionCount > 0) MaterialTheme.colorScheme.error else StatusPaid,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Infractions: ${quizState.infractionCount}/3",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quizState.infractionCount > 0) MaterialTheme.colorScheme.error else StatusPaid
                                )
                            }
                        }
                    }
                }
            }

            // Question Box
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Question ${qIndex + 1} of ${quiz.questions.size}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${quiz.maxMarks / quiz.questions.size} Marks",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = currentQuestion.text,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            lineHeight = 24.sp
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Options
                        currentQuestion.options.forEachIndexed { optIndex, optText ->
                            val isSelected = selectedOption == optIndex
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { viewModel.selectQuizAnswer(qIndex, optIndex) }
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) AcademicPrimary else MaterialTheme.colorScheme.outlineVariant,
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) AcademicPrimary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectQuizAnswer(qIndex, optIndex) },
                                        colors = RadioButtonDefaults.colors(selectedColor = AcademicPrimary)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = optText,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Navigation and Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = { viewModel.prevQuizQuestion() },
                        enabled = qIndex > 0,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Previous")
                    }

                    if (qIndex < quiz.questions.size - 1) {
                        Button(
                            onClick = { viewModel.nextQuizQuestion() },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Next Question")
                        }
                    } else {
                        Button(
                            onClick = { viewModel.finishQuiz(wasForced = false) },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StatusPaid)
                        ) {
                            Text("Submit Exam")
                        }
                    }
                }
            }

            // Prototyping action: test anti-cheat simulation button
            item {
                OutlinedButton(
                    onClick = { viewModel.recordAntiCheatInfraction() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_anti_cheat_button"),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulate Tab/App Switch (Test Anti-Cheat Warning)")
                }
            }
        }
    }
}
