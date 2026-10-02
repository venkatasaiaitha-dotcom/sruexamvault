package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ExamBranch
import com.example.data.model.ExamType

@Composable
fun UploadPaperDialog(
    onDismiss: () -> Unit,
    onSubmit: (
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
    ) -> Unit
) {
    var subjectCode by remember { mutableStateOf("") }
    var subjectName by remember { mutableStateOf("") }
    var branch by remember { mutableStateOf("CSE") }
    var semester by remember { mutableIntStateOf(4) }
    var examType by remember { mutableStateOf("SEM_END") }
    var academicYear by remember { mutableStateOf("2023-2024") }
    var examMonthYear by remember { mutableStateOf("Dec 2023") }
    var regulation by remember { mutableStateOf("R22") }
    var maxMarks by remember { mutableIntStateOf(60) }
    var duration by remember { mutableStateOf("3 Hours") }
    var partAContent by remember { mutableStateOf("") }
    var partBContent by remember { mutableStateOf("") }
    var solutionHints by remember { mutableStateOf("") }

    var branchDropdownOpen by remember { mutableStateOf(false) }
    var examTypeDropdownOpen by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Contribute Question Paper",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_upload_dialog")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Incentive Karma Banner
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Karma Reward",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Earn +50 Contributor Points and climb the SR University Leaderboard!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                // Branch & Semester Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Branch Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            Text("Branch", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .clickable { branchDropdownOpen = true }
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                    .testTag("branch_selector_trigger")
                            ) {
                                Text(branch, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            DropdownMenu(
                                expanded = branchDropdownOpen,
                                onDismissRequest = { branchDropdownOpen = false }
                            ) {
                                listOf("CSE", "ECE", "EEE", "MECH", "CIVIL", "AIML").forEach { b ->
                                    DropdownMenuItem(
                                        text = { Text(b) },
                                        onClick = {
                                            branch = b
                                            branchDropdownOpen = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Semester Selector
                    Box(modifier = Modifier.weight(1f)) {
                        Column {
                            Text("Semester", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(1, 2, 3, 4, 5, 6, 7, 8).take(4).forEach { sem ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (semester == sem) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                            )
                                            .clickable { semester = sem }
                                            .padding(vertical = 10.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$sem",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (semester == sem) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Exam Type Selector (Mid-1, Mid-2, Sem-End)
                Column {
                    Text("Exam Type", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "MID_1" to "Mid-1",
                            "MID_2" to "Mid-2",
                            "SEM_END" to "Semester End",
                            "SUPPLEMENTARY" to "Supply"
                        ).forEach { (code, label) ->
                            val isSelected = examType == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        examType = code
                                        if (code.startsWith("MID")) {
                                            maxMarks = 30
                                            duration = "90 Mins"
                                        } else {
                                            maxMarks = 60
                                            duration = "3 Hours"
                                        }
                                    }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Name & Code
                OutlinedTextField(
                    value = subjectName,
                    onValueChange = { subjectName = it },
                    label = { Text("Subject Name (e.g. Computer Networks)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("upload_subject_name"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = subjectCode,
                        onValueChange = { subjectCode = it },
                        label = { Text("Subject Code (e.g. 21CS501)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_subject_code"),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = regulation,
                        onValueChange = { regulation = it },
                        label = { Text("Regulation (e.g. R22)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("upload_regulation"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = academicYear,
                        onValueChange = { academicYear = it },
                        label = { Text("Academic Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = examMonthYear,
                        onValueChange = { examMonthYear = it },
                        label = { Text("Exam Month/Year") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                // Questions Content
                OutlinedTextField(
                    value = partAContent,
                    onValueChange = { partAContent = it },
                    label = { Text("Part A Questions (Short Answer)") },
                    placeholder = { Text("1. Define Big-O notation [2M]\n2. State two properties of B-Trees [2M]...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .testTag("upload_part_a"),
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = partBContent,
                    onValueChange = { partBContent = it },
                    label = { Text("Part B Questions (Long Answer)") },
                    placeholder = { Text("3. (a) Explain AVL Tree rotations with diagrams [5M]\n   (b) Describe Dijkstra's algorithm [5M]...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .testTag("upload_part_b"),
                    maxLines = 8
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = solutionHints,
                    onValueChange = { solutionHints = it },
                    label = { Text("Solving Hints / Formula Key (Optional)") },
                    placeholder = { Text("Add key formulas, syllabus references, or answer hints...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    maxLines = 4
                )

                if (validationError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            if (subjectName.isBlank()) {
                                validationError = "Please enter subject name"
                                return@Button
                            }
                            if (subjectCode.isBlank()) {
                                validationError = "Please enter subject code"
                                return@Button
                            }
                            if (partAContent.isBlank() && partBContent.isBlank()) {
                                validationError = "Please enter at least some question content"
                                return@Button
                            }
                            validationError = null
                            onSubmit(
                                subjectCode,
                                subjectName,
                                branch,
                                semester,
                                examType,
                                academicYear,
                                examMonthYear,
                                regulation,
                                maxMarks,
                                duration,
                                partAContent.ifBlank { "Questions archived by student." },
                                partBContent.ifBlank { "Refer to standard SRU syllabus for full descriptive questions." },
                                solutionHints
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.testTag("submit_upload_paper")
                    ) {
                        Text("Publish Paper (+50 Pts)")
                    }
                }
            }
        }
    }
}
