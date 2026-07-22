# JSON Content Structure

Learn Portuguese / Nenoling content should be data-driven. Future niveauer should be added mainly by adding a new folder under:

```text
app/src/main/assets/levels/
```

Recommended folder pattern:

```text
levels/
  level1/
    level.json
    lesson01.json
    lesson02.json
  level2/
    level.json
    lesson01.json
    lesson02.json
  level3/
    level.json
    lesson01.json
  level4/
    level.json
    lesson01.json
```

The app discovers available `level*` folders from assets and reads lesson files named `lessonNN.json`.

## Shared Adult And Children Schema

Adult niveauer and the children niveau use one shared JSON schema and one shared navigation engine.

- Niveau 1, Niveau 2, børneindhold, and future niveauer all live under `app/src/main/assets/levels/levelN/`.
- Children lessons must use the same `lessonNN.json` structure as adult lessons.
- Children dialogs must use the same `dialogues`, `phrases`, `vocabulary`, `grammar`, `quiz`, and `story` fields.
- The app must not use a special children parser, special children navigation code, or hardcoded children phrases.
- If a lesson or dialog is missing required JSON content, the app should show a clear missing-content message instead of displaying fallback/sample phrases.
- The only intended difference between adult and children levels is the language-learning content itself.

## Niveau Metadata

Each niveau should include `level.json`.

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

## Lesson File

Each `lessonNN.json` should contain:

```json
{
  "id": 1,
  "titleDa": "At være: ser",
  "descriptionDa": "Træn ser til identitet, nationalitet, arbejde og familie.",
  "dialogues": [],
  "quiz": []
}
```

## Dialogues And Phrases

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

## Grammar Notes

Grammar notes can include explanation text, conjugation rows, examples, and help notes. The app parser keeps the same screen structure and formats these fields into the existing grammar panel.

### `ser`

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
    { "pt": "Ela é professora.", "da": "Hun er lærer." },
    { "pt": "Nós somos amigos.", "da": "Vi er venner." }
  ],
  "notesDa": "Spørg: Er det identitet eller en fast egenskab? Så er ser normalt det rigtige valg."
}
```

### `estar`

```json
{
  "titleDa": "estar - bøjning og brug",
  "explanationDa": "Estar bruges om midlertidig tilstand, placering, humør og aktuelle situationer.",
  "conjugation": [
    { "subject": "eu", "form": "estou" },
    { "subject": "tu", "form": "estás" },
    { "subject": "ele/ela/você", "form": "está" },
    { "subject": "nós", "form": "estamos" },
    { "subject": "vocês", "form": "estão" },
    { "subject": "eles/elas", "form": "estão" }
  ],
  "examples": [
    { "pt": "Eu estou bem.", "da": "Jeg har det godt." },
    { "pt": "Ele está em casa.", "da": "Han er hjemme." },
    { "pt": "Estamos no café.", "da": "Vi er på cafeen." }
  ],
  "notesDa": "Spørg: Er det lige nu, en placering eller en midlertidig situation? Så er estar ofte det rigtige valg."
}
```

### `ter`

```json
{
  "titleDa": "ter - bøjning og brug",
  "explanationDa": "Ter betyder at have og bruges også i faste udtryk om alder, sult og tørst.",
  "conjugation": [
    { "subject": "eu", "form": "tenho" },
    { "subject": "tu", "form": "tens" },
    { "subject": "ele/ela/você", "form": "tem" },
    { "subject": "nós", "form": "temos" },
    { "subject": "vocês", "form": "têm" },
    { "subject": "eles/elas", "form": "têm" }
  ],
  "examples": [
    { "pt": "Eu tenho um café.", "da": "Jeg har en kaffe." },
    { "pt": "Ela tem uma pergunta.", "da": "Hun har et spørgsmål." },
    { "pt": "Nós temos tempo.", "da": "Vi har tid." }
  ],
  "notesDa": "På portugisisk bruger man ter i flere udtryk, hvor dansk bruger at være, for eksempel tenho fome."
}
```

## Quiz

```json
{
  "id": 1,
  "questionDa": "Hvilken sætning betyder: Jeg er dansker?",
  "answers": [
    { "text": "Eu sou dinamarquês.", "correct": true },
    { "text": "Eu estou bem.", "correct": false }
  ],
  "explanationDa": "Sou er jeg-formen af ser."
}
```

## Status And Safety Content

The lesson overview status line is shared across Niveau 1, Niveau 2, Children, and future niveauer:

```text
10/10 gennemført · 5 klar
```

`gennemført` is the number of completed dialogs in the lesson. `klar` is the number of quiz questions available in `quiz` for that same lesson. It must not mean different things in different levels.

Children safety content is stored in JSON, currently in `app/src/main/assets/levels/level3/lesson10.json`. It includes ordinary police-help phrases and emergency-only 112 phrases. The emergency wording was checked against the European Commission page "112 - the EU's emergency phone number":

```text
https://digital-strategy.ec.europa.eu/en/policies/112
```

## Content Rules

- Put level titles and level intro text in `level.json`.
- Put lesson titles and lesson descriptions in `lessonNN.json`.
- Put dialog titles and objectives in `dialogues`.
- Put Portuguese phrases in `textPt`.
- Put Danish translations in `textDa`.
- Put vocabulary in `vocabulary`.
- Put grammar help, conjugations, examples, and notes in `grammar`.
- Keep UI behavior in Java, but keep language-learning content in JSON.
- Add new future levels as new `levels/levelN/` folders rather than duplicating Java code.

## Learn Portuguese 3 Grammar And Reading Content

Learn Portuguese 3 continues the shared JSON schema. Since `level3` is Children, the Learn Portuguese 3 product package is stored as `levels/level4` and declares `productLevel: 3` in `level.json`.

Each Learn Portuguese 3 lesson must include:

- exactly 10 dialog objects
- exactly 10 original phrase entries per dialog, with `textPt` and `textDa`
- vocabulary arrays inside dialogs
- grammar notes in JSON with `titleDa`, `explanationDa`, `conjugation`, `examples`, `notesDa` and `commonMistakes`
- one `story` array with approximately 20 reading lines
- quiz questions derived from lesson JSON content: vocabulary, grammar, conjugation, numbers, sentence completion and translation

Each Learn Portuguese 3 dialog should read as a small coherent exchange, not as isolated sentence drills. Avoid repeating the same sentence frame across dialogs. Vary subjects, questions, requests, answers, polite forms and practical problem-solving language. Repeated fallback/helper phrases such as `Obrigado.`, `Muito bem.` or `Repete devagar.` must never be used as lesson dialog content.

The current Learn Portuguese 3 theme set is:

- Biblioteca
- Café e pastelaria
- Notícias e informação
- Câmara municipal
- Piscina e centro desportivo
- Roupa para crianças
- Computador e internet
- Alugar uma bicicleta
- Praia
- Táxi e encontrar o caminho

Pharmacy content belongs inside the shared lesson/dialog schema too. It should use cautious, practical language such as asking for a duty pharmacy, asking whether a prescription is needed, asking about suitability for children, reading package instructions and contacting a doctor when relevant. It must be framed as language-learning support, not medical advice.

The optional `commonMistakes` grammar array uses this shape:

```json
{
  "wrong": "Ontem eu falo.",
  "correct": "Ontem eu falei.",
  "noteDa": "Brug datid efter ontem, når handlingen er afsluttet."
}
```

The app displays this through the shared grammar formatter. It must not introduce a separate Learn Portuguese 3 grammar engine.
