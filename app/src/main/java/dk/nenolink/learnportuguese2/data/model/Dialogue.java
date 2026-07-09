package dk.nenolink.learnportuguese2.data.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dialogue {
    private final int id;
    private final String titleDa;
    private final String objectiveDa;
    private final List<Phrase> phrases;
    private final List<VocabularyItem> vocabulary;
    private final List<GrammarNote> grammar;

    public Dialogue(
            int id,
            String titleDa,
            String objectiveDa,
            List<Phrase> phrases,
            List<VocabularyItem> vocabulary,
            List<GrammarNote> grammar
    ) {
        this.id = id;
        this.titleDa = titleDa;
        this.objectiveDa = objectiveDa;
        this.phrases = immutableCopy(phrases);
        this.vocabulary = immutableCopy(vocabulary);
        this.grammar = immutableCopy(grammar);
    }

    public int getId() {
        return id;
    }

    public String getTitleDa() {
        return titleDa;
    }

    public String getObjectiveDa() {
        return objectiveDa;
    }

    public List<Phrase> getPhrases() {
        return phrases;
    }

    public List<VocabularyItem> getVocabulary() {
        return vocabulary;
    }

    public List<GrammarNote> getGrammar() {
        return grammar;
    }

    private static <T> List<T> immutableCopy(List<T> items) {
        if (items == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(new ArrayList<>(items));
    }
}
