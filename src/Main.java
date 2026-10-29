import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static java.util.function.Consumer<String> errorReporter = message ->
            JOptionPane.showMessageDialog(null, message);

    public static void generateStep1() {
        generate(false, true, false, false, false);
    }

    public static void generateStep2() {
        generate(false, false, true, false, false);
    }

    public static void generateStep3() {
        generate(false, false, false, true, true);
    }

    public static void generateStep4() {
        generate(false, false, false, false, true);
    }

    public static boolean generateAllFiles() {
        return generate(true, true, true, true, true);
    }

    public static void generateAllFilesAndRun() {
        if (generateAllFiles()) autoRunUdec();
    }

    private static boolean generate(boolean all, boolean s1, boolean s2,
                                    boolean s3, boolean s4) {
        String toleranceProblem = ParameterList.edgeProblem();
        if (toleranceProblem != null) {
            errorReporter.accept(toleranceProblem);
            return false;
        }
        if (!ensureDirs()) return false;
        ParameterList.syncFromLayers();
        if ((all || s1) && ParameterList.autoRunAfterGenerate) {
            ParameterList.s1Calls2 = true;
            ParameterList.s2Calls3 = true;
        }
        Path base = Paths.get(ParameterList.outputPath.trim());
        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory(base, ".audec_tmp_");
            List<Path[]> moves = new ArrayList<>();
            if (s1) {
                Path temp = tempDir.resolve("step_1.dat");
                Step1.step1(temp.toString(), ParameterList.s1Calls2);
                moves.add(new Path[]{temp, base.resolve("code/step_1.dat")});
            }
            if (s2) {
                Path temp = tempDir.resolve("step_2.dat");
                Step2.step2(temp.toString(), ParameterList.s2Calls3);
                moves.add(new Path[]{temp, base.resolve("code/step_2.dat")});
            }
            if (s3) {
                Path temp = tempDir.resolve("step_3.dat");
                Step3.step3(temp.toString(), true);
                moves.add(new Path[]{temp, base.resolve("code/step_3.dat")});
                s4 = true;
            }
            if (s4) {
                Path temp = tempDir.resolve("step_4.dat");
                Step4.step4(temp.toString());
                moves.add(new Path[]{temp, base.resolve("code/step_4.dat")});
            }
            Path codeDir = base.resolve("code");
            Files.createDirectories(codeDir);
            for (Path[] move : moves) moveInto(move[0], move[1]);
            if (all) System.out.println("生成完毕（阶段1-4）：" + ParameterList.outputPath);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            errorReporter.accept("生成失败，code 目录未更新：\n" + e.getMessage());
            return false;
        } finally {
            deleteTree(tempDir);
        }
    }

    private static void moveInto(Path source, Path target) throws IOException {
        try {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
        } catch (java.nio.file.AtomicMoveNotSupportedException e) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteTree(Path dir) {
        if (dir == null) return;
        try (java.util.stream.Stream<Path> paths = Files.walk(dir)) {
            paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static void autoRunUdec() {
        if (!ParameterList.autoRunAfterGenerate) return;
        if (ParameterList.outputPath == null || ParameterList.outputPath.trim().isEmpty()) return;
        String exe = ParameterList.udecExePath == null ? "" : ParameterList.udecExePath.trim();
        if (exe.isEmpty() || !Files.exists(Paths.get(exe))) {
            javax.swing.JOptionPane.showMessageDialog(null, "请先设置有效的 UDEC 可执行文件路径");
            return;
        }
        try {
            Path codeDir = Paths.get(ParameterList.outputPath, "code").toAbsolutePath();
            Path runDir = Paths.get(ParameterList.outputPath, ".udec_run").toAbsolutePath();
            Files.createDirectories(codeDir);
            Files.createDirectories(runDir);
            try { Files.setAttribute(runDir, "dos:hidden", true); } catch (Exception ignored) { }

            // code 只保留生成文件；旧的辅助文件清理掉
            deleteQuietly(codeDir.resolve("UDEC.INI"));
            deleteQuietly(codeDir.resolve("_run_udec.ps1"));
            deleteQuietly(codeDir.resolve("_run_udec.bat"));
            deleteQuietly(codeDir.resolve("_run_udec.log"));
            deleteQuietly(codeDir.resolve("commandLine.txt"));

            // 不覆盖 UDEC 安装目录的 INI；删除旧的工作目录 INI，避免 new 重读造成循环
            deleteQuietly(runDir.resolve("UDEC.INI"));

            String exeWin = exe.replace("/", "\\");
            String step1 = codeDir.resolve("step_1.dat").toString().replace("\\", "/");
            Path ps1 = runDir.resolve("_run_udec.ps1");
            String script = "$exe = '" + exeWin + "'\r\n"
                    + "$dir = '" + runDir + "'\r\n"
                    + "$step1 = '" + step1 + "'\r\n"
                    + "$ws = New-Object -ComObject WScript.Shell\r\n"
                    + "$target = $null\r\n"
                    + "$p = Start-Process -FilePath $exe -ArgumentList '" + ParameterList.udecMemoryHigh + "' -WorkingDirectory $dir -PassThru\r\n"
                    + "for ($i = 0; $i -lt " + ParameterList.udecWindowPollCount + "; $i++) {\r\n"
                    + "  $target = Get-Process | Where-Object { $_.MainWindowTitle -match '^UDEC\\s+\\d' } | Select-Object -First 1\r\n"
                    + "  if ($target) { break }\r\n"
                    + "  if ($p.HasExited) { break }\r\n"
                    + "  Start-Sleep -Milliseconds " + ParameterList.udecWindowPollMs + "\r\n"
                    + "}\r\n"
                    + "if (-not $target) {\r\n"
                    + "  if (-not $p.HasExited) { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue }\r\n"
                    + "  $p = Start-Process -FilePath $exe -ArgumentList '" + ParameterList.udecMemoryLow + "' -WorkingDirectory $dir -PassThru\r\n"
                    + "  for ($i = 0; $i -lt " + ParameterList.udecWindowPollCount + "; $i++) {\r\n"
                    + "    $target = Get-Process | Where-Object { $_.MainWindowTitle -match '^UDEC\\s+\\d' } | Select-Object -First 1\r\n"
                    + "    if ($target) { break }\r\n"
                    + "    if ($p.HasExited) { break }\r\n"
                    + "    Start-Sleep -Milliseconds " + ParameterList.udecWindowPollMs + "\r\n"
                    + "  }\r\n"
                    + "}\r\n"
                    + "if ($target) {\r\n"
                    + "  $ws.AppActivate($target.Id) | Out-Null\r\n"
                    + "  Start-Sleep -Seconds " + ParameterList.udecActivateWaitSec + "\r\n"
                    + "  $ws.SendKeys(\"call $step1{ENTER}\")\r\n"
                    + "} else {\r\n"
                    + "  'UDEC window not found' | Out-File -Encoding ASCII ($dir + '\\_run_udec.log')\r\n"
                    + "}\r\n";
            Files.write(ps1, script.getBytes(java.nio.charset.StandardCharsets.US_ASCII));

            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-NoProfile",
                    "-ExecutionPolicy", "Bypass", "-WindowStyle", "Hidden", "-File", ps1.toString());
            pb.start();
            System.out.println("已在新窗口启动 UDEC（从 step_1 开始）：" + exe);
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(null, "自动调用 UDEC 失败：\n" + e.getMessage());
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
        }
    }

    private static boolean ensureDirs() {
        String base = ParameterList.outputPath == null ? "" : ParameterList.outputPath.trim();
        if (base.isEmpty()) {
            JOptionPane.showMessageDialog(null, "请先选择输出路径");
            return false;
        }
        try {
            Files.createDirectories(Paths.get(base));
            Files.createDirectories(Paths.get(base, "dat"));
            Files.createDirectories(Paths.get(base, "output"));
            return true;
        } catch (IOException e) {
            System.out.println("创建目录失败：" + e.getMessage());
            JOptionPane.showMessageDialog(null, "创建输出目录失败：\n" + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        AppConfig.ensureTemplate();
        AppConfig.apply();
        SwingUtilities.invokeLater(() -> new ConfigFrame().setVisible(true));
    }
}
