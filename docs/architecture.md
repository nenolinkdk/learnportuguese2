# Learn Portuguese 2 Architecture

## Overview

Learn Portuguese 2 is a native Android Java app with a data-driven lesson system. The app keeps the same screen structure, navigation, progress tracking, quiz flow, and TextToSpeech behavior across niveauer. Language-learning content should live in JSON assets, not in Java or XML.

The app currently supports:

- Niveau 1
- Niveau 2
- Children
- Future niveauer added as new JSON folders

## JSON Niveau System

Niveauer are stored under:

```text
app/src/main/assets/levels/
```

The app discovers available niveau folders by scanning folders named:

```text
level1/
level2/
level3/
```

Each niveau folder should contain:

```text
level.json
lesson01.json
lesson02.json
lesson03.json
...
```

`level.json` contains niveau metadata:

```json
{
  "id": 2,
  "titleDa": "Learn Portuguese 2",
  "subtitleDa": "Dansk → europæisk portugisisk",
  "introDa": "10 offlinelektioner · ser, estar, ter, tal og hverdagssituationer",
  "aiDisclosureDa": "AI brugt: Niveau 2-dialoger, ordforråd og grammatiknoter",
  "contentVersion": 1
}
```

## Content Locations

Niveau 1 content:

```text
app/src/main/assets/levels/level1/
```

Niveau 2 content:

```text
app/src/main/assets/levels/level2/
```

Children content:

```text
app/src/main/assets/levels/level3/
```

Legacy Niveau 1 content is also available under:

```text
app/src/main/assets/lessons/
```

That legacy folder is kept as a fallback for Niveau 1 compatibility. New work should use `app/src/main/assets/levels/`.

## Adding Future Niveauer

To add Niveau 4 or later niveauer, add a new folder:

```text
app/src/main/assets/levels/levelN/
```

Then add:

```text
level.json
lesson01.json
lesson02.json
...
```

No new Java class should be needed when the new niveau uses the existing JSON structure. The app discovers the new folder and renders it with the same screens, navigation, TTS, progress, vocabulary, grammar, and quiz logic.

## Loader And Model Classes

Main loader:

```text
app/src/main/java/dk/nenolink/learnportuguese2/data/repository/LessonRepository.java
```

Responsibilities:

- Finds available `levels/level*` folders.
- Loads `level.json`.
- Loads `lessonNN.json`.
- Parses lessons, dialogs, phrases, vocabulary, grammar, and quiz data.
- Keeps a legacy fallback for Niveau 1 lesson files under `assets/lessons`.

Data models:

```text
app/src/main/java/dk/nenolink/learnportuguese2/data/model/Level.java
app/src/main/java/dk/nenolink/learnportuguese2/data/model/Lesson.java
app/src/main/java/dk/nenolink/learnportuguese2/data/model/Dialogue.java
app/src/main/java/dk/nenolink/learnportuguese2/data/model/Phrase.java
app/src/main/java/dk/nenolink/learnportuguese2/data/model/VocabularyItem.java
app/src/main/java/dk/nenolink/learnportuguese2/data/model/GrammarNote.java
app/src/main/java/dk/nenolink/learnportuguese2/data/model/QuizQuestion.java
app/src/main/java/dk/nenolink/learnportuguese2/data/model/QuizAnswer.java
```

Progress:

```text
app/src/main/java/dk/nenolink/learnportuguese/data/datastore/ProgressRepository.java
```

Responsibilities:

- Stores latest level, lesson, and dialog.
- Stores completed dialogs.
- Stores quiz results.
- Keeps progress keys level-aware, so Niveau 1 and Niveau 2 do not overwrite each other.

UI and navigation:

```text
app/src/main/java/dk/nenolink/learnportuguese/MainActivity.java
```

Responsibilities:

- Builds the existing screens programmatically.
- Shows level selection from loaded JSON metadata.
- Shows lesson list, dialog list, phrase view, quiz, progress, and settings.
- Reuses the same Previous/Next navigation across levels.
- Reuses the same TextToSpeech button across levels.

## Lesson JSON Structure

Each lesson file contains:

```json
{
  "id": 1,
  "titleDa": "At være: ser",
  "descriptionDa": "Træn ser til identitet, nationalitet, arbejde og familie.",
  "dialogues": [],
  "quiz": []
}
```

Each dialog contains:

```json
{
  "id": 1,
  "titleDa": "Nationalitet",
  "objectiveDa": "Træn: Jeg er dansker.",
  "phrases": [
    {
      "speaker": "A",
      "textPt": "Eu sou dinamarquês.",
      "textDa": "Jeg er dansker.",
      "grammarDa": "Sou er jeg-formen af ser."
    }
  ],
  "vocabulary": [
    {
      "textPt": "dinamarquês",
      "textDa": "dansker"
    }
  ],
  "grammar": []
}
```

## Grammar JSON

Grammar explanations, conjugation tables, examples, and notes are represented in each dialog's `grammar` array.

Example:

```json
{
  "titleDa": "ser - bøjning og brug",
  "explanationDa": "Ser bruges om identitet, nationalitet, egenskaber, relationer og mere faste fakta.",
  "conjugation": [
    { "subject": "eu", "form": "sou" },
    { "subject": "tu", "form": "és" },
    { "subject": "ele/ela/você", "form": "é" },
    { "subject": "nós", "form": "somos" },
    { "subject": "vocês", "form": "são" },
    { "subject": "eles/elas", "form": "são" }
  ],
  "examples": [
    { "pt": "Eu sou dinamarquês.", "da": "Jeg er dansker." },
    { "pt": "Ela é professora.", "da": "Hun er lærer." }
  ],
  "notesDa": "Spørg: Er det identitet eller en fast egenskab? Så er ser normalt det rigtige valg."
}
```

`LessonRepository` formats this into the existing grammar panel. That keeps the UI reusable while allowing richer grammar content in JSON.

## Keep In JSON

Keep these content types in JSON:

- Niveau titles and intro/help text
- Lesson titles and descriptions
- Dialog titles and objectives
- Portuguese phrases
- Danish translations
- Vocabulary
- Grammar explanations
- Verb conjugation tables
- Examples
- Notes/help text for grammar
- Numbers vocabulary
- Shopping, cafe, and supermarket vocabulary
- Quiz questions, answers, and explanations

Avoid hardcoding new language-learning content in Java or XML. Java should define behavior and screen flow. JSON should define learning content.

## TTS Reuse

TextToSpeech is handled in `MainActivity`.

The app:

- Initializes TTS only when the user presses the pronunciation button.
- Uses Portuguese from Portugal with `new Locale("pt", "PT")`.
- Speaks the currently displayed Portuguese phrase.
- Reuses the same pronunciation button for all levels.

New levels do not need TTS-specific code. They only need Portuguese phrases in `textPt`.

## Navigation Reuse

Navigation is handled in `MainActivity`.

The app reuses the same flow for every level:

```text
Niveau selector
Lesson list
Dialog list
Phrase screen
Quiz
Progress
Settings
```

Previous/Next behavior is shared across levels:

- If the current dialog has another phrase, Next/Previous moves between phrases.
- At phrase boundaries, navigation moves to adjacent dialogs.
- At the end of a lesson's dialogs, Next moves into the quiz.

New niveauer do not need navigation code when they follow the existing JSON structure.

## Shared UI Status

Lesson overview rows and lesson detail summaries use one shared status format:

```text
10/10 gennemført · 5 klar
```

`gennemført` counts completed dialogs for the current lesson. `klar` means the number of quiz questions available in that lesson JSON (`lesson.quiz.size()` in the Android code). This definition is the same for Niveau 1, Niveau 2, Children, and future niveauer.

Dialog and story screens use one shared breadcrumb format at the bottom of the phrase view:

```text
Frase 1 af 1 · Niveau 3 · Lektion 6 · Dialog 1 · Skal vi lege?
```

All values are derived from the currently loaded JSON objects and current phrase/dialog indexes. Children content must not have a separate screen, parser, status formatter, or navigation path.

## Children Safety Content

Children safety phrases live in JSON under `app/src/main/assets/levels/level3/lesson10.json`. They distinguish ordinary police help from emergency use of 112 in Portugal.

Emergency-number wording was checked against the European Commission page "112 - the EU's emergency phone number", which states that 112 is available everywhere in the EU, free of charge, can contact ambulance/fire/police, Portugal has opted for 112 as its only national emergency number, and 112 operators respond only to real emergencies:

```text
https://digital-strategy.ec.europa.eu/en/policies/112
```

## Release Metadata And External Link

Release metadata is defined in `app/build.gradle`:

- `versionName`
- `versionCode`
- `releaseDate`

The main menu displays these values from Gradle-generated Android metadata/resources. The Nenolink website URL is also defined once in `app/build.gradle` as the generated `nenolink_url` string resource. The user guide opens this URL through a browser intent; the guide itself remains bundled and offline.

## Checklist: Add A New Niveau

1. Create `app/src/main/assets/levels/levelN/`.
2. Add `level.json` with title, subtitle, intro, and disclosure text.
3. Add `lesson01.json`, `lesson02.json`, and so on.
4. Use stable numeric lesson IDs.
5. Add dialogs with stable numeric dialog IDs.
6. Put Portuguese phrases in `textPt`.
7. Put Danish translations in `textDa`.
8. Put vocabulary in `vocabulary`.
9. Put grammar, conjugations, examples, and notes in `grammar`.
10. Add quiz questions if the lesson should have a quiz.
11. Validate every JSON file parses.
12. Build and test in Android Studio.

## Checklist: Test A New Niveau

1. Start the app and confirm the new level appears in the level selector.
2. Open the first and last lesson in the new level.
3. Open the first and last dialog in each tested lesson.
4. Test Previous/Next inside a dialog with one phrase.
5. Test Previous/Next inside a dialog with multiple phrases.
6. Confirm Portuguese text and Danish translation display correctly.
7. Confirm vocabulary appears in the Ordliste panel.
8. Confirm grammar, conjugation, examples, and notes appear in the Grammatik panel.
9. Tap Udtal portugisisk and confirm TTS speaks the current phrase.
10. Complete a quiz and confirm the result is saved.
11. Switch back to Niveau 1 and confirm it still opens.
12. Switch to Niveau 2 and confirm it still opens.
13. Close and reopen the app to confirm latest position/progress is still valid.
