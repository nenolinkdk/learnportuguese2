package dk.nenolink.learnportuguese2.data.model;

public class QuizResult {
    private final int lessonId;
    private final int correctAnswers;
    private final int totalQuestions;
    private final long completedAtMillis;

    public QuizResult(int lessonId, int correctAnswers, int totalQuestions, long completedAtMillis) {
        this.lessonId = lessonId;
        this.correctAnswers = correctAnswers;
        this.totalQuestions = totalQuestions;
        this.completedAtMillis = completedAtMillis;
    }

    public int getLessonId() {
        return lessonId;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public long getCompletedAtMillis() {
        return completedAtMillis;
    }
}
