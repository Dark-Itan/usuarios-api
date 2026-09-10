# Cargar variables del .env
Get-Content .env | ForEach-Object {
    if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
        [System.Environment]::SetEnvironmentVariable($matches[1].Trim(), $matches[2].Trim(), 'Process')
    }
}

Write-Host 'Variables cargadas:' -ForegroundColor Green
Write-Host "  DB_URL: $env:DB_URL"
Write-Host "  DB_USERNAME: $env:DB_USERNAME"
Write-Host "  PORT: $env:PORT"
Write-Host ''

# Arrancar la aplicación
./gradlew bootRun
