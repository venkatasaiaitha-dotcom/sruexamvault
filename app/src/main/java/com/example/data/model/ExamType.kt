package com.example.data.model

enum class ExamType(val code: String, val displayName: String, val shortBadge: String) {
    ALL("ALL", "All Exams", "ALL"),
    MID_1("MID_1", "Mid-Term 1", "MID-I"),
    MID_2("MID_2", "Mid-Term 2", "MID-II"),
    SEM_END("SEM_END", "Semester End Exam", "SEM-END"),
    SUPPLEMENTARY("SUPPLEMENTARY", "Supplementary", "SUPPLY");

    companion object {
        fun fromCode(code: String): ExamType {
            return entries.find { it.code.equals(code, ignoreCase = true) } ?: SEM_END
        }
    }
}
