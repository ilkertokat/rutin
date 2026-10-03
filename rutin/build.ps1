# Rutin uygulamasini imzali APK'ye derler (kaynak + asset destekli).
# Kullanim: powershell -ExecutionPolicy Bypass -File build.ps1 [-OutName Rutin] [-SkipTests]
param(
    [string] $OutName = "Rutin",
    [switch] $SkipTests
)
$ErrorActionPreference = "Stop"
$Proj  = $PSScriptRoot
$Tools = Join-Path $env:USERPROFILE ".android-apk-tools"
$Sdk   = Join-Path $Tools "android-sdk"
$bt    = "$Sdk\build-tools\35.0.1"
$androidJar = "$Sdk\platforms\android-35\android.jar"
$ks    = "$Tools\debug.keystore"
$jdk = (Get-ChildItem "$Tools\jdk" -Directory | Select-Object -First 1).FullName
$env:JAVA_HOME = $jdk
$env:PATH = "$jdk\bin;$env:PATH"
$javac = "$jdk\bin\javac.exe"
$java  = "$jdk\bin\java.exe"
$jar   = "$jdk\bin\jar.exe"

$app = Join-Path $Proj "app"
$out = Join-Path $Proj "out"
if (Test-Path $out) { Remove-Item $out -Recurse -Force }
New-Item -ItemType Directory -Force "$out\classes", "$out\dex", "$out\gen", "$out\tools", "$out\test" | Out-Null

$coreFiles = Get-ChildItem "$app\src\com\ilker\rutin\core" -Filter *.java | ForEach-Object { $_.FullName }

Write-Host "[0/8] ses dosyalari uretiliyor..."
New-Item -ItemType Directory -Force "$app\res\raw" | Out-Null
& $javac -encoding UTF-8 -d "$out\tools" $coreFiles "$Proj\tools\GenSounds.java"
if ($LASTEXITCODE -ne 0) { throw "GenSounds derleme basarisiz" }
& $java -cp "$out\tools" GenSounds "$app\res\raw"
if ($LASTEXITCODE -ne 0) { throw "GenSounds calisma basarisiz" }

if (-not $SkipTests) {
    Write-Host "[1/8] cekirdek testleri..."
    & $javac -encoding UTF-8 -d "$out\test" $coreFiles "$Proj\tests\CoreTest.java"
    if ($LASTEXITCODE -ne 0) { throw "test derleme basarisiz" }
    & $java -cp "$out\test" CoreTest
    if ($LASTEXITCODE -ne 0) { throw "TESTLER BASARISIZ" }
}

Write-Host "[2/8] aapt2 compile..."
& "$bt\aapt2.exe" compile --dir "$app\res" -o "$out\res.zip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 compile basarisiz" }

Write-Host "[3/8] aapt2 link..."
& "$bt\aapt2.exe" link -o "$out\base.apk" -I $androidJar --manifest "$app\AndroidManifest.xml" `
    -A "$app\assets" --java "$out\gen" --auto-add-overlay --min-sdk-version 26 --target-sdk-version 35 `
    --version-code 1 --version-name "1.0" -0 ttf -0 wav "$out\res.zip"
if ($LASTEXITCODE -ne 0) { throw "aapt2 link basarisiz" }

Write-Host "[4/8] javac..."
New-Item -ItemType Directory -Force "$out\stubs" | Out-Null
& $javac -source 1.8 -target 1.8 -Xlint:-options -bootclasspath $androidJar -d "$out\stubs" "$Proj\tools\stubs\java\lang\invoke\LambdaMetafactory.java"
if ($LASTEXITCODE -ne 0) { throw "stub derleme basarisiz" }
$javaFiles = @(Get-ChildItem "$app\src" -Recurse -Filter *.java | ForEach-Object { $_.FullName }) + @(Get-ChildItem "$out\gen" -Recurse -Filter *.java | ForEach-Object { $_.FullName })
& $javac -encoding UTF-8 -source 1.8 -target 1.8 -Xlint:-options -bootclasspath "$out\stubs;$androidJar" -classpath $androidJar -d "$out\classes" $javaFiles
if ($LASTEXITCODE -ne 0) { throw "javac basarisiz" }

Write-Host "[5/8] d8..."
& $jar cf "$out\classes.jar" -C "$out\classes" .
if ($LASTEXITCODE -ne 0) { throw "classes.jar basarisiz" }
& "$bt\d8.bat" --release --lib $androidJar --min-api 26 --output "$out\dex" "$out\classes.jar"
if ($LASTEXITCODE -ne 0) { throw "d8 basarisiz" }

Write-Host "[6/8] dex -> apk..."
Copy-Item "$out\base.apk" "$out\unsigned.apk" -Force
Push-Location "$out\dex"
& $jar uf "$out\unsigned.apk" "classes.dex"
$je = $LASTEXITCODE
Pop-Location
if ($je -ne 0) { throw "jar ekleme basarisiz" }

Write-Host "[7/8] zipalign..."
& "$bt\zipalign.exe" -p -f 4 "$out\unsigned.apk" "$out\aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "zipalign basarisiz" }

Write-Host "[8/8] imzalama..."
$apk = Join-Path $Proj "$OutName.apk"
& "$bt\apksigner.bat" sign --ks $ks --ks-pass pass:android --key-pass pass:android --min-sdk-version 26 --out $apk "$out\aligned.apk"
if ($LASTEXITCODE -ne 0) { throw "apksigner basarisiz" }
& "$bt\apksigner.bat" verify $apk | Out-Null
$ok = $LASTEXITCODE -eq 0
Write-Host ""
Write-Host "=== BITTI ==="
Write-Host ("APK: $apk  ({0:N1} KB)  imza_dogrulandi={1}" -f ((Get-Item $apk).Length/1KB), $ok)
