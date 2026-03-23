package com.tracker.DSA.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tracker.DSA.data.local.Problem
import com.tracker.DSA.data.local.TopicWithProblems
import com.tracker.DSA.ui.theme.*

@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel(), onOpenUrl: (String, String) -> Unit = { _, _ -> }) {
    val topics by viewModel.topicsWithProblems.collectAsState()
    val completedCount by viewModel.completedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")

    // Progress bar color lerped from red → green based on progress
    val progressBarColor = androidx.compose.ui.graphics.lerp(ProgressRed, ProgressGreen, progress)

    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor)) {
        Spacer(modifier = Modifier.height(8.dp)) // Small top padding instead of progress section

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
        ) {
            items(topics) { topicWithProblems ->
                TopicCard(topicWithProblems, onProblemToggled = { problem, isChecked ->
                    viewModel.toggleProblemStatus(problem.id, isChecked)
                }, onOpenUrl = onOpenUrl)
            }
        }
    }
}

@Composable
fun TopicCard(topicWithProblems: TopicWithProblems, onProblemToggled: (Problem, Boolean) -> Unit, onOpenUrl: (String, String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = topicWithProblems.topic.name,
                color = TextMain,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            HorizontalDivider(color = Color(0xFFF2F2F7), thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            topicWithProblems.problems.forEach { problem ->
                ProblemItem(problem, onProblemToggled, onOpenUrl)
            }
        }
    }
}

@Composable
fun ProblemItem(problem: Problem, onProblemToggled: (Problem, Boolean) -> Unit, onOpenUrl: (String, String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF9F9FB))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = problem.isCompleted,
            onCheckedChange = { isChecked ->
                onProblemToggled(problem, isChecked)
            },
            colors = CheckboxDefaults.colors(
                checkedColor = PrimaryColor,
                uncheckedColor = Color(0xFFC7C7CC),
                checkmarkColor = Color.White
            ),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = problem.name,
                color = if (problem.isCompleted) TextMuted else TextMain,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                textDecoration = if (problem.isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                modifier = Modifier.clickable {
                    onOpenUrl(problem.url, problem.name)
                }
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Difficulty badge
        val badgeColor = if (problem.difficulty == "easy") EasyColor else MediumColor
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor.copy(alpha = 0.12f))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = problem.difficulty.replaceFirstChar { it.uppercase() },
                color = badgeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
