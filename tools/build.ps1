$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
if (-not $env:JAVA_HOME -and (Test-Path 'C:\Program Files\Android\Android Studio\jbr')) {
    $env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
}
if (-not $env:ANDROID_HOME -and (Test-Path "$env:LOCALAPPDATA\Android\Sdk")) {
    $env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
}
Push-Location $projectRoot
try {
    & (Join-Path $projectRoot 'gradlew.bat') :app:assembleRelease :app:lintRelease --console=plain
    if ($LASTEXITCODE -ne 0) { throw 'Android build failed' }
    $outputPath = Join-Path $projectRoot 'output'
    New-Item -ItemType Directory -Path $outputPath -Force | Out-Null
    $apkPath = Join-Path $outputPath 'lessons-is232.apk'
    Copy-Item -LiteralPath (Join-Path $projectRoot 'app\build\outputs\apk\release\app-release.apk') -Destination $apkPath
    $hash = (Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash.ToLowerInvariant()
    [System.IO.File]::WriteAllText((Join-Path $outputPath 'lessons-is232.apk.sha256'), "$hash  lessons-is232.apk`n")
    Write-Output "APK: $apkPath"
} finally { Pop-Location }
