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

        const val WORD_LEARNED_XP = 10

        const val QUIZ_XP_PER_CORRECT = 10

        const val HIGH_ACCURACY_THRESHOLD = 90

        const val SPEAKING_SESSION_XP = 15
        const val LISTENING_SESSION_XP = 15

        const val GRAMMAR_LESSON_XP = 10
    }
}
