if(-not (Test-Path "$env:USERPROFILE\.m2\repository\net\jcgm-core\*\*.jar" -PathType Leaf -Verbose)){
    Write-Verbose -Message "Querying for latest JCGM-Core" -Verbose
    Invoke-RestMethod -Uri https://api.github.com/repos/SwissAS/jcgm-core/releases/latest -Verbose | Set-Variable -Name LatestJCGMCore
    $LatestJCGMCoreJars = $LatestJCGMCore.assets | Where-Object { $_.name -like "*.jar" }
    $version = $LatestJCGMCoreJars.name -replace '.*?([\d\.]+).*', '$1' -replace '\.$' | Select-Object -First 1
    Write-Verbose -Message "Found $( $LatestJCGMCoreJars.Count ) JCGM-Core version $version assets" -Verbose
    [xml]$pom = Get-Content -Path .\pom.xml
    $pom.project.properties.'jcgm.version' = $version
    $pom.Save(((Get-Item -Path '.\pom.xml').FullName))
    foreach ($JCGMCoreJar in $LatestJCGMCoreJars)
    {
        Write-Verbose -Message "Downloading '$( $JCGMCoreJar.name )'"
        $jarPath = "$env:TEMP\$( $JCGMCoreJar.name )"
        Invoke-WebRequest -Uri ($JCGMCoreJar.browser_download_url) -OutFile $jarPath -Verbose
        if ((Test-Path -Path $jarPath -PathType Leaf -Verbose) -and -not [string]::IsNullOrEmpty($version))
        {
            Write-Verbose -Message "Installing '$( $JCGMCoreJar.name )'"
            & mvn install:install-file -Dfile="$jarPath" -DgroupId='net.sf.jcgm.core' -DartifactId='jcgm-core' -Dversion="$version" -Dpackaging='jar'
            if ($?)
            {
                Write-Verbose -Message "Successfully installed '$( $JCGMCoreJar.name )'"
                Remove-Item -Path $jarPath -Verbose
            }
            else
            {
                Write-Error -Message "Failed to install '$( $JCGMCoreJar.name )' with ``& mvn install:install-file -Dfile="$jarPath" -DgroupId='net.sf.jcgm.core' -DartifactId='jcgm-core' -Dversion="$version" -Dpackaging='jar'``"
                exit 1
            }
        }
    }
}