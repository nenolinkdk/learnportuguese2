package dk.nenolink.learnportuguese.data.repository;

import android.content.Context;

import dk.nenolink.learnportuguese.data.model.Dialogue;
import dk.nenolink.learnportuguese.data.model.GrammarNote;
import dk.nenolink.learnportuguese.data.model.Level;
import dk.nenolink.learnportuguese.data.model.Lesson;
import dk.nenolink.learnportuguese.data.model.NumberEntry;
import dk.nenolink.learnportuguese.data.model.Phrase;
import dk.nenolink.learnportuguese.data.model.QuizAnswer;
import dk.nenolink.learnportuguese.data.model.QuizQuestion;
import dk.nenolink.learnportuguese.data.model.VocabularyItem;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LessonRepository {
    private static final int FIRST_LESSON_ID = 1;
    private static final int LAST_LESSON_ID = 10;
    private static final String LESSON_ASSET_PATTERN = "lessons/lesson%02d.json";
    private static final String LEVELS_ASSET_PATH = "levels";
    private static final String LEVEL_FOLDER_PATTERN = "level%d";
    private static final String LEVEL_METADATA_PATTERN = "levels/level%d/level.json";
    private static final String LEVEL_LESSON_ASSET_PATTERN = "levels/level%d/lesson%02d.json";
    private static final String NUMBERS_ASSET_PATH = "content/numbers_1_100.json";

    private final Context appContext;

    public LessonRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public List<String> loadAllLessonJson() throws IOException {
        return loadAllLessonJson(1);
    }

    public List<String> loadAllLessonJson(int levelId) throws IOException {
        List<String> lessons = new ArrayList<>();
        for (int lessonId : listLessonIds(levelId)) {
            lessons.add(loadLessonJson(levelId, lessonId));
        }
        return Collections.unmodifiableList(lessons);
    }

    public List<Lesson> loadAllLessons() throws IOException {
        return loadAllLessons(1);
    }

    public List<Lesson> loadAllLessons(int levelId) throws IOException {
        List<Lesson> lessons = new ArrayList<>();
        for (int lessonId : listLessonIds(levelId)) {
            lessons.add(loadLesson(levelId, lessonId));
        }
        return Collections.unmodifiableList(lessons);
    }

    public List<Level> loadAvailableLevels() throws IOException {
        String[] levelFolders = appContext.getAssets().list(LEVELS_ASSET_PATH);
        if (levelFolders == null || levelFolders.length == 0) {
            return Collections.singletonList(createFallbackLevel(1));
        }

        List<Level> levels = new ArrayList<>();
        for (String folder : levelFolders) {
            int levelId = parseLevelId(folder);
            if (levelId > 0) {
                levels.add(loadLevel(levelId));
            }
        }
        Collections.sort(levels, (left, right) -> Integer.compare(left.getId(), right.getId()));
        return Collections.unmodifiableList(levels);
    }

    public Level loadLevel(int levelId) throws IOException {
        String metadataPath = getLevelMetadataPath(levelId);
        try {
            return parseLevel(new JSONObject(readAsset(metadataPath)), levelId);
        } catch (IOException exception) {
            return createFallbackLevel(levelId);
        } catch (JSONException exception) {
            throw new IOException("Invalid level JSON in " + metadataPath, exception);
        }
    }

    public Lesson loadLesson(int lessonId) throws IOException {
        return loadLesson(1, lessonId);
    }

    public Lesson loadLesson(int levelId, int lessonId) throws IOException {
        String assetPath = getLevelLessonAssetPath(levelId, lessonId);
        try {
            return parseLesson(new JSONObject(readLessonAsset(levelId, lessonId)));
        } catch (JSONException exception) {
            throw new IOException("Invalid lesson JSON in " + assetPath, exception);
        }
    }

    public String loadLessonJson(int lessonId) throws IOException {
        return loadLessonJson(1, lessonId);
    }

    public String loadLessonJson(int levelId, int lessonId) throws IOException {
        return readLessonAsset(levelId, lessonId);
    }

    private String readLessonAsset(int levelId, int lessonId) throws IOException {
        String levelAssetPath = getLevelLessonAssetPath(levelId, lessonId);
        try {
            return readAsset(levelAssetPath);
        } catch (IOException exception) {
            if (levelId == 1) {
                return readAsset(getLessonAssetPath(lessonId));
            }
            throw exception;
        }
    }

    public String getLevelLessonAssetPath(int levelId, int lessonId) {
        return String.format(java.util.Locale.US, LEVEL_LESSON_ASSET_PATTERN, levelId, lessonId);
    }

    public String getLevelMetadataPath(int levelId) {
        return String.format(java.util.Locale.US, LEVEL_METADATA_PATTERN, levelId);
    }

    public String getLessonAssetPath(int lessonId) {
        return String.format(java.util.Locale.US, LESSON_ASSET_PATTERN, lessonId);
    }

    public String loadLegacyLessonJson(int lessonId) throws IOException {
        return readAsset(getLessonAssetPath(lessonId));
    }

    public List<NumberEntry> loadNumbers() throws IOException {
        try {
            JSONObject json = new JSONObject(readAsset(NUMBERS_ASSET_PATH));
            return parseNumbers(json.optJSONArray("numbers"));
        } catch (JSONException exception) {
            throw new IOException("Invalid numbers JSON in " + NUMBERS_ASSET_PATH, exception);
        }
    }

    private String readAsset(String assetPath) throws IOException {
        try (InputStream inputStream = appContext.getAssets().open(assetPath);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        }
    }

    private List<Integer> listLessonIds(int levelId) throws IOException {
        String levelPath = getLevelFolderPath(levelId);
        String[] files = appContext.getAssets().list(levelPath);
        List<Integer> lessonIds = new ArrayList<>();
        if (files != null) {
            for (String file : files) {
                int lessonId = parseLessonId(file);
                if (lessonId > 0) {
                    lessonIds.add(lessonId);
                }
            }
        }

        if (lessonIds.isEmpty() && levelId == 1) {
            for (int lessonId = FIRST_LESSON_ID; lessonId <= LAST_LESSON_ID; lessonId++) {
                lessonIds.add(lessonId);
            }
        }

        Collections.sort(lessonIds);
        return lessonIds;
    }

    private String getLevelFolderPath(int levelId) {
        return LEVELS_ASSET_PATH + "/" + String.format(java.util.Locale.US, LEVEL_FOLDER_PATTERN, levelId);
    }

    private int parseLevelId(String folderName) {
        if (folderName == null || !folderName.startsWith("level")) {
            return 0;
        }
        return parsePositiveInt(folderName.substring("level".length()));
    }

    private int parseLessonId(String fileName) {
        if (fileName == null || !fileName.startsWith("lesson") || !fileName.endsWith(".json")) {
            return 0;
        }
        String value = fileName.substring("lesson".length(), fileName.length() - ".json".length());
        return parsePositiveInt(value);
    }

    private int parsePositiveInt(String value) {
        try {
            int parsed = Integer.parseInt(value);
            return parsed > 0 ? parsed : 0;
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    private Level parseLevel(JSONObject json, int fallbackLevelId) {
        return new Level(
                json.optInt("id", fallbackLevelId),
                json.optString("titleDa", "Learn Portuguese " + fallbackLevelId),
                json.optString("subtitleDa", "Dansk til europæisk portugisisk"),
                json.optString("introDa", "Offlinelektioner · dansk til europæisk portugisisk"),
                json.optString("aiDisclosureDa", "AI brugt: begynderfraser og grammatiknoter")
        );
    }

    private Level createFallbackLevel(int levelId) {
        return new Level(
                levelId,
                "Learn Portuguese " + levelId,
                "Dansk til europæisk portugisisk",
                "Offlinelektioner · dansk til europæisk portugisisk",
                "AI brugt: begynderfraser og grammatiknoter"
        );
    }

    private Lesson parseLesson(JSONObject json) throws JSONException {
        return new Lesson(
                json.optInt("id"),
                json.optString("titleDa"),
                json.optString("descriptionDa"),
                parseDialogues(json.optJSONArray("dialogues")),
                parseQuizQuestions(json.optJSONArray("quiz")),
                json.optString("storyTitleDa"),
                json.optString("storyObjectiveDa"),
                parsePhrases(json.optJSONArray("story"))
        );
    }

    private List<NumberEntry> parseNumbers(JSONArray array) throws JSONException {
        if (array == null) {
            return Collections.emptyList();
        }

        List<NumberEntry> numbers = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject json = array.getJSONObject(index);
            numbers.add(new NumberEntry(
                    json.optInt("number"),
                    json.optString("textPt"),
                    json.optString("textDa"),
                    json.optString("noteDa")
            ));
        }
        return Collections.unmodifiableList(numbers);
    }

    private List<Dialogue> parseDialogues(JSONArray array) throws JSONException {
        if (array == null) {
            return Collections.emptyList();
        }

        List<Dialogue> dialogues = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject json = array.getJSONObject(index);
            dialogues.add(new Dialogue(
                    json.optInt("id"),
                    json.optString("titleDa"),
                    json.optString("objectiveDa"),
                    parsePhrases(json.optJSONArray("phrases")),
                    parseVocabulary(json.optJSONArray("vocabulary")),
                    parseGrammarNotes(json.optJSONArray("grammar"))
            ));
        }
        return dialogues;
    }

    private List<Phrase> parsePhrases(JSONArray array) throws JSONException {
        if (array == null) {
            return Collections.emptyList();
        }

        List<Phrase> phrases = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject json = array.getJSONObject(index);
            phrases.add(new Phrase(
                    json.optString("speaker"),
                    json.optString("textPt"),
                    json.optString("textDa"),
                    json.optString("grammarDa")
            ));
        }
        return phrases;
    }

    private List<VocabularyItem> parseVocabulary(JSONArray array) throws JSONException {
        if (array == null) {
            return Collections.emptyList();
        }

        List<VocabularyItem> vocabulary = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject json = array.getJSONObject(index);
            vocabulary.add(new VocabularyItem(
                    json.optString("textPt"),
                    json.optString("textDa")
            ));
        }
        return vocabulary;
    }

    private List<GrammarNote> parseGrammarNotes(JSONArray array) throws JSONException {
        if (array == null) {
            return Collections.emptyList();
        }

        List<GrammarNote> grammar = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject json = array.getJSONObject(index);
            grammar.add(new GrammarNote(
                    json.optString("titleDa"),
                    formatGrammarExplanation(json)
            ));
        }
        return grammar;
    }

    private String formatGrammarExplanation(JSONObject json) throws JSONException {
        StringBuilder builder = new StringBuilder(json.optString("explanationDa"));

        JSONArray conjugation = json.optJSONArray("conjugation");
        if (conjugation != null && conjugation.length() > 0) {
            appendSection(builder, "Bøjning");
            for (int index = 0; index < conjugation.length(); index++) {
                JSONObject row = conjugation.getJSONObject(index);
                appendLine(builder, row.optString("subject") + " = " + row.optString("form"));
            }
        }

        JSONArray examples = json.optJSONArray("examples");
        if (examples != null && examples.length() > 0) {
            appendSection(builder, "Eksempler");
            for (int index = 0; index < examples.length(); index++) {
                JSONObject example = examples.getJSONObject(index);
                appendLine(builder, example.optString("pt") + " = " + example.optString("da"));
            }
        }

        String notes = json.optString("notesDa");
        if (!notes.isEmpty()) {
            appendSection(builder, "Hjælp");
            appendLine(builder, notes);
        }

        return builder.toString();
    }

    private void appendSection(StringBuilder builder, String title) {
        if (builder.length() > 0) {
            builder.append('\n');
        }
        builder.append(title).append(':');
    }

    private void appendLine(StringBuilder builder, String line) {
        if (line == null || line.trim().isEmpty()) {
            return;
        }
        builder.append('\n').append(line);
    }

    private List<QuizQuestion> parseQuizQuestions(JSONArray array) throws JSONException {
        if (array == null) {
            return Collections.emptyList();
        }

        List<QuizQuestion> quiz = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject json = array.getJSONObject(index);
            quiz.add(new QuizQuestion(
                    json.optInt("id"),
                    json.optString("questionDa"),
                    parseQuizAnswers(json.optJSONArray("answers")),
                    json.optString("explanationDa")
            ));
        }
        return quiz;
    }

    private List<QuizAnswer> parseQuizAnswers(JSONArray array) throws JSONException {
        if (array == null) {
            return Collections.emptyList();
        }

        List<QuizAnswer> answers = new ArrayList<>();
        for (int index = 0; index < array.length(); index++) {
            JSONObject json = array.getJSONObject(index);
            answers.add(new QuizAnswer(
                    json.optString("text"),
                    json.optBoolean("correct")
            ));
        }
        return answers;
    }
}
