import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class ExcavationTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        check("default regions", ParameterList.excavationLayers.size() == 2);
        ExcavationLayer coal9 = ParameterList.excavationLayers.get(0);
        check("default rect", coal9.x2 > coal9.x1 && coal9.y2 > coal9.y1);

        List<Double> xs = Util.calculateExcavationXList(coal9);
        check("default xs", xs.size() >= 2);
        System.out.println("9# xs = " + xs);

        ExcavationLayer region = new ExcavationLayer("测试", 20, 10, 60, 14);
        region.cutWidth = 8;
        region.stepDist = 10;
        region.direction = Util.Direction.LEFT_TO_RIGHT;
        check("auto steps", region.stepCount() == 4);
        ParameterList.excavationLayers.add(region);

        check("snap x corners", SnapTargets.xTargets(12, 0.5).contains(20.0)
                && SnapTargets.xTargets(12, 0.5).contains(60.0));
        check("snap y corners", SnapTargets.yTargets(30, 0.5).contains(10.0)
                && SnapTargets.yTargets(30, 0.5).contains(14.0));
        check("snap corner", SnapTargets.nearest(59.9, SnapTargets.xTargets(14, 0.5), 0.5) == 60.0
                && SnapTargets.nearest(14.1, SnapTargets.yTargets(60, 0.5), 0.5) == 14.0);

        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path step1 = dir.resolve("step_1_exc.txt");
        Step1.step1(step1.toString(), false);
        String text = new String(Files.readAllBytes(step1), Charset.forName("GBK"));
        check("independent vertical crack", text.contains("crack 20.00 10.00 20.00 14.00"));
        check("independent group", text.contains("group openOffCut_测试 range 20.00 28.00 10.00 14.00"));
        check("independent last group", text.contains("60.00 14.00"));

        byte[] saved = ProjectIO.toBytes();
        ParameterList.excavationLayers.remove(region);
        ProjectIO.loadBytes(saved);
        check("region reload", ParameterList.excavationLayers.size() == 3);
        ExcavationLayer loaded = ParameterList.excavationLayers.get(2);
        check("rect reload", Math.abs(loaded.x1 - 20) < 1e-9 && Math.abs(loaded.x2 - 60) < 1e-9);
        check("autoGeometry reload", !loaded.autoGeometry);

        System.out.println("EXCAVATION TESTS PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
