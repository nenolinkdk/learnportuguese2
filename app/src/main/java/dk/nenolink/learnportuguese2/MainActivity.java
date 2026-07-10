package dk.nenolink.learnportuguese2;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dk.nenolink.learnportuguese2.data.datastore.ProgressRepository;
import dk.nenolink.learnportuguese2.data.model.Dialogue;
import dk.nenolink.learnportuguese2.data.model.GrammarNote;
import dk.nenolink.learnportuguese2.data.model.Level;
import dk.nenolink.learnportuguese2.data.model.Lesson;
import dk.nenolink.learnportuguese2.data.model.NumberEntry;
import dk.nenolink.learnportuguese2.data.model.QuizAnswer;
import dk.nenolink.learnportuguese2.data.model.QuizQuestion;
import dk.nenolink.learnportuguese2.data.model.VocabularyItem;
import dk.nenolink.learnportuguese2.data.repository.LessonRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends Activity {
    private static final String TAG = "LearnPortuguese2";
    private static final int COLOR_BACKGROUND = 0xFFFFF7EF;
    private static final int COLOR_HEADER = 0xFF173D35;
    private static final int COLOR_BODY = 0xFF33413C;
    private static final int COLOR_MUTED = 0xFF5D665F;
    private static final int COLOR_BADGE = 0xFFE3F4EC;
    private static final int COLOR_BUTTON = 0xFFFFFFFF;
    private static final int COLOR_LESSON_BUTTON = 0xFFDDF3EA;
    private static final int COLOR_NAV_BUTTON = 0xFFDDEBFA;
    private static final int COLOR_SETTINGS_BUTTON = 0xFFEDE4F8;
    private static final int COLOR_QUIZ_BUTTON = 0xFFFFE8C7;
    private static final int COLOR_NUMBERS_BUTTON = 0xFFFFDDE5;
    private static final int COLOR_BUTTON_TEXT = 0xFF24324A;
    private static final int COLOR_PANEL = 0xFFFFFFFF;
    private static final int COLOR_PANEL_ALT = 0xFFFFF1F5;
    private static final int COLOR_SUCCESS = 0xFF1B7F3A;
    private static final int COLOR_ERROR = 0xFFB3261E;
    private static final int DEFAULT_LEVEL = 2;
    private static final int NUMBER_QUIZ_SIZE = 10;
    private static final String EMPTY_VOCABULARY = "Ingen ordlistenoter endnu.";
    private static final String EMPTY_GRAMMAR = "Ingen grammatiknote endnu.";
    private static final String FOOTER_TEXT = "\u00a9 Nenolink - Henrik Nielsen";

    private List<Level> levels = Collections.emptyList();
    private List<Lesson> lessons = Collections.emptyList();
    private List<Phrase> phrases = Collections.emptyList();
    private List<QuizQuestion> quizQuestions = Collections.emptyList();
    private List<NumberEntry> numbers = Collections.emptyList();
    private List<NumberEntry> numberQuizQuestions = Collections.emptyList();
    private Lesson selectedLesson;
    private Dialogue selectedDialogue;
    private ProgressRepository progressRepository;
    private Screen currentScreen = Screen.WELCOME;
    private QuizMode quizMode = QuizMode.LESSON;
    private final Random random = new Random();
    private int index = 0;
    private int quizIndex = 0;
    private int quizCorrectAnswers = 0;
    private int numberQuizIndex = 0;
    private int numberQuizCorrectAnswers = 0;
    private int selectedLevel = DEFAULT_LEVEL;
    private Level selectedLevelInfo = createFallbackLevel(DEFAULT_LEVEL);
    private String startupErrorMessage;
    private TextToSpeech textToSpeech;
    private LinearLayout contentRoot;
    private TextView counterView;
    private TextView portugueseView;
    private TextView danishView;
    private TextView grammarView;
    private TextView glossaryView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            progressRepository = new ProgressRepository(this);
            loadLevels();
            setContentView(buildLayout());
            showWelcomeScreen();
            contentRoot.post(() -> loadLessons(selectedLevel));
        } catch (RuntimeException exception) {
            Log.e(TAG, "Startup failed", exception);
            showStartupError(exception);
        }
    }

    private void showStartupError(Throwable exception) {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new WelcomeBackgroundLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        root.setBackgroundColor(COLOR_BACKGROUND);
        scrollView.addView(root);

        TextView title = text("LearnPortuguese2", 26, COLOR_HEADER, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView message = text(
                "Appen kunne ikke starte korrekt.\n\n"
                        + getSafeErrorMessage(exception)
                        + "\n\nPrøv at installere APK'en igen. Hvis fejlen fortsætter, skal JSON- og launcher-konfigurationen kontrolleres.",
                16,
                COLOR_ERROR,
                true
        );
        message.setGravity(Gravity.CENTER);
        message.setPadding(dp(14), dp(12), dp(14), dp(12));
        message.setBackgroundColor(COLOR_PANEL);
        root.addView(message, matchWrap());
        setContentView(scrollView);
    }

    private String getSafeErrorMessage(Throwable exception) {
        if (exception == null) {
            return "Ukendt fejl.";
        }

        String message = exception.getMessage();
        return message == null || message.trim().isEmpty()
                ? exception.getClass().getSimpleName()
                : message;
    }

    private void setupSpeechAndSpeak() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("pt", "PT"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Portugisisk stemme er ikke installeret på denne Android-enhed.", Toast.LENGTH_LONG).show();
                } else {
                    speakCurrentPhrase();
                }
            }
        });
    }

    private View buildLayout() {
        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);

        LinearLayout root = new WelcomeBackgroundLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(28));
        root.setBackgroundColor(COLOR_BACKGROUND);
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView title = text("LearnPortuguese2", 28, COLOR_HEADER, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView languageBadge = text(selectedLevelInfo.getSubtitleDa(), 14, COLOR_BODY, false);
        languageBadge.setGravity(Gravity.CENTER);
        languageBadge.setPadding(dp(10), dp(6), dp(10), dp(6));
        languageBadge.setBackgroundColor(COLOR_PANEL_ALT);
        root.addView(languageBadge, matchWrap());

        TextView aiBadge = text(selectedLevelInfo.getAiDisclosureDa(), 13, COLOR_MUTED, false);
        aiBadge.setGravity(Gravity.CENTER);
        aiBadge.setPadding(dp(10), dp(8), dp(10), dp(8));
        aiBadge.setBackgroundColor(COLOR_BADGE);
        root.addView(aiBadge, matchWrap());

        contentRoot = new LinearLayout(this);
        contentRoot.setOrientation(LinearLayout.VERTICAL);
        contentRoot.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(contentRoot, matchWrap());

        TextView footer = text(FOOTER_TEXT, 12, COLOR_MUTED, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(18), 0, 0);
        root.addView(footer, matchWrap());

        return scrollView;
    }

    private void showWelcomeScreen() {
        currentScreen = Screen.WELCOME;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        clearContent();

        TextView heading = text("Velkommen", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView intro = text("V\u00e6lg niveau, quiz, ordforr\u00e5d eller taltr\u00e6ning.", 16, COLOR_MUTED, false);
        intro.setGravity(Gravity.CENTER);
        contentRoot.addView(intro, matchWrap());

        addStartupErrorPanelIfNeeded();

        for (Level level : levels) {
            Button levelButton = lessonButton(getWelcomeLevelLabel(level));
            int levelId = level.getId();
            levelButton.setOnClickListener(v -> switchLevel(levelId));
            contentRoot.addView(levelButton, matchWrap());
        }

        Button vocabularyQuizButton = quizButton("Quiz: ordforr\u00e5d generelt");
        vocabularyQuizButton.setOnClickListener(v -> showVocabularyQuiz());
        contentRoot.addView(vocabularyQuizButton, matchWrap());

        Button quizButton = quizButton("Quiz");
        quizButton.setOnClickListener(v -> showQuizMenu());
        contentRoot.addView(quizButton, matchWrap());

        Button numbersButton = numbersButton("Numbers / Tal 1-100");
        numbersButton.setOnClickListener(v -> showNumbers());
        contentRoot.addView(numbersButton, matchWrap());

        Button settingsButton = settingsButton("Settings / Info");
        settingsButton.setOnClickListener(v -> showSettings());
        contentRoot.addView(settingsButton, matchWrap());
    }

    private void showLessonList() {
        currentScreen = Screen.LESSONS;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        clearContent();

        TextView heading = text("V\u00e6lg lektion", 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView intro = text(selectedLevelInfo.getIntroDa(), 15, COLOR_MUTED, false);
        intro.setGravity(Gravity.CENTER);
        contentRoot.addView(intro, matchWrap());

        addStartupErrorPanelIfNeeded();

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());

        if (lessons.isEmpty()) {
            TextView empty = text("Ingen lektioner kunne indl\u00e6ses.", 16, COLOR_MUTED, false);
            empty.setGravity(Gravity.CENTER);
            contentRoot.addView(empty, matchWrap());
            return;
        }

        if (progressRepository.hasLatestPosition() && progressRepository.getLatestLevelId() == selectedLevel) {
            Button continueButton = button("Forts\u00e6t: Niveau "
                    + progressRepository.getLatestLevelId()
                    + " - Lektion "
                    + progressRepository.getLatestLessonId()
                    + " - Dialog "
                    + progressRepository.getLatestDialogueId());
            continueButton.setOnClickListener(v -> openLatestPosition());
            contentRoot.addView(continueButton, matchWrap());
        }

        LinearLayout secondaryActions = new LinearLayout(this);
        secondaryActions.setOrientation(LinearLayout.HORIZONTAL);
        secondaryActions.setGravity(Gravity.CENTER);
        contentRoot.addView(secondaryActions, matchWrap());

        Button progressButton = navButton("Se fremskridt");
        progressButton.setOnClickListener(v -> showProgress());
        secondaryActions.addView(progressButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button settingsButton = settingsButton("Indstillinger");
        settingsButton.setOnClickListener(v -> showSettings());
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        settingsParams.setMargins(dp(12), 0, 0, 0);
        secondaryActions.addView(settingsButton, settingsParams);

        TextView progress = text("Gennemf\u00f8rte lektioner: " + getCompletedLessonCount() + " af " + lessons.size(), 14, COLOR_MUTED, false);
        progress.setGravity(Gravity.CENTER);
        contentRoot.addView(progress, matchWrap());

        for (Lesson lesson : lessons) {
            int dialogueCount = getDialoguesForDisplay(lesson).size();
            String status = progressRepository.isLessonCompleted(selectedLevel, lesson.getId(), dialogueCount) ? "[OK] " : "";
            Button lessonButton = lessonButton(status + "Lektion " + lesson.getId() + ": " + lesson.getTitleDa());
            lessonButton.setOnClickListener(v -> showDialogList(lesson));
            contentRoot.addView(lessonButton, matchWrap());
        }
    }

    private void addLevelControls() {
        LinearLayout levelRow = new LinearLayout(this);
        levelRow.setOrientation(LinearLayout.HORIZONTAL);
        levelRow.setGravity(Gravity.CENTER);
        contentRoot.addView(levelRow, matchWrap());

        for (Level level : levels) {
            int levelId = level.getId();
            Button levelButton = selectedLevel == levelId
                    ? lessonButton(level.getTitleDa())
                    : navButton(level.getTitleDa());
            int targetLevel = levelId;
            levelButton.setOnClickListener(v -> switchLevel(targetLevel));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            if (levelId != levels.get(0).getId()) {
                params.setMargins(dp(12), 0, 0, 0);
            }
            levelRow.addView(levelButton, params);
        }
    }

    private void switchLevel(int levelId) {
        selectedLevel = levelId;
        selectedLevelInfo = findLevelInfo(levelId);
        loadLessons(selectedLevel);
        setContentView(buildLayout());
        showLessonList();
    }

    private String getWelcomeLevelLabel(Level level) {
        if (level == null) {
            return "Niveau";
        }

        if ("B\u00f8rn".equalsIgnoreCase(level.getTitleDa())) {
            return "B\u00f8rn";
        }

        return level.getTitleDa();
    }

    private void showDialogList(Lesson lesson) {
        currentScreen = Screen.DIALOGUES;
        selectedLesson = lesson;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        clearContent();

        List<Dialogue> dialogues = getDialoguesForDisplay(lesson);
        int completedDialogues = getCompletedDialogueCount(lesson, dialogues);

        TextView heading = text("Lektion " + lesson.getId() + ": " + lesson.getTitleDa(), 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView description = text(lesson.getDescriptionDa(), 15, COLOR_MUTED, false);
        description.setGravity(Gravity.CENTER);
        contentRoot.addView(description, matchWrap());

        TextView overview = panel("Oversigt\n"
                + dialogues.size() + " dialoger\n"
                + completedDialogues + " gennemført\n"
                + lesson.getQuiz().size() + " quizspørgsmål klar\n"
                + getQuizResultText(lesson));
        contentRoot.addView(overview, matchWrap());

        Button backButton = navButton("Tilbage til lektioner");
        backButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(backButton, matchWrap());

        Button quizButton = quizButton("Quiz");
        quizButton.setOnClickListener(v -> showQuiz(lesson));
        contentRoot.addView(quizButton, matchWrap());

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());

        for (Dialogue dialogue : dialogues) {
            String status = progressRepository.isDialogueCompleted(selectedLevel, lesson.getId(), dialogue.getId()) ? "[OK] " : "";
            Button dialogueButton = button(status + "Dialog " + dialogue.getId() + ": " + dialogue.getTitleDa());
            dialogueButton.setOnClickListener(v -> showDialogue(lesson, dialogue));
            contentRoot.addView(dialogueButton, matchWrap());
        }

        if (!lesson.getStory().isEmpty()) {
            String storyTitle = isEmpty(lesson.getStoryTitleDa()) ? "Kort dialog" : lesson.getStoryTitleDa();
            Button storyButton = navButton(storyTitle);
            storyButton.setOnClickListener(v -> showStory(lesson));
            contentRoot.addView(storyButton, matchWrap());
        }
    }

    private void showDialogue(Lesson lesson, Dialogue dialogue) {
        currentScreen = Screen.PHRASES;
        selectedLesson = lesson;
        selectedDialogue = dialogue;
        progressRepository.saveLatestPosition(selectedLevel, lesson.getId(), dialogue.getId());
        progressRepository.markDialogueCompleted(selectedLevel, lesson.getId(), dialogue.getId());
        phrases = mapDialogueToPhrases(lesson, dialogue);
        if (phrases.isEmpty()) {
            phrases = createFallbackPhrases();
        }

        clearContent();

        Button backButton = navButton("Tilbage til dialoger");
        backButton.setOnClickListener(v -> showDialogList(selectedLesson));
        contentRoot.addView(backButton, matchWrap());

        Button lessonsButton = navButton("Til lektioner");
        lessonsButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(lessonsButton, matchWrap());

        TextView dialogueHeading = text(
                "Lektion " + lesson.getId() + " · Dialog " + dialogue.getId() + " af " + getDialoguesForDisplay(lesson).size(),
                18,
                COLOR_HEADER,
                true
        );
        dialogueHeading.setGravity(Gravity.CENTER);
        contentRoot.addView(dialogueHeading, matchWrap());

        TextView dialogueTitle = text(dialogue.getTitleDa(), 16, COLOR_BODY, false);
        dialogueTitle.setGravity(Gravity.CENTER);
        contentRoot.addView(dialogueTitle, matchWrap());

        if (!isEmpty(dialogue.getObjectiveDa())) {
            TextView objective = text(dialogue.getObjectiveDa(), 14, COLOR_MUTED, false);
            objective.setGravity(Gravity.CENTER);
            contentRoot.addView(objective, matchWrap());
        }

        counterView = text("", 14, COLOR_MUTED, false);
        counterView.setPadding(0, dp(18), 0, dp(8));
        contentRoot.addView(counterView);

        portugueseView = text("", 30, 0xFF101A17, true);
        portugueseView.setGravity(Gravity.CENTER);
        contentRoot.addView(portugueseView, matchWrap());

        danishView = text("", 20, COLOR_BODY, false);
        danishView.setGravity(Gravity.CENTER);
        danishView.setPadding(0, dp(8), 0, dp(18));
        contentRoot.addView(danishView, matchWrap());

        Button speakButton = navButton("Udtal portugisisk");
        speakButton.setOnClickListener(v -> speakCurrentPhrase());
        contentRoot.addView(speakButton, matchWrap());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(16), 0, dp(16));
        contentRoot.addView(row, matchWrap());

        Button previousButton = navButton("Forrige");
        previousButton.setOnClickListener(v -> movePrevious());
        row.addView(previousButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button nextButton = button("Næste");
        nextButton.setOnClickListener(v -> moveNext());
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nextParams.setMargins(dp(12), 0, 0, 0);
        row.addView(nextButton, nextParams);

        grammarView = panel("Grammatik");
        contentRoot.addView(grammarView, matchWrap());

        glossaryView = panel("Ordliste");
        contentRoot.addView(glossaryView, matchWrap());

        showPhrase(0);
    }

    private void showStory(Lesson lesson) {
        if (lesson.getStory().isEmpty()) {
            Toast.makeText(this, "Kort dialog mangler i JSON for denne lektion.", Toast.LENGTH_LONG).show();
            return;
        }

        currentScreen = Screen.STORY;
        selectedLesson = lesson;
        selectedDialogue = null;
        phrases = mapStoryToPhrases(lesson);
        index = 0;
        clearContent();

        Button backButton = navButton("Tilbage til dialoger");
        backButton.setOnClickListener(v -> showDialogList(selectedLesson));
        contentRoot.addView(backButton, matchWrap());

        Button lessonsButton = navButton("Til lektioner");
        lessonsButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(lessonsButton, matchWrap());

        TextView heading = text(isEmpty(lesson.getStoryTitleDa()) ? "Kort dialog" : lesson.getStoryTitleDa(), 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        if (!isEmpty(lesson.getStoryObjectiveDa())) {
            TextView objective = text(lesson.getStoryObjectiveDa(), 14, COLOR_MUTED, false);
            objective.setGravity(Gravity.CENTER);
            contentRoot.addView(objective, matchWrap());
        }

        counterView = text("", 14, COLOR_MUTED, false);
        counterView.setPadding(0, dp(18), 0, dp(8));
        contentRoot.addView(counterView);

        portugueseView = text("", 28, 0xFF101A17, true);
        portugueseView.setGravity(Gravity.CENTER);
        contentRoot.addView(portugueseView, matchWrap());

        danishView = text("", 19, COLOR_BODY, false);
        danishView.setGravity(Gravity.CENTER);
        danishView.setPadding(0, dp(8), 0, dp(18));
        contentRoot.addView(danishView, matchWrap());

        Button speakButton = navButton("Udtal portugisisk");
        speakButton.setOnClickListener(v -> speakCurrentPhrase());
        contentRoot.addView(speakButton, matchWrap());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(16), 0, dp(16));
        contentRoot.addView(row, matchWrap());

        Button previousButton = navButton("Forrige");
        previousButton.setOnClickListener(v -> movePrevious());
        row.addView(previousButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button nextButton = navButton("Næste");
        nextButton.setOnClickListener(v -> moveNext());
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        nextParams.setMargins(dp(12), 0, 0, 0);
        row.addView(nextButton, nextParams);

        grammarView = panel("Noter");
        contentRoot.addView(grammarView, matchWrap());

        glossaryView = panel("Ordliste");
        contentRoot.addView(glossaryView, matchWrap());

        showPhrase(0);
    }

    @Override
    public void onBackPressed() {
        if (currentScreen == Screen.WELCOME) {
            super.onBackPressed();
            return;
        }

        if (currentScreen == Screen.NUMBERS || currentScreen == Screen.QUIZ_MENU) {
            showWelcomeScreen();
            return;
        }

        if (currentScreen == Screen.NUMBER_QUIZ || currentScreen == Screen.NUMBER_RESULTS) {
            showNumbers();
            return;
        }

        if (currentScreen == Screen.SETTINGS) {
            showWelcomeScreen();
            return;
        }

        if (currentScreen == Screen.PROGRESS) {
            showLessonList();
            return;
        }

        if ((currentScreen == Screen.QUIZ || currentScreen == Screen.RESULTS) && selectedLesson != null) {
            showDialogList(selectedLesson);
            return;
        }

        if (currentScreen == Screen.QUIZ || currentScreen == Screen.RESULTS) {
            showQuizMenu();
            return;
        }

        if ((currentScreen == Screen.PHRASES || currentScreen == Screen.STORY) && selectedLesson != null) {
            showDialogList(selectedLesson);
            return;
        }

        if (currentScreen == Screen.DIALOGUES) {
            showLessonList();
            return;
        }

        super.onBackPressed();
    }

    private void showPhrase(int newIndex) {
        if (phrases.isEmpty()) {
            return;
        }

        if (newIndex < 0) {
            index = phrases.size() - 1;
        } else if (newIndex >= phrases.size()) {
            index = 0;
        } else {
            index = newIndex;
        }

        Phrase phrase = phrases.get(index);
        counterView.setText("Frase " + (index + 1) + " af " + phrases.size() + " · " + phrase.source);
        portugueseView.setText(phrase.portuguese);
        danishView.setText(phrase.danish);
        grammarView.setText("Grammatik\n" + phrase.grammar);
        if (isEmptyGlossary(phrase.glossary)) {
            glossaryView.setVisibility(View.GONE);
        } else {
            glossaryView.setVisibility(View.VISIBLE);
            glossaryView.setText("Ordliste\n" + phrase.glossary);
        }
    }

    private void speakCurrentPhrase() {
        if (phrases.isEmpty()) {
            return;
        }

        if (textToSpeech == null) {
            setupSpeechAndSpeak();
            return;
        }

        textToSpeech.speak(phrases.get(index).portuguese, TextToSpeech.QUEUE_FLUSH, null, "phrase-" + index);
    }

    private void movePrevious() {
        if (currentScreen == Screen.STORY) {
            showPhrase(index - 1);
            return;
        }

        if (phrases.size() > 1 && index > 0) {
            showPhrase(index - 1);
            return;
        }

        showAdjacentDialogue(-1);
    }

    private void moveNext() {
        if (currentScreen == Screen.STORY) {
            showPhrase(index + 1);
            return;
        }

        if (phrases.size() > 1 && index < phrases.size() - 1) {
            showPhrase(index + 1);
            return;
        }

        showAdjacentDialogue(1);
    }

    private void showAdjacentDialogue(int offset) {
        if (selectedLesson == null || selectedDialogue == null) {
            return;
        }

        List<Dialogue> dialogues = getDialoguesForDisplay(selectedLesson);
        if (dialogues.isEmpty()) {
            return;
        }

        int currentPosition = 0;
        for (int i = 0; i < dialogues.size(); i++) {
            if (dialogues.get(i).getId() == selectedDialogue.getId()) {
                currentPosition = i;
                break;
            }
        }

        int nextPosition = currentPosition + offset;
        if (offset > 0 && currentPosition == dialogues.size() - 1) {
            showQuiz(selectedLesson);
            return;
        }

        if (nextPosition < 0) {
            nextPosition = dialogues.size() - 1;
        } else if (nextPosition >= dialogues.size()) {
            nextPosition = 0;
        }

        showDialogue(selectedLesson, dialogues.get(nextPosition));
    }

    private void showQuizMenu() {
        currentScreen = Screen.QUIZ_MENU;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        clearContent();

        TextView heading = text("Quiz", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView intro = text("Vælg en blandet quiz for et niveau eller træn tal 1-100.", 15, COLOR_MUTED, false);
        intro.setGravity(Gravity.CENTER);
        contentRoot.addView(intro, matchWrap());

        for (Level level : levels) {
            Button levelQuizButton = quizButton("Quiz - Level " + level.getId());
            int levelId = level.getId();
            levelQuizButton.setOnClickListener(v -> showLevelQuiz(levelId));
            contentRoot.addView(levelQuizButton, matchWrap());
        }

        Button numberQuizButton = numbersButton("Talquiz 1-100");
        numberQuizButton.setOnClickListener(v -> showNumberQuiz());
        contentRoot.addView(numberQuizButton, matchWrap());

        Button lessonsButton = navButton("Til lektioner");
        lessonsButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(lessonsButton, matchWrap());

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());
    }

    private void showLevelQuiz(int levelId) {
        selectedLevel = levelId;
        selectedLevelInfo = findLevelInfo(levelId);
        loadLessons(selectedLevel);

        List<QuizQuestion> allQuestions = new ArrayList<>();
        for (Lesson lesson : lessons) {
            allQuestions.addAll(lesson.getQuiz());
        }

        if (allQuestions.isEmpty()) {
            Toast.makeText(this, "Quizdata er ikke klar til dette niveau endnu.", Toast.LENGTH_LONG).show();
            return;
        }

        currentScreen = Screen.QUIZ;
        quizMode = QuizMode.LEVEL;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        quizQuestions = allQuestions;
        Collections.shuffle(quizQuestions, random);
        quizIndex = 0;
        quizCorrectAnswers = 0;
        showQuizQuestion();
    }

    private void showVocabularyQuiz() {
        List<VocabularyItem> vocabulary = loadGeneralVocabulary();
        if (vocabulary.size() < 4) {
            Toast.makeText(this, "Der er ikke nok ordforr\u00e5d i JSON til en generel quiz endnu.", Toast.LENGTH_LONG).show();
            return;
        }

        Collections.shuffle(vocabulary, random);
        List<QuizQuestion> generatedQuestions = new ArrayList<>();
        int maxQuestions = Math.min(12, vocabulary.size());
        for (int questionIndex = 0; questionIndex < maxQuestions; questionIndex++) {
            VocabularyItem item = vocabulary.get(questionIndex);
            List<QuizAnswer> answers = new ArrayList<>();
            answers.add(new QuizAnswer(item.getTextDa(), true));

            for (VocabularyItem candidate : vocabulary) {
                if (answers.size() >= 4) {
                    break;
                }

                if (!candidate.getTextDa().equals(item.getTextDa())) {
                    answers.add(new QuizAnswer(candidate.getTextDa(), false));
                }
            }

            generatedQuestions.add(new QuizQuestion(
                    questionIndex + 1,
                    "Hvad betyder: " + item.getTextPt() + "?",
                    answers,
                    item.getTextPt() + " = " + item.getTextDa()
            ));
        }

        currentScreen = Screen.QUIZ;
        quizMode = QuizMode.VOCABULARY;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        quizQuestions = generatedQuestions;
        Collections.shuffle(quizQuestions, random);
        quizIndex = 0;
        quizCorrectAnswers = 0;
        showQuizQuestion();
    }

    private List<VocabularyItem> loadGeneralVocabulary() {
        LessonRepository repository = new LessonRepository(this);
        List<VocabularyItem> vocabulary = new ArrayList<>();
        for (Level level : levels) {
            try {
                for (Lesson lesson : repository.loadAllLessons(level.getId())) {
                    for (Dialogue dialogue : lesson.getDialogues()) {
                        for (VocabularyItem item : dialogue.getVocabulary()) {
                            if (!isEmpty(item.getTextPt()) && !isEmpty(item.getTextDa())) {
                                vocabulary.add(item);
                            }
                        }
                    }
                }
            } catch (Exception exception) {
                Log.e(TAG, "Could not load vocabulary for level " + level.getId(), exception);
            }
        }
        return vocabulary;
    }

    private void showQuiz(Lesson lesson) {
        if (lesson.getQuiz().isEmpty()) {
            Toast.makeText(this, "Quizdata er ikke klar til denne lektion endnu.", Toast.LENGTH_LONG).show();
            return;
        }

        currentScreen = Screen.QUIZ;
        quizMode = QuizMode.LESSON;
        selectedLesson = lesson;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        quizQuestions = new ArrayList<>(lesson.getQuiz());
        Collections.shuffle(quizQuestions, random);
        quizIndex = 0;
        quizCorrectAnswers = 0;
        showQuizQuestion();
    }

    private String getQuizHeadingText() {
        if (quizMode == QuizMode.VOCABULARY) {
            return "Quiz - ordforråd";
        }

        if (selectedLesson == null) {
            return "Quiz - Niveau " + selectedLevel;
        }

        return "Quiz - Lektion " + selectedLesson.getId();
    }

    private String getQuizScopeText() {
        if (quizMode == QuizMode.VOCABULARY) {
            return "Ordforråd generelt";
        }

        if (selectedLesson == null) {
            return "Niveau " + selectedLevel;
        }

        return "Lektion " + selectedLesson.getId();
    }

    private void showQuizQuestion() {
        if (quizQuestions.isEmpty()) {
            showLessonList();
            return;
        }

        if (quizIndex >= quizQuestions.size()) {
            showQuizResult();
            return;
        }

        clearContent();

        Button backButton = navButton(selectedLesson == null ? "Til quizmenu" : "Tilbage til lektion");
        backButton.setOnClickListener(v -> {
            if (selectedLesson == null) {
                showQuizMenu();
            } else {
                showDialogList(selectedLesson);
            }
        });
        contentRoot.addView(backButton, matchWrap());

        Button lessonsButton = navButton("Til lektioner");
        lessonsButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(lessonsButton, matchWrap());

        QuizQuestion question = quizQuestions.get(quizIndex);
        String headingText = getQuizHeadingText();
        TextView heading = text(headingText, 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView counter = text("Spørgsmål " + (quizIndex + 1) + " af " + quizQuestions.size(), 14, COLOR_MUTED, false);
        counter.setGravity(Gravity.CENTER);
        contentRoot.addView(counter, matchWrap());

        String questionText = isEmpty(question.getQuestionDa())
                ? "Dette spørgsmål mangler tekst i JSON."
                : question.getQuestionDa();
        TextView questionView = panel(questionText);
        questionView.setTextSize(18);
        contentRoot.addView(questionView, matchWrap());

        if (!isValidQuizQuestion(question)) {
            showInvalidQuizQuestionMessage(question);
            return;
        }

        List<QuizAnswer> answers = new ArrayList<>(question.getAnswers());
        Collections.shuffle(answers, random);
        for (QuizAnswer answer : answers) {
            Button answerButton = quizButton(answer.getText());
            answerButton.setOnClickListener(v -> handleQuizAnswer(answer, question));
            contentRoot.addView(answerButton, matchWrap());
        }
    }

    private boolean isValidQuizQuestion(QuizQuestion question) {
        if (question.getAnswers().isEmpty()) {
            return false;
        }

        for (QuizAnswer answer : question.getAnswers()) {
            if (answer.isCorrect()) {
                return true;
            }
        }
        return false;
    }

    private void showInvalidQuizQuestionMessage(QuizQuestion question) {
        String message = question.getAnswers().isEmpty()
                ? "Dette spørgsmål mangler svarmuligheder i JSON."
                : "Dette spørgsmål mangler et korrekt svar i JSON.";
        TextView invalidQuestion = text(message, 15, COLOR_ERROR, true);
        invalidQuestion.setGravity(Gravity.CENTER);
        contentRoot.addView(invalidQuestion, matchWrap());

        Button skipButton = button("Spring spørgsmål over");
        skipButton.setOnClickListener(v -> {
            quizIndex++;
            showQuizQuestion();
        });
        contentRoot.addView(skipButton, matchWrap());
    }

    private void handleQuizAnswer(QuizAnswer answer, QuizQuestion question) {
        if (answer.isCorrect()) {
            quizCorrectAnswers++;
        }

        clearContent();

        TextView heading = text(answer.isCorrect() ? "Korrekt" : "Ikke helt", 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        String explanation = isEmpty(question.getExplanationDa())
                ? "Der er ingen forklaring til dette spørgsmål endnu."
                : question.getExplanationDa();
        TextView explanationView = panel(explanation);
        contentRoot.addView(explanationView, matchWrap());

        Button nextButton = quizButton(quizIndex + 1 >= quizQuestions.size() ? "Se resultat" : "Næste spørgsmål");
        nextButton.setOnClickListener(v -> {
            quizIndex++;
            showQuizQuestion();
        });
        contentRoot.addView(nextButton, matchWrap());
    }

    private void showQuizResult() {
        currentScreen = Screen.RESULTS;
        if (selectedLesson != null) {
            progressRepository.saveQuizResult(selectedLevel, selectedLesson.getId(), quizCorrectAnswers, quizQuestions.size());
        }
        clearContent();

        TextView heading = text("Quizresultat", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        String quizScope = getQuizScopeText();
        TextView score = panel(quizScope
                + "\nScore: " + quizCorrectAnswers + " af " + quizQuestions.size());
        score.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        score.setTextSize(20);
        contentRoot.addView(score, matchWrap());

        boolean passed = isQuizPassed(quizCorrectAnswers, quizQuestions.size());
        TextView feedback = panel(getQuizFeedback(quizCorrectAnswers, quizQuestions.size()));
        feedback.setTextColor(passed ? COLOR_SUCCESS : COLOR_ERROR);
        feedback.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        feedback.setTextSize(20);
        contentRoot.addView(feedback, matchWrap());

        Button repeatButton = quizButton("Prøv quiz igen");
        repeatButton.setOnClickListener(v -> {
            if (quizMode == QuizMode.VOCABULARY) {
                showVocabularyQuiz();
            } else if (selectedLesson == null) {
                showLevelQuiz(selectedLevel);
            } else {
                showQuiz(selectedLesson);
            }
        });
        contentRoot.addView(repeatButton, matchWrap());

        if (selectedLesson != null) {
            Button lessonButton = navButton("Tilbage til lektionen");
            lessonButton.setOnClickListener(v -> showDialogList(selectedLesson));
            contentRoot.addView(lessonButton, matchWrap());
        }

        Button homeButton = navButton("Til lektionslisten");
        homeButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(homeButton, matchWrap());

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());
    }

    private void showNumbers() {
        currentScreen = Screen.NUMBERS;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        loadNumbers();
        clearContent();

        TextView heading = text("Tal 1-100", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView intro = text("Alle tal ligger i JSON og kan genbruges af fremtidige niveauer.", 15, COLOR_MUTED, false);
        intro.setGravity(Gravity.CENTER);
        contentRoot.addView(intro, matchWrap());

        Button quizButton = numbersButton("Start talquiz");
        quizButton.setOnClickListener(v -> showNumberQuiz());
        contentRoot.addView(quizButton, matchWrap());

        Button lessonsButton = navButton("Til lektioner");
        lessonsButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(lessonsButton, matchWrap());

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());

        StringBuilder numberList = new StringBuilder();
        for (NumberEntry number : numbers) {
            if (numberList.length() > 0) {
                numberList.append('\n');
            }
            numberList
                    .append(number.getNumber())
                    .append(" = ")
                    .append(number.getTextPt())
                    .append(" = ")
                    .append(number.getTextDa());
        }
        contentRoot.addView(panel(numberList.toString()), matchWrap());
    }

    private void showNumberQuiz() {
        loadNumbers();
        if (numbers.isEmpty()) {
            Toast.makeText(this, "Taldata kunne ikke indlæses.", Toast.LENGTH_LONG).show();
            return;
        }

        currentScreen = Screen.NUMBER_QUIZ;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        numberQuizQuestions = new ArrayList<>(numbers);
        Collections.shuffle(numberQuizQuestions, random);
        if (numberQuizQuestions.size() > NUMBER_QUIZ_SIZE) {
            numberQuizQuestions = new ArrayList<>(numberQuizQuestions.subList(0, NUMBER_QUIZ_SIZE));
        }
        numberQuizIndex = 0;
        numberQuizCorrectAnswers = 0;
        showNumberQuizQuestion();
    }

    private void showNumberQuizQuestion() {
        if (numberQuizQuestions.isEmpty()) {
            showNumbers();
            return;
        }

        if (numberQuizIndex >= numberQuizQuestions.size()) {
            showNumberQuizResult();
            return;
        }

        clearContent();

        Button numbersNavButton = navButton("Til taloversigt");
        numbersNavButton.setOnClickListener(v -> showNumbers());
        contentRoot.addView(numbersNavButton, matchWrap());

        Button lessonsButton = navButton("Til lektioner");
        lessonsButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(lessonsButton, matchWrap());

        NumberEntry question = numberQuizQuestions.get(numberQuizIndex);
        TextView heading = text("Talquiz 1-100", 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView counter = text("Spørgsmål " + (numberQuizIndex + 1) + " af " + numberQuizQuestions.size(), 14, COLOR_MUTED, false);
        counter.setGravity(Gravity.CENTER);
        contentRoot.addView(counter, matchWrap());

        TextView questionView = panel("Hvad hedder tallet " + question.getNumber() + " på portugisisk?");
        questionView.setTextSize(18);
        contentRoot.addView(questionView, matchWrap());

        for (NumberEntry option : createNumberOptions(question)) {
            Button answerButton = numbersButton(option.getTextPt());
            answerButton.setOnClickListener(v -> handleNumberAnswer(option, question));
            contentRoot.addView(answerButton, matchWrap());
        }
    }

    private List<NumberEntry> createNumberOptions(NumberEntry correct) {
        List<NumberEntry> options = new ArrayList<>();
        options.add(correct);

        List<NumberEntry> pool = new ArrayList<>(numbers);
        Collections.shuffle(pool, random);
        for (NumberEntry candidate : pool) {
            if (options.size() >= 4) {
                break;
            }
            if (candidate.getNumber() != correct.getNumber()) {
                options.add(candidate);
            }
        }

        Collections.shuffle(options, random);
        return options;
    }

    private void handleNumberAnswer(NumberEntry answer, NumberEntry question) {
        boolean correct = answer.getNumber() == question.getNumber();
        if (correct) {
            numberQuizCorrectAnswers++;
        }

        clearContent();

        TextView heading = text(correct ? "Korrekt" : "Ikke helt", 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView explanation = panel(question.getNumber()
                + " = "
                + question.getTextPt()
                + "\nDansk: "
                + question.getTextDa()
                + "\n"
                + question.getNoteDa());
        contentRoot.addView(explanation, matchWrap());

        Button nextButton = numbersButton(numberQuizIndex + 1 >= numberQuizQuestions.size() ? "Se resultat" : "Næste tal");
        nextButton.setOnClickListener(v -> {
            numberQuizIndex++;
            showNumberQuizQuestion();
        });
        contentRoot.addView(nextButton, matchWrap());
    }

    private void showNumberQuizResult() {
        currentScreen = Screen.NUMBER_RESULTS;
        clearContent();

        TextView heading = text("Talquiz resultat", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView score = panel("Score: " + numberQuizCorrectAnswers + " af " + numberQuizQuestions.size());
        score.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        score.setTextSize(20);
        contentRoot.addView(score, matchWrap());

        Button repeatButton = numbersButton("Prøv talquiz igen");
        repeatButton.setOnClickListener(v -> showNumberQuiz());
        contentRoot.addView(repeatButton, matchWrap());

        Button numbersNavButton = navButton("Til taloversigt");
        numbersNavButton.setOnClickListener(v -> showNumbers());
        contentRoot.addView(numbersNavButton, matchWrap());

        Button lessonsButton = navButton("Til lektioner");
        lessonsButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(lessonsButton, matchWrap());

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());
    }

    private void showProgress() {
        currentScreen = Screen.PROGRESS;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        clearContent();

        TextView heading = text("Fremskridt", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView summary = panel("Gennemførte lektioner: " + getCompletedLessonCount() + " af " + lessons.size()
                + "\nSeneste position: " + getLatestPositionText());
        summary.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        contentRoot.addView(summary, matchWrap());

        Button backButton = navButton("Tilbage til lektioner");
        backButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(backButton, matchWrap());

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());

        for (Lesson lesson : lessons) {
            List<Dialogue> dialogues = getDialoguesForDisplay(lesson);
            int completedDialogues = getCompletedDialogueCount(lesson, dialogues);
            String status = progressRepository.isLessonCompleted(selectedLevel, lesson.getId(), dialogues.size()) ? "[OK] " : "";
            TextView lessonProgress = panel(status + "Lektion " + lesson.getId() + ": " + lesson.getTitleDa()
                    + "\nDialoger: " + completedDialogues + " af " + dialogues.size()
                    + "\n" + getQuizResultText(lesson));
            contentRoot.addView(lessonProgress, matchWrap());
        }
    }

    private void showSettings() {
        currentScreen = Screen.SETTINGS;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        clearContent();

        TextView heading = text("Indstillinger", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView appInfo = panel("Learn Portuguese\n"
                + "Dansk til europæisk portugisisk\n"
                + "Offline Android-app\n"
                + "Data gemmes lokalt på enheden");
        contentRoot.addView(appInfo, matchWrap());

        TextView speechInfo = panel("Udtale\n"
                + "Appen bruger Android Text-to-Speech.\n"
                + "Portugisisk Portugal bruges, hvis stemmen findes på enheden.");
        contentRoot.addView(speechInfo, matchWrap());

        Button resetButton = settingsButton("Nulstil fremskridt");
        resetButton.setTextColor(COLOR_ERROR);
        resetButton.setOnClickListener(v -> confirmResetProgress());
        contentRoot.addView(resetButton, matchWrap());

        Button backButton = navButton("Tilbage til lektioner");
        backButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(backButton, matchWrap());

        Button welcomeButton = navButton("Til velkomst");
        welcomeButton.setOnClickListener(v -> showWelcomeScreen());
        contentRoot.addView(welcomeButton, matchWrap());
    }

    private void confirmResetProgress() {
        new AlertDialog.Builder(this)
                .setTitle("Nulstil fremskridt?")
                .setMessage("Dette sletter seneste position, gennemførte dialoger og quizresultater på denne enhed.")
                .setNegativeButton("Annuller", null)
                .setPositiveButton("Nulstil", (dialog, which) -> {
                    progressRepository.clearProgress();
                    Toast.makeText(this, "Fremskridt er nulstillet.", Toast.LENGTH_LONG).show();
                    showLessonList();
                })
                .show();
    }

    private void openLatestPosition() {
        if (progressRepository.getLatestLevelId() != selectedLevel) {
            selectedLevel = progressRepository.getLatestLevelId();
            selectedLevelInfo = findLevelInfo(selectedLevel);
            loadLessons(selectedLevel);
        }

        Lesson latestLesson = findLessonById(progressRepository.getLatestLessonId());
        if (latestLesson == null) {
            showLessonList();
            return;
        }

        List<Dialogue> dialogues = getDialoguesForDisplay(latestLesson);
        Dialogue latestDialogue = findDialogueById(dialogues, progressRepository.getLatestDialogueId());
        if (latestDialogue == null && !dialogues.isEmpty()) {
            latestDialogue = dialogues.get(0);
        }

        if (latestDialogue == null) {
            showDialogList(latestLesson);
            return;
        }

        showDialogue(latestLesson, latestDialogue);
    }

    private Lesson findLessonById(int lessonId) {
        for (Lesson lesson : lessons) {
            if (lesson.getId() == lessonId) {
                return lesson;
            }
        }
        return null;
    }

    private Dialogue findDialogueById(List<Dialogue> dialogues, int dialogueId) {
        for (Dialogue dialogue : dialogues) {
            if (dialogue.getId() == dialogueId) {
                return dialogue;
            }
        }
        return null;
    }

    private String getQuizResultText(Lesson lesson) {
        if (!progressRepository.hasQuizResult(selectedLevel, lesson.getId())) {
            return "Quizresultat: ikke gennemført";
        }

        return "Quizresultat: "
                + progressRepository.getQuizCorrectAnswers(selectedLevel, lesson.getId())
                + " af "
                + progressRepository.getQuizTotalQuestions(selectedLevel, lesson.getId());
    }

    private String getQuizFeedback(int correctAnswers, int totalQuestions) {
        if (totalQuestions <= 0) {
            return "Ingen quizspørgsmål.";
        }

        if (correctAnswers == totalQuestions) {
            return "Flot klaret.";
        }

        if (correctAnswers >= Math.ceil(totalQuestions * 0.6)) {
            return "Bestået.";
        }

        return "Gentag gerne lektionen.";
    }

    private boolean isQuizPassed(int correctAnswers, int totalQuestions) {
        if (totalQuestions <= 0) {
            return false;
        }

        return correctAnswers >= Math.ceil(totalQuestions * 0.6);
    }

    private String getLatestPositionText() {
        if (!progressRepository.hasLatestPosition()) {
            return "ikke startet";
        }

        return "niveau "
                + progressRepository.getLatestLevelId()
                + ", lektion "
                + progressRepository.getLatestLessonId()
                + ", dialog "
                + progressRepository.getLatestDialogueId();
    }

    private int getCompletedDialogueCount(Lesson lesson, List<Dialogue> dialogues) {
        int completed = 0;
        for (Dialogue dialogue : dialogues) {
            if (progressRepository.isDialogueCompleted(selectedLevel, lesson.getId(), dialogue.getId())) {
                completed++;
            }
        }
        return completed;
    }

    private int getCompletedLessonCount() {
        int completed = 0;
        for (Lesson lesson : lessons) {
            int dialogueCount = getDialoguesForDisplay(lesson).size();
            if (progressRepository.isLessonCompleted(selectedLevel, lesson.getId(), dialogueCount)) {
                completed++;
            }
        }
        return completed;
    }

    private void loadLevels() {
        LessonRepository repository = new LessonRepository(this);
        try {
            levels = repository.loadAvailableLevels();
            startupErrorMessage = null;
        } catch (Exception exception) {
            Log.e(TAG, "Could not load levels", exception);
            startupErrorMessage = "Niveau-JSON kunne ikke indlæses. Appen viser nødindhold.";
            levels = Collections.singletonList(createFallbackLevel(DEFAULT_LEVEL));
        }

        selectedLevelInfo = findLevelInfo(selectedLevel);
        selectedLevel = selectedLevelInfo.getId();
    }

    private Level findLevelInfo(int levelId) {
        for (Level level : levels) {
            if (level.getId() == levelId) {
                return level;
            }
        }

        if (!levels.isEmpty()) {
            return levels.get(levels.size() - 1);
        }

        return createFallbackLevel(levelId);
    }

    private static Level createFallbackLevel(int levelId) {
        return new Level(
                levelId,
                "Learn Portuguese " + levelId,
                "Dansk til europæisk portugisisk",
                "Offlinelektioner - dansk til europæisk portugisisk",
                "AI brugt: begynderfraser og grammatiknoter"
        );
    }

    private void loadLessons(int levelId) {
        LessonRepository repository = new LessonRepository(this);
        try {
            lessons = repository.loadAllLessons(levelId);
        } catch (Exception exception) {
            Log.e(TAG, "Could not load lessons for level " + levelId, exception);
            startupErrorMessage = "Lektions-JSON kunne ikke indlæses. Appen viser nødindhold.";
            lessons = createFallbackLessons();
            showSafeToast("Kunne ikke indlæse lektions-JSON. Bruger indbygget nødlektion.");
        }
    }

    private void loadNumbers() {
        LessonRepository repository = new LessonRepository(this);
        try {
            numbers = repository.loadNumbers();
        } catch (Exception exception) {
            Log.e(TAG, "Could not load numbers", exception);
            numbers = Collections.emptyList();
            startupErrorMessage = "Tal-JSON kunne ikke indlæses.";
        }
    }

    private void addStartupErrorPanelIfNeeded() {
        if (isEmpty(startupErrorMessage)) {
            return;
        }

        TextView errorView = panel("Bemærk\n" + startupErrorMessage);
        errorView.setTextColor(COLOR_ERROR);
        errorView.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        contentRoot.addView(errorView, matchWrap());
    }

    private void showSafeToast(String message) {
        try {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (RuntimeException exception) {
            Log.e(TAG, "Could not show toast", exception);
        }
    }

    private List<Phrase> mapDialogueToPhrases(Lesson lesson, Dialogue dialogue) {
        if (lesson == null || dialogue == null) {
            return Collections.emptyList();
        }

        List<Phrase> mappedPhrases = new ArrayList<>();
        String glossary = formatVocabulary(dialogue.getVocabulary());
        String dialogueGrammar = formatGrammar(dialogue.getGrammar());
        String source = "Niveau " + selectedLevel + " · Lektion " + lesson.getId() + " · Dialog " + dialogue.getId() + " · " + dialogue.getTitleDa();

        for (dk.nenolink.learnportuguese2.data.model.Phrase phrase : dialogue.getPhrases()) {
            mappedPhrases.add(new Phrase(
                    phrase.getTextPt(),
                    phrase.getTextDa(),
                    mergeGrammar(phrase.getGrammarDa(), dialogueGrammar),
                    glossary,
                    source
            ));
        }
        return mappedPhrases;
    }


    private List<Phrase> mapStoryToPhrases(Lesson lesson) {
        if (lesson == null) {
            return Collections.emptyList();
        }

        List<Phrase> mappedPhrases = new ArrayList<>();
        String source = "Niveau " + selectedLevel + " - Lektion " + lesson.getId() + " - Kort dialog";

        for (dk.nenolink.learnportuguese2.data.model.Phrase phrase : lesson.getStory()) {
            mappedPhrases.add(new Phrase(
                    phrase.getTextPt(),
                    phrase.getTextDa(),
                    isEmpty(phrase.getGrammarDa()) ? EMPTY_GRAMMAR : phrase.getGrammarDa(),
                    EMPTY_VOCABULARY,
                    source
            ));
        }
        return mappedPhrases;
    }

    private String mergeGrammar(String phraseGrammar, String dialogueGrammar) {
        if (isEmpty(phraseGrammar)) {
            return dialogueGrammar;
        }

        if (isEmpty(dialogueGrammar) || EMPTY_GRAMMAR.equals(dialogueGrammar)) {
            return phraseGrammar;
        }

        return phraseGrammar + "\n" + dialogueGrammar;
    }

    private List<Dialogue> getDialoguesForDisplay(Lesson lesson) {
        List<Dialogue> sourceDialogues = lesson.getDialogues();
        if (sourceDialogues.size() >= 10) {
            return sourceDialogues;
        }

        if (sourceDialogues.size() == 1 && !sourceDialogues.get(0).getPhrases().isEmpty()) {
            return splitPrototypeDialogue(sourceDialogues.get(0));
        }

        if (!sourceDialogues.isEmpty()) {
            return sourceDialogues;
        }

        return createPlaceholderDialogues(lesson);
    }

    private List<Dialogue> splitPrototypeDialogue(Dialogue sourceDialogue) {
        List<Dialogue> dialogues = new ArrayList<>();
        List<dk.nenolink.learnportuguese2.data.model.Phrase> sourcePhrases = sourceDialogue.getPhrases();
        int phrasesPerDialogue = Math.max(1, (int) Math.ceil(sourcePhrases.size() / 10.0));

        for (int dialogueId = 1; dialogueId <= 10; dialogueId++) {
            int fromIndex = Math.min(sourcePhrases.size(), (dialogueId - 1) * phrasesPerDialogue);
            int toIndex = Math.min(sourcePhrases.size(), fromIndex + phrasesPerDialogue);
            List<dk.nenolink.learnportuguese2.data.model.Phrase> phrasesForDialogue =
                    fromIndex < toIndex ? sourcePhrases.subList(fromIndex, toIndex) : Collections.emptyList();

            dialogues.add(new Dialogue(
                    dialogueId,
                    "Begynderfraser " + dialogueId,
                    sourceDialogue.getObjectiveDa(),
                    phrasesForDialogue,
                    sourceDialogue.getVocabulary(),
                    sourceDialogue.getGrammar()
            ));
        }
        return dialogues;
    }

    private List<Dialogue> createPlaceholderDialogues(Lesson lesson) {
        List<Dialogue> dialogues = new ArrayList<>();
        for (int dialogueId = 1; dialogueId <= 10; dialogueId++) {
            List<dk.nenolink.learnportuguese2.data.model.Phrase> placeholderPhrases = new ArrayList<>();
            placeholderPhrases.add(new dk.nenolink.learnportuguese2.data.model.Phrase(
                    "A",
                    "Conteúdo em preparação.",
                    "Indhold er under forberedelse.",
                    "Denne dialog er en placeholder, indtil det fulde JSON-indhold tilføjes."
            ));

            dialogues.add(new Dialogue(
                    dialogueId,
                    "Dialog " + dialogueId,
                    "Forberedt dialogplads til " + lesson.getTitleDa() + ".",
                    placeholderPhrases,
                    Collections.emptyList(),
                    Collections.emptyList()
            ));
        }
        return dialogues;
    }

    private String formatVocabulary(List<VocabularyItem> vocabulary) {
        if (vocabulary.isEmpty()) {
            return EMPTY_VOCABULARY;
        }

        StringBuilder builder = new StringBuilder();
        for (VocabularyItem item : vocabulary) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(item.getTextPt()).append(" = ").append(item.getTextDa());
        }
        return builder.toString();
    }

    private String formatGrammar(List<GrammarNote> grammar) {
        if (grammar.isEmpty()) {
            return EMPTY_GRAMMAR;
        }

        StringBuilder builder = new StringBuilder();
        for (GrammarNote note : grammar) {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(note.getTitleDa()).append(": ").append(note.getExplanationDa());
        }
        return builder.toString();
    }

    private boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    private boolean isEmptyGlossary(String value) {
        return isEmpty(value) || EMPTY_VOCABULARY.equals(value);
    }

    private void clearContent() {
        contentRoot.removeAllViews();
    }

    private static List<Lesson> createFallbackLessons() {
        List<Dialogue> dialogues = new ArrayList<>();
        dialogues.add(new Dialogue(
                1,
                "Indbyggede fraser",
                "Nøddialog hvis lektions-JSON ikke kan indlæses.",
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList()
        ));

        List<Lesson> fallbackLessons = new ArrayList<>();
        fallbackLessons.add(new Lesson(
                1,
                "Indbygget prototype",
                "Nødlektion hvis offline JSON ikke kan indlæses.",
                dialogues,
                Collections.emptyList(),
                "Kort dialog",
                "Nødindhold hvis JSON ikke kan indlæses.",
                Collections.emptyList()
        ));
        return fallbackLessons;
    }

    private static List<Phrase> createFallbackPhrases() {
        List<Phrase> fallback = new ArrayList<>();
        fallback.add(new Phrase(
                "Conteúdo indisponível.",
                "Indholdet kunne ikke indlæses.",
                "Det rigtige læringsindhold skal ligge i JSON.",
                EMPTY_VOCABULARY,
                "fallback"
        ));
        return fallback;
    }

    private TextView text(String content, int sp, int color, boolean bold) {
        TextView view = new TextView(this);
        view.setText(content);
        view.setTextSize(sp);
        view.setTextColor(color);
        if (bold) {
            view.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return view;
    }

    private TextView panel(String title) {
        TextView view = text(title, 17, COLOR_HEADER, false);
        view.setPadding(dp(14), dp(12), dp(14), dp(12));
        view.setBackgroundColor(COLOR_PANEL);
        return view;
    }

    private Button button(String label) {
        return styledButton(label, COLOR_BUTTON, false);
    }

    private Button navButton(String label) {
        return styledButton(label, COLOR_NAV_BUTTON, false);
    }

    private Button quizButton(String label) {
        return styledButton(label, COLOR_QUIZ_BUTTON, false);
    }

    private Button numbersButton(String label) {
        return styledButton(label, COLOR_NUMBERS_BUTTON, false);
    }

    private Button settingsButton(String label) {
        return styledButton(label, COLOR_SETTINGS_BUTTON, false);
    }

    private Button lessonButton(String label) {
        return styledButton(label, COLOR_LESSON_BUTTON, true);
    }

    private Button styledButton(String label, int backgroundColor, boolean bold) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTextColor(COLOR_BUTTON_TEXT);
        button.setBackgroundColor(backgroundColor);
        button.setMinHeight(dp(42));
        button.setMinWidth(0);
        button.setPadding(dp(10), dp(6), dp(10), dp(6));
        if (bold) {
            button.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
        }
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(8), 0, dp(8));
        return params;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }

    private static class WelcomeBackgroundLayout extends LinearLayout {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Path path = new Path();

        WelcomeBackgroundLayout(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            int width = getWidth();
            int height = getHeight();
            if (width <= 0 || height <= 0) {
                return;
            }

            drawSea(canvas, width, height);
            drawBoat(canvas, width, height);
            drawPalm(canvas, width, height);
        }

        private void drawSea(Canvas canvas, int width, int height) {
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x3327B7C8);
            canvas.drawRect(0, height * 0.62f, width, height, paint);

            paint.setColor(0x22FFFFFF);
            for (int row = 0; row < 4; row++) {
                float y = height * (0.68f + row * 0.07f);
                canvas.drawOval(new RectF(-width * 0.1f, y, width * 0.65f, y + 28f), paint);
                canvas.drawOval(new RectF(width * 0.35f, y + 10f, width * 1.15f, y + 38f), paint);
            }
        }

        private void drawBoat(Canvas canvas, int width, int height) {
            float boatY = height * 0.58f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x336A4A2F);
            path.reset();
            path.moveTo(width * 0.58f, boatY);
            path.lineTo(width * 0.82f, boatY);
            path.lineTo(width * 0.76f, boatY + 34f);
            path.lineTo(width * 0.63f, boatY + 34f);
            path.close();
            canvas.drawPath(path, paint);

            paint.setColor(0x33FFFFFF);
            path.reset();
            path.moveTo(width * 0.68f, boatY - 90f);
            path.lineTo(width * 0.68f, boatY);
            path.lineTo(width * 0.78f, boatY);
            path.close();
            canvas.drawPath(path, paint);
        }

        private void drawPalm(Canvas canvas, int width, int height) {
            float baseX = width * 0.12f;
            float baseY = height * 0.66f;
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(12f);
            paint.setColor(0x33815A34);
            path.reset();
            path.moveTo(baseX, baseY);
            path.quadTo(baseX + 28f, baseY - 120f, baseX + 80f, baseY - 210f);
            canvas.drawPath(path, paint);

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(0x3346A56A);
            float crownX = baseX + 82f;
            float crownY = baseY - 218f;
            for (int i = 0; i < 6; i++) {
                canvas.save();
                canvas.rotate(-70 + i * 28, crownX, crownY);
                canvas.drawOval(new RectF(crownX - 12f, crownY - 12f, crownX + 120f, crownY + 24f), paint);
                canvas.restore();
            }
        }
    }

    private static class Phrase {
        final String portuguese;
        final String danish;
        final String grammar;
        final String glossary;
        final String source;

        Phrase(String portuguese, String danish, String grammar, String glossary, String source) {
            this.portuguese = portuguese;
            this.danish = danish;
            this.grammar = grammar;
            this.glossary = glossary;
            this.source = source;
        }
    }

    private enum Screen {
        WELCOME,
        LESSONS,
        DIALOGUES,
        PHRASES,
        STORY,
        QUIZ_MENU,
        QUIZ,
        RESULTS,
        NUMBERS,
        NUMBER_QUIZ,
        NUMBER_RESULTS,
        PROGRESS,
        SETTINGS
    }

    private enum QuizMode {
        LESSON,
        LEVEL,
        VOCABULARY
    }
}
