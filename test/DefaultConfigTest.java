import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DefaultConfigTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        check("model length", Math.abs(ParameterList.modelLength - 238) < 1e-9);
        check("ovtol", Math.abs(ParameterList.ovtol - 0.05) < 1e-9);
        check("empty output", ParameterList.outputPath.isEmpty());
        check("empty udec", ParameterList.udecExePath.isEmpty());

        ExcavationLayer coal9 = ParameterList.excavationLayers.get(0);
        check("coal9 x", Math.abs(coal9.x1 - 100) < 1e-6
                && Math.abs(coal9.x2 - 138) < 1e-6);
        check("coal9 y", Math.abs(coal9.y1 - 21.6) < 1e-6
                && Math.abs(coal9.y2 - 26.4) < 1e-6);

        BoundaryCondition top = null;
        for (BoundaryCondition b : ParameterList.boundaries) {
            if (b.edge == BoundaryCondition.Edge.TOP) top = b;
            check("no displacement boundary",
                    b.type != BoundaryCondition.Type.DISPLACE_X
                            && b.type != BoundaryCondition.Type.DISPLACE_Y);
        }
        check("top stress", top != null && Math.abs(top.syy + 3.1525e6) < 1e-3);

        check("bolt groups", ParameterList.boltGroups.size() == 4);
        BoltGroup b1 = ParameterList.boltGroups.get(0);
        check("bolt1 x", Math.abs(b1.x1 - 100) < 1e-9 && Math.abs(b1.x2 - 108) < 1e-9);
        check("bolt3 x", Math.abs(ParameterList.boltGroups.get(2).x1 - 100) < 1e-9);
        check("bolt4 x", Math.abs(ParameterList.boltGroups.get(3).x1 - 108) < 1e-9);
        check("pline", Math.abs(ParameterList.plines.get(0).x2 - 238) < 1e-9
                && ParameterList.plines.get(0).points == 1190);

        check("stage1 title", "_block".equals(find(ParameterList.plotPresets1, "块体模型").title));
        check("stage3 title", "原岩Y方向应力云图".equals(
                find(ParameterList.plotPresets3, "Y方向应力云图").title));
        check("stage4 title", "_syy_block_cable".equals(
                find(ParameterList.plotPresets4, "Y应力+块体+锚杆").title));

        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path step1 = dir.resolve("default_step_1.txt");
        Step1.step1(step1.toString(), true);
        String text = new String(Files.readAllBytes(step1), Charset.forName("GBK"));
        check("excavation group", text.contains(
                "group openOffCut_9 range 100.00 108.00 21.60 26.40"));
        check("save file", text.contains("/dat/step_1.save"));
        check("call file", text.contains("/code/step_2.dat"));
        check("round first", text.indexOf("round 0.03") < text.indexOf("set ovtol 0.05"));

        System.out.println("DEFAULT CONFIG TEST PASSED");
    }

    private static PlotPreset find(java.util.List<PlotPreset> list, String label) {
        for (PlotPreset p : list) {
            if (label.equals(p.label)) return p;
        }
        throw new AssertionError("missing preset " + label);
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
