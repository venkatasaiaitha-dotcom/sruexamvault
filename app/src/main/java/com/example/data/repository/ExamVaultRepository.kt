package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.AppNotification
import com.example.data.model.LeaderboardEntry
import com.example.data.model.PaperEntity
import com.example.data.model.PaperRating
import com.example.data.model.StudentProfile
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File

class ExamVaultRepository(private val context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val paperDao = database.paperDao()
    private val studentDao = database.studentDao()
    private val leaderboardDao = database.leaderboardDao()
    private val ratingDao = database.ratingDao()
    private val notificationDao = database.notificationDao()

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialDataIfNeeded()
        }
    }

    fun getAllPapers(): Flow<List<PaperEntity>> = paperDao.getAllPapers()

    fun getPaperById(id: Long): Flow<PaperEntity?> = paperDao.getPaperById(id)

    fun getDownloadedPapers(): Flow<List<PaperEntity>> = paperDao.getDownloadedPapers()

    fun getBookmarkedPapers(): Flow<List<PaperEntity>> = paperDao.getBookmarkedPapers()

    fun getActiveProfile(): Flow<StudentProfile?> = studentDao.getActiveProfile()

    fun getLeaderboard(): Flow<List<LeaderboardEntry>> = leaderboardDao.getAllLeaderboard()

    fun getPaperRatings(paperId: Long): Flow<List<PaperRating>> = ratingDao.getRatingsForPaper(paperId)

    fun getNotifications(): Flow<List<AppNotification>> = notificationDao.getAllNotifications()

    fun getUnreadNotificationsCount(): Flow<Int> = notificationDao.getUnreadCount()

    suspend fun downloadPaper(paperId: Long) {
        val timestamp = System.currentTimeMillis()
        // Create an offline mock document file on local device storage
        try {
            val offlineDir = File(context.filesDir, "downloaded_papers")
            if (!offlineDir.exists()) offlineDir.mkdirs()
            val paperFile = File(offlineDir, "sru_paper_$paperId.txt")
            if (!paperFile.exists()) {
                paperFile.writeText("SR UNIVERSITY EXAM ARCHIVE\nPaper ID: $paperId\nDownloaded on: $timestamp")
            }
        } catch (_: Exception) {}

        paperDao.updateDownloadStatus(paperId, true, timestamp)
        val activeStudent = studentDao.getActiveProfile().firstOrNull()
        if (activeStudent != null) {
            studentDao.incrementDownloads(activeStudent.rollNo)
        }
    }

    suspend fun deleteDownloadedPaper(paperId: Long) {
        paperDao.updateDownloadStatus(paperId, false, 0L)
    }

    suspend fun toggleBookmark(paperId: Long, currentBookmarked: Boolean) {
        paperDao.updateBookmarkStatus(paperId, !currentBookmarked)
    }

    suspend fun uploadPaper(
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
    ): Long {
        val activeStudent = studentDao.getActiveProfile().firstOrNull()
        val uploaderName = activeStudent?.fullName ?: "SRU Student Contributor"
        val uploaderRoll = activeStudent?.rollNo ?: "21SR1A0500"
        val uploaderEmail = activeStudent?.email ?: "student@sru.edu.in"

        val newPaper = PaperEntity(
            subjectCode = subjectCode.trim().uppercase(),
            subjectName = subjectName.trim(),
            branch = branch.trim().uppercase(),
            semester = semester,
            examType = examType,
            academicYear = academicYear,
            examMonthYear = examMonthYear,
            regulation = regulation,
            maxMarks = maxMarks,
            duration = duration,
            partAContent = partAContent,
            partBContent = partBContent,
            solutionHints = solutionHints,
            uploaderName = uploaderName,
            uploaderRollNo = uploaderRoll,
            uploaderEmail = uploaderEmail,
            uploadDateMillis = System.currentTimeMillis(),
            averageRating = 5.0f,
            ratingCount = 1,
            downloadCount = 0,
            isDownloaded = true,
            downloadedTimestamp = System.currentTimeMillis()
        )

        val newId = paperDao.insertPaper(newPaper)

        // Award contribution points (+50 points)
        if (activeStudent != null) {
            studentDao.incrementContribution(activeStudent.rollNo, 50)

            // Update leaderboard entry
            val updatedPoints = activeStudent.contributionPoints + 50
            val updatedUploads = activeStudent.uploadsCount + 1
            val badge = when {
                updatedPoints >= 400 -> "Campus Legend"
                updatedPoints >= 250 -> "Senior Scholar"
                updatedPoints >= 150 -> "Top Contributor"
                else -> "Rising Scholar"
            }
            leaderboardDao.insertOrUpdate(
                LeaderboardEntry(
                    rollNo = activeStudent.rollNo,
                    studentName = activeStudent.fullName,
                    branch = activeStudent.branch,
                    semester = activeStudent.semester,
                    points = updatedPoints,
                    uploadsCount = updatedUploads,
                    averagePaperRating = 4.8f,
                    badgeTitle = badge
                )
            )
        }

        // Post in-app alert & push notification
        val notificationTitle = "New $examType Paper Uploaded!"
        val notificationMsg = "$subjectName ($subjectCode) for $branch Sem-$semester was uploaded by $uploaderName."

        notificationDao.insertNotification(
            AppNotification(
                title = notificationTitle,
                message = notificationMsg,
                branch = branch,
                paperId = newId,
                timestampMillis = System.currentTimeMillis()
            )
        )

        NotificationHelper.sendPushNotification(
            context = context,
            title = notificationTitle,
            message = notificationMsg,
            branch = branch
        )

        return newId
    }

    suspend fun ratePaper(
        paperId: Long,
        rating: Int,
        tag: String,
        reviewComment: String
    ) {
        val activeStudent = studentDao.getActiveProfile().firstOrNull()
        val raterRoll = activeStudent?.rollNo ?: "21SR1A0501"
        val raterName = activeStudent?.fullName ?: "Student Reviewer"

        ratingDao.insertRating(
            PaperRating(
                paperId = paperId,
                raterRollNo = raterRoll,
                raterName = raterName,
                rating = rating,
                tag = tag,
                reviewComment = reviewComment.trim(),
                timestampMillis = System.currentTimeMillis()
            )
        )

        val newAvg = ratingDao.getAverageRating(paperId) ?: 4.5f
        val newCount = ratingDao.getRatingCount(paperId)
        paperDao.updateRating(paperId, newAvg, newCount)
    }

    suspend fun loginOrUpdateProfile(
        rollNo: String,
        fullName: String,
        email: String,
        branch: String,
        semester: Int
    ) {
        studentDao.clearActiveFlag()
        val existing = studentDao.getProfileByRollNo(rollNo)
        val profile = if (existing != null) {
            existing.copy(
                fullName = fullName,
                email = email,
                branch = branch,
                semester = semester,
                isCurrentActiveUser = true
            )
        } else {
            StudentProfile(
                rollNo = rollNo.trim().uppercase(),
                fullName = fullName.trim(),
                email = email.trim().lowercase(),
                branch = branch,
                semester = semester,
                contributionPoints = 120,
                uploadsCount = 1,
                rankBadge = "Rising Scholar",
                isCurrentActiveUser = true
            )
        }
        studentDao.insertOrUpdateProfile(profile)

        // Ensure user is on the leaderboard
        leaderboardDao.insertOrUpdate(
            LeaderboardEntry(
                rollNo = profile.rollNo,
                studentName = profile.fullName,
                branch = profile.branch,
                semester = profile.semester,
                points = profile.contributionPoints,
                uploadsCount = profile.uploadsCount,
                averagePaperRating = 4.7f,
                badgeTitle = profile.rankBadge
            )
        )
    }

    suspend fun markNotificationAsRead(id: Long) {
        notificationDao.markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() {
        notificationDao.markAllAsRead()
    }

    private suspend fun seedInitialDataIfNeeded() {
        // Erase all question papers as requested by user
        paperDao.deleteAllPapers()

        if (studentDao.getActiveProfile().firstOrNull() == null) {
            seedInitialStudent()
        }
        if (leaderboardDao.getCount() == 0) {
            seedInitialLeaderboard()
        }
    }

    private suspend fun seedInitialStudent() {
        val defaultStudent = StudentProfile(
            rollNo = "21SR1A05A2",
            fullName = "Venkat Sai Aitha",
            email = "venkatasaiaitha@sru.edu.in",
            branch = "CSE",
            semester = 5,
            contributionPoints = 190,
            uploadsCount = 3,
            downloadsCount = 12,
            rankBadge = "Top Contributor",
            isCurrentActiveUser = true
        )
        studentDao.insertOrUpdateProfile(defaultStudent)
    }

    private suspend fun seedInitialLeaderboard() {
        val initialEntries = listOf(
            LeaderboardEntry(
                rollNo = "20SR1A0512",
                studentName = "Rohith Varma",
                branch = "CSE",
                semester = 7,
                points = 480,
                uploadsCount = 9,
                averagePaperRating = 4.9f,
                badgeTitle = "Campus Legend",
                rank = 1
            ),
            LeaderboardEntry(
                rollNo = "21SR1A0418",
                studentName = "Pooja Reddy",
                branch = "ECE",
                semester = 6,
                points = 360,
                uploadsCount = 7,
                averagePaperRating = 4.8f,
                badgeTitle = "Senior Scholar",
                rank = 2
            ),
            LeaderboardEntry(
                rollNo = "21SR1A05A2",
                studentName = "Venkat Sai Aitha",
                branch = "CSE",
                semester = 5,
                points = 190,
                uploadsCount = 3,
                averagePaperRating = 4.7f,
                badgeTitle = "Top Contributor",
                rank = 3
            ),
            LeaderboardEntry(
                rollNo = "21SR1A0244",
                studentName = "Naveen Kumar",
                branch = "EEE",
                semester = 5,
                points = 170,
                uploadsCount = 3,
                averagePaperRating = 4.6f,
                badgeTitle = "Top Contributor",
                rank = 4
            ),
            LeaderboardEntry(
                rollNo = "22SR1A0309",
                studentName = "Harsha Vardhan",
                branch = "MECH",
                semester = 4,
                points = 140,
                uploadsCount = 2,
                averagePaperRating = 4.5f,
                badgeTitle = "Rising Scholar",
                rank = 5
            ),
            LeaderboardEntry(
                rollNo = "22SR1A0105",
                studentName = "Anusha Patil",
                branch = "CIVIL",
                semester = 4,
                points = 125,
                uploadsCount = 2,
                averagePaperRating = 4.6f,
                badgeTitle = "Rising Scholar",
                rank = 6
            ),
            LeaderboardEntry(
                rollNo = "22SR1A6615",
                studentName = "Karthik Raja",
                branch = "AIML",
                semester = 3,
                points = 110,
                uploadsCount = 2,
                averagePaperRating = 4.4f,
                badgeTitle = "Rising Scholar",
                rank = 7
            )
        )
        leaderboardDao.insertAll(initialEntries)
    }

    private suspend fun seedInitialPapers() {
        val papers = listOf(
            // 1. CSE - DSA (Sem End)
            PaperEntity(
                subjectCode = "21CS301",
                subjectName = "Data Structures & Algorithms",
                branch = "CSE",
                semester = 3,
                examType = "SEM_END",
                academicYear = "2023-2024",
                examMonthYear = "Dec 2023",
                regulation = "R22",
                maxMarks = 60,
                duration = "3 Hours",
                instructions = "Part-A is compulsory (10 x 2 = 20 Marks). Part-B: Answer any FIVE full questions (5 x 8 = 40 Marks).",
                partAContent = """
1. Define asymptotic notations (Big-O, Omega, Theta) with mathematical representations. [2M]
2. Differentiate between circular queue and linear queue with overflow conditions. [2M]
3. What is an AVL tree? Define balance factor with a small example. [2M]
4. State the advantages of Doubly Linked List over Singly Linked List. [2M]
5. Write the recurrence relation for Merge Sort and find its worst-case complexity. [2M]
6. Compare DFS and BFS traversal with respect to data structures utilized. [2M]
7. Explain collision resolution in hashing using Quadratic Probing. [2M]
8. Define B-Tree of order m and specify the maximum number of children in root. [2M]
9. What is a topological sort? Give an example of a Directed Acyclic Graph (DAG). [2M]
10. State the minimum spanning tree properties used in Prim's algorithm. [2M]
                """.trimIndent(),
                partBContent = """
11. (a) Write an algorithm to convert an infix expression to postfix using a Stack. Trace for (A + B) * (C - D / E). [5M]
    (b) Implement basic queue operations using two stacks with time complexity analysis. [3M]
    -- OR --
12. (a) Write a complete C/C++ function to reverse a Singly Linked List iteratively. [5M]
    (b) Explain polynomial addition representation using linked lists with diagrams. [3M]

13. (a) Construct an AVL Tree by inserting the following keys sequentially: 15, 20, 24, 10, 13, 7, 30, 36, 25. Mention rotations. [5M]
    (b) Write recursive algorithms for Inorder, Preorder, and Postorder tree traversals. [3M]
    -- OR --
14. (a) Explain Dijkstra's shortest path algorithm. Find the shortest path from vertex A for the given weighted graph. [5M]
    (b) Formulate Kruskal's algorithm for finding Minimum Spanning Tree with Disjoint Set Union. [3M]

15. (a) Explain Quick Sort algorithm with partitioning logic. Analyze its average and worst-case time complexity. [5M]
    (b) Illustrate Radix Sort for the numbers: 170, 45, 75, 90, 802, 24, 2, 66. [3M]
                """.trimIndent(),
                solutionHints = "Key Tips: For Q13 AVL tree, remember double rotations (LR and RL) whenever balance factor deviates beyond {-1, 0, 1}. For Q11 Stack conversion, track operator precedence correctly (*, / before +, -).",
                uploaderName = "Rohith Varma",
                uploaderRollNo = "20SR1A0512",
                uploaderEmail = "rohith.v@sru.edu.in",
                averageRating = 4.9f,
                ratingCount = 18,
                downloadCount = 142,
                isDownloaded = true,
                downloadedTimestamp = System.currentTimeMillis() - 86400000L,
                isBookmarked = true
            ),

            // 2. CSE - Operating Systems (Mid 1)
            PaperEntity(
                subjectCode = "21CS401",
                subjectName = "Operating Systems",
                branch = "CSE",
                semester = 4,
                examType = "MID_1",
                academicYear = "2023-2024",
                examMonthYear = "March 2024",
                regulation = "R22",
                maxMarks = 30,
                duration = "90 Mins",
                instructions = "Answer Question 1 (10 Marks). Answer any TWO from Q2, Q3, Q4 (10 Marks each).",
                partAContent = """
1. (a) What is a System Call? How does it differ from a library function call? [2M]
   (b) Draw the Process State Transition Diagram with 5 states. [2M]
   (c) Define turnaround time and waiting time in CPU scheduling. [2M]
   (d) What is a PCB (Process Control Block)? List four fields in it. [2M]
   (e) Differentiate between preemptive and non-preemptive scheduling. [2M]
                """.trimIndent(),
                partBContent = """
2. Consider the following processes with CPU burst times (P1: 8ms, P2: 4ms, P3: 9ms, P4: 5ms) arriving at time 0.
   Draw Gantt charts and compute average waiting time for:
   (i) FCFS (ii) SJF (Non-preemptive) (iii) Round Robin (Quantum = 3ms). [10M]

3. Explain the Critical Section Problem. State the three requirements (Mutual Exclusion, Progress, Bounded Waiting) and explain Peterson's solution for two processes. [10M]

4. (a) Explain Multithreading models (Many-to-One, One-to-One, Many-to-Many). [5M]
   (b) Describe Inter-Process Communication (IPC) via Shared Memory and Message Passing. [5M]
                """.trimIndent(),
                solutionHints = "Formulas: Turnaround Time = Completion Time - Arrival Time. Waiting Time = Turnaround Time - Burst Time. In Round Robin, maintain ready queue order strictly.",
                uploaderName = "Venkat Sai Aitha",
                uploaderRollNo = "21SR1A05A2",
                uploaderEmail = "venkatasaiaitha@sru.edu.in",
                averageRating = 4.8f,
                ratingCount = 14,
                downloadCount = 89,
                isDownloaded = false,
                downloadedTimestamp = 0L,
                isBookmarked = false
            ),

            // 3. CSE - DBMS (Mid 2)
            PaperEntity(
                subjectCode = "21CS302",
                subjectName = "Database Management Systems",
                branch = "CSE",
                semester = 3,
                examType = "MID_2",
                academicYear = "2023-2024",
                examMonthYear = "Nov 2023",
                regulation = "R22",
                maxMarks = 30,
                duration = "90 Mins",
                instructions = "Answer ALL questions.",
                partAContent = """
1. Define 3NF and BCNF with functional dependency conditions. [3M]
2. What are ACID properties in transaction processing? [3M]
3. Differentiate between conflict serializability and view serializability. [2M]
4. State the difference between dense index and sparse index. [2M]
                """.trimIndent(),
                partBContent = """
5. Given relation R(A, B, C, D, E) and FDs {A->BC, CD->E, B->D, E->A}.
   Find candidate keys and determine highest normal form of R. Decompose to BCNF if necessary. [10M]

6. Explain Two-Phase Locking (2PL) protocol. Contrast Strict 2PL and Rigorous 2PL. How does 2PL ensure serializability? [10M]
                """.trimIndent(),
                solutionHints = "Candidate key computation: calculate closures {A}+ = {A,B,C,D,E}. Hence A is candidate key. Also check E and CD.",
                uploaderName = "Pooja Reddy",
                uploaderRollNo = "21SR1A0418",
                uploaderEmail = "pooja.r@sru.edu.in",
                averageRating = 4.7f,
                ratingCount = 9,
                downloadCount = 65,
                isDownloaded = true,
                downloadedTimestamp = System.currentTimeMillis() - 43200000L,
                isBookmarked = true
            ),

            // 4. ECE - Digital Signal Processing (Sem End)
            PaperEntity(
                subjectCode = "21EC402",
                subjectName = "Digital Signal Processing",
                branch = "ECE",
                semester = 4,
                examType = "SEM_END",
                academicYear = "2023-2024",
                examMonthYear = "May 2024",
                regulation = "R22",
                maxMarks = 60,
                duration = "3 Hours",
                instructions = "Part A (10x2=20M), Part B (5x8=40M). Answer all sections.",
                partAContent = """
1. Define Discrete Fourier Transform (DFT) and its inverse formula. [2M]
2. State the circular convolution property of DFT. [2M]
3. What is the twiddle factor W_N and state its periodicity property? [2M]
4. Compare computational complexity of Direct DFT vs Radix-2 FFT for N=1024. [2M]
5. Differentiate between IIR and FIR filters in terms of phase linearity. [2M]
6. What is Bilinear Transformation? Why is frequency warping caused? [2M]
7. What are the common windowing techniques used in FIR filter design? [2M]
8. Define limit cycle oscillations due to product round-off error. [2M]
9. What is multirate signal processing? Define decimation and interpolation. [2M]
10. State two real-time applications of DSP processors. [2M]
                """.trimIndent(),
                partBContent = """
11. Compute the 8-point DFT of the sequence x(n) = {1, 2, 2, 1, 1, 2, 2, 1} using Decimation-in-Time (DIT) FFT algorithm. Draw signal flow graph. [8M]
12. Design a digital Butterworth low-pass filter using Bilinear Transformation with sampling frequency 10 kHz, passband cutoff 1.5 kHz, stopband cutoff 3 kHz, passband ripple <= 1 dB, stopband attenuation >= 15 dB. [8M]
13. Design an FIR high-pass filter using Hamming window with cutoff frequency wc = pi/4 and length N = 7. [8M]
                """.trimIndent(),
                solutionHints = "For DIT-FFT: bit-reversal indexing for inputs: 0, 4, 2, 6, 1, 5, 3, 7. Pre-warp frequencies for Bilinear: Omega = (2/T)*tan(omega/2).",
                uploaderName = "Pooja Reddy",
                uploaderRollNo = "21SR1A0418",
                uploaderEmail = "pooja.r@sru.edu.in",
                averageRating = 4.8f,
                ratingCount = 12,
                downloadCount = 98,
                isDownloaded = false,
                downloadedTimestamp = 0L,
                isBookmarked = false
            ),

            // 5. EEE - Power Systems-I (Sem End)
            PaperEntity(
                subjectCode = "21EE401",
                subjectName = "Power Systems Engineering",
                branch = "EEE",
                semester = 4,
                examType = "SEM_END",
                academicYear = "2023-2024",
                examMonthYear = "June 2024",
                regulation = "R22",
                maxMarks = 60,
                duration = "3 Hours",
                instructions = "Answer all questions in Part A and any five from Part B.",
                partAContent = """
1. Define load factor, diversity factor and plant capacity factor. [2M]
2. Why is skin effect prominent in AC transmission lines? [2M]
3. Explain proximity effect and its impact on line resistance. [2M]
4. Define string efficiency of suspension insulators. [2M]
5. What is sag in overhead transmission lines? Mention factors influencing it. [2M]
6. Compare nominal-T and nominal-Pi representation of medium transmission lines. [2M]
7. What is Ferranti effect? Under what conditions does it occur? [2M]
8. Describe the methods of improving string efficiency using grading rings. [2M]
9. What is corona phenomenon? State factors affecting disruptive critical voltage. [2M]
10. State advantages of underground cables over overhead lines. [2M]
                """.trimIndent(),
                partBContent = """
11. Derive an expression for loop inductance and capacitance of a 3-phase symmetrically spaced overhead transmission line. [8M]
12. A 3-phase, 50 Hz, 132 kV overhead transmission line has conductors of 1.2 cm diameter spaced 3m symmetrically. Calculate capacitance and charging current per km. [8M]
13. Derive expressions for sending end voltage and current using ABCD parameters for a medium line using Nominal-Pi method. [8M]
                """.trimIndent(),
                solutionHints = "Sag formula: S = (w * L^2) / (8 * T). In Nominal-Pi, half charging capacitance placed at sending end and half at receiving end.",
                uploaderName = "Naveen Kumar",
                uploaderRollNo = "21SR1A0244",
                uploaderEmail = "naveen.k@sru.edu.in",
                averageRating = 4.6f,
                ratingCount = 11,
                downloadCount = 74,
                isDownloaded = true,
                downloadedTimestamp = System.currentTimeMillis() - 172800000L,
                isBookmarked = false
            ),

            // 6. MECH - Applied Thermodynamics (Sem End)
            PaperEntity(
                subjectCode = "21ME301",
                subjectName = "Applied Thermodynamics",
                branch = "MECH",
                semester = 3,
                examType = "SEM_END",
                academicYear = "2023-2024",
                examMonthYear = "Jan 2024",
                regulation = "R22",
                maxMarks = 60,
                duration = "3 Hours",
                instructions = "Steam tables and Mollier chart permitted. Answer all questions.",
                partAContent = """
1. State the First Law of Thermodynamics for an open steady flow system. [2M]
2. Define Clausius inequality and entropy principle. [2M]
3. What is an air standard cycle? State assumptions in Otto cycle. [2M]
4. Draw p-V and T-s diagram for Diesel cycle. [2M]
5. What is reheat factor in steam turbines? [2M]
6. Define volumetric efficiency of reciprocating compressor. [2M]
7. Explain the function of an economizer in boiler plant. [2M]
8. Differentiate between impulse and reaction turbines. [2M]
9. What is dryness fraction of steam? [2M]
10. Define coefficient of performance (COP) for refrigeration cycle. [2M]
                """.trimIndent(),
                partBContent = """
11. An engine working on Otto cycle has bore 200 mm and stroke 300 mm. Clearance volume is 0.0016 m^3. Calculate compression ratio, air standard efficiency, and mean effective pressure if initial conditions are 1 bar and 27 C. [8M]
12. In a Rankine cycle, steam enters turbine at 30 bar, 350 C and exhausts to condenser at 0.1 bar. Determine thermal efficiency, specific steam consumption, and work ratio. [8M]
13. Explain multistage compression with intercooling. Derive condition for minimum work of compression in a two-stage air compressor. [8M]
                """.trimIndent(),
                solutionHints = "Condition for minimum work: Intermediate pressure P2 = sqrt(P1 * P3). Rankine efficiency = (h1 - h2) / (h1 - hf2).",
                uploaderName = "Harsha Vardhan",
                uploaderRollNo = "22SR1A0309",
                uploaderEmail = "harsha.v@sru.edu.in",
                averageRating = 4.5f,
                ratingCount = 8,
                downloadCount = 59,
                isDownloaded = false,
                downloadedTimestamp = 0L,
                isBookmarked = false
            ),

            // 7. CIVIL - Structural Analysis (Sem End)
            PaperEntity(
                subjectCode = "21CE401",
                subjectName = "Structural Analysis - I",
                branch = "CIVIL",
                semester = 4,
                examType = "SEM_END",
                academicYear = "2023-2024",
                examMonthYear = "June 2024",
                regulation = "R22",
                maxMarks = 60,
                duration = "3 Hours",
                instructions = "Answer all questions in Part A and any five in Part B.",
                partAContent = """
1. Differentiate between determinate and indeterminate structures with degree formulas. [2M]
2. State Castigliano's First and Second Theorem. [2M]
3. Define static and kinematic indeterminacy for a continuous beam. [2M]
4. Explain Betti's law of reciprocal deflections. [2M]
5. What is an influence line diagram (ILD)? [2M]
6. State the three-moment equation (Clapeyron's theorem). [2M]
7. What are the advantages of propped cantilever over simple cantilever? [2M]
8. Define shape factor in plastic analysis. [2M]
9. What is carry-over factor in moment distribution method? [2M]
10. Explain tension coefficient method for space trusses. [2M]
                """.trimIndent(),
                partBContent = """
11. A continuous beam ABC covers two spans AB = 6m and BC = 4m. Span AB carries UDL of 20 kN/m and BC carries concentrated load of 60 kN at mid-span. Calculate support moments using Clapeyron's theorem of three moments. Draw SFD and BMD. [8M]
12. Analyze a propped cantilever beam of span L carrying UDL w/unit length over entire span using Strain Energy method. Find prop reaction and fixity moment. [8M]
13. Determine deflection at mid-span for a simply supported beam with point load W at center using Unit Load Method. [8M]
                """.trimIndent(),
                solutionHints = "Clapeyron equation: M_A*L1 + 2*M_B*(L1+L2) + M_C*L2 = -6*(a1*x1/L1 + a2*x2/L2). Prop reaction for propped cantilever under UDL = 3/8 * w * L.",
                uploaderName = "Anusha Patil",
                uploaderRollNo = "22SR1A0105",
                uploaderEmail = "anusha.p@sru.edu.in",
                averageRating = 4.7f,
                ratingCount = 10,
                downloadCount = 62,
                isDownloaded = false,
                downloadedTimestamp = 0L,
                isBookmarked = false
            ),

            // 8. AIML - Machine Learning Foundations (Sem End)
            PaperEntity(
                subjectCode = "21AI401",
                subjectName = "Machine Learning Foundations",
                branch = "AIML",
                semester = 4,
                examType = "SEM_END",
                academicYear = "2023-2024",
                examMonthYear = "May 2024",
                regulation = "R22",
                maxMarks = 60,
                duration = "3 Hours",
                instructions = "Answer all questions in Part A and any five from Part B.",
                partAContent = """
1. Differentiate between Supervised, Unsupervised, and Reinforcement Learning. [2M]
2. Define bias-variance tradeoff with graphical illustration. [2M]
3. What is L1 (Lasso) and L2 (Ridge) regularization? [2M]
4. Explain Precision, Recall, F1-Score, and ROC-AUC curve. [2M]
5. What is the role of activation function in neural network? [2M]
6. Define entropy and information gain in ID3 decision tree algorithm. [2M]
7. What is kernel trick in Support Vector Machines (SVM)? [2M]
8. Differentiate between Bagging and Boosting ensemble methods. [2M]
9. What is K-Means clustering? How is optimal K selected using Elbow method? [2M]
10. State Bayes theorem and its application in Naive Bayes classifier. [2M]
                """.trimIndent(),
                partBContent = """
11. (a) Derive closed-form normal equation for Linear Regression with Mean Squared Error loss. [5M]
    (b) Explain Gradient Descent algorithm with learning rate considerations and step-by-step update rule. [3M]
12. Construct a Decision Tree for a dataset with features [Weather, Temperature, Humidity, Wind] and target [Play Tennis] using Information Gain criterion. [8M]
13. Explain the architecture of Multilayer Perceptron (MLP). Derive the Backpropagation weight update rule using chain rule of calculus. [8M]
                """.trimIndent(),
                solutionHints = "Information Gain formula: Gain(S, A) = Entropy(S) - sum(|Sv|/|S| * Entropy(Sv)). In backprop: delta_j = (y_j - t_j) * f'(net_j).",
                uploaderName = "Karthik Raja",
                uploaderRollNo = "22SR1A6615",
                uploaderEmail = "karthik.r@sru.edu.in",
                averageRating = 4.9f,
                ratingCount = 21,
                downloadCount = 168,
                isDownloaded = true,
                downloadedTimestamp = System.currentTimeMillis() - 120000000L,
                isBookmarked = true
            )
        )

        paperDao.insertAll(papers)
    }
}
