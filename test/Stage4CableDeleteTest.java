import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Stage4CableDeleteTest {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        ParameterList.boltGroups.clear();
        ParameterList.plines.clear();
        ParameterList.logGroups.clear();
        ParameterList.stage4Actions.clear();

        BoltGroup group = new BoltGroup("BOLT1");
        group.x1 = 100; group.y1 = 26.4; group.x2 = 108; group.y2 = 26.4;
        group.count = 8;
        group.length = 2.4;
        group.rebuild();
        ParameterList.boltGroups.add(group);

        add(Stage4Action.Type.EXCAVATE, "openOffCut_9", false);
        add(Stage4Action.Type.BOLT, "BOLT1", true);
        add(Stage4Action.Type.EXCAVATE, "state_1_9", true);

        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path out = dir.resolve("step_4_cable_delete.txt");
        Step4.step4(out.toString());
        String text = new String(Files.readAllBytes(out), Charset.forName("GBK"));
        System.out.println(text);

        String open = "cable delete range 100.00 108.00 21.60 26.40";
        String state = "cable delete range 108.00 118.00 21.60 26.40";
        check("openOffCut has cable delete", text.contains(open));
        check("state has cable delete", text.contains(state));
        check("cable delete before openOffCut",
                text.indexOf(open) < text.indexOf("delete range group openOffCut_9"));
        check("cable delete before state",
                text.indexOf(state) < text.indexOf("delete range group state_1_9"));
        check("openOffCut label", "9# 开切眼".equals(Stage4Geometry.label("openOffCut_9")));
        check("state label", "9# 第 1 步".equals(Stage4Geometry.label("state_1_9")));
        check("unknown group label", "custom".equals(Stage4Geometry.label("custom")));
        check("unknown group range", Stage4Geometry.range("custom") == null);

        double[] r = Stage4Geometry.range("state_2_9");
        check("state range", r != null && Math.abs(r[0] - 118) < 1e-6
                && Math.abs(r[1] - 128) < 1e-6
                && Math.abs(r[2] - 21.6) < 1e-6
                && Math.abs(r[3] - 26.4) < 1e-6);

        System.out.println("STAGE4 CABLE DELETE TEST PASSED");
    }

    private static void add(Stage4Action.Type type, String name, boolean solve) {
        Stage4Action a = new Stage4Action(type);
        a.name = name;
        if (type == Stage4Action.Type.EXCAVATE) a.group = name;
        a.solve = solve;
        ParameterList.stage4Actions.add(a);
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
