$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH

$mavenDir = "C:\Users\gatos\AppData\Local\Apache\apache-maven-3.9.6"
$mvnBin = "$mavenDir\bin\mvn.cmd"

if (!(Test-Path $mvnBin)) {
    Write-Host "Descargando Apache Maven..."
    $url = "https://archive.apache.org/dist/maven/maven-3/3.9.6/binaries/apache-maven-3.9.6-bin.zip"
    $zip = "C:\Users\gatos\AppData\Local\Temp\maven.zip"
    $out = "C:\Users\gatos\AppData\Local\Apache"
    Invoke-WebRequest -Uri $url -OutFile $zip
    Expand-Archive -Path $zip -DestinationPath $out -Force
}

Write-Host "Iniciando servidor Spring Boot con JDK de Android Studio..."
& $mvnBin spring-boot:run
