import java.util.ArrayList;
import java.util.List;

class PlotPresets {
    // 标签、绘图命令、历史记录命令（不需要 hist 时为 null）
    private static final String[][] CATALOG = {
            {"最大不平衡力", "plot hist", "hist unbal"},
            {"最大速度", "plot hist", "hist vmax"},
            {"Y方向位移云图", "plot block ydis fill", null},
            {"Y方向应力云图", "plot block syy fill", null},
            {"块体模型", "plot block", null},
            {"单元体模型图", "plot zone", null},
            {"材料分布图", "plot block mat", null},
            {"X方向位移云图", "plot block xdis fill", null},
            {"X方向应力云图", "plot block sxx fill", null},
            {"剪应力云图", "plot block sxy fill", null},
            {"最大主应力云图", "plot block sig1", null},
            {"最小主应力云图", "plot block sig2", null},
            {"塑性区图", "plot block plastic", null},
            {"运算速率", "plot hist", "hist solve_ratio"}
    };

    static List<PlotPreset> stage1() {
        return build(1, 5);
    }

    static List<PlotPreset> stage2() {
        return build(2, 6, 7);
    }

    static List<PlotPreset> stage3() {
        return build(3, 1, 3, 4);
    }

    static List<PlotPreset> stage4() {
        List<PlotPreset> list = build(4);
        list.add(new PlotPreset("块体+锚杆", "plot block white cable blue",
                false, "块体+锚杆", "step_4_15.jpg"));
        list.add(new PlotPreset("Y应力+块体+锚杆", "plot syy fill block white cable blue",
                false, "Y应力+块体+锚杆", "step_4_16.jpg"));
        list.add(new PlotPreset("Y位移+块体+锚杆", "plot ydis fill block white cable blue",
                false, "Y位移+块体+锚杆", "step_4_17.jpg"));
        return list;
    }

    // 三个阶段使用同一份预设目录，只替换输出文件名前缀与默认勾选。
    private static List<PlotPreset> build(int stage, int... selected) {
        List<PlotPreset> list = new ArrayList<>();
        int order = 0;
        for (int i = 0; i < CATALOG.length; i++) {
            boolean on = false;
            for (int s : selected) {
                if (s == i + 1) {
                    on = true;
                    break;
                }
            }
            String label = CATALOG[i][0];
            PlotPreset preset = new PlotPreset(label, CATALOG[i][1], CATALOG[i][2], on,
                    label, "step_" + stage + "_" + (i + 1) + ".jpg");
            preset.order = on ? ++order : 0;
            list.add(preset);
        }
        return list;
    }
}
