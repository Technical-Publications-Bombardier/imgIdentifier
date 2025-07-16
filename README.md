# 🖼️ ImageTypeIdentifier

This Java-based utility scans folders for image files and identifies their type using signature-based detection when file extensions are missing (file ends in `.xxx`).
 
## 🚀 Getting Started

### Prerequisites

- Java 8 or higher installed
- PowerShell (optional, for batch execution on Windows)

### Download

Get the latest version from the [Releases page](https://github.com/Technical-Publications-Bombardier/imgIdentifier/releases/latest).

> 📦 File to download:  
> [`imageTools-1.0-SNAPSHOT-jar-with-dependencies.jar`](https://github.com/Technical-Publications-Bombardier/imgIdentifier/releases/download/shade/imageTools-1.0-SNAPSHOT-jar-with-dependencies.jar)

## 📦 Usage

### Single Folder Execution

To scan a folder of images:

```sh
java -jar imageTools-1.0-SNAPSHOT-jar-with-dependencies.jar /path/to/images
```

### PowerShell Loop (Windows)

To process multiple `gfx` folders in bulk:

```pwsh
foreach ($gfxFolder in Get-ChildItem -Path 'O:\dev\_GLOBALMM-GOLIVE\*\gfx') {
    Write-Verbose -Message "Checking graphics in '$gfxFolder'" -Verbose
    java -jar "$env:USERPROFILE\Downloads\imageTools-1.0-SNAPSHOT-jar-with-dependencies.jar" "$gfxFolder"
}
```

## 🔍 Image Types Detected

The tool can identify the following image formats:
- **JPEG (.jpg)**
- **GIF (.gif)**
- **BMP (.bmp)**
- **PNG (.png)**
- **CGM (.cgm)**
- **EPS (.eps)**  
  _EPS detection is enhanced with known-byte signature matching for improved accuracy._

## 🧠 How It Works

- Scans all files in the specified folder
- Applies byte-level signature detection to determine image type
- Prints results to the console

## 📄 License

Internal tool—usage subject to approval. For inquiries, contact repository maintainers.
