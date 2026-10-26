public class MeshEdgeRatioTest {
    public static void main(String[] args) {
        ParameterList.syncFromLayers();

        String[] lines = meshLines();
        check("gen edge 行数等于层数", lines.length == ParameterList.layerCount());

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            String[] parts = line.split("\\s+");
            check("第 " + i + " 行以 gen edge 开头", parts[0].equals("gen") && parts[1].equals("edge"));

            double actual = Double.parseDouble(parts[2]);
            double width = Util.layerWidthSpacing(i, spacingOf(i, 90.0));
            double height = Util.layerHeightSpacing(i, spacingOf(i, 0.0));
            double diagonal = Math.hypot(width, height);
            double expected = 0.60 * diagonal;

            check("第 " + i + " 层边长等于对角线的 60%", near(actual, expected));
            check("第 " + i + " 层边长大于半对角线", actual > 0.5 * diagonal);
            check("第 " + i + " 层边长小于对角线", actual < diagonal);
            check("第 " + i + " 层范围与层高一致",
                    line.contains(String.format("%.2f %.2f",
                            ParameterList.heightList[i + 1], ParameterList.heightList[i])));
        }

        // 用户场景：层配置里的 meshSize 是 0.25，但节理间距是 2.0，两者不同源
        int bottom = ParameterList.layerCount() - 1;
        for (JointSet js : ParameterList.layers.get(bottom).joints) {
            js.spacing = 2.0;
        }
        ParameterList.syncFromLayers();
        lines = meshLines();
        double bottomEdge = Double.parseDouble(lines[bottom].trim().split("\\s+")[2]);
        double expectBottom = 0.60 * Math.hypot(
                Util.layerWidthSpacing(bottom, 2.0), Util.layerHeightSpacing(bottom, 2.0));
        check("底层网格跟随节理间距而非 meshSize", near(bottomEdge, expectBottom));
        check("底层网格约 1.67 m", Math.abs(bottomEdge - 1.6689) < 0.01);
        check("底层不再取 meshSize 的半对角线",
                bottomEdge > 5 * 0.5 * Math.hypot(ParameterList.spacing[bottom], ParameterList.spacing[bottom]));

        System.out.println("MESH EDGE RATIO TEST PASSED (" + lines.length + " layers, bottom=" + bottomEdge + ")");
    }

    private static String[] meshLines() {
        return Util.generateMeshCommands(ParameterList.heightList, ParameterList.layerJoints,
                ParameterList.spacing, ParameterList.modelLength).split("\n");
    }

    private static double spacingOf(int layer, double angle) {
        for (JointSet js : ParameterList.layerJoints.get(layer)) {
            if (js.baseGroup && Math.abs(js.angle - angle) < 1e-6) return js.spacing;
        }
        return ParameterList.spacing[layer];
    }

    private static boolean near(double a, double b) {
        return Math.abs(a - b) < 1e-4;
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
