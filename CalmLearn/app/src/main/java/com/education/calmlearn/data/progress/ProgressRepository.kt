package com.education.calmlearn.data.progress

import com.education.calmlearn.data.auth.AuthErrorReason

/**
 * Tach biet man hinh (Fragment) khoi dich vu luu tien do hoc tap that. Cung mot pattern voi
 * AuthRepository/AuthRepositoryProvider (xem data/auth/).
 */
interface ProgressRepository {

    /** Doc tien do hien tai cua nguoi dang dang nhap. */
    suspend fun loadProgress(): ProgressResult

    /**
     * Danh dau/bo danh dau mot tu la "da thuoc". Chi cong XP + cap nhat streak trong lan DAU TIEN
     * tu nay duoc danh dau hoc xong (xem [LearningProgress.xpAwardedWordIds]) - goi lai nhieu lan
     * (vd bam qua lai "Da thuoc"/"Can on lai") khong tao them XP.
     */
    suspend fun setWordLearned(wordId: String, learned: Boolean): ProgressResult

    /** Bat/tat yeu thich cho mot tu - khong lien quan XP/streak. */
    suspend fun setFavorite(wordId: String, favorite: Boolean): ProgressResult

    /**
     * Ghi nhan mot lan hoan thanh quiz. Cong XP + cap nhat streak CHI trong lan hoan thanh DAU
     * TIEN cua [quizId] trong ngay hom nay (lam lai quiz nhieu lan trong ngay van duoc, nhung
     * khong sinh them XP - xem [LearningProgress.quizXpAwardedKeys]). Do chinh xac lan nay luon
     * duoc ghi lai (khong lien quan gioi han XP).
     */
    suspend fun recordQuizResult(quizId: String, correctCount: Int, totalQuestions: Int): ProgressResult

    /**
     * Ghi nhan da hoan thanh MOT luot luyen phat am o [level] (vd "WORD"/"PHRASE"/"SENTENCE").
     * Ket qua dung/sai tung cau trong Speaking la mo phong nen KHONG duoc dung de tinh XP/do
     * chinh xac - chi ghi nhan SU KIEN hoan thanh, cong XP + streak toi da 1 lan/cap do/ngay.
     */
    suspend fun recordSpeakingSessionCompleted(level: String): ProgressResult

    /**
     * Ghi nhan da hoan thanh xong toan bo cau hoi cua mot bai luyen nghe [lessonId]. Cong XP +
     * streak toi da 1 lan/bai/ngay (lam lai trong ngay khong sinh them XP).
     */
    suspend fun recordListeningLessonCompleted(lessonId: String): ProgressResult

    /**
     * Cong them [seconds] giay vao tong thoi gian hoc THAT cua HOM NAY (tu dong reset ve 0 khi
     * sang ngay moi). KHONG lien quan XP/streak - chi phuc vu widget "Muc tieu hom nay" tren Home.
     */
    suspend fun addStudySeconds(seconds: Int): ProgressResult

    /**
     * Ghi nhan da xem/kiem tra mini-quiz cua bai ngu phap [lessonId] LAN DAU TIEN (mot lan cho ca
     * doi, khong theo ngay - khac Quiz/Speaking/Listening la noi dung luyen tap lap lai). Cong XP +
     * streak toi da 1 lan/bai; goi lai voi cung lessonId khong sinh them XP.
     */
    suspend fun recordGrammarLessonCompleted(lessonId: String): ProgressResult
}

/** Ket qua doc/ghi tien do - cung nguyen tac voi ProfileResult: khong gop "chua dang nhap" va
 *  "loi doc/ghi" thanh mot gia tri null duy nhat. */
sealed class ProgressResult {
    data class Loaded(val progress: LearningProgress) : ProgressResult()
    object NotSignedIn : ProgressResult()
    data class ReadError(val reason: AuthErrorReason) : ProgressResult()
}
