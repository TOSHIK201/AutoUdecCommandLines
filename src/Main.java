import javax.swing.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void generateStep1() {
        ensureDirs();
        ParameterList.syncFromLayers();
        if (ParameterList.autoRunAfterGenerate) {
            ParameterList.s1Calls2 = true;
            ParameterList.s2Calls3 = true;
        }
        Step1.step1(ParameterList.outputPath + "/code/step_1.txt", ParameterList.s1Calls2);
    }

    public static void generateStep2() {
        ensureDirs();
        ParameterList.syncFromLayers();
        Step2.step2(ParameterList.outputPath + "/code/step_2.txt", ParameterList.s2Calls3);
    }

    public static void generateStep3() {
        ensureDirs();
        ParameterList.syncFromLayers();
        Step3.step3(ParameterList.outputPath + "/code/step_3.txt", ParameterList.s3Calls4);
    }

    public static void generateStep4() {
        ensureDirs();
        ParameterList.syncFromLayers();
        Step4.step4(ParameterList.outputPath + "/code/step_4.txt");
    }

    public static void generateAllFiles() {
        ensureDirs();
        ParameterList.syncFromLayers();
        if (ParameterList.autoRunAfterGenerate) {
            ParameterList.s1Calls2 = true;
            ParameterList.s2Calls3 = true;
        }
        Step1.step1(ParameterList.outputPath + "/code/step_1.txt", ParameterList.s1Calls2);
        Step2.step2(ParameterList.outputPath + "/code/step_2.txt", ParameterList.s2Calls3);
        Step3.step3(ParameterList.outputPath + "/code/step_3.txt", ParameterList.s3Calls4);
        System.out.println("生成完毕（阶段1-3）：" + ParameterList.outputPath);
    }

    public static void generateAllFilesAndRun() {
        generateAllFiles();
        autoRunUdec();
    }

    public static void autoRunUdec() {
        if (!ParameterList.autoRunAfterGenerate) return;
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

            // 清空隐藏目录里的 UDEC.INI，避免 step_1 的 new 命令反复重读 INI 造成死循环
            Files.write(runDir.resolve("UDEC.INI"), new byte[0]);

            String exeWin = exe.replace("/", "\\");
            String step1 = codeDir.resolve("step_1.txt").toString().replace("\\", "/");
            Path ps1 = runDir.resolve("_run_udec.ps1");
            String script = "$exe = '" + exeWin + "'\r\n"
                    + "$dir = '" + runDir + "'\r\n"
                    + "$step1 = '" + step1 + "'\r\n"
                    + "$ws = New-Object -ComObject WScript.Shell\r\n"
                    + "$target = $null\r\n"
                    + "$p = Start-Process -FilePath $exe -ArgumentList '2048' -WorkingDirectory $dir -PassThru\r\n"
                    + "for ($i = 0; $i -lt 20; $i++) {\r\n"
                    + "  $target = Get-Process | Where-Object { $_.MainWindowTitle -match '^UDEC\\s+\\d' } | Select-Object -First 1\r\n"
                    + "  if ($target) { break }\r\n"
                    + "  if ($p.HasExited) { break }\r\n"
                    + "  Start-Sleep -Milliseconds 500\r\n"
                    + "}\r\n"
                    + "if (-not $target) {\r\n"
                    + "  if (-not $p.HasExited) { Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue }\r\n"
                    + "  $p = Start-Process -FilePath $exe -ArgumentList '1024' -WorkingDirectory $dir -PassThru\r\n"
                    + "  for ($i = 0; $i -lt 20; $i++) {\r\n"
                    + "    $target = Get-Process | Where-Object { $_.MainWindowTitle -match '^UDEC\\s+\\d' } | Select-Object -First 1\r\n"
                    + "    if ($target) { break }\r\n"
                    + "    if ($p.HasExited) { break }\r\n"
                    + "    Start-Sleep -Milliseconds 500\r\n"
                    + "  }\r\n"
                    + "}\r\n"
                    + "if ($target) {\r\n"
                    + "  $ws.AppActivate($target.Id) | Out-Null\r\n"
                    + "  Start-Sleep -Seconds 2\r\n"
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

    private static void ensureDirs() {
        try {
            Files.createDirectories(Paths.get(ParameterList.outputPath + "/code"));
            Files.createDirectories(Paths.get(ParameterList.outputPath + "/dat"));
            Files.createDirectories(Paths.get(ParameterList.outputPath + "/output"));
        } catch (IOException e) {
            System.out.println("创建目录失败：" + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ConfigFrame().setVisible(true));
    }
}
