package dk.nenolink.learnportuguese2.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Lesson {
    private final int id;
    private final String titleDa;
    private final String descriptionDa;
    private final List<Dialogue> dialogues;
    private final List<QuizQuestion> quiz;
    private final String storyTitleDa;
    private final String storyObjectiveDa;
    private final List<Phrase> story;

    public Lesson(
            int id,
            String titleDa,
            String descriptionDa,
            List<Dialogue> dialogues,
            List<QuizQuestion> quiz,
            String storyTitleDa,
            String storyObjectiveDa,
            List<Phrase> story
    ) {
        this.id = id;
        this.titleDa = titleDa;
        this.descriptionDa = descriptionDa;
        this.dialogues = immutableCopy(dialogues);
        this.quiz = immutableCopy(quiz);
        this.storyTitleDa = storyTitleDa;
        this.storyObjectiveDa = storyObjectiveDa;
        this.story = immutableCopy(story);
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

    public String getStoryTitleDa() {
        return storyTitleDa;
    }

    public String getStoryObjectiveDa() {
        return storyObjectiveDa;
    }

    public List<Phrase> getStory() {
        return story;
    }

    private static <T> List<T> immutableCopy(List<T> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
