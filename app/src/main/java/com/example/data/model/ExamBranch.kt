package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.BranchAIML
import com.example.ui.theme.BranchCSE
import com.example.ui.theme.BranchCivil
import com.example.ui.theme.BranchECE
import com.example.ui.theme.BranchEEE
import com.example.ui.theme.BranchMech

enum class ExamBranch(val code: String, val displayName: String, val accentColor: Color) {
    ALL("ALL", "All Branches", Color(0xFF4B5563)),
    CSE("CSE", "Computer Science", BranchCSE),
    ECE("ECE", "Electronics & Comm", BranchECE),
    EEE("EEE", "Electrical & Electronics", BranchEEE),
    MECH("MECH", "Mechanical Engg", BranchMech),
    CIVIL("CIVIL", "Civil Engg", BranchCivil),
    AIML("AIML", "AI & Machine Learning", BranchAIML);

    companion object {
        fun fromCode(code: String): ExamBranch {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: CSE
        }
    }
}
