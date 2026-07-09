package dk.nenolink.learnportuguese;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import dk.nenolink.learnportuguese.data.datastore.ProgressRepository;
import dk.nenolink.learnportuguese.data.model.Dialogue;
import dk.nenolink.learnportuguese.data.model.GrammarNote;
import dk.nenolink.learnportuguese.data.model.Lesson;
import dk.nenolink.learnportuguese.data.model.QuizAnswer;
import dk.nenolink.learnportuguese.data.model.QuizQuestion;
import dk.nenolink.learnportuguese.data.model.VocabularyItem;
import dk.nenolink.learnportuguese.data.repository.LessonRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final int COLOR_BACKGROUND = 0xFFFFF7EF;
    private static final int COLOR_HEADER = 0xFF173D35;
    private static final int COLOR_BODY = 0xFF33413C;
    private static final int COLOR_MUTED = 0xFF5D665F;
    private static final int COLOR_BADGE = 0xFFE3F4EC;
    private static final int COLOR_BUTTON = 0xFFFFFFFF;
    private static final int COLOR_LESSON_BUTTON = 0xFFE3F4EC;
    private static final int COLOR_BUTTON_TEXT = 0xFF24324A;
    private static final int COLOR_PANEL = 0xFFFFFFFF;
    private static final int COLOR_PANEL_ALT = 0xFFFFF1F5;
    private static final int COLOR_SUCCESS = 0xFF1B7F3A;
    private static final int COLOR_ERROR = 0xFFB3261E;
    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 2;
    private static final int DEFAULT_LEVEL = 2;
    private static final String EMPTY_VOCABULARY = "Ingen ordlistenoter endnu.";
    private static final String EMPTY_GRAMMAR = "Ingen grammatiknote endnu.";

    private List<Lesson> lessons = Collections.emptyList();
    private List<Phrase> phrases = Collections.emptyList();
    private List<QuizQuestion> quizQuestions = Collections.emptyList();
    private Lesson selectedLesson;
    private Dialogue selectedDialogue;
    private ProgressRepository progressRepository;
    private Screen currentScreen = Screen.LESSONS;
    private int index = 0;
    private int quizIndex = 0;
    private int quizCorrectAnswers = 0;
    private int selectedLevel = DEFAULT_LEVEL;
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
        progressRepository = new ProgressRepository(this);
        setContentView(buildLayout());
        loadLessons(selectedLevel);
        showLessonList();
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

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(24), dp(20), dp(24));
        root.setBackgroundColor(COLOR_BACKGROUND);
        scrollView.addView(root, new ScrollView.LayoutParams(
                ScrollView.LayoutParams.MATCH_PARENT,
                ScrollView.LayoutParams.WRAP_CONTENT
        ));

        TextView title = text("Learn Portuguese 2", 28, COLOR_HEADER, true);
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView languageBadge = text("Dansk → europæisk portugisisk", 14, COLOR_BODY, false);
        languageBadge.setGravity(Gravity.CENTER);
        languageBadge.setPadding(dp(10), dp(6), dp(10), dp(6));
        languageBadge.setBackgroundColor(COLOR_PANEL_ALT);
        root.addView(languageBadge, matchWrap());

        TextView aiBadge = text("AI brugt: begynderfraser og grammatiknoter", 13, COLOR_MUTED, false);
        aiBadge.setGravity(Gravity.CENTER);
        aiBadge.setPadding(dp(10), dp(8), dp(10), dp(8));
        aiBadge.setBackgroundColor(COLOR_BADGE);
        root.addView(aiBadge, matchWrap());

        contentRoot = new LinearLayout(this);
        contentRoot.setOrientation(LinearLayout.VERTICAL);
        contentRoot.setGravity(Gravity.CENTER_HORIZONTAL);
        root.addView(contentRoot, matchWrap());

        return scrollView;
    }

    private void showLessonList() {
        currentScreen = Screen.LESSONS;
        selectedLesson = null;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        clearContent();

        TextView heading = text("Vælg lektion", 22, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView intro = text("10 offlinelektioner · dansk til europæisk portugisisk", 15, COLOR_MUTED, false);
        intro.setGravity(Gravity.CENTER);
        contentRoot.addView(intro, matchWrap());

        addLevelControls();

        if (lessons.isEmpty()) {
            TextView empty = text("Ingen lektioner kunne indlæses.", 16, COLOR_MUTED, false);
            empty.setGravity(Gravity.CENTER);
            contentRoot.addView(empty, matchWrap());
            return;
        }

        if (progressRepository.hasLatestPosition() && progressRepository.getLatestLevelId() == selectedLevel) {
            Button continueButton = button("Fortsæt: Niveau "
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

        Button progressButton = button("Se fremskridt");
        progressButton.setOnClickListener(v -> showProgress());
        secondaryActions.addView(progressButton, new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

        Button settingsButton = button("Indstillinger");
        settingsButton.setOnClickListener(v -> showSettings());
        LinearLayout.LayoutParams settingsParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
        settingsParams.setMargins(dp(12), 0, 0, 0);
        secondaryActions.addView(settingsButton, settingsParams);

        TextView progress = text("Gennemførte lektioner: " + getCompletedLessonCount() + " af " + lessons.size(), 14, COLOR_MUTED, false);
        progress.setGravity(Gravity.CENTER);
        contentRoot.addView(progress, matchWrap());

        for (Lesson lesson : lessons) {
            int dialogueCount = getDialoguesForDisplay(lesson).size();
            String status = progressRepository.isLessonCompleted(selectedLevel, lesson.getId(), dialogueCount) ? "✓ " : "";
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

        for (int levelId = MIN_LEVEL; levelId <= MAX_LEVEL; levelId++) {
            Button levelButton = selectedLevel == levelId
                    ? lessonButton("Niveau " + levelId)
                    : button("Niveau " + levelId);
            int targetLevel = levelId;
            levelButton.setOnClickListener(v -> switchLevel(targetLevel));

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            if (levelId > MIN_LEVEL) {
                params.setMargins(dp(12), 0, 0, 0);
            }
            levelRow.addView(levelButton, params);
        }
    }

    private void switchLevel(int levelId) {
        if (levelId == selectedLevel) {
            return;
        }

        selectedLevel = levelId;
        loadLessons(selectedLevel);
        showLessonList();
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

        Button backButton = button("Tilbage til lektioner");
        backButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(backButton, matchWrap());

        Button quizButton = button("Quiz");
        quizButton.setOnClickListener(v -> showQuiz(lesson));
        contentRoot.addView(quizButton, matchWrap());

        for (Dialogue dialogue : dialogues) {
            String status = progressRepository.isDialogueCompleted(selectedLevel, lesson.getId(), dialogue.getId()) ? "✓ " : "";
            Button dialogueButton = button(status + "Dialog " + dialogue.getId() + ": " + dialogue.getTitleDa());
            dialogueButton.setOnClickListener(v -> showDialogue(lesson, dialogue));
            contentRoot.addView(dialogueButton, matchWrap());
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

        Button backButton = button("Tilbage til dialoger");
        backButton.setOnClickListener(v -> showDialogList(selectedLesson));
        contentRoot.addView(backButton, matchWrap());

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

        Button speakButton = button("Udtal portugisisk");
        speakButton.setOnClickListener(v -> speakCurrentPhrase());
        contentRoot.addView(speakButton, matchWrap());

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(16), 0, dp(16));
        contentRoot.addView(row, matchWrap());

        Button previousButton = button("Forrige");
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

    @Override
    public void onBackPressed() {
        if (currentScreen == Screen.SETTINGS) {
            showLessonList();
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

        if (currentScreen == Screen.PHRASES && selectedLesson != null) {
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
        if (phrases.size() > 1 && index > 0) {
            showPhrase(index - 1);
            return;
        }

        showAdjacentDialogue(-1);
    }

    private void moveNext() {
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

    private void showQuiz(Lesson lesson) {
        if (lesson.getQuiz().isEmpty()) {
            Toast.makeText(this, "Quizdata er ikke klar til denne lektion endnu.", Toast.LENGTH_LONG).show();
            return;
        }

        currentScreen = Screen.QUIZ;
        selectedLesson = lesson;
        selectedDialogue = null;
        phrases = Collections.emptyList();
        quizQuestions = lesson.getQuiz();
        quizIndex = 0;
        quizCorrectAnswers = 0;
        showQuizQuestion();
    }

    private void showQuizQuestion() {
        if (selectedLesson == null || quizQuestions.isEmpty()) {
            showLessonList();
            return;
        }

        if (quizIndex >= quizQuestions.size()) {
            showQuizResult();
            return;
        }

        clearContent();

        Button backButton = button("Tilbage til lektion");
        backButton.setOnClickListener(v -> showDialogList(selectedLesson));
        contentRoot.addView(backButton, matchWrap());

        QuizQuestion question = quizQuestions.get(quizIndex);
        TextView heading = text("Quiz · Lektion " + selectedLesson.getId(), 22, COLOR_HEADER, true);
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

        for (QuizAnswer answer : question.getAnswers()) {
            Button answerButton = button(answer.getText());
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

        Button nextButton = button(quizIndex + 1 >= quizQuestions.size() ? "Se resultat" : "Næste spørgsmål");
        nextButton.setOnClickListener(v -> {
            quizIndex++;
            showQuizQuestion();
        });
        contentRoot.addView(nextButton, matchWrap());
    }

    private void showQuizResult() {
        currentScreen = Screen.RESULTS;
        if (selectedLesson == null) {
            showLessonList();
            return;
        }

        progressRepository.saveQuizResult(selectedLevel, selectedLesson.getId(), quizCorrectAnswers, quizQuestions.size());
        clearContent();

        TextView heading = text("Quizresultat", 24, COLOR_HEADER, true);
        heading.setGravity(Gravity.CENTER);
        contentRoot.addView(heading, matchWrap());

        TextView score = panel("Lektion " + selectedLesson.getId()
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

        Button repeatButton = button("Prøv quiz igen");
        repeatButton.setOnClickListener(v -> showQuiz(selectedLesson));
        contentRoot.addView(repeatButton, matchWrap());

        Button lessonButton = button("Tilbage til lektionen");
        lessonButton.setOnClickListener(v -> showDialogList(selectedLesson));
        contentRoot.addView(lessonButton, matchWrap());

        Button homeButton = button("Til lektionslisten");
        homeButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(homeButton, matchWrap());
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

        Button backButton = button("Tilbage til lektioner");
        backButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(backButton, matchWrap());

        for (Lesson lesson : lessons) {
            List<Dialogue> dialogues = getDialoguesForDisplay(lesson);
            int completedDialogues = getCompletedDialogueCount(lesson, dialogues);
            String status = progressRepository.isLessonCompleted(selectedLevel, lesson.getId(), dialogues.size()) ? "✓ " : "";
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

        Button resetButton = button("Nulstil fremskridt");
        resetButton.setTextColor(COLOR_ERROR);
        resetButton.setOnClickListener(v -> confirmResetProgress());
        contentRoot.addView(resetButton, matchWrap());

        Button backButton = button("Tilbage til lektioner");
        backButton.setOnClickListener(v -> showLessonList());
        contentRoot.addView(backButton, matchWrap());
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

    private void loadLessons(int levelId) {
        LessonRepository repository = new LessonRepository(this);
        try {
            List<Lesson> loadedLessons = repository.loadAllLessons(levelId);
            if (!loadedLessons.isEmpty()) {
                lessons = loadedLessons;
            }
        } catch (IOException exception) {
            lessons = createFallbackLessons();
            Toast.makeText(this, "Kunne ikke indlæse lektions-JSON. Bruger indbygget nødlektion.", Toast.LENGTH_LONG).show();
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

        for (dk.nenolink.learnportuguese.data.model.Phrase phrase : dialogue.getPhrases()) {
            String grammar = isEmpty(phrase.getGrammarDa()) ? dialogueGrammar : phrase.getGrammarDa();
            mappedPhrases.add(new Phrase(
                    phrase.getTextPt(),
                    phrase.getTextDa(),
                    grammar,
                    glossary,
                    source
            ));
        }
        return mappedPhrases;
    }

    private List<Dialogue> getDialoguesForDisplay(Lesson lesson) {
        List<Dialogue> sourceDialogues = lesson.getDialogues();
        if (sourceDialogues.size() >= 10) {
            return sourceDialogues;
        }

        if (sourceDialogues.size() == 1 && !sourceDialogues.get(0).getPhrases().isEmpty()) {
            return splitPrototypeDialogue(sourceDialogues.get(0));
        }

        return createPlaceholderDialogues(lesson);
    }

    private List<Dialogue> splitPrototypeDialogue(Dialogue sourceDialogue) {
        List<Dialogue> dialogues = new ArrayList<>();
        List<dk.nenolink.learnportuguese.data.model.Phrase> sourcePhrases = sourceDialogue.getPhrases();
        int phrasesPerDialogue = Math.max(1, (int) Math.ceil(sourcePhrases.size() / 10.0));

        for (int dialogueId = 1; dialogueId <= 10; dialogueId++) {
            int fromIndex = Math.min(sourcePhrases.size(), (dialogueId - 1) * phrasesPerDialogue);
            int toIndex = Math.min(sourcePhrases.size(), fromIndex + phrasesPerDialogue);
            List<dk.nenolink.learnportuguese.data.model.Phrase> phrasesForDialogue =
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
            List<dk.nenolink.learnportuguese.data.model.Phrase> placeholderPhrases = new ArrayList<>();
            placeholderPhrases.add(new dk.nenolink.learnportuguese.data.model.Phrase(
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
                Collections.emptyList()
        ));
        return fallbackLessons;
    }

    private static List<Phrase> createFallbackPhrases() {
        List<Phrase> fallback = new ArrayList<>();
        fallback.add(new Phrase("Olá, bom dia.", "Hej, godmorgen.", "Use bom dia before lunch.", "olá = hej, bom = god, dia = dag", "basic beginner words"));
        fallback.add(new Phrase("Eu sou da Dinamarca.", "Jeg er fra Danmark.", "Eu sou means I am.", "eu = jeg, sou = er, Dinamarca = Danmark", "basic beginner words"));
        fallback.add(new Phrase("Como te chamas?", "Hvad hedder du?", "Questions often keep the same word order as statements.", "como = hvordan, te chamas = hedder du", "basic beginner words"));
        fallback.add(new Phrase("Chamo-me Anna.", "Jeg hedder Anna.", "Chamo-me is a simple way to say my name is.", "chamo-me = jeg hedder", "basic beginner words"));
        fallback.add(new Phrase("Por favor, fala devagar.", "Vær sød at tale langsomt.", "Fala is the informal command for speak.", "por favor = vær sød, devagar = langsomt", "basic beginner words"));
        fallback.add(new Phrase("Não percebo.", "Jeg forstår ikke.", "Não goes before the verb to make a negative sentence.", "não = ikke, percebo = forstår", "basic beginner words"));
        fallback.add(new Phrase("Onde fica a estação?", "Hvor ligger stationen?", "Onde asks where.", "onde = hvor, fica = ligger, estação = station", "basic beginner words"));
        fallback.add(new Phrase("Quero um café.", "Jeg vil gerne have en kaffe.", "Quero means I want and is useful in cafés and shops.", "quero = jeg vil have, um = en, café = kaffe", "basic beginner words"));
        fallback.add(new Phrase("A água é boa.", "Vandet er godt.", "A and o often mean the.", "água = vand, é = er, boa = god", "basic beginner words"));
        fallback.add(new Phrase("Tenho fome.", "Jeg er sulten.", "Portuguese uses have hunger instead of am hungry.", "tenho = jeg har, fome = sult", "basic beginner words"));
        fallback.add(new Phrase("Tenho sede.", "Jeg er tørstig.", "This also uses tenho, meaning I have.", "sede = tørst", "basic beginner words"));
        fallback.add(new Phrase("A conta, por favor.", "Regningen, tak.", "Short polite phrases work well in restaurants.", "conta = regning, por favor = tak", "basic beginner words"));
        fallback.add(new Phrase("Quanto custa?", "Hvad koster det?", "Quanto asks how much.", "quanto = hvor meget, custa = koster", "basic beginner words"));
        fallback.add(new Phrase("Gosto de música.", "Jeg kan lide musik.", "Gosto de means I like.", "gosto de = jeg kan lide, música = musik", "basic beginner words"));
        fallback.add(new Phrase("Moro em Copenhaga.", "Jeg bor i København.", "Em means in or at.", "moro = jeg bor, em = i, Copenhaga = København", "basic beginner words"));
        fallback.add(new Phrase("Hoje está sol.", "I dag er der sol.", "Está describes temporary states like weather.", "hoje = i dag, sol = sol", "basic beginner words"));
        fallback.add(new Phrase("Amanhã vou estudar.", "I morgen vil jeg studere.", "Vou plus verb makes a simple future.", "amanhã = i morgen, vou = jeg går/vil, estudar = studere", "basic beginner words"));
        fallback.add(new Phrase("Preciso de ajuda.", "Jeg har brug for hjælp.", "Preciso de means I need.", "preciso de = jeg har brug for, ajuda = hjælp", "basic beginner words"));
        fallback.add(new Phrase("Até logo.", "Vi ses senere.", "Até means until and is used in goodbyes.", "até logo = vi ses senere", "basic beginner words"));
        fallback.add(new Phrase("Obrigado pela ajuda.", "Tak for hjælpen.", "Obrigado is said by a male speaker; obrigada by a female speaker.", "obrigado = tak, ajuda = hjælp", "basic beginner words"));
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
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextColor(COLOR_BUTTON_TEXT);
        button.setBackgroundColor(COLOR_BUTTON);
        return button;
    }

    private Button lessonButton(String label) {
        Button button = button(label);
        button.setBackgroundColor(COLOR_LESSON_BUTTON);
        return button;
    }

    private LinearLayout.LayoutParams matchWrap() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dp(6), 0, dp(6));
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
        LESSONS,
        DIALOGUES,
        PHRASES,
        QUIZ,
        RESULTS,
        PROGRESS,
        SETTINGS
    }
}
