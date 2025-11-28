$propsFile = Join-Path $PSScriptRoot "gradle.properties"
if (-not (Test-Path $propsFile)) {
    Write-Error "gradle.properties not found at $propsFile"
    exit 1
}

$content = Get-Content $propsFile
$newContent = @()
$versionUpdated = $false

foreach ($line in $content) {
    if ($line -match "^mod_version=(\d+)\.(\d+)\.(\d+)$") {
        $major = $matches[1]
        $minor = $matches[2]
        $patch = [int]$matches[3] + 1
        $newVersion = "$major.$minor.$patch"
        Write-Host "Incrementing version: $($matches[0]) -> mod_version=$newVersion" -ForegroundColor Green
        $versionUpdated = $true
        $newContent += "mod_version=$newVersion"
    } else {
        $newContent += $line
    }
}

if ($versionUpdated) {
    $newContent | Set-Content $propsFile
    
    # Ensure correct Java version (JDK 17) is used
    $jdk17 = "C:\Program Files\Java\jdk-17.0.5"
    if (Test-Path $jdk17) {
        $env:JAVA_HOME = $jdk17
        Write-Host "Temporarily set JAVA_HOME to $jdk17" -ForegroundColor Yellow
    }

    Write-Host "Starting build..." -ForegroundColor Cyan
    & .\gradlew.bat build
    if ($LASTEXITCODE -eq 0) {
        Write-Host "Build successful!" -ForegroundColor Green
    } else {
        Write-Error "Build failed with exit code $LASTEXITCODE"
    }
} else {
    Write-Warning "Could not find or parse 'mod_version' in gradle.properties. Expected format: mod_version=x.y.z"
}
