import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.SwingUtilities;

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
            Files.createDirectories(codeDir);
            // 清空旧的 UDEC.INI，避免 step_1 的 new 命令反复重读 INI 造成死循环
            Files.write(codeDir.resolve("UDEC.INI"), new byte[0]);

            String exeWin = exe.replace("/", "\\");
            String step1 = codeDir.resolve("step_1.txt").toString().replace("\\", "/");
            Path ps1 = codeDir.resolve("_run_udec.ps1");
            String script = "$exe = '" + exeWin + "'\r\n"
                    + "$dir = '" + codeDir + "'\r\n"
                    + "$step1 = '" + step1 + "'\r\n"
                    + "$p = Start-Process -FilePath $exe -WorkingDirectory $dir -PassThru\r\n"
                    + "$ws = New-Object -ComObject WScript.Shell\r\n"
                    + "$ok = $false\r\n"
                    + "for ($i = 0; $i -lt 20; $i++) {\r\n"
                    + "  if ($ws.AppActivate($p.Id)) { $ok = $true; break }\r\n"
                    + "  Start-Sleep -Milliseconds 500\r\n"
                    + "}\r\n"
                    + "if (-not $ok) { $ok = $ws.AppActivate('UDEC') }\r\n"
                    + "if ($ok) {\r\n"
                    + "  Start-Sleep -Seconds 3\r\n"
                    + "  $ws.SendKeys(\"call $step1{ENTER}\")\r\n"
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
