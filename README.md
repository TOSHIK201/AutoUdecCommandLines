# AutoUDEC

Java Swing tool for generating UDEC 4.0 command files for a coal-seam roadway model.

## Requirements

- Windows 10/11
- JDK 22 (`C:\Users\13978\.jdks\openjdk-22.0.1`)
- UDEC 4.0 with a valid licence

The packaged application includes a Java runtime; the target computer does not need Java.

## Build

```powershell
$jdk = "C:\Users\13978\.jdks\openjdk-22.0.1"
New-Item -ItemType Directory -Force build, dist | Out-Null
& "$jdk\bin\javac.exe" -encoding UTF-8 -d build (Get-ChildItem src\*.java).FullName
& "$jdk\bin\jar.exe" --create --file dist\AutoUDEC.jar --main-class Main -C build .
& "$jdk\bin\jpackage.exe" --type app-image --name AutoUDEC --app-version 1.0.0 `
  --input dist --main-jar AutoUDEC.jar --main-class Main --dest package `
  --java-options "-Dfile.encoding=UTF-8"
```

The result is `package\AutoUDEC\AutoUDEC.exe`. The whole `AutoUDEC` directory, including `app\` and `runtime\`, must be distributed.

## First run

- `outputPath` and `udecExePath` are empty by default. Select an output directory and the UDEC executable in the main window.
- The application creates `defaults.properties` next to the executable on first launch. Edit it to change global defaults, then restart.
- `startupProject` can point to a `.audec` project to replace layers, bolts, plines and stage-4 actions as a complete default configuration.

## Workflow

| Stage | Content | Output |
|---|---|---|
| 1 | Layer geometry, joint geometry, excavation geometry | `code\step_1.dat`, `dat\step_1.save` |
| 2 | Rock material and mesh, joint material | `code\step_2.dat`, `dat\step_2.save` |
| 3 | Boundary conditions, gravity, solve, plots | `code\step_3.dat`, `dat\step_3.save` |
| 4 | Bolts, material, plines, excavation sequence, save, plots, logs | `code\step_4.dat`, `dat\*.save` |

Stage 3 ends with `call step_4.dat`. Command files are written in GBK for UDEC 4.0.

## Project file

`.audec` format version `AUDEC10` stores layers, joints, excavations, boundaries, plot presets and stage-4 data. Versions 1–9 are readable; boundary gradients default to zero for older files.

## Tests

Compile all sources and tests, then run the test classes from the project root:

```powershell
$jdk = "C:\Users\13978\.jdks\openjdk-22.0.1"
$dest = "build_check"
New-Item -ItemType Directory -Force $dest | Out-Null
$files = @(Get-ChildItem src\*.java, test\*.java | ForEach-Object { $_.FullName })
& "$jdk\bin\javac.exe" -encoding UTF-8 -d $dest $files
$tests = "DefaultConfigTest","UiStructureTest","BoundaryGradientTest","BoundaryGenTest",
         "ExcavationTest","ChainTest","HistTest","Stage4Test","DefaultStep4Test",
         "Stage4CableDeleteTest","Stage4LabelTest","Stage4CanvasTest","BoltLayoutTest",
         "AppConfigTest","FrameSmokeTest"
foreach ($test in $tests) { & "$jdk\bin\java.exe" -cp $dest $test }
```

## Notes

- The stress boundary supports uniform stress and optional `xgrad` / `ygrad` components.
- Old `step_1~4.txt` and `dat\*.dat` files are not deleted automatically; new versions use `.dat` and `.save`.
- UDEC must be installed separately; the application does not include UDEC or a licence.
