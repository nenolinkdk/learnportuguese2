package dk.nenolink.learnportuguese2.data.model;

public class GrammarNote {
    private final String titleDa;
    private final String explanationDa;

    public GrammarNote(String titleDa, String explanationDa) {
        this.titleDa = titleDa;
        this.explanationDa = explanationDa;
    }

    public String getTitleDa() {
        return titleDa;
    }

    public String getExplanationDa() {
        return explanationDa;
    }
}
