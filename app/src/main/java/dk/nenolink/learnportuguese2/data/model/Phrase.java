package dk.nenolink.learnportuguese2.data.model;

public class Phrase {
    private final String speaker;
    private final String textPt;
    private final String textDa;
    private final String grammarDa;

    public Phrase(String speaker, String textPt, String textDa, String grammarDa) {
        this.speaker = speaker;
        this.textPt = textPt;
        this.textDa = textDa;
        this.grammarDa = grammarDa;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getTextPt() {
        return textPt;
    }

    public String getTextDa() {
        return textDa;
    }

    public String getGrammarDa() {
        return grammarDa;
    }
}
