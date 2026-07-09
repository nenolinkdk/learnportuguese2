# JSON Content Structure

Learn Portuguese / Nenoling content should be data-driven. Future levels should be added mainly by adding a new folder under:

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
```

The app discovers available `level*` folders from assets and reads lesson files named `lessonNN.json`.

## Level Metadata

Each level should include `level.json`.

```json
{
  "id": 2,
  "titleDa": "Learn Portuguese 2",
  "subtitleDa": "Dansk → europæisk portugisisk",
  "introDa": "10 offlinelektioner · ser, estar, ter, tal og hverdagssituationer",
  "aiDisclosureDa": "AI brugt: Level 2-dialoger, ordforråd og grammatiknoter",
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
