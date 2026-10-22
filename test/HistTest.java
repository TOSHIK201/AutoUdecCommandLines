import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HistTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        for (PlotPreset preset : ParameterList.plotPresets3) {
            preset.selected = false;
            preset.order = 0;
        }
        PlotPreset vmax = find("最大速度");
        PlotPreset unbal = find("最大不平衡力");
        vmax.selected = true;
        vmax.order = 1;
        unbal.selected = true;
        unbal.order = 2;

        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path step3 = dir.resolve("step_3_hist.txt");
        Step3.step3(step3.toString(), false);
        String text = new String(Files.readAllBytes(step3), Charset.forName("GBK"));
        System.out.println(text);

        int reset = text.indexOf("reset hist");
        int hVmax = text.indexOf("hist vmax");
        int hUnbal = text.indexOf("hist unbal");
        int solve = text.indexOf("\nsolve");
        check("reset before hist", reset >= 0 && reset < hVmax);
        check("selection order", hVmax < hUnbal);
        check("hist before solve", hUnbal < solve);
        check("plot index 1", text.contains("最大速度\nplot hist 1"));
        check("plot index 2", text.contains("最大不平衡力\nplot hist 2"));
        check("no solve_ratio", !text.contains("hist solve_ratio"));

        vmax.console = true;
        Step3.step3(step3.toString(), false);
        text = new String(Files.readAllBytes(step3), Charset.forName("GBK"));
        check("console vmax", text.contains("hist vmax type 1"));
        check("no console unbal", !text.contains("hist unbal type"));

        vmax.console = false;
        unbal.console = true;
        Step3.step3(step3.toString(), false);
        text = new String(Files.readAllBytes(step3), Charset.forName("GBK"));
        check("console unbal", text.contains("hist unbal type 2"));
        check("no console vmax", !text.contains("hist vmax type"));

        byte[] saved = ProjectIO.toBytes();
        ParameterList.plotPresets3.forEach(p -> { p.order = 0; p.selected = false; });
        ProjectIO.loadBytes(saved);
        PlotPreset reloaded = find("最大速度");
        check("order reload", reloaded.selected && reloaded.order == 1);
        check("console reload", find("最大不平衡力").console);
        System.out.println("HIST TESTS PASSED");
    }

    private static PlotPreset find(String label) {
        for (PlotPreset preset : ParameterList.plotPresets3) {
            if (label.equals(preset.label)) return preset;
        }
        throw new AssertionError("找不到预设：" + label);
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
