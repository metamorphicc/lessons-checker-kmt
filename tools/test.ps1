$ErrorActionPreference = 'Stop'
$projectRoot = Split-Path -Parent $PSScriptRoot
$javaBin = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin' } else { 'C:\Program Files\Android\Android Studio\jbr\bin' }
$classes = Join-Path $projectRoot 'tmp\test-classes'
New-Item -ItemType Directory -Path $classes -Force | Out-Null
$source = Join-Path $projectRoot 'app\src\main\java\ru\is232\lessons'
& (Join-Path $javaBin 'javac.exe') -encoding UTF-8 -d $classes (Join-Path $source 'ScheduleEngine.java') (Join-Path $source 'ScheduleData.java') (Join-Path $projectRoot 'tests\ScheduleEngineTest.java')
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
& (Join-Path $javaBin 'java.exe') -cp $classes ru.is232.lessons.ScheduleEngineTest
if ($LASTEXITCODE -ne 0) { throw 'Schedule checks failed' }
