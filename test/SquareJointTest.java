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
        check("default width spacing", near(w, 2.0));
        check("default square", Math.abs(w / h - 1) < 0.20);
        check("default region edge not thin", acceptable(38, w));
        check("default cut divides width", multiple(region.cutWidth, w));
        check("default step divides width", multiple(region.stepDist, w));
        check("start y", near(Util.excavationStartY(6), 21.6));

        region.x2 = 137;
        region.y2 = 25.6;
        h = Util.layerHeightSpacing(6, 2.0);
        w = Util.layerWidthSpacing(6, 2.0);
        check("drag row height", near(h, 2.0));
        check("drag region edge not thin", acceptable(37, w));
        check("drag square", Math.abs(w / h - 1) < 0.10);

        region.y2 = 25.3;
        h = Util.layerHeightSpacing(6, 2.0);
        w = Util.layerWidthSpacing(6, 2.0);
        check("partial row height", near(h, 1.85));
        check("partial square", Math.abs(w / h - 1) < 0.15);

        region.x2 = 139;
        region.y2 = 26.4;
        w = Util.layerWidthSpacing(6, 2.0);
        h = Util.layerHeightSpacing(6, 2.0);
        check("edge region edge not thin", acceptable(39, w));
        check("edge still square", Math.abs(w / h - 1) < 0.20);

        region.x2 = 138;
        region.y2 = 26.4;
        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path out = dir.resolve("step_1_square.txt");
        Step1.step1(out.toString(), false);
        String text = new String(Files.readAllBytes(out), Charset.forName("GBK"));
        check("width in jset", text.contains("0 2.0000 0"));
        check("row height in jset", text.contains("2.4000 0 2.4000"));
        check("start y in jset", text.contains("21.6000"));
        check("stagger offset in jset", text.contains("1.0000 24.0000"));
        check("preview builds", JointGeometry.build(5.0).size() == ParameterList.layerCount());

        System.out.println("SQUARE JOINT TEST PASSED");
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-6;
    }

    /** 余下的末端块宽不小于半列（或视同整除）即视为不产生薄块。 */
    private static boolean acceptable(double value, double spacing) {
        double r = value - Math.floor(value / spacing + 1e-9) * spacing;
        double edge = Math.min(r, spacing - r);
        return edge <= 2e-3 || edge >= spacing * 0.5 - 2e-3;
    }

    private static boolean multiple(double value, double spacing) {
        double r = value - Math.floor(value / spacing + 1e-9) * spacing;
        double near = Math.min(r, spacing - r);
        return near < 2e-3;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}