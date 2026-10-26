public class CoalShortcutTest {
    public static void main(String[] args) {
        ParameterList.syncFromLayers();
        int index = ParameterList.coal9Layer;
        ExcavationLayer e = new ExcavationLayer(index, "TEST");
        e.cutWidth = 8;
        e.stepDist = 10;
        e.steps = 5;
        double bottom = ParameterList.heights().get(index + 1);
        double thickness = ParameterList.layers.get(index).thickness;
        e.applyCoalSeam(ParameterList.modelLength, bottom, thickness);

        check("centered left", Math.abs(e.x1 - 90) < 1e-9);
        check("centered right", Math.abs(e.x2 - 148) < 1e-9);
        check("full seam bottom", Math.abs(e.y1 - bottom) < 1e-9);
        check("full seam top", Math.abs(e.y2 - (bottom + thickness)) < 1e-9);
        check("steps", e.stepCount() == 5);
        check("one shot", !e.autoGeometry);
        check("overall height", Math.abs(e.overallHeight - thickness) < 1e-9);

        e.steps = 3;
        e.applyCoalSeam(ParameterList.modelLength, bottom, thickness);
        check("three steps left", Math.abs(e.x1 - 100) < 1e-9);
        check("three steps right", Math.abs(e.x2 - 138) < 1e-9);
        System.out.println("COAL SHORTCUT TEST PASSED");
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
