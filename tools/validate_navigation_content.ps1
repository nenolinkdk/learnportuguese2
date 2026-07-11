param(
    [string]$Root = "."
)

$ErrorActionPreference = "Stop"
$errors = New-Object System.Collections.Generic.List[string]

function Read-Json($Path) {
    Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json
}

function Get-DialogPhraseCount($Dialog) {
    if ($null -eq $Dialog.phrases) {
        return 0
    }
    return @($Dialog.phrases).Count
}

$levelRoot = Join-Path $Root "app/src/main/assets/levels"
$childrenRoot = Join-Path $levelRoot "level3"
$bannedDialogPhrases = @(
    "Muito bem. Repete devagar.",
    "Obrigado."
)

foreach ($lessonFile in Get-ChildItem -LiteralPath $childrenRoot -Filter "lesson*.json") {
    $lesson = Read-Json $lessonFile.FullName
    if (@($lesson.dialogues).Count -lt 1) {
        $errors.Add("$($lessonFile.Name) has no dialogs")
        continue
    }

    foreach ($dialog in $lesson.dialogues) {
        $phraseCount = Get-DialogPhraseCount $dialog
        if ($phraseCount -lt 2) {
            $errors.Add("$($lessonFile.Name) dialog $($dialog.id) has fewer than two phrases")
        }

        foreach ($phrase in $dialog.phrases) {
            if ($bannedDialogPhrases -contains [string]$phrase.textPt) {
                $errors.Add("$($lessonFile.Name) dialog $($dialog.id) contains helper phrase in dialog navigation: $($phrase.textPt)")
            }
        }
    }
}

foreach ($levelDir in Get-ChildItem -LiteralPath $levelRoot -Directory) {
    foreach ($lessonFile in Get-ChildItem -LiteralPath $levelDir.FullName -Filter "lesson*.json") {
        $lesson = Read-Json $lessonFile.FullName
        $dialogs = @($lesson.dialogues)
        if ($dialogs.Count -lt 2) {
            continue
        }

        for ($dialogIndex = 0; $dialogIndex -lt $dialogs.Count; $dialogIndex++) {
            $current = $dialogs[$dialogIndex]
            $currentCount = Get-DialogPhraseCount $current
            if ($currentCount -lt 1) {
                $errors.Add("$($levelDir.Name)/$($lessonFile.Name) dialog $($current.id) has no phrases")
                continue
            }

            $nextDialog = $dialogs[($dialogIndex + 1) % $dialogs.Count]
            $previousDialog = $dialogs[($dialogIndex - 1 + $dialogs.Count) % $dialogs.Count]
            $nextPhrase = @($nextDialog.phrases)[0]
            $previousPhrase = @($previousDialog.phrases)[(Get-DialogPhraseCount $previousDialog) - 1]

            if ([string]::IsNullOrWhiteSpace([string]$nextPhrase.textPt)) {
                $errors.Add("$($levelDir.Name)/$($lessonFile.Name) next navigation lands on empty phrase after dialog $($current.id)")
            }
            if ([string]::IsNullOrWhiteSpace([string]$previousPhrase.textPt)) {
                $errors.Add("$($levelDir.Name)/$($lessonFile.Name) previous navigation lands on empty phrase before dialog $($current.id)")
            }
        }
    }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Output "Navigation/content validation OK"
