param(
    [string]$Root = "."
)

$ErrorActionPreference = "Stop"
$errors = New-Object System.Collections.Generic.List[string]

function Read-Json($Path) {
    Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json
}

function Is-Blank($Value) {
    return [string]::IsNullOrWhiteSpace([string]$Value)
}

function As-Array($Value) {
    if ($null -eq $Value) {
        return ,@()
    }
    return ,@($Value)
}

function Add-Error($Message) {
    $errors.Add($Message)
}

function Test-Phrase($LevelName, $LessonFileName, $Dialog, $Phrase, $PhraseIndex) {
    if (Is-Blank $Phrase.textPt) {
        Add-Error "$LevelName/$LessonFileName dialog $($Dialog.id) phrase $PhraseIndex is missing textPt"
    }
    if (Is-Blank $Phrase.textDa) {
        Add-Error "$LevelName/$LessonFileName dialog $($Dialog.id) phrase $PhraseIndex is missing textDa"
    }
}

function Move-Next($State, $Dialogs) {
    $phrases = As-Array $Dialogs[$State.DialogIndex].phrases
    if ($phrases.Count -gt 1 -and $State.PhraseIndex -lt ($phrases.Count - 1)) {
        return @{ DialogIndex = $State.DialogIndex; PhraseIndex = $State.PhraseIndex + 1 }
    }
    return @{ DialogIndex = (($State.DialogIndex + 1) % $Dialogs.Count); PhraseIndex = 0 }
}

function Move-Previous($State, $Dialogs) {
    $phrases = As-Array $Dialogs[$State.DialogIndex].phrases
    if ($phrases.Count -gt 1 -and $State.PhraseIndex -gt 0) {
        return @{ DialogIndex = $State.DialogIndex; PhraseIndex = $State.PhraseIndex - 1 }
    }

    $previousDialogIndex = ($State.DialogIndex - 1 + $Dialogs.Count) % $Dialogs.Count
    $previousPhrases = As-Array $Dialogs[$previousDialogIndex].phrases
    return @{ DialogIndex = $previousDialogIndex; PhraseIndex = $previousPhrases.Count - 1 }
}

$levelRoot = Join-Path $Root "app/src/main/assets/levels"
$childrenRoot = Join-Path $levelRoot "level3"
$childrenSafetyPath = Join-Path $childrenRoot "lesson10.json"
$buildGradlePath = Join-Path $Root "app/build.gradle"
$guidePath = Join-Path $Root "app/src/main/assets/docs/user_guide.json"
$bannedChildrenFiller = @(
    "Olá. Como estás?",
    "Estou bem.",
    "Obrigado.",
    "Muito bem. Repete devagar.",
    "Repete devagar."
)

$bannedFallbackPhrases = @(
    "Olá.",
    "Como estás?",
    "Estou bem.",
    "Obrigado.",
    "Muito bem.",
    "Repete devagar."
)

foreach ($levelDir in Get-ChildItem -LiteralPath $levelRoot -Directory | Sort-Object Name) {
    foreach ($lessonFile in Get-ChildItem -LiteralPath $levelDir.FullName -Filter "lesson*.json" | Sort-Object Name) {
        $lesson = Read-Json $lessonFile.FullName
        if (Is-Blank $lesson.id) {
            Add-Error "$($levelDir.Name)/$($lessonFile.Name) is missing lesson id"
        }
        if (Is-Blank $lesson.titleDa) {
            Add-Error "$($levelDir.Name)/$($lessonFile.Name) is missing lesson titleDa"
        }

        $dialogs = As-Array $lesson.dialogues
        if ($dialogs.Count -lt 1) {
            Add-Error "$($levelDir.Name)/$($lessonFile.Name) has no dialogues"
            continue
        }

        $dialogIds = New-Object System.Collections.Generic.HashSet[string]
        foreach ($dialog in $dialogs) {
            if (Is-Blank $dialog.id) {
                Add-Error "$($levelDir.Name)/$($lessonFile.Name) has a dialog without id"
            } elseif (-not $dialogIds.Add([string]$dialog.id)) {
                Add-Error "$($levelDir.Name)/$($lessonFile.Name) has duplicate dialog id $($dialog.id)"
            }
            if (Is-Blank $dialog.titleDa) {
                Add-Error "$($levelDir.Name)/$($lessonFile.Name) dialog $($dialog.id) is missing titleDa"
            }

            $phrases = As-Array $dialog.phrases
            if ($phrases.Count -lt 1) {
                Add-Error "$($levelDir.Name)/$($lessonFile.Name) dialog $($dialog.id) has no phrases"
                continue
            }

            for ($phraseIndex = 0; $phraseIndex -lt $phrases.Count; $phraseIndex++) {
                $phrase = $phrases[$phraseIndex]
                Test-Phrase $levelDir.Name $lessonFile.Name $dialog $phrase ($phraseIndex + 1)

                if ($levelDir.Name -eq "level3" -and ($bannedChildrenFiller -contains [string]$phrase.textPt)) {
                    Add-Error "$($levelDir.Name)/$($lessonFile.Name) dialog $($dialog.id) contains generated filler phrase: $($phrase.textPt)"
                }
            }

            if ($levelDir.Name -eq "level3") {
                $vocabulary = As-Array $dialog.vocabulary
                if ($vocabulary.Count -lt 1) {
                    Add-Error "$($levelDir.Name)/$($lessonFile.Name) dialog $($dialog.id) has no vocabulary array items"
                }
            }
        }
    }
}

foreach ($lessonFile in Get-ChildItem -LiteralPath $childrenRoot -Filter "lesson*.json" | Sort-Object Name) {
    $lesson = Read-Json $lessonFile.FullName
    $dialogs = As-Array $lesson.dialogues
    if ($dialogs.Count -lt 3) {
        Add-Error "level3/$($lessonFile.Name) needs at least three dialogs for navigation validation"
        continue
    }

    $state = @{ DialogIndex = 0; PhraseIndex = 0 }
    for ($step = 1; $step -le 20; $step++) {
        $state = Move-Next $state $dialogs
        $dialog = $dialogs[$state.DialogIndex]
        $phrases = As-Array $dialog.phrases
        if ($state.PhraseIndex -lt 0 -or $state.PhraseIndex -ge $phrases.Count) {
            Add-Error "level3/$($lessonFile.Name) Next step $step lands outside dialog $($dialog.id)"
        } elseif (Is-Blank $phrases[$state.PhraseIndex].textPt) {
            Add-Error "level3/$($lessonFile.Name) Next step $step lands on an empty phrase"
        }
    }

    $state = @{ DialogIndex = [Math]::Min(2, $dialogs.Count - 1); PhraseIndex = 0 }
    for ($step = 1; $step -le 20; $step++) {
        $state = Move-Previous $state $dialogs
        $dialog = $dialogs[$state.DialogIndex]
        $phrases = As-Array $dialog.phrases
        if ($state.PhraseIndex -lt 0 -or $state.PhraseIndex -ge $phrases.Count) {
            Add-Error "level3/$($lessonFile.Name) Previous step $step lands outside dialog $($dialog.id)"
        } elseif (Is-Blank $phrases[$state.PhraseIndex].textPt) {
            Add-Error "level3/$($lessonFile.Name) Previous step $step lands on an empty phrase"
        }
    }
}

$mainActivityPath = Join-Path $Root "app/src/main/java/dk/nenolink/learnportuguese2/MainActivity.java"
$mainActivity = Get-Content -LiteralPath $mainActivityPath -Raw -Encoding UTF8
if ($mainActivity -match "createFallbackPhrases|createPlaceholderDialogues|Conteúdo em preparação") {
    Add-Error "MainActivity contains a dialog phrase fallback or placeholder content path"
}
if ($mainActivity -notmatch "formatLessonStatusLine") {
    Add-Error "MainActivity is missing shared lesson status formatter"
}
if ($mainActivity -notmatch "buildPhraseBreadcrumb") {
    Add-Error "MainActivity is missing shared dialog breadcrumb builder"
}
if ($mainActivity -notmatch "getPackageInfo\(\)" -or $mainActivity -notmatch "getPackageVersionCode" -or $mainActivity -notmatch "R\.string\.release_date") {
    Add-Error "MainActivity does not display version/build/date from build metadata"
}
if ($mainActivity -notmatch "Intent\.ACTION_VIEW" -or $mainActivity -notmatch "R\.string\.nenolink_url") {
    Add-Error "MainActivity does not open the configured Nenolink URL through a browser intent"
}

$buildGradle = Get-Content -LiteralPath $buildGradlePath -Raw -Encoding UTF8
if ($buildGradle -notmatch 'versionName\s+"[^"]+"' -or $buildGradle -notmatch 'versionCode\s+\d+') {
    Add-Error "app/build.gradle is missing versionName or versionCode"
}
if ($buildGradle -notmatch 'releaseDate\s*=\s*"[^"]+"' -or $buildGradle -notmatch 'resValue\s+"string",\s+"release_date"' -or $buildGradle -notmatch 'resValue\s+"string",\s+"nenolink_url"') {
    Add-Error "app/build.gradle is missing release date or Nenolink URL resource values"
}

$guide = Read-Json $guidePath
$guideText = $guide | ConvertTo-Json -Depth 20 -Compress
if ($guideText -match "Level") {
    Add-Error "User guide still uses visible English Level terminology"
}

$childrenSafety = Read-Json $childrenSafetyPath
$safetyPhrases = @()
foreach ($dialog in (As-Array $childrenSafety.dialogues)) {
    foreach ($phrase in (As-Array $dialog.phrases)) {
        Test-Phrase "level3" "lesson10.json" $dialog $phrase "safety"
        $safetyPhrases += [string]$phrase.textPt
    }
}
foreach ($requiredPhrase in @("Quero falar com a polícia.", "Onde é a esquadra?", "Não é uma emergência.", "É uma emergência.", "Ligue para o 112 em Portugal.", "Ligue só em caso de perigo.")) {
    if ($safetyPhrases -notcontains $requiredPhrase) {
        Add-Error "Children safety content is missing required Portuguese phrase: $requiredPhrase"
    }
}

$lp3Root = Join-Path $levelRoot "level4"
if (-not (Test-Path -LiteralPath $lp3Root)) {
    Add-Error "Learn Portuguese 3 content folder is missing: levels/level4"
} else {
    $lp3Level = Read-Json (Join-Path $lp3Root "level.json")
    if ($lp3Level.titleDa -ne "Learn Portuguese 3") {
        Add-Error "level4/level.json must identify the package as Learn Portuguese 3"
    }

    $lp3Lessons = @(Get-ChildItem -LiteralPath $lp3Root -Filter "lesson*.json" | Sort-Object Name)
    if ($lp3Lessons.Count -ne 10) {
        Add-Error "Learn Portuguese 3 must contain exactly 10 lesson files; found $($lp3Lessons.Count)"
    }

    $lp3PhrasePtIndex = @{}
    $lp3PhraseDaIndex = @{}
    foreach ($lessonFile in $lp3Lessons) {
        $lesson = Read-Json $lessonFile.FullName
        if (Is-Blank $lesson.id -or Is-Blank $lesson.titleDa) {
            Add-Error "level4/$($lessonFile.Name) is missing a valid lesson id or title"
        }

        $dialogs = As-Array $lesson.dialogues
        if ($dialogs.Count -ne 10) {
            Add-Error "level4/$($lessonFile.Name) must contain exactly 10 dialogs; found $($dialogs.Count)"
        }

        $story = As-Array $lesson.story
        if ($story.Count -lt 18 -or $story.Count -gt 22) {
            Add-Error "level4/$($lessonFile.Name) story should contain approximately 20 lines; found $($story.Count)"
        }
        foreach ($storyPhrase in $story) {
            Test-Phrase "level4" $lessonFile.Name ([pscustomobject]@{ id = "story" }) $storyPhrase "story"
        }
        $storyPt = New-Object System.Collections.Generic.HashSet[string]
        foreach ($storyPhrase in $story) {
            if (-not (Is-Blank $storyPhrase.textPt) -and -not $storyPt.Add([string]$storyPhrase.textPt)) {
                Add-Error "level4/$($lessonFile.Name) story contains duplicate Portuguese reading line: $($storyPhrase.textPt)"
            }
        }

        foreach ($dialog in $dialogs) {
            if (Is-Blank $dialog.id -or Is-Blank $dialog.titleDa) {
                Add-Error "level4/$($lessonFile.Name) has a dialog missing id or title"
            }
            $phrases = As-Array $dialog.phrases
            if ($phrases.Count -ne 10) {
                Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) must contain exactly 10 phrases; found $($phrases.Count)"
            }

            $dialogPt = New-Object System.Collections.Generic.HashSet[string]
            $dialogDa = New-Object System.Collections.Generic.HashSet[string]
            $euStarts = 0
            for ($phraseIndex = 0; $phraseIndex -lt $phrases.Count; $phraseIndex++) {
                $phrase = $phrases[$phraseIndex]
                Test-Phrase "level4" $lessonFile.Name $dialog $phrase ($phraseIndex + 1)

                $textPt = [string]$phrase.textPt
                $textDa = [string]$phrase.textDa
                if ($bannedFallbackPhrases -contains $textPt) {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) contains fallback/helper phrase: $textPt"
                }
                if ($textPt -match "^(Eu|eu)\s") {
                    $euStarts++
                }
                if (-not (Is-Blank $textPt) -and -not $dialogPt.Add($textPt)) {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) repeats Portuguese phrase: $textPt"
                }
                if (-not (Is-Blank $textDa) -and -not $dialogDa.Add($textDa)) {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) repeats Danish phrase: $textDa"
                }
                if (-not (Is-Blank $textPt)) {
                    if ($lp3PhrasePtIndex.ContainsKey($textPt)) {
                        Add-Error "Learn Portuguese 3 repeats Portuguese phrase in $($lessonFile.Name) dialog $($dialog.id): $textPt"
                    } else {
                        $lp3PhrasePtIndex[$textPt] = "$($lessonFile.Name) dialog $($dialog.id)"
                    }
                }
                if (-not (Is-Blank $textDa)) {
                    if ($lp3PhraseDaIndex.ContainsKey($textDa)) {
                        Add-Error "Learn Portuguese 3 repeats Danish phrase in $($lessonFile.Name) dialog $($dialog.id): $textDa"
                    } else {
                        $lp3PhraseDaIndex[$textDa] = "$($lessonFile.Name) dialog $($dialog.id)"
                    }
                }
                if ($textPt -match "Hoje eu falo em casa|Ontem eu falei em casa|Também preciso de falar sobre") {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) contains old repetitive placeholder pattern: $textPt"
                }
            }
            if ($euStarts -gt 3) {
                Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) starts too many phrases with Eu/eu; found $euStarts"
            }
            if (@($phrases | Where-Object { [string]$_.textPt -match "\?" }).Count -lt 1) {
                Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) should include at least one question"
            }

            $grammarItems = As-Array $dialog.grammar
            if ($grammarItems.Count -lt 1) {
                Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) is missing grammar notes"
            }
            foreach ($grammar in $grammarItems) {
                if (Is-Blank $grammar.titleDa -or Is-Blank $grammar.explanationDa -or Is-Blank $grammar.notesDa) {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) has incomplete grammar text"
                }
                if ((As-Array $grammar.conjugation).Count -lt 6) {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) needs a conjugation table"
                }
                if ((As-Array $grammar.examples).Count -lt 2) {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) needs grammar examples"
                }
                if ((As-Array $grammar.commonMistakes).Count -lt 1) {
                    Add-Error "level4/$($lessonFile.Name) dialog $($dialog.id) needs commonMistakes"
                }
            }
        }

        if ($lessonFile.Name -eq "lesson10.json") {
            $lesson10Text = $lesson | ConvertTo-Json -Depth 50 -Compress
            foreach ($requiredPharmacyText in @("farmácia de serviço", "receita", "adequado para crianças", "efeito secundário", "médico", "instruções da embalagem", "não substitui aconselhamento médico")) {
                if ($lesson10Text -notmatch [regex]::Escape($requiredPharmacyText)) {
                    Add-Error "level4/lesson10.json is missing cautious pharmacy wording: $requiredPharmacyText"
                }
            }
        }

        if ((As-Array $lesson.quiz).Count -lt 5) {
            Add-Error "level4/$($lessonFile.Name) needs reusable quiz questions from JSON"
        }

        $state = @{ DialogIndex = 0; PhraseIndex = 0 }
        for ($step = 1; $step -le 20; $step++) {
            $state = Move-Next $state $dialogs
            $dialog = $dialogs[$state.DialogIndex]
            $phrases = As-Array $dialog.phrases
            if ($state.PhraseIndex -lt 0 -or $state.PhraseIndex -ge $phrases.Count) {
                Add-Error "level4/$($lessonFile.Name) Next step $step lands outside dialog $($dialog.id)"
            }
        }
    }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Output "Navigation/content validation OK"
