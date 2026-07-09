package dk.nenolink.learnportuguese2.data.model;

public class VocabularyItem {
    private final String textPt;
    private final String textDa;

    public VocabularyItem(String textPt, String textDa) {
        this.textPt = textPt;
        this.textDa = textDa;
    }

    public String getTextPt() {
        return textPt;
    }

    public String getTextDa() {
        return textDa;
    }
}
