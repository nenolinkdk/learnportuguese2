package dk.nenolink.learnportuguese.data.datastore;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ProgressRepository {
    private static final String PREFS_NAME = "learn_portuguese_progress";
    private static final String KEY_LATEST_LEVEL_ID = "latest_level_id";
    private static final String KEY_LATEST_LESSON_ID = "latest_lesson_id";
    private static final String KEY_LATEST_DIALOGUE_ID = "latest_dialogue_id";
    private static final String KEY_COMPLETED_DIALOGUES = "completed_dialogues";
    private static final String KEY_QUIZ_CORRECT_PREFIX = "quiz_correct_";
    private static final String KEY_QUIZ_TOTAL_PREFIX = "quiz_total_";

    private final SharedPreferences preferences;

    public ProgressRepository(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void saveLatestPosition(int lessonId, int dialogueId) {
        saveLatestPosition(1, lessonId, dialogueId);
    }

    public void saveLatestPosition(int levelId, int lessonId, int dialogueId) {
        preferences.edit()
                .putInt(KEY_LATEST_LEVEL_ID, levelId)
                .putInt(KEY_LATEST_LESSON_ID, lessonId)
                .putInt(KEY_LATEST_DIALOGUE_ID, dialogueId)
                .apply();
    }

    public boolean hasLatestPosition() {
        return getLatestLessonId() > 0 && getLatestDialogueId() > 0;
    }

    public int getLatestLessonId() {
        return preferences.getInt(KEY_LATEST_LESSON_ID, 0);
    }

    public int getLatestLevelId() {
        return preferences.getInt(KEY_LATEST_LEVEL_ID, 1);
    }

    public int getLatestDialogueId() {
        return preferences.getInt(KEY_LATEST_DIALOGUE_ID, 0);
    }

    public void markDialogueCompleted(int lessonId, int dialogueId) {
        markDialogueCompleted(1, lessonId, dialogueId);
    }

    public void markDialogueCompleted(int levelId, int lessonId, int dialogueId) {
        Set<String> completedDialogues = getCompletedDialogueKeys();
        completedDialogues.add(createDialogueKey(levelId, lessonId, dialogueId));
        preferences.edit()
                .putStringSet(KEY_COMPLETED_DIALOGUES, completedDialogues)
                .apply();
    }

    public boolean isDialogueCompleted(int lessonId, int dialogueId) {
        return isDialogueCompleted(1, lessonId, dialogueId);
    }

    public boolean isDialogueCompleted(int levelId, int lessonId, int dialogueId) {
        Set<String> completedDialogues = getCompletedDialogueKeys();
        return completedDialogues.contains(createDialogueKey(levelId, lessonId, dialogueId))
                || (levelId == 1 && completedDialogues.contains(createLegacyDialogueKey(lessonId, dialogueId)));
    }

    public boolean isLessonCompleted(int lessonId, int dialogueCount) {
        return isLessonCompleted(1, lessonId, dialogueCount);
    }

    public boolean isLessonCompleted(int levelId, int lessonId, int dialogueCount) {
        if (dialogueCount <= 0) {
            return false;
        }

        for (int dialogueId = 1; dialogueId <= dialogueCount; dialogueId++) {
            if (!isDialogueCompleted(levelId, lessonId, dialogueId)) {
                return false;
            }
        }
        return true;
    }

    public void saveQuizResult(int lessonId, int correctAnswers, int totalQuestions) {
        saveQuizResult(1, lessonId, correctAnswers, totalQuestions);
    }

    public void saveQuizResult(int levelId, int lessonId, int correctAnswers, int totalQuestions) {
        preferences.edit()
                .putInt(createQuizCorrectKey(levelId, lessonId), correctAnswers)
                .putInt(createQuizTotalKey(levelId, lessonId), totalQuestions)
                .apply();
    }

    public boolean hasQuizResult(int lessonId) {
        return hasQuizResult(1, lessonId);
    }

    public boolean hasQuizResult(int levelId, int lessonId) {
        return preferences.contains(createQuizTotalKey(levelId, lessonId))
                || (levelId == 1 && preferences.contains(KEY_QUIZ_TOTAL_PREFIX + lessonId));
    }

    public int getQuizCorrectAnswers(int lessonId) {
        return getQuizCorrectAnswers(1, lessonId);
    }

    public int getQuizCorrectAnswers(int levelId, int lessonId) {
        if (preferences.contains(createQuizCorrectKey(levelId, lessonId))) {
            return preferences.getInt(createQuizCorrectKey(levelId, lessonId), 0);
        }
        return levelId == 1 ? preferences.getInt(KEY_QUIZ_CORRECT_PREFIX + lessonId, 0) : 0;
    }

    public int getQuizTotalQuestions(int lessonId) {
        return getQuizTotalQuestions(1, lessonId);
    }

    public int getQuizTotalQuestions(int levelId, int lessonId) {
        if (preferences.contains(createQuizTotalKey(levelId, lessonId))) {
            return preferences.getInt(createQuizTotalKey(levelId, lessonId), 0);
        }
        return levelId == 1 ? preferences.getInt(KEY_QUIZ_TOTAL_PREFIX + lessonId, 0) : 0;
    }

    public void clearProgress() {
        preferences.edit().clear().apply();
    }

    private Set<String> getCompletedDialogueKeys() {
        Set<String> storedKeys = preferences.getStringSet(KEY_COMPLETED_DIALOGUES, Collections.emptySet());
        return new HashSet<>(storedKeys);
    }

    private String createDialogueKey(int levelId, int lessonId, int dialogueId) {
        return levelId + ":" + lessonId + ":" + dialogueId;
    }

    private String createLegacyDialogueKey(int lessonId, int dialogueId) {
        return lessonId + ":" + dialogueId;
    }

    private String createQuizCorrectKey(int levelId, int lessonId) {
        return KEY_QUIZ_CORRECT_PREFIX + levelId + "_" + lessonId;
    }

    private String createQuizTotalKey(int levelId, int lessonId) {
        return KEY_QUIZ_TOTAL_PREFIX + levelId + "_" + lessonId;
    }
}
