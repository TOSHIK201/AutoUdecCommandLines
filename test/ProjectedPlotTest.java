import java.util.List;

public class ProjectedPlotTest {
    public static void main(String[] args) {
        List<PlotPreset> stage3 = PlotPresets.stage3();
        check("syy", command(stage3, "Y方向应力云图")
                .equals("plot block syy fill projected"));
        check("sxx", command(stage3, "X方向应力云图")
                .equals("plot block sxx fill projected"));
        check("sxy", command(stage3, "剪应力云图")
                .equals("plot block sxy fill projected"));
        check("sig1", command(stage3, "最大主应力云图")
                .equals("plot block sig1 projected"));
        check("sig2", command(stage3, "最小主应力云图")
                .equals("plot block sig2 projected"));
        check("displacement untouched",
                !command(stage3, "Y方向位移云图").contains("projected"));

        List<PlotPreset> stage4 = PlotPresets.stage4();
        check("layered stress", command(stage4, "Y应力+块体+锚杆")
                .equals("plot syy fill projected block white cable blue"));
        check("layered displacement untouched",
                !command(stage4, "Y位移+块体+锚杆").contains("projected"));
        System.out.println("PROJECTED PLOT TEST PASSED");
    }

    private static String command(List<PlotPreset> list, String label) {
        for (PlotPreset preset : list) {
            if (preset.label.equals(label)) return preset.plotCommand;
        }
        throw new AssertionError("missing preset " + label);
    }

    private static void check(String name, boolean ok) {
        if (!ok) throw new AssertionError("FAILED: " + name);
    }
}
