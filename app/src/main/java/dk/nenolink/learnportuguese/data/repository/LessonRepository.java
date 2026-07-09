package dk.nenolink.learnportuguese.data.repository;

import android.content.Context;

import dk.nenolink.learnportuguese.data.model.Dialogue;
import dk.nenolink.learnportuguese.data.model.GrammarNote;
import dk.nenolink.learnportuguese.data.model.Lesson;
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
    private static final String LEVEL_LESSON_ASSET_PATTERN = "levels/level%d/lesson%02d.json";

    private final Context appContext;

    public LessonRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public List<String> loadAllLessonJson() throws IOException {
        return loadAllLessonJson(1);
    }

    public List<String> loadAllLessonJson(int levelId) throws IOException {
        List<String> lessons = new ArrayList<>();
        for (int lessonId = FIRST_LESSON_ID; lessonId <= LAST_LESSON_ID; lessonId++) {
            lessons.add(loadLessonJson(levelId, lessonId));
        }
        return Collections.unmodifiableList(lessons);
    }

    public List<Lesson> loadAllLessons() throws IOException {
        return loadAllLessons(1);
    }

    public List<Lesson> loadAllLessons(int levelId) throws IOException {
        List<Lesson> lessons = new ArrayList<>();
        for (int lessonId = FIRST_LESSON_ID; lessonId <= LAST_LESSON_ID; lessonId++) {
            lessons.add(loadLesson(levelId, lessonId));
        }
        return Collections.unmodifiableList(lessons);
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

    public String getLessonAssetPath(int lessonId) {
        return String.format(java.util.Locale.US, LESSON_ASSET_PATTERN, lessonId);
    }

    public String loadLegacyLessonJson(int lessonId) throws IOException {
        return readAsset(getLessonAssetPath(lessonId));
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

    private Lesson parseLesson(JSONObject json) throws JSONException {
        return new Lesson(
                json.optInt("id"),
                json.optString("titleDa"),
                json.optString("descriptionDa"),
                parseDialogues(json.optJSONArray("dialogues")),
                parseQuizQuestions(json.optJSONArray("quiz"))
        );
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
                    json.optString("explanationDa")
            ));
        }
        return grammar;
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
