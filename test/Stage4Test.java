import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Stage4Test {
    public static void main(String[] args) throws Exception {
        ParameterList.syncFromLayers();
        ParameterList.boltGroups.clear();
        ParameterList.plines.clear();
        ParameterList.logGroups.clear();
        ParameterList.stage4Actions.clear();

        BoltGroup group = new BoltGroup("BOLT1");
        group.x1 = 56.5; group.y1 = 26.4; group.x2 = 63.5; group.y2 = 26.4;
        group.count = 8;
        group.length = 2.4;
        group.rebuild();
        check("bolt count", group.bolts.size() == 8);
        check("bolt upward", Math.abs(group.baseAngle - 90) < 1e-6);
        check("bolt endpoint", Math.abs(group.bolts.get(1).ex() - 57.5) < 1e-6
                && Math.abs(group.bolts.get(1).ey() - 28.8) < 1e-6);
        check("auto cable mat", group.cableMat == 11 && group.groutMat == 12);
        group.bolts.get(0).angleDeg = 165;
        group.bolts.get(0).custom = true;
        ParameterList.boltGroups.add(group);

        Pline pline = new Pline();
        pline.x1 = 0; pline.y1 = 38.75; pline.x2 = 150; pline.y2 = 38.75; pline.points = 200;
        ParameterList.plines.add(pline);

        for (PlotPreset p : ParameterList.plotPresets4) p.selected = false;
        PlotPreset hist = find("最大不平衡力");
        hist.selected = true; hist.order = 1;
        PlotPreset layered = find("Y应力+块体+锚杆");
        layered.selected = true; layered.order = 2;

        add(Stage4Action.Type.EXCAVATE, "openOffCut_9", false);
        Stage4Action material = new Stage4Action(Stage4Action.Type.MATERIAL);
        material.cableMat = 11;
        material.groutMat = 12;
        ParameterList.stage4Actions.add(material);
        add(Stage4Action.Type.BOLT, null, true);
        add(Stage4Action.Type.PLINE, null, true);
        add(Stage4Action.Type.SOLVE, null, true);
        add(Stage4Action.Type.EXCAVATE, "state_1_9", true);
        add(Stage4Action.Type.SAVE, "step_4_1", true);
        add(Stage4Action.Type.PLOT, "after_state_1_9", true);
        Stage4Action plotAction = ParameterList.stage4Actions.get(ParameterList.stage4Actions.size() - 1);
        plotAction.plotItems.add(PlotItem.from(find("最大不平衡力")));
        plotAction.plotItems.add(PlotItem.from(find("Y应力+块体+锚杆")));
        add(Stage4Action.Type.LOG, "after_state_1_9_pline1_ydis.log", true);
        Stage4Action logAction = ParameterList.stage4Actions.get(ParameterList.stage4Actions.size() - 1);
        logAction.pline = 1;
        logAction.data = "ydis";

        Path dir = Paths.get("test_out");
        Files.createDirectories(dir);
        Path out = dir.resolve("step_4_test.txt");
        Step4.step4(out.toString());
        String text = new String(Files.readAllBytes(out), Charset.forName("GBK"));
        System.out.println(text);

        check("initial opening", text.contains("delete range group openOffCut_9"));
        check("cable commands", text.split("\ncable ", -1).length - 1 == 8);
        check("angled bolt", text.contains("cable 56.5000 26.4000 54.1818 27.0212"));
        check("cable props", text.contains("prop mat 11 cb_ymod")
                && text.contains("prop mat 12 cb_kbond"));
        check("pline", text.contains("set pline 0.00 38.75 150.00 38.75 200"));
        check("state solve", text.contains("delete range group state_1_9\nsolve"));
        check("save module", text.contains("save F:/Itasca/UDECProjects/102/dat/step_4_1.dat"));
        check("layered plot", text.contains("plot syy fill block white cable blue"));
        check("plot tagged", text.contains("after_state_1_9_step_4_16.jpg"));
        check("plot title tagged", text.contains("after_state_1_9 Y"));
        check("hist plot", text.contains("plot hist 1"));
        check("log file", text.contains("after_state_1_9_pline1_ydis.log"));
        check("log print", text.contains("print pline 1 ydis"));
        check("log off", text.contains("set log off"));

        byte[] saved = ProjectIO.toBytes();
        ParameterList.boltGroups.clear();
        ParameterList.plines.clear();
        ParameterList.logGroups.clear();
        ParameterList.stage4Actions.clear();
        ProjectIO.loadBytes(saved);
        check("bolt reload", ParameterList.boltGroups.size() == 1
                && ParameterList.boltGroups.get(0).bolts.size() == 8);
        check("bolt custom reload", ParameterList.boltGroups.get(0).bolts.get(0).custom);
        check("pline reload", ParameterList.plines.size() == 1);
        check("action reload", ParameterList.stage4Actions.size() == 9);
        Stage4Action reloadedLog = null;
        for (Stage4Action a : ParameterList.stage4Actions) {
            if (a.type == Stage4Action.Type.LOG) reloadedLog = a;
        }
        check("log reload", reloadedLog != null
                && Integer.valueOf(1).equals(reloadedLog.pline)
                && "ydis".equals(reloadedLog.data));

        System.out.println("STAGE4 TESTS PASSED");
    }

    private static void add(Stage4Action.Type type, String name, boolean solve) {
        Stage4Action a = new Stage4Action(type);
        a.name = name;
        a.group = type == Stage4Action.Type.EXCAVATE ? name : null;
        a.solve = solve;
        ParameterList.stage4Actions.add(a);
    }

    private static PlotPreset find(String label) {
        for (PlotPreset p : ParameterList.plotPresets4) {
            if (label.equals(p.label)) return p;
        }
        throw new AssertionError("找不到预设 " + label);
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
