package com.education.calmlearn.data.progress

/**
 * Tien do hoc tap cua MOT nguoi dung, doc/ghi qua [ProgressRepository]. Chi luu trang thai (id tu,
 * XP, streak) - KHONG luu lai noi dung tu vung (dinh nghia, vi du, phat am...), noi dung tinh van
 * lay tu MockData.
 *
 * [xpAwardedWordIds] la tap MOT CHIEU (chi them, khong bao gio xoa): dung de dam bao moi tu chi
 * duoc cong XP DUNG MOT LAN duy nhat trong doi, du nguoi dung bam "Da thuoc"/"Can on lai" qua lai
 * nhieu lan sau do (xem FirebaseProgressRepository.setWordLearned).
 */
data class LearningProgress(
    val learnedWordIds: Set<String> = emptySet(),
    val favoriteWordIds: Set<String> = emptySet(),
    val xpAwardedWordIds: Set<String> = emptySet(),
    /** Khoa "{quizId}|{ngay}" da duoc cong XP - dung de moi quiz chi cong XP DUNG MOT LAN cho MOI
     *  NGAY (lam lai quiz nhieu lan trong cung 1 ngay khong sinh them XP, nhung van duoc lam lai
     *  thoai mai de luyen tap - xem FirebaseProgressRepository.recordQuizResult). */
    val quizXpAwardedKeys: Set<String> = emptySet(),
    val xp: Int = 0,
    val streak: Int = 0,
    val lastActiveDate: String? = null,
    /** Do chinh xac (%) cua lan lam quiz GAN NHAT - ghi de moi lan, khong cong don, khong the "farm". */
    val lastQuizAccuracyPercent: Int? = null,
    /** So lan hoan thanh quiz dat >=90% CHINH XAC, moi ngay chi tinh toi da 1 lan cho moi quiz (dung
     *  chung "cong" voi quizXpAwardedKeys nen khong the farm bang cach lam lai quiz nhieu lan). Dung
     *  de danh gia huy hieu "Quiz Master" (xem MockData.applyAchievementProgress). */
    val highAccuracyQuizCount: Int = 0,

    /** Khoa "{level}|{ngay}" da duoc cong XP cho luyen phat am - toi da 1 lan/cap do/ngay. Ket qua
     *  dung/sai trong Speaking la MO PHONG (Random, xem SpeakingFragment.showResult) nen KHONG luu
     *  do chinh xac that - chi luu SU KIEN "da hoan thanh mot luot luyen" (co that, xac minh duoc). */
    val speakingSessionAwardedKeys: Set<String> = emptySet(),
    /** Tong so luot luyen phat am da hoan thanh (cong don qua thoi gian) - dung cho huy hieu
     *  "Speaking Star". */
    val speakingSessionCount: Int = 0,

    /** Khoa "{lessonId}|{ngay}" da duoc cong XP cho mot bai luyen nghe - toi da 1 lan/bai/ngay. */
    val listeningSessionAwardedKeys: Set<String> = emptySet(),
    /** Tong so bai luyen nghe da hoan thanh (cong don qua thoi gian) - dung cho huy hieu
     *  "Great Listener". */
    val listeningSessionCount: Int = 0,

    /** So giay THAT nguoi dung o foreground tren cac man hinh hoc tap TRONG NGAY [todayStudyDate].
     *  Tu dong ve 0 khi doc/ghi vao mot ngay moi (xem FirebaseProgressRepository) - KHONG lien quan
     *  XP/streak (chi la chi so "thoi gian hoc" rieng cho widget Muc tieu hom nay tren Home). */
    val todayStudySeconds: Int = 0,
    val todayStudyDate: String? = null,
    /** Tong so giay hoc THAT cong don TOAN BO thoi gian (khong bao gio reset, khac voi
     *  todayStudySeconds) - dung cho "Tong thoi gian hoc" trong Progress. */
    val totalStudySeconds: Int = 0,

    /** Id cac bai ngu phap da bam "Kiem tra" mini-quiz LAN DAU TIEN (mot lan cho ca doi, giong
     *  xpAwardedWordIds - bai ngu phap la noi dung doc mot lan, khong phai luyen tap lap lai nhu
     *  Quiz/Speaking/Listening). Dung de tinh "Bai hoc hoan thanh" trong Progress, cong voi so chu
     *  de tu vung da hoc HET tu (xem MockData.lessonsCompletedCount). */
    val completedGrammarLessonIds: Set<String> = emptySet()
)
