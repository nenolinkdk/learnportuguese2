package dk.nenolink.learnportuguese2.data.model;

public class NumberEntry {
    private final int number;
    private final String textPt;
    private final String textDa;
    private final String noteDa;

    public NumberEntry(int number, String textPt, String textDa, String noteDa) {
        this.number = number;
        this.textPt = textPt;
        this.textDa = textDa;
        this.noteDa = noteDa;
    }

    public int getNumber() {
        return number;
    }

    public String getTextPt() {
        return textPt;
    }

    public String getTextDa() {
        return textDa;
    }

    public String getNoteDa() {
        return noteDa;
    }
}
