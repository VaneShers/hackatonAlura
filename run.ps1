#Requires -Version 5.1
[CmdletBinding()]
param(
    [switch]$Build
)

function Get-DockerPath {
    $candidates = @(
        "docker",
        "C:\Program Files\Docker\Docker\resources\bin\docker.exe",
        "C:\Program Files\Docker\Docker\DockerCli.exe"
    )
    foreach ($p in $candidates) {
        try {
            $exists = $false
            if ($p -eq "docker") {
                $exists = (Get-Command docker -ErrorAction SilentlyContinue) -ne $null
            } else {
                $exists = Test-Path $p
            }
            if ($exists) { return $p }
        } catch {}
    }
    throw "Docker CLI not found in PATH or common locations"
}

function Compose {
    param([string]$Docker, [string]$ComposeFile, [string[]]$Args)
    if ($Docker -eq "docker") {
        & docker compose -f $ComposeFile @Args
    } else {
        & $Docker compose -f $ComposeFile @Args
    }
}

function Wait-HttpOk {
    param([string]$Url, [int]$Retries = 30, [int]$DelaySeconds = 2, [hashtable]$Headers)
    for ($i = 0; $i -lt $Retries; $i++) {
        try {
            $params = @{ Uri = $Url; UseBasicParsing = $true }
            if ($Headers) { $params.Headers = $Headers }
            $res = Invoke-RestMethod @params
            return $res
        } catch {
            Start-Sleep -Seconds $DelaySeconds
        }
    }
    throw "Timeout waiting for $Url"
}

Push-Location $PSScriptRoot
$composeFile = Join-Path $PSScriptRoot "docker-compose.yml"
$docker = Get-DockerPath

Write-Host "Starting containers via Docker Compose..." -ForegroundColor Cyan
$composeArgs = @("up", "-d")
if ($Build) { $composeArgs += "--build" }
Compose -Docker $docker -ComposeFile $composeFile -Args $composeArgs

Write-Host "Waiting for DS health (http://localhost:8000/health)..." -ForegroundColor Cyan
$ds = Wait-HttpOk -Url "http://localhost:8000/health"
Write-Host "DS status: $($ds.status)" -ForegroundColor Green

Write-Host "Obtaining JWT and checking API health..." -ForegroundColor Cyan
$loginBody = @{ email = "admin@local"; password = "Admin123!" } | ConvertTo-Json
$res = Invoke-RestMethod -Method POST -Uri "http://localhost:8080/api/auth/login" -ContentType "application/json" -Body $loginBody -UseBasicParsing
$token = $res.token
$headers = @{ Authorization = "Bearer $token" }
$api = Wait-HttpOk -Url "http://localhost:8080/actuator/health" -Headers $headers
Write-Host "API status: $($api.status)" -ForegroundColor Green

Write-Host "Opening dashboard at http://localhost:8501" -ForegroundColor Cyan
Start-Process "http://localhost:8501"

Write-Host "Done. Containers are running. Use 'docker compose ps' and 'docker compose logs -f' for status/logs." -ForegroundColor Green
Pop-Location
