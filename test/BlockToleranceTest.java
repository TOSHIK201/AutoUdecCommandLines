import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

// 圆角、最小块体边、重叠容差三者的一致性：默认取 2×round，非法组合拦截生成
public class BlockToleranceTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        Path dir = Files.createTempDirectory("audec_tol_");
        ParameterList.outputPath = dir.toString().replace('\\', '/');

        // 1) 未指定最小块体边时按手册默认取 2×圆角
        ParameterList.round = 0.03;
        ParameterList.edge = 0;
        check("auto edge = 2 x round", near(ParameterList.effectiveEdge(), 0.06));
        check("auto edge needs no warning", ParameterList.edgeProblem() == null);

        // 2) 手动设置小于 2×round → 判定为问题
        ParameterList.edge = 0.05;
        String problem = ParameterList.edgeProblem();
        check("too small edge detected", problem != null);
        check("problem names the limit", problem.contains("0.06"));

        // 3) 合法的自定义值
        ParameterList.edge = 0.1;
        check("larger edge accepted", ParameterList.edgeProblem() == null);

        // 4) 重叠容差超默认值只提示
        ParameterList.edge = 0;
        ParameterList.ovtol = 0.15;
        check("loose ovtol warned", ParameterList.ovtolWarning() != null);
        ParameterList.ovtol = 0.01;
        check("tight ovtol accepted", ParameterList.ovtolWarning() == null);

        // 5) step_1 写出 set edge，且在 block 之前
        Path file = Paths.get(dir.toString(), "step_1.dat");
        Step1.step1(file.toString(), false);
        String text = read(file);
        check("auto edge written", text.contains("set edge 0.06"));
        int edgeAt = text.indexOf("set edge");
        int blockAt = text.indexOf("block ");
        check("edge line precedes block", edgeAt >= 0 && blockAt > edgeAt);
        check("ovtol still written", text.contains("set ovtol"));

        ParameterList.edge = 0.1;
        Step1.step1(file.toString(), false);
        check("explicit edge written", read(file).contains("set edge 0.1"));

        // 6) 工程文件往返保留该值
        ParameterList.edge = 0.08;
        byte[] bytes = ProjectIO.toBytes();
        ParameterList.edge = 0;
        ProjectIO.loadBytes(bytes);
        check("edge survives project round trip", near(ParameterList.edge, 0.08));

        // 7) 非法组合不生成命令文件
        List<String> messages = new ArrayList<>();
        Main.errorReporter = messages::add;
        ParameterList.round = 0.03;
        ParameterList.edge = 0.05;
        check("generation blocked", !Main.generateAllFiles());
        check("blocked with one message", messages.size() == 1
                && messages.get(0).contains("0.06"));

        System.out.println("BLOCK TOLERANCE TEST PASSED");
    }

    private static String read(Path file) throws Exception {
        return new String(Files.readAllBytes(file), Charset.forName("GBK"));
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-9;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
