package dk.nenolink.learnportuguese2.data.model;

public class Level {
    private final int id;
    private final String titleDa;
    private final String subtitleDa;
    private final String introDa;
    private final String aiDisclosureDa;

    public Level(int id, String titleDa, String subtitleDa, String introDa, String aiDisclosureDa) {
        this.id = id;
        this.titleDa = titleDa;
        this.subtitleDa = subtitleDa;
        this.introDa = introDa;
        this.aiDisclosureDa = aiDisclosureDa;
    }

    public int getId() {
        return id;
    }

    public String getTitleDa() {
        return titleDa;
    }

    public String getSubtitleDa() {
        return subtitleDa;
    }

    public String getIntroDa() {
        return introDa;
    }

    public String getAiDisclosureDa() {
        return aiDisclosureDa;
    }
}
