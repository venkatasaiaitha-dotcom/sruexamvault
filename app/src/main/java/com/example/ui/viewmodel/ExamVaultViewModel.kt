package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.ExamBranch
import com.example.data.model.ExamType
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PaperEntity
import com.example.data.model.PaperRating
import com.example.data.model.StudentProfile
import com.example.data.repository.ExamVaultRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExamVaultUiState(
    val selectedBranch: ExamBranch = ExamBranch.ALL,
    val selectedExamType: ExamType = ExamType.ALL,
    val selectedSemester: Int? = null,
    val searchQuery: String = "",
    val onlyDownloadedFilter: Boolean = false,
    val isDarkMode: Boolean? = null, // null = system, true = dark, false = light
    val selectedPaper: PaperEntity? = null,
    val currentRatings: List<PaperRating> = emptyList(),
    val showUploadDialog: Boolean = false,
    val showRatingDialog: Boolean = false,
    val showProfileDialog: Boolean = false,
    val showNotificationDrawer: Boolean = false,
    val activeTab: Int = 0, // 0 = Papers Hub, 1 = Offline Vault, 2 = Leaderboard, 3 = Profile
    val userMessage: String? = null
)

class ExamVaultViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ExamVaultRepository(application)

    private val _uiState = MutableStateFlow(ExamVaultUiState())
    val uiState: StateFlow<ExamVaultUiState> = _uiState.asStateFlow()

    val allPapers: StateFlow<List<PaperEntity>> = repository.getAllPapers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val downloadedPapers: StateFlow<List<PaperEntity>> = repository.getDownloadedPapers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeStudent: StateFlow<StudentProfile?> = repository.getActiveProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val leaderboard: StateFlow<List<LeaderboardEntry>> = repository.getLeaderboard()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<AppNotification>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = repository.getUnreadNotificationsCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val filteredPapers: StateFlow<List<PaperEntity>> = combine(
        allPapers,
        _uiState
    ) { papers, state ->
        papers.filter { paper ->
            val matchesBranch = state.selectedBranch == ExamBranch.ALL ||
                    paper.branch.equals(state.selectedBranch.code, ignoreCase = true)

            val matchesExamType = state.selectedExamType == ExamType.ALL ||
                    paper.examType.equals(state.selectedExamType.code, ignoreCase = true)

            val matchesSemester = state.selectedSemester == null ||
                    paper.semester == state.selectedSemester

            val matchesDownloaded = !state.onlyDownloadedFilter || paper.isDownloaded

            val matchesQuery = if (state.searchQuery.isBlank()) true else {
                val q = state.searchQuery.trim().lowercase()
                paper.subjectName.lowercase().contains(q) ||
                        paper.subjectCode.lowercase().contains(q) ||
                        paper.branch.lowercase().contains(q) ||
                        paper.academicYear.lowercase().contains(q)
            }

            matchesBranch && matchesExamType && matchesSemester && matchesDownloaded && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedBranch(branch: ExamBranch) {
        _uiState.value = _uiState.value.copy(selectedBranch = branch)
    }

    fun setSelectedExamType(type: ExamType) {
        _uiState.value = _uiState.value.copy(selectedExamType = type)
    }

    fun setSelectedSemester(semester: Int?) {
        _uiState.value = _uiState.value.copy(selectedSemester = semester)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setOnlyDownloadedFilter(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(onlyDownloadedFilter = enabled)
    }

    fun setActiveTab(tabIndex: Int) {
        _uiState.value = _uiState.value.copy(activeTab = tabIndex)
    }

    fun toggleDarkMode() {
        val next = when (_uiState.value.isDarkMode) {
            null -> true
            true -> false
            false -> null
        }
        _uiState.value = _uiState.value.copy(isDarkMode = next)
    }

    fun selectPaper(paper: PaperEntity?) {
        _uiState.value = _uiState.value.copy(selectedPaper = paper)
        if (paper != null) {
            viewModelScope.launch {
                repository.getPaperRatings(paper.id).collect { ratings ->
                    _uiState.value = _uiState.value.copy(currentRatings = ratings)
                }
            }
        }
    }

    fun downloadPaper(paperId: Long) {
        viewModelScope.launch {
            repository.downloadPaper(paperId)
            // refresh selected paper if open
            if (_uiState.value.selectedPaper?.id == paperId) {
                _uiState.value = _uiState.value.copy(
                    selectedPaper = _uiState.value.selectedPaper?.copy(isDownloaded = true)
                )
            }
            showToast("Paper downloaded for offline study!")
        }
    }

    fun removeDownloadedPaper(paperId: Long) {
        viewModelScope.launch {
            repository.deleteDownloadedPaper(paperId)
            if (_uiState.value.selectedPaper?.id == paperId) {
                _uiState.value = _uiState.value.copy(
                    selectedPaper = _uiState.value.selectedPaper?.copy(isDownloaded = false)
                )
            }
            showToast("Offline copy removed")
        }
    }

    fun toggleBookmark(paper: PaperEntity) {
        viewModelScope.launch {
            repository.toggleBookmark(paper.id, paper.isBookmarked)
            if (_uiState.value.selectedPaper?.id == paper.id) {
                _uiState.value = _uiState.value.copy(
                    selectedPaper = _uiState.value.selectedPaper?.copy(isBookmarked = !paper.isBookmarked)
                )
            }
        }
    }

    fun openUploadDialog() {
        _uiState.value = _uiState.value.copy(showUploadDialog = true)
    }

    fun closeUploadDialog() {
        _uiState.value = _uiState.value.copy(showUploadDialog = false)
    }

    fun openRatingDialog() {
        _uiState.value = _uiState.value.copy(showRatingDialog = true)
    }

    fun closeRatingDialog() {
        _uiState.value = _uiState.value.copy(showRatingDialog = false)
    }

    fun openProfileDialog() {
        _uiState.value = _uiState.value.copy(showProfileDialog = true)
    }

    fun closeProfileDialog() {
        _uiState.value = _uiState.value.copy(showProfileDialog = false)
    }

    fun openNotificationDrawer() {
        _uiState.value = _uiState.value.copy(showNotificationDrawer = true)
    }

    fun closeNotificationDrawer() {
        _uiState.value = _uiState.value.copy(showNotificationDrawer = false)
    }

    fun submitPaperUpload(
        subjectCode: String,
        subjectName: String,
        branch: String,
        semester: Int,
        examType: String,
        academicYear: String,
        examMonthYear: String,
        regulation: String,
        maxMarks: Int,
        duration: String,
        partAContent: String,
        partBContent: String,
        solutionHints: String
    ) {
        viewModelScope.launch {
            val newId = repository.uploadPaper(
                subjectCode = subjectCode,
                subjectName = subjectName,
                branch = branch,
                semester = semester,
                examType = examType,
                academicYear = academicYear,
                examMonthYear = examMonthYear,
                regulation = regulation,
                maxMarks = maxMarks,
                duration = duration,
                partAContent = partAContent,
                partBContent = partBContent,
                solutionHints = solutionHints
            )
            closeUploadDialog()
            showToast("Paper published successfully! +50 Contributor Karma earned.")
        }
    }

    fun submitRating(
        paperId: Long,
        rating: Int,
        tag: String,
        reviewComment: String
    ) {
        viewModelScope.launch {
            repository.ratePaper(paperId, rating, tag, reviewComment)
            closeRatingDialog()
            showToast("Thank you! Rating submitted.")
        }
    }

    fun updateStudentProfile(
        rollNo: String,
        fullName: String,
        email: String,
        branch: String,
        semester: Int
    ) {
        viewModelScope.launch {
            repository.loginOrUpdateProfile(rollNo, fullName, email, branch, semester)
            closeProfileDialog()
            showToast("SR University profile updated successfully!")
        }
    }

    fun markNotificationAsRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }

    fun triggerDemoNotification() {
        viewModelScope.launch {
            val student = activeStudent.value
            val branch = student?.branch ?: "CSE"
            repository.uploadPaper(
                subjectCode = "21CS502",
                subjectName = "Cloud Computing & DevOps",
                branch = branch,
                semester = 5,
                examType = "MID_1",
                academicYear = "2024-2025",
                examMonthYear = "Oct 2024",
                regulation = "R22",
                maxMarks = 30,
                duration = "90 Mins",
                partAContent = "1. Define IaaS, PaaS, and SaaS.\n2. What is containerization in Docker?\n3. Explain horizontal vs vertical scaling.",
                partBContent = "4. Explain Kubernetes architecture with Master and Worker node components.\n5. Describe CI/CD pipeline stages in Jenkins.",
                solutionHints = "Focus on container orchestration concepts for question 4."
            )
        }
    }

    fun showToast(message: String) {
        _uiState.value = _uiState.value.copy(userMessage = message)
    }

    fun dismissUserMessage() {
        _uiState.value = _uiState.value.copy(userMessage = null)
    }
}
