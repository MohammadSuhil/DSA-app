package com.tracker.DSA.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.DSA.ui.theme.BackgroundColor
import com.tracker.DSA.ui.theme.PrimaryColor
import com.tracker.DSA.ui.theme.SurfaceColor
import com.tracker.DSA.ui.theme.TextMain
import com.tracker.DSA.ui.theme.TextMuted

@Composable
fun LoginDialog(
    onDismiss: () -> Unit,
    onLoginClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Sign In Required",
                color = TextMain,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        },
        text = {
            Text(
                "To join communities, chat, or sync your progress to the leaderboard, you need to sign in with Google.",
                color = TextMuted,
                fontSize = 16.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
            ) {
                Text("Sign In with Google", color = BackgroundColor)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextMuted)
            }
        },
        containerColor = SurfaceColor,
        shape = RoundedCornerShape(16.dp)
    )
}
