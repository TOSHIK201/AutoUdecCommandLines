import java.util.ArrayList;
import java.util.List;

class PlotPresets {
    // 标签、绘图命令、历史记录命令（不需要 hist 时为 null）
    private static final String[][] CATALOG = {
            {"最大不平衡力", "plot hist", "hist unbal"},
            {"最大速度", "plot hist", "hist vmax"},
            {"Y方向位移云图", "plot block ydis fill", null},
            {"Y方向应力云图", "plot block syy fill projected", null},
            {"块体模型", "plot block", null},
            {"单元体模型图", "plot zone", null},
            {"材料分布图", "plot block mat", null},
            {"X方向位移云图", "plot block xdis fill", null},
            {"X方向应力云图", "plot block sxx fill projected", null},
            {"剪应力云图", "plot block sxy fill projected", null},
            {"最大主应力云图", "plot block sig1 projected", null},
            {"最小主应力云图", "plot block sig2 projected", null},
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
                false, "_block_cable", "step_4_15.jpg"));
        list.add(new PlotPreset("Y应力+块体+锚杆", "plot syy fill projected block white cable blue",
                false, "_syy_block_cable", "step_4_16.jpg"));
        list.add(new PlotPreset("Y位移+块体+锚杆", "plot ydis fill block white cable blue",
                false, "_ydis_block_cable", "step_4_17.jpg"));
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
                    titleFor(stage, label), "step_" + stage + "_" + (i + 1) + ".jpg");
            preset.order = on ? ++order : 0;
            list.add(preset);
        }
        return list;
    }

    private static String titleFor(int stage, String label) {
        if (stage == 3) return "原岩" + label;
        return tag(label);
    }

    private static String tag(String label) {
        switch (label) {
            case "最大不平衡力": return "_unbal";
            case "最大速度": return "_vmax";
            case "Y方向位移云图": return "_ydis";
            case "Y方向应力云图": return "_syy";
            case "块体模型": return "_block";
            case "单元体模型图": return "_zone";
            case "材料分布图": return "_mat";
            case "X方向位移云图": return "_xdis";
            case "X方向应力云图": return "_sxx";
            case "剪应力云图": return "_sxy";
            case "最大主应力云图": return "_sig1";
            case "最小主应力云图": return "_sig2";
            case "塑性区图": return "_plastic";
            case "运算速率": return "_ratio";
            default: return "_plot";
        }
    }
}
