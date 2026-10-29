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
| 1 | Layer geometry, joint geometry, excavation geometry | `code\step_1.dat`, `dat\step_1.sav` |
| 2 | Rock material and mesh, joint material | `code\step_2.dat`, `dat\step_2.sav` |
| 3 | Boundary conditions, gravity, solve, plots | `code\step_3.dat`, `dat\step_3.sav` |
| 4 | Bolts, material, plines, excavation sequence, save, plots, logs | `code\step_4.dat`, `dat\*.sav` |

Stage 3 ends with `call step_4.dat`. Command files are written in GBK for UDEC 4.0.

All paths embedded in generated command files are normalized to forward slashes (`/`); backslashes, duplicate separators and trailing separators in the output root are removed before writing.

Stress-contour presets add the `projected` switch. UDEC projects zone stresses to gridpoints before building the contour, which allows the contour to reach the model edge; it is the default stress-contour construction method.

Generated command files are first written to a temporary directory under the output folder. `code\step_1~4.dat` is created or replaced only after every selected stage succeeds; a failure keeps the previous successful files.

Boundary conditions use axis-aligned UDEC ranges. A diagonal input line is drawn in the preview, but the applied range is a vertical or horizontal strip along its dominant direction; the UI labels such conditions as `条带近似`.

## Project file

`.audec` format version `AUDEC12` stores layers, joints, excavations, boundaries, plot presets and stage-4 data. Versions 1–11 are readable; boundary gradients default to zero, bolt groups default to absolute positions and the minimum block edge defaults to twice the rounding length for older files.

Bolt groups can be bound to an excavation layer's open-off cut (`开切眼`); the stored coordinates are then offsets from the cut's start bottom corner and follow later excavation edits. The excavation form's `开挖煤层` shortcut centres the region with the entered cut width, step distance and step count, and takes the full seam thickness in one application.

The stage-4 excavation dropdown is rebuilt from the current excavation layers whenever stage 4 is opened, so changes to step count, cut width or region name appear without restarting the application.

Deleting an excavation region (from the excavation list or by deleting its rock layer) also removes the stage-4 excavation actions that point at that region's groups; the deletion is one undo step like any other.

Stage pages are built on first use. Startup constructs only the active stage, so the first click on 阶段2/3/4 costs a few tens of milliseconds while later visits are instant.

`step_1.dat` now writes `set edge` before `BLOCK`: the value is twice the rounding length unless the "最小块体边" field overrides it. UDEC requires `emin >= 2 × round`, so generation is refused when the entered value is smaller, and the status bar warns when `ovtol` exceeds half the rounding length. Projects store the field from `AUDEC12` on; older files load with the automatic value.

Layers crossed by an excavation use a square-adaptive joint grid: row height follows the excavation's overlap height, column width is the nearest model-length divisor, and the grid phase follows the excavation's lower-left corner. Dragging any region corner updates the grid on the next repaint.

Mesh size (`gen edge`) is 60% of the joint cell diagonal, not half of it. The value sits above the half-diagonal and below the diagonal, which forces UDEC to split each cell along both diagonals into four triangular zones instead of leaving a single poorly shaped quad. The ratio is `Util.MESH_EDGE_DIAGONAL_RATIO`.

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
- Old `step_1~4.txt`, `dat\*.dat` and `dat\*.save` files are not deleted automatically; new versions use `.dat` for commands and `.sav` for saves. The save-name field accepts `.sav`, `.save` or `.dat` input and always writes `.sav`.
- UDEC must be installed separately; the application does not include UDEC or a licence.
