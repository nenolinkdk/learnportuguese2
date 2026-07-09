# Lesson JSON

This folder contains the offline lesson data used by the app.

`LessonRepository` loads these files from Android assets and parses them into lesson model objects.

The current app UI reads lesson, dialogue, phrase and quiz data through the repository layer.

Expected naming:

```text
lesson01.json
lesson02.json
...
lesson10.json
```

Current content:

- 10 lesson files
- 10 dialogue entries per lesson in the app UI
- 5 quiz questions per lesson
- Danish explanations and questions
- European Portuguese phrase focus

Lesson 1 contains the original longer beginner phrase deck and is split into display dialogues by the UI. Lessons 2-10 already contain 10 explicit dialogue entries each.
