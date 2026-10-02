package com.example

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.NotificationHelper
import com.example.ui.components.FilterBar
import com.example.ui.components.LeaderboardView
import com.example.ui.components.NotificationDrawer
import com.example.ui.components.OfflinePaperViewer
import com.example.ui.components.PaperCard
import com.example.ui.components.RatingDialog
import com.example.ui.components.StudentProfileView
import com.example.ui.components.UploadPaperDialog
import com.example.ui.components.WebPortalView
import com.example.ui.theme.SRUExamVaultTheme
import com.example.ui.viewmodel.ExamVaultViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationHelper.createNotificationChannel(this)

        setContent {
            val viewModel: ExamVaultViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()

            // Request Notification Permission on Android 13+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val permissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    // Notification permission handled
                }
                LaunchedEffect(Unit) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            val systemInDark = isSystemInDarkTheme()
            val isDarkTheme = uiState.isDarkMode ?: systemInDark

            SRUExamVaultTheme(darkTheme = isDarkTheme) {
                ExamVaultApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamVaultApp(viewModel: ExamVaultViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val filteredPapers by viewModel.filteredPapers.collectAsStateWithLifecycle()
    val downloadedPapers by viewModel.downloadedPapers.collectAsStateWithLifecycle()
    val activeStudent by viewModel.activeStudent.collectAsStateWithLifecycle()
    val leaderboard by viewModel.leaderboard.collectAsStateWithLifecycle()
    val notifications by viewModel.notifications.collectAsStateWithLifecycle()
    val unreadNotifsCount by viewModel.unreadNotificationsCount.collectAsStateWithLifecycle()

    var isWebPortalMode by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissUserMessage()
        }
    }

    // Handle System Back Button
    BackHandler(enabled = uiState.selectedPaper != null || (!isWebPortalMode && uiState.activeTab != 0)) {
        if (uiState.selectedPaper != null) {
            viewModel.selectPaper(null)
        } else if (!isWebPortalMode && uiState.activeTab != 0) {
            viewModel.setActiveTab(0)
        }
    }

    // If viewing a paper in the offline document reader
    if (uiState.selectedPaper != null) {
        OfflinePaperViewer(
            paper = uiState.selectedPaper!!,
            ratings = uiState.currentRatings,
            onBackClick = { viewModel.selectPaper(null) },
            onDownloadClick = {
                val paper = uiState.selectedPaper!!
                if (paper.isDownloaded) {
                    viewModel.removeDownloadedPaper(paper.id)
                } else {
                    viewModel.downloadPaper(paper.id)
                }
            },
            onBookmarkClick = {
                uiState.selectedPaper?.let { viewModel.toggleBookmark(it) }
            },
            onOpenRatingClick = { viewModel.openRatingDialog() }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.secondary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "SRU Logo",
                                    tint = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "SRU ExamVault",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Text(
                                    text = "SR University Exam Archives",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                            }
                        }
                    },
                    actions = {
                        // Web Portal vs Native App Switcher
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isWebPortalMode) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                    .clickable { isWebPortalMode = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("switch_to_web_mode")
                            ) {
                                Text(
                                    text = "Portal",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isWebPortalMode) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (!isWebPortalMode) MaterialTheme.colorScheme.secondary else Color.Transparent)
                                    .clickable { isWebPortalMode = false }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("switch_to_app_mode")
                            ) {
                                Text(
                                    text = "Native",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!isWebPortalMode) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // Quick Dark Mode Toggle
                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("top_bar_theme_toggle")
                        ) {
                            Icon(
                                imageVector = if (uiState.isDarkMode == true) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "Toggle theme",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Notification Bell with Badge
                        IconButton(
                            onClick = { viewModel.openNotificationDrawer() },
                            modifier = Modifier.testTag("top_bar_notif_bell")
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifsCount > 0) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.error,
                                            contentColor = MaterialTheme.colorScheme.onError
                                        ) {
                                            Text(text = "$unreadNotifsCount")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }

                        // Profile Avatar
                        IconButton(
                            onClick = { viewModel.setActiveTab(3) },
                            modifier = Modifier.testTag("top_bar_profile_btn")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = activeStudent?.fullName?.take(1) ?: "U",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                )
            },
            bottomBar = {
                if (!isWebPortalMode) {
                    NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationBarItem(
                        selected = uiState.activeTab == 0,
                        onClick = { viewModel.setActiveTab(0) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == 0) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                                contentDescription = "Browse Papers"
                            )
                        },
                        label = { Text("Papers", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_papers")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 1,
                        onClick = { viewModel.setActiveTab(1) },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (downloadedPapers.isNotEmpty()) {
                                        Badge(
                                            containerColor = Color(0xFF059669),
                                            contentColor = Color.White
                                        ) {
                                            Text("${downloadedPapers.size}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (uiState.activeTab == 1) Icons.Default.CloudDownload else Icons.Outlined.CloudDownload,
                                    contentDescription = "Offline Vault"
                                )
                            }
                        },
                        label = { Text("Offline", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_offline")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 2,
                        onClick = { viewModel.setActiveTab(2) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == 2) Icons.Default.EmojiEvents else Icons.Outlined.EmojiEvents,
                                contentDescription = "Leaderboard"
                            )
                        },
                        label = { Text("Leaders", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_leaderboard")
                    )

                    NavigationBarItem(
                        selected = uiState.activeTab == 3,
                        onClick = { viewModel.setActiveTab(3) },
                        icon = {
                            Icon(
                                imageVector = if (uiState.activeTab == 3) Icons.Default.Person else Icons.Outlined.Person,
                                contentDescription = "Profile"
                            )
                        },
                        label = { Text("Profile", fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("nav_tab_profile")
                    )
                }
                }
            },
            floatingActionButton = {
                if (!isWebPortalMode && (uiState.activeTab == 0 || uiState.activeTab == 1)) {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openUploadDialog() },
                        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                        text = { Text("Upload Paper (+50 Pts)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_upload_paper")
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isWebPortalMode) {
                    WebPortalView(modifier = Modifier.fillMaxSize())
                } else {
                    when (uiState.activeTab) {
                    0 -> {
                        // Main Browse Hub
                        Column(modifier = Modifier.fillMaxSize()) {
                            FilterBar(
                                searchQuery = uiState.searchQuery,
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                selectedBranch = uiState.selectedBranch,
                                onBranchSelect = { viewModel.setSelectedBranch(it) },
                                selectedExamType = uiState.selectedExamType,
                                onExamTypeSelect = { viewModel.setSelectedExamType(it) },
                                selectedSemester = uiState.selectedSemester,
                                onSemesterSelect = { viewModel.setSelectedSemester(it) },
                                onlyDownloaded = uiState.onlyDownloadedFilter,
                                onOnlyDownloadedToggle = { viewModel.setOnlyDownloadedFilter(it) }
                            )

                            if (filteredPapers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.MenuBook,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(54.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No question papers found",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Try adjusting your branch, exam type, or search term.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    item {
                                        Text(
                                            text = "Showing ${filteredPapers.size} Question Papers",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    items(filteredPapers, key = { it.id }) { paper ->
                                        PaperCard(
                                            paper = paper,
                                            onClick = { viewModel.selectPaper(paper) },
                                            onDownloadClick = {
                                                if (paper.isDownloaded) {
                                                    viewModel.removeDownloadedPaper(paper.id)
                                                } else {
                                                    viewModel.downloadPaper(paper.id)
                                                }
                                            },
                                            onBookmarkClick = { viewModel.toggleBookmark(paper) }
                                        )
                                    }

                                    item {
                                        Spacer(modifier = Modifier.height(72.dp))
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // Offline Vault Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .testTag("offline_vault_screen")
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFF059669).copy(alpha = 0.12f)
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = null,
                                        tint = Color(0xFF059669),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Offline Study Vault",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF059669)
                                        )
                                        Text(
                                            text = "These papers are saved locally on your phone. Read anytime without internet!",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            if (downloadedPapers.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDownload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            modifier = Modifier.size(54.dp)
                                        )
                                        Spacer(modifier = Modifier.height(12.dp))
                                        Text(
                                            text = "No offline papers saved",
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Tap 'Save Offline' on any paper to study without network access.",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                }
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(downloadedPapers, key = { it.id }) { paper ->
                                        PaperCard(
                                            paper = paper,
                                            onClick = { viewModel.selectPaper(paper) },
                                            onDownloadClick = { viewModel.removeDownloadedPaper(paper.id) },
                                            onBookmarkClick = { viewModel.toggleBookmark(paper) }
                                        )
                                    }
                                    item {
                                        Spacer(modifier = Modifier.height(72.dp))
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Leaderboard
                        LeaderboardView(
                            leaderboard = leaderboard,
                            currentStudent = activeStudent
                        )
                    }

                    3 -> {
                        // Student Profile & Settings
                        StudentProfileView(
                            student = activeStudent,
                            isDarkMode = uiState.isDarkMode,
                            onToggleTheme = { viewModel.toggleDarkMode() },
                            onTestNotification = {
                                viewModel.triggerDemoNotification()
                                viewModel.showToast("Test push notification dispatched!")
                            },
                            onUpdateProfile = { r, n, e, b, s ->
                                viewModel.updateStudentProfile(r, n, e, b, s)
                            }
                        )
                    }
                }
                }
            }
        }
    }

    // Upload Dialog
    if (uiState.showUploadDialog) {
        UploadPaperDialog(
            onDismiss = { viewModel.closeUploadDialog() },
            onSubmit = { code, name, branch, sem, type, year, monthYear, reg, marks, dur, partA, partB, hints ->
                viewModel.submitPaperUpload(
                    code, name, branch, sem, type, year, monthYear, reg, marks, dur, partA, partB, hints
                )
            }
        )
    }

    // Rating Dialog
    if (uiState.showRatingDialog && uiState.selectedPaper != null) {
        RatingDialog(
            paperTitle = "${uiState.selectedPaper!!.subjectCode} - ${uiState.selectedPaper!!.subjectName}",
            onDismiss = { viewModel.closeRatingDialog() },
            onSubmit = { rating, tag, comment ->
                viewModel.submitRating(uiState.selectedPaper!!.id, rating, tag, comment)
            }
        )
    }

    // Notification Drawer
    if (uiState.showNotificationDrawer) {
        NotificationDrawer(
            notifications = notifications,
            onDismiss = { viewModel.closeNotificationDrawer() },
            onMarkAllRead = { viewModel.markAllNotificationsAsRead() },
            onSelectNotification = { notif ->
                viewModel.markNotificationAsRead(notif.id)
                viewModel.closeNotificationDrawer()
                // If notification has paperId, open that paper
                if (notif.paperId != null) {
                    val paper = filteredPapers.find { it.id == notif.paperId }
                    if (paper != null) {
                        viewModel.selectPaper(paper)
                    }
                }
            }
        )
    }
}
