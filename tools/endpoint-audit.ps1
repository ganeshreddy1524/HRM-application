$ErrorActionPreference = 'Stop'

function Normalize-Path([string]$p) {
  if (-not $p) { return '' }
  $p = $p.Trim() -replace '\\','/'
  while ($p -match '//') { $p = $p -replace '//','/' }
  if (-not $p.StartsWith('/')) { $p = '/' + $p }
  if ($p.StartsWith('/api/')) { $p = $p.Substring(4) }
  if ($p.Length -gt 1 -and $p.EndsWith('/')) { $p = $p.TrimEnd('/') }
  $p = $p -replace "\?.*$", ''
  $p = $p -replace '\$\{[^}]+\}', '{}'
  return $p
}

$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

$knownRoots = @('/auth','/users','/employees','/leaves','/admin','/goals','/performance','/internal')

# Collect endpoints referenced by the Angular frontend.
$frontendFiles = Get-ChildItem -Recurse -File frontend\src\app -Filter *.ts | Select-Object -ExpandProperty FullName
$frontendEndpoints = New-Object System.Collections.Generic.HashSet[string]

# 1) Direct ApiService calls with string literal.
$apiCallRegex = [regex]'\b(?:this\.)?api\.(get|post|put|patch|delete)\s*(?:<[^>]*>)?\s*\(\s*(?:''([^'']+)''|"([^"]+)"|`([^`]+)`)'
foreach ($f in $frontendFiles) {
  $text = Get-Content -Raw $f
  foreach ($m in $apiCallRegex.Matches($text)) {
    $ep = $m.Groups[2].Value
    if (-not $ep) { $ep = $m.Groups[3].Value }
    if (-not $ep) { $ep = $m.Groups[4].Value }
    $ep = Normalize-Path $ep
    if (-not $ep) { continue }
    if ($knownRoots | Where-Object { $ep.StartsWith($_) }) { [void]$frontendEndpoints.Add($ep) }
  }
}

# 2) Any endpoint-like string literals (covers: const endpoint = '...').
$singleQuoted = [regex]"'(/[^'\r\n]+)'"
$doubleQuoted = [regex]'"(/[^"\r\n]+)"'
$backtickQuoted = [regex]'`(/[^`\r\n]+)`'
foreach ($f in $frontendFiles) {
  $text = Get-Content -Raw $f
  foreach ($m in $singleQuoted.Matches($text)) {
    $ep = Normalize-Path $m.Groups[1].Value
    if ($knownRoots | Where-Object { $ep.StartsWith($_) }) { [void]$frontendEndpoints.Add($ep) }
  }
  foreach ($m in $doubleQuoted.Matches($text)) {
    $ep = Normalize-Path $m.Groups[1].Value
    if ($knownRoots | Where-Object { $ep.StartsWith($_) }) { [void]$frontendEndpoints.Add($ep) }
  }
  foreach ($m in $backtickQuoted.Matches($text)) {
    $ep = Normalize-Path $m.Groups[1].Value
    if ($knownRoots | Where-Object { $ep.StartsWith($_) }) { [void]$frontendEndpoints.Add($ep) }
  }
}

# Collect endpoints exposed by backend controllers.
$controllerFiles = Get-ChildItem -Recurse -File -Filter *Controller.java | Select-Object -ExpandProperty FullName
$backend = @()
$methodRegex = [regex]'@(GetMapping|PostMapping|PutMapping|PatchMapping|DeleteMapping)(\(([^)]*)\))?'
$rmBaseRegex = [regex]'@RequestMapping\((?:value\s*=\s*)?"([^"]+)"'
foreach ($f in $controllerFiles) {
  $text = Get-Content -Raw $f
  $base = ''
  $baseMatch = $rmBaseRegex.Match($text)
  if ($baseMatch.Success) { $base = $baseMatch.Groups[1].Value }
  $service = (Split-Path (Split-Path (Split-Path $f -Parent) -Parent) -Leaf)

  foreach ($m in $methodRegex.Matches($text)) {
    $verb = $m.Groups[1].Value -replace 'Mapping$',''
    $args = $m.Groups[3].Value
    $sub = ''
    if ($args) {
      $s = [regex]::Match($args, '"([^"]*)"')
      if ($s.Success) { $sub = $s.Groups[1].Value }
    }
    $full = Normalize-Path ($base + '/' + $sub)
    $full = $full -replace '\{[^}]+\}','{}'
    $backend += [pscustomobject]@{ service = $service; verb = $verb.ToUpper(); path = $full }
  }
}

$backend = $backend | Sort-Object service, verb, path -Unique

# Compare: list backend endpoints not referenced anywhere in the frontend.
$report = $backend | ForEach-Object {
  $p = $_.path
  $used = $frontendEndpoints.Contains($p)
  if (-not $used -and ($p -match '\{\}')) {
    $prefix = $p -replace '\{\}',''
    $used = ($frontendEndpoints | Where-Object { $_.StartsWith($prefix) } | Measure-Object).Count -gt 0
  }
  [pscustomobject]@{ service = $_.service; verb = $_.verb; path = $_.path; usedInFrontend = $used }
}

$unused = $report | Where-Object { -not $_.usedInFrontend } | Sort-Object service, verb, path

"FRONTEND_ENDPOINTS_DISCOVERED=$($frontendEndpoints.Count)"
"BACKEND_ENDPOINTS_DISCOVERED=$($report.Count)"
"BACKEND_ENDPOINTS_NOT_REFERENCED_IN_FRONTEND=$($unused.Count)"
$unused | Format-Table -AutoSize

