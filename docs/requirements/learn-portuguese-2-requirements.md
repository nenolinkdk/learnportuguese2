# Learn Portuguese 2 Requirements

## Purpose

Learn Portuguese 2 is the next level in the Android app for learning basic Portuguese. It builds on Learn Portuguese 1 and should reuse the same structure, navigation, and pronunciation features wherever possible.

Main themes:

- Conjugation and use of `ser` and `estar`
- Conjugation and use of `ter`
- More everyday dialogs
- Numbers from 20 to 100
- Shop, supermarket, cafe, restaurant, and transport situations
- Expanded Portuguese vocabulary with Danish translations

Working title: **Learn Portuguese 2**

Possible later branding: **Nenoling Portuguese 2**

## Reuse From Learn Portuguese 1

Reuse as much existing Android code as possible:

- Screen structure
- Lesson and dialog navigation
- Previous/Next logic
- Pronunciation/TextToSpeech button
- JSON loading
- Points/progress, if present
- Layout and basic design

Primary expected changes:

- App name and title
- JSON data
- Level and lesson names
- Icon or splash screen, if wanted later

## Content Structure

Content should live in JSON so the app can be expanded without program logic changes.

Suggested structure:

- 10 lessons
- 20 dialogs per lesson
- Short Portuguese phrases
- Danish translation
- Optional vocabulary per dialog or lesson
- Optional grammar note per phrase

## Lesson Plan

| Lesson | Title | Themes |
| --- | --- | --- |
| 1 | Repetition og korte samtaler | Greetings, how are you, where are you from, name, country, and language |
| 2 | At være: ser | Nationality, work, identity, and family |
| 3 | At være: estar | Mood, place, temporary situations, cafe, home, and town |
| 4 | At have: ter | Things you have, time, money, questions, and answers |
| 5 | Tal 20-100 | Prices, age, phone numbers, and clock time |
| 6 | I butik | I would like, what does it cost, do you have, payment |
| 7 | Supermarked | Food items, quantities, bag, receipt, card, and cash |
| 8 | Cafe og restaurant | Ordering, bill, water, coffee, food, and polite phrases |
| 9 | Hverdag og transport | Bus, train, taxi, directions, times, and appointments |
| 10 | Mini-dialoger og repetition | Mixed situations using `ser`, `estar`, and `ter` |

## Grammar Themes

### `ser`

`ser` is used for more permanent identity, characteristics, nationality, and roles.

Examples:

- `Eu sou dinamarquês.` - Jeg er dansker.
- `Ela é professora.` - Hun er lærer.
- `Nós somos amigos.` - Vi er venner.

### `estar`

`estar` is used for temporary state, location, and situation.

Examples:

- `Eu estou bem.` - Jeg har det godt.
- `Ele está em casa.` - Han er hjemme.
- `Estamos no cafe.` - Vi er på cafeen.

### `ter`

`ter` is used to express having something.

Forms to train:

- `eu tenho`
- `tu tens`
- `ele/ela tem`
- `nós temos`
- `vocês têm`
- `eles/elas têm`

Examples:

- `Eu tenho um cafe.` - Jeg har en kaffe.
- `Ela tem uma pergunta.` - Hun har et spørgsmål.
- `Nós temos tempo.` - Vi har tid.
- `Você tem dinheiro?` - Har du penge?

## JSON Contract

The content JSON should support this structure:

```json
{
  "level": 2,
  "title": "Learn Portuguese 2",
  "lessons": [
    {
      "id": "lesson_01",
      "title": "Ser og estar",
      "dialogs": [
        {
          "id": "dialog_01",
          "title": "How are you?",
          "phrases": [
            {
              "pt": "Eu estou bem.",
              "da": "Jeg har det godt.",
              "note": "estar bruges om midlertidig tilstand"
            },
            {
              "pt": "Eu sou dinamarquês.",
              "da": "Jeg er dansker.",
              "note": "ser bruges om identitet"
            }
          ],
          "vocabulary": [
            {
              "pt": "bem",
              "da": "godt"
            },
            {
              "pt": "dinamarquês",
              "da": "dansker"
            }
          ]
        }
      ]
    }
  ]
}
```

## Functional Requirements

| ID | Requirement | Acceptance Criteria |
| --- | --- | --- |
| F-01 | The app shall show lessons. | The user can view or navigate to the available lessons. |
| F-02 | The app shall show dialogs. | The user can view dialogs for the selected lesson. |
| F-03 | The app shall show Portuguese text. | The Portuguese phrase is visible on screen. |
| F-04 | The app shall show Danish translation. | The Danish translation is visible with the Portuguese phrase. |
| F-05 | The app shall play Portuguese pronunciation using TTS. | The user can press a pronunciation button and hear the current Portuguese phrase. |
| F-06 | TTS shall initialize only when the user presses the pronunciation button. | The app does not start TTS during app startup. |
| F-07 | The app shall support Previous/Next navigation between phrases and dialogs. | The user can move backward and forward without losing context. |
| F-08 | The app shall show vocabulary. | The user can see vocabulary for a dialog or lesson. |
| F-09 | The app shall handle dialogs with one phrase. | Single-phrase dialogs do not break navigation. |
| F-10 | The app shall handle dialogs with multiple phrases. | Multi-phrase dialogs can be completed normally. |
| F-11 | The app shall load content from JSON. | New JSON content can be added without changing app logic. |

## Non-Functional Requirements

| ID | Requirement | Acceptance Criteria |
| --- | --- | --- |
| NF-01 | The app shall be simple to use. | A beginner can start learning without extra instructions. |
| NF-02 | The app shall start quickly. | The app avoids heavy work during startup. |
| NF-03 | The app shall work offline. | Lesson content is available locally. |
| NF-04 | TTS may depend on Android system voice data. | Missing Portuguese TTS voice data does not crash the app. |
| NF-05 | The app shall be easy to expand. | Future levels can reuse the same JSON-driven structure. |
| NF-06 | The app shall be suitable for a later Play Store version. | The app avoids unnecessary permissions and unstable dependencies. |

## Data Rules

- Portuguese text belongs in `pt`.
- Danish translation belongs in `da`.
- Grammar notes belong in `note`.
- Vocabulary belongs in `vocabulary`.
- IDs should be stable and unique inside the level.
- Dialogs should be short enough for mobile screens.
- Phrases should be suitable for TextToSpeech.

## Testing Requirements

Before release, verify:

- The app starts without errors.
- Lessons can be opened.
- First and last dialogs can be shown.
- Previous/Next works at phrase and dialog boundaries.
- Dialogs with one phrase work.
- Dialogs with multiple phrases work.
- The TTS button plays the current Portuguese phrase.
- The app does not crash if Portuguese TTS is unavailable.
- Missing optional `note` values do not crash the app.
- Empty `vocabulary` values do not crash the app.

## Future Expansion

The same structure can later support:

- Learn Portuguese 3-9
- French to Danish
- Danish to Swahili
- Web version
- Play Store version
- Nenoling branding
- More quizzes and points
