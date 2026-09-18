package com.education.calmlearn.data.progress

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.education.calmlearn.data.auth.AuthErrorReason
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

/**
 * Implementation THAT cua [ProgressRepository], dung Cloud Firestore, collection rieng
 * [PROGRESS_COLLECTION] (KHONG chung document voi ho so trong "users" - xem firestore.rules,
 * isValidProfile() chi cho phep dung 4 truong co dinh nen khong the them truong tien do vao do).
 *
 * Moi nguoi dung chi co DUNG MOT document (id = uid), giong pattern cua FirebaseAuthRepository.
 */
class FirebaseProgressRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ProgressRepository {

    override suspend fun loadProgress(): ProgressResult {
        val uid = auth.currentUser?.uid ?: return ProgressResult.NotSignedIn
        return try {
            val snapshot = progressDoc(uid).get().await()
            ProgressResult.Loaded(snapshot.toLearningProgress())
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProgressResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ProgressResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun setWordLearned(wordId: String, learned: Boolean): ProgressResult {
        val uid = auth.currentUser?.uid ?: return ProgressResult.NotSignedIn
        return try {
            if (!learned) {
                // Bo danh dau: chi bo khoi danh sach dang "da thuoc". KHONG dung toi xpAwardedWordIds
                // nen sau nay danh dau lai se khong duoc cong XP lan thu hai.
                val update = mapOf(
                    FIELD_LEARNED to FieldValue.arrayRemove(wordId),
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
                progressDoc(uid).set(update, SetOptions.merge()).await()
                return loadProgress()
            }

            val today = todayDateString()
            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(progressDoc(uid))
                val current = snapshot.toLearningProgress()

                val alreadyAwarded = current.xpAwardedWordIds.contains(wordId)
                val newLearned = current.learnedWordIds + wordId
                val newXpAwarded = if (alreadyAwarded) current.xpAwardedWordIds else current.xpAwardedWordIds + wordId
                val award = computeAward(today, current, alreadyAwarded, WORD_LEARNED_XP)

                val data = mapOf(
                    FIELD_LEARNED to newLearned.toList(),
                    FIELD_XP_AWARDED to newXpAwarded.toList(),
                    FIELD_XP to award.xp,
                    FIELD_STREAK to award.streak,
                    FIELD_LAST_ACTIVE to award.lastActiveDate,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
                transaction.set(progressDoc(uid), data, SetOptions.merge())

                current.copy(
                    learnedWordIds = newLearned,
                    xpAwardedWordIds = newXpAwarded,
                    xp = award.xp,
                    streak = award.streak,
                    lastActiveDate = award.lastActiveDate
                )
            }.await()
            ProgressResult.Loaded(result)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProgressResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ProgressResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun recordQuizResult(quizId: String, correctCount: Int, totalQuestions: Int): ProgressResult {
        val uid = auth.currentUser?.uid ?: return ProgressResult.NotSignedIn
        if (totalQuestions <= 0) return loadProgress()
        return try {
            val today = todayDateString()
            val awardKey = "$quizId|$today"
            val accuracyPercent = (correctCount * 100) / totalQuestions
            val xpAmount = correctCount * QUIZ_XP_PER_CORRECT

            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(progressDoc(uid))
                val current = snapshot.toLearningProgress()

                val alreadyAwarded = current.quizXpAwardedKeys.contains(awardKey)
                val newQuizAwarded = if (alreadyAwarded) current.quizXpAwardedKeys else current.quizXpAwardedKeys + awardKey
                val award = computeAward(today, current, alreadyAwarded, xpAmount)
                // Chi tang bo dem "quiz dat >=90%" o LAN DAU TIEN trong ngay (dung chung cong voi
                // XP) - lam lai quiz nhieu lan trong ngay khong lam tang bo dem them.
                val newHighAccuracyCount = if (!alreadyAwarded && accuracyPercent >= HIGH_ACCURACY_THRESHOLD) {
                    current.highAccuracyQuizCount + 1
                } else {
                    current.highAccuracyQuizCount
                }

                val data = mapOf(
                    FIELD_QUIZ_XP_AWARDED to newQuizAwarded.toList(),
                    FIELD_LAST_QUIZ_ACCURACY to accuracyPercent,
                    FIELD_HIGH_ACCURACY_QUIZ_COUNT to newHighAccuracyCount,
                    FIELD_XP to award.xp,
                    FIELD_STREAK to award.streak,
                    FIELD_LAST_ACTIVE to award.lastActiveDate,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
                transaction.set(progressDoc(uid), data, SetOptions.merge())

                current.copy(
                    quizXpAwardedKeys = newQuizAwarded,
                    lastQuizAccuracyPercent = accuracyPercent,
                    highAccuracyQuizCount = newHighAccuracyCount,
                    xp = award.xp,
                    streak = award.streak,
                    lastActiveDate = award.lastActiveDate
                )
            }.await()
            ProgressResult.Loaded(result)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProgressResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ProgressResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun recordSpeakingSessionCompleted(level: String): ProgressResult = recordDailySessionCompleted(
        awardKeyPrefix = level,
        xpAmount = SPEAKING_SESSION_XP,
        countField = FIELD_SPEAKING_SESSION_COUNT,
        awardedKeysField = FIELD_SPEAKING_AWARDED,
        currentAwardedKeys = { it.speakingSessionAwardedKeys },
        currentCount = { it.speakingSessionCount },
        withUpdatedState = { current, newAwardedKeys, newCount ->
            current.copy(speakingSessionAwardedKeys = newAwardedKeys, speakingSessionCount = newCount)
        }
    )

    override suspend fun recordListeningLessonCompleted(lessonId: String): ProgressResult = recordDailySessionCompleted(
        awardKeyPrefix = lessonId,
        xpAmount = LISTENING_SESSION_XP,
        countField = FIELD_LISTENING_SESSION_COUNT,
        awardedKeysField = FIELD_LISTENING_AWARDED,
        currentAwardedKeys = { it.listeningSessionAwardedKeys },
        currentCount = { it.listeningSessionCount },
        withUpdatedState = { current, newAwardedKeys, newCount ->
            current.copy(listeningSessionAwardedKeys = newAwardedKeys, listeningSessionCount = newCount)
        }
    )

    /**
     * Logic dung chung cho "hoan thanh mot luot luyen tap" (Speaking/Listening): cong XP + streak +
     * tang bo dem CHI o lan hoan thanh DAU TIEN trong ngay cua [awardKeyPrefix] (cap do Speaking
     * hoac id bai Listening) - lam lai trong ngay van duoc nhung khong sinh them XP/bo dem.
     */
    private suspend fun recordDailySessionCompleted(
        awardKeyPrefix: String,
        xpAmount: Int,
        countField: String,
        awardedKeysField: String,
        currentAwardedKeys: (LearningProgress) -> Set<String>,
        currentCount: (LearningProgress) -> Int,
        withUpdatedState: (current: LearningProgress, newAwardedKeys: Set<String>, newCount: Int) -> LearningProgress
    ): ProgressResult {
        val uid = auth.currentUser?.uid ?: return ProgressResult.NotSignedIn
        return try {
            val today = todayDateString()
            val awardKey = "$awardKeyPrefix|$today"
            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(progressDoc(uid))
                val current = snapshot.toLearningProgress()

                val awardedKeys = currentAwardedKeys(current)
                val alreadyAwarded = awardedKeys.contains(awardKey)
                val newAwardedKeys = if (alreadyAwarded) awardedKeys else awardedKeys + awardKey
                val newCount = if (alreadyAwarded) currentCount(current) else currentCount(current) + 1
                val award = computeAward(today, current, alreadyAwarded, xpAmount)

                val data = mapOf(
                    awardedKeysField to newAwardedKeys.toList(),
                    countField to newCount,
                    FIELD_XP to award.xp,
                    FIELD_STREAK to award.streak,
                    FIELD_LAST_ACTIVE to award.lastActiveDate,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
                transaction.set(progressDoc(uid), data, SetOptions.merge())

                withUpdatedState(current, newAwardedKeys, newCount).copy(
                    xp = award.xp,
                    streak = award.streak,
                    lastActiveDate = award.lastActiveDate
                )
            }.await()
            ProgressResult.Loaded(result)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProgressResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ProgressResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun addStudySeconds(seconds: Int): ProgressResult {
        val uid = auth.currentUser?.uid ?: return ProgressResult.NotSignedIn
        if (seconds <= 0) return loadProgress()
        return try {
            val today = todayDateString()
            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(progressDoc(uid))
                // toLearningProgress() da tu dong tra ve 0 cho todayStudySeconds neu ngay luu khac
                // hom nay, nen cong don o day luon dung du ngay co the vua sang.
                val current = snapshot.toLearningProgress()
                val newSeconds = current.todayStudySeconds + seconds
                val newTotalSeconds = current.totalStudySeconds + seconds

                val data = mapOf(
                    FIELD_TODAY_STUDY_SECONDS to newSeconds,
                    FIELD_TODAY_STUDY_DATE to today,
                    FIELD_TOTAL_STUDY_SECONDS to newTotalSeconds,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
                transaction.set(progressDoc(uid), data, SetOptions.merge())

                current.copy(todayStudySeconds = newSeconds, todayStudyDate = today, totalStudySeconds = newTotalSeconds)
            }.await()
            ProgressResult.Loaded(result)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProgressResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ProgressResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun recordGrammarLessonCompleted(lessonId: String): ProgressResult {
        val uid = auth.currentUser?.uid ?: return ProgressResult.NotSignedIn
        return try {
            val today = todayDateString()
            val result = firestore.runTransaction { transaction ->
                val snapshot = transaction.get(progressDoc(uid))
                val current = snapshot.toLearningProgress()

                val alreadyAwarded = current.completedGrammarLessonIds.contains(lessonId)
                val newCompleted = if (alreadyAwarded) current.completedGrammarLessonIds else current.completedGrammarLessonIds + lessonId
                val award = computeAward(today, current, alreadyAwarded, GRAMMAR_LESSON_XP)

                val data = mapOf(
                    FIELD_COMPLETED_GRAMMAR to newCompleted.toList(),
                    FIELD_XP to award.xp,
                    FIELD_STREAK to award.streak,
                    FIELD_LAST_ACTIVE to award.lastActiveDate,
                    FIELD_UPDATED_AT to FieldValue.serverTimestamp()
                )
                transaction.set(progressDoc(uid), data, SetOptions.merge())

                current.copy(
                    completedGrammarLessonIds = newCompleted,
                    xp = award.xp,
                    streak = award.streak,
                    lastActiveDate = award.lastActiveDate
                )
            }.await()
            ProgressResult.Loaded(result)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProgressResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ProgressResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    override suspend fun setFavorite(wordId: String, favorite: Boolean): ProgressResult {
        val uid = auth.currentUser?.uid ?: return ProgressResult.NotSignedIn
        return try {
            val update = mapOf(
                FIELD_FAVORITE to if (favorite) FieldValue.arrayUnion(wordId) else FieldValue.arrayRemove(wordId),
                FIELD_UPDATED_AT to FieldValue.serverTimestamp()
            )
            progressDoc(uid).set(update, SetOptions.merge()).await()
            loadProgress()
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (network: FirebaseNetworkException) {
            ProgressResult.ReadError(AuthErrorReason.NETWORK_ERROR)
        } catch (unexpected: Exception) {
            ProgressResult.ReadError(AuthErrorReason.UNKNOWN)
        }
    }

    private fun progressDoc(uid: String) = firestore.collection(PROGRESS_COLLECTION).document(uid)

    private fun DocumentSnapshot.toLearningProgress(): LearningProgress {
        if (!exists()) return LearningProgress()
        return LearningProgress(
            learnedWordIds = stringSet(FIELD_LEARNED),
            favoriteWordIds = stringSet(FIELD_FAVORITE),
            xpAwardedWordIds = stringSet(FIELD_XP_AWARDED),
            quizXpAwardedKeys = stringSet(FIELD_QUIZ_XP_AWARDED),
            xp = (getLong(FIELD_XP) ?: 0L).toInt(),
            streak = (getLong(FIELD_STREAK) ?: 0L).toInt(),
            lastActiveDate = getString(FIELD_LAST_ACTIVE),
            lastQuizAccuracyPercent = getLong(FIELD_LAST_QUIZ_ACCURACY)?.toInt(),
            highAccuracyQuizCount = (getLong(FIELD_HIGH_ACCURACY_QUIZ_COUNT) ?: 0L).toInt(),
            speakingSessionAwardedKeys = stringSet(FIELD_SPEAKING_AWARDED),
            speakingSessionCount = (getLong(FIELD_SPEAKING_SESSION_COUNT) ?: 0L).toInt(),
            listeningSessionAwardedKeys = stringSet(FIELD_LISTENING_AWARDED),
            listeningSessionCount = (getLong(FIELD_LISTENING_SESSION_COUNT) ?: 0L).toInt(),
            // Neu ngay luu trong Firestore khac hom nay, tuc da sang ngay moi ma chua co lan
            // addStudySeconds() nao ghi de - coi nhu 0 giay cho hom nay (khong cong don sang ngay
            // moi tu leftover cua hom qua).
            todayStudySeconds = if (getString(FIELD_TODAY_STUDY_DATE) == todayDateString()) {
                (getLong(FIELD_TODAY_STUDY_SECONDS) ?: 0L).toInt()
            } else {
                0
            },
            todayStudyDate = getString(FIELD_TODAY_STUDY_DATE),
            totalStudySeconds = (getLong(FIELD_TOTAL_STUDY_SECONDS) ?: 0L).toInt(),
            completedGrammarLessonIds = stringSet(FIELD_COMPLETED_GRAMMAR)
        )
    }

    private fun DocumentSnapshot.stringSet(field: String): Set<String> =
        (get(field) as? List<*>)?.filterIsInstance<String>()?.toSet() ?: emptySet()

    private data class XpAward(val xp: Int, val streak: Int, val lastActiveDate: String)

    /**
     * Tinh XP/streak/ngay hoat dong MOI cho mot "hanh dong hoc tap" bat ky (hoc xong 1 tu, hoan
     * thanh 1 quiz...) - dung CHUNG mot khai niem streak cho toan app: ngay nao co IT NHAT MOT
     * hanh dong DAU TIEN (chua tung duoc cong thuong) thi tinh la mot ngay hoat dong.
     */
    private fun computeAward(today: String, current: LearningProgress, alreadyAwarded: Boolean, xpAmount: Int): XpAward {
        return if (alreadyAwarded) {
            XpAward(current.xp, current.streak, current.lastActiveDate ?: today)
        } else {
            XpAward(
                xp = current.xp + xpAmount,
                streak = StreakCalculator.computeStreak(today, current.lastActiveDate, current.streak),
                lastActiveDate = today
            )
        }
    }

    private fun todayDateString(): String = StreakCalculator.todayDateString()

    private companion object {
        const val PROGRESS_COLLECTION = "learningProgress"
        const val FIELD_LEARNED = "learnedWordIds"
        const val FIELD_FAVORITE = "favoriteWordIds"
        const val FIELD_XP_AWARDED = "xpAwardedWordIds"
        const val FIELD_XP = "xp"
        const val FIELD_STREAK = "streak"
        const val FIELD_LAST_ACTIVE = "lastActiveDate"
        const val FIELD_UPDATED_AT = "updatedAt"
        const val FIELD_QUIZ_XP_AWARDED = "quizXpAwardedKeys"
        const val FIELD_LAST_QUIZ_ACCURACY = "lastQuizAccuracyPercent"
        const val FIELD_HIGH_ACCURACY_QUIZ_COUNT = "highAccuracyQuizCount"
        const val FIELD_SPEAKING_AWARDED = "speakingSessionAwardedKeys"
        const val FIELD_SPEAKING_SESSION_COUNT = "speakingSessionCount"
        const val FIELD_LISTENING_AWARDED = "listeningSessionAwardedKeys"
        const val FIELD_LISTENING_SESSION_COUNT = "listeningSessionCount"
        const val FIELD_TODAY_STUDY_SECONDS = "todayStudySeconds"
        const val FIELD_TODAY_STUDY_DATE = "todayStudyDate"
        const val FIELD_TOTAL_STUDY_SECONDS = "totalStudySeconds"
        const val FIELD_COMPLETED_GRAMMAR = "completedGrammarLessonIds"

        /** So XP nguoi dung nhan duoc khi hoc xong MOT tu moi (lan dau tien). */
        const val WORD_LEARNED_XP = 10

        /** So XP cho MOI cau dung trong quiz - khop voi cong thuc hien thi cu (correctCount * 10)
         *  de trai nghiem khong doi, chi khac la gio duoc luu that va co gioi han chong farm. */
        const val QUIZ_XP_PER_CORRECT = 10

        /** Nguong % dung de tinh vao huy hieu "Quiz Master" (xem MockData.applyAchievementProgress). */
        const val HIGH_ACCURACY_THRESHOLD = 90

        /** So XP cho MOT luot luyen phat am / MOT bai luyen nghe hoan thanh (dau tien trong ngay). */
        const val SPEAKING_SESSION_XP = 15
        const val LISTENING_SESSION_XP = 15

        /** So XP cho lan dau tien kiem tra mini-quiz cua mot bai ngu phap. */
        const val GRAMMAR_LESSON_XP = 10
    }
}
