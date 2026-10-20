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
            Files.write(codeDir.resolve("UDEC.INI"), "; auto-run uses stdin\n".getBytes(Charset.forName("GBK")));

            String exeWin = exe.replace("/", "\\");
            Path bat = codeDir.resolve("_run_udec.bat");
            String batText = "@echo off\r\n"
                    + "cd /d \"%~dp0\"\r\n"
                    + "echo call step_1.txt | \"" + exeWin + "\"\r\n"
                    + "pause\r\n";
            Files.write(bat, batText.getBytes(Charset.forName("GBK")));

            String command = "start \"\" /D \"" + codeDir + "\" \"" + bat + "\"";
            ProcessBuilder pb = new ProcessBuilder("cmd.exe", "/c", command);
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
