package dk.nenolink.learnportuguese.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuizQuestion {
    private final int id;
    private final String questionDa;
    private final List<QuizAnswer> answers;
    private final String explanationDa;

    public QuizQuestion(int id, String questionDa, List<QuizAnswer> answers, String explanationDa) {
        this.id = id;
        this.questionDa = questionDa;
        this.answers = answers == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(answers));
        this.explanationDa = explanationDa;
    }

    public int getId() {
        return id;
    }

    public String getQuestionDa() {
        return questionDa;
    }

    public List<QuizAnswer> getAnswers() {
        return answers;
    }

    public String getExplanationDa() {
        return explanationDa;
    }
}
