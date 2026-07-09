package dk.nenolink.learnportuguese2.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserProgress {
    private final int latestLessonId;
    private final int latestDialogueId;
    private final List<String> completedDialogueKeys;
    private final List<Integer> completedLessonIds;
    private final List<QuizResult> quizResults;

    public UserProgress(
            int latestLessonId,
            int latestDialogueId,
            List<String> completedDialogueKeys,
            List<Integer> completedLessonIds,
            List<QuizResult> quizResults
    ) {
        this.latestLessonId = latestLessonId;
        this.latestDialogueId = latestDialogueId;
        this.completedDialogueKeys = completedDialogueKeys == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(completedDialogueKeys));
        this.completedLessonIds = completedLessonIds == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(completedLessonIds));
        this.quizResults = quizResults == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(quizResults));
    }

    public int getLatestLessonId() {
        return latestLessonId;
    }

    public int getLatestDialogueId() {
        return latestDialogueId;
    }

    public List<String> getCompletedDialogueKeys() {
        return completedDialogueKeys;
    }

    public List<Integer> getCompletedLessonIds() {
        return completedLessonIds;
    }

    public List<QuizResult> getQuizResults() {
        return quizResults;
    }
}
