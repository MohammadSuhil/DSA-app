package com.tracker.DSA

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.tracker.DSA.ui.auth.AuthViewModel
import com.tracker.DSA.ui.screens.ChatScreen
import com.tracker.DSA.ui.screens.CommunityScreen
import com.tracker.DSA.ui.screens.HomeScreen
import com.tracker.DSA.ui.screens.LeaderboardScreen
import com.tracker.DSA.ui.screens.WebViewScreen
import com.tracker.DSA.ui.theme.BackgroundColor
import com.tracker.DSA.ui.theme.DSATheme
import com.tracker.DSA.ui.theme.PrimaryColor
import com.tracker.DSA.ui.theme.TextMuted
import com.google.android.gms.auth.api.signin.GoogleSignIn
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DSATheme {
                DSATrackerApp()
            }
        }
    }
}

@Composable
fun DSATrackerApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val authViewModel: AuthViewModel = viewModel()
    val user by authViewModel.currentUser.collectAsState()
    val profile by authViewModel.userProfile.collectAsState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    LaunchedEffect(Unit) {
        if (FirebaseAuth.getInstance().currentUser == null) {
            android.util.Log.d("MainActivity", "No user on startup, attempting anonymous sign-in")
            authViewModel.signInAnonymously()
        } else {
            // Already logged in, ensure test data is seeded if needed
            authViewModel.seedTestData()
        }
    }
    
    var showAuthDialog by remember { mutableStateOf(false) }

    val homeViewModel: com.tracker.DSA.ui.screens.HomeViewModel = viewModel()
    
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("MainActivity", "Google Sign-In result received: ${result.resultCode}")
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken
            android.util.Log.d("MainActivity", "Google Sign-In success, idToken present: ${idToken != null}")
            
            if (idToken == null) {
                android.util.Log.e("MainActivity", "Google Sign-In failed: idToken is null")
                android.widget.Toast.makeText(context, "Login failed: No ID Token received", android.widget.Toast.LENGTH_LONG).show()
                return@rememberLauncherForActivityResult
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            coroutineScope.launch {
                try {
                    android.util.Log.d("MainActivity", "Starting Firebase Auth with Google credential")
                    val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
                    authResult.user?.let { firebaseUser ->
                        android.util.Log.d("MainActivity", "Firebase Auth success: ${firebaseUser.uid}")
                        android.widget.Toast.makeText(context, "Welcome ${firebaseUser.displayName ?: "User"}!", android.widget.Toast.LENGTH_SHORT).show()
                        authViewModel.syncUserToFirestore(firebaseUser)
                        homeViewModel.syncProgress()
                    }
                    showAuthDialog = false
                } catch (e: Exception) {
                    android.util.Log.e("MainActivity", "Firebase Auth failed", e)
                    android.widget.Toast.makeText(context, "Firebase Auth failed: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: ApiException) {
            val statusMessage = when(e.statusCode) {
                10 -> "Developer Error: Check SHA-1/Package Name in Firebase Console"
                12500 -> "Sign-in failed: Google Play Services issue"
                7 -> "Network error"
                else -> "Error code: ${e.statusCode}"
            }
            android.util.Log.e("MainActivity", "Google Sign-In failed: $statusMessage")
            android.widget.Toast.makeText(context, "Google Sign-In failed: $statusMessage", android.widget.Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Google Sign-In failed with unexpected error", e)
            android.widget.Toast.makeText(context, "Unexpected error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }

    // Hide nav bar on certain routes (webview, chat) and on scroll
    var isNavVisible by rememberSaveable { mutableStateOf(true) }

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (available.y < -10) {
                isNavVisible = false  // scrolling down → hide
            } else if (available.y > 10) {
                isNavVisible = true   // scrolling up → show
            }
            return Offset.Zero
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            user = user,
            onDismiss = { showAuthDialog = false },
            onLoginWithGoogle = {
                android.util.Log.d("MainActivity", "Clicked: Login with Google")
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                val client = GoogleSignIn.getClient(context, gso)
                googleSignInLauncher.launch(client.signInIntent)
            },
            onLoginGuest = {
                android.util.Log.d("MainActivity", "Clicked: Login as Guest")
                authViewModel.signInAnonymously()
                showAuthDialog = false
            },
            onLogout = {
                authViewModel.signOut()
                showAuthDialog = false
            },
            onSyncNow = {
                homeViewModel.syncProgress()
            }
        )
    }

    // Determine current route for showing/hiding Bottom Bar
    val isFullscreenRoute = currentRoute?.startsWith("webview") == true || currentRoute?.startsWith("chat") == true
    val showBottomBar = !isFullscreenRoute && isNavVisible

    Scaffold(
        topBar = {
            if (!isFullscreenRoute) {
                // Global Top Bar
                val completedCount by homeViewModel.completedCount.collectAsState()
                val totalCount by homeViewModel.totalCount.collectAsState()
                val progress = if (totalCount > 0) completedCount.toFloat() / totalCount.toFloat() else 0f
                val progressBarColor = androidx.compose.ui.graphics.lerp(com.tracker.DSA.ui.theme.ProgressRed, com.tracker.DSA.ui.theme.ProgressGreen, progress)

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp))
                        .background(Color.White)
                        .statusBarsPadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // A Logo (Bold Italic)
                        Text(
                            text = "A",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            fontStyle = FontStyle.Italic,
                            color = Color.Black,
                            modifier = Modifier.padding(end = 12.dp)
                        )

                        // Progress Text (Centered)
                        Text(
                            text = "${(progress * 100).toInt()}%  •  $completedCount / $totalCount solved",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = com.tracker.DSA.ui.theme.EasyColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        // Profile Icon
                        IconButton(onClick = { showAuthDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Profile",
                                modifier = Modifier.size(28.dp),
                                tint = Color.Black
                            )
                        }
                    }
                    
                    // Global Thin Gradient Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .background(Color(0xFFF2F2F7))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(progress)
                                .fillMaxHeight()
                                .background(progressBarColor)
                        )
                    }
                }
            }
        },
        containerColor = BackgroundColor
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .nestedScroll(nestedScrollConnection)
            ) {
                appRoutes(navController)
            }

            if (showBottomBar) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = androidx.compose.foundation.shape.CircleShape,
                        color = Color.White,
                        shadowElevation = 8.dp,
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .height(60.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val items = listOf(
                                Triple("home", Icons.Default.Home, "Home"),
                                Triple("leaderboard", Icons.AutoMirrored.Filled.List, "Leaderboard"),
                                Triple("community", Icons.Default.Person, "Community")
                            )

                            items.forEach { (route, icon, label) ->
                                val isSelected = currentRoute == route
                                IconButton(
                                    onClick = {
                                        if (currentRoute != route) {
                                            navController.navigate(route) {
                                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = label,
                                        tint = if (isSelected) com.tracker.DSA.ui.theme.PrimaryColor else com.tracker.DSA.ui.theme.TextMuted,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AuthDialog(
    user: com.google.firebase.auth.FirebaseUser?,
    onDismiss: () -> Unit,
    onLoginWithGoogle: () -> Unit,
    onLoginGuest: () -> Unit,
    onLogout: () -> Unit,
    onSyncNow: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (user == null) "Welcome to DSA Tracker" else "My Profile") },
        text = {
            if (user == null) {
                Text("Login with Google to sync your progress across devices and appear on the leaderboard.")
            } else {
                Column {
                    Text("Logged in as: ${user.displayName ?: "Guest"}")
                    Text("Email: ${user.email ?: "Anonymous"}", fontSize = 12.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onSyncNow,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = com.tracker.DSA.ui.theme.ProgressGreen)
                    ) {
                        Text("Sync Progress Now")
                    }
                }
            }
        },
        confirmButton = {
            if (user == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Button(
                        onClick = onLoginWithGoogle,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryColor)
                    ) {
                        Text("Login with Google")
                    }
                    Button(
                        onClick = onLoginGuest,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
                    ) {
                        Text("Login as Guest", color = Color.Black)
                    }
                }
            } else {
                TextButton(onClick = onLogout) {
                    Text("Logout", color = Color.Red)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        containerColor = Color.White
    )
}

fun androidx.navigation.NavGraphBuilder.appRoutes(navController: NavHostController) {
    composable("home") {
        HomeScreen(onOpenUrl = { url, title ->
            val encodedUrl = java.net.URLEncoder.encode(url, "UTF-8")
            val encodedTitle = java.net.URLEncoder.encode(title, "UTF-8")
            navController.navigate("webview/$encodedUrl/$encodedTitle")
        })
    }
    composable("leaderboard") { LeaderboardScreen() }
    composable("community") {
        CommunityScreen(onNavigateToChat = { id, name ->
            navController.navigate("chat/$id/$name")
        })
    }
    composable(
        route = "chat/{communityId}/{communityName}",
        arguments = listOf(
            navArgument("communityId") { type = NavType.StringType },
            navArgument("communityName") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val communityId = backStackEntry.arguments?.getString("communityId") ?: ""
        val communityName = backStackEntry.arguments?.getString("communityName") ?: ""
        ChatScreen(
            communityId = communityId,
            communityName = communityName,
            onBack = { navController.popBackStack() }
        )
    }
    composable(
        route = "webview/{url}/{title}",
        arguments = listOf(
            navArgument("url") { type = NavType.StringType },
            navArgument("title") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val url = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("url") ?: "", "UTF-8")
        val title = java.net.URLDecoder.decode(backStackEntry.arguments?.getString("title") ?: "", "UTF-8")
        WebViewScreen(
            url = url,
            title = title,
            onBack = { navController.popBackStack() }
        )
    }
}