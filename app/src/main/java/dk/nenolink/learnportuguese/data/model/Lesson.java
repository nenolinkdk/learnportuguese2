package dk.nenolink.learnportuguese.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lesson {
    private final int id;
    private final String titleDa;
    private final String descriptionDa;
    private final List<Dialogue> dialogues;
    private final List<QuizQuestion> quiz;

    public Lesson(
            int id,
            String titleDa,
            String descriptionDa,
            List<Dialogue> dialogues,
            List<QuizQuestion> quiz
    ) {
        this.id = id;
        this.titleDa = titleDa;
        this.descriptionDa = descriptionDa;
        this.dialogues = immutableCopy(dialogues);
        this.quiz = immutableCopy(quiz);
    }

    public int getId() {
        return id;
    }

    public String getTitleDa() {
        return titleDa;
    }

    public String getDescriptionDa() {
        return descriptionDa;
    }

    public List<Dialogue> getDialogues() {
        return dialogues;
    }

    public List<QuizQuestion> getQuiz() {
        return quiz;
    }

    private static <T> List<T> immutableCopy(List<T> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
