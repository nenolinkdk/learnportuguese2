package dk.nenolink.learnportuguese.data.model;

public class QuizAnswer {
    private final String text;
    private final boolean correct;

    public QuizAnswer(String text, boolean correct) {
        this.text = text;
        this.correct = correct;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correct;
    }
}
