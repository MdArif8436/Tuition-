package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ForumComment
import com.example.data.model.ForumPost
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.TuitionViewModel

@Composable
fun ForumScreen(
    viewModel: TuitionViewModel
) {
    val currentRole by viewModel.currentRole.collectAsState()
    val posts by viewModel.filteredForumPosts.collectAsState()
    val searchQuery by viewModel.forumSearchQuery.collectAsState()
    val selectedTag by viewModel.selectedForumTag.collectAsState()
    val commentsMap by viewModel.forumComments.collectAsState()

    var activeDiscussionPost by remember { mutableStateOf<ForumPost?>(null) }
    var showCreatePostDialog by remember { mutableStateOf(false) }

    val allTags = listOf("DBMS", "Normalization", "MAKAUT", "Exam-Notice", "Java", "DSA")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("forum_screen"),
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "BCA Peer Learning & Discussion Forum",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Search study topics, ask doubts & verify accepted solutions",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        FilledTonalButton(
                            onClick = { showCreatePostDialog = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.AddComment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Ask")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setForumSearch(it) },
                        placeholder = { Text("Search topics, keywords (e.g. BCNF, joins)...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setForumSearch("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = null)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tag filters
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(
                            selected = selectedTag == null,
                            onClick = { viewModel.selectForumTag(null) },
                            label = { Text("All") }
                        )
                        allTags.take(4).forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { viewModel.selectForumTag(if (selectedTag == tag) null else tag) },
                                label = { Text("#$tag") }
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Community Discussions (${posts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(posts) { post ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { activeDiscussionPost = post },
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (post.authorRole == UserRole.ADMIN) AcademicTertiary.copy(alpha = 0.2f)
                                        else AcademicPrimary.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = post.authorName.take(1),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (post.authorRole == UserRole.ADMIN) AcademicTertiary else AcademicPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = post.authorName,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = post.createdAt,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (post.isSolved) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = StatusPaid.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = StatusPaid, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("Solved", style = MaterialTheme.typography.labelSmall, color = StatusPaid, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tags row
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        post.tags.forEach { t ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    text = "#$t",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.toggleForumUpvote(post.id) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = if (post.isUpvotedByMe) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt,
                                    contentDescription = "Upvote",
                                    tint = if (post.isUpvotedByMe) AcademicPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "${post.upvotes} Upvotes",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${post.commentsCount} Answers",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Thread Discussion Dialog
    if (activeDiscussionPost != null) {
        val post = activeDiscussionPost!!
        val comments = commentsMap[post.id] ?: emptyList()
        var newCommentText by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { activeDiscussionPost = null },
            title = {
                Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(post.content, style = MaterialTheme.typography.bodySmall)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Discussion & Verified Answers (${comments.size})", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)

                    comments.forEach { c ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (c.isAcceptedAnswer) StatusPaid.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(c.authorName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    if (c.isAcceptedAnswer) {
                                        Text("✓ Accepted Solution", color = StatusPaid, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(c.content, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { Text("Contribute an answer...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newCommentText.isNotBlank()) {
                        viewModel.addForumComment(post.id, newCommentText)
                        newCommentText = ""
                    }
                }) {
                    Text("Post Answer")
                }
            },
            dismissButton = {
                TextButton(onClick = { activeDiscussionPost = null }) {
                    Text("Close")
                }
            }
        )
    }

    // New Post Dialog
    if (showCreatePostDialog) {
        var pTitle by remember { mutableStateOf("") }
        var pContent by remember { mutableStateOf("") }
        var pTags by remember { mutableStateOf("DBMS, Normalization") }

        AlertDialog(
            onDismissRequest = { showCreatePostDialog = false },
            title = { Text("Ask a Doubt / Start Discussion") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = pTitle,
                        onValueChange = { pTitle = it },
                        label = { Text("Question Title") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = pContent,
                        onValueChange = { pContent = it },
                        label = { Text("Describe the problem or query...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                    OutlinedTextField(
                        value = pTags,
                        onValueChange = { pTags = it },
                        label = { Text("Tags (comma separated e.g. SQL, MAKAUT)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (pTitle.isNotBlank()) {
                        val tagsList = pTags.split(",").map { it.trim() }.filter { it.isNotBlank() }
                        viewModel.addForumPost(pTitle, pContent, tagsList)
                        showCreatePostDialog = false
                    }
                }) {
                    Text("Publish Question")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreatePostDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
