package com.education.calmlearn.ui.lessons.word

sealed class GradeResult {
    data class Passed(val feedback: String) : GradeResult()
    data class NotPassed(val feedback: String) : GradeResult()
    data class Unparsable(val raw: String) : GradeResult()
}

object WordPracticeGrader {

    const val SENTENCE_COUNT = 5

    private const val PASSED = "DAT"
    private const val NOT_PASSED = "CHUA_DAT"

    const val SYSTEM_INSTRUCTION =
        "Bạn là giáo viên tiếng Anh chấm bài. Người học phải viết $SENTENCE_COUNT câu tiếng Anh, " +
            "mỗi câu có dùng từ cho trước. Dòng đầu tiên của câu trả lời phải là đúng một từ: " +
            "$PASSED nếu cả $SENTENCE_COUNT câu đều dùng đúng từ đó và đúng ngữ pháp cơ bản, " +
            "hoặc $NOT_PASSED nếu còn lỗi ngữ pháp hoặc dùng sai từ. Bỏ qua lỗi dấu câu " +
            "(ví dụ thiếu dấu chấm cuối câu) và lỗi viết hoa/viết thường, không tính đó là lỗi khi chấm. " +
            "Không thêm ký tự hay chữ nào khác trên dòng đầu tiên. " +
            "Các dòng sau là nhận xét ngắn gọn bằng tiếng Việt cho từng câu."

    fun buildPrompt(word: String, sentences: List<String>): String = buildString {
        append("Từ cần dùng: ").append(word).append('\n')
        sentences.forEachIndexed { index, sentence ->
            append("Câu ").append(index + 1).append(": ").append(sentence).append('\n')
        }
    }.trim()

    fun parse(reply: String): GradeResult {
        val lines = reply.trim().lines()
        val feedback = lines.drop(1).joinToString("\n").trim()
        return when (lines.firstOrNull()?.trim()) {
            PASSED -> GradeResult.Passed(feedback)
            NOT_PASSED -> GradeResult.NotPassed(feedback)
            else -> GradeResult.Unparsable(reply.trim())
        }
    }
}
