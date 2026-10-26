import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class SquareJointTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        for (JointSet js : ParameterList.layers.get(6).joints) {
            if (js.baseGroup) js.spacing = 2.0;
        }
        ExcavationLayer region = ParameterList.excavationLayers.get(0);
        region.autoGeometry = false;
        region.x1 = 100; region.x2 = 138;
        region.y1 = 21.6; region.y2 = 26.4;

        double h = Util.layerHeightSpacing(6, 2.0);
        double w = Util.layerWidthSpacing(6, 2.0);
        check("default row height", near(h, 2.4));
        check("default width spacing", near(w, 238.0 / 99.0));
        check("default square", Math.abs(w / h - 1) < 0.02);
        check("start y", near(Util.excavationStartY(6), 21.6));

        region.x2 = 137;
        region.y2 = 25.6;
        h = Util.layerHeightSpacing(6, 2.0);
        w = Util.layerWidthSpacing(6, 2.0);
        check("drag row height", near(h, 2.0));
        check("drag width spacing", near(w, 2.0));
        check("drag square", Math.abs(w / h - 1) < 0.02);

        region.y2 = 25.3;
        h = Util.layerHeightSpacing(6, 2.0);
        w = Util.layerWidthSpacing(6, 2.0);
        check("partial row height", near(h, 1.85));
        check("partial square", Math.abs(w / h - 1) < 0.02);

        region.x2 = 139;
        region.y2 = 26.4;
        w = Util.layerWidthSpacing(6, 2.0);
        h = Util.layerHeightSpacing(6, 2.0);
        double remainder = 39.0 % w;
        check("edge block not thin", remainder > w * 0.5);
        check("edge still square", Math.abs(w / h - 1) < 0.05);

        region.x2 = 138;
        region.y2 = 26.4;
        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path out = dir.resolve("step_1_square.txt");
        Step1.step1(out.toString(), false);
        String text = new String(Files.readAllBytes(out), Charset.forName("GBK"));
        check("square width in jset", text.contains("2.4040"));
        check("row height in jset", text.contains("2.4000"));
        check("start y in jset", text.contains("21.6000"));
        check("preview builds", JointGeometry.build(5.0).size() == ParameterList.layerCount());

        System.out.println("SQUARE JOINT TEST PASSED");
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
