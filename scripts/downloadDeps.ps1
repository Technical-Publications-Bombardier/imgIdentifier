if (-not(Get-Command -Name mvn -ErrorAction SilentlyContinue)) {
    if(Test-Path -Path "$env:LOCALAPPDATA\Programs\Maven\bin\")
    {
        $env:PATH += ";$env:LOCALAPPDATA\Programs\Maven\bin\"
    }else{
        Write-Error -Message "Cannot find Maven executable"
        return
    }
}
Write-Verbose -Message 'Querying for latest JCGM-Core' -Verbose
Invoke-RestMethod -Uri "https://api.github.com/repos/SwissAS/jcgm-core/releases/latest" -Verbose | Set-Variable -Name LatestJCGMCore
$LatestJCGMCoreJars = $LatestJCGMCore.assets | Where-Object { $_.name -like '*.jar' }
$jcgmLatestVersion = $LatestJCGMCoreJars.name -replace '.*?([\d\.]+).*', '$1' -replace '\.$' | Select-Object -First 1
Write-Verbose -Message "Found $( $LatestJCGMCoreJars.Count ) assets for JCGM-Core version '$jcgmLatestVersion'" -Verbose
[xml]$pom = Get-Content -Path .\pom.xml
$pomVersion = $pom.project.properties.'jcgm.version'
Write-Verbose -Message "Current POM version of jcgm is '$pomVersion'" -Verbose
$jcgmMvnPath = "$env:USERPROFILE\.m2\repository\net\sf\jcgm\core\jcgm-core\*\*.jar"
if (-not (Test-Path -Path ($jcgmMvnPath  -replace '\\\*(?=\\\*\.jar)',"\$jcgmLatestVersion") -PathType Leaf -Verbose) -or $pomVersion -ne $jcgmLatestVersion) {
    Write-Verbose -Message 'Updating project jcgm version' -Verbose
    $pom.project.properties.'jcgm.version' = $jcgmLatestVersion
    $pom.Save(((Get-Item -Path '.\pom.xml').FullName))
    foreach ($JCGMCoreJar in $LatestJCGMCoreJars) {
        Write-Verbose -Message "Downloading '$( $JCGMCoreJar.name )'"
        $jarPath = "$env:TEMP\$( $JCGMCoreJar.name )"
        Invoke-WebRequest -Uri ($JCGMCoreJar.browser_download_url) -OutFile $jarPath -Verbose
        if ((Test-Path -Path $jarPath -PathType Leaf -Verbose) -and -not [string]::IsNullOrEmpty($jcgmLatestVersion)) {
            Write-Verbose -Message "Installing '$( $JCGMCoreJar.name )'"
            if ($JCGMCoreJar.name -like '*-sources.jar') {
                & mvn install:install-file -Dfile="$jarPath" -DgroupId='net.sf.jcgm.core' -DartifactId='jcgm-core' -Dversion="$jcgmLatestVersion" -Dpackaging='jar' -Dclassifier='sources'
            } else {
                & mvn install:install-file -Dfile="$jarPath" -DgroupId='net.sf.jcgm.core' -DartifactId='jcgm-core' -Dversion="$jcgmLatestVersion" -Dpackaging='jar'
            }
            if ($?) {
                Write-Verbose -Message "Successfully installed '$( $JCGMCoreJar.name )'"
                Remove-Item -Path $jarPath -Verbose
            }
            else {
                Write-Error -Message "Failed to install '$( $JCGMCoreJar.name )' with ``& mvn install:install-file -Dfile="$jarPath" -DgroupId='net.sf.jcgm.core' -DartifactId='jcgm-core' -Dversion="$jcgmLatestVersion" -Dpackaging='jar'``"
            }
        }
    }
}
else {
    $jcgmCore = Get-Item -Path $jcgmMvnPath
    Write-Verbose -Message "Already installed JCGM Core:`n`t$($jcgmCore -join "`n`t")" -Verbose
}